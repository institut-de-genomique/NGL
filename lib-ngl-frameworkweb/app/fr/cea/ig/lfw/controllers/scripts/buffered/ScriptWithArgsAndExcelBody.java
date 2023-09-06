package fr.cea.ig.lfw.controllers.scripts.buffered;

import java.io.File;
import java.io.FileInputStream;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import play.mvc.Http.RequestBody;

/*
 * 
 * Script with arguments and an excel body under the 'xlsx' key.
 * @param <T> URL parameter description
 * @author sgas
 *
 */
public abstract class ScriptWithArgsAndExcelBody<T> extends Script<T> {
	@Override
	public void execute(T args) throws Exception {
		RequestBody body = play.mvc.Http.Context.current().request().body();
		File xlsx = (File) body.asMultipartFormData().getFile("xlsx").getFile();
		FileInputStream fis = new FileInputStream(xlsx);
		XSSFWorkbook workbook = new XSSFWorkbook (fis);
		execute(args, workbook);
	}
	
	/**
	 * Script execution code.
	 * @param workbook   excel workbook
	 * @throws Exception an error occurred
	 */
	public abstract void execute(T args, XSSFWorkbook workbook) throws Exception;

}
