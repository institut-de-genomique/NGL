package services.instance.container;

import java.sql.SQLException;

import javax.inject.Inject;

import fr.cea.ig.ngl.NGLApplication;
import models.utils.dao.DAOException;
import validation.ContextValidation;

public class SizingImportCNS extends ContainerImportCNS {

	@Inject
	public SizingImportCNS(NGLApplication app) {
		super("Container Sizing CNS", app);		
	}

//	@Override
//	public void runImport() throws SQLException, DAOException {
//		createContainers(contextError,"pl_MaterielmanipToNGL @emnco=16 ","tube","IW-P","sizing","pl_ContentFromContainer @matmanom=?");
//	}
	
	@Override
	public void runImport(ContextValidation contextError) throws SQLException, DAOException {
		createContainers(contextError,"pl_MaterielmanipToNGL @emnco=16 ","tube","IW-P","sizing","pl_ContentFromContainer @matmanom=?");
	}

}