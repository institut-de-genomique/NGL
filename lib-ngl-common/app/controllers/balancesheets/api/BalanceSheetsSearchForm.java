package controllers.balancesheets.api;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;

import com.fasterxml.jackson.annotation.JsonIgnore;

import controllers.DBObjectListForm;
import models.laboratory.balancesheet.instance.BalanceSheet;

public class BalanceSheetsSearchForm extends DBObjectListForm<BalanceSheet> {
	
	private static final String CODE_FIELD = "code";
	private static final String YEAR_FIELD = "year";
	private static final String TYPE_FIELD = "type";
	
	public String code;
	public String year;
	public String type;

	@Override
	@JsonIgnore
	public Query getQuery() {
		if(StringUtils.isNotBlank(code)) {
			return DBQuery.is(CODE_FIELD, code);
		} else {
			List<Query> ands = new ArrayList<>(2);
			if(StringUtils.isNotBlank(year)) ands.add(DBQuery.is(YEAR_FIELD, year));
			if(StringUtils.isNotBlank(type)) ands.add(DBQuery.is(TYPE_FIELD, type));
			return DBQuery.and(ands.stream().toArray(Query[]::new));
		}
	}

}
