package controllers.sra.samples.api;

import java.util.List;

import views.components.datatable.DatatableForm;

// classe utilisee pour la recuperation des variables du formulaire samples/create.scala.html
// attention à mettre les memes noms ici et dans le formulaire create.scala.html
public class SamplesCreationForm  extends DatatableForm {
	public List<String> projCode; // meme nom que dans la vue (view) et les services .js
	public int taxonId;
	public String refCollab;
	public String title;
	public String description;
	public String anonymizedName;
	public String attributes;
}
