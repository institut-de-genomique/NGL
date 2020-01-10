package controllers.instruments.api;

import controllers.ListForm;

public class InstrumentCategoriesSearchForm extends ListForm{
	public String instrumentTypeCode;

	@Override
	public String toString() {
		return "InstrumentCategoriesSearchForm [typeCode=" + instrumentTypeCode + "]";
	}
}
