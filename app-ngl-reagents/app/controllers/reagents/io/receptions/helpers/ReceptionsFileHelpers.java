package controllers.reagents.io.receptions.helpers;

import static controllers.reagents.io.receptions.mappers.MapperHelper.shouldPass;

import java.util.Iterator;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import controllers.reagents.io.receptions.mappers.row.RequiredPropertiesRowMapper.RequiredProperties;

public final class ReceptionsFileHelpers {
	
	private ReceptionsFileHelpers() {}
	
	public static boolean isEmpty(Row row) {
		final Iterator<Cell> cells = row.cellIterator();
		while (cells.hasNext()) {
			Cell nextCell = cells.next();
			if(!shouldPass(nextCell)) {
				return false;
			}
		} return true;
	}
	
	public static int getIndex(Row row) {
		return row.getRowNum() + 1;
	}
	
	public static boolean isValid(RequiredProperties requiredProperties) {
		return StringUtils.isNoneBlank(
				requiredProperties.getProviderCode(), 
				requiredProperties.getKitCatalogName(), 
				requiredProperties.getCatalogRefCode());
	}

}
