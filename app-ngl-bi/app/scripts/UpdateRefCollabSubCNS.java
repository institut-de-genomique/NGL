package scripts;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptWithExcelBody;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.run.instance.ReadSet;
import models.utils.InstanceConstants;
import play.Logger;
import validation.ContextValidation;

public class UpdateRefCollabSubCNS extends ScriptWithExcelBody{

	/**
	 * Script permettant de modifier/ ajouter une refCollabSub sur un ensemble de readset CNS
	 * Prend en entrée un fichier excel au format suivant 
	 * onglet "index"
	 * CodeReadset | refCollabSub
	 * Il génère un fichier scriptUpdateRefCollabSub_out.xls dans le repertoire d'éxecution
	 * Code readset | old refCollabSub | new refCollabSub
	 */
	
	
	final String user = "ngl-support";
	
	@Override
	@SuppressWarnings("unchecked")
	public void execute(XSSFWorkbook workbook) throws Exception {

		ContextValidation cv = ContextValidation.createUpdateContext(user);
		cv.putObject("RecapReadSet", new ArrayList<String>());
		//Add Header Recap
		((List<String>)cv.getObject("RecapReadSet")).add("Code readset,old refCollabSub,new refCollabSub");

		workbook.getSheetAt(0).rowIterator().forEachRemaining(row -> {
			if(row.getRowNum() == 0) return; // skip header
			
		
			if(row!=null && row.getCell(0)!=null && row.getCell(1)!=null){
				String readsetCode = row.getCell(0).getStringCellValue();
				String newRefCollabSub = row.getCell(1).getStringCellValue();
				Logger.debug("Code readset "+readsetCode+" refCollabSub "+newRefCollabSub);

				PropertyValue oldRefCollabSub=new PropertySingleValue("not defined");

				//Get readsetCode
				ReadSet readset = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readsetCode);
				if(readset!=null){
					//Create contexte validation 
					ContextValidation ctx = ContextValidation.createUpdateContext("ngl-support");

					if (readset.properties.get("refCollabSub") != null)
						oldRefCollabSub = readset.properties.get("refCollabSub");
				
						readset.properties.put("refCollabSub", new PropertySingleValue(newRefCollabSub.toString()));
					
						readset.traceInformation.modifyUser=user;
						readset.traceInformation.modifyDate= new Date();
						
					readset.validate(ctx);
					
					((List<String>)cv.getObject("RecapReadSet")).add(readsetCode+","+oldRefCollabSub.getValue()+","+newRefCollabSub);

					
					if(!ctx.hasErrors()){
						MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, readset);
					}else{
						Logger.error("Error readset update refCollabSub "+readsetCode+" "+ctx.getErrors());
					}
				}else{
					Logger.error("Readset code not found "+readsetCode);
				}
			}
		});

		//Create excel file to recap in execution directory
		createExcelFileRecap(cv);

		println("End of refCollabSub update ");
		Logger.info("End of refCollabSub update ");
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
		try (OutputStream fileOut = new FileOutputStream("scriptUpdateRefCollabSub_out.xls")) {
			wb.write(fileOut);
			fileOut.flush();
		}catch(Exception e){
			Logger.debug(e.getMessage());
		}

	}
}
