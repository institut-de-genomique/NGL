package scripts;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.chunked.ScriptWithArgsAndBody;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.processes.instance.Process;
import models.utils.InstanceConstants;
import play.Logger;
import play.mvc.Http.RequestBody;
import validation.ContextValidation;
import workflows.process.ProcWorkflowHelper;

/**
 * Script reprise histo NGL_2957 : ajout propriété process niveau content et propagation
 * Excel file en entrée
 * ProcessCode newPropertyCode newPropertyValue
 * 
 * ATTENTION les tags secondaires ne sont pas pris en compte
 * @author ejacoby
 *
 */

public class AddProcessPropertiesContent extends ScriptWithArgsAndBody<AddProcessPropertiesContent.Args>{

	final String user = "ngl-support";

	private ProcWorkflowHelper procWorkflowHelper;

	public static class Args {
	}


	@Inject
	public AddProcessPropertiesContent(ProcWorkflowHelper procWorkflowHelper)
	{
		this.procWorkflowHelper=procWorkflowHelper;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void execute(Args args, RequestBody body) throws Exception {
		ContextValidation validation = ContextValidation.createUpdateContext(user);
		validation.putObject("RecapContainer", new ArrayList<String>());
		validation.putObject("RecapExperiment", new ArrayList<String>());
		validation.putObject("RecapProcess", new ArrayList<String>());
		validation.putObject("RecapReadSet", new ArrayList<String>());

		//Add Header Recap
		((List<String>)validation.getObject("RecapContainer")).add("Code process,code container,codeProperty,valueProperty");
		((List<String>)validation.getObject("RecapExperiment")).add("Code process,code experiment,codeProperty,valueProperty");
		((List<String>)validation.getObject("RecapProcess")).add("Code process,code process udpated,codeProperty,valueProperty");
		((List<String>)validation.getObject("RecapReadSet")).add("Code process,code readSet,codeProperty,valueProperty");
		//Read excel file
		File fxlsx = (File) body.asMultipartFormData().getFile("xlsx").getFile();
		FileInputStream fis = new FileInputStream(fxlsx);

		XSSFWorkbook workbook = new XSSFWorkbook (fis);// workbook du
		workbook.getSheetAt(0).rowIterator().forEachRemaining(row -> {
			if(row.getRowNum() == 0) return; // skip header
			if(row!=null && row.getCell(0)!=null && row.getCell(1)!=null && row.getCell(2)!=null){
				String processCode = row.getCell(0).getStringCellValue();
				String newPropertyCode = row.getCell(1).getStringCellValue();
				String newPropertyValue = row.getCell(2).getStringCellValue();

				//Get processus
				Process process = MongoDBDAO.findByCode(InstanceConstants.PROCESS_COLL_NAME, Process.class, processCode);

				//add new properti
				if(process.properties==null) {
					process.properties=new HashMap<String,PropertyValue>();
				}
				if(!process.properties.containsKey(newPropertyCode)){
					process.properties.put(newPropertyCode, new PropertySingleValue(newPropertyValue));
				}

				//Call propagate value properties
				Logger.debug("Start updateContentProcessPropertiesAttribute");
				procWorkflowHelper.updateContentProcessPropertiesAttribute(validation, process);
				Logger.debug("Start updateContentPropertiesWithContentProcessProperties");
				//procWorkflowHelper.updateContentPropertiesWithContentProcessProperties(validation, process);
				Set<String> sampleCodes = process.sampleCodes;
				Set<String> projectCodes = process.projectCodes;
				Set<String> tags = ProcessPropertiesContentHelper.getTagAssignFromProcessContainers(process);
				Set<String> outputContainerCodes = process.outputContainerCodes;

				ProcessPropertiesContentHelper.updateProcessContentPropertiesWithCascade(outputContainerCodes, sampleCodes, projectCodes, tags, newPropertyCode, newPropertyValue, process.code, validation);
				
				//update Process 
				MongoDBDAO.update(InstanceConstants.PROCESS_COLL_NAME, process);
				Logger.debug("End update process "+process.code);
			}
		});
		ProcessPropertiesContentHelper.createExcelFileRecap(validation);
	}

	
}
