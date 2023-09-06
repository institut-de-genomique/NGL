package models;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import models.laboratory.common.instance.Comment;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TBoolean;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.common.instance.Valuation;
import models.laboratory.common.instance.property.PropertyObjectValue;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.ContainerSupport;
import models.laboratory.container.instance.Content;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.parameter.index.IlluminaIndex;
import models.laboratory.parameter.index.Index;
import models.laboratory.project.description.ProjectType;
import models.laboratory.project.instance.BioinformaticParameters;
import models.laboratory.project.instance.Project;
import models.laboratory.sample.description.SampleType;
import models.laboratory.sample.instance.Sample;
import models.utils.InstanceHelpers;
import models.utils.dao.DAOException;
import models.utils.instance.ContainerHelper;
import models.utils.instance.ContainerSupportHelper;
import services.instance.experiment.ExperimentImport;
import validation.ContextValidation;

// CTX: clean validation context uses
/**
 * Import data from CNG's LIMS to NGL 
 * Functions to get projects, samples,  containers ( lanes, tubes), indexes
 * Sub-functions to map data (between Solexa and NGL)
 * Sub-functions to update dates (in order to know what data has been imported)
 * 
 * @author dnoisett
 * 
 */
@Repository
public class LimsCNGDAO {

	private static final play.Logger.ALogger logger = play.Logger.of(LimsCNGDAO.class);
	
	private JdbcTemplate jdbcTemplate;

	private static final String CONTAINER_STATE_CODE_IW_P = "IW-P";
	private static final String CONTAINER_STATE_CODE_IS   = "IS";
	
	protected static final String PROJECT_TYPE_CODE_DEFAULT  = "default-project";
	protected static final String PROJECT_STATE_CODE_DEFAULT = "IP";
	protected static final String PROJECT_PROPERTIES_SYNCHRO_PROJ = "synchroProj";
	protected static final String QTREE_QUOTA = "qtreeQuota";
	
	protected static final String IMPORT_CATEGORY_CODE     = "sample-import";  // inutilisé...
	protected static final String IMPORT_TYPE_CODE_DEFAULT = "default-import";
	
	protected static final String SAMPLE_TYPE_CODE_DEFAULT = "default-sample-cng"; // inutilisé...
	protected static final String SAMPLE_USED_TYPE_CODE    = "default-sample-cng";	
	
	@Autowired
	@Qualifier("lims")
	private void setDataSource(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);              
	}
	
	/* ************************************************************************************************************************************************
	 * 1 - Common mapping for Project
	 * @param rs
	 * @param rowNum
	 * @param ctxErr
	 * @return
	 * @throws SQLException
	 */
	private Project commonProjectMapRow(ResultSet rs, int rowNum, ContextValidation ctxErr) throws SQLException { 
		Project project = new Project();
		
		project.code = rs.getString("code");
		project.name = rs.getString("name").trim();		
		
		project.typeCode=PROJECT_TYPE_CODE_DEFAULT;
		ProjectType projectType=null;
		try {
			projectType = ProjectType.find.get().findByCode(project.typeCode);
		} catch (DAOException e) {
			logger.error("",e);
			return null;
		}
		if (projectType == null) {
			ctxErr.addError("code", "error.codeNotExist", project.typeCode, project.code);
			return null;
		}
		
		project.categoryCode=projectType.category.code;
		
		project.properties = new HashMap<>();
		project.properties.put(PROJECT_PROPERTIES_SYNCHRO_PROJ, new PropertySingleValue(Boolean.TRUE));
		
		project.state = new State(); 
		project.state.code=PROJECT_STATE_CODE_DEFAULT;
		project.state.user = ctxErr.getUser();
		project.state.date = new Date();
		
		project.traceInformation = new TraceInformation(); 
		project.traceInformation.setTraceInformation(ctxErr.getUser());
	
		// just one comment for one project
		if (rs.getString("comments") != null ) {
			project.comments = new ArrayList<>(); 
			InstanceHelpers.addComment(project.comments, rs.getString("comments"), Constants.NGL_DATA_USER);
		}
		
		//specific to CNG
		project.bioinformaticParameters = new BioinformaticParameters();
		project.bioinformaticParameters.biologicalAnalysis = Boolean.TRUE; 
		
		return project;
	}
	
	/* ************************************************************************************************************************************************
	 * 2 - Common mapping for Sample
	 * @param rs
	 * @param rowNum
	 * @param contextError
	 * @return
	 * @throws SQLException
	 */
	private Sample commonSampleMapRow(ResultSet rs, int rowNum, ContextValidation ctxErr) throws SQLException {
			
			Sample sample = new Sample();
			
			sample.traceInformation = new TraceInformation();
			sample.traceInformation.setTraceInformation(ctxErr.getUser());

			sample.code=rs.getString("code");
			
			String sampleTypeCode=rs.getString("sample_type");
			logger.debug("[commonSampleMapRowSample] code :"+sample.code+ " Sample type code :"+sampleTypeCode);
			
			SampleType sampleType=null;
			try {
				sampleType = SampleType.find.get().findByCode(sampleTypeCode);
			} catch (DAOException e) {
				logger.error("",e);
				return null;
			}
			if ( sampleType==null ) {
				ctxErr.addError("code", "error.codeNotExist", sampleTypeCode, sample.code);
				return null;
			}
			
			sample.typeCode=sampleType.code;
			sample.categoryCode=sampleType.category.code;
			sample.name=rs.getString("name");
			sample.referenceCollab= rs.getString("ref_collab");
			sample.taxonCode=rs.getString("taxon_code");
			sample.importTypeCode=IMPORT_TYPE_CODE_DEFAULT;
		
			sample.projectCodes = new HashSet<>();
			if (rs.getString("project") != null) {
				sample.projectCodes.add(rs.getString("project"));
			} else {
				sample.projectCodes.add(" "); 
			}

			sample.comments = new ArrayList<>();
			
			if (rs.getString("comments") != null) {
				sample.comments.add(new Comment(rs.getString("comments"), Constants.NGL_DATA_USER));
			}
			
			sample.properties = new HashMap<>(); // <String, PropertyValue>();
			sample.properties.put("limsCode", new PropertySingleValue(rs.getInt("lims_code")));		
			
			return sample;
	}

	/* ************************************************************************************************************************************************
	 * 3 - Common mapping for container
	 * FDS 18/01/2016 prise en charge des cas sample-well et library-well: les mapper =>well
	 * @param rs
	 * @param rowNum
	 * @param ctxErr
	 * @param experimentTypeCode 
	 * @param importState   22/10/2015 ajouté pour la reprise avec status differencié 
	 * @return
	 * @throws SQLException
	 */
	private Container commonContainerMapRow(ResultSet rs, int rowNum, ContextValidation ctxErr, String containerCategoryCode, String experimentTypeCode, String importState) throws SQLException {
		Container container = new Container();
		String specialContainerCategoryCode=containerCategoryCode; // garder une trace pour les cas xxx-well
		
		container.traceInformation = new TraceInformation();
		container.traceInformation.setTraceInformation(ctxErr.getUser());
		container.code = rs.getString("container_code");
		logger.debug("[commonContainerMapRow] Container code :"+container.code);

		//FDS 20/01/2016 ne pas ajouter des commentaires vides  ""...
		if ((rs.getString("comment") != null) && (! rs.getString("comment").equals(""))) {
			container.comments = new ArrayList<>();	
			container.comments.add(new Comment(rs.getString("comment"), Constants.NGL_DATA_USER));
		}
		
		//FDS 20/01/2016 n'ajouter que s'il n'y a qq chose!!
		if (experimentTypeCode != null) {
			container.fromTransformationTypeCodes=new HashSet<>();
			container.fromTransformationTypeCodes.add(experimentTypeCode);
		}
		
		container.state = new State(); 
		// si pas d'import State forcer a 'in stock'
		if ( (importState == null)  || (importState.equals("is")) ) {
			container.state.code = CONTAINER_STATE_CODE_IS; 
		}
		else if (importState.equals("iw-p")) {
			container.state.code = CONTAINER_STATE_CODE_IW_P;
		}
		container.state.user = ctxErr.getUser();
		container.state.date = new Date(); 
		
		container.valuation = new Valuation(); 
		container.valuation.valid = TBoolean.UNSET;
		
		// FDS 14/01/2016 remonter avant getContainerSupport pour traiter les differents cas de concentration
		//                => inverser le sens du test: tube/sample-well/library-well<=>lane
		if ( ! containerCategoryCode.equals("lane")) {
			
			//round concentration to 2 decimals using BigDecimal
			Double concentration = null;
			BigDecimal d = null;
//			if ((Float) rs.getFloat("concentration") != null) 
			{
				 d = new BigDecimal(rs.getFloat("concentration"));
				 BigDecimal d2 = d.setScale(2, BigDecimal.ROUND_HALF_UP); 
				 concentration = d2.doubleValue();
			}
			
			/* test 15/06/2016 ajouter concentration pour library-well */
			if (containerCategoryCode.equals("tube") || containerCategoryCode.equals("library-well")) {
				container.concentration = new PropertySingleValue(concentration, "nM");
			} else if ( containerCategoryCode.equals("sample-well")) {
				container.concentration = new PropertySingleValue(concentration, "ng/µl");
				
				//FDS dans ce cas on a aussi un volume 
				Double volume= null;
//				if ((Float) rs.getFloat("volume") != null) {
				{
					d = new BigDecimal(rs.getFloat("volume"));
					 BigDecimal d2 = d.setScale(2, BigDecimal.ROUND_HALF_UP); 
					 volume = d2.doubleValue();
				}
				container.volume = new PropertySingleValue(volume,"µL");
			}
		}
		
		// 14/10/2016 containerCategoryCode est surchargé on a sample-well, library-well dans certains cas
		// remettre la valeur initiale
		if (containerCategoryCode.equals("sample-well") || containerCategoryCode.equals("library-well")) {
			logger.debug("[commonContainerMapRow] ContainerCategorycode :" + containerCategoryCode);			
			container.categoryCode="well";
			containerCategoryCode="well";
		} else {
			container.categoryCode = containerCategoryCode; 
		}
		
		// define container support attributes
		try {
			// 14/10/2015 FDS ajout storage_code
			container.support = ContainerSupportHelper.getContainerSupport(containerCategoryCode, 
					                                                       rs.getInt("nb_usable_container"),
					                                                       rs.getString("support_code"),
					                                                       rs.getString("column"),
					                                                       rs.getString("row"),
					                                                       rs.getString("storage_code"));  
		} catch(DAOException e) {
			logger.error("[commonContainerMapRow] Can't get container support !"); 
		}
		
		container.properties = new HashMap<>(); // <String, PropertyValue>();
		container.properties.put("limsCode",new PropertySingleValue(rs.getInt("lims_code")));
			
		if (rs.getString("project")!=null) {
			container.projectCodes = new HashSet<>();
			container.projectCodes.add(rs.getString("project"));
		}		
		
		// creation du content d'un container
		if (rs.getString("sample_code")!=null) {
			Content content=new Content();
			content.sampleCode = rs.getString("sample_code");
			content.projectCode = rs.getString("project");
						
			String sampleTypeCode=rs.getString("sample_type");
			//Logger.debug("[commonContainerMapRow] content.Sample type code :"+sampleTypeCode);
			
			SampleType sampleType=null;
			try {
				sampleType = SampleType.find.get().findByCode(sampleTypeCode);
			} catch (DAOException e) {
				logger.error("",e);
				return null;
			}
			if (sampleType == null) {
				ctxErr.addError("sample code", "error.codeNotExist", sampleTypeCode, content.sampleCode);
				return null;
			}	
			
			content.sampleTypeCode = sampleType.code;
			content.sampleCategoryCode = sampleType.category.code;
			
			content.properties = new HashMap<>(); // <String, PropertyValue>();
			
			//FDS 20/01/2016 ne pas ajouter ces proprietes pour sample-well, elle ne peuvent pas exister...
			if (! "sample-well".equals(specialContainerCategoryCode)) {
				if (rs.getString("tag") != null) { 
					content.properties.put("tag", new PropertySingleValue(rs.getString("tag")));
					content.properties.put("tagCategory", new PropertySingleValue(rs.getString("tagcategory")));
				} else {
					content.properties.put("tag",new PropertySingleValue("-1")); // specific value for making comparison, suppressed in demultiplexContainer
					content.properties.put("tagCategory",new PropertySingleValue("-1"));// specific value for making comparison, suppressed in demultiplexContainer
				}				
				if (rs.getString("exp_short_name")!=null) {
					content.properties.put("libProcessTypeCode", new PropertySingleValue(rs.getString("exp_short_name")));
				} else {
					logger.warn("[commonContainerMapRow] content exp_short_name : null !!!!!!");
					content.properties.put("libProcessTypeCode", new PropertySingleValue("-1"));// specific value for making comparison, suppressed in demultiplexContainer
				}
			
				// FDS 15/06/2015 JIRA NGL-673 Ajout du barcode de la librairie solexa initiale ( aliquot )=> nouvelle propriété de content 
				if (rs.getString("aliquote_code")!=null) { 
					content.properties.put("sampleAliquoteCode", new PropertySingleValue(rs.getString("aliquote_code")));
				} else {
					logger.warn("[commonContainerMapRow] content aliquote code : null !!!!!");
					content.properties.put("sampleAliquoteCode", new PropertySingleValue("-1"));// specific value for making comparison, suppressed in demultiplexContainer
				}
			} else if ("sample-well".equals(specialContainerCategoryCode)) {
				if (rs.getString("aliquote_code")!=null) { 
					content.properties.put("sampleAliquoteCode", new PropertySingleValue(rs.getString("aliquote_code")));
				}
			}
			container.contents.add(content);		
			container.sampleCodes=new HashSet<>();
			container.sampleCodes.add(rs.getString("sample_code"));	
			logger.debug("[commonContainerMapRow] container sampleCodes: " + container.sampleCodes);		
		}
		return container;
	}
	
	/* ************************************************************************************************************************************************
	 * 4- common mapping for containerSupport
	 * @param rs
	 * @param rowNum
	 * @param ctxErr
	 * @return
	 * @throws SQLException
	 *  FDS uniquement  appellé dans setSequencingProgramTypeToContainerSupport ?????????????????
	 */
	private ContainerSupport commonContainerSupportMapRow(ResultSet rs, int rowNum, ContextValidation ctxErr) throws SQLException {
		ContainerSupport containerSupport = new ContainerSupport();	
		containerSupport.code = rs.getString("support_code");
		if (rs.getString("seq_program_type").equals("PE") || rs.getString("seq_program_type").equals("SR")) {
			containerSupport.properties= new HashMap<>(); // <String, PropertyValue>();
			containerSupport.properties.put("sequencingProgramType", new PropertySingleValue(rs.getString("seq_program_type")));
		} else {
			logger.error("Wrong value of seq_program_type : " + rs.getString("seq_program_type") + "! (expected SE ou PR) for code : " + rs.getString("support_code")); 
		}
		return containerSupport;
	}	
	
	
	/* * FDS: no IndexMapRow
	 *  mapping is done in findIndexToCreate
	 */
	
	/* ************************************************************************************************************************************************
	 **
	 * 1a - To get new projects
	 * @param contextError
	 * @return
	 * @throws SQLException
	 * @throws DAOException
	 */
	public List<Project> findProjectToCreate(final ContextValidation contextError, String groupName) throws SQLException, DAOException {		
		List<Project> results = this.jdbcTemplate.query("select code, name, comments from v_project_tongl", new Object[]{},  
			new RowMapper<Project>() {
				@Override
				public Project mapRow(ResultSet rs, int rowNum) throws SQLException {								
					ResultSet rs0 = rs;
					int rowNum0 = rowNum;
					ContextValidation ctxErr = contextError; 
					Project p =  commonProjectMapRow(rs0, rowNum0, ctxErr); 
					
					Map<String, Object> map = new HashMap<>();
					map.put("proj", 0);
					map.put("rawdata", 0);
					map.put("scratch", 100);

					p.properties.put(QTREE_QUOTA, new PropertyObjectValue(map));
					
					return p;
				}	
		});
		return results;
	}

	/* ************************************************************************************************************************************************
	 * 1b - To get projects that have been updated in Soxela
	 * @param contextError
	 * @return
	 * @throws SQLException
	 * @throws DAOException
	 */
	public List<Project> findProjectToModify(final ContextValidation contextError) throws SQLException, DAOException {	
		List<Project> results = this.jdbcTemplate.query("select  code, name, comments from v_project_updated_tongl", new Object[]{}, 
			new RowMapper<Project>() {
				@Override
				public Project mapRow(ResultSet rs, int rowNum) throws SQLException {
					ResultSet rs0 = rs;
					int rowNum0 = rowNum;
					ContextValidation ctxErr = contextError; 
					Project p =  commonProjectMapRow(rs0, rowNum0, ctxErr); 
					return p;
				}	
		});
		return results;
	}
	
	/* ************************************************************************************************************************************************ 
	 * UPDATE Solexa t_project import/update dates
	 * @param projects
	 * @param contextError
	 * @throws DAOException
	 */
	public void updateLimsProjects(List<Project> projects, ContextValidation contextError, String mode) throws DAOException {
		String key, column;
		if (mode.equals("creation")) {
			key = "update_ImportDate";
			column = "nglimport_date";
		} else {
			key = "update_UpdateDate";
			column = "ngl_update_date";			
		}
		contextError.addKeyToRootKeyName(key);
		
		String sql = "UPDATE t_project SET " + column + " = ? WHERE name = ?";
		
		List<Object[]> parameters = new ArrayList<>();
		for (Project project : projects) {
	        parameters.add(new Object[] {new Date(), project.code}); 
		}
		this.jdbcTemplate.batchUpdate(sql, parameters);  
		contextError.removeKeyFromRootKeyName(key);
	}
	
	/* ************************************************************************************************************************************************
	 ** FDS 17/06/2015 inutile puisqu'il n'y a plus qu'un seul projet pour un sample ???
	 * 2a -To set projectCodes to samples
	 * @param results
	 * @return
	 */
	public List<Sample> demultiplexSample(List<Sample> results) {
		//affect all the project codes to a same sample 
		/// required to have an ordered list (see ORDER BY clause in the sql of the view)
		int pos = 0;
		int x = 1;
		int listSize = results.size(); 
		while (pos < listSize-1) {
			// _FDS_: meme recodage a faire que pour les containers...
			while (pos < listSize-1 && results.get(pos).code.equals(results.get(pos+x).code)) {
				// difference between the two project codes
				if (! results.get(pos).projectCodes.toArray(new String[0])[0].equals(results.get(pos+x).projectCodes.toArray(new String[0])[0])) {
					if (! results.get(pos).projectCodes.contains(results.get(pos+x).projectCodes.toArray(new String[0])[0])) {
						results.get(pos).projectCodes.add( results.get(pos+x).projectCodes.toArray(new String[0])[0] );
					}
				}
				// all the difference have been reported on the first sample found (at the position pos)
				// so we can delete the sample at the position (posNext)
				results.remove(pos+x);
				listSize--;
			}
			pos++;
		}
		//for remove null comment or project
		for (Sample s : results) {
			for (String projectCode :s.projectCodes) {
				if (projectCode.equals(" ")) {
					s.projectCodes.remove(projectCode);
				}
			}
		}
		return results;
	}

	/* ************************************************************************************************************************************************ 
	 * 2b - To get all the samples (first loading, migration) 
	 * @param contextError
	 * @return
	 * @throws DAOException
	 */
	public List<Sample> findAllSample(final ContextValidation contextError) throws DAOException {
		List<Sample> results = this.jdbcTemplate.query("select * from v_sample_tongl_reprise order by code, project desc, comments", new Object[]{} 
		,new RowMapper<Sample>() {
			@Override
			public Sample mapRow(ResultSet rs, int rowNum) throws SQLException {
				ResultSet rs0 = rs;
				int rowNum0 = rowNum;
				ContextValidation ctxErr = contextError; 
				Sample s=  commonSampleMapRow(rs0, rowNum0, ctxErr); 
				return s;
			}
		});
		//demultiplexSample toujours necessaire car le code est le SOLEXA stock_barcode=> plusieurs samples peuvent avoir le meme code
		return demultiplexSample(results);			
	}
	
	/* ************************************************************************************************************************************************
	 * 2c - To get samples updated in the CNG's LIMS (Solexa database)
	 * @param contextError
	 * @return
	 * @throws SQLException
	 * @throws DAOException
	 */
	public List<Sample> findSampleToModify(final ContextValidation contextError) throws SQLException, DAOException {
		return findSampleToModify(contextError, null);
	}
	
	/* ************************************************************************************************************************************************
	 * 2d To get a particular sample updated in the CNG's LIMS (Solexa database)
	 * @param contextError
	 * @param sampleCode
	 * @return
	 * @throws SQLException
	 * @throws DAOException
	 */
	public List<Sample> findSampleToModify(final ContextValidation contextError, String sampleCode) throws SQLException, DAOException {		
		List<Sample> results = null;
		//FDS  25/01/2016 fusion des 2 appels a this.jdbcTemplate.query
		String sqlQuery = "";
		Object[] queryObj = null;
		
		if (sampleCode != null) { 	
			sqlQuery = "select * from v_sample_updated_tongl where code=? order by code, project desc, comments";
			logger.debug("Modify 1 sample ("+ sampleCode+ ") with SOLEXA sql: "+ sqlQuery );
			queryObj = new Object[]{sampleCode};
		} else {
			sqlQuery = "select * from v_sample_updated_tongl order by code, project desc, comments";
			logger.debug("Modify samples with SOLEXA sql: "+ sqlQuery );
			queryObj = new Object[]{};
		}
		
		results = this.jdbcTemplate.query(sqlQuery, queryObj
			,new RowMapper<Sample>() {
				@Override
				public Sample mapRow(ResultSet rs, int rowNum) throws SQLException {
					ResultSet rs0 = rs;
					int rowNum0 = rowNum;
					ContextValidation ctxErr = contextError; 
					Sample s=  commonSampleMapRow(rs0, rowNum0, ctxErr); 
					return s;
				}
			});
		
		//demultiplexSample toujours necessaire car le code est le SOLEXA stock_barcode=> plusieurs samples peuvent avoir le meme code
		return demultiplexSample(results);	
	}
	
	/* ************************************************************************************************************************************************
	 * 2e To get new samples
	 * @param contextError
	 * @return
	 * @throws SQLException
	 * @throws DAOException
	 */
	public List<Sample> findSampleToCreate(final ContextValidation contextError) throws SQLException, DAOException {
		return findSampleToCreate(contextError, null);
	}
	
	/* *
	 * To get a new particular sample
	 * @param contextError
	 * @param sampleCode
	 * @return
	 * @throws SQLException
	 * @throws DAOException
	 */
	public List<Sample> findSampleToCreate(final ContextValidation contextError, String sampleCode) throws SQLException, DAOException {		
		List<Sample> results = null;
		//FDS  25/01/2016 fusion des 2 appels a this.jdbcTemplate.query
		String sqlQuery = "";
		Object[] queryObj = null;
		
		if (sampleCode != null) {	
			sqlQuery = "select * from v_sample_tongl where code = ? order by code, project desc, comments";
			logger.debug("Import 1 sample ("+ sampleCode+ ") with SOLEXA sql: "+ sqlQuery );
			queryObj = new Object[]{sampleCode};
		} else {		
			sqlQuery = "select * from v_sample_tongl order by code, project desc, comments";
			logger.debug("Import samples with SOLEXA sql:" +  sqlQuery );
			queryObj = new Object[]{};
		}
		results = this.jdbcTemplate.query(sqlQuery, queryObj
			,new RowMapper<Sample>() {
				@Override
				public Sample mapRow(ResultSet rs, int rowNum) throws SQLException {
					ResultSet rs0 = rs;
					int rowNum0 = rowNum;
					ContextValidation ctxErr = contextError; 
					Sample s=  commonSampleMapRow(rs0, rowNum0, ctxErr); 
					return s;
				}
			});
		//demultiplexSample toujours necessaire car le code est le SOLEXA stock_barcode=> plusieurs samples peuvent avoir le meme code
		return demultiplexSample(results);	
	}
	
	/* ************************************************************************************************************************************************
	 * To set projectCodes & sampleCodes
	 * @param results
	 * @return
	 * @throws DAOException
	 */
	public static List<Container> demultiplexContainer(List<Container> results) throws DAOException {
		//affect all the project codes /samples /tags to the same container (for having unique codes of containers) 
		/// required to have an ordered list (see ORDER BY clause in the sql of the view)
		logger.debug("start demultiplexing containers");
		
		int pos = 0;
		int x = 1;
		int listSize = results.size();
		Boolean findContent;
		Content[] tmpArray = new Content[1];
		
		while (pos < listSize-1) {
			
			while ( (pos < listSize-1) && (results.get(pos).code.equals(results.get(pos+x).code)) ) {
				// 10-07-15 refactored by NW
				logger.debug("demultiplexing container "+ results.get(pos).code);
				assert results.get(pos+x).sampleCodes.size() <= 1;
				
				// difference between two consecutive sampleCodes
				java.util.Iterator<String> iter = results.get(pos+x).sampleCodes.iterator();
				if (iter.hasNext()) {
					String oneSampleCode = iter.next();
					if (! results.get(pos).sampleCodes.contains(oneSampleCode)) {
						results.get(pos).sampleCodes.add(oneSampleCode);
					}
				}

				findContent = false;
				//just to be sure that we don't create content in double
				// FDS 16/06/2015 get("sampleAliquoteCode") ajouté pour JIRA NGL-273
				for (Content content : results.get(pos).contents) {
					// Content nextContent = results.get(pos+x).contents.iterator().next();
					Content nextContent = results.get(pos+x).contents.toArray(tmpArray)[0];
					if ( (content.sampleCode.equals(nextContent.sampleCode))  
								&& (content.properties.get("tag").value.equals(nextContent.properties.get("tag").value)) 
								&& (content.properties.get("libProcessTypeCode").value.equals(nextContent.properties.get("libProcessTypeCode").value)) 
								&& (content.properties.get("sampleAliquoteCode").value.equals(nextContent.properties.get("sampleAliquoteCode").value)) ) {
						findContent = true;
						//Logger.debug("content already created !");
						break;
					}
				}				
				
				if (!findContent) createContent(results, pos, pos+x);
								
				// all the difference have been reported on the first sample found (at the position pos)
				// so we can delete the sample at the position (posNext)
				results.remove(pos+x);
				listSize--;
			}
			pos++;
		}	
		
		for (Container r : results) {
			// 10-07-15 refactoredF by NW
			
			//For now we have not the % of each content=> assume equimolarity !
			//Logger.debug("Nb contents in container=" + r.contents.size());
			//FDS calcul identique pour les contents d'un meme container=>ne pas mettre dans le for!!
			Double equiPercent = ContainerHelper.getEquiPercentValue(r.contents.size());
			//Logger.debug("equiPercent="+equiPercent);
			
			for (Content content : r.contents) {		
				//remove bad properties;  FDS comments 04/05/2015 : valeurs -1 positionnées dans commonContainerMapRow 
				// FDS 17/06/2015 ajout sampleAliquoteCode pour JIRA NGL-673
				for (String propName : new String[]{"tag", "tagCategory", "libProcessTypeCode", "sampleAliquoteCode"}) {
					PropertyValue propVal = content.properties.get(propName);
					if (propVal != null && (propVal.value == null || propVal.value.equals("-1"))) {
						content.properties.remove(propName);
					}
				}
				
				//set percentage
				content.percentage = equiPercent; 
			}
		}	
		
		//define container projects from projects contents
		defineContainerProjectCodes(results); 
		
		logger.debug("end demultiplexing containers");
		return results;
	}
	
	/* ************************************************************************************************************************************************
	 * @param results
	 * @return
	 * @throws DAOException
	 */
	public static List<Container> defineContainerProjectCodes(List<Container> results) throws DAOException {
		for (Container r : results) {
			Set<String> projectCodes = new HashSet<>();
			for (Content c : r.contents) {
				projectCodes.add(c.projectCode);
			}
			r.projectCodes = projectCodes; 
		}
		return results;
	}
	
	/* ************************************************************************************************************************************************
	 * Create a content and attach it to a container 
	 * @param results
	 * @param posCurrent
	 * @param posNext
	 * @return
	 * @throws DAOException
	 */
	public static List<Container>  createContent(List<Container> results, int posCurrent, int posNext) throws DAOException{
		Content content = new Content();
		
		//FDS refactor todo ???    toArray(new String[0])[0]; !!!!
		
		content.sampleCode = results.get(posNext).sampleCodes.toArray(new String[0])[0];
		content.projectCode = results.get(posNext).projectCodes.toArray(new String[0])[0];
		content.sampleTypeCode =results.get(posNext).contents.toArray(new Content[0])[0].sampleTypeCode;
		content.sampleCategoryCode =results.get(posNext).contents.toArray(new Content[0])[0].sampleCategoryCode;
		
		content.properties = new HashMap<>(); // <String, PropertyValue>();
		// FDS: il peut ne pas y avoir d'index et pourtant pas de pb de null pointer exception ici ???
		content.properties.put("tag", new PropertySingleValue(results.get(posNext).contents.toArray(new Content[0])[0].properties.get("tag").value));
		content.properties.put("tagCategory", new PropertySingleValue(results.get(posNext).contents.toArray(new Content[0])[0].properties.get("tagCategory").value));
	
		if (results.get(posNext).contents.toArray(new Content[0])[0].properties.get("libProcessTypeCode") == null) {	
			logger.debug("[createContent] content.sampleCode =" + content.sampleCode + " pas de lib process type code (exp_type_code) !!!!!");
		} else {
			content.properties.put("libProcessTypeCode", new PropertySingleValue(results.get(posNext).contents.toArray(new Content[0])[0].properties.get("libProcessTypeCode").value));
			logger.debug("[createContent] content.sampleCode =" + content.sampleCode + "; content.libProcessTypeCode ="+ content.properties.get("libProcessTypeCode").value);
		}
		
		//FDS 16/06/2015 JIRA NGL-673: ajouter aliquote code 
		//FDS 19/01/2016 !! pas d'aliquote code pour des containers qui contiennent des samples
		if (results.get(posNext).contents.toArray(new Content[0])[0].properties.get("sampleAliquoteCode") == null) {
			logger.debug("[createContent] content.sampleCode =" + content.sampleCode + " pas de aliquote code !!!!!");
		} else {
			content.properties.put("sampleAliquoteCode", new PropertySingleValue(results.get(posNext).contents.toArray(new Content[0])[0].properties.get("sampleAliquoteCode").value));
			//Logger.debug("[createContent] content.sampleCode =" + content.sampleCode + "; content.sampleAliquoteCode ="+ content.properties.get("sampleAliquoteCode").value);
		}
		results.get(posCurrent).contents.add(content); 
		
		return results;
	}
	
	/* ************************************************************************************************************************************************
	 * To create new containers
	 * @param contextError
	 * @param containerCategoryCode
	 * @param experimentTypeCode
	 * @param importState 22/10/2015 parametre pour avec creation a des states differents
	 * @return
	 * @throws SQLException
	 * @throws DAOException
	 */
	public List<Container> findContainerToCreate(final ContextValidation contextError, String containerCategoryCode, String experimentTypeCode, String importState) throws SQLException, DAOException {
		return findContainerToCreate(contextError, null, containerCategoryCode, experimentTypeCode, importState);
	}
	
	/* *
	 * To create a particular new container
	 * @param containerCategoryCode
	 * @param experimentTypeCode
	 * @param importState 22/10/2015 parametre reprise avec creation a des states differents
	 * @param contextError
	 * @return
	 * @throws DAOException 
	 */
	public List<Container> findContainerToCreate(final ContextValidation contextError, String containerCode, String containerCategoryCode, String experimentTypeCode, String importState) throws DAOException {
		String sqlView="";
		String sqlQuery="";
		String sqlClause="";
		String sqlOrder="";
		Object[] queryObj = null;

		/* FDS 14/01/2016  on n'import plus les lanes
		if (containerCategoryCode.equals("lane")) {
			sqlView = "v_flowcell_tongl";
			sqlClause="";
		}
		else*/
		if (containerCategoryCode.equals("tube")) {
			
			if (experimentTypeCode.equals("lib-normalization")) {		
					sqlView = "v_libnorm_tube_tongl_new";   /* 15/06/2016 renommage des vues */
			} else if (experimentTypeCode.equals("denat-dil-lib")) {
					sqlView = "v_libdenatdil_tube_tongl_new";  /* 15/06/2016 renommage des vues */
			} else {
					//autres experimentTypeCode a venir ??
					sqlView = " UNSUPPORTED";
					logger.error("findContainerToCreate: unsupported experimentTypeCode: "+experimentTypeCode);
			}
			
			if (importState == null ) {
				sqlClause="";
			} else if (importState.equals("is")) {
				sqlClause=" and ngl_status='done' ";
			} else if (importState.equals("iw-p")) {
				sqlClause=" and ngl_status='ready' ";
			} else {
				sqlClause=" UNSUPPORTED";
				logger.error("findContainerToCreate: unsupported importState : "+importState);
			}
			//13/03/2015 le order by est TRES IMPORTANT: demultiplexContainer en depend !! 
			sqlOrder=" order by container_code, project desc, sample_code, tag, exp_short_name";
		} else if (containerCategoryCode.equals("sample-well")) {
			sqlView = "v_sample_plate_new_tongl";
			sqlOrder=" order by container_code, project desc, sample_code";
		} else if (containerCategoryCode.equals("library-well")) { /*	test 14/06/2016 */
			if (experimentTypeCode.equals("lib-normalization")) {		
					sqlView = "v_libnorm_plate_tongl_new"; 
			} else if (experimentTypeCode.equals("denat-dil-lib")) {
					sqlView = "v_libdenatdil_plate_tongl_new";
			} else {
					//autres experimentTypeCode a venir ??
					sqlView = " UNSUPPORTED";
					logger.error("findContainerToCreate: unsupported experimentTypeCode: "+experimentTypeCode);
			}
			if (importState == null ) {
				sqlClause="";
			} else if (importState.equals("is")) {
				sqlClause=" and ngl_status='done' ";
			} else if (importState.equals("iw-p")) {
				sqlClause=" and ngl_status='ready' ";
			} else {
				sqlClause=" UNSUPPORTED";
				logger.error("findContainerToCreate: unsupported importState : "+importState);
			}	
			//13/03/2015 le order by est TRES IMPORTANT: demultiplexContainer en depend !! 
			sqlOrder = " order by container_code, project desc, sample_code, tag, exp_short_name";
		}
		// fusion des 2 appels a jdbcTemplate.query
		List<Container> results = null;
		if (containerCode != null) {
			// FDS note: si containerCategoryCode = sample-well ou library-well=> n'a aucun sens d'importer un puits tout seul!!!
			logger.debug("Import container " + containerCategoryCode +"("+ containerCode+ ") with SOLEXA sql: "+ sqlView + sqlClause + sqlOrder);
			sqlQuery="select * from " + sqlView + " where container_code = ? " + sqlClause + sqlOrder;
			queryObj = new Object[]{containerCode};
		} else {
			logger.debug("Import containers " + containerCategoryCode + " with SOLEXA sql: "+ sqlView + sqlClause+ sqlOrder);
			sqlQuery="select * from " + sqlView + " where 1=1 " + sqlClause + sqlOrder;
			queryObj = new Object[]{};
		}
		results = this.jdbcTemplate.query(sqlQuery, queryObj, new RowMapper<Container>() {
			@Override
			public Container mapRow(ResultSet rs, int rowNum) throws SQLException {
				ResultSet rs0 = rs;
				int rowNum0 = rowNum;
				ContextValidation ctxErr = contextError; 

				Container c=  commonContainerMapRow(rs0, rowNum0, ctxErr, containerCategoryCode, experimentTypeCode, importState);
				return c;
			}
		});
		//FDS NOTE: c'est dans demultiplexContainer.createContent() que sont crees le(s) content(s) d'un container
		return demultiplexContainer(results);			
	}
	
	/* ************************************************************************************************************************************************
	 * To create all containers (for mass loading the first time or for migration)
	 * pas de parametre importState => tout sera créé 'In Stock'...
	 * @param contextError
	 * @param containerCategoryCode
	 * @param experimentTypeCode
	 * @param 
	 * @return
	 * @throws DAOException
	 */
	public List<Container> findAllContainer(final ContextValidation contextError, String containerCategoryCode, String experimentTypeCode) throws DAOException {
		final String _containerCategoryCode = containerCategoryCode;
		String sqlView = "";
		String sqlOrder = "";
		
		/* 19/01/2016 normalement on n'importe plus les lanes
		if (containerCategoryCode.equals("lane")) {
			sqlView = "v_flowcell_tongl_reprise";
		}
		else */
		if (containerCategoryCode.equals("tube")) {
			if (experimentTypeCode.equals("lib-normalization")) {
				sqlView = "v_libnorm_tube_tongl_all"; /* 15/06/2016 renommage des vues tube */
			} else if (experimentTypeCode.equals("denat-dil-lib")) {
				sqlView = "v_libdenatdil_tube_tongl_all"; /* 15/06/2016 renommage des vues tube */
			} else {
				//autres experimentTypeCode a venir ??
				sqlView = " UNSUPPORTED";
			}
			logger.error("findAllContainer: unsupported experimentTypeCode: "+experimentTypeCode);
			sqlOrder = " order by container_code, project desc, sample_code, tag, exp_short_name";
		} else if (containerCategoryCode.equals("sample-well")) {
				sqlView = "v_sample_plate_tongl_reprise";
				sqlOrder = " order by container_code, project desc, sample_code";
		} else if (containerCategoryCode.equals("library-well")) { /* test 15/06/2016 */
			if (experimentTypeCode.equals("lib-normalization")) {
				sqlView = "v_libnorm_plate_tongl_all";
			} else if (experimentTypeCode.equals("denat-dil-lib")) {
				sqlView = "v_libdenatdil_plate_tongl_all";
			} else {
				//autres experimentTypeCode a venir ??
				sqlView = " UNSUPPORTED";
				logger.error("findAllContainer: unsupported experimentTypeCode: "+experimentTypeCode);
			}
			// a verifier !!!!!!
			sqlOrder=" order by container_code, project desc, sample_code, tag, exp_short_name";
		}
		List<Container> results = this.jdbcTemplate.query("select * from " + sqlView + sqlOrder , new Object[]{} 
		,new RowMapper<Container>() {
			@Override
			public Container mapRow(ResultSet rs, int rowNum) throws SQLException {
				ResultSet rs0 = rs;
				int rowNum0 = rowNum;
				ContextValidation ctxErr = contextError; 
				//importState non précisé => tout sera créé 'In Stock'...
				Container c=  commonContainerMapRow(rs0, rowNum0, ctxErr, _containerCategoryCode, experimentTypeCode, null);
				return c;
			}
		});
		//FDS NOTE: c'est dans demultiplexContainer.createContent() que sont crees le(s) content(s) d'un container
		return demultiplexContainer(results);			
	}

	/* ************************************************************************************************************************************************
	 * To update containers
	 * pas de param importState => .... 
	 * @param contextError
	 * @param containerCategoryCode
	 * @param experimentTypeCode
	 * @return
	 * @    throws SQLException  NON dit Nicolas...car jdbcTemplate.query l'enrobe lui meme dans une DataAccessException
	 * @throws DAOException
	 */
	public List<Container> findContainerToModify(final ContextValidation contextError, String containerCategoryCode, String experimentTypeCode) 
			throws DAOException {
		return findContainerToModify(contextError, null, containerCategoryCode,experimentTypeCode);
	}
	
	/* *
	 * To update a particular container
	 * method for mass loading
	 * @param contextError
	 * @param containerCategoryCode
	 * @param experimentTypeCode
	 *  pas de importState ???
	 * @return
	 * @throws DAOException 
	 */
	public List<Container> findContainerToModify(final ContextValidation contextError, String containerCode, String containerCategoryCode, String experimentTypeCode) throws DAOException {		
		String sqlView = "";
		String sqlClause = "";
		String sqlOrder = "";
		Object[] queryObj=null;
		
		/* normalement on n'import plus les lanes...
		if (containerCategoryCode.equals("lane")) {
			sqlView = "v_flowcell_updated_tongl";
		}
		else */
		if (containerCategoryCode.equals("tube")) {
			if (experimentTypeCode.equals("lib-normalization")) {
				sqlView = "v_libnorm_tube_tongl_updated"; /* 15/06/2016 renommage des vues tube */
			} else if (experimentTypeCode.equals("denat-dil-lib")) {
				sqlView = "v_libdenatdil_tube_tongl_updated"; /* 15/06/2016 renommage des vues tube */
			} else {
				//autres experimentTypeCode a venir ??
				sqlView = " UNSUPPORTED";
				logger.error("findContainerToModify: unsupported experimentTypeCode: "+experimentTypeCode);
			}
			sqlOrder = " order by container_code, project desc, sample_code, tag, exp_short_name";
		} else if (containerCategoryCode.equals("sample-well")) {
			sqlView ="v_sample_plate_updated_tongl";
			sqlOrder = " order by container_code, project desc, sample_code";
		} else if (containerCategoryCode.equals("library-well")) { /* test 15/06/2016*/
			if (experimentTypeCode.equals("lib-normalization")) {
				sqlView = "v_libnorm_plate_tongl_updated";
			} else if (experimentTypeCode.equals("denat-dil-lib")) {
				sqlView = "v_libdenatdil_plate_tongl_updated";
			} else {
				//autres experimentTypeCode a venir ??
				sqlView = " UNSUPPORTED";
				logger.error("findContainerToModify: unsupported experimentTypeCode: " + experimentTypeCode);
			}
			// a verifier
			sqlOrder = " order by container_code, project desc, sample_code, tag, exp_short_name";
		}
		List<Container> results = null;	
		
		// FDS 21/01/2016 fusion des 2 appel a jdbcTemplate.query
		if (containerCode != null) {
			sqlClause = " where container_code = ? ";
			queryObj = new Object[] { containerCode };	
			logger.debug("Modify 1 container " + containerCategoryCode +"("+ containerCode+ ") with SOLEXA view: "+ sqlView );
		} else {
			logger.debug("Modify containers " + containerCategoryCode + " with SOLEXA view: "+ sqlView );
		}
		results = this.jdbcTemplate.query("select * from " + sqlView + sqlClause + sqlOrder, queryObj 
			,new RowMapper<Container>() {
				@Override
				public Container mapRow(ResultSet rs, int rowNum) throws SQLException {
					ResultSet rs0 = rs;
					int rowNum0 = rowNum;
					ContextValidation ctxErr = contextError;
					// en modification passer importState=null... ecrasera l'ancienne  valeur et mettra 'In Stock' ??????
					Container c=  commonContainerMapRow(rs0, rowNum0, ctxErr, containerCategoryCode, experimentTypeCode, null);
					return c;
				}
			});			
		
		//FDS NOTE: c'est dans demultiplexContainer.createContent() que sont crees le(s) content(s) d'un container
		return demultiplexContainer(results);			
	}
	
	/* ************************************************************************************************************************************************
	 * FDS 14/01/2016 DEPRECATED..on n'importe plus les flowcells
	 * Sub-method to set the sequencingProgramType of a flowcell
	 * @param contextError
	 * @param mode
	 * @return
	 * @throws DAOException
	 */	
	public HashMap<String, PropertyValue>  setSequencingProgramTypeToContainerSupport(final ContextValidation contextError, String mode)  throws DAOException {
		String sqlView;
		String sqlQuery;
		
		if (mode.equals("creation")) {
			sqlView = "v_flowcell_tongl"; 
		} else {
			sqlView = "v_flowcell_updated_tongl";
		}
		
		List<ContainerSupport> results = null;
		
		sqlQuery= "select support_code, seq_program_type from " + sqlView + " order by container_code, project desc, sample_code, tag, exp_short_name";
		results = this.jdbcTemplate.query(sqlQuery, new Object[]{} 
		,new RowMapper<ContainerSupport>() {
			@Override
			public ContainerSupport mapRow(ResultSet rs, int rowNum) throws SQLException {
				ResultSet rs0 = rs;
				int rowNum0 = rowNum;
				ContextValidation ctxErr = contextError; 
				ContainerSupport c = commonContainerSupportMapRow(rs0, rowNum0, ctxErr); 
				return c;
			}
		});
		// map data
		HashMap<String,PropertyValue> mapCodeSupportSequencing = new HashMap<>(); // <String,PropertyValue<String>>();
		for (ContainerSupport result : results) {
			if (!mapCodeSupportSequencing.containsKey(result.code)) {
				// mapCodeSupportSequencing.put(result.code, result.properties.get("sequencingProgramType"));
//				mapCodeSupportSequencing.put(result.code, (PropertyValue<String>)result.properties.get("sequencingProgramType"));
//				@SuppressWarnings("unchecked") // no way around this cast
//				PropertyValue pvs = (PropertyValue)result.properties.get("sequencingProgramType");
				PropertyValue pvs = result.properties.get("sequencingProgramType");
				mapCodeSupportSequencing.put(result.code, pvs);
			}
		}	
		return mapCodeSupportSequencing;
	}
	
	/* ************************************************************************************************************************************************
	 * FDS 14/01/2016 DEPRECATED...on n'importe plus les flowcells
	 * for eventually find all the "depot" (in case of a migration) 
	 */
	public List<Experiment> findAllIlluminaDepotExperimentToCreate(final ContextValidation contextError, final String protocoleCode) throws DAOException {
		List<Experiment> results = this.jdbcTemplate.query("SELECT * FROM v_depotfc_tongl_reprise ORDER BY 1", new Object[]{} 
		,new RowMapper<Experiment>() {
			@Override
			public Experiment mapRow(ResultSet rs, int rowNum) throws SQLException {
				ResultSet rs0 = rs;
				int rowNum0 = rowNum;
				ContextValidation ctxErr = contextError; 
				Experiment e = ExperimentImport.experimentDepotIlluminaMapRow(rs0, rowNum0, ctxErr, protocoleCode); 
				return e;
			}
		}); 
		return results;
	}
	
	/* ************************************************************************************************************************************************
	 * FDS 14/01/2016 DEPRECATED...on n'importe plus les flowcells
	 * for normal use
	 */	
	public List<Experiment> findIlluminaDepotExperiment(final ContextValidation contextError, final String protocoleCode) throws DAOException {
		List<Experiment> results = this.jdbcTemplate.query("SELECT * FROM v_depotfc_tongl ORDER BY 1", new Object[]{} 
		,new RowMapper<Experiment>() {
			@Override
			public Experiment mapRow(ResultSet rs, int rowNum) throws SQLException {
				ResultSet rs0 = rs;
				int rowNum0 = rowNum;
				ContextValidation ctxErr = contextError; 
				Experiment e = ExperimentImport.experimentDepotIlluminaMapRow(rs0, rowNum0, ctxErr, protocoleCode); 
				return e;
			}
		}); 
		return results;
	}
	
	/* ************************************************************************************************************************************************
	 * To get the indexes and update the "Parameter" collection
	 * FDS 30/04/2015: nglbi_code=>code, short_name=>shortName (et non plus code), cng_name=>name!
	 */	
	public List<Index> findIndexIlluminaToCreate(final ContextValidation contextError)throws SQLException {
		List<Index> results = this.jdbcTemplate.query("select nglbi_code, short_name, cng_name,(CASE WHEN type = 1 THEN 'SINGLE-INDEX'::text WHEN type = 2 THEN 'DUAL-INDEX'::text WHEN type = 3 THEN 'MID'::text ELSE NULL::text END) AS code_category,sequence from t_index order by 1" 
				,new RowMapper<Index>() {
//					@SuppressWarnings("rawtypes")
					@Override
					public Index mapRow(ResultSet rs, int rowNum) throws SQLException {
						Index index = new IlluminaIndex();
						index.code         = rs.getString("nglbi_code");
						index.shortName    = rs.getString("short_name");
						index.name         = rs.getString("cng_name");
						index.categoryCode = rs.getString("code_category");
						index.sequence     = rs.getString("sequence");
//						index.traceInformation=new TraceInformation();
//						InstanceHelpers.updateTraceInformation(index.traceInformation, Constants.NGL_DATA_USER);
						index.setTraceCreationStamp(contextError, Constants.NGL_DATA_USER); // Assume creation from method name
						logger.info("index code: {}", index.code);
						return index;
					}
				});
		return results;
	}

	/* ************************************************************************************************************************************************
	 * UPDATE Solexa tables t_sample & t_individual tables (import/update dates) 
	 * @param samples
	 * @param contextError
	 * @throws DAOException
	 */
	public void updateLimsSamples(List<Sample> samples, ContextValidation contextError, String mode) throws DAOException {
		String key, column;
		if (mode.equals("creation")) {
			key = "update_ImportDate";
			column = "nglimport_date";
		} else {
			key = "update_UpdateDate";
			column = "ngl_update_date";			
		}
		
		contextError.addKeyToRootKeyName(key);
		
		String sql = "UPDATE t_sample SET " + column + " = ? WHERE stock_barcode = ?";
		List<Object[]> parameters = new ArrayList<>();
		for (Sample sample : samples) {
	        parameters.add(new Object[] {new Date(), sample.code}); 
		}
		this.jdbcTemplate.batchUpdate(sql, parameters);  
		
		sql = "UPDATE t_individual SET " + column + " = ? WHERE id in (select individual_id from t_sample where stock_barcode = ?)";
		parameters = new ArrayList<>();
		for (Sample sample : samples) {
	        parameters.add(new Object[] {new Date(), sample.code}); 
		}
		this.jdbcTemplate.batchUpdate(sql, parameters);  
		
		contextError.removeKeyFromRootKeyName(key);
	}
	
	/* ************************************************************************************************************************************************
	 * FDS 14/01/2016 DEPRECATED on n'importe plus les lanes...
	 * UPDATE Solexa table t_lane (import/update dates) 
	 * @param containers
	 * @param contextError
	 * @throws DAOException
	 */
	public void updateLimsLanes(List<Container> containers, ContextValidation contextError, String mode) throws DAOException {
		String key, column;
		if (mode.equals("creation")) {
			key = "update_ImportDate";
			column = "nglimport_date";
		} else {
			key = "update_UpdateDate";
			column = "ngl_update_date";			
		}
		
		contextError.addKeyToRootKeyName(key);
		
		String sql = "UPDATE t_lane SET " + column + " = ? WHERE id = ?";
		List<Object[]> parameters = new ArrayList<>();
		for (Container container : containers) {
	        parameters.add(new Object[] {new Date(), container.properties.get("limsCode").value}); 
		}
//		this.jdbcTemplate.batchUpdate(sql, parameters);  
		jdbcTemplate.batchUpdate(sql, parameters);  
		
		sql = "UPDATE t_sample_lane SET " + column + " = ? WHERE lane_id = ?";
		parameters = new ArrayList<>();
		for (Container container : containers) {
	        parameters.add(new Object[] {new Date(), container.properties.get("limsCode").value}); 
		}
//		this.jdbcTemplate.batchUpdate(sql, parameters);   		
		jdbcTemplate.batchUpdate(sql, parameters);   		
		contextError.removeKeyFromRootKeyName(key);
	}
	
	/* ************************************************************************************************************************************************
	 * UPDATE tube containers import/update dates 
	 * @param containers
	 * @param contextError
	 * @param mode
	 * @throws DAOException
	 */
	public void updateLimsTubes(List<Container> containers, ContextValidation contextError, String mode) throws DAOException {
		String key, column;
		if (mode.equals("creation")) {
			key = "update_ImportDate";
			column = "nglimport_date";
		} else {
			key = "update_UpdateDate";
			column = "ngl_update_date";			
		}
		
		contextError.addKeyToRootKeyName(key);
		
		String sql = "UPDATE t_tube SET " + column + " = ? WHERE id = ?";
		List<Object[]> parameters = new ArrayList<>();
		for (Container container : containers) {
	        parameters.add(new Object[] {new Date(), container.properties.get("limsCode").value}); 
		}
		try {
			this.jdbcTemplate.batchUpdate(sql, parameters);
		} catch(Exception e) {
			logger.debug(e.getMessage());
		}
		contextError.removeKeyFromRootKeyName(key);
	}
	
	/* ************************************************************************************************************************************************
	 * FDS 18/01/2016 UPDATE sample plates import/update dates 
	 *    Pour les plaques de samples, il est impossible de passer par la mise a jour de la table t_sample
	 *    qui est deja mise a jour lors des sample au sens NGL..
	 *    on ne peut donc que passer par la mise a jour de la table t_group...
	 * @param containers
	 * @param contextError
	 * @param mode
	 * @throws DAOException
	 */
	public void updateLimsSamplePlates(List<Container> containers, ContextValidation contextError, String mode) throws DAOException {
		String key, column;
		if (mode.equals("creation")) {
			key = "update_ImportDate";
			column = "nglimport_date";
		} else {
			key = "update_UpdateDate";
			column = "ngl_update_date";			
		}
		contextError.addKeyToRootKeyName(key);
		String sql = "UPDATE t_group SET " + column + " = ? WHERE name = ? and type=4";
		List<Object[]> parameters = new ArrayList<>();
		// ceci va updater une plaque autant de fois qu'elle a de puits ==> A ameliorer !!!!!!
		for (Container container : containers) {
	        parameters.add(new Object[] {new Date(), container.support.code}); 
		}
		try {
			this.jdbcTemplate.batchUpdate(sql, parameters);
		} catch(Exception e) {
			logger.debug(e.getMessage());
		}
		contextError.removeKeyFromRootKeyName(key);
	}
	
	/* ************************************************************************************************************************************************
	 * FDS 08/06/2016 UPDATE library plates import/update dates 
	 * @param containers
	 * @param contextError
	 * @param mode
	 * @throws DAOException
	 */
	public void updateLimsTubePlates(List<Container> containers, ContextValidation contextError, String mode) throws DAOException {
		String key, column;
		if (mode.equals("creation")) {
			key = "update_ImportDate";
			column = "nglimport_date";
		} else {
			key = "update_UpdateDate";
			column = "ngl_update_date";			
		}
		contextError.addKeyToRootKeyName(key);
		//-1-mise a jour de la plaque ( table t_group avec type=4)
		String sql = "UPDATE t_group SET " + column + " = ? WHERE name = ? and type=4";
		List<Object[]> parameters = new ArrayList<>();
		// ceci va updater une plaque autant de fois qu'elle a de puits ==> A ameliorer !!!!!!
		for (Container container : containers) {
	        parameters.add(new Object[] {new Date(), container.support.code}); 
		}
		try {
//			this.jdbcTemplate.batchUpdate(sql, parameters);
			jdbcTemplate.batchUpdate(sql, parameters);
		} catch(Exception e) {
			logger.debug(e.getMessage());
		}
		
		//-2- mise a jour du puits ( table t_tube )
		sql = "UPDATE t_tube SET " + column + " = ? WHERE id = ?";
		parameters = new ArrayList<>();
		for (Container container : containers) {
			      parameters.add(new Object[] {new Date(), container.properties.get("limsCode").value}); 
		}
		try {
			this.jdbcTemplate.batchUpdate(sql, parameters);
		} catch(Exception e) {
			logger.debug(e.getMessage());
		}	
		contextError.removeKeyFromRootKeyName(key);
	}
	
	
	/* ************************************************************************************************************************************************
	 * FDS 14/10/2016 DEPRECATED...on n'importe plus les flowcell..
	 * UPDATE main table witch contains experiments of type "depots" in Solexa to keep trace of the imports
	 * @param experiments
	 * @param contextError
	 * @param mode
	 * @throws DAOException
	 */
	public void updateLimsDepotExperiment(List<Experiment> experiments, ContextValidation contextError, String mode) throws DAOException {
		String key, column;
		key = "update_synchroDate";
		column = "ngl_synchro_date";			
		
		contextError.addKeyToRootKeyName(key);
		
		String sql = "UPDATE t_workflow SET " + column + " = ? WHERE id = ?";
		List<Object[]> parameters = new ArrayList<>();
		for (Experiment experiment : experiments) {
	        parameters.add(new Object[] {new Date(), experiment.experimentProperties.get("limsCode").value}); 
		}
		try {
			this.jdbcTemplate.batchUpdate(sql, parameters);
		} catch(Exception e) {
			logger.debug(e.getMessage());
		}
		contextError.removeKeyFromRootKeyName(key);
	}
	
	// 21/03/2017 TX Nicolas=> il n'y a pas dans jdbctemplate une methode qui retourne map <string,string> donc il faut l'implementer
	private class MyExtractor implements ResultSetExtractor<Map<String,String>> {
		
		@Override
		public Map<String, String> extractData(ResultSet rs) throws SQLException, org.springframework.dao.DataAccessException {
			Map<String,String> map = new HashMap<>();
			while (rs.next()) {
				String key = rs.getString(1);
				map.put(key, rs.getString(2));
			}
			return map;
		}
		
	}
	
	// 21/03/2017 Les vieux echantillons ont été importés sans leur sample type (ils sont 'defaut-sample-cng' actuellement) trouver leur vrai sample type
	//                il y a 3 types qui doivent etre recodés, les autres sont corrects
	public Map<String, String> findOldSampleTypes() throws DAOException {
		MyExtractor extractor = new MyExtractor();
		// key=stock_barcode   value=sample_type
		String sql =  "SELECT  s.stock_barcode AS sample_code, "
			       + " CASE WHEN st.name='DNA' THEN 'gDNA' "
			       + "      WHEN st.name='MBD' THEN 'methylated-base-DNA' "
			       + "      WHEN st.name='UNK' THEN 'default-sample-cng' "
			       + " ELSE st.name "
			       + " END AS sample_type "
			       + " FROM t_sample s join t_sample_type st on s.type_id=st.id "
			       + " WHERE nglimport_date < '09/15/2016'"; 

		 return jdbcTemplate.query(sql, extractor);
	}
	
}
