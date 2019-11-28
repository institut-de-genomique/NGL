package services.instance.container;

import java.sql.SQLException;

import javax.inject.Inject;

import fr.cea.ig.ngl.NGLApplication;
import models.utils.dao.DAOException;
import validation.ContextValidation;

public class TubeImportCNS extends ContainerImportCNS {

	@Inject
	public TubeImportCNS(NGLApplication app) {
		super("Container Tube CNS", app);		
	}

//	@Override
//	public void runImport() throws SQLException, DAOException {
//		createContainers(contextError,"pl_TubeToNGL ","tube","IW-P",null,null);
//			//contextError.setUpdateMode();
//	//		updateSampleFromTara();
//	}
	
	@Override
	public void runImport(ContextValidation contextError) throws SQLException, DAOException {
		createContainers(contextError,"pl_TubeToNGL ","tube","IW-P",null,null);
		// contextError.setUpdateMode();
		// updateSampleFromTara();
	}

}
