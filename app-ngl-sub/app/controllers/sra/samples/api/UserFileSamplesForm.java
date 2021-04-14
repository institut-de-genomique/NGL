package controllers.sra.samples.api;

import controllers.sra.api.SraSearchForm;

// heritage de SraSearchForm plutot que ListForm pour pouvoir utiliser 
// methode replacePseudoStateCodesToStateCodesInFormulaire 
public class UserFileSamplesForm  extends SraSearchForm { 
	public String editableCreateUser; 
	public String editableStateCode;
	public final String editableType = "Sample"; // important de ne pas proposer ExternalSample pour update 

	public String base64UserFileSample;
	public String userFileSample;
	
}