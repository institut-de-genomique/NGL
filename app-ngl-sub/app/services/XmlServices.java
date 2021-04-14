package services;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import fr.cea.ig.ngl.dao.api.sra.ExperimentAPI;
import fr.cea.ig.ngl.dao.api.sra.ProjectAPI;
import fr.cea.ig.ngl.dao.api.sra.SampleAPI;
import fr.cea.ig.ngl.dao.api.sra.StudyAPI;
import models.sra.submit.common.instance.Project;
import models.sra.submit.common.instance.Sample;
import models.sra.submit.common.instance.Study;
import models.sra.submit.common.instance.Submission;
import models.sra.submit.sra.instance.Experiment;
import models.sra.submit.sra.instance.RawData;
import models.sra.submit.sra.instance.ReadSpec;
import models.sra.submit.sra.instance.Run;
import models.sra.submit.util.SraException;
import models.sra.submit.util.SubmissionTypeException;
import models.sra.submit.util.VariableSRA;

/**
 * Service permettant d'ecrire les objets SRA au format xml accepté par l'EBI.
 * Aucun changement d'etat des objets dans ce service.
 * @author sgas
 *
 */
public class XmlServices {

	private static final play.Logger.ALogger logger = play.Logger.of(XmlServices.class);
	private final StudyAPI      studyAPI;
	private final ExperimentAPI experimentAPI;
	private final SampleAPI     sampleAPI;
	private final ProjectAPI    projectAPI;

	
	@Inject
	public XmlServices(StudyAPI      studyAPI,
				       SampleAPI     sampleAPI,
				       ExperimentAPI experimentAPI,
				       ProjectAPI    projectAPI) {
		this.studyAPI      = studyAPI;
		this.sampleAPI     = sampleAPI;
		this.experimentAPI = experimentAPI;
		this.projectAPI    = projectAPI;
	}

	
	/**
	 * Ecrit les fichiers xml correspondant à la soumission et met à jour hors base de données, 
	 * la soumission pour les champs 
	 * {@link Submission #xmlSubmission}
	 * {@link Submission #xmlStudys}
	 * {@link Submission #xmlSamples}
	 * {@link Submission #xmlExperiments}
	 * {@link Submission #xmRuns}
	 * {@link Submission #xmlProjects}
	 * Aucune mise à jour de status de la soumission.
	 * @param submission        objet soumission
	 * @throws SraException     Error
	 */	
	public void writeAllXml(Submission submission) throws SraException {
		try {
			String resultDirectory = submission.submissionDirectory;
			logger.debug("resultDirectory = " + resultDirectory);
			writeAllXml(submission, resultDirectory);
			
		} catch (IOException e){
			throw new SraException("writeAllXml", e);
		}
	}
	
	// ecrit uniquement project.xml ou bien l'ensemble study.xml et sample.xml et experiment.xml et run.xml:
	public void writeOtherSubmission(Submission submission, String resultDirectory) throws IOException, SraException {
		if (StringUtils.isNotBlank(submission.ebiProjectCode)) {	
			File projectFile = new File(resultDirectory + File.separator + VariableSRA.xmlProjects);
			writeProjectXml(submission, projectFile);
			submission.xmlProjects = VariableSRA.xmlProjects; 
			return;
		}
		if (StringUtils.isNotBlank(submission.studyCode)) {	
			File studyFile = new File(resultDirectory + File.separator + VariableSRA.xmlStudies);
			writeStudyXml(submission, studyFile);
			submission.xmlStudys = VariableSRA.xmlStudies; 
		}
		if (submission.sampleCodes.size() != 0){
			File sampleFile = new File(resultDirectory + File.separator + VariableSRA.xmlSamples);
			writeSampleXml(submission, sampleFile); 
			submission.xmlSamples = VariableSRA.xmlSamples;
		}
		if (submission.experimentCodes.size() != 0){
			File experimentFile = new File(resultDirectory + File.separator + VariableSRA.xmlExperiments);
			writeExperimentXml(submission, experimentFile); 
			submission.xmlExperiments = VariableSRA.xmlExperiments;
		} 
		if (submission.runCodes.size() != 0){
			File runFile = new File(resultDirectory + File.separator + VariableSRA.xmlRuns);
			writeRunXml(submission, runFile); 
			submission.xmlRuns = VariableSRA.xmlRuns;
		} 
		
	}
	
	/**
	 * Ecrit les fichiers xml correspondant à la soumission et met à jour hors base de donnée, la soumission 
	 * pour les champs 
	 * {@link Submission #xmlSubmission}
	 * {@link Submission #xmlStudys}
	 * {@link Submission #xmlSamples}
	 * {@link Submission #xmlExperiments}
	 * {@link Submission #xmRuns}
	 * @param submission        objet soumission
	 * @param resultDirectory   Repertoire ou se seront généres les fichiers xml
	 * @throws IOException      Error
	 * @throws SraException     Error
	 */
	public void writeAllXml(Submission submission, String resultDirectory) throws IOException, SraException {
		logger.debug("creation des fichiers xml pour l'ensemble de la soumission "+ submission.code);
		logger.debug("resultDirectory = " + resultDirectory);
		// Recuperer l'objet submission:
//		Submission submission = submissionAPI.dao_getObject(submissionCode);
		logger.debug("Recuperation de la submission" + submission.code);
		// si on est dans soumission de données :
//		if (!submission.release) {
		// meme code dans le cas de creation ou update, sauf pour ecriture de run qui ne doit pas etre 
		// faite si UPDATE car impossible de mettre à jour run à l'EBI, mais dans ce cas, submission.runCodes
		// doit etre vide.
		switch (submission.type) {
		case  CREATION : { 
			writeOtherSubmission(submission, resultDirectory);
			File submissionFile = new File(resultDirectory + File.separator + VariableSRA.xmlSubmission);
			submission.xmlSubmission = VariableSRA.xmlSubmission;
			writeSubmissionXml(submission, submissionFile);
			break;
		}
		case  UPDATE : {
			writeOtherSubmission(submission, resultDirectory);
			File submissionFile = new File(resultDirectory + File.separator + VariableSRA.xmlSubmission);
			submission.xmlSubmission = VariableSRA.xmlSubmission;
			writeSubmissionUpdateXml(submission, submissionFile);
			break;
		}
		case RELEASE : {
			File submissionFile = new File(resultDirectory + File.separator + VariableSRA.xmlSubmission);
			submission.xmlSubmission = VariableSRA.xmlSubmission;
			writeSubmissionReleaseXml(submission, submissionFile);
			break;
		}

		default :
			//throw new SubmissionTypeException(submission);
			throw new SraException("Type de soumission non gerée :" + submission.type 
			+ " pour la soumission " + submission.code);
		}


	}

	
	
	public void writeStudyXml (Submission submission, File outputFile) throws IOException, SraException {	
		if (submission == null) {
			return;
		}
		// Si demande de release pas d'ecriture de study.
//		if (submission.release) {
		if (submission.type == Submission.Type.RELEASE) {
				return;
		}
		if (StringUtils.isNotBlank(submission.studyCode)) {	
			logger.debug("Creation du fichier " + outputFile);
			// ouvrir fichier en ecriture
			StringBuilder sb = new StringBuilder();
			
			sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n");
			sb.append("<STUDY_SET>\n");
			String studyCode = submission.studyCode;
			// Recuperer objet study dans la base :
			Study study = studyAPI.dao_getObject(studyCode);
			if (study == null){
				throw new SraException("study impossible à recuperer dans base :"+ studyCode);
			}
			logger.debug("Ecriture du study " + studyCode);

			sb.append("  <STUDY alias=\"").append(studyCode).append("\" ");
			if (StringUtils.isNotBlank(study.accession)) {	
				sb.append(" accession=\"").append(study.accession).append("\" ");
			}
				
			sb.append(">\n");
			sb.append("    <DESCRIPTOR>\n");
			sb.append("      <STUDY_TITLE>").append(study.title).append("</STUDY_TITLE>\n");
			sb.append("      <STUDY_TYPE existing_study_type=\"").append(VariableSRA.mapExistingStudyType().get(study.existingStudyType.toLowerCase())).append("\"/>\n");
			sb.append("      <STUDY_ABSTRACT>").append(study.studyAbstract).append("</STUDY_ABSTRACT>\n");
			sb.append("      <CENTER_PROJECT_NAME>").append(study.centerProjectName).append("</CENTER_PROJECT_NAME>\n"); 
			//if (study.bioProjectId != 0) {
			sb.append("      <RELATED_STUDIES>\n");
			sb.append("        <RELATED_STUDY>\n");
			sb.append("          <RELATED_LINK>\n");
			sb.append("            <DB>ENA</DB>\n");
//			sb.append("            <ID>").append(study.bioProjectId).append("</ID>\n");
			sb.append("            <ID>0</ID>\n");
			sb.append("          </RELATED_LINK>\n");
			sb.append("          <IS_PRIMARY>false</IS_PRIMARY>\n");
			sb.append("        </RELATED_STUDY>\n");
			sb.append("      </RELATED_STUDIES>\n");
			//}
				
			sb.append("      <STUDY_DESCRIPTION>").append(study.description).append("</STUDY_DESCRIPTION>\n");
			sb.append("    </DESCRIPTOR>\n");
			sb.append("  </STUDY>\n");
			sb.append("</STUDY_SET>\n");
			
			try (BufferedWriter output_buffer = new BufferedWriter(new java.io.FileWriter(outputFile))) {
				output_buffer.write(sb.toString());
			}			
			submission.xmlStudys = outputFile.getName();
		} // end if		
	} // end writeStudyXml
	   
	
	
	public void writeProjectXml (Submission submission, File outputFile) throws IOException, SraException {	
		if (submission == null) {
			return;
		}
		// ecriture de project.xml uniquement si submission.type == UPDATE
		if (submission.type != Submission.Type.UPDATE) {
				return;
		}

		if (StringUtils.isBlank(submission.ebiProjectCode)) {	
			return;
		}
		logger.debug("Creation du fichier " + outputFile);
		// ouvrir fichier en ecriture
		StringBuilder sb = new StringBuilder();
			
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n");
		sb.append("<PROJECT_SET>\n");
		String projectCode = submission.ebiProjectCode;
		// Recuperer objet project dans la base :
		Project project = projectAPI.dao_getObject(projectCode);
		if (project == null){
			throw new SraException("project impossible à recuperer dans base :"+ projectCode);
		}
		logger.debug("Ecriture du project " + projectCode);

		sb.append("  <PROJECT alias=\"").append(projectCode).append("\" ");
		if (StringUtils.isNotBlank(project.accession)) {	
			sb.append(" accession=\"").append(project.accession).append("\" ");
		}
		sb.append(" center_name=\"").append(VariableSRA.centerName).append("\" ");	
		sb.append(">\n");
		sb.append("  <IDENTIFIERS>\n");
		if (StringUtils.isNotBlank(project.accession)) {
		sb.append("     <PRIMARY_ID>").append(project.accession).append("</PRIMARY_ID>\n");
		}
		if (StringUtils.isNotBlank(project.externalId)) {
			sb.append("     <SECONDARY_ID>").append(project.externalId).append("</SECONDARY_ID>\n");
		}
		String centerName = "\"".concat(VariableSRA.centerName).concat("\"");
	
		sb.append("     <SUBMITTER_ID namespace=\"").append(VariableSRA.centerName).append("\">").append(project.code).append("</SUBMITTER_ID>\n");
		sb.append("  </IDENTIFIERS>\n");
	
		if(StringUtils.isNotBlank(project.name)) {
			sb.append("  <NAME>").append(project.name).append("</NAME>\n");
		}
		if (StringUtils.isNotBlank(project.title)) {
			sb.append("  <TITLE>").append(project.title).append("</TITLE>\n");
		}
		if (StringUtils.isNotBlank(project.description)) {
			sb.append("  <DESCRIPTION>").append(project.description).append("</DESCRIPTION>\n");
		}
		sb.append("  <SUBMISSION_PROJECT>\n");
		sb.append("     <SEQUENCING_PROJECT>\n");
		if ( project.locusTagPrefixs != null && ! project.locusTagPrefixs.isEmpty() ) {
			for (String locusTagPrefix : project.locusTagPrefixs) {
				if (StringUtils.isNotBlank(locusTagPrefix)) {
					sb.append("     <LOCUS_TAG_PREFIX>").append(locusTagPrefix).append("</LOCUS_TAG_PREFIX>\n");

				}
			}
		}
		sb.append("     </SEQUENCING_PROJECT>\n");
		sb.append("  </SUBMISSION_PROJECT>\n");
		sb.append("  </PROJECT>\n");
		sb.append("</PROJECT_SET>\n");
			
		try (BufferedWriter output_buffer = new BufferedWriter(new java.io.FileWriter(outputFile))) {
			output_buffer.write(sb.toString());
		}			
		submission.xmlProjects = outputFile.getName();
	} // end writeProjectXml
	   
	
	public void writeSampleXml (Submission submission, File outputFile) throws IOException, SraException {
		if (submission == null)
			return;
		logger.debug("sample = "  + submission.sampleCodes.get(0));
		// Si demande de release pas d'ecriture de sample.
//		if (submission.release) {
		if (submission.type == Submission.Type.RELEASE) {
			return;
		}
		if (! submission.sampleCodes.isEmpty()) {	
			// ouvrir fichier en ecriture
			logger.debug("Creation du fichier " + outputFile);
			StringBuilder sb = new StringBuilder();
			sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n");
			sb.append("<SAMPLE_SET>\n");
			for (String sampleCode : submission.sampleCodes){
				logger.debug("sampleCode = '" + sampleCode +"'");
				// Recuperer objet sample dans la base :
				Sample sample = sampleAPI.dao_getObject(sampleCode);
				if (sample == null){
					throw new SraException("sample impossible à recuperer dans base :"+ sampleCode);
				}
				//output_buffer.write("//\n");
				logger.debug("Ecriture du sample " + sampleCode);

				sb.append("  <SAMPLE alias=\"").append(sampleCode).append("\"");
				
				if (StringUtils.isNotBlank(sample.accession)) {
					sb.append(" accession=\"").append(sample.accession).append("\"");
				}
				sb.append(">\n");
				if (StringUtils.isNotBlank(sample.title)) {
					sb.append("    <TITLE>").append(sample.title).append("</TITLE>\n");
				}
				sb.append("    <SAMPLE_NAME>\n");
				sb.append("      <TAXON_ID>").append(sample.taxonId).append("</TAXON_ID>\n");
				if (StringUtils.isNotBlank(sample.scientificName)) {
					sb.append("      <SCIENTIFIC_NAME>").append(sample.scientificName).append("</SCIENTIFIC_NAME>\n");
				}
				if (StringUtils.isNotBlank(sample.commonName)) {
					sb.append("      <COMMON_NAME>").append(sample.commonName).append("</COMMON_NAME>\n");
				}
				if (StringUtils.isNotBlank(sample.anonymizedName)) {
					sb.append("      <ANONYMIZED_NAME>").append(sample.anonymizedName).append("</ANONYMIZED_NAME>\n");
				}
				sb.append("    </SAMPLE_NAME>\n");
				if (StringUtils.isNotBlank(sample.description)) {
					sb.append("    <DESCRIPTION>").append(sample.description).append("</DESCRIPTION>\n");
				}
				// apres reprise historique, on n'affiche plus l'attribut clone si champs clone existe.
				// C'est de la responsabilite de l'utilisateur de remplir attributes s'il veut y voir un tag clone.
				if (StringUtils.isNotBlank(sample.attributes)) {
					String sampleAttributes = sample.attributes; 
					logger.debug("sample.attributes = '" + sample.attributes + "'");
					if(sampleAttributes.startsWith("\n")) {
						sampleAttributes = sampleAttributes.replaceFirst("^\\s*\n\\s*", "    "); 
					}
					if(sampleAttributes.endsWith("\n")) {
						sampleAttributes = sampleAttributes.replace("\n$", ""); 
						sampleAttributes = sampleAttributes.replaceFirst("\\s*\n\\s*$", "  ");
					}
					logger.debug("sampleAttributes = '" + sampleAttributes + "'");

					sb.append("  <SAMPLE_ATTRIBUTES>\n");
						sb.append(sampleAttributes + "\n"); 
					sb.append("  </SAMPLE_ATTRIBUTES>\n");
				}
				sb.append("  </SAMPLE>\n");
			}
			sb.append("</SAMPLE_SET>\n");
			
			try (BufferedWriter output_buffer = new BufferedWriter(new java.io.FileWriter(outputFile))) {
				output_buffer.write(sb.toString());
			} 
			
			submission.xmlSamples = outputFile.getName();
		}
	}
	
	
	public void writeExperimentXml (Submission submission, File outputFile) throws IOException, SraException {
		if (submission == null) {
			return;
		}
		// Si demande de release pas d'ecriture d'experiment.
//		if (submission.release) {
		if (submission.type == Submission.Type.RELEASE) {
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
		logger.debug("Creation du fichier " + outputFile);
		StringBuilder sb = new StringBuilder();
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n");
		sb.append("<EXPERIMENT_SET>\n");
		for (String experimentCode : experimentsCodes) {
			// Recuperer objet experiment dans la base :
//			Experiment experiment = MongoDBDAO.findByCode(InstanceConstants.SRA_EXPERIMENT_COLL_NAME, models.sra.submit.sra.instance.Experiment.class, experimentCode);
			Experiment experiment =  experimentAPI.dao_getObject(experimentCode);
			logger.debug("Ecriture de experiment " + experimentCode);
			if (experiment == null){
				throw new SraException("experiment impossible à recuperer dans base :"+ experimentCode);
			}
			sb.append("  <EXPERIMENT alias=\"").append(experimentCode).append("\" center_name=\"").append(VariableSRA.centerName).append("\"");
			if (StringUtils.isNotBlank(experiment.accession)) {
				sb.append(" accession=\"").append(experiment.accession).append("\" ");	
			}
			sb.append(">\n");
			// Les champs title et libraryName sont considerés comme obligatoires
			sb.append("    <TITLE>").append(experiment.title).append("</TITLE>\n");
			sb.append("    <STUDY_REF ");
			//if (StringUtils.isNotBlank(experiment.studyCode) && (experiment.studyCode.startsWith("external"))) { 
			if (StringUtils.isNotBlank(experiment.studyCode)) { 
				if (! experiment.studyCode.startsWith("external")){
					sb.append(" refname=\"").append(experiment.studyCode).append("\"");
				}
			}
			if (StringUtils.isNotBlank(experiment.studyAccession)){
				sb.append(" accession=\"").append(experiment.studyAccession).append("\"");
			}
			sb.append("/>\n"); 

			sb.append("      <DESIGN>\n").append("        <DESIGN_DESCRIPTION></DESIGN_DESCRIPTION>\n");
			sb.append("          <SAMPLE_DESCRIPTOR  ");
			//if (StringUtils.isNotBlank(experiment.sampleCode) && (experiment.sampleCode.startsWith("external"))) {
			if (StringUtils.isNotBlank(experiment.sampleCode)){
				// Ecrire le nom du sample uniquement si sample Genoscope car nom "bidon" pour les samples externe
				if (! experiment.sampleCode.startsWith("external")){
					sb.append("refname=\"").append(experiment.sampleCode).append("\"");
				}
			}
			if (StringUtils.isNotBlank(experiment.sampleAccession)){
				sb.append(" accession=\"").append(experiment.sampleAccession).append("\"");
			}
			sb.append("/>\n");

			sb.append("          <LIBRARY_DESCRIPTOR>\n");
			sb.append("            <LIBRARY_NAME>").append(experiment.libraryName).append("</LIBRARY_NAME>\n");
			sb.append("            <LIBRARY_STRATEGY>").append(VariableSRA.mapLibraryStrategy().get(experiment.libraryStrategy.toLowerCase())).append("</LIBRARY_STRATEGY>\n");
			sb.append("            <LIBRARY_SOURCE>").append(VariableSRA.mapLibrarySource().get(experiment.librarySource.toLowerCase())).append("</LIBRARY_SOURCE>\n");
			sb.append("            <LIBRARY_SELECTION>").append(VariableSRA.mapLibrarySelection().get(experiment.librarySelection.toLowerCase())).append("</LIBRARY_SELECTION>\n");
			sb.append("            <LIBRARY_LAYOUT>\n");

			sb.append("              <").append(VariableSRA.mapLibraryLayout().get(experiment.libraryLayout.toLowerCase()));	
			if("PAIRED".equalsIgnoreCase(experiment.libraryLayout)) {
				sb.append(" NOMINAL_LENGTH=\"").append(experiment.libraryLayoutNominalLength).append("\"");
			}
			sb.append(" />\n");

			sb.append("            </LIBRARY_LAYOUT>\n");
			if (StringUtils.isBlank(experiment.libraryConstructionProtocol)){
				sb.append("            <LIBRARY_CONSTRUCTION_PROTOCOL>none provided</LIBRARY_CONSTRUCTION_PROTOCOL>\n");
			} else {
				sb.append("            <LIBRARY_CONSTRUCTION_PROTOCOL>").append(experiment.libraryConstructionProtocol).append("</LIBRARY_CONSTRUCTION_PROTOCOL>\n");
			}
			sb.append("          </LIBRARY_DESCRIPTOR>\n");
			if (! "OXFORD_NANOPORE".equalsIgnoreCase(experiment.typePlatform)) {
				sb.append("          <SPOT_DESCRIPTOR>\n");
				sb.append("            <SPOT_DECODE_SPEC>\n");
				sb.append("              <SPOT_LENGTH>").append(experiment.spotLength).append("</SPOT_LENGTH>\n");
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
					sb.append("              <READ_SPEC>\n");
					sb.append("                <READ_INDEX>");
					sb.append(                    readSpec.readIndex).append("</READ_INDEX>\n");
					sb.append("                <READ_LABEL>");
					sb.append(                    readSpec.readLabel).append("</READ_LABEL>\n");
					sb.append("                <READ_CLASS>");
					sb.append(                    readSpec.readClass).append("</READ_CLASS>\n");
					sb.append("                <READ_TYPE>");
					sb.append(                    readSpec.readType).append("</READ_TYPE>\n");
					sb.append("                <BASE_COORD>");
					sb.append(                    readSpec.baseCoord).append("</BASE_COORD>\n");
					sb.append("              </READ_SPEC>\n");
				}
				sb.append("            </SPOT_DECODE_SPEC>\n");
				sb.append("          </SPOT_DESCRIPTOR>\n");
			}
			sb.append("      </DESIGN>\n");
			sb.append("      <PLATFORM>\n");
			sb.append("        <").append(VariableSRA.mapTypePlatform().get(experiment.typePlatform.toLowerCase())).append(">\n");
			sb.append("          <INSTRUMENT_MODEL>")
									.append(VariableSRA.mapInstrumentModel().get(experiment.instrumentModel.toLowerCase()))
									.append("</INSTRUMENT_MODEL>\n");
			sb.append("        </").append(VariableSRA.mapTypePlatform().get(experiment.typePlatform.toLowerCase())).append(">\n");
			sb.append("      </PLATFORM>\n");
			sb.append("  </EXPERIMENT>\n");
		}
		sb.append("</EXPERIMENT_SET>\n");
		
		try (BufferedWriter output_buffer = new BufferedWriter(new java.io.FileWriter(outputFile))) {
			output_buffer.write(sb.toString());
		}
	}
	
	public void writeRunXml (Submission submission, File outputFile) throws IOException, SraException {
		if (submission == null) {
			return;
		}
//		if (submission.release) {
		// si release pas d'ecriture de run et si update, ne doit pas concerner les runs
		if (!submission.type.equals(Submission.Type.CREATION)) {
			return;
		}
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		// On accede au run via l'experiment:
		
		if (! submission.experimentCodes.isEmpty()) {	
			// ouvrir fichier en ecriture
			logger.debug("Creation du fichier " + outputFile);
			StringBuilder sb = new StringBuilder();
			sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n");
			sb.append("<RUN_SET>\n");
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
				String runCode = run.code;
				logger.debug("Ecriture du run " + runCode);
				sb.append("  <RUN alias=\"").append(runCode).append("\" ");
				if (StringUtils.isNotBlank(run.accession)) {
					sb.append(" accession=\"").append(run.accession).append("\" ");
				}
				
				//Format date
				sb.append("run_date=\"").append(formatter.format(run.runDate)).append("\"  run_center=\"").append(run.runCenter).append("\" ");
				sb.append(">\n");
				sb.append("    <EXPERIMENT_REF refname=\"").append(experimentCode).append("\"/>\n");
				sb.append("    <DATA_BLOCK>\n");
				sb.append("      <FILES>\n");
			
				for (RawData rawData: run.listRawData) {
					String fileType = rawData.extention;
					String collabFileName = rawData.collabFileName;
					
					fileType = fileType.replace(".gz", "");
					sb.append("        <FILE filename=\"").append(collabFileName).append("\" ").append("filetype=\"").append(fileType).append("\" checksum_method=\"MD5\" checksum=\"").append(rawData.md5).append("\">\n");
					if ( run.listRawData.size() == 2 ) {
						sb.append("          <READ_LABEL>F</READ_LABEL>\n");
						sb.append("          <READ_LABEL>R</READ_LABEL>\n");
					}
					sb.append("        </FILE>\n");
				}
				sb.append("      </FILES>\n");
				sb.append("    </DATA_BLOCK>\n");
				sb.append("  </RUN>\n");
			}
			sb.append("</RUN_SET>\n");
				
				try (BufferedWriter output_buffer = new BufferedWriter(new java.io.FileWriter(outputFile))) {
					output_buffer.write(sb.toString());
				}				
				submission.xmlRuns = outputFile.getName();
			}
		}
	

	public void writeSubmissionXml (Submission submission, File outputFile) throws IOException {
		if (submission == null) {
			return;
		}
		// ouvrir fichier en ecriture
		logger.debug("Creation du fichier " + outputFile);
		StringBuilder sb = new StringBuilder();
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n");
		sb.append("<SUBMISSION_SET>\n");
		sb.append("  <SUBMISSION alias=\"").append(submission.code).append("\" >\n");
		sb.append("    <CONTACTS>\n");
		sb.append("      <CONTACT name=\"william\" inform_on_status=\"william@genoscope.cns.fr\" inform_on_error=\"william@genoscope.cns.fr\"/>\n");
		sb.append("    </CONTACTS>\n");
		sb.append("    <ACTIONS>\n");
		sb.append("      <ACTION>\n");
		sb.append("        <HOLD/>\n");// en mode CREATION, soumission systematique en confidential 
		sb.append("      </ACTION>\n");

		if (StringUtils.isNotBlank(submission.studyCode)) {
			sb.append("      <ACTION>\n");
			sb.append("        <ADD source=\"study.xml\" schema=\"study\"/>\n");
			sb.append("      </ACTION>\n");
		}
		if (!submission.sampleCodes.isEmpty()){
			sb.append("      <ACTION>\n");
			sb.append("        <ADD source=\"sample.xml\" schema=\"sample\"/>\n");
			sb.append("      </ACTION>\n");
		}
		if (!submission.experimentCodes.isEmpty()){
			sb.append("      <ACTION>\n");
			sb.append("        <ADD source=\"experiment.xml\" schema=\"experiment\"/>\n");
			sb.append("      </ACTION>\n");
		}
		if (!submission.runCodes.isEmpty()){
			sb.append("      <ACTION>\n");
			sb.append("        <ADD source=\"run.xml\" schema=\"run\"/>\n");
			sb.append("      </ACTION>\n");
		}
		sb.append("    </ACTIONS>\n");		
		sb.append("  </SUBMISSION>\n");
		sb.append("</SUBMISSION_SET>\n");
		
		try (BufferedWriter output_buffer = new BufferedWriter(new java.io.FileWriter(outputFile))) {
			output_buffer.write(sb.toString());
		}
		submission.xmlSubmission = outputFile.getName();
	}

	public void writeSubmissionUpdateXml (Submission submission, File outputFile) throws IOException {
		if (submission == null) {
			return;
		}
		// ouvrir fichier en ecriture
		logger.debug("Creation du fichier " + outputFile);
		StringBuilder sb = new StringBuilder();
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n");
		sb.append("<SUBMISSION_SET>\n");
		sb.append("  <SUBMISSION alias=\"").append(submission.code).append("\" >\n");
		sb.append("    <CONTACTS>\n");
		sb.append("      <CONTACT name=\"william\" inform_on_status=\"william@genoscope.cns.fr\" inform_on_error=\"william@genoscope.cns.fr\"/>\n");
		sb.append("    </CONTACTS>\n");
		sb.append("    <ACTIONS>\n");
		sb.append("      <ACTION>\n");
		sb.append("        <HOLD/>\n");// en mode UPDATE, soumission systematique en confidential par securite
		sb.append("      </ACTION>\n");
		
		if (StringUtils.isNotBlank(submission.ebiProjectCode)) {
			sb.append("      <ACTION>\n");
			sb.append("        <MODIFY source=\"project.xml\" schema=\"project\"/>\n");
			sb.append("      </ACTION>\n");
		}
		if (StringUtils.isNotBlank(submission.studyCode)) {
			sb.append("      <ACTION>\n");
			sb.append("        <MODIFY source=\"study.xml\" schema=\"study\"/>\n");
			sb.append("      </ACTION>\n");
		}
		if (!submission.sampleCodes.isEmpty()){
			sb.append("      <ACTION>\n");
			sb.append("        <MODIFY source=\"sample.xml\" schema=\"sample\"/>\n");
			sb.append("      </ACTION>\n");
		}
		if (!submission.experimentCodes.isEmpty()){
			sb.append("      <ACTION>\n");
			sb.append("        <MODIFY source=\"experiment.xml\" schema=\"experiment\"/>\n");
			sb.append("      </ACTION>\n");
		}
		// pas d'ecriture de runs dans le cas d'un update sauf s'il faut refaire les md5 en cas de debugage
		// seul cas ou on renseignera submission.run.codes
		if (!submission.runCodes.isEmpty()){
			sb.append("      <ACTION>\n");
			sb.append("        <MODIFY source=\"run.xml\" schema=\"run\"/>\n");
			sb.append("      </ACTION>\n");
		}
		sb.append("    </ACTIONS>\n");		
		sb.append("  </SUBMISSION>\n");
		sb.append("</SUBMISSION_SET>\n");
		
		try (BufferedWriter output_buffer = new BufferedWriter(new java.io.FileWriter(outputFile))) {
			output_buffer.write(sb.toString());
		}
		submission.xmlSubmission = outputFile.getName();
	}

	
	
	public void writeSubmissionReleaseXml (Submission submission, File outputFile) throws IOException, SraException {
		if (submission == null) {
			throw new SraException("Aucune soumission en argument");
		}
		if (StringUtils.isBlank(submission.studyCode)) {
			throw new SraException("Impossible de faire la soumission " + submission.code + " pour releaser un study sans renseigner le champs submission.studyCode");
		}
		Study study = studyAPI.dao_getObject(submission.studyCode);	
		if (StringUtils.isBlank(study.accession)) {
			throw new SraException("Impossible de releaser le study " + study.code + " sans numeros d'accession");
		}

		// ouvrir fichier en ecriture
		logger.debug("Creation du fichier " + outputFile);
		StringBuilder sb = new StringBuilder();

		
		logger.debug("Ecriture du submission " + submission.code);
		
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n");
		sb.append("<SUBMISSION_SET>\n");
		sb.append("  <SUBMISSION alias=\"").append(submission.code).append("\" >\n");
		sb.append("    <CONTACTS>\n");
		sb.append("      <CONTACT  name=\"william\" inform_on_status=\"william@genoscope.cns.fr\" inform_on_error=\"william@genoscope.cns.fr\"/>\n");
		sb.append("    </CONTACTS>\n");			
		sb.append("    <ACTIONS>\n");
		sb.append("      <ACTION>\n");
		sb.append("         <RELEASE target=\"").append(study.accession).append("\"/>\n ");
		sb.append("      </ACTION>\n");
		sb.append("    </ACTIONS>\n");
		sb.append("  </SUBMISSION>\n");
		sb.append("</SUBMISSION_SET>\n");
		
		try (BufferedWriter output_buffer = new BufferedWriter(new java.io.FileWriter(outputFile))) {
			output_buffer.write(sb.toString());
		}
		submission.xmlSubmission = outputFile.getName();
	}

}
