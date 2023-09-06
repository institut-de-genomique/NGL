package controllers.reagents.io.receptions.mappers;

import org.apache.poi.ss.usermodel.Cell;

public final class MapperHelper {
	
	private MapperHelper() {}
	
	public static boolean shouldPass(Cell cell) {
		final int type = cell.getCellType();
		return type == Cell.CELL_TYPE_BLANK || type == Cell.CELL_TYPE_ERROR || type == Cell.CELL_TYPE_BOOLEAN
				|| type == Cell.CELL_TYPE_FORMULA;
	}
	
	public static int getColumnIndex(Cell cell) {
		return cell.getColumnIndex() + 1;
	}

}
