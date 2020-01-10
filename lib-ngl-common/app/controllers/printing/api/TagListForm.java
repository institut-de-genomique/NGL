package controllers.printing.api;

import java.util.List;

public class TagListForm {

	public String experimentCode;
	
	public List<String> containerSupportCodes;

	@Override
	public String toString() {
		return "TagListForm [experimentCode=" + experimentCode
				+ ", containerSupportCodes=" + containerSupportCodes + "]";
	}


}
