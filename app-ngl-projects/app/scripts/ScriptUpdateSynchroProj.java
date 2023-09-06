package scripts;

import java.util.List;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.chunked.ScriptWithExcelBody;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.project.instance.Project;
import models.utils.InstanceConstants;
import play.Logger;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Script permettant de mettre à jour la property synchroProj.
 * Il prend en entrée un fichier Excel avec une liste de projets. 
 * Si un projet est dans le fichier, on met "synchroProj" à TRUE. 
 * Si un projet n'est pas dans le fichier, on met "synchroProj" à FALSE.
 * 
 * Exemple d'appel du script :
 * curl -X POST \
 * http://localhost:9000/scripts/run/scripts.ScriptUpdateSynchroProj \
 * -H 'cache-control: no-cache' \
 * -F xlsx=@test.xlsx
 * 
 * @author jcharpen - Jordi CHARPENTIER - jcharpen@genoscope.cns.fr
 */
public class ScriptUpdateSynchroProj extends ScriptWithExcelBody {
	
	private boolean isPresent = false;
	
	private void updateSynchroProjCNS(XSSFWorkbook workbook) {
		List<Project> allProjectsList = MongoDBDAO.find(InstanceConstants.PROJECT_COLL_NAME, Project.class).toList();
		
		for (int i = 0; i < allProjectsList.size(); i++) {			
			this.isPresent = false;
			Project p = allProjectsList.get(i);
			
			workbook.getSheetAt(0).rowIterator().forEachRemaining(row -> {				
				String projectCode = row.getCell(0).getStringCellValue();
				
				projectCode = projectCode.trim();
				
				if (projectCode.trim().equals(p.code)) {
					this.isPresent = true;
					Project projEnBase = MongoDBDAO.findByCode(InstanceConstants.PROJECT_COLL_NAME, Project.class, projectCode);
					projEnBase.properties.put("synchroProj", new PropertySingleValue(Boolean.TRUE));
					
					MongoDBDAO.update(InstanceConstants.PROJECT_COLL_NAME, projEnBase);
				}
			});
			
			if (!this.isPresent) {
				Project projEnBase = MongoDBDAO.findByCode(InstanceConstants.PROJECT_COLL_NAME, Project.class, allProjectsList.get(i).code);
				projEnBase.properties.put("synchroProj", new PropertySingleValue(Boolean.FALSE));
				
				MongoDBDAO.update(InstanceConstants.PROJECT_COLL_NAME, projEnBase);
			}
		}	
		
		Logger.debug("Terminé");
	}

	@Override
	public void execute(XSSFWorkbook workbook) throws Exception {
		Logger.debug("Start update synchroProj");
		
		updateSynchroProjCNS(workbook);
	}
}