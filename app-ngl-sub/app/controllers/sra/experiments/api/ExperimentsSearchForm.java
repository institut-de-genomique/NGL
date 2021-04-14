package controllers.sra.experiments.api;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import controllers.ListForm;
import controllers.sra.api.SraSearchForm;
import play.data.validation.ValidationError;

// heritage de SraSearchForm plutot que ListForm pour pouvoir utiliser 
// methode replacePseudoStateCodesToStateCodesInFormulaire 
public class ExperimentsSearchForm  extends SraSearchForm { 
	public String code;
	public List<String> codes;    
	public String codeRegex;
	public String submissionCode;
	public String studyCode;       // ajout pour interface release et update-ctrl.js    
	public String sampleCode;      // ajout pour update-ctrl.js     
	public String runCode;
	public List<String> projCodes = new ArrayList<>();
	public String stateCode;
	public List<String> stateCodes = new ArrayList<>();
	public List<String> pseudoStateCodes = new ArrayList<>();
	public String accession;
	public List<String> accessions = new ArrayList<>();
	public String accessionRegex;
	public String studyAccession;  // ajout pour interface bilan et update-ctrl.js
	public String sampleAccession; // ajout pour interface bilan et update-ctrl.js
	public String createUser;      // pour recherche pour un proprietaire (update-ctrl.js)
	
}