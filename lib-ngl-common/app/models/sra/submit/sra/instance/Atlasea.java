package models.sra.submit.sra.instance;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import fr.cea.ig.DBObject;
import models.laboratory.common.instance.Comment;
import models.laboratory.common.instance.TraceInformation;

public class Atlasea  extends DBObject {
	public String  code;             // code de l'analyse    
	public String  tolId;	    
	public String  taxonId;
	public Date    date;
	public String  scientificName;
	public String  umbrellaCode;
	public String  umbrellaAc;                   
	public String  umbrellaSubmissionCode;       // code de la soumission pour la creation de l'umbrella
	public String  umbrellaSubmissionAc;         // AC de la soumission pour la creation de l'umbrella
	public String  umbrellaUpdateSubmissionCode; // code de la soumission pour l'update de l'umbrella (on ne recoit pas d'AC pour une soumission de type update)
	public String  sequencingProjectCode;
	public String  sequencingProjectAc;
	public String  sequencingSubmissionCode;
	public String  sequencingSubmissionAc;
	public String  primaryAssemblyProjectCode;
	public String  primaryAssemblyProjectAc;
	public String  primaryAssemblySubmissionCode;
	public String  primaryAssemblySubmissionAc;
	public boolean endSubmitProjects;
	public List<OldTolid> oldTolids;
    public List<Comment>    comments         = new ArrayList<Comment>();// commentaires des utilisateurs.
	public TraceInformation traceInformation = new TraceInformation();

	
	
    public Atlasea() {
	}
    
    public Atlasea(String analyseCode, String tolId, String taxonId, String scientificName) {
    	this.code              = analyseCode;
		this.tolId             = tolId;
		this.taxonId           = taxonId;
		this.scientificName    = scientificName;
		this.endSubmitProjects = false;
	}
	
    
		
}
