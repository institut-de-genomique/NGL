package controllers.balancesheets.api;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.inject.Inject;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.drools.core.util.StringUtils;

import akka.stream.javadsl.Source;
import akka.util.ByteString;
import controllers.NGLAPIController;
import controllers.balancesheets.api.BalanceSheetReportRequest.BSRRow;
import controllers.balancesheets.api.BalanceSheetReportRequest.BSRSheet;
import controllers.balancesheets.api.BalanceSheetReportRequest.BSRTable;
import fr.cea.ig.authentication.Authenticated;
import fr.cea.ig.authorization.Authorized;
import fr.cea.ig.lfw.utils.Streamer;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.balancesheets.BalanceSheetsAPI;
import fr.cea.ig.ngl.dao.balancesheets.BalanceSheetsDAO;
import fr.cea.ig.ngl.support.ListFormWrapper;
import models.laboratory.balancesheet.instance.BalanceSheet;
import play.data.Form;
import play.mvc.Result;

public class BalanceSheets extends NGLAPIController<BalanceSheetsAPI, BalanceSheetsDAO, BalanceSheet>{
	
	private static final String IS_TOTAL_TABLE_REGEX = "Somme .*:";

	private final Form<BalanceSheet> balanceSheetForm;
	private final Form<BalanceSheetReportRequest> balanceSheetTableForm;

	@Inject
	public BalanceSheets(NGLApplication app, BalanceSheetsAPI api) {
		super(app, api, BalanceSheetsSearchForm.class);
		this.balanceSheetForm = app.formFactory().form(BalanceSheet.class);
		this.balanceSheetTableForm = app.formFactory().form(BalanceSheetReportRequest.class);
	}	
	
	@Override
	@Authenticated
    @Authorized.Read
    public Result list() {
		return globalExceptionHandler(() -> {
            try {
                Source<ByteString, ?> resultsAsStream = api().list(new ListFormWrapper<>(objectFromRequestQueryString(this.searchFormClass), form -> generateBasicDBObjectFromKeys(form)));
                return Streamer.okStream(resultsAsStream);
            } catch (APIException e) {
                getLogger().error(e.getMessage(), e);
                return badRequestAsJson(e.getMessage());
            }
        });
    }

	@Override
	public BalanceSheet saveImpl() throws APIException {
		BalanceSheet bs = getFilledForm(balanceSheetForm, BalanceSheet.class).get();
		return api().create(bs, getCurrentUser());
	}

	@Override
	public BalanceSheet updateImpl(String code) throws Exception, APIException, APIValidationException {
		BalanceSheet bs = getFilledForm(balanceSheetForm, BalanceSheet.class).get();
		return api().update(bs, getCurrentUser());
	}
	
	/**
	 * Compute the width needed for each columns, depending of theirs values lengths.
	 * @param sheet
	 * @return positive int array
	 */
	private int[] getColumnsWidths(BSRSheet sheet) {
		int numberColumns = sheet.tables.stream()
		.mapToInt((BSRTable table) -> {
			// get first row number values (columns)
			return table.rows.get(0).values.size();
		})
		.max().getAsInt();
		int[] columnWidths = new int[numberColumns];
		sheet.tables.forEach(table -> {
			table.rows.forEach(row -> {
				for(int i=0; i<row.values.size(); i++) {
					// length of the string
					int valueLength = row.values.get(i).length();
					// replace if bigger word
					if(columnWidths[i] < valueLength) {
						columnWidths[i] = valueLength;
					}
				}
			});
		}); return columnWidths;
	}
	
	/**
	 * Should table be considered as a 'total' table.
	 * @param table a BalanceSheetReportRequest's table
	 * @return boolean
	 */
	private boolean isTotalTable(BSRTable table) {
		return table.rows.stream()
		// get first value of each row
		.map((BSRRow row) -> row.values.get(0))
		// value match regex
		.anyMatch((String value) -> value.matches(IS_TOTAL_TABLE_REGEX));
	}
	
	/**
	 * Apply correct style depending on cell coordinates.
	 * @param cell the cell to apply style
	 * @param wb the XLSX workbook
	 * @param rowIndex cell's row index in current table
	 * @param isTotalTable should table be considered as 'total' table
	 * @param cellIndex cell's column index in current row
	 * @param nCells columns (cells) number in row
	 */
	private void setCellStyle(Cell cell, Workbook wb, int rowIndex, boolean isTotalTable, int cellIndex, int nCells) {
		CellStyle style = wb.createCellStyle();
		style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
		style.setBorderTop(HSSFCellStyle.BORDER_THIN);
		if(isTotalTable) { // 'total' datatable
			Font font = wb.createFont();
			font.setBoldweight(Font.BOLDWEIGHT_BOLD);
			style.setFont(font);
			if(cellIndex == 0) { // first cell
				style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
			} else if(cellIndex == nCells - 1) { // last cell
				style.setBorderRight(HSSFCellStyle.BORDER_THIN);
			}
		} else { // datatable (with headers)
			style.setBorderRight(HSSFCellStyle.BORDER_THIN);
			style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
			if(rowIndex == 0) { // header
				style.setFillForegroundColor(IndexedColors.PALE_BLUE.index);
				style.setFillPattern(CellStyle.SOLID_FOREGROUND);
			}
		} cell.setCellStyle(style);
	}
	
	/**
	 * Set cell value. Try to convert value string into number at first.
	 * @param cell the cell to fill with value
	 * @param value a string representation, could be number or text
	 */
	private void setCellValue(Cell cell, String value) {
		try {
			// try to parse value as number
			long longValue = Long.parseLong(
					// remove all blank characters
					value.replaceAll("[\\s|\\u00A0]+", StringUtils.EMPTY));
			cell.setCellType(Cell.CELL_TYPE_NUMERIC);
			cell.setCellValue(longValue);
		} catch(NumberFormatException e) {
			// else write value as string
			cell.setCellType(Cell.CELL_TYPE_STRING);
			cell.setCellValue(value);
		}
	}
	
	private void addTitleToSheet(Sheet sheet, String title) {
		Row firstRow = sheet.createRow(0);
		Cell firstCell = firstRow.createCell(1);
		this.setCellValue(firstCell, title);
	}
	

	/**
	 * Build XLSX workbook from report request.
	 * @param bsrr the request
	 * @return workbook
	 */
	private Workbook buildXlsxReport(BalanceSheetReportRequest bsrr) {
		Workbook wb = new XSSFWorkbook();

		for(BSRSheet bsrrSheet : bsrr.sheets) {
			Sheet sheet = wb.createSheet(bsrrSheet.name);
			addTitleToSheet(sheet, bsrr.title);
			// global row index in sheet
			int globalRowIndex = 2;
			// write each table of the same sheet
			for(BSRTable bsrrTable : bsrrSheet.tables) {
				// local row index in table
				int localRowIndex = 0;
				// should table be considered as a 'total' table
				boolean isTotalTable = this.isTotalTable(bsrrTable);
				// write each table's row
				for(BSRRow bsrrRow : bsrrTable.rows) {;
					Row row = sheet.createRow(globalRowIndex);
					int cellIndex = 0;
					// write cells
					for(String value : bsrrRow.values) {
						Cell cell = row.createCell(cellIndex);
						this.setCellStyle(cell, wb, localRowIndex, isTotalTable, cellIndex, bsrrRow.values.size());
						this.setCellValue(cell, value);
						cellIndex++;
					}
					localRowIndex++;
					globalRowIndex++;
				}
				// add an empty row between each tables
				globalRowIndex++;
			}
			// apply columns width
			int[] columnWidths = this.getColumnsWidths(bsrrSheet);
			for(int c=0; c < columnWidths.length; c++) {
				sheet.setColumnWidth(c, columnWidths[c] * 256);
			}
		} return wb;
	}
	
	/**
	 * Get XLSX blob from balancesheet tables (from POST body).
	 * @return response with XLSX blob
	 * @throws APIException on blob generation error
	 */
	public Result excelReport() throws APIException {
		try {
			BalanceSheetReportRequest bsrr = getFilledForm(balanceSheetTableForm, BalanceSheetReportRequest.class).get();
			// generate xlsx data
			Workbook wb = buildXlsxReport(bsrr);
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			// write data in byte array
			wb.write(byteArrayOutputStream);
			byte[] bytes = byteArrayOutputStream.toByteArray();
			// create inputStream to read from byte array
			ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
			return ok(byteArrayInputStream).as("application/x-download");
		} catch (IOException e) {
			throw new APIException("Error while generating XLSX document.", e);
		}
	}

}
