package sra.scripts;

import java.io.BufferedReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;

import org.mongojack.DBQuery;

import fr.cea.ig.lfw.controllers.scripts.buffered.Script;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.dao.api.sra.ExperimentAPI;
import models.sra.submit.sra.instance.Experiment;
import models.sra.submit.sra.instance.RawData;
import models.sra.submit.sra.instance.UserExperimentExtendedType;
import models.sra.submit.util.SraException;
import play.libs.Json;
import services.Tools;
import validation.ContextValidation;


/*
 * Script a lancer pour recalculer les md5sum des collabFileName indiqués dans le fichier d'entrée et verifier et/ou corriger les md5sum dans ngl_sub
 * Exemple de lancement pour verifier les md5sum sans corriger la base ngl-sub:
 * http://appdev.genoscope.cns.fr:9005/sra/scripts/run/sra.scripts.ReloadMd5sum?inputFile=/env/cns/tmp/collabFileNameNoMd5.txt&user=sgas&corrige_ngl_sub=false
 * Exemple de lancement pour verifier les md5sum et corriger la base ngl-sub:
 * http://appdev.genoscope.cns.fr:9005/sra/scripts/run/sra.scripts.ReloadMd5sum?inputFile=/env/cns/tmp/collabFileNameNoMd5.txt&user=sgas&corrige_ngl_sub=true
 * script deprecated car calcul en direct des md5sum trop long => passer par ReloadMd5sum.java, qui prend en entree un fichier avec les md5 sum deja calculés.
 * 
@author sgas
 *
 */
// ok aucune erreur en PROD
@Deprecated 
public class ReloadMd5sumOld extends Script<ReloadMd5sumOld.Args> {
	
	private final ExperimentAPI experimentAPI;
	private final NGLApplication    app;

	private static final play.Logger.ALogger logger = play.Logger.of(ReloadMd5sumOld.class);
	private String pattern_LineVide = "^\\s*$";   // pour ignorer les lignes sans caracteres visibles  
	private java.util.regex.Pattern plv = Pattern.compile(pattern_LineVide); 

	private String pattern_LineComment = "^\\s*#";   // pour ignorer les lignes de commentaire
	private java.util.regex.Pattern plc = Pattern.compile(pattern_LineComment);
	

	@Inject
	public ReloadMd5sumOld (NGLApplication app,
				    	 ExperimentAPI  experimentAPI) {
		this.app           = app;
		this.experimentAPI = experimentAPI;

	}
	
	
	public static class Args {
		public String inputFile; 
		// Chemin complet du fichier des collabFileName pour lesquels le md5sum doit etre recalculé et mis dans NGL-SUB
		//public String filePathMd5sum;
		public String user;
		public boolean corrige_ngl_sub;

	}

	public List<String> parseUserFile(String fileName) {
		//println("Dans parseUserFile");
		
		Map<String, UserExperimentExtendedType> mapUserExperiment = new HashMap<>();
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
				collabFileName = collabFileName.replaceAll("[^/]/", "");  // prendre uniquement chemin relatif du fichier si chemin complet indiqué
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


	@Override
	public void execute(Args args) throws Exception {
		println ("corrige_ngl_sub = " + args.corrige_ngl_sub);
		List<String> collabFileNames = parseUserFile(args.inputFile);
		for (String collabFileName : collabFileNames) {
			//println("collabFileName = " + collabFileName);
			List <Experiment> experimentList = experimentAPI.dao_find(DBQuery.is("run.listRawData.collabFileName", collabFileName)).toList();
			if(experimentList.size() <= 0) {
				logger.debug("Aucun experiment avec rawData.collabFileName=" + collabFileName);
				println("Aucun experiment avec rawData.collabFileName=" + collabFileName);
				continue;	
			}
			if (experimentList.size()>1) {
				String mess = "";
				for (Experiment experiment : experimentList) {
					mess = mess + experiment.code + ", ";
				}				
				int endIndex   = mess.lastIndexOf(",");
				mess = mess.substring(0, endIndex);
				logger.debug("plusieurs experiment avec rawData.collabFileName=" + collabFileName + " => " + mess );
			}
			
			for (Experiment experiment : experimentList) {
				for (RawData rawData : experiment.run.listRawData) {
					if(rawData.collabFileName.equals(collabFileName)) {
						File fileCible = new File(rawData.directory + File.separator + rawData.relatifName);
						String md5Hex = DigestUtils.md5Hex((InputStream) new FileInputStream(fileCible));
						String runAC = "NONE";
						if (StringUtils.isNotBlank(experiment.run.accession)) {
							runAC = experiment.run.accession;
						}
						printfln("md5Sum calcule %s pour collabFileName=%s avec AC=%s", md5Hex, rawData.collabFileName, runAC);
						if (rawData.md5.equals(md5Hex)) {
							printfln("OK md5sum pour relatifName=%s (ou collabFileName=%s) du readset %s", rawData.relatifName, rawData.collabFileName,experiment.readSetCode);
						} else {
							printfln("ERROR md5sum pour relatifName=%s (ou collabFileName=%s) du readset %s", rawData.relatifName, rawData.collabFileName,experiment.readSetCode);
							// correction du md5sum dans NGL-SUB :
							rawData.md5= md5Hex;
							ContextValidation ctxVal = ContextValidation.createUpdateContext(args.user);
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
		
	}




	


}
