package scripts.ATSea;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

public class ReadExcelServices {
	
	public static String getStringCellValue(Row row, int numCol)
	{
		if(row.getCell(numCol)!=null)
			return row.getCell(numCol).getStringCellValue();
		else
			return null;
	}
	
	public static Integer getNumericCellValue(Row row, int numCol)
	{
		if(row.getCell(numCol)!=null)
			return (int) row.getCell(numCol).getNumericCellValue();
		else
			return null;
	
	}
	
	public static Date getDateCellValue(Row row, int numCol)
	{
		if(row.getCell(numCol)!=null)
			return row.getCell(numCol).getDateCellValue();
		else
			return null;
	}
	public static boolean checkIfRowIsEmpty(Row row) {
		if (row == null) {
			return true;
		}
		if (row.getLastCellNum() <= 0) {
			return true;
		}
		for (int cellNum = row.getFirstCellNum(); cellNum < row.getLastCellNum(); cellNum++) {
			Cell cell = row.getCell(cellNum);
			if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK && StringUtils.isNotBlank(cell.toString())) {
				return false;
			}
		}
		return true;
	}

}
