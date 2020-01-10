package controllers.sra.submissions.api;

import java.util.List;

import views.components.datatable.DatatableForm;

// classe utilisee pour la recuperation des variables du formulaire submissions/create.scala.html
// attention à mettre les memes noms ici et dans le formulaire create.scala.html
public class SubmissionsCreationForm  extends DatatableForm {
	public List<String> projCodes; // meme nom que dans la vue (view) et les services .js
	public String studyCode;
	public String configurationCode;
	public List<String> readSetCodes;
	public String base64UserFileClonesToAc;
	public String base64UserFileExperiments;
	public String base64UserFileSamples;
	public String base64UserFileReadSet;
	public String acStudy;
	public String acSample;

}
