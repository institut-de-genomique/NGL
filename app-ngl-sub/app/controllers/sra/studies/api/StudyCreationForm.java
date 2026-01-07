package controllers.sra.studies.api;

import java.util.List;

import views.components.datatable.DatatableForm;


public class StudyCreationForm  extends DatatableForm {
	public List<String>     projectCodes;
	public String           existingStudyType;
	public String           centerProjectName;
	public String           title;
	public String 		    description;                   // meme nom que dans les services .js
    public String           strTaxonId; 
    public String           strIdsPubmeds;                 // meme nom que dans les services .js
    public String           strLocusTagPrefixs;            // meme nom que dans les services .js
    public String           strParentProjectAccessions;
    // NGL-4708
    public String           strAttributes;    
    public String           dropBox;

}
