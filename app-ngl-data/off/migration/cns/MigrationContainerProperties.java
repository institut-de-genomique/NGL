package controllers.migration.cns;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;










import models.LimsCNSDAO;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.Content;
import models.laboratory.run.instance.ReadSet;
import models.utils.InstanceConstants;










import org.mongojack.DBQuery;


import org.mongojack.DBUpdate;
import org.springframework.jdbc.core.RowMapper;

import play.Logger;
import play.Logger.ALogger;
import play.api.modules.spring.Spring;
import play.mvc.Result;
import services.instance.sample.UpdateSamplePropertiesCNS;
import validation.ContextValidation;
import controllers.CommonController;
import fr.cea.ig.MongoDBDAO;

public class MigrationContainerProperties  extends CommonController{

	protected static ALogger logger=Logger.of("MigrationContainerProperties");
	protected static LimsCNSDAO  limsServices = Spring.getBeanOfType(LimsCNSDAO.class);

	public static Result migration() {
				//updateDateCreationTube();
				//updateAmplificationExt();
				updateQuantificationError();
				return ok("Migration update container Finish");
	}

	
	
	
	private static void updateQuantificationError() {
		List<Container> containers=MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class,DBQuery.is("qualityControlResults.code", "FLUO-QUANTIFICATION-20170301_152358DCG")).toList();
		
		for(Container c:containers){
			c.qualityControlResults.stream().filter(q -> q.code.equals("FLUO-QUANTIFICATION-20170301_152358DCG")).forEach(q->{
				
				if(q.properties.containsKey("quantity1"))
					c.quantity=(PropertySingleValue) q.properties.get("quantity1");
				if(q.properties.containsKey("volume1"))
					c.volume=(PropertySingleValue) q.properties.get("volume1");
				if(q.properties.containsKey("concentration1"))
					c.concentration=(PropertySingleValue) q.properties.get("concentration1");
				
				MongoDBDAO.save(InstanceConstants.CONTAINER_COLL_NAME,c);
			}); 
		}
		
		
	}


	public static SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
	
	private static void updateDateCreationTube() {
		String sql="select matmanom,  tubdc from Materielmanip m, Tubeident t where m.matmaco=t.matmaco and matmaInNGL!=null";
		List<Container> results =  limsServices.jdbcTemplate.query(sql 
				,new RowMapper<Container>() {

			public Container mapRow(ResultSet rs, int rowNum) throws SQLException {
				Container container=new Container();
				container.code=rs.getString("matmanom");
				container.traceInformation=new TraceInformation(); 
				try {
					container.traceInformation.creationDate=formatter.parse(rs.getString("tubdc"));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return container;
			}

		}); 
		
		for(Container c:results){
			MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME, Container.class, DBQuery.is("code",c.code),DBUpdate.set("traceInformation.creationDate", c.traceInformation.creationDate));
		}
		
	}


	private static void updateAmplificationExt() {
		String sql="select matmanom, matmadc from Materielmanip m where proco in (163,480) and emnco=18 and matmaInNGL!=null";
		List<Container> results =  limsServices.jdbcTemplate.query(sql 
				,new RowMapper<Container>() {

			public Container mapRow(ResultSet rs, int rowNum) throws SQLException {
				Container container=new Container();
				container.code=rs.getString("matmanom");
				container.properties=new HashMap<String, PropertyValue>();
				try {
					container.properties.put("receptionDate",new PropertySingleValue(formatter.parse(rs.getString("matmadc"))));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return container;
			}

		});

		for(Container c:results){
			Logger.debug("Container "+c.code+" date "+ c.properties.get("receptionDate").value);

			MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME, Container.class, DBQuery.is("code",c.code),DBUpdate.set("properties.receptionDate", c.properties.get("receptionDate")));
		}
	}


	public static void updatesampleAliquoteCode(){
		ContextValidation contextError=new ContextValidation("ngl-sq");
		List<Container> containers=null;

	containers = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class, 
			DBQuery.exists("properties.limsCode").notExists("contents.properties.sampleAliquoteCode").notExists("contents.properties.tag").notIn("fromTransformationTypeCodes","prepa-flowcell")).toList();

	Logger.info("Nb containers to update :"+containers.size());

	containers.forEach(c->{
		MigrationContainerProperties.updateProperties(c, "sampleAliquoteCode", new PropertySingleValue(c.code), c.contents.get(0).sampleCode, null);
	});

	
	containers = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class, DBQuery.exists("properties.limsCode").notExists("contents.properties.sampleAliquoteCode").size("fromTransformationTypeCodes", 1)).toList();

	Logger.info("Nb containers to update :"+containers.size());
	containers.forEach(container -> {
		Logger.debug("Container "+container.code);
		try {

			List<Content> contents =limsServices.findContentsFromContainer("pl_ContentFromContainer @matmanom=?", container.code);
			contents.forEach(c->{
				Logger.debug("Content container :"+ container.code+", sample "+c.sampleCode+", tag "+c.properties.get("tag").value);
				MigrationContainerProperties.updateProperties(container, "sampleAliquoteCode", (PropertySingleValue) c.properties.get("sampleAliquoteCode"), c.sampleCode, c.properties.get("tag").value.toString());
			});
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	});
	

	}
	
	public static void updateProperties(Container container, String propertyName, PropertySingleValue propertyValue,String sampleCode, String tag){
		if(propertyValue!=null){
			Logger.debug("Update Container :"+container.code);

			DBQuery.Query queryContainer=DBQuery.is("code",container.code).is("contents.sampleCode", sampleCode);

			if(tag!=null){
				queryContainer.and(DBQuery.is("contents.properties.tag.value",tag));
			}

			MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME, Container.class
					,queryContainer
					,DBUpdate.set("contents.$.properties."+propertyName,propertyValue));

			//Update next container
			List<Container> sonContainers=MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class,DBQuery.is("treeOfLife.from.containers.code",container.code)).toList();

			if(sonContainers!=null && sonContainers.size()>0){
				sonContainers.forEach(sc->{
					MigrationContainerProperties.updateProperties(sc, propertyName, propertyValue, sampleCode, tag);
				});
			}else {
				//Update readSet if exists
				//Logger.debug("Update readSets to container"+container.code);
				DBQuery.Query queryReadSet=DBQuery.is("sampleOnContainer.containerCode",container.code)
						.is("sampleOnContainer.sampleCode",sampleCode);

				if(tag!=null){
					queryReadSet.and(DBQuery.is("sampleOnContainer.properties.tag.value",tag));
				}


				MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class
						, queryReadSet,DBUpdate.set("sampleOnContainer.properties."+propertyName,propertyValue));
			}
		}else {
			logger.error("No "+propertyName +" for container "+container.code);
		}

	}

}
