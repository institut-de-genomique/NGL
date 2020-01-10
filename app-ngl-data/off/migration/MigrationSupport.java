package controllers.migration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import models.Constants;
import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.ContainerSupport;
import models.laboratory.container.instance.LocationOnContainerSupport;
import models.utils.InstanceConstants;
import models.utils.InstanceHelpers;
import models.utils.instance.ContainerSupportHelper;

import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;
import org.mongojack.JacksonDBCollection;

import play.Logger;
import play.mvc.Result;
import validation.ContextValidation;
import controllers.CommonController;
import controllers.migration.models.ContainerOld;
import fr.cea.ig.MongoDBDAO;

public class MigrationSupport extends CommonController{

	private static final String CONTAINER_COLL_NAME_BCK = InstanceConstants.CONTAINER_COLL_NAME+"_BCK";


	public static Result migration(){

		Logger.info("Start point of Migration ContainerSupport");

		JacksonDBCollection<ContainerOld, String> containersCollBck = MongoDBDAO.getCollection(CONTAINER_COLL_NAME_BCK, ContainerOld.class);
		if(containersCollBck.count() == 0){

			Logger.info("Migration ContainerSupport start");

			backupContainerCollection();

			List<ContainerOld> oldContainers = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, ContainerOld.class).toList();
			Logger.debug("migre "+oldContainers.size()+" containers");
			for (ContainerOld container : oldContainers) {
				migreBarCode(container);
			}
			Logger.info("Migration barCode end");

			List<Container> newContainers=MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class).toList();
			Map<String,ContainerSupport> mapSupport = new HashMap<String, ContainerSupport>();
			for (Container container:newContainers) {
				createSupportCollection(container, mapSupport);
			}
			Logger.info("Creation Supports OK");

			updateBD(mapSupport);

		}else{
			Logger.info("Migration ContainerSupport already execute !");
		}
		Logger.info("Migration ContainerSupport finish");
		return ok("Migration ContainerSupport Finish");
	}




	public static void migreBarCode(ContainerOld container) {
		if (container.support != null) {
			Container c = new Container();
			c.support = new LocationOnContainerSupport();
			if (container.support.barCode != null) {
				c.support.code = container.support.barCode;
				MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME, Container.class, 
						DBQuery.is("code", container.code), 
						DBUpdate.unset("support.barCode") 
						.set("support.supportCode", c.support.code) );
			}
			else {
				MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME, Container.class, 
						DBQuery.is("code", container.code), 
						DBUpdate.unset("support.barCode")  );
			}
		}
	}


	public static void createSupportCollection(Container container, Map<String,ContainerSupport> mapSupports) {
		if (container.support != null) {
			//ContainerSupport newSupport = ContainerSupportHelper.createSupport(container.support, container.projectCodes, container.sampleCodes);
			ContainerSupport newSupport = ContainerSupportHelper.createContainerSupport(container.support.code,null, container.support.categoryCode,"ngl");
			newSupport.projectCodes=new HashSet<String>(container.projectCodes);
			newSupport.sampleCodes=new HashSet<String>(container.sampleCodes);

			if (!mapSupports.containsKey(newSupport.code)) {
				mapSupports.put(newSupport.code, newSupport);
			}
			else {
				ContainerSupport oldSupport = (ContainerSupport) mapSupports.get(newSupport.code);
				oldSupport.projectCodes.addAll(newSupport.projectCodes); 
				oldSupport.sampleCodes.addAll(newSupport.sampleCodes);
			}
		}
	}

	
	public static Result updateBD(Map<String,ContainerSupport>  mapSupport) {
		ContextValidation contextValidation=new ContextValidation(Constants.NGL_DATA_USER);
		contextValidation.setCreationMode();
		InstanceHelpers.save(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, new ArrayList<ContainerSupport>(mapSupport.values()),contextValidation);
		if(contextValidation.hasErrors()){
			Logger.info("CreateSupportCollection ends with errors");
			return badRequest("CreateSupportCollection ends with errors");
		}else {
			Logger.info("CreateSupportCollection ends without errors");		
			return ok("CreateSupportCollection ends without errors");
		}

	}


	private static void backupContainerCollection() {
		Logger.info("\tCopie "+InstanceConstants.CONTAINER_COLL_NAME+" start");
		MongoDBDAO.save(CONTAINER_COLL_NAME_BCK, MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, ContainerOld.class).toList());
		Logger.info("\tCopie "+InstanceConstants.CONTAINER_COLL_NAME+" end");
	}

}

