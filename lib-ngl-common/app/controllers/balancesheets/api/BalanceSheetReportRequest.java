package controllers.balancesheets.api;

import java.util.List;

public class BalanceSheetReportRequest {
	
	public String title;
	
	public List<BSRSheet> sheets;
	
	/**
	 * BalanceSheetReport - Sheet
	 * @author aprotat
	 *
	 */
	public static class BSRSheet {
		public String name;
		public List<BSRTable> tables;
	}
	
	/**
	 * BalanceSheetReport - Table
	 * @author aprotat
	 *
	 */
	public static class BSRTable {
		public List<BSRRow> rows;
	}
	
	/**
	 * BalanceSheetReport - Row
	 * @author aprotat
	 *
	 */
	public static class BSRRow {
		public List<String> values;
	}

}
