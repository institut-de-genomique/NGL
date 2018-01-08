package models;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Iterator;

import javax.sql.DataSource;

import models.administration.authorisation.User;
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
import models.util.DataMappingGET;
import models.util.MappingHelper;
import models.utils.CodeHelper;
import models.utils.InstanceConstants;
import models.utils.InstanceHelpers;
import models.utils.ListObject;
import models.utils.dao.DAOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import play.Logger;
import play.api.modules.spring.Spring;
import services.instance.container.ContainerImportGET;
import validation.ContextValidation;
import validation.utils.ValidationConstants;
import fr.cea.ig.MongoDBDAO;


/**
 * @author mhaquell
 *
 */
@Repository
public class LimsGETDAO{

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
	@Qualifier("esitoul")
	public void setDataSource(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);              
	}

	public List<String> findProjectsForContainer(Integer barcodeId){

		String getProjectForBarcode = "SELECT pr.ident as project FROM project pr "
				+ "INNER JOIN trace_object_project top ON top.projectid=pr.projectid  "
				+ "WHERE top.object_id = ?"
                + "order by top.trace_object_project_id DESC";


		List<String> projects = new ArrayList<String>();
		List<Map<String,Object>>rows = this.jdbcTemplate.queryForList(getProjectForBarcode,barcodeId);
		for (Map<String, Object> row : rows) {
			projects.add(row.get("project").toString());
		}
		return projects;
	}
	
    public List<String> findProjectsForBarcode(String barcode){

        String getProjectForBarcode = "SELECT pr.ident as project FROM project pr "
                + "INNER JOIN trace_object_project top ON top.projectid=pr.projectid "
                + "WHERE top.object_id = (SELECT object_id FROM trace_object "
                + "                            WHERE object_barcode = ?) "
                + "ORDER BY top.trace_object_project_id DESC";

        List<String> projects = new ArrayList<String>();
        List<Map<String,Object>>rows = this.jdbcTemplate.queryForList(getProjectForBarcode,barcode);
        for (Map<String, Object> row : rows) {
            projects.add(row.get("project").toString());
        }
        return projects;
    }

	public List<User> findUsersToSynchronize(String SQLUsers){
		List<User> resultsUsers = this.jdbcTemplate.query(SQLUsers,new Object[]{},new RowMapper<User>() {

		@SuppressWarnings("rawtypes")
		public User mapRow(ResultSet rs, int rowNum) throws SQLException {
			User user = new User();
			try {
				
				user.code=rs.getString("ident");
				user.firstname=rs.getString("firstname");
				user.lastname=rs.getString("lastname");
				user.email=rs.getString("mail");
				user.password=rs.getString("password_crypt");
				user.active=true;
				if (!rs.getBoolean("actif")){
					user.active=false;
				}
				if (rs.getString("description").equals("Responsable Technique")){
					user.technicaluser = 1;
				}
				Logger.debug("LimsGETDAO - findUsersToSynchronize mapRow- : Utilisateur retourné = " + user.code);
			
			} catch (DAOException e) {
				Logger.error("LimsGETDAO - findUsersToSynchronize mapRow- Erreur",e);
			} 
							
			return user;
		}

	});
	return resultsUsers;
	}
	/**
	 * Find Tube Lims who have flag 'tubinNGL=0' ( this flag is update to 1 when Tube exists in NGL database)
	 * 
	 * @param contextError
	 * @return
	 */
	public List<Container> findContainersToCreate(String procedure,ContextValidation contextError, final String containerCategoryCode, final String containerStateCode, final String experimentTypeCode){
		
		List<Container> results = this.jdbcTemplate.query(procedure,new Object[]{},new RowMapper<Container>() {

			@SuppressWarnings("rawtypes")
			public Container mapRow(ResultSet rs, int rowNum) throws SQLException {

				Container container = null;
				try {
					container = ContainerImportGET.createContainerFromResultSet(rs, containerCategoryCode,containerStateCode,experimentTypeCode);
					Logger.debug("LimsGETDAO - findContainersToCreate apres createContainerFromResultSet container.categoryCode: " + container.categoryCode);
				
				} catch (DAOException e) {
					Logger.error("LimsGETDAO - findContainersToCreate mapRow- Erreur",e);
				} 
								
				return container;
			}

		});        
		
		//check before record in Mongo
		List<Container> listContenairesNonVide = new ArrayList<Container>();
		for (Container cont : results) {
			if (cont.categoryCode != null){
				listContenairesNonVide.add(cont);
			}else {
				Logger.error("LimsGETDAO - findContainersToCreate categoryCode idefini pour : " + cont.code + " il est ne donc PAS IMPORTE dans Mongo");
			}
		}
		
//		for (Container container : results) {
//				Logger.error("LimsGETDAO - findContainersToCreate : " + container.toString());
//		}
		return listContenairesNonVide;
	}


	public Sample findSampleToCreate(final ContextValidation contextError, String sampleCode) throws SQLException, DAOException {
		Logger.debug("LimsGETDAO - findSampleToCreate - findSampleToCreate : " + sampleCode);
		String SQLSample="SELECT  DISTINCT "
				+ "tob.object_barcode as code,"
				+"tob.object_id as barcodeid,"
				+ "tob.object_barcode as name, "
				+ "tot.type_object_name as typeCode, "
				+ "tot.type_object_name as categoryCode,"
				+ "tob.object_barcode as sampleCode, "
				+ "tob.description as comment,"
				+ "tob.localization_barcode as codeSupport, "
				+ "tob.position_on_real_localization as position, "
				+ "tot.max_position as nbContainer, "
				+ "tob.creation_date as receptionDate "
				+", pep.ident as createUser "
				+ "FROM trace_object tob "
				+ "INNER JOIN people pep on tob.userid = pep.userid "
				+ "INNER JOIN trace_object_type tot on tot.type_object_id = tob.type_object_id "
				+ "WHERE tob.object_barcode=?";
		//Logger.debug("SQLSample " + SQLSample);
		List<Sample> results = this.jdbcTemplate.query(SQLSample,new Object[]{sampleCode} 
		,new RowMapper<Sample>() {

			@SuppressWarnings("rawtypes")
			public Sample mapRow(ResultSet rs, int rowNum) throws SQLException {

				Sample sample = new Sample();
				
				if (rs.getString("createUser") != null){
					Logger.debug("sample.createUser " + rs.getString("createUser"));
					InstanceHelpers.updateTraceInformation(sample.traceInformation, rs.getString("createUser"));
				}else{
					InstanceHelpers.updateTraceInformation(sample.traceInformation, "ngl-data");
				}
				
				Logger.debug("LimsGETDAO - FindSample mapRow:   param query " + sampleCode);
				sample.code=rs.getString("code");
				sample.properties=new HashMap<String, PropertyValue>();
				sample.properties = getCaracteristiquesForContainer(rs.getInt("barcodeid"));
				
				if (sample.properties.containsKey("Nom_echantillon_collaborateur")){
					PropertySingleValue aa ;
					aa = (PropertySingleValue)sample.properties.get("Nom_echantillon_collaborateur");
					Logger.debug("LimsGETDAO - FindSample mapRow if Nom_echantillon_collaborateur: " + aa.toString() );
					sample.referenceCollab = aa.value.toString();
//					container.concentration = new PropertySingleValue(Math.round(rs.getFloat("measuredConcentration")*100.0)/100.0, mesuredConcentrationUnit);
				}
				String sampleTypeCode = new String();
				SampleType sampleType=null;
				if (sample.properties.containsKey("type_echantillon")){
					PropertySingleValue aa ;
					aa = (PropertySingleValue)sample.properties.get("type_echantillon");
					Logger.debug("LimsGETDAO - mapRow, type_echantillon : " + aa.value.toString());
//					sampleTypeCode = aa.value.toString();
//					container.concentration = new PropertySingleValue(Math.round(rs.getFloat("measuredConcentration")*100.0)/100.0, mesuredConcentrationUnit);
					
					sampleTypeCode=DataMappingGET.getSampleTypeFromLims(aa.value.toString());
					Logger.debug("LimsGETDAO - mapRow if, apres getSampleTypeFromLims");
					try {
//						Logger.info("LimsGETDAO - findSampleToCreate : ERROR CODE findByCode " + sampleTypeCode);
						sampleType = SampleType.find.findByCode(sampleTypeCode);
						
					} catch (DAOException e) {
						Logger.error("LimsGETDAO - findSampleToCreate : catch findByCode " + e.toString(),e);
						return null;
					}
					if( sampleType==null ){
						contextError.addErrors("code <", "error.codeNotExist", sampleTypeCode, sample.code);
						return null;
					}
					sample.categoryCode=sampleType.category.code;
					sample.typeCode=sampleTypeCode;
					Logger.debug("ContainerImportGET - createContainerFromResultSet, type_echantillon , sampleType.category.code : " + sampleType.category.code);
				}
				
				sample.projectCodes=new HashSet<String>();
				List<String> projects = new ArrayList<String>();
				projects = findProjectsForContainer(rs.getInt("barcodeid"));
				for (String project : projects) {
					Logger.debug("LimsGETDAO - findSampleToCreate - Projet lié à sample " + sample.code + "  : " + project);
					sample.projectCodes.add(project);
				}
				sample.name=rs.getString("name");
//				sample.referenceCollab=rs.getString("referenceCollab");

				sample.comments=new ArrayList<Comment>();
				sample.comments.add(new Comment(rs.getString("comment"), "ngl-data"));
				
				sample.properties = new HashMap<String, PropertyValue>();

				Logger.debug("LimsGETDAO - findSampleToCreate - Properties sample "+sample.properties.containsKey("taxonSize"));


				sample.importTypeCode="default-import";
				Logger.debug("LimsGETDAO - findSampleToCreate - END "+sample);
				return sample;
			}


		});        
		Logger.debug("LimsGETDAO - findSampleToCreate - END. Nombre de samples =   "+results.size());
		if(results.size()==1)
		{
			Logger.debug("LimsGETDAO - findSampleToCreate - END - One sample");
			return results.get(0);
		}
		else {
			Logger.error("LimsGETDAO - findSampleToCreate - END - Nombre de samples : " + Integer.toString((results.size())) + ". Retourne null du coup.");
			return null;
		}

		//				Sample sample = new Sample();
		//				InstanceHelpers.updateTraceInformation(sample.traceInformation, "ngl-data");
		//				String tadco = rs.getString("tadco");
		//				String tprco = rs.getString("tprco");
		//				sample.code=rs.getString("code");
		//				
		//				Logger.debug("Code Materiel (adnco) :"+rs.getString(LIMS_CODE)+" , Type Materiel (tadco) :"+tadco +", Type Projet (tprco) :"+tprco);
		//
		//				String sampleTypeCode=DataMappingGET.getSampleTypeFromLims(tadco,tprco);
		//
		//				if(sampleTypeCode==null){
		//					contextError.addErrors( "typeCode", "limsdao.error.emptymapping", tadco, sample.code);
		//					return null;
		//				}
		//
		//				SampleType sampleType=null;
		//				try {
		//					sampleType = SampleType.find.findByCode(sampleTypeCode);
		//				} catch (DAOException e) {
		//					Logger.error("",e);
		//					return null;
		//				}
		//
		//
		//				if( sampleType==null ){
		//					contextError.addErrors("code", "error.codeNotExist", sampleTypeCode, sample.code);
		//					return null;
		//				}
		//
		//				Logger.debug("Sample Type :"+sampleTypeCode);
		//
		//				sample.typeCode=sampleTypeCode;
		//
		//
		//				sample.projectCodes=new HashSet<String>();
		//				sample.projectCodes.add(rs.getString("project"));
		//
		//				sample.name=rs.getString("name");
		//				sample.referenceCollab=rs.getString("referenceCollab");
		//				sample.taxonCode=rs.getString("taxonCode");
		//
		//				sample.comments=new ArrayList<Comment>();
		//				sample.comments.add(new Comment(rs.getString("comment")));
		//				sample.categoryCode=sampleType.category.code;
		//
		//				sample.properties=new HashMap<String, PropertyValue>();
		//				MappingHelper.getPropertiesFromResultSet(rs,sampleType.propertiesDefinitions,sample.properties);
		//
		//				//Logger.debug("Properties sample "+sample.properties.containsKey("taxonSize"));
		//
		//				boolean tara=false;
		//
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
		//						tara=false;
		//					}
		//
		//				}
		//				//Logger.debug("Adpatateur :"+sample.properties.get("adaptateur").value.toString());
		//
		//				boolean adapter=false;
		//				if(sample.properties.get(SAMPLE_ADPATER)!=null){
		//					adapter= Boolean.parseBoolean(sample.properties.get(SAMPLE_ADPATER).value.toString());
		//				}
		//
		//				sample.importTypeCode=DataMappingGET.getImportTypeCode(tara,adapter);
		//				//Logger.debug("Import Type "+sample.importTypeCode);
		//				return sample;
		//			}
		//
		//
		//		});        
		//
		//		if(results.size()==1)
		//		{
		//			//	Logger.debug("One sample");
		//			return results.get(0);
		//		}
		//		else return null;

	}

	/**
	 * 
	 * @param caracteristicstypeid
	 * @return lastInsertid pour la caracteristique creee
	 * @throws SQLException
	 * @author okorovina
	 */
	public int createCaracteristicDateImportNGL(int caracteristicstypeid) throws SQLException {
		
		//get today date string
		String date = Constants.today();
		Logger.debug("Date : "+ date);
		
//		Object[] dateImportCaracteristic = new Object[] { new Date(),  caracteristicstypeid };
//		Object[] dateImportCaracteristic = new Object[] { date,  caracteristicstypeid };
		
		try {
			//try if today date characteristic exist
			Logger.debug("Resultat int : " + this.jdbcTemplate.queryForObject("SELECT caracteristique_id FROM trace_caracteristique WHERE caracteristique_type_id = '" + caracteristicstypeid + "' AND valeur = '"+ date +"'", Integer.class));
			
		} catch (Exception e) {			
			//if today date characteristic don't exist create a new characteristic with import NGL date type (id = 270)
			String insertSql =  "INSERT INTO trace_caracteristique(valeur,caracteristique_type_id) VALUES (?, ?)";
			this.jdbcTemplate.update(insertSql, new Object[] { date,  caracteristicstypeid});
			Logger.debug("new insertid"+ this.jdbcTemplate.queryForObject("SELECT caracteristique_id FROM trace_caracteristique WHERE caracteristique_type_id = '" + caracteristicstypeid + "' AND valeur = '"+ date +"'", Integer.class));
		} 
		
		
//		this.jdbcTemplate.update(insertSql, new Object[] { new Date(),  caracteristicstypeid});
//		//TODO revoir le requete de recup du dernier id de caracteristique
//		Logger.debug("LimsGETDAO - createCaracteristicDateImportNGL LAST INSERT ID  "+ this.jdbcTemplate.queryForInt("select max(caracteristique_id) FROM trace_caracteristique"));
//		//return this.jdbcTemplate.queryForInt( "select last_insert_id()" );
//		return this.jdbcTemplate.queryForInt("select max(caracteristique_id) FROM trace_caracteristique");
		
		//find and return today date characteristic type id
		return this.jdbcTemplate.queryForObject("SELECT caracteristique_id FROM trace_caracteristique WHERE caracteristique_type_id = '"+ caracteristicstypeid +"' AND valeur = '"+ date +"'", Integer.class);
	}



	/**
	 * Cette methode associe un barcode a la caracteristique Date_import_NGL
	 * 
	 * @param barcode
	 * @param caracteristicId
	 * @throws SQLException
	 * @author okorovina
	 */
	public void linkBarcodeToCaracteristics3(String barcode, int caracteristicId) throws SQLException {
		//get barcode id
		int barcodeId = fBarcodeId(barcode);
		//create new link
		String insertSql =  "INSERT INTO trace_caracteristique_link_object(object_id,caracteristique_id) VALUES (?, ?)";
		this.jdbcTemplate.update(insertSql, new Object[]{barcodeId, caracteristicId});
		Logger.debug("New link id : "+ this.jdbcTemplate.queryForObject("SELECT trace_caracteristique_link_object_id FROM trace_caracteristique_link_object WHERE object_id=? AND caracteristique_id=?", new Object[] {barcodeId, caracteristicId}, Integer.class));
		
	}
	 
	public int fBarcodeId(String barcode) {
		return this.jdbcTemplate.queryForObject("SELECT object_id FROM trace_object WHERE object_barcode=?", new Object[] {barcode}, Integer.class);
	}
	public int fCaracteristiqueTypeId(int caracteristicId) {
		return this.jdbcTemplate.queryForObject("SELECT caracteristique_type_id FROM trace_caracteristique WHERE caracteristique_id =?", new Object[] {caracteristicId}, Integer.class);
	}

	public void deletSameLinkCaracteristics(String barcode, int caracteristicId) throws SQLException {
		int barcodeId = fBarcodeId(barcode);
		int caracteristicTypeId = fCaracteristiqueTypeId(caracteristicId);
		String sqldel = "DELETE FROM trace_caracteristique_link_object "
				+ "WHERE trace_caracteristique_link_object_id = ? ";
		String sqlTmp = "SELECT tclo.trace_caracteristique_link_object_id FROM trace_caracteristique_link_object tclo "
				+ "INNER JOIN trace_caracteristique tc on tclo.caracteristique_id=tc.caracteristique_id "
				+ "WHERE object_id = ? "  
				+ "AND tc.caracteristique_type_id = ?"
				+ "AND tclo.caracteristique_id != ?";
		List<Integer> links = this.jdbcTemplate.queryForList(sqlTmp, new Object[] {barcodeId, caracteristicTypeId, caracteristicId}, Integer.class);
		for (Integer delId : links) {
			this.jdbcTemplate.update(sqldel, delId);
			Logger.debug("LimsGETDAO deletSameLinkCaracteristics"  + delId);
		}
	}


public HashMap<String, PropertyValue> getCaracteristiquesForContainer(int barcodeId){
	HashMap<String, PropertyValue> caracteristiques= new HashMap<String, PropertyValue>();
	String sql_query = "SELECT tct.intitule as intitule, tct.unite as unite, tc.valeur as valeur, tc.caracteristique_type_id as caracteristiqueTypeId "
			+ "FROM trace_caracteristique_link_object tclo "
			+ "INNER JOIN trace_caracteristique tc ON tc.caracteristique_id  = tclo.caracteristique_id "
			+ "INNER JOIN trace_caracteristique_type tct ON tct.caracteristique_type_id = tc.caracteristique_type_id "
			+ "WHERE tclo.object_id = ?";
	 
	List<Map<String,Object>>rows = this.jdbcTemplate.queryForList(sql_query,barcodeId);
	Logger.debug("LimsGETDAO - getCaracteristiquesForContainer - Caracteristiques d'eSIToul pour l'objet " + barcodeId);
	for (Map<String, Object> row : rows) {
		Logger.debug("Caracteristique " + row.get("intitule").toString() + " ("+row.get("unite").toString()+") " + row.get("caracteristiqueTypeId").toString() + " : " + row.get("valeur").toString());
		//DataMappingGET.getInstrumentTypeCodeMapping(rs.getString("insCategoryCode"));

		if ("274".equals(row.get("caracteristiqueTypeId").toString())){
			Logger.debug("Index " + row.get("valeur").toString());
			if (!"NoIndex".equals(row.get("valeur").toString())){
				caracteristiques.put("tag",new PropertySingleValue(row.get("valeur").toString().substring(0, row.get("valeur").toString().indexOf(":"))));
				caracteristiques.put("tagCategory",new PropertySingleValue(DataMappingGET.getTagCategory(row.get("valeur").toString())));
			}
			
		}else if (!row.get("unite").toString().equals("indéfini")){
			caracteristiques.put(row.get("intitule").toString(), new PropertySingleValue(row.get("valeur").toString(), row.get("unite").toString()));
//			Logger.debug("Index " + row.get("intitule").toString()+ ", " + row.get("valeur").toString() + ", " + row.get("unite").toString());
		}else{
			caracteristiques.put(row.get("intitule").toString(), new PropertySingleValue(row.get("valeur").toString()));
		}
	}
	return caracteristiques;
		
}

	public List<Project> findProjectToCreate(final ContextValidation contextError) throws SQLException, DAOException {

		String sql_query = "SELECT DISTINCT * FROM project";
		
		List<Project> results = this.jdbcTemplate.query(sql_query,new Object[]{} ,new RowMapper<Project>() {

			@SuppressWarnings("rawtypes")
			public Project mapRow(ResultSet rs, int rowNum) throws SQLException {


				Project project = new Project();
				project.code = rs.getString(2).trim();
				project.name = rs.getString(3).replaceAll("[\\s\"]+", " ").trim();
				String fgGroupe=null;
				//String fgGroupe=rs.getString("groupefg");
				//				if(fgGroupe==null){
				//					project.typeCode=PROJECT_TYPE_CODE_DEFAULT;
				//				}
				//				else {
				//					project.typeCode=PROJECT_TYPE_CODE_FG;
				//					project.properties= new HashMap<String, PropertyValue>();
				//					project.properties.put(PROJECT_PROPERTIES_FG_GROUP, new PropertySingleValue(fgGroupe));
				//				}
				//
				//				project.categoryCode=PROJECT_CATEGORY_CODE;
				//
				//				project.state = new State(); 
				//				project.state.code="IP";
				if(rs.getString("in_france_genomique") == "TRUE")
				{
					fgGroupe = PROJECT_TYPE_CODE_FG;
				}

				if(fgGroupe==null)
				{
					project.typeCode=PROJECT_TYPE_CODE_DEFAULT;
				}

				else 
				{
					project.typeCode=PROJECT_TYPE_CODE_FG;
					project.properties= new HashMap<String, PropertyValue>(); 
					//					project.properties.put(PROJECT_PROPERTIES_FG_GROUP, new PropertySingleValue(fgGroupe));
					project.properties.put(PROJECT_PROPERTIES_FG_GROUP, new PropertySingleValue(fgGroupe));
				}

				project.categoryCode=PROJECT_CATEGORY_CODE;

				project.state = new State(); 
//				project.state.code="F";
				project.state.code="N";

//				if(rs.getString("avancement").startsWith("En"))
//				{
//					project.state.code = "IP";	
//				}

				project.state.user = InstanceHelpers.getUser();
				project.state.date = new Date();

				project.bioinformaticParameters = new BioinformaticParameters();

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
		Logger.debug("LimsGETDAO - findContentsFromContainer : code - " + code + ", requete : " + sqlContent);

		List<Content> results = this.jdbcTemplate.query(sqlContent,new Object[]{code},new RowMapper<Content>() {
			
			@SuppressWarnings("rawtypes")
			public Content mapRow(ResultSet rs, int rowNum) throws SQLException {
				Logger.debug("LimsGETDAO - findContentsFromContainer mapRow : " + rs.getString("sampleCode") );
				Content sampleUsed = new Content(rs.getString("sampleCode"),null,null);
				
				List<String> projects = new ArrayList<String>();
				projects = findProjectsForContainer(rs.getInt("barcodeid"));
				//verifier l'existance des informations liés au content
				
				if (projects.isEmpty()){	
					Logger.error("LimsGETDAO - findContentsFromContainer : sampleUsed projects.isEmpty " + rs.getString("sampleCode"));
				}else {
						
					if (projects.size() > 1){
						Logger.error("LimsGETDAO - findContentsFromContainer : plusieurs projets associés au même barre-code : "+ rs.getInt("barcodeid"));
						
					} 
					sampleUsed.projectCode = projects.get(0);
					
	//				sampleUsed.projectCode = rs.getString("project");
					Logger.debug("LimsGETDAO - findContentsFromContainer : sampleUsed.projectCode - "+sampleUsed.projectCode);
					//TODO add projectCode
					// Todo add properties from ExperimentType
					sampleUsed.properties=new HashMap<String, PropertyValue>();
					sampleUsed.percentage=10.0; //default value
					sampleUsed.properties.put("libProcessTypeCode",new PropertySingleValue(""));//default value
					sampleUsed.properties.put("libLayoutNominalLength", new PropertySingleValue(0));//default value
					sampleUsed.properties = getCaracteristiquesForContainer(rs.getInt("barcodeid"));
	//				if(rs.getString("percentPerLane")!=null){
	//					sampleUsed.properties.put("percentPerLane", new PropertySingleValue(rs.getDouble("percentPerLane")));
	//					sampleUsed.percentage=rs.getDouble("percentPerLane");
	//				}else {	
	//					sampleUsed.percentage=rs.getDouble("percentage");
	//				}
	//				sampleUsed.properties.put("libProcessTypeCode",new PropertySingleValue(rs.getString("libProcessTypeCode")));
	//				if(rs.getString("tag")!=null){
	//					sampleUsed.properties.put("tag", new PropertySingleValue(rs.getString("tag")));
	//					sampleUsed.properties.put("tagCategory",new PropertySingleValue(rs.getString("tagCategory")));
	//				}
	//
	//				if(rs.getString("libLayoutNominalLength") != null){
	//					sampleUsed.properties.put("libLayoutNominalLength", new PropertySingleValue(rs.getInt("libLayoutNominalLength")));
	//				} 
					Logger.debug("LimsGETDAO - findContentsFromContainer : sampleUsed OK");
				
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
				run.instrumentUsed.typeCode = DataMappingGET.getInstrumentTypeCodeMapping(rs.getString("insCategoryCode"));
				run.typeCode =DataMappingGET.getRunTypeCodeMapping(rs.getString("insCategoryCode"));
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
				run.state.code = DataMappingGET.getStateRunFromLims(run.valuation.valid);
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
				readSet.state.code=DataMappingGET.getStateReadSetFromLims(rs.getString("state"),TBoolean.valueOf(rs.getString("validationBioinformatic")));
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
				readSet.state.code=DataMappingGET.getStateReadSetFromLims(rs.getString("state"),TBoolean.valueOf(rs.getString("validationBioinformatic")));
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
		String indexSQLQuery ="SELECT t.code as tag_code, t.name as tag_name,t.category as tag_category, t.sequence as tag_sequence, p.name as provider_name from ngl_tag as t inner join provider p on p.providerid = t.provider_id";
		List<Index> results = this.jdbcTemplate.query(indexSQLQuery 
				,new RowMapper<Index>() {
					@SuppressWarnings("rawtypes")
					public Index mapRow(ResultSet rs, int rowNum) throws SQLException {
						Index index=new IlluminaIndex();
						index.code=rs.getString("tag_code");
						index.name=rs.getString("tag_code");
						index.shortName=rs.getString("tag_code");
						index.categoryCode=rs.getString("tag_category");
						index.sequence=rs.getString("tag_sequence");
						index.supplierName=new HashMap<String, String>();
						index.supplierName.put(rs.getString("provider_name"),rs.getString("tag_code"));
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
			sql=sql+"  datediff(day,uaddm,uadInNGL)<0";
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


//	public List<Process> findProcessToCreate(String sql, final Container container, String processTypeCode, final ContextValidation contextError) {
//		List<Process> results=null;
//
//		try {
//			final ProcessType processType = ProcessType.find.findByCode(processTypeCode);
//			if(processType!=null){
//				results = this.jdbcTemplate.query(sql ,new Object[]{container.code}
//				,new RowMapper<Process>() {
//					@SuppressWarnings("rawtypes")
//					public Process mapRow(ResultSet rs, int rowNum) throws SQLException {
//						Process process=new Process();
//						process.typeCode=processType.code;
//						process.categoryCode=processType.category.code;
//						process.containerInputCode=container.code;
//						process.projectCode=rs.getString("projectCode");
//						process.sampleCode=rs.getString("sampleCode");
//						process.traceInformation=new TraceInformation();
//						process.traceInformation.createUser=contextError.getUser();
//						process.traceInformation.creationDate=new Date();
//						process.state=new State("N",contextError.getUser());
//						process.properties=new HashMap<String, PropertyValue>();
//						MappingHelper.getPropertiesFromResultSet(rs,processType.getPropertyDefinitionByLevel(Level.CODE.Process),process.properties);
//						process.code=CodeHelper.getInstance().generateProcessCode(process);
//						Content c=null;
//						for(Content content:container.contents){
//							if(content.projectCode.equals(process.projectCode) && content.sampleCode.equals(process.sampleCode)){
//								c=content;
//							}
//						}
//						if(c!=null){
//							process.sampleOnInputContainer=InstanceHelpers.getSampleOnInputContainer(c, container);
//						}else { contextError.addErrors("content",ValidationConstants.ERROR_CODE_NOTEXISTS_MSG,container); }
//						return process;
//					}
//				});
//			}else { contextError.addErrors("processType", ValidationConstants.ERROR_CODE_NOTEXISTS_MSG, processTypeCode); }
//
//
//		}	
//		catch (DAOException e) {
//			Logger.error("",e);
//		}
//		return results;
//
//	}

	public ReadSet findLSRunProjData(ReadSet readset){

		List<ReadSet> results = this.jdbcTemplate.query("pl_LSRunProjUnReadSetToNGL @readSetCode=?", new String[]{readset.code} 
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

