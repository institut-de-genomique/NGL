package controllers.reagents.io.receptions.mappers.cell;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;

public interface StringCellMapper {
	
	public default String numericToStringMapping(Cell cell) {
		// DataFormatter n'est pas threadSafe, il vaut mieux recréer une instance à chaque appel
		DataFormatter formatter = new DataFormatter();
		return formatter.formatCellValue(cell);
	}
	
	public default String getString(Cell cell) {
		switch (cell.getCellType()) {
		case Cell.CELL_TYPE_STRING:
			return cell.getStringCellValue();
		case Cell.CELL_TYPE_NUMERIC:
			return numericToStringMapping(cell);
		default:
			throw new IllegalStateException("Invalid cell type for string: " + cell.getCellType());
		}
	}

}
