package controllers.reagents.io.receptions.mappers.row;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import com.google.common.collect.Iterators;

import controllers.reagents.io.receptions.mappers.MapperHelper;
import controllers.reagents.io.receptions.mappers.cell.StringCellMapper;

public interface CellFinder extends StringCellMapper {
	
	public default Cell findCell(Iterator<Cell> cells, int columnIndex) throws NoSuchElementException {
		return Iterators.find(cells, (Cell cell) -> cell.getColumnIndex() == columnIndex);
	}
	
	public default Cell getCell(Row row, int columnIndex) {
		try {
			Iterator<Cell> cells = row.cellIterator();
			return findCell(cells, columnIndex);
		} catch(final NoSuchElementException e) {
			return null; // no value found
		}
	}
	
	public default boolean isValid(Cell cell) {
		return !(cell == null || MapperHelper.shouldPass(cell));
	}
	
	public default String getCellValue(Row row, int columnIndex) {
		Cell cell = getCell(row, columnIndex);
		return isValid(cell) ? getString(cell) : null;
	}

}
