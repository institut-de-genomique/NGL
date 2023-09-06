package services.instance.container;

import java.sql.SQLException;

import javax.inject.Inject;

import com.mongodb.MongoException;

import fr.cea.ig.ngl.NGLApplication;
import models.utils.dao.DAOException;
import rules.services.RulesException;
import validation.ContextValidation;

public class UpdateSolutionStockCNS extends UpdateContainerImportCNS {

	@Inject
	public UpdateSolutionStockCNS(NGLApplication app) {
		super("UpdateSolutionStock", app);
	}

//	@Override
//	public void runImport() throws SQLException, DAOException, MongoException, RulesException {
//		updateContainer("pl_SolutionStockToNGL @updated=1",contextError,"tube","solution-stock");
//	}

	@Override
	public void runImport(ContextValidation contextError) throws SQLException, DAOException, MongoException, RulesException {
		updateContainer("pl_SolutionStockToNGL @updated=1",contextError,"tube","solution-stock");
	}

}
