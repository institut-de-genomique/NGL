package controllers.reagents.io;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Iterator;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;

import models.laboratory.reagent.instance.AbstractReception;

public abstract class AbstractReceptionMapper {

	static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	public boolean passCell(Cell cell) {
		final int type = cell.getCellType();
		return type == Cell.CELL_TYPE_BLANK || type == Cell.CELL_TYPE_ERROR || type == Cell.CELL_TYPE_BOOLEAN
				|| type == Cell.CELL_TYPE_FORMULA;
	}

	public Date getDate(Cell cell) {
		switch (cell.getCellType()) {
		case Cell.CELL_TYPE_STRING:
			return Date.from(Instant.from(LocalDate.parse(cell.getStringCellValue(), DATE_FORMATTER)));
		case Cell.CELL_TYPE_NUMERIC:
			return cell.getDateCellValue();
		default:
			throw new IllegalStateException("Invalid cell type for date: " + cell.getCellType());
		}
	}

	public String getString(Cell cell) {
		switch (cell.getCellType()) {
		case Cell.CELL_TYPE_STRING:
			return cell.getStringCellValue();
		case Cell.CELL_TYPE_NUMERIC:
			// DataFormatter n'est pas threadSafe, il vaut mieux recréer une instance à
			// chaque appel
			return new DataFormatter().formatCellValue(cell);
		default:
			throw new IllegalStateException("Invalid cell type for string: " + cell.getCellType());
		}
	}

	/**
	 * Apply correct type illumina or nanopore
	 *
	 * @return
	 */
	public abstract AbstractReception withCorrectTypes(AbstractReception reception);

	public abstract String getRefCatalog(Row row);

	public abstract String getProvider(Row row);
	
	public abstract String getKitCatalogName(Row row);

	public abstract void mapCell(AbstractReception reception, Cell cell, String user);

	public AbstractReception mapRow(Row row, AbstractReception reception, String user) {
		final Iterator<Cell> cells = row.cellIterator();

		boolean hasBeenModified = false;
		while (cells.hasNext()) {
			final Cell cell = cells.next();
			if (!this.passCell(cell)) {
				try {
					this.mapCell(reception, cell, user);
					hasBeenModified = true;
				} catch (final IllegalStateException e) {
					throw new IllegalStateException(
							"[row:" + cell.getRowIndex() + "|column:" + cell.getColumnIndex() + "]: Invalid cell type.",
							e);
				}
			}
		}
		return hasBeenModified ? reception : null;
	}

}
