package services;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Classe permettant de stoquer le resultat du parsing du fichier retour de l'EBI en reponse à
 * l'envoie d'une soumission pour creation de données ou d'une soumission pour update de données.
 * @author sgas
 *
 */
class EbiRetour {
	protected String nameFileInput = "";
	protected Map<String, String> mapSamples      = new HashMap<>(); // String, String>(); 
	protected Map<String, String> mapExtIdSamples = new HashMap<>(); // String, String>(); 
	protected Map<String, String> mapExperiments  = new HashMap<>(); // <String, String>(); 
	protected Map<String, String> mapRuns         = new HashMap<>(); // String, String>(); 
	protected String submissionCode = "";
	protected String submissionAc   = "";
	protected Date submissionDate   = null;
	protected String stringSubmissionDate = "";
	protected String studyCode      = "";
	protected String studyAc        = "";
	protected String studyExtId     = "";
	protected String analysisCode   = "";
	protected String analysisAc     = "";
	protected String projectCode    = "";
	protected String projectAc      = "";
	protected String projectExtId   = "";
	protected String message        = "";
	
//	public EbiRetour(File ebiFile) {
//		nameFileInput = ebiFile.getPath() + File.separator + ebiFile.getName();
//	}
	
	public EbiRetour(File ebiFile) {
		nameFileInput = ebiFile.getPath();
	}
	public Date getSubmissionDate() {
		return submissionDate;
	}
	public String getStringSubmissionDate() {
		return stringSubmissionDate;
	}
	public String getCodeSubmission() {
		return submissionCode;
	}
	public String getAccessionSubmission() {
		return submissionAc;
	}
	public String getCodeStudy() {
		return studyCode;
	}
	public String getAccessionStudy() {
		return studyAc;
	}
	public String getCodeAnalysis() {
		return analysisCode;
	}
	public String getAccessionAnalysis() {
		return analysisAc;
	}
	public String getExtIdStudy() {
		return studyExtId;
	}
	public String getCodeProject() {
		return projectCode;
	}
	public String getAccessionProject() {
		return projectAc;
	}	
	public String getExtIdProject() {
		return projectExtId;
	}
	public String getCodeSample(String accession) {
		return this.mapSamples.get(accession);
	}
	public String getAccessionSample(String code) {
		return this.mapSamples.get(code);
	}
	public String getExtIdSample(String code) {
		return this.mapExtIdSamples.get(code);
	}
	public String getCodeExperiment(String accession) {
		return this.mapExperiments.get(accession);
	}
	public String getAccessionExperiment(String code) {
		return this.mapExperiments.get(code);
	}
	public String getCodeRun(String accession) {
		return this.mapRuns.get(accession);
	}	
	public String getAccessionRun(String code) {
		return this.mapRuns.get(code);
	}
	public String getNameFileInput() {
		return nameFileInput;
	}			
}