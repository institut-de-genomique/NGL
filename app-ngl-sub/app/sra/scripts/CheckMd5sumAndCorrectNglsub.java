package sra.scripts;

import java.io.BufferedReader;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;

import org.mongojack.DBQuery;

import fr.cea.ig.lfw.controllers.scripts.buffered.Script;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.dao.api.sra.ExperimentAPI;
import models.sra.submit.sra.instance.Experiment;
import models.sra.submit.sra.instance.RawData;
import models.sra.submit.util.SraException;
import play.libs.Json;
import validation.ContextValidation;


/*
 * Script a lancer avec un fichier md5sum des collabFileNames.
 * Script qui verifie et/ou corriger les md5sum dans ngl_sub
 * 
 * Exemple de lancement pour verifier les md5sum sans corriger la base ngl-sub:
 * http://appdev.genoscope.cns.fr:9005/sra/scripts/run/sra.scripts.CheckMd5sumAndCorrectNglsub?md5File=/env/cns/submit_traces/SRA/SNTS_output_xml/NGL/FIX_TRANSFERT/2023-03-02/md5.txt&corrige_ngl_sub=false
 * localhost/sra/scripts/run/sra.scripts.CheckMd5sumAndCorrectNglsub?md5File=C:\Users\sgas\debug\md5.txt&corrige_ngl_sub=false

 * Exemple de lancement pour verifier les md5sum et corriger la base ngl-sub:
 * http://appdev.genoscope.cns.fr:9005/sra/scripts/run/sra.scripts.CheckMd5sumAndCorrectNglsub?md5File=/env/cns/submit_traces/SRA/SNTS_output_xml/NGL/FIX_TRANSFERT/2023-03-02/md5.txt&corrige_ngl_sub=true 
 * @author sgas
 *
 */

public class CheckMd5sumAndCorrectNglsub extends Script<CheckMd5sumAndCorrectNglsub.Args> {
	
	private final ExperimentAPI     experimentAPI;
	private final NGLApplication    app;
	
	private static final play.Logger.ALogger logger = play.Logger.of(CheckMd5sumAndCorrectNglsub.class);
	private String pattern_LineVide = "^\\s*$";   // pour ignorer les lignes sans caracteres visibles  
	private java.util.regex.Pattern plv = Pattern.compile(pattern_LineVide); 

	private String pattern_LineComment = "^\\s*#";   // pour ignorer les lignes de commentaire
	private java.util.regex.Pattern plc = Pattern.compile(pattern_LineComment);
	
	private String pattern_LineInteret = "^\\s*(\\S+)\\s+(\\S+)";   // pour recuperer md5sum calculé et collabFileName
	private java.util.regex.Pattern pli = Pattern.compile(pattern_LineInteret);
	private final String user = "cnsnglapps";

	@Inject
	public CheckMd5sumAndCorrectNglsub (NGLApplication app,
				    	 ExperimentAPI  experimentAPI
				    	 ) {
		this.app           = app;
		this.experimentAPI = experimentAPI;

	}
	
	
	public static class Args {
		public String md5File; 
		// Chemin complet du fichier des collabFileName pour lesquels le md5sum doit etre recalculé et mis dans NGL-SUB
		//public String filePathMd5sum;
		public boolean corrige_ngl_sub;

	}

	public Map<String, String> parseUserFile(String fileName) throws Exception {
		//println("Dans parseUserFile");
		
		Map<String, String> collabFileNamesMd5 = new HashMap<>();
		InputStream inputStream;
		//List<String> collabFileNames = new ArrayList<String>();
		try {
			inputStream = new FileInputStream(fileName);
		} catch (FileNotFoundException e1) {
			throw new SraException("Absence du fichier " + fileName + " sur les disques", e1);
		}
		String ligne;
		try (BufferedReader input_buffer = new BufferedReader(new InputStreamReader(inputStream))) {
			while ((ligne = input_buffer.readLine()) != null) {	
				Matcher mlv = plv.matcher(ligne);
				Matcher mlc = plc.matcher(ligne);
				Matcher mli =  pli.matcher(ligne);
				if ( mlv.find() ) { // si ligne vide, ignorer la ligne
					//logger.debug("!!!!!!!!!!!!!!!!!ligne vide :" + ligne);
					continue;
				} else if ( mlc.find() ) { // si ligne de commentaire, ignorer la ligne
					//logger.debug("!!!!!!!!!!!!!!!!!ligne commentaire :" + ligne);
					continue;
				} else if ( mli.find() ) { // si ligne d'interet
					logger.debug("!!!!!!!!!!!!!!!!! ligne d'interet :" + ligne);
					String md5 = mli.group(1);
					String collabFileName = mli.group(2);
					//collabFileName= Tools.clean(collabFileName);
			
					collabFileName = collabFileName.replaceAll("[^/]*/", "");  // prendre uniquement chemin relatif du fichier si chemin complet indiqué

					if(!collabFileNamesMd5.containsKey(collabFileName)) {
						collabFileNamesMd5.put(collabFileName, md5);
					}	
				} else {
					logger.debug("!!!!!!!!!!!!!!!!! ligne qui ne repond pas au format attendu  :" + ligne);
					throw new Exception("ligne qui ne repond pas au format attendu  :\" + ligne");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new SraException("", e);
		}
		return collabFileNamesMd5;
	}

	// methode utilisée par autre script mais difficile de factoriser car utilisation de println
	// retourne true si ok et false sinon
	public boolean checkCollabFileNamesInNglsub(Set<String> collabFileNames)  throws Exception {
		boolean ok = true;
		// verifier que pour chaque collabFileName on a bien un et un seul experiment :
		for (String collabFileName : collabFileNames) {
			//println("collabFileName = " + collabFileName);
			List <Experiment> experimentList = experimentAPI.dao_find(DBQuery.is("run.listRawData.collabFileName", collabFileName)).toList();
			if(experimentList.size() <= 0) {
				logger.debug("Aucun experiment avec rawData.collabFileName=" + collabFileName);
				println("Aucun experiment avec rawData.collabFileName=" + collabFileName);
				ok = false;
				continue;	
			} else if (experimentList.size()>1) {
				String mess = "";
				for (Experiment experiment : experimentList) {
					mess = mess + experiment.code + ", ";
				}				
				int endIndex   = mess.lastIndexOf(",");
				mess = mess.substring(0, endIndex);
				logger.debug("plusieurs experiment avec rawData.collabFileName=" + collabFileName + " => " + mess );
				println("plusieurs experiment avec rawData.collabFileName=" + collabFileName + " => " + mess );
				ok = false;
			} else {
				// rien : cas normal
			}
		}
		return ok;
	}
	
	
	@Override
	public void execute(Args args) throws Exception {
		String errorMess = "Probleme de md5sum dans la base pour : \n";
		String okMess = "md5sum dans la base ok pour : \n";
		println ("corrige_ngl_sub = " + args.corrige_ngl_sub);
		Map<String, String> collabFileNamesMd5 = parseUserFile(args.md5File);
		Set<String> collabFileNames = collabFileNamesMd5.keySet();
		if ( ! checkCollabFileNamesInNglsub(collabFileNames)) {
			throw new Exception ("Erreurs voir logger");
		}
		
		for (String collabFileName : collabFileNames) {
			List <Experiment> experimentList = experimentAPI.dao_find(DBQuery.is("run.listRawData.collabFileName", collabFileName)).toList();
			for (Experiment experiment : experimentList) {				
				for (RawData rawData : experiment.run.listRawData) {
					if(rawData.collabFileName.equals(collabFileName)) {
						String md5Hex = collabFileNamesMd5.get(collabFileName);
						String runAC = "NONE";
						if (StringUtils.isNotBlank(experiment.run.accession)) {
							runAC = experiment.run.accession;
						}
						//printfln("md5Sum calcule %s pour collabFileName=%s avec AC=%s", md5Hex, rawData.collabFileName, runAC);
						if (rawData.md5.equals(md5Hex)) {
							okMess = okMess + "relatifName=" + rawData.relatifName + " (collabFileName= " + rawData.collabFileName + ")  du readset " + experiment.readSetCode + "\n";
						} else {
							errorMess = errorMess + "relatifName=" + rawData.relatifName + " (collabFileName= " + rawData.collabFileName + ")  du readset " + experiment.readSetCode + "\n";
							// correction du md5sum dans NGL-SUB :
							rawData.md5= md5Hex;
							ContextValidation ctxVal = ContextValidation.createUpdateContext(user);
							experiment.validate(ctxVal);
							if(args.corrige_ngl_sub) {
								if(ctxVal.hasErrors()) {
									ctxVal.displayErrors(logger, "debug");
									println("Error pour " + experiment.code);
									println(Json.prettyPrint(app.errorsAsJson(ctxVal.getErrors())));				
								} else {
									experimentAPI.dao_saveObject(experiment);				
								}
							}
						}
					}
				}
			}
		}
		println(okMess + "\n");
		println(errorMess + "\n");
		println("Fin du traitement");
		
	}


}
