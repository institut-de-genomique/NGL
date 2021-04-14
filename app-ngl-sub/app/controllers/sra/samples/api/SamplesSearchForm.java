package controllers.sra.samples.api;

import java.util.ArrayList;
import java.util.List;
import controllers.sra.api.SraSearchForm;

public class SamplesSearchForm extends SraSearchForm {
	//public String projCode;
	public List<String> projCodes = new ArrayList<>();
	//public List<String> listSampleCodes; // remplacé par codes.
	public List<String> stateCodes = new ArrayList<>();
	public String stateCode = null;
	public List<String> accessions = new ArrayList<>();
	public String accessionRegex;
	public List<String> codes = new ArrayList<>();
	public String codeRegex;
	public String externalId        = null;
	public List<String> externalIds = new ArrayList<>();
	public String externalIdRegex;
	public String type;            // si on veut selectionner Sample ou ExternalSample
	public String createUser;      // pour recherche pour un proprietaire (update-ctrl.js)
	//public String base64UserFileSample;
}
