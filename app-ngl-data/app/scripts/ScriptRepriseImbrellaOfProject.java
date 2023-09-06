package scripts;

import javax.inject.Inject;
import java.util.List;
import models.LimsCNSDAO;
import models.laboratory.project.instance.Project;
import models.utils.InstanceConstants;
import play.api.modules.spring.Spring;
import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptNoArgs;


public class ScriptRepriseImbrellaOfProject extends ScriptNoArgs  {

	private LimsCNSDAO limsServices = Spring.getBeanOfType(LimsCNSDAO.class);
	
	@Inject
	public ScriptRepriseImbrellaOfProject() {
		super();
	}

	@Override
	public void execute() throws Exception {
		getLogger().error("Start ScriptRepriseImbrellaOfProject");

        List<Project> projectsList = MongoDBDAO.find(InstanceConstants.PROJECT_COLL_NAME, Project.class).toList();
		
		limsServices.setProjectUmbrellaAndGenreAndCommentsOfProjectList(projectsList);

		getLogger().error("End ScriptRepriseImbrellaOfProject");
	}
}
