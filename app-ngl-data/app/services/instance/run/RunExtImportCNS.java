package services.instance.run;

import java.sql.SQLException;

import javax.inject.Inject;

import com.mongodb.MongoException;

import fr.cea.ig.ngl.NGLApplication;
import models.utils.dao.DAOException;
import rules.services.RulesException;
import services.instance.AbstractImportDataCNS;
import services.instance.container.ContainerImportCNS;
import validation.ContextValidation;

public class RunExtImportCNS extends AbstractImportDataCNS {

	@Inject
	public RunExtImportCNS(NGLApplication app) {
		super("RunExterieurCNS", app);
	}

//	@Override
//	public void runImport() throws SQLException, DAOException, MongoException, RulesException {
//		ContainerImportCNS.createContainers(contextError,"pl_PrepaflowcellExtToNGL","lane","F","prepa-flowcell",null);
//		// RunImportCNS.createRuns("pl_RunExtToNGL",contextError);
//		app.injector().instanceOf(RunImportCNS.class).createRuns("pl_RunExtToNGL",contextError);
//	}
	
	@Override
	public void runImport(ContextValidation contextError) throws SQLException, DAOException, MongoException, RulesException {
		ContainerImportCNS.createContainers(contextError,"pl_PrepaflowcellExtToNGL","lane","F","prepa-flowcell",null);
		// RunImportCNS.createRuns("pl_RunExtToNGL",contextError);
		app.injector().instanceOf(RunImportCNS.class).createRuns("pl_RunExtToNGL",contextError);
	}
	
}
