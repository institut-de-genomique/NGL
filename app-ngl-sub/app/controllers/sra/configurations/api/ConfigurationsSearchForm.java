package controllers.sra.configurations.api;

import java.util.List;

import controllers.ListForm;

public class ConfigurationsSearchForm extends ListForm {
	public List<String> projCodes; // meme nom que dans la vue et les services .js
	public List<String> stateCodes;
	public String stateCode = null;
	public List<String> codes;
	public String codeRegex;
}
