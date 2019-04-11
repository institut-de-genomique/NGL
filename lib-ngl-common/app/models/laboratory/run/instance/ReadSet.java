package models.laboratory.run.instance;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.cea.ig.DBObject;
import models.laboratory.common.description.Level;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.common.instance.Valuation;
import models.laboratory.project.instance.Project;
import models.laboratory.run.description.ReadSetType;
import models.utils.InstanceConstants;
import validation.ContextValidation;
import validation.IValidation;
import validation.run.instance.FileValidationHelper;
import validation.run.instance.ReadSetValidationHelper;
import validation.run.instance.TreatmentValidationHelper;
import validation.utils.ValidationHelper;

public class ReadSet extends DBObject implements IValidation {

	/**
	 * Read set type code (required {@link #validate(ContextValidation)}, references {@link ReadSetType}).
	 */
	public String typeCode;
	
	public State state;
	
	public String runCode;
	public String runTypeCode;
	public Date runSequencingStartDate;
	public Integer laneNumber;
	
	public Boolean dispatch = Boolean.FALSE;
	public String sampleCode; //nom de l'ind / ech //used for search
	
	/**
	 * Project code (required {@link #validate(ContextValidation)}, references {@link Project}).
	 */
	public String projectCode;
	
	public Valuation productionValuation    = new Valuation(); // TODO: rename to QCValuation
	public Valuation bioinformaticValuation = new Valuation(); // TODO: rename to bioinformaticUsable
	
    public String location; // CCRT or CNS or CNG
    public State submissionState; 
    public String path;
	public String archiveId;
	public Date archiveDate;
	public TraceInformation traceInformation;
	public Map<String, Treatment> treatments = new HashMap<>();
	public Map<String, PropertyValue> properties = new HashMap<>(); // <String, PropertyValue>();
	
	public List<File> files;
	
	// insert after ngsrg
	public SampleOnContainer sampleOnContainer;
	public Date releaseDate; 
	public Date submissionDate; 
	
	// TODO BA Ref !!!!
	
	/*
	 * for "archives" optimization purpose (query links)
	 */
	
	/*
	indexSequence 			tag lié au ls
	nbRead 					nombre de read de sequencage du ls
	???						ssid du ls (archivage)
	???						date d'archivage du ls
	nbClusterInternalFilter	nombre de clusters passant les filtres du ls
	nbBaseInternalFilter	nombre de bases correspondant au clusters passant les filtres du ls
	fraction				fraction de run du ls
	insertLength			id de la taille d'insert
	nbUsefulBase				nombre de bases utiles ls
	nbUsefulCluster			nombre de clusters utiles passant les filtres du ls
	q30 					q30 du ls
	score					score qualite moyen du ls
	 */

	/**
	 * Validate a ReadSet (context parameter "external"). 
	 */
	@Override
	public void validate(ContextValidation contextValidation) {
		ReadSetValidationHelper  .validateIdPrimary               (contextValidation, this);
		ReadSetValidationHelper  .validateCodePrimary             (contextValidation, this, InstanceConstants.READSET_ILLUMINA_COLL_NAME);
		ReadSetValidationHelper  .validateReadSetTypeRequired     (contextValidation, typeCode, properties);
		ReadSetValidationHelper  .validateStateRequired           (contextValidation, typeCode, state);
		// TODO: validation runTypeCode et runSequencingStartDate
		// TODO: passage de la mauvaise cle dans le message d'erreur
		ReadSetValidationHelper  .validateValuationRequired       (contextValidation, typeCode, bioinformaticValuation);
		ReadSetValidationHelper  .validateValuationRequired       (contextValidation, typeCode, productionValuation);
		ReadSetValidationHelper  .validateTraceInformationRequired(contextValidation, traceInformation);
		ReadSetValidationHelper  .validateReadSetRunCodeRequired  (contextValidation, runCode);
		
		if ("default-readset".equals(typeCode) || "rsillumina".equals(typeCode)) {
			ReadSetValidationHelper.validateReadSetLaneNumber   (runCode, laneNumber ,contextValidation);
			ReadSetValidationHelper.validateReadSetCodeInRunLane(code, runCode, laneNumber, contextValidation);			
		}
		ReadSetValidationHelper  .validateProjectCodeRequired     (contextValidation, projectCode);
		ReadSetValidationHelper  .validateSampleCodeRequired      (contextValidation, sampleCode, projectCode);
		ValidationHelper         .validateNotEmpty                (contextValidation, path,     "path");
		ValidationHelper         .validateNotEmpty                (contextValidation, location, "location");
		
		contextValidation.putObject("readSet",     this);
		contextValidation.putObject("objectClass", getClass());
		contextValidation.putObject("level",       Level.CODE.ReadSet);
//		TreatmentValidationHelper.validationTreatments            (contextValidation, treatments);
		TreatmentValidationHelper.validateTreatments            (contextValidation, treatments, this);
//		FileValidationHelper     .validateFiles                   (contextValidation, files);
		FileValidationHelper     .validateFiles                   (contextValidation, this, files);
//		if (contextValidation.getContextObjects().containsKey("external") && (Boolean)contextValidation.getContextObjects().get("external")) {
		if (contextValidation.containsKey("external") && contextValidation.<Boolean>getTypedObject("external")) {
			ReadSetValidationHelper.validateSampleOnContainer(sampleOnContainer, contextValidation);
		}
	}
	
}
