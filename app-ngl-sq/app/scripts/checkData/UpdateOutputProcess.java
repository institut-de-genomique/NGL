package scripts.checkData;

import java.io.File;
import java.io.FileInputStream;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.chunked.ScriptWithArgsAndBody;
import models.laboratory.processes.instance.Process;
import models.utils.InstanceConstants;
import play.Logger;
import play.mvc.Http.RequestBody;
import scripts.AddProcessPropertiesContent;
import scripts.AddProcessPropertiesContent.Args;

/**
 * Script permettant la mise à jour des processus après éxecution du script CheckOutputProcess
 * 
 * Nettoyage des code container out des processus en fonction du fichier récap du script CheckOutputProcess
 * 
 * 
 * @author ejacoby
 *
 */
public class UpdateOutputProcess extends ScriptWithArgsAndBody<AddProcessPropertiesContent.Args>{

	@Override
	public void execute(Args args, RequestBody body) throws Exception {


		//Read excel file
		File fxlsx = (File) body.asMultipartFormData().getFile("xlsx").getFile();
		FileInputStream fis = new FileInputStream(fxlsx);

		XSSFWorkbook workbook = new XSSFWorkbook (fis);// workbook du
		workbook.getSheetAt(0).rowIterator().forEachRemaining(row -> {
			if(row.getRowNum() == 0) return; // skip header
			if(row!=null && row.getCell(0)!=null && row.getCell(1)!=null){
				String processCode = row.getCell(0).getStringCellValue();
				String badCodeContainer = row.getCell(1).getStringCellValue();
				Logger.debug("Update "+processCode+" for bad container "+badCodeContainer);
				//Get process to update
				Process process = MongoDBDAO.findByCode(InstanceConstants.PROCESS_COLL_NAME, Process.class, processCode);
				process.outputContainerCodes.remove(badCodeContainer);
				MongoDBDAO.update(InstanceConstants.PROCESS_COLL_NAME, Process.class,DBQuery.is("code", processCode), DBUpdate.set("outputContainerCodes", process.outputContainerCodes));
			}
		});
	}

}
