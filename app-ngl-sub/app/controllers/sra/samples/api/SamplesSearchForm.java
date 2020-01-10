package controllers.sra.samples.api;

import java.util.ArrayList;
import java.util.List;

import controllers.ListForm;

public class SamplesSearchForm extends ListForm {
	//public String projCode;
	public List<String> projCodes = new ArrayList<>();
	//public List<String> listSampleCodes; // remplacé par codes.
	public List<String> stateCodes = new ArrayList<>();
	public String stateCode = null;
	public List<String> accessions = new ArrayList<>();
	public String accessionRegex;
	public List<String> codes = new ArrayList<>();
	public String codeRegex;
	public List<String> externalIds = new ArrayList<>();
	public String externalIdRegex;

}
