package services.instance.container;

import java.sql.SQLException;

import javax.inject.Inject;

import com.mongodb.MongoException;

import fr.cea.ig.ngl.NGLApplication;
import models.utils.dao.DAOException;
import validation.ContextValidation;

public class PrepaflowcellImportCNS extends ContainerImportCNS {
	
	@Inject
	public PrepaflowcellImportCNS(NGLApplication app) {
		super("Container Prepaflowcell CNS", app);
	}

//	@Override
//	public void runImport() throws SQLException, DAOException, MongoException {
//		createContainers(contextError,"pl_PrepaflowcellToNGL","lane","F","prepa-flowcell","pl_BanquesolexaUneLane @nom_lane=?");
//	}
	
	@Override
	public void runImport(ContextValidation contextError) throws SQLException, DAOException, MongoException {
		createContainers(contextError,"pl_PrepaflowcellToNGL","lane","F","prepa-flowcell","pl_BanquesolexaUneLane @nom_lane=?");
	}

}
