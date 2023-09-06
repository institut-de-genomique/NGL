package scripts;

import javax.inject.Inject;
import javax.naming.NamingException;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.chunked.ScriptWithExcelBody;
import fr.cea.ig.ngl.dao.projects.MembersProjectsAPI;
import models.laboratory.project.instance.Project;
import models.utils.InstanceConstants;
import play.Logger;
import services.projects.members.ActiveDirectoryServices;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * @author jcharpen - Jordi CHARPENTIER - jcharpen@genoscope.cns.fr
 */
public class ScriptUpdateMissingADGroup extends ScriptWithExcelBody {

	private ActiveDirectoryServices adServices;

	@Inject
	public ScriptUpdateMissingADGroup(MembersProjectsAPI membersProjectsAPI, ActiveDirectoryServices adServices) {
		super();

		this.adServices = adServices;
	}

	private void createADGroup(XSSFWorkbook workbook) {
		workbook.getSheetAt(0).rowIterator().forEachRemaining(row -> {				
			String projectCode = row.getCell(0).getStringCellValue();				
			projectCode = projectCode.trim();

			Project project = MongoDBDAO.findByCode(InstanceConstants.PROJECT_COLL_NAME, Project.class, projectCode);
				
			if (project == null) {
				throw new RuntimeException("Unknown code project : " + projectCode);
			}

			try {
				String groupName = this.adServices.generateGroupName(project.code);
				this.adServices.createGroup(groupName, true);
				Logger.info("'" + groupName + "' group created for project '" + project.code + "'");

				String adminGroupName = this.adServices.generateAdminGroupName(project.code);
				this.adServices.createGroup(adminGroupName, false);
				Logger.info("'" + adminGroupName + "' admin group created for project '" + project.code + "'");

			} catch (NamingException e) {
				e.printStackTrace();
			}
		});
	}

	@Override
	public void execute(XSSFWorkbook workbook) throws Exception {
		Logger.debug("Start update missing AD Group");
		
		createADGroup(workbook);

		Logger.debug("End update missing AD Group");
	}
}