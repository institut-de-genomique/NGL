package scripts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.chunked.ScriptWithExcelBody;
import models.laboratory.common.instance.property.PropertyObjectValue;
import models.laboratory.project.instance.Project;
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
				
				Map<String, Object> map = new HashMap<>();
				map.put("scratch", scratchQtree);
				map.put("proj", 0 /* projQtree */);
				map.put("rawdata", 0) /* rawQtree */;

				projectCode = projectCode.trim();
				
				if (projectCode.trim().equals(p.code)) {
					projectsUpdated.add(p);

					Project projEnBase = MongoDBDAO.findByCode(InstanceConstants.PROJECT_COLL_NAME, Project.class, projectCode);
					projEnBase.properties.put("qtreeQuota", new PropertyObjectValue(map));
					
					MongoDBDAO.update(InstanceConstants.PROJECT_COLL_NAME, projEnBase);
				}
			});
		}	

		List<Project> notUpdated = new ArrayList<>(allProjectsList);
		notUpdated.removeAll(projectsUpdated);

		for (int i = 0; i < notUpdated.size(); i++) {
			Logger.debug("Updating " + notUpdated.get(i).code);

			Map<String, Object> map = new HashMap<>();
			map.put("scratch", 0);
			map.put("proj", 0);
			map.put("rawdata", 0);

			Project projEnBase = MongoDBDAO.findByCode(InstanceConstants.PROJECT_COLL_NAME, Project.class, notUpdated.get(i).code);
			projEnBase.properties.put("qtreeQuota", new PropertyObjectValue(map));
			
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