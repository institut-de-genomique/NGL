package controllers.sra.submissions.api;

import java.util.List;

import views.components.datatable.DatatableForm;

public class SubmissionsCreationFormForUmbrella  extends DatatableForm {
	public String 		    title;                     // meme nom que dans les services .js
	public String           description;               // meme nom que dans les services .js
    public List<String>     childrenProjectAccessions; // meme nom que dans les services .js
    public List<String>     idsPubmed;                 // meme nom que dans les services .js
    public String           strTaxonId;                    
    public String submissionProjectType;
}
