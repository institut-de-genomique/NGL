package fr.cea.ig.lfw.controllers.scripts.buffered;

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
public abstract class ScriptWithExcelBody extends Script<Object> {
	
	@Override
	public void execute(Object args) throws Exception {
		RequestBody body = play.mvc.Http.Context.current().request().body();
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
