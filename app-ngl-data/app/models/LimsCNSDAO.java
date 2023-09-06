package models;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.utils.Iterables;
import models.laboratory.common.description.Level;
import models.laboratory.common.instance.Comment;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TBoolean;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.common.instance.TransientState;
import models.laboratory.common.instance.Valuation;
import models.laboratory.common.instance.property.PropertyObjectValue;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.Content;
import models.laboratory.parameter.index.IlluminaIndex;
import models.laboratory.parameter.index.Index;
import models.laboratory.project.instance.BioinformaticParameters;
import models.laboratory.project.instance.Project;
import models.laboratory.project.instance.UmbrellaProject;
import models.laboratory.run.description.ReadSetType;
import models.laboratory.run.description.TreatmentType;
import models.laboratory.run.instance.File;
import models.laboratory.run.instance.InstrumentUsed;
import models.laboratory.run.instance.Lane;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.run.instance.Run;
import models.laboratory.run.instance.Treatment;
import models.laboratory.sample.description.ImportType;
import models.laboratory.sample.description.SampleType;
import models.laboratory.sample.instance.Sample;
import models.util.DataMappingCNS;
import models.util.MappingHelper;
import models.utils.CodeHelper;
import models.utils.InstanceConstants;
import models.utils.dao.DAOException;
import play.Logger;
import play.api.modules.spring.Spring;
import services.instance.container.ContainerImportCNS;
import validation.ContextValidation;

/**
 * @author mhaquell
 *
 */
@Repository
public class LimsCNSDAO {

	private static final play.Logger.ALogger logger = play.Logger.of(LimsCNSDAO.class);

	private static final Object[] NO_ARG = {};
	
	public    static final String LIMS_CODE                   = "limsCode";
	public    static final String SAMPLE_ADPATER              = "isAdapters";
	protected static final String PROJECT_CATEGORY_CODE       = "default";
	protected static final String PROJECT_TYPE_CODE_FG        = "france-genomique";
	protected static final String PROJECT_TYPE_CODE_DEFAULT   = "default-project";

	protected static final String UMBRELLA_PROJECT_CATEGORY_CODE       = "default";
	protected static final String UMBRELLA_PROJECT_TYPE_CODE_DEFAULT   = "default-umbrella-project";
	
	private static final String PROJECT_STATE_CODE_IP = "IP";
	private static final String PROJECT_STATE_CODE_F   = "F";
	//protected static final String PROJECT_PROPERTIES_FG_GROUP = "fgGroup";
	// EJACOBY: AD
	protected static final String PROJECT_PROPERTIES_UNIX_GROUP = "unixGroup";
	protected static final String PROJECT_PROPERTIES_SYNCHRO_PROJ = "synchroProj";
	protected static final String QTREE_QUOTA = "qtreeQuota";
	protected static final String NGSRG_CODE                  = "ngsrg";
	protected static final String GLOBAL_CODE                 = "global";
	protected static final String IMPORT_CATEGORY_CODE        = "sample-import";
	protected static final String RUN_TYPE_CODE               = "ngsrg-illumina";
	protected static final String READSET_DEFAULT_CODE        = "default-readset";
	protected static final String UNIX_GROUP_DEFAULT		  = "g-extprj";

	public JdbcTemplate jdbcTemplate;

	@Autowired
	@Qualifier("lims")
	public void setDataSource(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);              
	}

	/**
	 * Create containers for an SQL query ran against 'lims' database.
	 * @param procedure             SQL query to run
	 * @param contextError          validation context
	 * @param containerCategoryCode container category code
	 * @param containerStateCode    container state code
	 * @param experimentTypeCode    experiment type code
	 * @return                      container instances
	 */
	// Find Tube LIMS who have flag 'tubinNGL=0' ( this flag is update to 1 when Tube exists in NGL database).
	public List<Container> findContainersToCreate(String            procedure, 
			ContextValidation contextError, 
			final String      containerCategoryCode, 
			final String      containerStateCode, 
			final String      experimentTypeCode) {
		return jdbcTemplate.query(procedure, NO_ARG, 
				(rs, rowNum) -> {
					try {
						return ContainerImportCNS.createContainerFromResultSet(rs, containerCategoryCode,containerStateCode,experimentTypeCode);
					} catch (DAOException e) {
						logger.error("",e);
						return null;
					}
				});        
	}

	public Sample findSampleToCreate(final ContextValidation contextError, String sampleCode) throws SQLException, DAOException {

		List<Sample> results = jdbcTemplate.query("pl_MaterielToNGLUn @nom_materiel=?", new Object[] { sampleCode },
				(rs, rowNum) -> {
					Sample sample = new Sample();
					Date creationDate = rs.getDate("uaddc");
					if (creationDate != null) {
						sample.getTraceInformation().forceCreationStamp(contextError.getUser(), creationDate);				
					} else {
						sample.setTraceCreationStamp(contextError, contextError.getUser());
					}

					Date modificationDate = rs.getDate("uaddm");
					if (modificationDate != null) {
						sample.getTraceInformation().forceModificationStamp(contextError.getUser(), modificationDate);				
					} else {
						sample.setTraceModificationStamp(contextError, contextError.getUser());
					}
					//InstanceHelpers.updateTraceInformation(sample.traceInformation, contextError.getUser());
					String tadco = rs.getString("tadco");
					String tprco = rs.getString("tprco");
					sample.code = rs.getString("code");

					logger.debug("Code Materiel (adnco) :"+rs.getString(LIMS_CODE)+" , Type Materiel (tadco) :"+tadco +", Type Projet (tprco) :"+tprco);

					String sampleTypeCode = DataMappingCNS.getSampleTypeFromLims(tadco,tprco);

					if(sampleTypeCode==null){
						contextError.addError( "typeCode", "limsdao.error.emptymapping", tadco, sample.code);
						return null;
					}

					SampleType sampleType=null;
					try {
						sampleType = SampleType.find.get().findByCode(sampleTypeCode);
					} catch (DAOException e) {
						logger.error("",e);
						return null;
					}


					if (sampleType == null) {
						contextError.addError("code", "error.codeNotExist", sampleTypeCode, sample.code);
						return null;
					}

					// Logger.debug("Sample Type : "+sampleTypeCode);
					logger.debug("Sample Type : {}", sampleTypeCode);

					sample.typeCode        = sampleTypeCode;
					sample.projectCodes    = new HashSet<>();
					sample.projectCodes.add(rs.getString("project"));
					sample.name            = rs.getString("name");
					sample.referenceCollab = rs.getString("referenceCollab");
					sample.taxonCode       = rs.getString("taxonCode");
					sample.comments        = new ArrayList<>();
					sample.comments.add(new Comment(rs.getString("comment"), "ngl-test"));
					sample.categoryCode    = sampleType.category.code;
					sample.properties      = new HashMap<>();


					boolean tara = false;
					if (rs.getInt("tara") == 1) {
						tara = true;
					}
					boolean adapter = rs.getBoolean("isAdapters");
					sample.importTypeCode=DataMappingCNS.getImportTypeCode(tara,adapter);
					ImportType importType = null;
					try {
						importType = ImportType.find.get().findByCode(sample.importTypeCode);
					} catch (DAOException e) {
						logger.error("",e);
						return null;
					}


					MappingHelper.getPropertiesFromResultSet(rs,importType.propertiesDefinitions,sample.properties);

					//Logger.debug("Properties sample "+sample.properties.containsKey("taxonSize"));



					if (tara) {

						// Logger.debug("Tara sample "+sample.code);
						logger.debug("Tara sample {}", sample.code);

						TaraDAO  taraServices = Spring.getBeanOfType(TaraDAO.class);
						if(sample.properties==null){ sample.properties=new HashMap<>();}

						Map<String, PropertyValue> map = taraServices.findTaraSampleFromLimsCode(rs.getInt(LIMS_CODE),contextError);

						if (map != null) {
							sample.properties.putAll(map);
						} else {
							tara = false;
						}

					}
					//Logger.debug("Adpatateur :"+sample.properties.get("adaptateur").value.toString());
					//Logger.debug("Import Type "+sample.importTypeCode);
					return sample;
				});        
		if (results.size() == 1)
			return results.get(0);
		return null;
	}

	public List<UmbrellaProject> findUmbrellaProjectToCreate(final ContextValidation contextError) {
		List<UmbrellaProject> result = jdbcTemplate.query("SELECT * FROM Projetini", NO_ARG,
						(resulSet, rowNumber) -> {
							UmbrellaProject umbrellaProject = new UmbrellaProject();
							umbrellaProject.code = CodeHelper.getInstance().generateUmbrellaProjectCode();
							umbrellaProject.typeCode = UMBRELLA_PROJECT_TYPE_CODE_DEFAULT;
							umbrellaProject.categoryCode = UMBRELLA_PROJECT_CATEGORY_CODE;

							umbrellaProject.name = resulSet.getString("priabr").trim();

							String commProjetInit = resulSet.getString("pricom");

							int codeProjet = resulSet.getInt("prico");

							if (codeProjet != 0) {
								jdbcTemplate.query("SELECT * FROM Projetgeneral p, Projetini pi WHERE p.prgco = pi.prgco AND pi.prico = ?", new Object[] { codeProjet },
									(resulSetCommProj, rowNumberCommProj) -> {
										String commProjet = resulSetCommProj.getString("prgnoml");

										umbrellaProject.description = "Commentaire projet initalisé : " + ((commProjetInit == null || commProjetInit.equals("NULL")) ? "" : commProjetInit) + " ; " +
															"Nom projet général : " + resulSetCommProj.getString("prgnom") + " ; " +
															"Description projet général : " + ((commProjet == null || commProjet.equals("NULL")) ? "" : commProjet);

										return null;
									});
							} else {
								Logger.error("Projet non trouvé pour le projet initialisé '" + codeProjet + "'");
							}
							

							TraceInformation ti = new TraceInformation(Constants.NGL_DATA_USER);

							Date dateCreation = new Date(); // Par défaut à aujourd'hui, ça permet que si une erreur arrive on n'a pas une date null.
							String dateBaseLims = resulSet.getString("priddp");

							try {
								dateCreation = new SimpleDateFormat("yyyy-MM-dd").parse(dateBaseLims);
							} catch (ParseException e) {
								e.printStackTrace();
							}

							ti.creationDate = dateCreation;

							Date dateModification = new Date(); // Par défaut à aujourd'hui, ça permet que si une erreur arrive on n'a pas une date null.
							String dateModifBaseLims = resulSet.getString("pridm");

							try {
								dateModification = new SimpleDateFormat("yyyy-MM-dd").parse(dateModifBaseLims);
							} catch (ParseException e) {
								e.printStackTrace();
							}

							ti.modifyDate = dateModification;

							int idUserModify = resulSet.getInt("percom");

							if (idUserModify != 0) {
								jdbcTemplate.query("SELECT * FROM Perint WHERE perco = ?", new Object[] { idUserModify },
									(resulSetUserModify, rowNumberUserModify) -> {
										String userModify = resulSetUserModify.getString("perlog");

										ti.modifyUser = userModify;

										return null;
									}); 
							} else {
								ti.modifyUser = Constants.NGL_DATA_USER;
							}							

							umbrellaProject.traceInformation = ti;

							int umbrellaCode = resulSet.getInt("prico");
							umbrellaProject.properties.put("limsCode", new PropertySingleValue(umbrellaCode));

							int codeApplicant = resulSet.getInt("colnum");

							if (codeApplicant != 0) {
								umbrellaProject.properties.put("applicantId", new PropertySingleValue(codeApplicant));

								jdbcTemplate.query("SELECT * FROM Collab WHERE colnum = ?", new Object[] { codeApplicant },
									(resulSetDemandeur, rowNumberRespInf) -> {
										String demandeur = resulSetDemandeur.getString("colNom");

										umbrellaProject.properties.put("applicant", new PropertySingleValue(demandeur));

										return null;
									});
							} else {
								Logger.error("Demandeur non trouvé pour le projet initialisé '" + codeProjet + "'");
							}

							return umbrellaProject;
						});
		return result;
	}

	// pl_ProjetToNGL=> Liste des ss-projets de sequencage issus du Lims
	// retour: prsnom	prsco	groupefg	prsarch	maxadnnom	groupenom
	//Corr proc 29/06 pour retourner le code état du code projet. esprjco:25 => état terminé
	//retour: prsnom	prsco	groupefg	prsarch	maxadnnom	groupenom esprjco
	public List<Project> findProjectToCreate(final ContextValidation contextError) throws SQLException, DAOException {
		List<Project> result = jdbcTemplate.query("pl_ProjetToNGL ", NO_ARG,
				(rs, rowNum) -> { 
					Project project = new Project();
					project.code = rs.getString(2).trim();
					project.name = rs.getString(1);
					
					project.properties = new HashMap<>();
					project.bioinformaticParameters = new BioinformaticParameters();

					String fgGroupe = rs.getString("groupefg");
					if (fgGroupe==null) {
						project.typeCode = PROJECT_TYPE_CODE_DEFAULT;
					} else {
						project.typeCode = PROJECT_TYPE_CODE_FG;
						project.bioinformaticParameters.fgGroup = fgGroupe;
						if(StringUtils.isNotBlank(fgGroupe))
							project.bioinformaticParameters.ccrtAutomaticTransfer=true;
					}

					String unixGroup = rs.getString("groupenom");
					if (unixGroup == null) {
						project.properties.put(PROJECT_PROPERTIES_UNIX_GROUP, new PropertySingleValue(UNIX_GROUP_DEFAULT));
					} else {
						project.properties.put(PROJECT_PROPERTIES_UNIX_GROUP, new PropertySingleValue(unixGroup));
					}

					project.properties.put(PROJECT_PROPERTIES_SYNCHRO_PROJ, new PropertySingleValue(Boolean.TRUE));
					
					Map<String, Object> map = new HashMap<>();
					map.put("proj", 0);
					map.put("rawdata", 0);
					map.put("scratch", 100);

					project.properties.put(QTREE_QUOTA, new PropertyObjectValue(map));

					project.categoryCode = PROJECT_CATEGORY_CODE;

					String stateCode=rs.getString(7);
					if (stateCode.equals("25")) {
						project.state = new State(PROJECT_STATE_CODE_F,Constants.NGL_DATA_USER);
					}else {
						project.state = new State(); 
						project.state.code = PROJECT_STATE_CODE_IP;
						project.state.user = contextError.getUser();
						project.state.date = new Date();
					}

					if (rs.getString("maxadnnom") != null) {
						project.lastSampleCode = project.code + "_" + rs.getString("maxadnnom");
						project.nbCharactersInSampleCode = rs.getString("maxadnnom").length();
					}

					project.traceInformation = new TraceInformation();
					project.traceInformation.forceCreationStamp(contextError.getUser());

					return project;
				});
		
		result.forEach(project -> {
			jdbcTemplate.query("SELECT * FROM Projet p, Projetini pi WHERE p.prsco = ? AND p.prico = pi.prico", new Object[] { project.code },
						(resulSet, rowNumber) -> {
							// Umbrella project
							int umbrellaCode = resulSet.getInt("prico");
							UmbrellaProject umbrellaProject = MongoDBDAO.find(InstanceConstants.UMBRELLA_PROJECT_COLL_NAME, UmbrellaProject.class, DBQuery.is("properties.limsCode.value", umbrellaCode)).toList().get(0);
							
							project.umbrellaProjectCode = umbrellaProject.code;
							
							// Responsable bio
							int idRespBio = resulSet.getInt("perco");

							if (idRespBio != 0) {
								jdbcTemplate.query("SELECT * FROM Perint WHERE perco = ?", new Object[] { idRespBio },
									(resulSetRespBio, rowNumberRespBio) -> {
										String respBio = resulSetRespBio.getString("perlog");

										project.properties.put("bioProjectManager", new PropertySingleValue(respBio));

										return null;
									}); 
							} else {
								Logger.error("Responsable bio non trouvé pour le projet '" + project.code + "'");
							}
							
							// Responsable info
							int idRespInf = resulSet.getInt("percoi");

							if (idRespInf != 0) {
								jdbcTemplate.query("SELECT * FROM Perint WHERE perco = ?", new Object[] { idRespInf },
									(resulSetRespInf, rowNumberRespInf) -> {
										String respInf = resulSetRespInf.getString("perlog");

										project.properties.put("infoProjectManager", new PropertySingleValue(respInf));

										return null;
									});
							} else {
								Logger.error("Responsable info non trouvé pour le projet '" + project.code + "'");
							}
						
							// Genre
							int idGenre = resulSet.getInt("genco");

							if (!resulSet.wasNull()) {
								jdbcTemplate.query("SELECT * FROM Genre WHERE genco = ?", new Object[] { idGenre },
									(resulSetGenre, rowNumberGenre) -> {
										String labelGenre = resulSetGenre.getString("genprj");

										project.properties.put("genre", new PropertySingleValue(labelGenre));

										return null;
									});
							} else {
								Logger.error("Genre non trouvé pour le projet '" + project.code + "'");
							}

							// Commentaire
							String comment = resulSet.getString("prscom");

							if (comment != null) {
								project.comments = new ArrayList<Comment>();
								Comment commentObj = new Comment(comment, Constants.NGL_DATA_USER);
								project.comments.add(commentObj);
							}

							return null;
			});
		});

		return result;
	}

	public void setProjectUmbrellaAndGenreAndCommentsOfProjectList(List<Project> result) {
		result.forEach(project -> {
			jdbcTemplate.query("SELECT * FROM Projet p, Projetini pi WHERE p.prsco = ? AND p.prico = pi.prico", new Object[] { project.code },
						(resulSet, rowNumber) -> {
							// Umbrella project
							int umbrellaCode = resulSet.getInt("prico");
							UmbrellaProject umbrellaProject = MongoDBDAO.find(InstanceConstants.UMBRELLA_PROJECT_COLL_NAME, UmbrellaProject.class, DBQuery.is("properties.limsCode.value", umbrellaCode)).toList().get(0);
							
							project.umbrellaProjectCode = umbrellaProject.code;	

							// Genre
							int idGenre = resulSet.getInt("genco");

							if (!resulSet.wasNull()) {
								jdbcTemplate.query("SELECT * FROM Genre WHERE genco = ?", new Object[] { idGenre },
									(resulSetGenre, rowNumberGenre) -> {
										String labelGenre = resulSetGenre.getString("genprj");

										project.properties.put("genre", new PropertySingleValue(labelGenre));

										return null;
									});
							} else {
								Logger.error("Genre non trouvé pour le projet '" + project.code + "'");
							}

							String comment = resulSet.getString("prscom");

							if (comment != null) {
								project.comments = new ArrayList<Comment>();
								Comment commentObj = new Comment(comment, Constants.NGL_DATA_USER);
								project.comments.add(commentObj);
							}

							MongoDBDAO.update(InstanceConstants.PROJECT_COLL_NAME, project);
							
							return null;
			});
		});
	}

	public void updateMaterielmanipLims(List<Container> containers, ContextValidation contextError) throws SQLException {
		contextError.addKeyToRootKeyName("updateMaterielmanipLims");
		for(Container container:containers){
			String rootKeyName = "container[" + container.code + "]";
			contextError.addKeyToRootKeyName(rootKeyName);
			String limsCode = container.properties.get(LIMS_CODE).value.toString();
			if (container.properties == null || limsCode == null) {
				contextError.addError("limsCode", "error.PropertyNotExist", LIMS_CODE,container.support.code);
			} else {
				try {
					if (!limsCode.equals("0")) {
						String sql = "pm_MaterielmanipInNGL @matmaco=?";
						logger.debug(sql + limsCode);
						jdbcTemplate.update(sql, Integer.parseInt(limsCode));
					}
				} catch(DataAccessException e) {
					contextError.addError("",e.getMessage(), container.support.code);
				}
			}
			contextError.removeKeyFromRootKeyName(rootKeyName);
		}
		contextError.removeKeyFromRootKeyName("updateMaterielmanipLims");
	}

	public void updateMaterielLims(Sample sample, ContextValidation contextError) throws SQLException {
		contextError.addKeyToRootKeyName("updateMaterielLims");
		String rootKeyName = "container[" + sample.code + "]";
		contextError.addKeyToRootKeyName(rootKeyName);
		if (sample.code == null) {
			contextError.addError("code","error.NotExist",sample.code);
		} else {
			try {
				String sql = "pm_SampleInNGL @code=?";
				logger.debug(sql + sample.code);
				jdbcTemplate.update(sql, sample.code);
			} catch(DataAccessException e){
				contextError.addError("",e.getMessage(), sample.code);
			}
		}
		contextError.removeKeyFromRootKeyName(rootKeyName);
		contextError.removeKeyFromRootKeyName("updateMaterielLims");
	}

	/* *
	 *  Find contents from a container code 
	 *  
	 *  */
	public List<Content> findContentsFromContainer(String sqlContent, String code) throws SQLException{
		return jdbcTemplate.query(sqlContent, new Object[] { code },
				(rs, rowNum) -> {
					Content sampleUsed = new Content(rs.getString("sampleCode"), null, null);
					sampleUsed.projectCode = rs.getString("project");
					// GA: add projectCode
					// GA: add properties from ExperimentType
					sampleUsed.properties = new HashMap<>();
					if (rs.getString("percentPerLane") != null) {
						sampleUsed.properties.put("percentPerLane", new PropertySingleValue(rs.getDouble("percentPerLane")));
						sampleUsed.percentage = rs.getDouble("percentPerLane");
					} else {	
						sampleUsed.percentage = rs.getDouble("percentage");
					}
					sampleUsed.properties.put("libProcessTypeCode", new PropertySingleValue(rs.getString("libProcessTypeCode")));
					if (rs.getString("tag") != null) {
						sampleUsed.properties.put("tag",         new PropertySingleValue(rs.getString("tag")));
						sampleUsed.properties.put("tagCategory", new PropertySingleValue(rs.getString("tagCategory")));
					}
					if (rs.getString("libLayoutNominalLength") != null) {
						sampleUsed.properties.put("libLayoutNominalLength", new PropertySingleValue(rs.getInt("libLayoutNominalLength")));
					}
					if (rs.getString("sampleAliquoteCode") != null) {
						sampleUsed.properties.put("sampleAliquoteCode", new PropertySingleValue(rs.getString("sampleAliquoteCode")));
					}
					return sampleUsed;
				});
	}

	public List<Run> findRunsToCreate(String sqlContent,final ContextValidation contextError)throws SQLException{

		List<Run> results = this.jdbcTemplate.query(sqlContent,new RowMapper<Run>() {

//			@SuppressWarnings("rawtypes")
			@Override
			public Run mapRow(ResultSet rs, int rowNum) throws SQLException {
				logger.debug("Begin findRunsToCreate");

				ContextValidation contextValidation = ContextValidation.createUndefinedContext(Constants.NGL_DATA_USER);
				contextValidation.addKeyToRootKeyName(contextError.getRootKeyName());

				Run run= MongoDBDAO.findByCode(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, rs.getString("code"));
				if(run==null){
					run= new Run();
				}
				run.code = rs.getString("code"); 
				run.dispatch = rs.getBoolean("dispatch");
				run.instrumentUsed = new InstrumentUsed();
				run.instrumentUsed.code = rs.getString("insCode");
				run.instrumentUsed.typeCode = DataMappingCNS.getInstrumentTypeCodeMapping(rs.getString("insCategoryCode"));
				run.typeCode =DataMappingCNS.getRunTypeCodeMapping(rs.getString("insCategoryCode"));
				run.containerSupportCode=rs.getString("containerSupportCode");
				run.sequencingStartDate=rs.getDate("sequencingStartDate");
				//Revoir l'etat en fonction du dispatch et de la validation
				// GA: fin de tranfert


				Valuation valuation=new Valuation();
				run.valuation=valuation;

				//
				run.valuation.valid=TBoolean.valueOf(rs.getString("validationValid"));
				run.valuation.user="lims";
				run.valuation.date=rs.getDate("validationDate");
				// GA: run.validation.resolutionCodes
				State state = new State();
				run.state = state;
				run.state.code = DataMappingCNS.getStateRunFromLims(run.valuation.valid);
				run.state.user = NGSRG_CODE;
				run.state.date = new Date();

				run.state.historical=new HashSet<>();		
				run.state.historical.add(newTransientState(rs.getDate("beginNGSRG"),"IP-RG",0));
				run.state.historical.add(newTransientState(rs.getDate("endNGSRG"),"F-RG",1));				

				TraceInformation ti = new TraceInformation(); 
				ti.setTraceInformation(NGSRG_CODE);
				run.traceInformation = ti; 

				contextValidation.addKeyToRootKeyName("run["+run.code+"]");
				run.treatments=new HashMap<>();
				run.treatments.put(NGSRG_CODE,newTreatment(contextValidation,rs, Level.CODE.Run,NGSRG_CODE,NGSRG_CODE,RUN_TYPE_CODE));
				contextValidation.removeKeyFromRootKeyName("run["+run.code+"]");

				if(rs.getString("sequencingProgramType")!=null){
					run.properties.put("sequencingProgramType",new PropertySingleValue(rs.getString("sequencingProgramType")));
				}

				if(contextValidation.hasErrors()){
					contextError.getErrors().putAll(contextValidation.getErrors());
					return null;
				}else {
					return run;
				}
			}

		});        

		return results;
	}

	// -----------------------------------------------------------------------
	// renamed
	
	/**
	 * Create a new transient state.
	 * @param date  date
	 * @param state state
	 * @param index index
	 * @return      new transient state
	 * @deprecated use {@link #newTransientState(Date, String, int)}
	 */
	@Deprecated
	protected TransientState getTransientState_(Date date, String state, int index) {
		return newTransientState(date, state, index);
	}
	
	/**
	 * Create a new transient state.
	 * @param date  date
	 * @param state state
	 * @param index index
	 * @return      new transient state
	 */
	protected TransientState newTransientState(Date date, String state, int index) {
		if (date != null) {
			TransientState transientState = new TransientState();
			transientState.date  = date;
			transientState.index = index;
			transientState.code  = state;
			transientState.user  = NGSRG_CODE;	
			return transientState;
		}
		return null;
	}

	// -----------------------------------------------------------------------

	public List<Lane> findLanesToCreateFromRun(final Run run, final ContextValidation contextError) throws SQLException { 
		return jdbcTemplate.query("pl_LaneUnRunToNGL @runCode=?", new Object[] { run.code },
				(rs, rowNum) -> {
					Lane lane = run.getLane(rs.getInt("lanenum"));
					if (lane == null) {
						logger.debug("Lane null");
						lane = new Lane();
						lane.number = rs.getInt("lanenum");
					}
					lane.valuation = new Valuation();
					lane.valuation.valid = TBoolean.valueOf(rs.getString("validationValid"));
					lane.valuation.user  = "lims";
					lane.valuation.date  = rs.getDate("validationDate");
					String key = "lane[" + lane.number + "].treatment[default]";
					contextError.addKeyToRootKeyName(key);
					Treatment treatment = newTreatment(contextError, rs, Level.CODE.Lane, NGSRG_CODE, NGSRG_CODE, RUN_TYPE_CODE);
					if (treatment == null) {
						return null;
					} else {
						lane.treatments.put(NGSRG_CODE, treatment);
					}
					contextError.removeKeyFromRootKeyName(key);
					return lane;
				});
	}

	public Treatment newTreatment(ContextValidation contextError,ResultSet rs,Level.CODE level,String categoryCode,String code,String typeCode) throws SQLException{
		Treatment treatment = new Treatment();
		treatment.categoryCode = categoryCode;
		treatment.code         = code;
		treatment.typeCode     = typeCode;
		Map<String,PropertyValue> m = new HashMap<>();
		try {
			TreatmentType treatmentType = TreatmentType.find.get().findByCode(treatment.typeCode);
			if (treatmentType == null) {
				contextError.addError("treatmentType", "error.codeNotExist", treatment.typeCode);
				return null;
			} else {
				MappingHelper.getPropertiesFromResultSet(rs, treatmentType.getPropertyDefinitionByLevel(level),m);
			}
		} catch (DAOException e) {
			logger.error("",e);
		}
		treatment.results = new HashMap<>();
		treatment.results.put("default", m);
		return treatment;
	}

	public List<ReadSet> findReadSetToCreateFromRun(final Run run, final ContextValidation contextError) throws SQLException {
		return jdbcTemplate.query("pl_ReadSetUnRunToNGL @runCode=?", new Object[] { run.code },
				(rs, rowNum) -> {
					ReadSet readSet = new ReadSet();
					readSet.code        = rs.getString("code");
					readSet.archiveId   = rs.getString("archiveId");
					readSet.archiveDate = rs.getDate("archiveDate");
					readSet.dispatch    = rs.getBoolean("dispatch");
					readSet.laneNumber  = rs.getInt("laneNumber");
					readSet.path        = rs.getString("readSetPath");
					readSet.projectCode = rs.getString("projectCode");
					readSet.runCode     = run.code;
					readSet.runTypeCode = run.typeCode;
					readSet.sampleCode  = rs.getString("sampleCode");
					readSet.state       = new State();
					readSet.state.code  = DataMappingCNS.getStateReadSetFromLims(rs.getString("state"),TBoolean.valueOf(rs.getString("validationBioinformatic")));
					readSet.state.date  = new Date();
					readSet.state.user  = "lims";

					readSet.state.historical = new HashSet<>();		
					readSet.state.historical.add(newTransientState(rs.getDate("beginNGSRG"), "IP-RG", 0));
					readSet.state.historical.add(newTransientState(rs.getDate("endNGSRG"),   "F-RG",  1));
					readSet.state.historical.add(newTransientState(rs.getDate("endNGSRG"),   "IW-QC", 2));

					readSet.traceInformation = new TraceInformation();
					readSet.traceInformation.setTraceInformation("lims");
					readSet.typeCode    = READSET_DEFAULT_CODE;
					readSet.archiveDate = rs.getDate("archiveDate");
					readSet.archiveId   = rs.getString("archiveId");
					readSet.runSequencingStartDate=rs.getDate("runSequencingStartDate");
					// To valide
					readSet.bioinformaticValuation=new Valuation();
					readSet.bioinformaticValuation.valid=TBoolean.valueOf(rs.getString("validationBioinformatic"));
					readSet.bioinformaticValuation.date=new Date();
					readSet.bioinformaticValuation.user="lims";
					readSet.productionValuation=new Valuation();
					readSet.productionValuation.valid=TBoolean.valueOf(rs.getString("validationProduction"));
					readSet.productionValuation.date=rs.getDate("validationProductionDate");
					readSet.productionValuation.user="lims";
					readSet.treatments.put(NGSRG_CODE,newTreatment(contextError,rs,Level.CODE.ReadSet,NGSRG_CODE,NGSRG_CODE,RUN_TYPE_CODE));
					readSet.treatments.put(GLOBAL_CODE,newTreatment(contextError,rs,Level.CODE.ReadSet,GLOBAL_CODE,GLOBAL_CODE,GLOBAL_CODE));
					return readSet;
				});
	}

	public void updateRunLims(List<Run> updateRuns,	ContextValidation contextError)throws SQLException {
		updateRunLims(updateRuns, true, contextError);
	}

	public void updateRunLims(List<Run> updateRuns, boolean inNGL, ContextValidation contextError) throws SQLException {
		contextError.addKeyToRootKeyName("updateRunLims");
		for (Run run : updateRuns) {
			String rootKeyName = "run[" + run.code + "]";
			contextError.addKeyToRootKeyName(rootKeyName);
			try {
				String sql = "pm_RunhdInNGL @runhnom=?, @InNGL=?";
				logger.debug(sql + run.code);
				int intInNGL = inNGL ? 1 : 0;
				jdbcTemplate.update(sql, run.code,intInNGL);
			} catch (DataAccessException e) {
				contextError.addError("", e.getMessage(), run.code);
			}
			contextError.removeKeyFromRootKeyName(rootKeyName);
		}
		contextError.removeKeyFromRootKeyName("updateRunLims");
	}

	public List<File> findFileToCreateFromReadSet(final ReadSet readSet, final ContextValidation contextError) throws SQLException {
		return jdbcTemplate.query("pl_FileUnReadSetToNGL @readSetCode=?", new Object[] { readSet.code },
				(rs, rowNum) -> {
					File file = new File();
					file.extension = rs.getString("extension");
					file.fullname  = rs.getString("fullname");
					file.typeCode  = rs.getString("typeCode");
					file.usable    = rs.getBoolean("usable");

					ReadSetType readSetType = null;
					try {
						readSetType = ReadSetType.find.get().findByCode(readSet.typeCode);
					} catch (DAOException e) {
						logger.error("",e);
						throw e;
					}
					file.properties = new HashMap<>();
					MappingHelper.getPropertiesFromResultSet(rs, readSetType.getPropertyDefinitionByLevel(Level.CODE.File), file.properties);
					return file;
				});
	}

	public List<Index> findIndexIlluminaToCreate(final ContextValidation contextError) throws SQLException {
		return jdbcTemplate.query("pl_TagUneEtmanip 13", 
				(rs, rowNum) -> {
					Index index = new IlluminaIndex();
					index.code         = rs.getString("tagkeyseq");
					index.name         = rs.getString("tagkeyseq");
					index.shortName    = rs.getString("tagkeyseq");
					index.categoryCode = rs.getString("categoryCode");
					index.sequence     = rs.getString("tagseq");
					index.supplierName = "illumina";
					index.supplierIndexName = rs.getString("tagnamefour");
					index.traceInformation = new TraceInformation();
					index.traceInformation.forceModificationStamp(contextError.getUser());
					return index;
				});
	}

	public List<String> findSampleUpdated(List<String> sampleCodes) {
		String sql = "select code=rtrim(prsco)+'_'+rtrim(adnnom) from Materiel m, Useadn u where u.adnco=m.adnco and ";
		if (sampleCodes == null) {
			sql = sql + "  uaddm > uadInNGL";
		} else {
			// Pour les tests unitaires
			sql += Iterables.map(sampleCodes, c -> "'" + c + "'")
					        .surround(" rtrim(prsco)+'_'+rtrim(adnnom) in (", ",", ")")
					        .asString();
		}
		// Search Sample to modify
		return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("code"));
	}

	public ReadSet findLSRunProjData(ReadSet inReadset) {
		List<ReadSet> results = jdbcTemplate.query("pl_LSRunProjUnReadSetToNGL @readSetCode=?", new String[] { inReadset.code },
				(rs, rowNum) -> {
					ReadSet readset = new ReadSet();
					readset.code = rs.getString("code");
					//readset.location = rs.getString("location");
					//readset.path = rs.getString("path");
					if (rs.getString("strandOrientation") != null) {
						readset.properties.put("strandOrientation", new PropertySingleValue(rs.getString("strandOrientation")));
					}
					if (rs.getString("insertSizeGoal") != null) {
						readset.properties.put("insertSizeGoal", new PropertySingleValue(rs.getString("insertSizeGoal")));
					}	
					return readset;						
				});
		if (results.size() == 1) 
			return results.get(0);
		return null;
	}
	
	public List<ReadSet> findLSRunProjData() {
		return jdbcTemplate.query("pl_LSRunProjUnReadSetToNGL",
				(rs, rowNum) -> {
					ReadSet readset = new ReadSet();
					readset.code     = rs.getString("code");
					readset.location = rs.getString("location");
					readset.path     = rs.getString("path");
					if (rs.getString("strandOrientation") != null) {
						readset.properties.put("strandOrientation", new PropertySingleValue(rs.getString("strandOrientation")));
					}
					if (rs.getString("insertSizeGoal") != null) {
						readset.properties.put("insertSizeGoal", new PropertySingleValue(rs.getString("insertSizeGoal")));
					}	
					return readset;						
				});
	}
	
}

