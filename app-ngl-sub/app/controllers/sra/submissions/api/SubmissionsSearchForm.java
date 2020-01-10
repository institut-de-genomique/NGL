package controllers.sra.submissions.api;

import java.util.List;

import controllers.ListForm;

public class SubmissionsSearchForm extends ListForm{
	public List<String> projCodes; // meme nom que dans la vue et les services .js
	public String stateCode;
	public List<String> stateCodes;
	public List<String> accessions;
	public String accessionRegex;
	public List<String> codes;
	public String codeRegex;
}
