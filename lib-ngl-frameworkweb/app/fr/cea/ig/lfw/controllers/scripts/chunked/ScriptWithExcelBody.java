package fr.cea.ig.lfw.controllers.scripts.chunked;

import java.io.File;
import java.io.FileInputStream;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import play.mvc.Http.RequestBody;

/**
 * Script with no arguments and an excel body under the 'xlsx' key.
 * 
 * @author vrd
 *
 */
public abstract class ScriptWithExcelBody extends ScriptWithBody {
	
	@Override
	public void execute(RequestBody body) throws Exception {
		File xlsx = (File) body.asMultipartFormData().getFile("xlsx").getFile();
		FileInputStream fis = new FileInputStream(xlsx);
		XSSFWorkbook workbook = new XSSFWorkbook (fis);
		execute(workbook);
	}
	
	/**
	 * Script execution code.
	 * @param workbook   excel workbook
	 * @throws Exception an error occurred
	 */
	public abstract void execute(XSSFWorkbook workbook) throws Exception;
	
}
