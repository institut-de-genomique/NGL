package scripts;

import java.util.Date;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.chunked.ScriptWithExcelBody;
import models.laboratory.project.instance.Project;
import models.utils.InstanceConstants;
import play.Logger;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Script permettant de mettre à jour la date de création des projets à
 * partir d'un fichier en entrée.
 * 
 * Exemple d'appel du script :
 * curl -X POST \
 * http://localhost:9000/scripts/run/scripts.ScriptUpdateCreationDate \
 * -H 'cache-control: no-cache' \
 * -F xlsx=@test.xlsx
 * 
 * @author jcharpen - Jordi CHARPENTIER - jcharpen@genoscope.cns.fr
 */
public class ScriptUpdateCreationDate extends ScriptWithExcelBody {
	
	private void updateSynchroProjCNS(XSSFWorkbook workbook) {			
		workbook.getSheetAt(0).rowIterator().forEachRemaining(row -> {				
			String projectCode = row.getCell(0).getStringCellValue();
			projectCode = projectCode.trim();
			
			Logger.debug("projectCode : " + projectCode);
			
			Date creationDate = row.getCell(1).getDateCellValue();
				
			Project projEnBase = MongoDBDAO.findByCode(InstanceConstants.PROJECT_COLL_NAME, Project.class, projectCode);
			
			if (projEnBase != null) {
				if (creationDate == null) {
					creationDate = new Date();
					creationDate.setTime(0);
					
					projEnBase.traceInformation.creationDate = creationDate;
				} else {				
					projEnBase.traceInformation.creationDate = creationDate;
				}
				
				Logger.debug("project updated");
				
				MongoDBDAO.update(InstanceConstants.PROJECT_COLL_NAME, projEnBase);
			} else {
				Logger.debug("project is missing");
			}
		});
		
		Logger.debug("Terminé");
	}

	@Override
	public void execute(XSSFWorkbook workbook) throws Exception {
		Logger.debug("Start update creationDate");
		
		updateSynchroProjCNS(workbook);
	}
}