package controllers.reagents.io.receptions.mappers;

import static controllers.reagents.io.receptions.mappers.MapperHelper.shouldPass;
import static controllers.reagents.io.receptions.mappers.MapperHelper.getColumnIndex;

import java.util.Iterator;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import controllers.reagents.io.receptions.mappers.cell.CellMapper;
import controllers.reagents.io.receptions.mappers.row.RequiredPropertiesRowMapper;
import models.laboratory.reagent.instance.AbstractReception;
import validation.ContextValidation;

public abstract class AbstractReceptionMapper implements CellMapper, ReceptionTypeMapper, RequiredPropertiesRowMapper {
	
	protected final ContextValidation contextValidation;
	
	public AbstractReceptionMapper(ContextValidation contextValidation) {
		this.contextValidation = contextValidation;
	}
	
	private boolean processCell(AbstractReception reception, Cell cell, String user) {
		if (shouldPass(cell)) {
			return false;
		}
		this.mapCell(reception, cell, user);
		return true;
	}
	
	private boolean handleCellProcessing(AbstractReception reception, Cell cell, String user) {
		try {
			return processCell(reception, cell, user);
		} catch (IllegalStateException|IllegalArgumentException e) {
			this.contextValidation.addError("Erreur Parsing :", e.getMessage());
			return false;
		}
	}
	
	private boolean handle(AbstractReception reception, Cell cell, String user) {
		int columnIndex = getColumnIndex(cell);
		String columnKey = "Colonne " + columnIndex;
		contextValidation.addKeyToRootKeyName(columnKey);
		boolean isProcessedCell = handleCellProcessing(reception, cell, user);
		contextValidation.removeKeyFromRootKeyName(columnKey);
		return isProcessedCell;
	}
	
	private void throwIfNonProcessedRow(boolean isRowProcessed) {
		if(!isRowProcessed) {
			throw new IllegalStateException("Empty row should not be processed.");
		}
	}

	/**
	 * Fill reception with mapped values (Assuming the row is not empty).
	 * 
	 * @param row
	 * @param reception
	 * @param user
	 */
	public void mapRow(Row row, AbstractReception reception, String user) {
		final Iterator<Cell> cells = row.cellIterator();
		boolean isRowProcessed = false;
		while (cells.hasNext()) {
			final Cell cell = cells.next();
			isRowProcessed = handle(reception, cell, user) || isRowProcessed;
		}
		throwIfNonProcessedRow(isRowProcessed);
	}	

}
