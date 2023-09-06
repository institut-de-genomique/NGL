package sra.scripts;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import javax.inject.Inject;
import org.mongojack.DBQuery;
import fr.cea.ig.lfw.controllers.scripts.buffered.Script;
import fr.cea.ig.ngl.dao.api.sra.ExperimentAPI;
import models.sra.submit.sra.instance.Experiment;
import models.sra.submit.sra.instance.RawData;
import models.sra.submit.sra.instance.Run;
import models.sra.submit.util.SraException;
import services.Tools;


/*
 * Script a lancer avec un fichier des collabFileNames 
 * et le chemin du repertoire dans lequels les fichier de metadonnées (run.xml et submission.xml) doivent etre crées.
 * 
 * le fichier collabFileNames indiqué doit avoir un format ou le premier mot de chaque ligne correspond à un collabFileName.
 * Le mail d'erreur de l'EBI peut convenir mais il faut enlever le texte en debut et fin de mail ainsi que la légende de la forme :
 * FILE_NAME | ERROR | MD5 | FILE_SIZE | DATE | RUN_ID/ANALYSIS_ID 
 * * Script qui genere le fichier des runs correspondant aux collabFileName indiqués
 * 

 * Exemple de lancement :
 * http://appdev.genoscope.cns.fr:9005/sra/scripts/run/sra.scripts.WriteMetadataForCollabFileName?collabFileNames=/env/cns/tmp/sgas/fileProcessingErrors.txt&resultDirectory=/env/cns/submit_traces/SRA/SNTS_output_xml/NGL/FIX_TRANSFERT/2023_03_03
 *
 * @author sgas
 *
 */
// ok aucune erreur en PROD
public class WriteMetadataForCollabFileName extends Script<WriteMetadataForCollabFileName.Args> {
	
	private final ExperimentAPI     experimentAPI;
	
	private static final play.Logger.ALogger logger = play.Logger.of(WriteMetadataForCollabFileName.class);
	private String pattern_LineVide = "^\\s*$";   // pour ignorer les lignes sans caracteres visibles  
	private java.util.regex.Pattern plv = Pattern.compile(pattern_LineVide); 

	private String pattern_LineComment = "^\\s*#";   // pour ignorer les lignes de commentaire
	private java.util.regex.Pattern plc = Pattern.compile(pattern_LineComment);
		

	@Inject
	public WriteMetadataForCollabFileName (ExperimentAPI  experimentAPI) {
		this.experimentAPI = experimentAPI;
	}
	
	
	public static class Args {
		public String collabFileNames; 
		// Chemin complet du fichier des collabFileName pour lesquels le fichier run.xml doit etre generé
		public String resultDirectory;

	}

	public List<String> parseUserFile(String fileName) {
		//println("Dans parseUserFile");
		InputStream inputStream;
		List<String> collabFileNames = new ArrayList<String>();
		try {
			inputStream = new FileInputStream(fileName);
		} catch (FileNotFoundException e1) {
			throw new SraException("", e1);
		}
		String ligne;

		try (BufferedReader input_buffer = new BufferedReader(new InputStreamReader(inputStream))) {
			while ((ligne = input_buffer.readLine()) != null) {	
				Matcher mlv = plv.matcher(ligne);
				Matcher mlc = plc.matcher(ligne);
				if ( mlv.find() ) { // si ligne vide, ignorer la ligne
					//logger.debug("!!!!!!!!!!!!!!!!!ligne vide :" + ligne);
					continue;
				}
				if ( mlc.find() ) { // si ligne de commentaires, ignorer la ligne
					//logger.debug("!!!!!!!!!!!!!!!!!commentaires ignores :" + ligne);
					continue;
				}

				String collabFileName = Tools.clean(ligne);
				collabFileName = collabFileName.replaceAll("\\s+.*", ""); // prendre uniquement premiere colonne du fichier qui doit correspondre au nom de fichier
				collabFileName = collabFileName.replaceAll("[^/]*/", "");  // prendre uniquement chemin relatif du fichier si chemin complet indiqué
				if(!collabFileNames.contains(collabFileName)) {
					collabFileNames.add(collabFileName);
				}	
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new SraException("", e);
		}
		return collabFileNames;
	}

	public void writeRunXml (Set<String>experimentCodes, File outputFile) throws IOException, SraException {

		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		// On accede au run via l'experiment:
		
		if (! experimentCodes.isEmpty()) {	
			StringBuilder sb = new StringBuilder();
			sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n");
			sb.append("<RUN_SET>\n");
			for (String experimentCode : experimentCodes){
				// Recuperer objet experiment dans la base :
//				Experiment experiment = MongoDBDAO.findByCode(InstanceConstants.SRA_EXPERIMENT_COLL_NAME, models.sra.submit.sra.instance.Experiment.class, experimentCode);
				Experiment experiment = experimentAPI.get(experimentCode);
				if (experiment == null) {
					throw new SraException("experiment impossible à recuperer dans base :"+ experimentCode);
				}
				Run run = experiment.run;
				if (run == null){
					throw new SraException("run impossible à recuperer dans objet experiment:"+ experimentCode);
				}
				String runCode = run.code;
				//logger.debug("Ecriture du run " + runCode);
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
			//println(sb.toString());
			logger.debug("Creation du fichier " + outputFile);
			try (BufferedWriter output_buffer = new BufferedWriter(new java.io.FileWriter(outputFile))) {
				output_buffer.write(sb.toString());
			}			
		}
	}
	
	public void writeSubmissionUpdateXml (String submissionCode, File outputFile) throws IOException {
		// ouvrir fichier en ecriture
		//logger.debug("Creation du fichier " + outputFile);
		StringBuilder sb = new StringBuilder();
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n");
		sb.append("<SUBMISSION_SET>\n");
		sb.append("  <SUBMISSION alias=\"").append(submissionCode).append("\" >\n");
		sb.append("    <CONTACTS>\n");
		sb.append("      <CONTACT name=\"william\" inform_on_status=\"william@genoscope.cns.fr\" inform_on_error=\"william@genoscope.cns.fr\"/>\n");
		sb.append("    </CONTACTS>\n");
		sb.append("    <ACTIONS>\n");
		
		// Par defaut on met l'action hold pour le reste de la soumission
		sb.append("      <ACTION>\n");
		sb.append("        <HOLD/>\n");// en mode UPDATE, soumission systematique en confidential par securite 
		sb.append("      </ACTION>\n");
		
		sb.append("      <ACTION>\n");
		sb.append("        <MODIFY source=\"run.xml\" schema=\"run\"/>\n");
		sb.append("      </ACTION>\n");
		
		sb.append("    </ACTIONS>\n");		
		sb.append("  </SUBMISSION>\n");
		sb.append("</SUBMISSION_SET>\n");
		
		try (BufferedWriter output_buffer = new BufferedWriter(new java.io.FileWriter(outputFile))) {
			output_buffer.write(sb.toString());
		}
	}

	
	@Override
	public void execute(Args args) throws Exception {
		List<String> collabFileNames = parseUserFile(args.collabFileNames);
		boolean error = false;
		Set<String> uniqExperimentCodes = new HashSet<String>();
		
		// verifier que pour chaque collabFileName on a bien un et un seul experiment :
		for (String collabFileName : collabFileNames) {
			//println("collabFileName = " + collabFileName);
			List <Experiment> experimentList = experimentAPI.dao_find(DBQuery.is("run.listRawData.collabFileName", collabFileName)).toList();
			if(experimentList.size() <= 0) {
				logger.debug("Aucun experiment avec rawData.collabFileName=" + collabFileName);
				println("Aucun experiment avec rawData.collabFileName=" + collabFileName);
				error = true;
				continue;	
			} else if (experimentList.size() > 1) {
				String mess = "";
				for (Experiment experiment : experimentList) {
					mess = mess + experiment.code + ", ";
				}				
				int endIndex   = mess.lastIndexOf(",");
				mess = mess.substring(0, endIndex);
				logger.debug("plusieurs experiment avec rawData.collabFileName=" + collabFileName + " => " + mess );
				error = true;
				continue;
			} else { // bon cas
				if (! uniqExperimentCodes.contains(experimentList.get(0).code)) {
					uniqExperimentCodes.add(experimentList.get(0).code);
				}
			}
		
		}
		if(error) {
			throw new Exception ("Erreurs voir logger");
		}
		println("nbre d'experiment code= "+ uniqExperimentCodes.size());
		String resultDirectory 		 = args.resultDirectory;
		String fileSep = File.separator;
		String pattern = "\\" + fileSep + "\\s*$";
		resultDirectory = resultDirectory.replaceFirst(pattern, ""); // oter / terminal si besoin
		File runFile = new File(resultDirectory + fileSep + "run.xml");
		File submissionFile = new File(resultDirectory +  fileSep + "submission.xml");
		pattern = "[^" + fileSep + "]+" + fileSep;
		String submissionName = "GSC_FixTransfert_" + resultDirectory.replaceAll(pattern, "");// on conserve nom du dernier sous-repertoire
		pattern = fileSep;
		submissionName = submissionName.replaceAll(pattern, ""); 
		writeRunXml(uniqExperimentCodes, runFile);
		writeSubmissionUpdateXml(submissionName, submissionFile);
		println("fin du traitement, voir fichiers dans " + resultDirectory);

	}


}
