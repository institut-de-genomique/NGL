package scripts;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptWithArgsAndExcelBody;
import fr.cea.ig.ngl.NGLApplication;
import models.laboratory.common.instance.Comment;
import models.laboratory.sample.instance.Sample;
import models.utils.InstanceConstants;
import play.libs.Json;
import validation.ContextValidation;

//http://localhost:9000/scripts/run/scripts.UpdateRefCollab?jira=SUPSQ-5243
//en indiquant dans le body,un fichier Excel avec header de 2 colonnes : colonne 1  avec codeSample et colonne 2 avec NouvelRefCollab
//Attention a ne pas mettre de valeurs dans la premiere ligne reservée aux intitulées des colonnes.
public class UpdateRefCollab extends ScriptWithArgsAndExcelBody <UpdateRefCollab.Args> {

	//private static final play.Logger.ALogger logger = play.Logger.of(UpdateRefCollab.class);
	private final NGLApplication    app;

	@Inject
	public UpdateRefCollab(NGLApplication app) {
		this.app = app;
	}
	
	public static class Args {
		public String jira;
	}

	/**
	 * Script permettant de modifier les refCollab d'un ensemble d'echantillon CNS ou CNG
	 * Ce script va appeler la validation du sample qui vérifiera les règles de nomenclature de la refCollab qui est pour rappel
	 * Regex=^[\\.A-Za-z0-9_-]+$ max caractères=25
	 * Prend en entrée un fichier excel au format suivant 
	 * CodeSample NouvelRefCollab
	 * ex de lancement :
	 * http://localhost:9000/scripts/run/scripts.UpdateRefCollab?jira=SUPSQ-5243
	 */
	
	public class SampleInfos  {
		String  refCollab;
		String  sampleCode;
		Sample  sample;
	}
	
	public List<SampleInfos> parseWorkbook(XSSFWorkbook workbook) throws Exception {
		List<SampleInfos> samplesInfos = new ArrayList<SampleInfos>();
		
		workbook.getSheetAt(0).rowIterator().forEachRemaining(row -> {
			if(row.getRowNum() == 0) return; // skip header
				if(row != null && row.getCell(0) != null && row.getCell(1) != null) {
					String sampleCode = row.getCell(0).getStringCellValue();
					String userRefCollab = row.getCell(1).getStringCellValue();
					SampleInfos sampleInfos = new SampleInfos();
					sampleInfos.sampleCode = sampleCode;
					sampleInfos.refCollab = userRefCollab;
					samplesInfos.add(sampleInfos); 
				} 
			}
		);
		return samplesInfos;
	}
	
	
	@Override
	public void execute(Args args, XSSFWorkbook workbook) throws Exception {
		ContextValidation ctx = ContextValidation.createUpdateContext("ngl-support");
		//NGL-4111
		if ( ! args.jira.matches("^(SUPSQ|SUPSQCNG|NGL)-\\d+$") ) {
			throw new RuntimeException("argument jira " +  args.jira + " qui n'a pas la forme attendue SUPSQ-XXX ou SUPSQCNG-XXX ou NGL-XXX");
		}
		
		String ticket = args.jira;

		// parser le fichier d'entree pour construire les association sampleCode, userRefCollab et construire la liste des userRefCollab
		List<SampleInfos> list_samplesInfos =  parseWorkbook(workbook);
		Map<String, SampleInfos> mapSamplesInfos = new HashMap<String, SampleInfos>(); // new HashMap<sampleCode, sampleInfos>
		for (SampleInfos sampleInfos : list_samplesInfos) {
			if (mapSamplesInfos.containsKey(sampleInfos.sampleCode)) {
				if(! mapSamplesInfos.get(sampleInfos.sampleCode).refCollab.equals(sampleInfos.refCollab)) {
					// duplication du sample code avec incoherence sur le refCollab :
					ctx.addError("sampleCode " +  sampleInfos.sampleCode, " duplication des sampleCode avec userRefCollab differents " + mapSamplesInfos.get(sampleInfos.sampleCode).refCollab + " et " + sampleInfos.refCollab);
				} else {
					// duplication mais pas d'incoherence
				}
			} else {
				mapSamplesInfos.put(sampleInfos.sampleCode, sampleInfos);
			}
		}
		
		// Recuperer les samples dans la base de données et les mettre à jour pour refCollab, modifyUser et modifyDate  et technicalCommentaire
		for (Iterator<Entry<String, SampleInfos>> iterator = mapSamplesInfos.entrySet().iterator(); iterator.hasNext();) {
			  Entry<String, SampleInfos> entry = iterator.next();
			  String sampleCode = entry.getKey();
			  String userRefCollab = entry.getValue().refCollab;
			  Sample sample = MongoDBDAO.findByCode(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, sampleCode);

			  if (sample==null) {
				  ctx.addError("sampleCode " + sampleCode, " n'existe pas dans la base");
				  continue;
			  }
			  String oldRefCollab = sample.referenceCollab;
			  String stComment = ticket;
			  if (StringUtils.isNotBlank(oldRefCollab)) {
				  stComment = ticket + " old refCollab : "  + oldRefCollab;
				  if (oldRefCollab.equals(userRefCollab)) {
					  println("pour le sampleCode " + sampleCode + " : Remplacement du refCollab " +  oldRefCollab + " par userRefCollab " + userRefCollab + " ignore");
					  iterator.remove();
					  continue;
				  }
			  }
			  if(sample != null) {
				  sample.referenceCollab = userRefCollab;
				  sample.traceInformation.modifyUser="ngl-support";
				  sample.traceInformation.modifyDate=new Date();
				  if (sample.technicalComments == null) {
					  sample.technicalComments =  new ArrayList<Comment>(); 
				  } 
				  sample.technicalComments.add(new Comment(stComment, "ngl-support", true));
			  } 
			  entry.getValue().sample = sample;
		} // end for
		

		// verifier validide des samples et si sample n'a pas de parent :
		for (Iterator<Entry<String, SampleInfos>> iterator = mapSamplesInfos.entrySet().iterator(); iterator.hasNext();) {
			  Entry<String, SampleInfos> entry = iterator.next();
			  String sampleCode = entry.getKey();
			  Sample sample = entry.getValue().sample;
			  if (sample == null) {
					//ctx.addError("sample " + sampleCode, " absent de la base"); // deja mis plus haut dans ctx
			  } else {
				  if(sample.life != null && sample.life.from != null && StringUtils.isNotBlank(sample.life.from.sampleCode) ) {
					ctx.addError("sample " + sampleCode, " a un parent " + sample.life.from.sampleCode +  " et n'est pas modifiable pour referenceCollab (modifier le parent)");
				  }
				  sample.validate(ctx);
			  }
		}
		// Faire mise à jour des samples si aucune erreur
		if(ctx.hasErrors()) {
			println("ERRORS:");
			//ctx.displayErrors(logger, "debug");
			println(Json.prettyPrint(app.errorsAsJson(ctx.getErrors())));
			println("Fin du traitement : Aucune modification des samples dans la base");
		} else {
			// mise à jour de la base pour tous les samples:
			println("Mise à jour de la base pour les samples");
			int cp = 0;
			for (Iterator<Entry<String, SampleInfos>> iterator = mapSamplesInfos.entrySet().iterator(); iterator.hasNext();) {
				Entry<String, SampleInfos> entry = iterator.next();
				cp++;
				Sample sample = entry.getValue().sample;			
				MongoDBDAO.update(InstanceConstants.SAMPLE_COLL_NAME, sample);
			}
			println("Fin du traitement : update de " + cp + " samples dans la base");
		}
	}

}
