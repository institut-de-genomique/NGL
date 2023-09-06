package controllers.reagents.io.receptions.helpers;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import fr.cea.ig.ngl.dao.api.APIException;
import models.laboratory.common.instance.property.PropertyFileValue;

public class ReceptionsFileReader {
	
	private Workbook readExcelFile(PropertyFileValue pfv) throws APIException {
		try {
			byte[] byteValue = pfv.byteValue();
			InputStream inputStream = new ByteArrayInputStream(byteValue);
			return WorkbookFactory.create(inputStream);
		} catch(InvalidFormatException | IOException e) {
			throw new APIException("An error has occured trying to extract receptions from file: " + e.getMessage(), e);
		}
	}
	
	public Iterator<Row> getRows(PropertyFileValue pfv) throws APIException {
		final Workbook wb = readExcelFile(pfv);
		final Sheet sheet = wb.getSheetAt(0);
		final Iterator<Row> rows = sheet.rowIterator();
		rows.next(); // remove header columns
		return rows;
	}

}
