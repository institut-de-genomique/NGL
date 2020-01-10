package services.instance.container;

import java.sql.SQLException;

import javax.inject.Inject;

import fr.cea.ig.ngl.NGLApplication;
import models.utils.dao.DAOException;
import validation.ContextValidation;

public class SolutionStockImportCNS extends ContainerImportCNS {

	@Inject
	public SolutionStockImportCNS(NGLApplication app) {
		super("Container Solution stock CNS", app);
	}

//	@Override
//	public void runImport() throws SQLException, DAOException {
//		createContainers(contextError,"pl_MaterielmanipToNGL @emnco=14 ","tube","IW-P","solution-stock","pl_ContentFromContainer @matmanom=?");
//	}
	
	@Override
	public void runImport(ContextValidation contextError) throws SQLException, DAOException {
		createContainers(contextError,"pl_MaterielmanipToNGL @emnco=14 ","tube","IW-P","solution-stock","pl_ContentFromContainer @matmanom=?");
	}

}
