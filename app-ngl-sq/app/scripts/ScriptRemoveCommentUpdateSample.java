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
import models.laboratory.sample.instance.Sample;
import models.utils.InstanceConstants;
import play.Logger;
import validation.ContextValidation;

/**
 * Script reprise histo ticket NGL-3569
 * Suppression commentaire technique de mise à jour dans les commentaires du sample lorsque celui est mode create 
 * ReceptionConfig : dd/MM/yy : échantillon mis à jour via le fichier imporTypeValue + ajout _ pour concaténation commentaire
 * 
 * @author ejacoby
 *
 */
public class ScriptRemoveCommentUpdateSample extends ScriptWithExcelBody{



	@SuppressWarnings("unchecked")
	public void execute(XSSFWorkbook workbook) throws Exception {
		ContextValidation validation = ContextValidation.createUpdateContext("ngl-support");
		validation.putObject("RecapSample", new ArrayList<String>());
		((List<String>)validation.getObject("RecapSample")).add("Code sample,old sample,new comment");
		
		String regex1 = "\\d{2}/\\d{2}/\\d{2} : échantillon mis à jour via le fichier rna-reception";
		String regex2 = "\\d{2}/\\d{2}/\\d{2} : échantillon mis à jour via le fichier dna-reception";
		String regex3 = " _ \\d{2}/\\d{2}/\\d{2} : échantillon mis à jour via le fichier dna-reception";
		String regex4 = "\\d{2}/\\d{2}/\\d{2} : échantillon mis à jour via le fichier biological-sample-reception";
		String regex5 = "\\d{2}/\\d{2}/\\d{2} : échantillon mis à jour via le fichier plate-from-bank-reception";
		String regex6 = " _ \\d{2}/\\d{2}/\\d{2} : échantillon mis à jour via le fichier plate-from-bank-reception";
		String regex7 = "\\d{2}/\\d{2}/\\d{2} : échantillon mis à jour via le fichier tube-from-bank-reception";
		String regex8 = " _ \\d{2}/\\d{2}/\\d{2} : échantillon mis à jour via le fichier library-idx-reception-internal-team";
		String regex9 = "\\d{2}/\\d{2}/\\d{2} : échantillon mis à jour via le fichier library-idx-reception-internal-team";
		String regex10 = " _ \\d{2}/\\d{2}/\\d{2} : échantillon mis à jour via le fichier library-idx-reception";
		workbook.getSheetAt(0).rowIterator().forEachRemaining(row -> {
			if(row.getRowNum() == 0) return; // skip header
			if(row!=null){
				String codeSample = row.getCell(0).getStringCellValue();
				Sample sample = MongoDBDAO.findByCode(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, codeSample);
				Logger.debug("Sample Before "+sample.code+"="+sample.comments.get(0).comment);
				String oldComment = sample.comments.get(0).comment;
				
				sample.comments.get(0).comment = sample.comments.get(0).comment.replaceAll(regex1, "");
				sample.comments.get(0).comment = sample.comments.get(0).comment.replaceAll(regex2, "");
				sample.comments.get(0).comment = sample.comments.get(0).comment.replaceAll(regex3, "");
				sample.comments.get(0).comment = sample.comments.get(0).comment.replaceAll(regex4, "");
				sample.comments.get(0).comment = sample.comments.get(0).comment.replaceAll(regex5, "");
				sample.comments.get(0).comment = sample.comments.get(0).comment.replaceAll(regex6, "");
				sample.comments.get(0).comment = sample.comments.get(0).comment.replaceAll(regex7, "");
				sample.comments.get(0).comment = sample.comments.get(0).comment.replaceAll(regex8, "");
				sample.comments.get(0).comment = sample.comments.get(0).comment.replaceAll(regex9, "");
				sample.comments.get(0).comment = sample.comments.get(0).comment.replaceAll(regex10, "");
				Logger.debug("Sample After "+sample.code+"="+sample.comments.get(0).comment);
				((List<String>)validation.getObject("RecapSample")).add(sample.code+","+oldComment+","+sample.comments.get(0).comment);
				MongoDBDAO.update(InstanceConstants.SAMPLE_COLL_NAME, sample);
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
