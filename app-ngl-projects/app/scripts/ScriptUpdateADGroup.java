package scripts;

import java.util.List;
import java.util.Random;

import javax.inject.Inject;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptNoArgs;
import fr.cea.ig.lfw.controllers.scripts.chunked.ScriptWithExcelBody;
import fr.cea.ig.ngl.dao.projects.MembersProjectsAPI;
import models.laboratory.project.instance.Members;
import models.laboratory.project.instance.Project;
import models.utils.InstanceConstants;
import play.Logger;
import services.projects.members.ActiveDirectoryServicesIbfj;
import services.projects.members.ActiveDirectoryServicesOriginal;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * @author jcharpen - Jordi CHARPENTIER - jcharpen@genoscope.cns.fr
 */
public class ScriptUpdateADGroup extends ScriptNoArgs {
	
	private MembersProjectsAPI mpAPI;

	private ActiveDirectoryServicesIbfj adServIbfj;

	private ActiveDirectoryServicesOriginal adServOriginal;

	@Inject
	public ScriptUpdateADGroup(MembersProjectsAPI mpAPI) {
		super();
		this.mpAPI = mpAPI;
	}


	@Override
	public void execute() throws Exception {
		Logger.error("Start update AD Group");

		for (int i = 0; i < 2; i++) {
			Members members = new Members();
			members.codeProjet = "ABC-JORDI" + new Random().nextInt(1000000);
			Logger.error("Projet : " + i);
			// this.mpAPI.create(members, "jcharpen");

			// String gidAvailable = this.adServIbfj.getGIDAvalaible(40078, 49999);

			// Logger.error(gidAvailable);
		}

		Logger.error("End update AD Group");
	}
}