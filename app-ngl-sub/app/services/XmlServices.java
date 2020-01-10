package services;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import fr.cea.ig.ngl.dao.api.sra.ExperimentAPI;
import fr.cea.ig.ngl.dao.api.sra.SampleAPI;
import fr.cea.ig.ngl.dao.api.sra.StudyAPI;
import fr.cea.ig.ngl.dao.api.sra.SubmissionAPI;
import models.sra.submit.common.instance.Sample;
import models.sra.submit.common.instance.Study;
import models.sra.submit.common.instance.Submission;
import models.sra.submit.sra.instance.Experiment;
import models.sra.submit.sra.instance.RawData;
import models.sra.submit.sra.instance.ReadSpec;
import models.sra.submit.sra.instance.Run;
import models.sra.submit.util.SraException;
import models.sra.submit.util.VariableSRA;

//import play.Logger;
//class ReadSpec_2 extends ReadSpec {
//	
//}

public class XmlServices {

	private static final play.Logger.ALogger logger = play.Logger.of(XmlServices.class);

	private final SubmissionAPI submissionAPI;
	private final StudyAPI      studyAPI;
	private final ExperimentAPI experimentAPI;
	private final SampleAPI     sampleAPI;
	
	@Inject
	public XmlServices(SubmissionAPI submissionAPI,
				       StudyAPI      studyAPI,
				       SampleAPI     sampleAPI,
				       ExperimentAPI experimentAPI) {
		this.submissionAPI = submissionAPI;
		this.studyAPI      = studyAPI;
		this.sampleAPI     = sampleAPI;
		this.experimentAPI = experimentAPI;
	}
	public Submission writeAllXml(String submissionCode) throws IOException, SraException {
//		Submission submission = MongoDBDAO.findByCode(InstanceConstants.SRA_SUBMISSION_COLL_NAME, models.sra.submit.common.instance.Submission.class, submissionCode);
		Submission submission = submissionAPI.dao_getObject(submissionCode);
		String resultDirectory = submission.submissionDirectory;
//		System.out.println("resultDirectory = " + resultDirectory);
		logger.debug("resultDirectory = " + resultDirectory);
		return writeAllXml(submissionCode, resultDirectory);
	}

	public Submission writeAllXml(String submissionCode, String resultDirectory) throws IOException, SraException {
		System.out.println("creation des fichiers xml pour l'ensemble de la soumission "+ submissionCode);
		System.out.println("resultDirectory = " + resultDirectory);
		// Recuperer l'objet submission:
//		Submission submission = MongoDBDAO.findByCode(InstanceConstants.SRA_SUBMISSION_COLL_NAME, models.sra.submit.common.instance.Submission.class, submissionCode);
		Submission submission = submissionAPI.dao_getObject(submissionCode);
		System.out.println ("Recuperation de la submission" + submission.code);
		// si on est dans soumission de données :
		if (!submission.release) {
			if (StringUtils.isNotBlank(submission.studyCode)) {	
				File studyFile = new File(resultDirectory + File.separator + VariableSRA.xmlStudies);
				writeStudyXml(submission, studyFile);
			}
			if (submission.sampleCodes.size() != 0){
				File sampleFile = new File(resultDirectory + File.separator + VariableSRA.xmlSamples);
				writeSampleXml(submission, sampleFile); 
			}
			if (submission.experimentCodes.size() != 0){
				File experimentFile = new File(resultDirectory + File.separator + VariableSRA.xmlExperiments);
				writeExperimentXml(submission, experimentFile); 
			} else {
				System.out.println("experimentCodes==0 ??????????");
				logger.debug("experimentCodes==0 ??????????");
			}
			if (submission.runCodes.size() != 0){
				File runFile = new File(resultDirectory + File.separator + VariableSRA.xmlRuns);
				writeRunXml(submission, runFile); 
			} else {
				System.out.println("runCodes==0 ??????????");
				logger.debug("runCodes==0 ??????????");
			}
		
			File submissionFile = new File(resultDirectory + File.separator + VariableSRA.xmlSubmission);
			writeSubmissionXml(submission, submissionFile);
		} else {
			File submissionFile = new File(resultDirectory + File.separator + VariableSRA.xmlSubmission);
			writeSubmissionReleaseXml(submission, submissionFile);

		}
		// mettre à jour dans la base l'objet submission pour les champs xml...
//		MongoDBDAO.update(InstanceConstants.SRA_SUBMISSION_COLL_NAME, Submission.class, 
//				DBQuery.is("code", submissionCode),
//				DBUpdate.set("xmlSubmission", submission.xmlSubmission).set("xmlStudys", submission.xmlStudys).set("xmlSamples", submission.xmlSamples).set("xmlExperiments", submission.xmlExperiments).set("xmlRuns", submission.xmlRuns).set("traceInformation.modifyUser", VariableSRA.admin).set("traceInformation.modifyDate", new Date()));
		submissionAPI.dao_update(DBQuery.is("code", submissionCode),
								 DBUpdate.set("xmlSubmission", submission.xmlSubmission)
								 .set("xmlStudys", submission.xmlStudys)
								 .set("xmlSamples", submission.xmlSamples)
								 .set("xmlExperiments", submission.xmlExperiments)
								 .set("xmlRuns", submission.xmlRuns)
								 .set("traceInformation.modifyUser", VariableSRA.admin)
								 .set("traceInformation.modifyDate", new Date()));
	
		return submission;
	}

	public void writeStudyXml (Submission submission, File outputFile) throws IOException, SraException {	
		if (submission == null) {
			return;
		}
		// Si demande de release pas d'ecriture de study.
		if (submission.release) {
			return;
		}
		if (StringUtils.isNotBlank(submission.studyCode)) {	
			System.out.println("Creation du fichier " + outputFile);
			// ouvrir fichier en ecriture
			String chaine = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n";
			chaine = chaine + "<STUDY_SET>\n";
			String studyCode = submission.studyCode;
			// Recuperer objet study dans la base :
//			Study study = MongoDBDAO.findByCode(InstanceConstants.SRA_STUDY_COLL_NAME, models.sra.submit.common.instance.Study.class, studyCode);
			Study study = studyAPI.dao_getObject(studyCode);
			//output_buffer.write("//\n");
			if (study == null){
				throw new SraException("study impossible à recuperer dans base :"+ studyCode);
			}
			System.out.println("Ecriture du study " + studyCode);

			chaine = chaine + "  <STUDY alias=\""+ studyCode + "\" ";
			if (StringUtils.isNotBlank(study.accession)) {	
				chaine = chaine + " accession=\"" + study.accession + "\" ";
			}
				
			chaine = chaine + ">\n";
			chaine = chaine + "    <DESCRIPTOR>\n";
			chaine = chaine + "      <STUDY_TITLE>" + study.title + "</STUDY_TITLE>\n";
			chaine = chaine + "      <STUDY_TYPE existing_study_type=\""+ VariableSRA.mapExistingStudyType().get(study.existingStudyType.toLowerCase()) +"\"/>\n";
			chaine = chaine + "      <STUDY_ABSTRACT>" + study.studyAbstract + "</STUDY_ABSTRACT>\n";
			chaine = chaine + "      <CENTER_PROJECT_NAME>" + study.centerProjectName+"</CENTER_PROJECT_NAME>\n"; 
			//if (study.bioProjectId != 0) {
				chaine = chaine + "      <RELATED_STUDIES>\n";
				chaine = chaine + "        <RELATED_STUDY>\n";
				chaine = chaine + "          <RELATED_LINK>\n";
				chaine = chaine + "            <DB>ENA</DB>\n";
				chaine = chaine + "            <ID>" + study.bioProjectId + "</ID>\n";
				chaine = chaine + "          </RELATED_LINK>\n";
				chaine = chaine + "          <IS_PRIMARY>false</IS_PRIMARY>\n";
				chaine = chaine + "        </RELATED_STUDY>\n";
				chaine = chaine + "      </RELATED_STUDIES>\n";
			//}
				
			chaine = chaine + "      <STUDY_DESCRIPTION>"+study.description+"</STUDY_DESCRIPTION>\n";
			chaine = chaine + "    </DESCRIPTOR>\n";
			chaine = chaine + "  </STUDY>\n";
			chaine = chaine + "</STUDY_SET>\n";
			
			try (BufferedWriter output_buffer = new BufferedWriter(new java.io.FileWriter(outputFile))) {
				output_buffer.write(chaine);
			}
			
			// output_buffer.close();
			submission.xmlStudys = outputFile.getName();
		} // end if		
	} // end writeStudyXml
	   
	
	public void writeSampleXml (Submission submission, File outputFile) throws IOException, SraException {
		if (submission == null)
			return;
		System.out.println("sample = "  + submission.sampleCodes.get(0));
		// Si demande de release pas d'ecriture de sample.
		if (submission.release) {
			return;
		}
		if (! submission.sampleCodes.isEmpty()) {	
			// ouvrir fichier en ecriture
			System.out.println("Creation du fichier " + outputFile);

			String chaine = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n";
			chaine = chaine + "<SAMPLE_SET>\n";
			for (String sampleCode : submission.sampleCodes){
				System.out.println("sampleCode = '" + sampleCode +"'");
				// Recuperer objet sample dans la base :
//				Sample sample = MongoDBDAO.findByCode(InstanceConstants.SRA_SAMPLE_COLL_NAME, models.sra.submit.common.instance.Sample.class, sampleCode);
				Sample sample = sampleAPI.dao_getObject(sampleCode);
				if (sample == null){
					throw new SraException("sample impossible à recuperer dans base :"+ sampleCode);
				}
				//output_buffer.write("//\n");
				System.out.println("Ecriture du sample " + sampleCode);

				chaine = chaine + "  <SAMPLE alias=\""+ sampleCode + "\"";
				
				if (StringUtils.isNotBlank(sample.accession)) {
					chaine = chaine + " accession=\"" + sample.accession + "\"";
				}
				chaine = chaine + ">\n";
				if (StringUtils.isNotBlank(sample.title)) {
					chaine = chaine + "    <TITLE>" + sample.title + "</TITLE>\n";
				}
				chaine = chaine + "    <SAMPLE_NAME>\n";
				chaine = chaine + "      <TAXON_ID>" + sample.taxonId + "</TAXON_ID>\n";
				if (StringUtils.isNotBlank(sample.scientificName)) {
					chaine = chaine + "      <SCIENTIFIC_NAME>" + sample.scientificName + "</SCIENTIFIC_NAME>\n";
				}
				if (StringUtils.isNotBlank(sample.commonName)) {
					chaine = chaine + "      <COMMON_NAME>" + sample.commonName + "</COMMON_NAME>\n";
				}
				if (StringUtils.isNotBlank(sample.anonymizedName)) {
					chaine = chaine + "      <ANONYMIZED_NAME>" + sample.anonymizedName + "</ANONYMIZED_NAME>\n";
				}
				chaine = chaine + "    </SAMPLE_NAME>\n";
				if (StringUtils.isNotBlank(sample.description)) {
					chaine = chaine + "      <DESCRIPTION>" + sample.description + "</DESCRIPTION>\n";
				}
				// apres reprise historique, on n'affiche plus l'attribut clone si champs clone existe.
				// C'est de la responsabilite de l'utilisateur de remplir attributes s'il veut y voir un tag clone.
				if (StringUtils.isNotBlank(sample.attributes)) {
					chaine = chaine + "      <SAMPLE_ATTRIBUTES>\n";
					chaine = chaine + "             " + sample.attributes; 
					chaine = chaine + "      </SAMPLE_ATTRIBUTES>\n";
				}
				chaine = chaine + "  </SAMPLE>\n";
			}
			chaine = chaine + "</SAMPLE_SET>\n";
			
			try (BufferedWriter output_buffer = new BufferedWriter(new java.io.FileWriter(outputFile))) {
				output_buffer.write(chaine);
			} 
			
			submission.xmlSamples = outputFile.getName();
		}
	}
	
	
	public void writeExperimentXml (Submission submission, File outputFile) throws IOException, SraException {
		if (submission == null) {
			return;
		}
		// Si demande de release pas d'ecriture d'experiment.
		if (submission.release) {
			return;
		}
		// Pas de creation de fichier experiment.xml si aucun experiment à soumettre:
		if (submission.experimentCodes.isEmpty()) {
			return;
		}
		writeSimpleExperimentXml(submission.experimentCodes, outputFile);
		submission.xmlExperiments = outputFile.getName();
	}
	
	public void writeSimpleExperimentXml (List<String> experimentsCodes, File outputFile) throws IOException, SraException {
		// ouvrir fichier en ecriture
		System.out.println("Creation du fichier " + outputFile);
		String chaine = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n";
		chaine = chaine + "<EXPERIMENT_SET>\n";
		for (String experimentCode : experimentsCodes){
			// Recuperer objet experiment dans la base :
//			Experiment experiment = MongoDBDAO.findByCode(InstanceConstants.SRA_EXPERIMENT_COLL_NAME, models.sra.submit.sra.instance.Experiment.class, experimentCode);
			Experiment experiment =  experimentAPI.dao_getObject(experimentCode);
			//output_buffer.write("//\n");
			System.out.println("Ecriture de experiment " + experimentCode);
			if (experiment == null){
				throw new SraException("experiment impossible à recuperer dans base :"+ experimentCode);
			}
			chaine = chaine + "  <EXPERIMENT alias=\"" + experimentCode + "\" center_name=\"" + VariableSRA.centerName + "\"";
			if (StringUtils.isNotBlank(experiment.accession)) {
				chaine = chaine + " accession=\"" + experiment.accession + "\" ";	
			}
			chaine = chaine + ">\n";
			// Les champs title et libraryName sont considerés comme obligatoires
			chaine = chaine + "    <TITLE>" + experiment.title + "</TITLE>\n";
			chaine = chaine + "    <STUDY_REF ";
			//if (StringUtils.isNotBlank(experiment.studyCode) && (experiment.studyCode.startsWith("external"))) { 
			if (StringUtils.isNotBlank(experiment.studyCode)) { 
				if (! experiment.studyCode.startsWith("external")){
					chaine = chaine + " refname=\"" + experiment.studyCode +"\"";
				}
			}
			if (StringUtils.isNotBlank(experiment.studyAccession)){
				chaine = chaine + " accession=\"" + experiment.studyAccession + "\"";
			}
			chaine = chaine + "/>\n"; 

			chaine = chaine + "      <DESIGN>\n";
			chaine = chaine + "        <DESIGN_DESCRIPTION></DESIGN_DESCRIPTION>\n";
			chaine = chaine + "          <SAMPLE_DESCRIPTOR  ";
			//if (StringUtils.isNotBlank(experiment.sampleCode) && (experiment.sampleCode.startsWith("external"))) {
			if (StringUtils.isNotBlank(experiment.sampleCode)){
				// Ecrire le nom du sample uniquement si sample Genoscope car nom "bidon" pour les samples externe
				if (! experiment.sampleCode.startsWith("external")){
					chaine = chaine+  "refname=\"" + experiment.sampleCode + "\"";
				}
			}
			if (StringUtils.isNotBlank(experiment.sampleAccession)){
				chaine = chaine + " accession=\""+experiment.sampleAccession + "\"";
			}
			chaine = chaine + "/>\n";

			chaine = chaine + "          <LIBRARY_DESCRIPTOR>\n";
			chaine = chaine + "            <LIBRARY_NAME>" + experiment.libraryName + "</LIBRARY_NAME>\n";
			chaine = chaine + "            <LIBRARY_STRATEGY>"+ VariableSRA.mapLibraryStrategy().get(experiment.libraryStrategy.toLowerCase()) + "</LIBRARY_STRATEGY>\n";
			chaine = chaine + "            <LIBRARY_SOURCE>" + VariableSRA.mapLibrarySource().get(experiment.librarySource.toLowerCase()) + "</LIBRARY_SOURCE>\n";
			chaine = chaine + "            <LIBRARY_SELECTION>" + VariableSRA.mapLibrarySelection().get(experiment.librarySelection.toLowerCase()) + "</LIBRARY_SELECTION>\n";
			chaine = chaine + "            <LIBRARY_LAYOUT>\n";

			chaine = chaine + "              <"+ VariableSRA.mapLibraryLayout().get(experiment.libraryLayout.toLowerCase());	
			if("PAIRED".equalsIgnoreCase(experiment.libraryLayout)) {
				chaine = chaine + " NOMINAL_LENGTH=\"" + experiment.libraryLayoutNominalLength + "\"";
			}
			chaine = chaine + " />\n";

			chaine = chaine + "            </LIBRARY_LAYOUT>\n";
			if (StringUtils.isBlank(experiment.libraryConstructionProtocol)){
				chaine = chaine + "            <LIBRARY_CONSTRUCTION_PROTOCOL>none provided</LIBRARY_CONSTRUCTION_PROTOCOL>\n";
			} else {
				chaine = chaine + "            <LIBRARY_CONSTRUCTION_PROTOCOL>"+experiment.libraryConstructionProtocol+"</LIBRARY_CONSTRUCTION_PROTOCOL>\n";
			}
			chaine = chaine + "          </LIBRARY_DESCRIPTOR>\n";
			if (! "OXFORD_NANOPORE".equalsIgnoreCase(experiment.typePlatform)) {
				chaine = chaine + "          <SPOT_DESCRIPTOR>\n";
				chaine = chaine + "            <SPOT_DECODE_SPEC>\n";
				chaine = chaine + "              <SPOT_LENGTH>"+experiment.spotLength+"</SPOT_LENGTH>\n";
				//for (ReadSpec readSpec: experiment.readSpecs) {

//				Exemple sans passer par des lambda:
//				Comparator< ? extends ReadSpec> x; // n'importe quel type qui etend ReadSpec 
//				Comparator< ? super ReadSpec> y;// n'importe quel type qui est une super class de ReadSpec 
				List <ReadSpec> list = new ArrayList<> (experiment.readSpecs);
				Collections.sort(list, new Comparator <ReadSpec>() {
					@Override
					public int compare(ReadSpec o1, ReadSpec o2) {
						return new Integer(o1.readIndex).compareTo(new Integer(o2.readIndex));
					}});	
//				List <ReadSpec_2> list_2 = new ArrayList<> ();
//				Collections.sort(list_2, new Comparator <ReadSpec_2>() {
//					@Override
//					public int compare(ReadSpec_2 o1, ReadSpec_2 o2) {
//						return new Integer(o1.readIndex).compareTo(new Integer(o2.readIndex));
//					}});	
//				Collections.sort(list_2, new Comparator <ReadSpec>() {
//					@Override
//					public int compare(ReadSpec o1, ReadSpec o2) {
//						return new Integer(o1.readIndex).compareTo(new Integer(o2.readIndex));
//					}});	
//				
//              Exemple avec lambda (demande d'avoir une seule methode compare
				experiment.readSpecs.sort((a,b)->Integer.compare(a.readIndex, b.readIndex)); // Trie la liste par readIndex.
				for (ReadSpec readSpec: experiment.readSpecs) {
					chaine = chaine + "              <READ_SPEC>\n";
					chaine = chaine + "                <READ_INDEX>"+readSpec.readIndex+"</READ_INDEX>\n";
					chaine = chaine + "                <READ_LABEL>"+readSpec.readLabel+"</READ_LABEL>\n";
					chaine = chaine + "                <READ_CLASS>"+readSpec.readClass+"</READ_CLASS>\n";
					chaine = chaine + "                <READ_TYPE>"+readSpec.readType+"</READ_TYPE>\n";
					chaine = chaine + "                <BASE_COORD>" + readSpec.baseCoord + "</BASE_COORD>\n";
					chaine = chaine + "              </READ_SPEC>\n";
				}
				chaine = chaine + "            </SPOT_DECODE_SPEC>\n";
				chaine = chaine + "          </SPOT_DESCRIPTOR>\n";
			}
			chaine = chaine + "      </DESIGN>\n";
			chaine = chaine + "      <PLATFORM>\n";
			chaine = chaine + "        <" + VariableSRA.mapTypePlatform().get(experiment.typePlatform.toLowerCase()) + ">\n";
			chaine = chaine + "          <INSTRUMENT_MODEL>" + VariableSRA.mapInstrumentModel().get(experiment.instrumentModel.toLowerCase()) + "</INSTRUMENT_MODEL>\n";
			chaine = chaine + "        </" + VariableSRA.mapTypePlatform().get(experiment.typePlatform.toLowerCase()) + ">\n";
			chaine = chaine + "      </PLATFORM>\n";
			chaine = chaine + "  </EXPERIMENT>\n";
		}
		chaine = chaine + "</EXPERIMENT_SET>\n";
		
		try (BufferedWriter output_buffer = new BufferedWriter(new java.io.FileWriter(outputFile))) {
			output_buffer.write(chaine);
		}
		//output_buffer.close();
	}
	
	public void writeRunXml (Submission submission, File outputFile) throws IOException, SraException {
		if (submission == null) {
			return;
		}
		// Si demande de release pas d'ecriture de run
		if (submission.release) {
			return;
		}
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		// On accede au run via l'experiment:
		
		if (! submission.experimentCodes.isEmpty()) {	
			// ouvrir fichier en ecriture
			System.out.println("Creation du fichier " + outputFile);
			String chaine = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n";
			chaine = chaine + "<RUN_SET>\n";
			for (String experimentCode : submission.experimentCodes){
				// Recuperer objet experiment dans la base :
//				Experiment experiment = MongoDBDAO.findByCode(InstanceConstants.SRA_EXPERIMENT_COLL_NAME, models.sra.submit.sra.instance.Experiment.class, experimentCode);
				Experiment experiment = experimentAPI.dao_getObject(experimentCode);
				if (experiment == null) {
					throw new SraException("experiment impossible à recuperer dans base :"+ experimentCode);
				}
				Run run = experiment.run;
				if (run == null){
					throw new SraException("run impossible à recuperer dans objet experiment:"+ experimentCode);
				}
				//output_buffer.write("//\n");
				String runCode = run.code;
				System.out.println("Ecriture du run " + runCode);
				chaine = chaine + "  <RUN alias=\""+ runCode + "\" ";
				if (StringUtils.isNotBlank(run.accession)) {
					chaine = chaine + " accession=\"" + run.accession + "\" ";
				}
				
				//Format date
				chaine =  chaine + "run_date=\""+ formatter.format(run.runDate)+"\"  run_center=\""+run.runCenter+ "\" ";
				chaine = chaine + ">\n";
				chaine = chaine + "    <EXPERIMENT_REF refname=\"" + experimentCode + "\"/>\n";
				chaine = chaine + "    <DATA_BLOCK>\n";
				chaine = chaine + "      <FILES>\n";
			
				for (RawData rawData: run.listRawData) {
					String fileType = rawData.extention;
					String relatifName = rawData.relatifName;
					fileType = fileType.replace(".gz", "");
					chaine = chaine + "        <FILE filename=\"" + relatifName + "\" "+"filetype=\"" + fileType + "\" checksum_method=\"MD5\" checksum=\"" + rawData.md5 + "\">\n";
					if ( run.listRawData.size() == 2 ) {
						chaine = chaine + "          <READ_LABEL>F</READ_LABEL>\n";
						chaine = chaine + "          <READ_LABEL>R</READ_LABEL>\n";
					}
					chaine = chaine + "        </FILE>\n";
				}
				chaine = chaine + "      </FILES>\n";
				chaine = chaine + "    </DATA_BLOCK>\n";
				chaine = chaine + "  </RUN>\n";
				}
				chaine = chaine + "</RUN_SET>\n";
				
				try (BufferedWriter output_buffer = new BufferedWriter(new java.io.FileWriter(outputFile))) {
					output_buffer.write(chaine);
				}
				// output_buffer.close();
				
				submission.xmlRuns = outputFile.getName();
			}
		}
	

		
	public void writeSubmissionXml (Submission submission, File outputFile) throws IOException {
		if (submission == null) {
			return;
		}
		
		
		// ouvrir fichier en ecriture
		System.out.println("Creation du fichier " + outputFile);
		String chaine = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n";
		chaine = chaine + "<SUBMISSION_SET>\n";
		
		System.out.println("Ecriture du submission " + submission.code);
		chaine = chaine + "  <SUBMISSION alias=\""+ submission.code + "\" ";
		chaine = chaine + ">\n";	
		chaine = chaine + "    <CONTACTS>\n";
		chaine = chaine + "      <CONTACT name=\"william\" inform_on_status=\"william@genoscope.cns.fr\" inform_on_error=\"william@genoscope.cns.fr\"/>\n";
		chaine = chaine + "    </CONTACTS>\n";
			
		chaine = chaine + "    <ACTIONS>\n";
		// soumission systematique en confidential meme si study deja public
		chaine = chaine + "      <ACTION>\n        <HOLD/>\n      </ACTION>\n";
		if (StringUtils.isNotBlank(submission.studyCode)) {
			chaine = chaine + "      <ACTION>\n        <ADD source=\"study.xml\" schema=\"study\"/>\n      </ACTION>\n";
		}
		if (!submission.sampleCodes.isEmpty()){
			chaine = chaine + "      <ACTION>\n        <ADD source=\"sample.xml\" schema=\"sample\"/>\n      </ACTION>\n";
		}
		if (!submission.experimentCodes.isEmpty()){
			chaine = chaine + "      <ACTION>\n        <ADD source=\"experiment.xml\" schema=\"experiment\"/>\n      </ACTION>\n";
			chaine = chaine + "      <ACTION>\n        <ADD source=\"run.xml\" schema=\"run\"/>\n      </ACTION>\n";
		}
		chaine = chaine + "    </ACTIONS>\n";
		
		
		
		chaine = chaine + "  </SUBMISSION>\n";
		chaine = chaine + "</SUBMISSION_SET>\n";
		
		try (BufferedWriter output_buffer = new BufferedWriter(new java.io.FileWriter(outputFile))) {
			output_buffer.write(chaine);
		}
		submission.xmlSubmission = outputFile.getName();
	}


	public void writeSubmissionReleaseXml (Submission submission, File outputFile) throws IOException, SraException {
		
		if (submission == null) {
			throw new SraException("Aucune soumission en argument");
		}
		if (StringUtils.isBlank(submission.studyCode)) {
			throw new SraException("Impossible de faire la soumission pour release " + submission.code + " sans studyCode");
		}
//		Study study = MongoDBDAO.findByCode(InstanceConstants.SRA_STUDY_COLL_NAME, Study.class, submission.studyCode);	
		Study study = studyAPI.dao_getObject(submission.studyCode);	
		if (StringUtils.isBlank(study.accession)) {
			throw new SraException("Impossible de releaser le study " + study.code + " sans numeros d'accession");
		}

		// ouvrir fichier en ecriture
		logger.debug("Creation du fichier " + outputFile);
		String chaine = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n";
		chaine = chaine + "<SUBMISSION_SET>\n";
		
		logger.debug("Ecriture du submission " + submission.code);
		chaine = chaine + "  <SUBMISSION alias=\""+ submission.code + "\" ";
		chaine = chaine + ">\n";	
		chaine = chaine + "    <CONTACTS>\n";
		chaine = chaine + "      <CONTACT  name=\"william\" inform_on_status=\"william@genoscope.cns.fr\" inform_on_error=\"william@genoscope.cns.fr\"/>\n";
		chaine = chaine + "    </CONTACTS>\n";
			
		chaine = chaine + "    <ACTIONS>\n";
		
		chaine = chaine + "      <ACTION>\n        <RELEASE target=\"" + study.accession + "\"/>\n      </ACTION>\n";
		
		chaine = chaine + "    </ACTIONS>\n";
		
		
		
		chaine = chaine + "  </SUBMISSION>\n";
		chaine = chaine + "</SUBMISSION_SET>\n";
		
		try (BufferedWriter output_buffer = new BufferedWriter(new java.io.FileWriter(outputFile))) {
			output_buffer.write(chaine);
		}
		submission.xmlSubmission = outputFile.getName();
	}

}
