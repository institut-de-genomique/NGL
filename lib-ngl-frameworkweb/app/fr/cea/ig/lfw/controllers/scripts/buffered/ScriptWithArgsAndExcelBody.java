package fr.cea.ig.lfw.controllers.scripts.buffered;

import java.io.File;
import java.io.FileInputStream;

import org.apache.poi.POIXMLException;
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
		File xlsx = null;
		try {
			xlsx = (File) body.asMultipartFormData().getFile("xlsx").getFile();
		} catch(Exception e) {
			throw new Exception("Exception = ", e);
		}
		try (FileInputStream fis = new FileInputStream(xlsx)) {
			XSSFWorkbook workbook = new XSSFWorkbook (fis);
			execute(args, workbook);
		} catch(POIXMLException e) {
			throw new Exception("Le fichier d'entree dans le body est absent ou dans un mauvais format (utiliser un fichier d'entree exel avec l'extention 'xlsx' ");
		}
	}
	
	/**
	 * Script execution code.
	 * @param workbook   excel workbook
	 * @throws Exception an error occurred
	 */
	public abstract void execute(T args, XSSFWorkbook workbook) throws Exception;

}
