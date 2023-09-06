package controllers.reagents.io.receptions.mappers.cell;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import org.apache.poi.ss.usermodel.Cell;

public interface DateCellMapper {
	
	public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	
	public default Date stringToDateMapping(String cellValue) {
		LocalDate localDate = LocalDate.parse(cellValue, DATE_FORMATTER);
		Instant instant = Instant.from(localDate);
		return Date.from(instant);
	}
	
	public default Date handleStringToDateMapping(Cell cell) {
		String cellValue = cell.getStringCellValue();
		try {
			return stringToDateMapping(cellValue);
		} catch(DateTimeException | NullPointerException e) {
			throw new IllegalStateException("Error getting date from string cell: " + String.valueOf(cellValue), e);
		}
	}
	
	public default Date handleDateCellMapping(Cell cell) {
		try {
			return cell.getDateCellValue();
		} catch(NumberFormatException e) {
			throw new IllegalStateException("Error getting date from numeric cell: not a parsable double", e);
		}
	}
	
	public default Date getDate(Cell cell) {
		switch (cell.getCellType()) {
		case Cell.CELL_TYPE_STRING:
			return handleStringToDateMapping(cell);
		case Cell.CELL_TYPE_NUMERIC:
			return handleDateCellMapping(cell);
		default:
			throw new IllegalStateException("Invalid cell type for date: " + cell.getCellType());
		}
	}

}
