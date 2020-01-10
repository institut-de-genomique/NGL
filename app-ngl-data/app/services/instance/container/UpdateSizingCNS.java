package services.instance.container;

import java.sql.SQLException;

import javax.inject.Inject;

import com.mongodb.MongoException;

import fr.cea.ig.ngl.NGLApplication;
import models.utils.dao.DAOException;
import rules.services.RulesException;
import validation.ContextValidation;

public class UpdateSizingCNS extends UpdateContainerImportCNS {

	@Inject
	public UpdateSizingCNS(NGLApplication app) {
		super("UpdateSizing", app);
	}

//	@Override
//	public void runImport() throws SQLException, DAOException, MongoException, RulesException {
//		updateContainer("pl_SizingToNGL @updated=1", contextError, "tube", "sizing");
//	}
	
	@Override
	public void runImport(ContextValidation contextError) throws SQLException, DAOException, MongoException, RulesException {
		updateContainer("pl_SizingToNGL @updated=1", contextError, "tube", "sizing");
	}

}