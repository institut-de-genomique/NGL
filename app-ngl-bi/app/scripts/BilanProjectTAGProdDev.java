package scripts;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.mongojack.DBQuery;
import com.mongodb.BasicDBObject;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptNoArgs;
import models.laboratory.project.instance.Project;
import models.laboratory.run.instance.ReadSet;
import models.utils.InstanceConstants;
import play.Logger;
import validation.ContextValidation;

public class BilanProjectTAGProdDev extends ScriptNoArgs{



	final String user = "ngl-support";

	@Override
	@SuppressWarnings("unchecked")
	public void execute() throws Exception {

		ContextValidation cv = ContextValidation.createUpdateContext(user);
		cv.putObject("RecapProject", new ArrayList<String>());
		//Add Header Recap
		((List<String>)cv.getObject("RecapProject")).add("Code project,nb tag PROD,nb tag DEV");

		//Liste les projets
		List<Project> projects = MongoDBDAO.find(InstanceConstants.PROJECT_COLL_NAME, Project.class).toList();
		
		BasicDBObject keys = new BasicDBObject();
		keys.put("treatments", 0);
		for(Project project : projects) {
			Logger.debug("Count for project "+project.code);
			List<ReadSet> codeReadsetsDEV = MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.and(DBQuery.is("projectCode",project.code),DBQuery.is("sampleOnContainer.properties.devProdContext.value", "DEV")),keys).toList();
			List<ReadSet> codeReadsetsPROD = MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.and(DBQuery.is("projectCode",project.code),DBQuery.is("sampleOnContainer.properties.devProdContext.value", "PROD")),keys).toList();
			
			((List<String>)cv.getObject("RecapProject")).add(project.code+","+codeReadsetsPROD.size()+","+codeReadsetsDEV.size());
		}
		
		//Create excel file to recap in execution directory
		createExcelFileRecap(cv);

		Logger.info("End of recapBilanTagProdDEV");

	}

	
	/**
	 * Create excel file in execution directory
	 * File recap : list all objects updated
	 * @param cv
	 */
	@SuppressWarnings("unchecked")
	private void createExcelFileRecap(ContextValidation cv)
	{
		Workbook wb = new HSSFWorkbook();
		CreationHelper createHelper = wb.getCreationHelper();
		for(String key : cv.getContextObjects().keySet()){
			Sheet sheet = wb.createSheet(key);
			List<String> recaps = (List<String>) cv.getObject(key);
			int nbLine=0;
			for(String recap : recaps){
				//	Logger.debug("recap: "+recap);
				Row row = sheet.createRow(nbLine);
				String[] tabRecap = recap.split(",");
				for(int i=0;i<tabRecap.length;i++){
					row.createCell(i).setCellValue(
							createHelper.createRichTextString(tabRecap[i]));
				}
				nbLine++;
			}
		}

		// Write the output to a file
		try (OutputStream fileOut = new FileOutputStream("outputScript_recapProjetTagProdDEV.xls")) {
			wb.write(fileOut);
			fileOut.flush();
		}catch(Exception e){
			Logger.debug(e.getMessage());
		}

	}

}
