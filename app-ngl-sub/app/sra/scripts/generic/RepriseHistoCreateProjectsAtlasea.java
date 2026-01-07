package sra.scripts.generic;

import static ngl.refactoring.state.SRASubmissionStateNames.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import controllers.sra.api.AbstractStudyAPI;
import controllers.sra.api.ProjectAPI;
import controllers.sra.api.SubmissionAPI;
import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.buffered.Script;
import fr.cea.ig.lfw.utils.Iterables;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.NGLConfig;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.AnalysisAPI;
import fr.cea.ig.ngl.dao.sra.AtlaseaDAO;
import mail.MailServiceException;
import mail.MailServices;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.run.instance.Analysis;
import models.laboratory.sample.instance.Sample;
import models.sra.submit.sra.instance.Atlasea;
import models.sra.submit.sra.instance.EmailAlert;
import models.sra.submit.sra.instance.Experiment;
import models.sra.submit.sra.instance.Project;
import models.sra.submit.sra.instance.Study;
import models.sra.submit.sra.instance.Submission;
import models.sra.submit.sra.instance.Submission.TypeRawDataSubmitted;
import models.utils.InstanceConstants;
import ngl.refactoring.state.SRASubmissionStateNames;
import play.data.validation.ValidationError;
import services.sra.EbiAPI;
import services.sra.SraException;
import services.sra.VariableSRA;
import services.taxonomy.TaxonomyServices;
import sra.api.SubmissionNewAPI;
import sra.api.SubmissionNewAPITools;
import sra.services.EbiFileResponseServices;
import sra.services.EbiRetour;
import sra.services.XmlServices;
import validation.ContextValidation;

//http://localhost:9000/sra/scripts/run/sra.scripts.generic.RepriseHistoCreateProjectsAtlasea?pathFileProjectExist=C:\Users\sgas.IBFJ-EVRY\mesDocs\Liste_PRJEB_Tolid_27_02_2025.txt
public class RepriseHistoCreateProjectsAtlasea extends Script<RepriseHistoCreateProjectsAtlasea.MyParam>{

	private final AnalysisAPI             biAnalysisAPI;


	@SuppressWarnings("unused")
	private static final play.Logger.ALogger logger = play.Logger.of(RepriseHistoCreateProjectsAtlasea.class);

	private String pattern_LineVide = "^\\s*$";   // pour ignorer les lignes sans caracteres visibles  
	private java.util.regex.Pattern plv = Pattern.compile(pattern_LineVide); 

	private String pattern_LineComment = "^\\s*#";   // pour ignorer les lignes de commentaire
	private java.util.regex.Pattern plc = Pattern.compile(pattern_LineComment);
	
	private String pattern_LineInteret = "^\\s*(\\S+)\\s+(\\S+)";   // pour recuperer 
	private java.util.regex.Pattern pli = Pattern.compile(pattern_LineInteret);
	private final String user = "ngsrg";
	
	@Inject
	public RepriseHistoCreateProjectsAtlasea (  NGLApplication app,
			AnalysisAPI             biAnalysisAPI) {
		this.biAnalysisAPI           = biAnalysisAPI;
	}

	// ma structure de controle et stockage des arguments de l'url
	public static class MyParam {
		public String pathFileProjectExist;
	}
 
	@Override
	public void execute(MyParam args) throws Exception {
		Map<String, String> map = parseUserFile(args.pathFileProjectExist);
		int cp = 0;
		for (String tolid : map.keySet()) {
			 println("tolid: "+ tolid + " | valeur: " + map.get(tolid));
			 // Pour chaque cle mettre l'analyse à jour :
			 if(! biAnalysisAPI.isObjectExist(DBQuery.and(
					 DBQuery.exists("properties.tolid"),
					 DBQuery.exists("properties.tolid.value"),
					 DBQuery.is("properties.tolid.value", tolid)))) {
				 println("Absence de l'analyse");
			 } else {
				 Analysis analysis = biAnalysisAPI.getObject(DBQuery.and(
						 DBQuery.exists("properties.tolid"),
						 DBQuery.exists("properties.tolid.value"),
						 DBQuery.is("properties.tolid.value", tolid)));
				 String analysisTolid = (String) analysis.properties.get("tolid").value;
				 println("analyse en base " + analysis.code + " avec tolId=" + analysisTolid + " et tolid_demande=" + tolid);
				 // ajout de la nouvelle property pour primaryAssemblyProjectAccession :
				 if (analysis.properties == null) {
					 analysis.properties = new HashMap<String,PropertyValue>();
				 }
				 if ( !analysis.properties.containsKey("primaryAssemblyProjectAccession") ) {
					analysis.properties.put("primaryAssemblyProjectAccession", new PropertySingleValue(map.get(tolid)));
				 }  
				 analysis.traceInformation.modifyDate = new Date();
				 analysis.traceInformation.modifyUser = this.user;
				 ContextValidation ctx = ContextValidation.createUpdateContext(this.user);
				 try {
					 //analysis.validate(ctx);
				 } catch (Exception e) {
					println("ERREUR="+e.getMessage());
				}
				 if(ctx.hasErrors()) {
					 println("ERREUR pour analysis"  + analysis.code);
					 continue;
				 }
				 cp++;
				 println("sauvegarde en base de l'analyse " + analysis.code);
				 biAnalysisAPI.dao_saveObject(analysis);
			 }
		}
		println("Fin du traitement des " + cp + " tolid");
		
	}

	// parse fichier sur 2 colonnes.
	// Renvoie map avec cle = col2 et valeur = col1
	public Map<String, String> parseUserFile(String fileName) throws Exception {
		//println("Dans parseUserFile");
		Map<String, String> map = new HashMap<>();
		//List<String> collabFileNames = new ArrayList<String>();
		try ( InputStream inputStream = new FileInputStream(fileName); 
			  BufferedReader input_buffer = new BufferedReader(new InputStreamReader(inputStream))) {
			String ligne;
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
					String col1 = mli.group(1);
					String col2 = mli.group(2);			
					if(!map.containsKey(col2)) {
						map.put(col2, col1);
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
		return map;
	}


}
