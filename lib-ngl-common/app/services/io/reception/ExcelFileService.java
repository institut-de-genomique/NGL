package services.io.reception;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import models.laboratory.common.instance.property.PropertyFileValue;
import models.laboratory.reception.instance.ReceptionConfiguration;
import services.io.ExcelHelper;
import validation.ContextValidation;

public class ExcelFileService extends FileService {
	
	private static final play.Logger.ALogger logger = play.Logger.of(ExcelFileService.class);
	
	public ExcelFileService(ReceptionConfiguration configuration, PropertyFileValue fileValue, ContextValidation contextValidation) {
		super(configuration, fileValue, contextValidation);		
	}

//	@Override
//	public void analyse() {
//		try {
//			// compute header label and column position
//			Sheet sheet = getSheet(0);
//			headerByIndex = convertRow(sheet.getRow(0));
//			updateHeaderConfiguration();
//			if (headerByIndex == null) {
//				contextValidation.addError("Headers", "not found");
//			} else {
//				Iterator<Row> iti = sheet.rowIterator();
//				iti.next();
//				while (iti.hasNext()) {
//					Row row = iti.next();
//					Map<Integer, String> rowMap = convertRow(row);
//					if (rowMap != null) {
//						contextValidation.addKeyToRootKeyName("line " + (row.getRowNum()+1));
//						treatLine(rowMap);
//						contextValidation.removeKeyFromRootKeyName("line " + (row.getRowNum()+1));
//					}
//				}
//				if (!contextValidation.hasErrors()) {
//					consolidateObjects();
//					saveObjects();
//				}				
//			}
//		} catch(Exception e) {
//			logger.error("Error import file "+e.getMessage(),e);
//			contextValidation.addError("Exception contact your administrator", e.getMessage());
//		}
//	}
	@Override
	public void analyse() {
		try {
			// compute header label and column position
			Sheet sheet = getSheet(0);
			headerByIndex = convertRow(sheet.getRow(0));
			updateHeaderConfiguration();
			if (headerByIndex == null) {
				contextValidation.addError("Headers", "not found");
				return;
			}
			Iterator<Row> iti = sheet.rowIterator();
			iti.next();
			while (iti.hasNext()) {
				Row row = iti.next();
				Map<Integer, String> rowMap = convertRow(row);
				if (rowMap != null) {
					contextValidation.addKeyToRootKeyName("line " + (row.getRowNum()+1));
					treatLine(rowMap);
					contextValidation.removeKeyFromRootKeyName("line " + (row.getRowNum()+1));
				}
			}
			if (!contextValidation.hasErrors()) {
				consolidateObjects();
				saveObjects();
			}				
		} catch(Exception e) {
			logger.error("Error import file "+e.getMessage(),e);
			contextValidation.addError("Exception contact your administrator", e.getMessage());
		}
	}

	/**
	 * Return map for row with at least one cell not empty.
	 * @param  row row
	 * @return map 
	 */
	private Map<Integer, String> convertRow(Row row) {
		Iterator<Cell> iti = row.cellIterator();
		Map<Integer, String> rowMap = new TreeMap<>();
		boolean isBlankLine = true;
		while (iti.hasNext()) {
			Cell cell = iti.next();
			int columnIndex = cell.getColumnIndex();
			String value = ExcelHelper.convertToStringValue(cell);
			
			if (StringUtils.isNotBlank(value)) {
				value = value.replaceAll("\u00A0"," ").trim();
				isBlankLine = false;
				rowMap.put(columnIndex, value);
			}
						
		}
		if (!isBlankLine) {
			return rowMap;
		} else {
			return null;
		}
	}
//	private Map<Integer, String> convertRow(Row row) {
//		Map<Integer, String> rowMap = Iterables
//				.zen(() -> row.cellIterator())
//				.map(c -> {
//					String value = ExcelHelper.convertToStringValue(c);
//					if (StringUtils.isNotBlank(value)) {
//						value = value.replaceAll("\u00A0"," ").trim();
//						return T.t2(c.getColumnIndex(), value);
//					}
//					return null;
//				})
//				.filter(t -> t != null)
//				.toMap(t -> t.a, t -> t.b);
//		return rowMap.size() == 0 ? null : rowMap;
//	}

	private Sheet getSheet(Integer sheetNumber) throws IOException, InvalidFormatException {
		try (InputStream is = new ByteArrayInputStream(fileValue.byteValue())) {
			Workbook wb = WorkbookFactory.create(is);
			Sheet sheet = wb.getSheetAt(sheetNumber);
			return sheet;
		}
	}
	
}
