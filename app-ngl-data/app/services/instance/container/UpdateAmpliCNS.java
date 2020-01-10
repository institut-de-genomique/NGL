package services.instance.container;

import java.sql.SQLException;

import javax.inject.Inject;

import com.mongodb.MongoException;

import fr.cea.ig.ngl.NGLApplication;
import models.utils.dao.DAOException;
import rules.services.RulesException;
import validation.ContextValidation;

public class UpdateAmpliCNS extends UpdateContainerImportCNS {

	@Inject
	public UpdateAmpliCNS(NGLApplication app) {
		super("UpdateAmpli", app);
	}

//	@Override
//	public void runImport() throws SQLException, DAOException, MongoException, RulesException {
//		updateContainer("pl_BanqueAmpliToNGL @updated=1",contextError,"tube","amplification");
//	}

	@Override
	public void runImport(ContextValidation contextError) throws SQLException, DAOException, MongoException, RulesException {
		updateContainer("pl_BanqueAmpliToNGL @updated=1",contextError,"tube","amplification");
	}

}