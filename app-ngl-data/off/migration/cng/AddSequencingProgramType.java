package controllers.migration.cng;

import controllers.CommonController;




import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;
import org.mongojack.JacksonDBCollection;
import org.mongojack.WriteResult;

import fr.cea.ig.MongoDBDAO;

import play.Logger;
import play.mvc.Result;
import validation.ContextValidation;
import models.Constants;
import models.laboratory.common.instance.Comment;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TBoolean;
import models.laboratory.common.instance.Valuation;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.container.instance.ContainerSupport;
import models.laboratory.container.instance.Content;
import models.utils.InstanceConstants;
import models.utils.InstanceHelpers;
import models.utils.dao.DAOException;
import models.utils.instance.ContainerHelper;
import models.utils.instance.ContainerSupportHelper;

import models.laboratory.container.instance.Container;
import models.laboratory.run.instance.Run;
import models.laboratory.sample.description.SampleType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

@Repository
public class AddSequencingProgramType  extends CommonController {
	
	private static final String SUPPORT_COLL_NAME_BCK = InstanceConstants.CONTAINER_SUPPORT_COLL_NAME+"_BCK";
	private static JdbcTemplate jdbcTemplate;
	
	
	@Autowired
	@Qualifier("lims")
	public void setDataSource(DataSource dataSource) {
		jdbcTemplate = new JdbcTemplate(dataSource);       
	}
	

	public static HashMap<String, PropertySingleValue>  setSequencingProgramTypeToContainerSupport(final ContextValidation contextError)  throws SQLException, DAOException {
		if (jdbcTemplate == null) {
			Logger.debug("jdbcTemplate is null !");
		}
		 List<ContainerSupport> results = jdbcTemplate.query("select code_support, seq_program_type from v_flowcell_tongl_reprise order by code", new Object[]{} 
		,new RowMapper<ContainerSupport>() {
			@SuppressWarnings("rawtypes")
			public ContainerSupport mapRow(ResultSet rs, int rowNum) throws SQLException {
				ResultSet rs0 = rs;
				int rowNum0 = rowNum;
				ContextValidation ctxErr = contextError; 
				@SuppressWarnings("rawtypes")
				ContainerSupport c=  commonContainerSupportMapRow(rs0, rowNum0, ctxErr); 
				return c;
			}
		});
		//map data (code -> sequencingProgramType)
		HashMap<String,PropertySingleValue> mapCodeSupportSequencing = new HashMap<String,PropertySingleValue>();
		for (ContainerSupport result : results) {
			if (!mapCodeSupportSequencing.containsKey(result.code)) {
				mapCodeSupportSequencing.put(result.code, (PropertySingleValue) result.properties.get("sequencingProgramType"));
			}
		}
		return mapCodeSupportSequencing;
	}
	
	
	
	public static ContainerSupport commonContainerSupportMapRow(ResultSet rs, int rowNum, ContextValidation ctxErr) throws SQLException {
		ContainerSupport containerSupport = new ContainerSupport();
		
		containerSupport.code = rs.getString("code_support");		
		containerSupport.properties= new HashMap<String, PropertyValue>();
		containerSupport.properties.put("sequencingProgramType", new PropertySingleValue(rs.getString("seq_program_type")));

		return containerSupport;
	}	
	

	
	
	
	public static Result migration(){
		
		JacksonDBCollection<ContainerSupport, String> supportsCollBck = MongoDBDAO.getCollection(SUPPORT_COLL_NAME_BCK, ContainerSupport.class);
		if(supportsCollBck.count() == 0){
			
			Logger.info("Migration container support  start");
			
			backupContainerSupport();

			ContextValidation contextError = new ContextValidation(Constants.NGL_DATA_USER);
			
			//create new HashMap
			HashMap<String,PropertySingleValue> mapCodeSupportSeq = new HashMap<String,PropertySingleValue>() ;
			
			
			try {
				mapCodeSupportSeq = setSequencingProgramTypeToContainerSupport(contextError);
			}
			catch(Exception e) {
				Logger.error("DAO or SQL error: "+e.getMessage(),e);;
			}
			
			//find current collection
			List<ContainerSupport> oldContainerSupports = MongoDBDAO.find(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, ContainerSupport.class).toList();

			//update container support in this collection
			for (ContainerSupport oldContainerSupport : oldContainerSupports) {
				

				HashMap<String, PropertyValue> properties = new HashMap<String, PropertyValue>();
				properties.put("sequencingProgramType",  mapCodeSupportSeq.get(oldContainerSupport.code));
				
				oldContainerSupport.properties = properties;
								
						
				//global update of the object to have the _type (json subtype) like in the import 
				MongoDBDAO.update(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, oldContainerSupport);
				
				
			}
			
				 			
		}else{
			Logger.info("Migration container support already execute !");
		}
			
		Logger.info("Migration finish");
		return ok("Migration Finish");
	}

	private static void backupContainerSupport() {
		Logger.info("\tCopie "+InstanceConstants.CONTAINER_SUPPORT_COLL_NAME+" start");		
		MongoDBDAO.save(SUPPORT_COLL_NAME_BCK, MongoDBDAO.find(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, ContainerSupport.class).toList());
		Logger.info("\tCopie "+InstanceConstants.CONTAINER_SUPPORT_COLL_NAME+" end");	
	}

}