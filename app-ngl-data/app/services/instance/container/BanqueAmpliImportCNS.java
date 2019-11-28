package services.instance.container;

import java.sql.SQLException;

import javax.inject.Inject;

import com.mongodb.MongoException;

import fr.cea.ig.ngl.NGLApplication;
import models.utils.dao.DAOException;
import rules.services.RulesException;
import validation.ContextValidation;

public class BanqueAmpliImportCNS extends ContainerImportCNS {

	@Inject
	public BanqueAmpliImportCNS(NGLApplication app) {
		super("Container Banque Amplifie", app);		
	}

//	@Override
//	public void runImport() throws SQLException, DAOException, MongoException, RulesException {
//		createContainers(contextError,"pl_MaterielmanipToNGL @emnco=18 ","tube","IW-P","pcr-amplification-and-purification","pl_ContentFromContainer @matmanom=?");
//	}
	
	@Override
	public void runImport(ContextValidation contextError) throws SQLException, DAOException, MongoException, RulesException {
		createContainers(contextError,"pl_MaterielmanipToNGL @emnco=18 ","tube","IW-P","pcr-amplification-and-purification","pl_ContentFromContainer @matmanom=?");
	}

}
