package controllers.migration.cng;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.sql.DataSource;

import models.LimsCNGDAO;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.Content;
import models.laboratory.sample.description.SampleType;
import models.utils.InstanceConstants;
import models.utils.dao.DAOException;

import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;
import org.mongojack.JacksonDBCollection;
import org.mongojack.WriteResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import play.Logger;
import play.mvc.Result;
import controllers.CommonController;
import fr.cea.ig.MongoDBDAO;

/**
 * add libProcessTypeCode to contents of containers
 * @author dnoisett
 * 23/10/2014
 */
@Repository
public class AddLibProcessTypeCode extends CommonController {
	
	private static final String CONTAINER_COLL_NAME_BCK = InstanceConstants.CONTAINER_COLL_NAME + "_BCKmigrationContentEx_20141023";
	private static JdbcTemplate jdbcTemplate;
	protected static final String SAMPLE_USED_TYPE_CODE = "default-sample-cng";	
	
	
	@Autowired
	@Qualifier("lims")
	private  void setDataSource(DataSource dataSource) {
		AddLibProcessTypeCode.jdbcTemplate = new JdbcTemplate(dataSource);              
	}
	
	
	//main
	public static Result migration() {	
		int n1 =0;
		
		JacksonDBCollection<Container, String> containersCollBck = MongoDBDAO.getCollection(CONTAINER_COLL_NAME_BCK, Container.class);
		if (containersCollBck.count() == 0) {
	
			backUpContainer();
			
			Logger.info("Migration container starts");
			
			n1 = migreContainer();
									
		} else {
			Logger.info("Migration container already executed !");
		}		
		Logger.info("Migration container end : " + n1 + " contents of containers updated !");
		
		return ok("Migration Finish");
	}

	
	private static void backUpContainer() {
		Logger.info("\tCopie "+InstanceConstants.CONTAINER_COLL_NAME+" starts");
		MongoDBDAO.save(CONTAINER_COLL_NAME_BCK, MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class).toList());
		Logger.info("\tCopie "+InstanceConstants.CONTAINER_COLL_NAME+" ended");
	}
	
	
	private static int migreContainer() {		
		int n=0;
		try {
			//find collection up to date
			List<Container> newContainers = null;
			newContainers = findSmallContainerToCreate();
			newContainers = LimsCNGDAO.demultiplexContainer(newContainers); 

			//find current containers
			List<Container> oldContainers = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class).toList();
			Logger.debug("Expected to migrate "+oldContainers.size()+" containers");
						
			for (Container oldContainer : oldContainers) {
				
				for (Container newContainer : newContainers) {		
					
					if (oldContainer.code.equals(newContainer.code)) {	
						oldContainer.contents = newContainer.contents;
	
						WriteResult r = (WriteResult) MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME, Container.class, DBQuery.is("code", oldContainer.code),   
								DBUpdate.set("contents", oldContainer.contents));
							
						
						n++;
						
						break;
					}
					
				}	
				
			}	
		} catch (DAOException e) {
			Logger.error("ERROR in findContainerToCreate():" + e.getMessage());
		}
		
		return n;
	}
	
	
	
	
	private static List<Container> findSmallContainerToCreate() throws DAOException {

		List<Container> results = jdbcTemplate.query("select * from v_flowcell_tongl_reprise order by code, project, code_sample, tag",new Object[]{} 
		,new RowMapper<Container>() {

			@SuppressWarnings("rawtypes")
			public Container mapRow(ResultSet rs, int rowNum) throws SQLException {

				Container container = new Container();
				container.code=rs.getString("code");
				
				if (rs.getString("project")!=null) {
					container.projectCodes=new HashSet<String>();
					container.projectCodes.add(rs.getString("project"));
				}
				
				if (rs.getString("code_sample")!=null) {
					Content content=new Content();
					content.sampleCode=rs.getString("code_sample");
					
					String sampleTypeCode = SAMPLE_USED_TYPE_CODE;
					SampleType sampleType=null;
					try {
						sampleType = SampleType.find.findByCode(sampleTypeCode);
					} catch (DAOException e) {
						Logger.error("",e);
						return null;
					}
					if( sampleType==null ){
						Logger.error("sampleTypeCode not exists : " + sampleTypeCode + " for content.sampleCode : " + content.sampleCode);
						return null;
					}		
					
					content.sampleTypeCode = sampleType.code;
					content.sampleCategoryCode = sampleType.category.code;
					
					content.properties = new HashMap<String, PropertyValue>();
					
					if(rs.getString("tag")!=null) { 
						content.properties.put("tag", new PropertySingleValue(rs.getString("tag")));
						content.properties.put("tagCategory", new PropertySingleValue(rs.getString("tagcategory")));
					}
					else {
						content.properties.put("tag",new PropertySingleValue("-1")); // specific value for making comparison, suppress it at the end of the function...
						content.properties.put("tagCategory",new PropertySingleValue("-1"));
					}						

					if(rs.getString("exp_short_name")!=null) {
						content.properties.put("libProcessTypeCode", new PropertySingleValue(rs.getString("exp_short_name")));
					}
					container.contents.add(content);	
					
					container.sampleCodes=new HashSet<String>();
					container.sampleCodes.add(rs.getString("code_sample"));
				}	
				return container;
			}
		}); 
		
		return results;
	}
	
	

}