package controllers.sra.experiments.api;

import controllers.sra.api.SraSearchForm;

// heritage de SraSearchForm plutot que ListForm pour pouvoir utiliser 
// methode replacePseudoStateCodesToStateCodesInFormulaire 
public class UserFileExperimentsForm  extends SraSearchForm { 
	public String editableCreateUser; 
	public String editableStateCode;
	public String base64UserFileExperiment;
	public String userFileExperiment;
	
}