package controllers.migration.cng;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import models.Constants;
import models.LimsCNGDAO;
import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.ContainerSupport;
import models.laboratory.container.instance.LocationOnContainerSupport;
import models.utils.InstanceConstants;
import models.utils.InstanceHelpers;
import models.utils.dao.DAOException;
import models.utils.instance.ContainerSupportHelper;

import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;
import org.mongojack.JacksonDBCollection;
import org.mongojack.WriteResult;

import play.Logger;
import play.api.modules.spring.Spring;
import play.mvc.Result;
import validation.ContextValidation;
import controllers.CommonController;
import controllers.migration.models.ContainerOld;
import fr.cea.ig.MongoDBDAO;

public class MigrationLanes extends CommonController{

	protected static LimsCNGDAO limsServices= Spring.getBeanOfType(LimsCNGDAO.class);	
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmm");
	private static String backupName = InstanceConstants.CONTAINER_COLL_NAME+"_BCK_C_"+sdf.format(new java.util.Date());	

	public static Result migration(){

		int processed=0;
		int nomodif=0;
		int migrOK=0;
		int migrERR=0;
		
		JacksonDBCollection<Container, String> containersCollBck = MongoDBDAO.getCollection(backupName, Container.class);

		Logger.info("debut Migration Lanes");

		// backuper d'abord...
		backupContainer();
			
		//find new values
		ContextValidation contextError = new ContextValidation(Constants.NGL_DATA_USER);
		List<Container>solexaLanes = null;
		try {
			solexaLanes = limsServices.findAllContainer(contextError, "lane", null);
			Logger.debug(solexaLanes.size()+" Lanes trouvees dans solexa...");
		} catch (DAOException e) {
			Logger.debug("ERROR in findAllContainer():" + e.getMessage());
		}
			
		//find current collection
		List<Container> mongoLanes = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class, DBQuery.is("categoryCode", "lane")).toList();
		
		Logger.debug(mongoLanes.size()+" Lanes a migrer...");
			
		for (Container mongoLane : mongoLanes) {
			Logger.debug(">> "+ mongoLane.code);
				
			boolean foundInSolexa= false;
			for (Container solexaLane : solexaLanes) {		
				
				int migrRes=0;
					
				if (solexaLane.code.equals(mongoLane.code)) {
					foundInSolexa=true; 
					migrRes=migrationLane(mongoLane, solexaLane);

					if ( migrRes == 0){
						migrOK++;
					}
					else {
						migrERR++;
					}
					    
					processed++;
					break;
				}		
			}
				
			if (! foundInSolexa ){
				Logger.debug("pas de donnée trouvée dans solexa pour cette lane...");
				nomodif++;
			}
		}
		
		Logger.info("Migration lanes terminée:" + processed +" migrées ("+ migrOK + " OK; "+ migrERR + " ERR)" );
		Logger.info("                        :" + nomodif   +" non migrées");
		return ok("Migration lanes terminée:" + processed +" migrées ("+ migrOK + " OK; "+ migrERR + " ERR)\n "+ nomodif   +" non migrées");
	}


	private static int migrationLane(Container mongoLane, Container solexaLane) {
		
		int migrRes=0;
		
		Logger.info("migration de  la lane : "+ mongoLane.code );
		
		// -1- mise a jour du from experiment type ==> HARDCODED 'prepa-flowcell'
		// fromExperimentTypeCodes est un HashSet
		 
		HashSet<String> fromexptype = new HashSet<String>();
		fromexptype.add("prepa-flowcell"); 
		
		// -2- mise a jour du type de sample et du tag
		// ne pas faire dans le detail ces valeurs sont des element de contents
	    // ==> recuperer directement les donnnees de solexa

		// Logger.info("SIMUL ecrasement content...");
		
		WriteResult r = (WriteResult) MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME, Container.class, 
				DBQuery.is("code", mongoLane.code), 
				DBUpdate.set("fromExperimentTypeCodes", fromexptype).set("contents", solexaLane.contents));
	
		

		return migrRes;
	}

	private static void backupContainer() {
		Logger.info("\tCopie "+InstanceConstants.CONTAINER_COLL_NAME+" to "+backupName+" start");		
		MongoDBDAO.save(backupName, MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class).toList());
		Logger.info("\tCopie "+InstanceConstants.CONTAINER_COLL_NAME+" to "+backupName+" end");	
	}
}