package scripts;

import java.util.List;
import javax.inject.Inject;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.chunked.ScriptWithExcelBody;
import fr.cea.ig.ngl.dao.projects.MembersProjectsAPI;
import models.laboratory.project.instance.Project;
import models.utils.InstanceConstants;
import play.Logger;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * @author jcharpen - Jordi CHARPENTIER - jcharpen@genoscope.cns.fr
 */
public class ScriptUpdateADGroup extends ScriptWithExcelBody {
	
	private MembersProjectsAPI membersApi;

	@Inject
	public ScriptUpdateADGroup(MembersProjectsAPI membersApi) {
		super();
		this.membersApi = membersApi;
	}

	private void updateADGroup(XSSFWorkbook workbook) {
		List<Project> allProjectsList = MongoDBDAO.find(InstanceConstants.PROJECT_COLL_NAME, Project.class).toList();
		
		for (int i = 0; i < allProjectsList.size(); i++) {			
			Project p = allProjectsList.get(i);
			
			workbook.getSheetAt(0).rowIterator().forEachRemaining(row -> {				
				String projectCode = row.getCell(0).getStringCellValue();				
				projectCode = projectCode.trim();
				
				if (projectCode.trim().equals(p.code)) {
					membersApi.delete(p.code, "vmeyer", "admin");
					membersApi.delete(p.code, "boland", "admin");
					membersApi.delete(p.code, "bacq", "admin");
				}
			});
		}	
		
		Logger.debug("Terminé");
	}

	@Override
	public void execute(XSSFWorkbook workbook) throws Exception {
		Logger.debug("Start update AD Group");
		
		updateADGroup(workbook);
	}
}