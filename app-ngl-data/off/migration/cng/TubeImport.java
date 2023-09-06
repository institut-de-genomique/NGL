package controllers.migration.cng;

import java.sql.SQLException;
import java.util.List;

import models.Constants;
import models.LimsCNGDAO;
import models.laboratory.container.instance.Container;
import models.utils.InstanceConstants;
import models.utils.InstanceHelpers;
import models.utils.dao.DAOException;
import models.utils.instance.ContainerHelper;

import org.mongojack.JacksonDBCollection;
import org.springframework.stereotype.Repository;

import play.Logger;
import play.api.modules.spring.Spring;
import play.mvc.Result;
import validation.ContextValidation;
import controllers.CommonController;
import fr.cea.ig.MongoDBDAO;

/**
 * Import tubes (type 3,4,5) from Solexa to NGL
 * Scope : validation of "prepa-flowcell" experiment (the view only import data {@literal > } 01/08/2014)
 * Corresponds to lib-b10n, lib-b5n et lib-b2n
 * @author dnoisett
 * 27/10/2014
 */
@Repository
public class TubeImport extends CommonController {
	
	protected static LimsCNGDAO limsServices= Spring.getBeanOfType(LimsCNGDAO.class);
	private static final String CONTAINER_COLL_NAME_BCK = InstanceConstants.CONTAINER_COLL_NAME + "_BCKmigrationContentEx_20141121_1";
	protected static final String SAMPLE_USED_TYPE_CODE = "default-sample-cng";	
		
	
	public static Result migration() {	
		JacksonDBCollection<Container, String> containersCollBck = MongoDBDAO.getCollection(CONTAINER_COLL_NAME_BCK, Container.class);
		if (containersCollBck.count() == 0) {	
			backUpContainer();
			
			Logger.info("Migration container starts");
			
			try {
				migreContainer();
			}
			catch(Exception e) {
				Logger.error(e.getMessage());
			}
									
		} else {
			Logger.info("Migration container already executed !");
		}		
		Logger.info("Migration container end");
		
		return ok("Migration Finish");
	}

	
	private static void backUpContainer() {
		Logger.info("\tCopie "+InstanceConstants.CONTAINER_COLL_NAME+" starts");
		MongoDBDAO.save(CONTAINER_COLL_NAME_BCK, MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class).toList());
		Logger.info("\tCopie "+InstanceConstants.CONTAINER_COLL_NAME+" ended");
	}
	
	private static void migreContainer()  throws SQLException, DAOException {
		ContextValidation contextError=new ContextValidation(Constants.NGL_DATA_USER);
		List<Container> containers = limsServices.findAllContainer(contextError, "tube","lib-normalization");
		ContainerHelper.createSupportFromContainers(containers, null, contextError);
		List<Container> ctrs=InstanceHelpers.save(InstanceConstants.CONTAINER_COLL_NAME, containers, contextError, true);
		limsServices.updateLimsTubes(ctrs, contextError, "creation");
	}
	
}