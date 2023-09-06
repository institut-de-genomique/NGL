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
import models.laboratory.sample.instance.Sample;
import models.utils.InstanceConstants;
import play.Logger;
import validation.ContextValidation;

/**
 * Script pour NGL-3507 pour remplacer des caractères dans commentaire mal interprété par excel 
 * @author ejacoby
 *
 */
public class ScriptUpdateCommentSpecialCharacter extends ScriptWithExcelBody{



	@SuppressWarnings("unchecked")
	public void execute(XSSFWorkbook workbook) throws Exception {
		ContextValidation validation = ContextValidation.createUpdateContext("ngl-support");
		validation.putObject("RecapSample", new ArrayList<String>());
		((List<String>)validation.getObject("RecapSample")).add("Code sample,old comment,new comment");
		validation.putObject("RecapContainer", new ArrayList<String>());
		((List<String>)validation.getObject("RecapContainer")).add("Code container,old comment,new comment");
		Logger.debug("Get samples ");
		//List<Sample> samplesToUpdate = MongoDBDAO.find(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, 
		//		DBQuery.regex("comments.0.comment", Pattern.compile("\r\n-"))).toList();
		//int nbSample = samplesToUpdate.size();
		workbook.getSheetAt(0).rowIterator().forEachRemaining(row -> {
			if(row.getRowNum() == 0) return; // skip header
			if(row!=null){
				String codeSample = row.getCell(0).getStringCellValue();
				Sample sample = MongoDBDAO.findByCode(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, codeSample);
				Logger.debug("Sample Before "+sample.code+"="+sample.comments.get(0).comment);
				String oldComment = sample.comments.get(0).comment;
				sample.comments.get(0).comment = sample.comments.get(0).comment.replaceAll("\r\n-", " _ ");
				Logger.debug("Sample After "+sample.code+"="+sample.comments.get(0).comment);
				((List<String>)validation.getObject("RecapSample")).add(sample.code+","+oldComment+","+sample.comments.get(0).comment);
				MongoDBDAO.update(InstanceConstants.SAMPLE_COLL_NAME, sample);
			}
		});

		workbook.getSheetAt(1).rowIterator().forEachRemaining(row -> {
			if(row.getRowNum() == 0) return; // skip header
			if(row!=null){
				String codeContainer = row.getCell(0).getStringCellValue();
				Container container = MongoDBDAO.findByCode(InstanceConstants.CONTAINER_COLL_NAME, Container.class, codeContainer);
				Logger.debug("Container Before "+container.code+"="+container.comments.get(0).comment);
				String oldComment = container.comments.get(0).comment;
				container.comments.get(0).comment = container.comments.get(0).comment.replaceAll("\r\n-", "_");
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
