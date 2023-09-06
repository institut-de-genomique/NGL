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

/*
 * bug dans la view v_tube_tongl_reprise
 * les tag ont ete importes avec les shortName au lieu des nglbi_code
 *  => remettre les tags corrects
 * 07/10/2015
 */

public class MigrationContainersTag extends CommonController{

	protected static LimsCNGDAO limsServices= Spring.getBeanOfType(LimsCNGDAO.class);	
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmm");
	private static String backupName = InstanceConstants.CONTAINER_COLL_NAME+"_BCK_C_"+sdf.format(new java.util.Date());	
	
	//private static String testContCollName = InstanceConstants.CONTAINER_COLL_NAME+"_BCK_C_20151008_0940";	

	public static Result migration(){

		int total_processed=0;
		int total_nomodif=0;
		int total_migrOK=0;
		int total_migrERR=0;
		
		/// sert a quoi ??? containersCollBck  pas utilisee..
		JacksonDBCollection<Container, String> containersCollBck = MongoDBDAO.getCollection(backupName, Container.class);

		Logger.info("debut Migration Containers / tubes   collection cible:" + InstanceConstants.CONTAINER_COLL_NAME);

		//-0- backuper d'abord...
		//DEV: travailler sur la collection  BCK_C_20151008_0940
		backupContainer();
			
		ContextValidation contextError = new ContextValidation(Constants.NGL_DATA_USER);
		List<Container>solexaLibs = null;
		
		//-1-find solexa (new)  lib-normalization
		// j'ai flage a need update dans solex uniqt celle pourlesquell on veut fair la mise a jour
		try {
			solexaLibs = limsServices.findContainerToModify(contextError, "tube", "lib-normalization");
			Logger.debug(solexaLibs.size()+" lib-normalization trouvees dans solexa...");
		} catch (DAOException e) {
			Logger.debug("ERROR in findContainerToModify():" + e.getMessage());
		}
			
		//find  mongo(current)  lib-normalization
		
		//MARCHE PAS List<Container> mongoLibs = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class, DBQuery.and (DBQuery.is("categoryCode", "tube"),DBQuery.is("fromExperimentTypeCodes", "lib-normalization"))).toList();
		//MARCHE PAS List<Container> mongoLibs = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class, DBQuery.is("fromExperimentTypeCodes", "lib-normalization"))).toList();
		// ESSAI Nicolas.... List<Container> mongoLibs = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class, DBQuery.elemMatch("fromExperimentTypeCodes", DBQuery.is(, "lib-normalization"))).toList();
		List<Container> mongoLibs = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, 
				                                Container.class, 
				                                DBQuery.and (DBQuery.is("categoryCode", "tube"),DBQuery.in("fromExperimentTypeCodes", "lib-normalization"))
				                                ).toList(); 

		Logger.debug(mongoLibs.size()+" lib-normalization a migrer...");
		///if (true) return ok("CANCELLED");
		
		int processed=0;
		int nomodif=0;
		int migrOK=0;
		int migrERR=0;
			
		for (Container mongoLib : mongoLibs) {
			Logger.debug(">> "+ mongoLib.code);
				
			boolean foundInSolexa= false;
			for (Container solexaLib : solexaLibs) {		
				
				int migrRes=0;
					
				if (solexaLib.code.equals(mongoLib.code)) {
					foundInSolexa=true; 
					migrRes=migrationLib(mongoLib, solexaLib);

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
				Logger.debug("pas de donnée trouvée dans solexa pour cette librairie...");
				nomodif++;
			}
		}
		
		Logger.info("Migration Lib-normalization  terminée:" + processed +" migrées ("+ migrOK + " OK; "+ migrERR + " ERR)" );
		Logger.info("                                     :" + nomodif   +" non migrées");
		
		total_processed=total_processed+processed;
		total_migrOK=total_migrOK+migrOK;
		total_migrERR=total_migrERR+migrERR;
		total_nomodif=total_nomodif+ nomodif;
		
		//-2-find new values for denat-dil-lib
		try {
			solexaLibs = limsServices.findContainerToModify(contextError, "tube", "denat-dil-lib");
			Logger.debug(solexaLibs.size()+" denat-dil-lib trouvees dans solexa...");
		} catch (DAOException e) {
			Logger.debug("ERROR in findContainerToModify():" + e.getMessage());
		}
			
		//find current denat-dil-lib
		mongoLibs = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, 
                Container.class, 
                DBQuery.and (DBQuery.is("categoryCode", "tube"),DBQuery.in("fromExperimentTypeCodes", "denat-dil-lib"))
                ).toList(); 
		
		Logger.debug(mongoLibs.size()+" denat-dil-lib a migrer...");
		
		processed=0;
		nomodif=0;
		migrOK=0;
		migrERR=0;
			
		for (Container mongoLib : mongoLibs) {
			Logger.debug(">> "+ mongoLib.code);
				
			boolean foundInSolexa= false;
			for (Container solexaLib : solexaLibs) {		
				
				int migrRes=0;
					
				if (solexaLib.code.equals(mongoLib.code)) {
					foundInSolexa=true; 
					migrRes=migrationLib(mongoLib, solexaLib);

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
				Logger.debug("pas de donnée trouvée dans solexa pour cette librairie...");
				nomodif++;
			}
		}
		
		Logger.info("Migration denat-dil-lib terminée:" + processed +" migrées ("+ migrOK + " OK; "+ migrERR + " ERR)" );
		Logger.info("                                :" + nomodif   +" non migrées");
		
		total_processed=total_processed+processed;
		total_migrOK=total_migrOK+migrOK;
		total_migrERR=total_migrERR+migrERR;
		total_nomodif=total_nomodif+ nomodif;
		
		
		return ok("Migration libraries terminée:" + total_processed +" migrées ("+ total_migrOK + " OK; "+ total_migrERR + " ERR)\n "+ total_nomodif   +" non migrées");
	}

    // faudrait un throw  qq chose  en cas d'execption MongoDB ??
	private static int migrationLib(Container mongoLib, Container solexaLib) {
		
		int migrRes=0;
		
		Logger.info("migration du tube : "+ mongoLib.code );
		
		// -2- mise a jour du tag
		// ne pas faire dans le detail ==> recuperer directement les donnnees "contents" dans solexa

		// Logger.info("SIMUL ecrasement content...");
		
		 WriteResult r = (WriteResult) MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME, Container.class, 
				                                         DBQuery.is("code", mongoLib.code), 
				                                         DBUpdate.set("contents", solexaLib.contents));
	
		return migrRes;
	}

	private static void backupContainer() {
		Logger.info("\tCopie "+InstanceConstants.CONTAINER_COLL_NAME+" to "+backupName+" start");		
		MongoDBDAO.save(backupName, MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class).toList());
		Logger.info("\tCopie "+InstanceConstants.CONTAINER_COLL_NAME+" to "+backupName+" end");	
	}
}
