package controllers.sra.projects.api;

import java.util.ArrayList;
import java.util.List;

import controllers.ListForm;
import controllers.sra.api.SraSearchForm;
import models.sra.submit.util.VariableSRA;

public class ProjectsSearchForm extends SraSearchForm {

	//public String projCode;
	public List<String> projCodes   = new ArrayList<>();
	public List<String> stateCodes  = new ArrayList<>();
	public Boolean confidential     = null;
	public String stateCode         = null;
	public String accession         = null;
	public List<String> accessions  = new ArrayList<>();
	public String accessionRegex;
	public List<String> codes       = new ArrayList<>();
	public String codeRegex;
	public String externalId        = null;
	public List<String> externalIds = new ArrayList<>();
	public String externalIdRegex;
	public String createUser;      // pour recherche pour un proprietaire (update-ctrl.js)

}
