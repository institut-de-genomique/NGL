package controllers.treatmenttypes.api;

import models.laboratory.common.description.Level;

import java.util.List;

import controllers.ListForm;

public class TreatmentTypesSearchForm extends ListForm{
	
	public Level.CODE levels;

	public String code;
	public List<String> codes;
	
	public String name;
	public List<String> names;
	
	//NGL-3530 filtrer les treatments sur les categories !
	public List<String> categoryNames;
}
