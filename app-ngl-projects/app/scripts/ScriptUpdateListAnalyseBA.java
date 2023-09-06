package scripts;

import java.util.ArrayList;
import java.util.List;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.chunked.ScriptWithExcelBody;
import models.laboratory.common.instance.property.PropertyListValue;
import models.laboratory.project.instance.Project;
import models.utils.InstanceConstants;
import play.Logger;
import validation.ContextValidation;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Script permettant de mettre à jour la liste des types analyses des projets BA
 * Entrée fichier excel avec header 
 * Colonne 1 : Code projet
 * Colonne 2 : Code type analyse
 * 
 * Exemple d'appel du script :
 * curl -X POST \
 * http://localhost:9000/scripts/run/scripts.ScriptUpdateListAnalyseBA \
 * -H 'cache-control: no-cache' \
 * -F xlsx=@test.xlsx
 * 
 * @author ejacoby
 */
public class ScriptUpdateListAnalyseBA extends ScriptWithExcelBody {
	
	
	
	@Override
	public void execute(XSSFWorkbook workbook) throws Exception {
		Logger.debug("Start update synchroProj");
		workbook.getSheetAt(0).rowIterator().forEachRemaining(row -> {		
			if(row.getRowNum() == 0) return; // skip header
			ContextValidation ctx = ContextValidation.createUpdateContext("ngl-support");
			String projectCode = row.getCell(0).getStringCellValue();
			String codeTypeAnalyse = row.getCell(1).getStringCellValue();
			projectCode = projectCode.trim();
			Logger.debug("Update Project "+projectCode);
			Project project = MongoDBDAO.findByCode(InstanceConstants.PROJECT_COLL_NAME, Project.class, projectCode);
			List<Object> values = new ArrayList<Object>();
			if(project.properties.containsKey("analysisTypes")) {
				values = ((PropertyListValue)project.properties.get("analysisTypes")).listValue();
			}
			values.add(codeTypeAnalyse);
			project.properties.put("analysisTypes", new PropertyListValue(values));
			project.validate(ctx);
			if(!ctx.hasErrors())
				MongoDBDAO.update(InstanceConstants.PROJECT_COLL_NAME, project);
			else {
				Logger.error("Project not validate "+projectCode);
				Logger.error(ctx.getErrors().toString());
			}
			
			
			
		});
	}
}