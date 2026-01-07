package scripts;

import java.sql.SQLException;
import java.util.List;

import javax.inject.Inject;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptNoArgs;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import models.LimsCNSDAO;
import models.laboratory.project.instance.Project;
import models.utils.InstanceConstants;
import models.utils.dao.DAOException;
import play.Logger;
import play.api.modules.spring.Spring;
import services.instance.project.ProjectImportCNS;
import validation.ContextValidation;

/**
 * @author jcharpen - Jordi CHARPENTIER - jcharpen@genoscope.cns.fr
 */
public class ScriptRepriseUmbrellaAndPropProject extends ScriptNoArgs {

	private ProjectImportCNS pImpCNS;

	private LimsCNSDAO limsCNSDAO;

	@Inject
	public ScriptRepriseUmbrellaAndPropProject(ProjectImportCNS pImpCNS) {
		this.pImpCNS = pImpCNS;
		this.limsCNSDAO = Spring.getBeanOfType(LimsCNSDAO.class);
	}
	
	@Override
	public void execute() throws Exception {
		Logger.error("Start ScriptRepriseUmbrellaAndPropProject");

		ContextValidation ctxVal = ContextValidation.createCreationContext("ngl-data");
		
		this.pImpCNS.createUmbrellaProject(ctxVal);

		updateProject(ctxVal);

		Logger.error("End ScriptRepriseUmbrellaAndPropProject");
	}

	public void updateProject(ContextValidation contextValidation) throws SQLException, DAOException, APIValidationException, APIException{
		List<Project> projects = MongoDBDAO.find(InstanceConstants.PROJECT_COLL_NAME, Project.class).toList();

		this.limsCNSDAO.setProjectUmbrellaAndGenreAndCommentsOfProjectList(projects);
	}
}