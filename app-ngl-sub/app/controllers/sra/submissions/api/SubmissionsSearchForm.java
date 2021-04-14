package controllers.sra.submissions.api;

import java.util.List;

import controllers.ListForm;
import controllers.sra.api.SraSearchForm;

public class SubmissionsSearchForm extends SraSearchForm {
	public List<String> projCodes; // meme nom que dans la vue et les services .js
	public String stateCode;
	public List<String> stateCodes;
	public List<String> accessions;
	public String accessionRegex;
	public List<String> codes;
	public String codeRegex;
	public String createUser;      // pour recherche pour un proprietaire (update-ctrl.js)
	public String type;
}
