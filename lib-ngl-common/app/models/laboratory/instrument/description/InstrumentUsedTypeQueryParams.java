package models.laboratory.instrument.description;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

public class InstrumentUsedTypeQueryParams {
	
	public String code;
	public List<String> codes;
	
	public String categoryCode;
	public List<String> categoryCodes;
	
	private boolean isStringValid(String value) {
		return StringUtils.isNotBlank(value);
	}
	
	private boolean isListValid(List<?> list) {
		return CollectionUtils.isNotEmpty(list);
	}
	
	public boolean isSingleCode() {
		return this.isStringValid(code);
	}
	
	public boolean isCodeList() {
		return this.isListValid(codes);
	}
	
	public boolean isSingleCategoryCode() {
		return this.isStringValid(categoryCode);
	}
	
	public boolean isCategoryCodeList() {
		return this.isListValid(categoryCodes);
	}
	
	public boolean isAtLeastOneParam(){
		return isSingleCode() || isCodeList() || isSingleCategoryCode() || isCategoryCodeList();
	}

}
