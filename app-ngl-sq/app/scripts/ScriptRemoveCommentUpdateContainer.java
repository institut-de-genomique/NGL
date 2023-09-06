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
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptWithExcelBody;
import models.laboratory.container.instance.Container;
import models.utils.InstanceConstants;
import play.Logger;
import validation.ContextValidation;

/**
 * Script reprise histo ticket NGL-3508
 * Suppression commentaire technique de mise à jour dans les commentaires du container 
 * ReceptionConfig : dd/MM/yy : échantillon mis à jour via le fichier imporTypeValue + ajout _ pour concaténation commentaire
 * Script Update _ dd/MM/yy : l'import type Update Tara Pacific est remis en Réception tara pacific (cf. NGL-XXXX) 
 * _ échantillon mis à jour via le fichier update tara pacific
 * Numéro ticket support
 * @author ejacoby
 *
 */
public class ScriptRemoveCommentUpdateContainer extends ScriptWithExcelBody{



	@SuppressWarnings("unchecked")
	public void execute(XSSFWorkbook workbook) throws Exception {
		ContextValidation validation = ContextValidation.createUpdateContext("ngl-support");
		validation.putObject("RecapContainer", new ArrayList<String>());
		((List<String>)validation.getObject("RecapContainer")).add("Code container,old comment,new comment");
		
		String regex = "- \\d{2}/\\d{2}/\\d{2} : l'import type \"Update Tara Pacific\" est remis en \"Réception tara pacific\" \\(cf\\. NGL-3317\\)- échantillon mis à jour via le fichier update tara pacific_";
		String regex2 = "_ \\d{2}/\\d{2}/\\d{2} : container mis à jour via le fichier update-tara-pacific";
		String regex3 = "_ \\d{2}/\\d{2}/\\d{2} : container mis à jour via le fichier update-tara-microbiome";
		String regex4 = "\\d{2}/\\d{2}/\\d{2} : container mis à jour via le fichier update-tara-microbiome";
		String regex5 = "- \\d{2}/\\d{2}/\\d{2} : l'import type \"Update Tara Pacific\" est remis en \"Réception tara pacific\" \\(cf\\. NGL-3317\\)- échantillon mis à jour via le fichier update tara pacific";
		workbook.getSheetAt(0).rowIterator().forEachRemaining(row -> {
			if(row.getRowNum() == 0) return; // skip header
			if(row!=null){
				String codeContainer = row.getCell(0).getStringCellValue();
				Container container = MongoDBDAO.findByCode(InstanceConstants.CONTAINER_COLL_NAME, Container.class, codeContainer);
				Logger.debug("Container Before "+container.code+"="+container.comments.get(0).comment);
				String oldComment = container.comments.get(0).comment;
				
				container.comments.get(0).comment = container.comments.get(0).comment.replaceAll(regex, "");
				container.comments.get(0).comment = container.comments.get(0).comment.replaceAll(regex2, "");
				container.comments.get(0).comment = container.comments.get(0).comment.replaceAll(regex3, "");
				container.comments.get(0).comment = container.comments.get(0).comment.replaceAll(regex4, "");
				container.comments.get(0).comment = container.comments.get(0).comment.replaceAll(regex5, "");
				Logger.debug("Container After "+container.code+"="+container.comments.get(0).comment);
				((List<String>)validation.getObject("RecapContainer")).add(container.code+","+oldComment+","+container.comments.get(0).comment);
				MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME, container);
			}
		});
		createExcelFileRecap(validation);
	}

	@SuppressWarnings("unchecked")
	public static void createExcelFileRecap(ContextValidation cv)
	{
		Workbook wb = new HSSFWorkbook();
		CreationHelper createHelper = wb.getCreationHelper();
		for(String key : cv.getContextObjects().keySet()){
			Sheet sheet = wb.createSheet(key);
			List<String> recaps = (List<String>) cv.getObject(key);
			int nbLine=0;
			for(String recap : recaps){
				//Logger.debug(recap);
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
		try (OutputStream fileOut = new FileOutputStream("Test.xls")) {
			wb.write(fileOut);
		}catch(Exception e){
			Logger.debug(e.getMessage());
		}
	}
}
