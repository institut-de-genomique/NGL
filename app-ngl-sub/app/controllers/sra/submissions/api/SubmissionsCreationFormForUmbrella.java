package controllers.sra.submissions.api;

import java.util.List;

import models.sra.submit.sra.instance.Attribute;
import views.components.datatable.DatatableForm;

public class SubmissionsCreationFormForUmbrella  extends DatatableForm {
	public String 		    title;                     // meme nom que dans les services .js
	public String           description;               // meme nom que dans les services .js
    public List<String>     childrenProjectAccessions; // meme nom que dans les services .js
    public List<String>     idsPubmed;                 // meme nom que dans les services .js
    public String           taxonId; 
    public String           dropBox;
    public  List<String>    parentProjectAccessions;    // meme nom que dans les services .js
    // NGL-4708
    //public List<Attribute>  attributes;                 
    public String           attributes;                 
    public String           submissionProjectType;
}
