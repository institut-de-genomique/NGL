package models;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import fr.cea.ig.MongoDBDAO;
import models.laboratory.common.description.Level;
import models.laboratory.common.instance.Comment;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TBoolean;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.common.instance.TransientState;
import models.laboratory.common.instance.Valuation;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.Content;
import models.laboratory.parameter.index.IlluminaIndex;
import models.laboratory.parameter.index.Index;
import models.laboratory.processes.description.ProcessType;
import models.laboratory.processes.instance.Process;
import models.laboratory.project.instance.BioinformaticParameters;
import models.laboratory.project.instance.Project;
import models.laboratory.run.description.ReadSetType;
import models.laboratory.run.description.TreatmentType;
import models.laboratory.run.instance.File;
import models.laboratory.run.instance.InstrumentUsed;
import models.laboratory.run.instance.Lane;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.run.instance.Run;
import models.laboratory.run.instance.Treatment;
import models.laboratory.sample.description.SampleType;
import models.laboratory.sample.instance.Sample;
import models.util.DataMappingCNS;
import models.util.MappingHelper;
import models.utils.CodeHelper;
import models.utils.InstanceConstants;
import models.utils.InstanceHelpers;
import models.utils.ListObject;
import models.utils.dao.DAOException;
import play.Logger;
import play.api.modules.spring.Spring;
import services.instance.container.ContainerImportCNS;
import validation.ContextValidation;
import validation.utils.ValidationConstants;


/**
 * @author mhaquell
 *
 */
@Repository
public class LimsCNSDAO{

	public JdbcTemplate jdbcTemplate;

	public static final String LIMS_CODE="limsCode";
	private static final String SAMPLE_ADPATER="isAdapters";
	protected static final String PROJECT_CATEGORY_CODE = "default";
	protected static final String PROJECT_TYPE_CODE_FG = "france-genomique";
	protected static final String PROJECT_TYPE_CODE_DEFAULT = "default-project";
	protected static final String PROJECT_PROPERTIES_FG_GROUP="fgGroup";
	protected static final String NGSRG_CODE="ngsrg";
	protected static final String GLOBAL_CODE="global";
	protected static final String IMPORT_CATEGORY_CODE="sample-import";
	protected static final String RUN_TYPE_CODE = "ngsrg-illumina";

	protected static final String READSET_DEFAULT_CODE = "default-readset";


	@Autowired
	@Qualifier("lims")
	public void setDataSource(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);              
	}


	/**
	 * Find Tube Lims who have flag 'tubinNGL=0' ( this flag is update to 1 when Tube exists in NGL database)
	 * 
	 * @param contextError
	 * @return
	 */
	public List<Container> findContainersToCreate(String procedure,ContextValidation contextError, final String containerCategoryCode, final String containerStateCode, final String experimentTypeCode){

		List<Container> results = this.jdbcTemplate.query(procedure,new Object[]{} 
		,new RowMapper<Container>() {

			@SuppressWarnings("rawtypes")
			public Container mapRow(ResultSet rs, int rowNum) throws SQLException {

				Container container = null;
				try {
					container = ContainerImportCNS.createContainerFromResultSet(rs, containerCategoryCode,containerStateCode,experimentTypeCode);
				} catch (DAOException e) {
					Logger.error("",e);
				}
				return container;
			}

		});        

		return results;
	}


	public Sample findSampleToCreate(final ContextValidation contextError, String sampleCode) throws SQLException, DAOException {

		List<Sample> results = this.jdbcTemplate.query("pl_MaterielToNGLUn @nom_materiel=?",new Object[]{sampleCode} 
		,new RowMapper<Sample>() {

			@SuppressWarnings("rawtypes")
			public Sample mapRow(ResultSet rs, int rowNum) throws SQLException {

				Sample sample = new Sample();
				InstanceHelpers.updateTraceInformation(sample.traceInformation, "ngl-data");
				String tadco = rs.getString("tadco");
				String tprco = rs.getString("tprco");
				sample.code=rs.getString("code");

				Logger.debug("Code Materiel (adnco) :"+rs.getString(LIMS_CODE)+" , Type Materiel (tadco) :"+tadco +", Type Projet (tprco) :"+tprco);

				String sampleTypeCode=DataMappingCNS.getSampleTypeFromLims(tadco,tprco);

				if(sampleTypeCode==null){
					contextError.addErrors( "typeCode", "limsdao.error.emptymapping", tadco, sample.code);
					return null;
				}

				SampleType sampleType=null;
				try {
					sampleType = SampleType.find.findByCode(sampleTypeCode);
				} catch (DAOException e) {
					Logger.error("",e);
					return null;
				}


				if( sampleType==null ){
					contextError.addErrors("code", "error.codeNotExist", sampleTypeCode, sample.code);
					return null;
				}

				Logger.debug("Sample Type :"+sampleTypeCode);

				sample.typeCode=sampleTypeCode;


				sample.projectCodes=new HashSet<String>();
				sample.projectCodes.add(rs.getString("project"));

				sample.name=rs.getString("name");
				sample.referenceCollab=rs.getString("referenceCollab");
				sample.taxonCode=rs.getString("taxonCode");
				sample.comments=new ArrayList<Comment>();
				sample.comments.add(new Comment(rs.getString("comment"), "ngl-test"));
				sample.categoryCode=sampleType.category.code;

				sample.properties=new HashMap<String, PropertyValue>();
				MappingHelper.getPropertiesFromResultSet(rs,sampleType.propertiesDefinitions,sample.properties);

				//Logger.debug("Properties sample "+sample.properties.containsKey("taxonSize"));

				boolean tara=false;

//				if(rs.getInt("tara")==1){
//					tara=true;
//				}
//
//				if(tara){
//
//					Logger.debug("Tara sample "+sample.code);
//
//					TaraDAO  taraServices = Spring.getBeanOfType(TaraDAO.class);
//					if(sample.properties==null){ sample.properties=new HashMap<String, PropertyValue>();}
//
//					Map<String, PropertyValue> map=taraServices.findTaraSampleFromLimsCode(rs.getInt(LIMS_CODE),contextError);
//
//					if(map!=null){
//						sample.properties.putAll(map);
//					} else {
						tara=false;
//					}
//
//				}
				//Logger.debug("Adpatateur :"+sample.properties.get("adaptateur").value.toString());

				boolean adapter=false;
				if(sample.properties.get(SAMPLE_ADPATER)!=null){
					adapter= Boolean.parseBoolean(sample.properties.get(SAMPLE_ADPATER).value.toString());
				}

				sample.importTypeCode=DataMappingCNS.getImportTypeCode(tara,adapter);
				//Logger.debug("Import Type "+sample.importTypeCode);
				return sample;
			}


		});        

		if(results.size()==1)
		{
			//	Logger.debug("One sample");
			return results.get(0);
		}
		else return null;

	}



	public List<Project> findProjectToCreate(final ContextValidation contextError) throws SQLException, DAOException {
		List<Project> results = this.jdbcTemplate.query("pl_ProjetToNGL ",new Object[]{} 
		,new RowMapper<Project>() {

			@SuppressWarnings("rawtypes")
			public Project mapRow(ResultSet rs, int rowNum) throws SQLException {


				Project project = new Project();
				project.code = rs.getString(2).trim();
				project.name = rs.getString(1);
				String fgGroupe=rs.getString("groupefg");
				if(fgGroupe==null){
					project.typeCode=PROJECT_TYPE_CODE_DEFAULT;
				}
				else {
					project.typeCode=PROJECT_TYPE_CODE_FG;
					project.properties= new HashMap<String, PropertyValue>();
					project.properties.put(PROJECT_PROPERTIES_FG_GROUP, new PropertySingleValue(fgGroupe));
				}

				project.categoryCode=PROJECT_CATEGORY_CODE;

				project.state = new State(); 
				project.state.code="IP";
				project.state.user = InstanceHelpers.getUser();
				project.state.date = new Date();

				project.bioinformaticParameters = new BioinformaticParameters();

				if(null != rs.getString("maxadnnom")){
					project.lastSampleCode = project.code+"_"+rs.getString("maxadnnom");
					project.nbCharactersInSampleCode = rs.getString("maxadnnom").length();
				}
				project.archive = rs.getBoolean("prsarch");
				
				project.traceInformation=new TraceInformation();
				InstanceHelpers.updateTraceInformation(project.traceInformation, "ngl-data");
				return project;
			}
		});

		return results;
	}




	public List<ListObject> getListObjectFromProcedureLims(String procedure) {
		List<ListObject> listObjects = this.jdbcTemplate.query(procedure,
				new RowMapper<ListObject>() {
			public ListObject mapRow(ResultSet rs, int rowNum)
					throws SQLException {
				ListObject value = new ListObject();
				value.name = rs.getString(1);
				value.code = rs.getString(2);
				return value;
			}
		});
		return listObjects;
	}


	public void updateMaterielmanipLims(List<Container> containers,ContextValidation contextError) throws SQLException{

		String limsCode=null;
		String rootKeyName=null;

		contextError.addKeyToRootKeyName("updateMaterielmanipLims");

		for(Container container:containers){

			rootKeyName="container["+container.code+"]";
			contextError.addKeyToRootKeyName(rootKeyName);
			limsCode=container.properties.get(LIMS_CODE).value.toString();

			if(container.properties==null || limsCode==null )
			{
				contextError.addErrors("limsCode","error.PropertyNotExist",LIMS_CODE,container.support.code);

			}else {
				try{
					if(!limsCode.equals("0")){
						String sql="pm_MaterielmanipInNGL @matmaco=?";
						Logger.debug(sql+limsCode);
						this.jdbcTemplate.update(sql, Integer.parseInt(limsCode));
					}
				} catch(DataAccessException e){

					contextError.addErrors("",e.getMessage(), container.support.code);
				}
			}

			contextError.removeKeyFromRootKeyName(rootKeyName);


		}

		contextError.removeKeyFromRootKeyName("updateMaterielmanipLims");
	}


	public void updateMaterielLims(Sample sample,ContextValidation contextError) throws SQLException{

		String rootKeyName=null;

		contextError.addKeyToRootKeyName("updateMaterielLims");
		rootKeyName="container["+sample.code+"]";
		contextError.addKeyToRootKeyName(rootKeyName);

		if(sample.code==null)
		{
			contextError.addErrors("code","error.NotExist",sample.code);

		}else {
			try{

				String sql="pm_SampleInNGL @code=?";
				Logger.debug(sql+sample.code);
				this.jdbcTemplate.update(sql, sample.code);

			} catch(DataAccessException e){

				contextError.addErrors("",e.getMessage(), sample.code);
			}
		}

		contextError.removeKeyFromRootKeyName(rootKeyName);

		contextError.removeKeyFromRootKeyName("updateMaterielLims");
	}


	/**
	 *  Find contents from a container code 
	 *  
	 *  */
	public List<Content> findContentsFromContainer(String sqlContent, String code) throws SQLException{

		List<Content> results = this.jdbcTemplate.query(sqlContent,new Object[]{code} 
		,new RowMapper<Content>() {

			@SuppressWarnings("rawtypes")
			public Content mapRow(ResultSet rs, int rowNum) throws SQLException {

				Content sampleUsed = new Content(rs.getString("sampleCode"),null,null);
				sampleUsed.projectCode = rs.getString("project");
				//TODO add projectCode
				// Todo add properties from ExperimentType
				sampleUsed.properties=new HashMap<String, PropertyValue>();
				if(rs.getString("percentPerLane")!=null){
					sampleUsed.properties.put("percentPerLane", new PropertySingleValue(rs.getDouble("percentPerLane")));
					sampleUsed.percentage=rs.getDouble("percentPerLane");
				}else {	
					sampleUsed.percentage=rs.getDouble("percentage");
				}
				sampleUsed.properties.put("libProcessTypeCode",new PropertySingleValue(rs.getString("libProcessTypeCode")));
				if(rs.getString("tag")!=null){
					sampleUsed.properties.put("tag", new PropertySingleValue(rs.getString("tag")));
					sampleUsed.properties.put("tagCategory",new PropertySingleValue(rs.getString("tagCategory")));
				}
				
				if(rs.getString("libLayoutNominalLength") != null){
					sampleUsed.properties.put("libLayoutNominalLength", new PropertySingleValue(rs.getInt("libLayoutNominalLength")));
				}
				
				if(rs.getString("sampleAliquoteCode") !=null){
					sampleUsed.properties.put("sampleAliquoteCode", new PropertySingleValue(rs.getString("sampleAliquoteCode")));
				}
				
				return sampleUsed;
			}

		});        

		return results;

	}

	public List<Run> findRunsToCreate(String sqlContent,final ContextValidation contextError)throws SQLException{

		List<Run> results = this.jdbcTemplate.query(sqlContent,new RowMapper<Run>() {

			@SuppressWarnings("rawtypes")
			public Run mapRow(ResultSet rs, int rowNum) throws SQLException {
				Logger.debug("Begin findRunsToCreate");

				ContextValidation contextValidation=new ContextValidation(Constants.NGL_DATA_USER);
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
				//TODO fin de tranfert


				Valuation valuation=new Valuation();
				run.valuation=valuation;

				//
				run.valuation.valid=TBoolean.valueOf(rs.getString("validationValid"));
				run.valuation.user="lims";
				run.valuation.date=rs.getDate("validationDate");
				//TODO	run.validation.resolutionCodes
				State state = new State();
				run.state = state;
				run.state.code = DataMappingCNS.getStateRunFromLims(run.valuation.valid);
				run.state.user = NGSRG_CODE;
				run.state.date = new Date();

				run.state.historical=new HashSet<TransientState>();		
				run.state.historical.add(getTransientState(rs.getDate("beginNGSRG"),"IP-RG",0));
				run.state.historical.add(getTransientState(rs.getDate("endNGSRG"),"F-RG",1));				

				TraceInformation ti = new TraceInformation(); 
				ti.setTraceInformation(NGSRG_CODE);
				run.traceInformation = ti; 

				contextValidation.addKeyToRootKeyName("run["+run.code+"]");
				run.treatments=new HashMap<String, Treatment>();
				run.treatments.put(NGSRG_CODE,newTreatment(contextValidation,rs, Level.CODE.Run,NGSRG_CODE,NGSRG_CODE,RUN_TYPE_CODE));
				contextValidation.removeKeyFromRootKeyName("run["+run.code+"]");

				if(rs.getString("sequencingProgramType")!=null){
					run.properties.put("sequencingProgramType",new PropertySingleValue(rs.getString("sequencingProgramType")));
				}

				if(contextValidation.hasErrors()){
					contextError.errors.putAll(contextValidation.errors);
					return null;
				}else {
					return run;
				}
			}

		});        

		return results;
	}


	protected TransientState getTransientState(java.sql.Date date,String state, int index) {
		if(date!=null){
			TransientState transientState=new TransientState();
			transientState.date=date;
			transientState.index=index;
			transientState.code=state;
			transientState.user=NGSRG_CODE;	
			return transientState;
		}
		return null;
	}


	public List<Lane> findLanesToCreateFromRun(final Run run,final ContextValidation contextError)throws SQLException{

		List<Lane> results = this.jdbcTemplate.query("pl_LaneUnRunToNGL @runCode=?",new Object[]{run.code} 
		,new RowMapper<Lane>() {
			@SuppressWarnings("rawtypes")
			public Lane mapRow(ResultSet rs, int rowNum) throws SQLException {

				Lane lane=getLane(run,rs.getInt("lanenum"));

				if(lane==null){
					Logger.debug("Lane null");
					lane=new Lane();
					lane.number=rs.getInt("lanenum");
				}
				lane.valuation=new Valuation();
				lane.valuation.valid=TBoolean.valueOf(rs.getString("validationValid"));
				lane.valuation.user="lims";
				lane.valuation.date=rs.getDate("validationDate");
				//TODO 
				contextError.addKeyToRootKeyName("lane["+lane.number+"].treatment[default]");
				Treatment treatment=newTreatment(contextError,rs,Level.CODE.Lane,NGSRG_CODE,NGSRG_CODE,RUN_TYPE_CODE);
				if(treatment==null){
					return null;
				}else{
					lane.treatments.put(NGSRG_CODE,treatment);
				}
				contextError.removeKeyFromRootKeyName("lane["+lane.number+"].treatment[default]");
				return lane;
			}
		});
		return results;
	}


	protected Lane getLane(Run run, int int1) {
		if(run.lanes!=null){
			for(Lane lane:run.lanes){
				if(lane.number==int1){
					return lane;
				}
			}}
		return null;
	}


	public Treatment newTreatment(ContextValidation contextError,ResultSet rs,Level.CODE level,String categoryCode,String code,String typeCode) throws SQLException{
		Treatment treatment=new Treatment();
		treatment.categoryCode=categoryCode;
		treatment.code=code;
		treatment.typeCode=typeCode;
		Map<String,PropertyValue> m = new HashMap<String,PropertyValue>();

		try {
			TreatmentType treatmentType=TreatmentType.find.findByCode(treatment.typeCode);
			if(treatmentType==null){
				contextError.addErrors("treatmentType","error.codeNotExist",treatment.typeCode);
				return null;
			}else {
				MappingHelper.getPropertiesFromResultSet(rs, treatmentType.getPropertyDefinitionByLevel(level),m);
			}
		} catch (DAOException e) {
			Logger.error("",e);
		}
		treatment.results=new HashMap<String, Map<String,PropertyValue>>();
		treatment.results.put("default",m);
		return treatment;
	}

	public List<ReadSet> findReadSetToCreateFromRun(final Run run,
			final ContextValidation contextError) throws SQLException{
		List<ReadSet> results = this.jdbcTemplate.query("pl_ReadSetUnRunToNGL @runCode=?",new Object[]{run.code} 
		,new RowMapper<ReadSet>() {
			@SuppressWarnings("rawtypes")
			public ReadSet mapRow(ResultSet rs, int rowNum) throws SQLException {
				ReadSet readSet=new ReadSet();
				readSet.code=rs.getString("code");
				readSet.archiveId=rs.getString("archiveId");
				readSet.archiveDate=rs.getDate("archiveDate");
				readSet.dispatch=rs.getBoolean("dispatch");
				readSet.laneNumber=rs.getInt("laneNumber");
				readSet.path=rs.getString("readSetPath");
				readSet.projectCode=rs.getString("projectCode");
				readSet.runCode=run.code;
				readSet.runTypeCode=run.typeCode;
				readSet.sampleCode=rs.getString("sampleCode");
				readSet.state=new State();
				readSet.state.code=DataMappingCNS.getStateReadSetFromLims(rs.getString("state"),TBoolean.valueOf(rs.getString("validationBioinformatic")));
				readSet.state.date= new Date();
				readSet.state.user="lims";

				readSet.state.historical=new HashSet<TransientState>();		
				readSet.state.historical.add(getTransientState(rs.getDate("beginNGSRG"),"IP-RG",0));
				readSet.state.historical.add(getTransientState(rs.getDate("endNGSRG"),"F-RG",1));
				readSet.state.historical.add(getTransientState(rs.getDate("endNGSRG"),"IW-QC",2));

				readSet.traceInformation=new TraceInformation();
				readSet.traceInformation.setTraceInformation("lims");
				readSet.typeCode=READSET_DEFAULT_CODE;
				readSet.archiveDate=rs.getDate("archiveDate");
				readSet.archiveId=rs.getString("archiveId");
				readSet.runSequencingStartDate=rs.getDate("runSequencingStartDate");
				//To valide
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
			}
		});
		return results;
	}


	public ReadSet findReadSetToUpdate(final ReadSet readSet,
			final ContextValidation contextError) throws SQLException{
		ReadSet results = this.jdbcTemplate.queryForObject("pl_ReadSetUnRunToNGL @readSetCode=?",new Object[]{readSet.code} 
		,new RowMapper<ReadSet>() {
			@SuppressWarnings("rawtypes")
			public ReadSet mapRow(ResultSet rs, int rowNum) throws SQLException {
				ReadSet readSet=new ReadSet();
				readSet.code=rs.getString("code");
				readSet.archiveId=rs.getString("archiveId");
				readSet.archiveDate=rs.getDate("archiveDate");
				readSet.dispatch=rs.getBoolean("dispatch");
				readSet.laneNumber=rs.getInt("laneNumber");
				readSet.path=rs.getString("readSetPath");
				readSet.projectCode=rs.getString("projectCode");
				readSet.runCode=readSet.runCode;
				readSet.runTypeCode=readSet.runTypeCode;
				readSet.sampleCode=rs.getString("sampleCode");
				readSet.state=new State();
				readSet.state.code=DataMappingCNS.getStateReadSetFromLims(rs.getString("state"),TBoolean.valueOf(rs.getString("validationBioinformatic")));
				readSet.state.date= new Date();
				readSet.state.user="lims";

				readSet.state.historical=new HashSet<TransientState>();		
				readSet.state.historical.add(getTransientState(rs.getDate("beginNGSRG"),"IP-RG",0));
				readSet.state.historical.add(getTransientState(rs.getDate("endNGSRG"),"F-RG",1));
				readSet.state.historical.add(getTransientState(rs.getDate("endNGSRG"),"IW-QC",2));

				readSet.traceInformation=new TraceInformation();
				readSet.traceInformation.setTraceInformation("lims");
				readSet.typeCode=READSET_DEFAULT_CODE;
				readSet.archiveDate=rs.getDate("archiveDate");
				readSet.archiveId=rs.getString("archiveId");
				readSet.runSequencingStartDate=rs.getDate("runSequencingStartDate");
				//To valide
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
			}
		});
		return results;
	}

	public void updateRunLims(List<Run> updateRuns,
			ContextValidation contextError)throws SQLException {
		updateRunLims(updateRuns, true, contextError);
	}

	public void updateRunLims(List<Run> updateRuns, boolean inNGL,
			ContextValidation contextError)throws SQLException {
		String rootKeyName=null;

		contextError.addKeyToRootKeyName("updateRunLims");

		for(Run run:updateRuns){

			rootKeyName="run["+run.code+"]";
			contextError.addKeyToRootKeyName(rootKeyName);

			try{
				String sql="pm_RunhdInNGL @runhnom=?, @InNGL=?";
				Logger.debug(sql+run.code);
				int intInNGL = (inNGL) ? 1 : 0;
				this.jdbcTemplate.update(sql, run.code,intInNGL);

			} catch(DataAccessException e){
				contextError.addErrors("",e.getMessage(), run.code);
			}

			contextError.removeKeyFromRootKeyName(rootKeyName);

		}
		contextError.removeKeyFromRootKeyName("updateRunLims");

	}


	public List<File> findFileToCreateFromReadSet(final ReadSet readSet,final ContextValidation contextError)throws SQLException {

		List<File> results = this.jdbcTemplate.query("pl_FileUnReadSetToNGL @readSetCode=?",new Object[]{readSet.code} 
		,new RowMapper<File>() {
			@SuppressWarnings("rawtypes")
			public File mapRow(ResultSet rs, int rowNum) throws SQLException {
				File file=new File();
				file.extension=rs.getString("extension");
				file.fullname=rs.getString("fullname");
				file.typeCode=rs.getString("typeCode");
				file.usable=rs.getBoolean("usable");

				ReadSetType readSetType = null;

				try {
					readSetType = ReadSetType.find.findByCode(readSet.typeCode);
				} catch (DAOException e) {
					Logger.error("",e);
				}
				file.properties=new HashMap<String, PropertyValue>();
				MappingHelper.getPropertiesFromResultSet(rs,readSetType.getPropertyDefinitionByLevel(Level.CODE.File),file.properties);

				return file;
			}
		});
		return results;

	}

	public List<Index> findIndexIlluminaToCreate(final ContextValidation contextError)throws SQLException {

		List<Index> results = this.jdbcTemplate.query("pl_TagUneEtmanip 13" 
				,new RowMapper<Index>() {
					@SuppressWarnings("rawtypes")
					public Index mapRow(ResultSet rs, int rowNum) throws SQLException {
						Index index=new IlluminaIndex();
						index.code=rs.getString("tagkeyseq");
						index.name=rs.getString("tagkeyseq");
						index.shortName=rs.getString("tagkeyseq");
						index.categoryCode=rs.getString("categoryCode");
						index.sequence=rs.getString("tagseq");
						index.supplierName=new HashMap<String, String>();
						index.supplierName.put("illumina",rs.getString("tagnamefour"));
						index.traceInformation=new TraceInformation();
						InstanceHelpers.updateTraceInformation(index.traceInformation, "ngl-data");
						return index;
					}
				});
		return results;

	}


	public List<String> findSampleUpdated(List<String> sampleCodes) {
		String sql="select code=rtrim(prsco)+'_'+rtrim(adnnom) from Materiel m, Useadn u where u.adnco=m.adnco and ";
		if(sampleCodes==null){
			sql=sql+"  uaddm > uadInNGL";
		}else {
			//Pour les tests unitaires
			sql=sql+" rtrim(prsco)+'_'+rtrim(adnnom) in (";
			for(String code:sampleCodes){
				sql=sql+"'"+code+"',";
			}
			sql=sql+"'')";
		}
		//Search Sample to modify
		List<String> results =  this.jdbcTemplate.query(sql 
				,new RowMapper<String>() {

			public String mapRow(ResultSet rs, int rowNum) throws SQLException {

				return rs.getString("code");
			}

		}); 

		return results;
	}


	public List<Process> findProcessToCreate(String sql, final Container container, String processTypeCode, final ContextValidation contextError) {
		List<Process> results=null;

		try {
			final ProcessType processType = ProcessType.find.findByCode(processTypeCode);
			if(processType!=null){
				results = this.jdbcTemplate.query(sql ,new Object[]{container.code}
			,new RowMapper<Process>() {
				@SuppressWarnings("rawtypes")
				public Process mapRow(ResultSet rs, int rowNum) throws SQLException {
					Process process=new Process();
					process.typeCode=processType.code;
					process.categoryCode=processType.category.code;
					process.inputContainerCode=container.code;
					process.projectCodes=new HashSet<String>();
					process.projectCodes.add(rs.getString("projectCode"));
					process.sampleCodes=new HashSet<String>();
					process.sampleCodes.add(rs.getString("sampleCode"));
					process.traceInformation=new TraceInformation();
					process.traceInformation.createUser=contextError.getUser();
					process.traceInformation.creationDate=new Date();
					process.state=new State("N",contextError.getUser());
					process.properties=new HashMap<String, PropertyValue>();
					MappingHelper.getPropertiesFromResultSet(rs,processType.getPropertyDefinitionByLevel(Level.CODE.Process),process.properties);
					process.code=CodeHelper.getInstance().generateProcessCode(process);
					Content c=null;
					for(Content content:container.contents){
						if(content.projectCode.equals(process.projectCodes) && content.sampleCode.equals(process.sampleCodes)){
							c=content;
						}
					}
					if(c!=null){
						process.sampleOnInputContainer=InstanceHelpers.getSampleOnInputContainer(c, container);
					}else { contextError.addErrors("content",ValidationConstants.ERROR_CODE_NOTEXISTS_MSG,container); }
					return process;
				}
			});
			}else { contextError.addErrors("processType", ValidationConstants.ERROR_CODE_NOTEXISTS_MSG, processTypeCode); }
			
			
		}	
		catch (DAOException e) {
			Logger.error("",e);
		}
		return results;

	}
	
	public ReadSet findLSRunProjData(ReadSet readset){
		
		List<ReadSet> results = this.jdbcTemplate.query("pl_LSRunProjUnReadSetToNGL @readSetCode=?", new String[]{readset.code} 
				,new RowMapper<ReadSet>() {
					@SuppressWarnings("rawtypes")
					public ReadSet mapRow(ResultSet rs, int rowNum) throws SQLException {
						ReadSet readset = new ReadSet();
						readset.code = rs.getString("code");
						//readset.location = rs.getString("location");
						//readset.path = rs.getString("path");
						if(null != rs.getString("strandOrientation")){
							readset.properties.put("strandOrientation", new PropertySingleValue(rs.getString("strandOrientation")));
						}
						if(null != rs.getString("insertSizeGoal")){
							readset.properties.put("insertSizeGoal", new PropertySingleValue(rs.getString("insertSizeGoal")));
						}	
						return readset;						
					}
				});
		
		if(results.size() != 1 ){
			//Logger.error("Probleme to load lsRunProjData with "+readset.code);
			return null;
		}else{
			return results.get(0);
		}
		
	}
	
	public List<ReadSet> findLSRunProjData(){
		
		List<ReadSet> results = this.jdbcTemplate.query("pl_LSRunProjUnReadSetToNGL" 
				,new RowMapper<ReadSet>() {
					@SuppressWarnings("rawtypes")
					public ReadSet mapRow(ResultSet rs, int rowNum) throws SQLException {
						ReadSet readset = new ReadSet();
						readset.code = rs.getString("code");
						readset.location = rs.getString("location");
						readset.path = rs.getString("path");
						if(null != rs.getString("strandOrientation")){
							readset.properties.put("strandOrientation", new PropertySingleValue(rs.getString("strandOrientation")));
						}
						if(null != rs.getString("insertSizeGoal")){
							readset.properties.put("insertSizeGoal", new PropertySingleValue(rs.getString("insertSizeGoal")));
						}	
						return readset;						
					}
				});
		
		return results;
		
	}
}

