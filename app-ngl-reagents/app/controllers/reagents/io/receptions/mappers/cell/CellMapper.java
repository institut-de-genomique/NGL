package controllers.reagents.io.receptions.mappers.cell;

import org.apache.poi.ss.usermodel.Cell;

import models.laboratory.reagent.instance.AbstractReception;

public interface CellMapper extends DateCellMapper, StringCellMapper {
	
	public void mapCell(AbstractReception reception, Cell cell, String user);

}
