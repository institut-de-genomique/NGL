package controllers.balancesheets.api;

import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Strings;

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
		if(Strings.isNullOrEmpty(code)) {
			if(Strings.isNullOrEmpty(year)) {
				if(Strings.isNullOrEmpty(type)) {
					return DBQuery.empty();
				} else {
					return DBQuery.is(TYPE_FIELD, type);
				}
			} else if(Strings.isNullOrEmpty(type)) {
				return DBQuery.is(YEAR_FIELD, year);
			} else {
				return DBQuery.and(DBQuery.is(YEAR_FIELD, year), DBQuery.is(TYPE_FIELD, type));
			}
		} else {
			return DBQuery.is(CODE_FIELD, code);
		}
		
	}

}
