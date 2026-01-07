package scripts;

import java.util.ArrayList;
import java.util.List;
import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.chunked.ScriptWithExcelBody;
import models.laboratory.project.instance.Project;
import models.laboratory.project.instance.SystemParameters.QtreeQuota;
import models.utils.InstanceConstants;
import play.Logger;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * @author jcharpen - Jordi CHARPENTIER - jcharpen@genoscope.cns.fr
 */
public class ScriptUpdateQtree extends ScriptWithExcelBody {
	
	private void updateQtree(XSSFWorkbook workbook) {
		List<Project> allProjectsList = MongoDBDAO.find(InstanceConstants.PROJECT_COLL_NAME, Project.class).toList();
		List<Project> projectsUpdated = new ArrayList<>();
		
		for (int i = 0; i < allProjectsList.size(); i++) {			
			Project p = allProjectsList.get(i);
			
			workbook.getSheetAt(0).rowIterator().forEachRemaining(row -> {				
				String projectCode = row.getCell(0).getStringCellValue();
				
				double scratchQtree = row.getCell(1).getNumericCellValue();
				// double rawQtree = row.getCell(2).getNumericCellValue();
				// double projQtree = row.getCell(3).getNumericCellValue();
				
				QtreeQuota qtreeQuota = new QtreeQuota();
				qtreeQuota.scratch = (int) scratchQtree;
				qtreeQuota.proj = 0; /* projQtree */
				qtreeQuota.rawdata = 0; /* rawQtree */

				projectCode = projectCode.trim();
				
				if (projectCode.trim().equals(p.code)) {
					projectsUpdated.add(p);

					Project projEnBase = MongoDBDAO.findByCode(InstanceConstants.PROJECT_COLL_NAME, Project.class, projectCode);
					projEnBase.systemParameters.qtreeQuota = qtreeQuota;
					
					MongoDBDAO.update(InstanceConstants.PROJECT_COLL_NAME, projEnBase);
				}
			});
		}	

		List<Project> notUpdated = new ArrayList<>(allProjectsList);
		notUpdated.removeAll(projectsUpdated);

		for (int i = 0; i < notUpdated.size(); i++) {
			Logger.debug("Updating " + notUpdated.get(i).code);
			
			QtreeQuota qtreeQuota = new QtreeQuota();
			qtreeQuota.scratch = 0;
			qtreeQuota.proj = 0;
			qtreeQuota.rawdata = 0;

			Project projEnBase = MongoDBDAO.findByCode(InstanceConstants.PROJECT_COLL_NAME, Project.class, notUpdated.get(i).code);
			projEnBase.systemParameters.qtreeQuota = qtreeQuota;
			
			MongoDBDAO.update(InstanceConstants.PROJECT_COLL_NAME, projEnBase);
		}
		
		Logger.debug("Termine");
	}

	@Override
	public void execute(XSSFWorkbook workbook) throws Exception {
		Logger.debug("Start update qtree");
		
		updateQtree(workbook);

		Logger.debug("End update qtree");
	}
}