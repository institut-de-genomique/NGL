package controllers.sra.studies.api;

import java.util.ArrayList;
import java.util.List;

import controllers.ListForm;

public class StudiesSearchForm extends ListForm {

	//public String projCode;
	public List<String> projCodes   = new ArrayList<>();
	public List<String> stateCodes  = new ArrayList<>();
	public Boolean confidential     = null;
	public String stateCode         = null;
	public List<String> accessions  = new ArrayList<>();
	public String accessionRegex;
	public List<String> codes       = new ArrayList<>();
	public String codeRegex;
	public List<String> externalIds = new ArrayList<>();
	public String externalIdRegex;

}
