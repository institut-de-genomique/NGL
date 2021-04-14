package controllers.commons.api;

import java.util.Date;
//import java.util.List;
import java.util.Set;

import controllers.ListForm;

public class ParametersSearchForm extends ListForm{
	
	public String typeCode; 
	public Set<String> typeCodes;     // drop down multiple
	public Set<String> categoryCodes; //drop down multiple
	public String categoryCode;
	
	/* FDS/EJ 27/11/2018 Il faudra creer une ressource "Index/Tag" distincte des parametres avec sa propre collection Mongo...
	   FDS NGL-836: Pour l'instant completer Parameter 
	    note: dans SampleSearchForm il y a les variable ET la methode public DBQuery.Query getQuery() pourquoi pas ici ???? */
	
	public Set<String> codes; //textarea
	public String codeRegex;  //regex
	
	public Set<String> names; //textarea
	public String nameRegex;  //regex
	
	public Set<String> shortNames; //textarea
	public String shortNameRegex;  //regex
	
	public Set<String> groupNames;  // drop down multiple
	public String supplierNameRegex;//regex
	
	public String sequence;
	public Set<String> sequences;  //textarea
	public String sequenceRegex;   //regex
	//public Integer size;           //input   ne filtre rien...
	public String size;
	
	public Date fromDate;     //input
	public Date toDate;       //input
	public String createUser; //input avec liste

}
