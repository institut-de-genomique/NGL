package scripts;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptWithArgsAndExcelBody;
import fr.cea.ig.ngl.NGLApplication;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.run.instance.ReadSet;
import models.utils.InstanceConstants;
import play.Logger;
import play.data.validation.ValidationError;
import play.libs.Json;
import rules.services.RulesServices6;
import validation.ContextValidation;



/**
 * <strong>Script permettant d'ajouter des refCollabSub sur des readset valides au CNS (rs.validate() sans erreur)</strong>
 * <br><br>
 * <strong>Il prend en entrée</strong><br> 
 * <ul>
 * <li> l'argument force : <strong>qui doit pratiquement toujours etre à false</strong> </li>
 * <li> l'argument pathFileOut : chemin complet du fichier de sortie sans extention 
 * (ou avec extention '.xls' et non '.xlsx'), qui recapitule les updates dans la base. </li>
 * <li> dans le body, le fichier des données excel, avec l'extention '.xlsx', et avec dans le premier onglet,  
 * sur la première ligne, l'intitulé des colonnes 'ReadsetCode | refCollabSub' </li>
 * </ul>
 * <br>
 * <strong>Il génère un fichier de sortie exel avec le recapitulatif des modifications en base.</strong> 
 * <br><br>
 * <p>
 * <strong>Avec l'argument force=false, </strong>, le script interdit d'inserer des refCollabSub sur des readsets invalides (rs.validate() avec error),
 * soumis ou en cours de soumission. Si les messages d'erreurs indiquent que les readsets sont soumis ou en cours de soumission (submission.state.value != NONE), 
 * il faut évaluer les mises à jour à faire dans NGL-SUB
 * (mise à jours des experiments de soumission avec modifications des samples de soumission, ou suppression de la 
 * soumission si les readsets n'ont pas encore obtenus de numéros d'accession) avant de modifier les refCollabSub dans NGLBI. 
 * <br><br>
 * 
 * <strong>Avec l'argument force=true, </strong> le script interdit d'inserer des refCollabSub sur des readsets invalides (rs.validate() avec error), 
 * mais il permet d'inserer des refCollabSub sur des readsets déjà soumis, ou en cours de soumission. 
 * Avec force=true, les readsets qui sont soumis ou en cours de soumission sont signalées sans generer d'erreur.
 * <br><br>
 * <strong>L'argument force=true, </strong> ne doit etre utilisé que si l'utilisateur a évalué les modifications à faire dans NGL-SUB et à l'EBI.
 * <br><br>
 * <strong>Si la refCollabSub ne répond pas au pattern autorisé,</strong> le script déclenchera toujours une erreur 
 * et ne fera aucune insertion en base.
 * <br><br>
 * <strong> Exemples de lancement avec curl :</strong>
 * <br><br>
 *  {@code curl --user-agent "bot"  -H 'Content-Type: multipart/form-data' 
 *  -v "http://appuatisoprod.genoscope.cns.fr:9104/scripts/run/scripts.UpdateRefCollabSubCNS?
 *  force=false&pathFileOut=/env/cns/bigtmp2/cnsnglapps/SUPSQ-6329-OUT"  -F xlsx="@/env/cns/bigtmp2/cnsnglapps/BGE_INPUT.xlsx"}
 *  <br><br>
 *  {@code curl --user-agent "bot"  -H 'Content-Type: multipart/form-data' 
 *  -v "http://ngl-bi.genoscope.cns.fr/scripts/run/scripts.UpdateRefCollabSubCNS?
 *  force=false&pathFileOut=/env/cns/bigtmp2/cnsnglapps/SUPSQ-6329-OUT"  -F xlsx="@/env/cns/bigtmp2/cnsnglapps/BGE_000.xlsx"} 
 *  <br><br>
 * <strong> Exemples de lancement dans postman en mettant le fichier de données dans le body :</strong>
 * <br><br>
 * {@code http://appuatisoprod.genoscope.cns.fr:9104/scripts/run/scripts.UpdateRefCollabSubCNS?force=false&pathFileOut=/env/cns/bigtmp2/refCollabSub_2024_07_16}
 * <br><br>
 * {@code http://ngl-bi.genoscope.cns.fr/scripts/run/scripts.UpdateRefCollabSubCNS?force=false&pathFileOut=/env/cns/bigtmp2/refCollabSub_2024_07_16}
 *
 */
public class UpdateRefCollabSubCNS extends ScriptWithArgsAndExcelBody  <UpdateRefCollabSubCNS.Args> {



	public static class Args {
		public String pathFileOut;
		public boolean force;
	}

	// Si on veut forcer le script a faire les updates meme si erreurs de validations, alors mettre force = true mais toujours remonter
	// le code avec force = false car les utilisateurs ne doivent pas avoir acces à l'option force sans modifier le code ici
	// et mettre ligne 340 en commentaire (//throw new Exception("Impossible d'ecrire dans le fichier " + args.pathFileOut);)
    // car le user bot ne peut pas ecrire sur la machine du user qui lance sbt
	

	//private static final String droolsRulesName  = "validations";
	private static final String ruleName         = "Check RefCollabSub in readset";
	final String user                            = "ngl-support";
	final String autorizedSubmissionStateCode    = "NONE";
	private static final play.Logger.ALogger logger = play.Logger.of(UpdateRefCollabSubCNS.class);
	private final NGLApplication    app;

	@Inject
	public UpdateRefCollabSubCNS(NGLApplication app) {
		this.app              = app;

	}
	public class ReadsetInfos  {
		String  readsetCode;
		String  refCollabSub;
		String  oldRefCollabSub;
	}


	public List<ReadsetInfos> parseWorkbook(XSSFWorkbook workbook) throws Exception {
		List<ReadsetInfos> listReadsetInfos = new ArrayList<ReadsetInfos>();
		Iterator<Row> itr = workbook.getSheetAt(0).rowIterator();
		//workbook.getSheetAt(0).rowIterator().forEachRemaining(row -> {
		int countLine = 0;

		while(itr.hasNext()) {
			countLine++;
			Row row = itr.next();
			// verification entete :
			if(countLine == 1) { 
				if(row != null && row.getCell(0) != null && row.getCell(1) != null) {
					// certains caracteres speciaux ne repondent pas au trim()  (voir fichier donné par Caro dans ticket SUPSQ-5948)
					// ni aux expressions regulieres classiques en java pour supprimer des espaces en debut et fin de mot
					// d'ou l'utilisation de StringUtils.normalizeSpace avant le trim
					String enteteCodeReadset = row.getCell(0).getStringCellValue();
					String enteteRefCollabSub = row.getCell(1).getStringCellValue();
					enteteCodeReadset = StringUtils.normalizeSpace(java.text.Normalizer.normalize(enteteCodeReadset, java.text.Normalizer.Form.NFD));
					enteteCodeReadset = enteteCodeReadset.trim();
					enteteRefCollabSub = StringUtils.normalizeSpace(java.text.Normalizer.normalize(enteteRefCollabSub, java.text.Normalizer.Form.NFD));
					enteteRefCollabSub = enteteRefCollabSub.trim();
					if(enteteCodeReadset.equalsIgnoreCase("readsetCode") && enteteRefCollabSub.equalsIgnoreCase("refCollabSub")) {
						// ok, on ignore entete.
					} else {
						throw new Exception("Absence de l'entete attendue (readsetCode refCollabSub) dans le fichier d'entree");
					}
				} else {
					throw new Exception("Absence de l'entete attendue (readsetCode refCollabSub) sur la premiere ligne du fichier d'entree");
				}
				continue;
			}
			// traitement des lignes de données :
			if(row == null) {
				logger.debug("ligne vide pour la ligne " + countLine);
				continue;
			}
			if( row.getCell(0) == null && row.getCell(0) == null) {
				logger.debug("ligne vide pour la ligne " + countLine);
				continue;
			}
			if( row.getCell(0) == null && row.getCell(1) != null) {
				throw new Exception("Ligne " + countLine + " en erreur car absence du champs readsetCode");
			}
			if( row.getCell(0) != null && row.getCell(1) == null) {
				throw new Exception("Ligne " + countLine + " en erreur car absence du champs refCollabSub");
			}
			// certains caracteres speciaux ne repondent pas au trim()  (voir fichier donné par Caro dans ticket SUPSQ-5948)
			// ni aux expressions regulieres classiques en java pour supprimer des espaces en debut et fin de mot
			// d'ou l'utilisation de StringUtils.normalizeSpace avant le trim
			String readsetCode = row.getCell(0).getStringCellValue();
			String refCollabSub = row.getCell(1).getStringCellValue();
			readsetCode = StringUtils.normalizeSpace(java.text.Normalizer.normalize(readsetCode, java.text.Normalizer.Form.NFD));
			readsetCode = readsetCode.trim();
			refCollabSub = StringUtils.normalizeSpace(java.text.Normalizer.normalize(refCollabSub, java.text.Normalizer.Form.NFD));
			refCollabSub = refCollabSub.trim();
			if(StringUtils.isBlank(readsetCode) && StringUtils.isBlank(refCollabSub)) {
				logger.debug("ligne vide pour la ligne " + countLine);
			} else if( StringUtils.isBlank(readsetCode) && StringUtils.isNotBlank(refCollabSub)) {
				throw new Exception("Ligne " + countLine + " en erreur car absence du champs readsetCode");
			} else if( StringUtils.isNotBlank(readsetCode) && StringUtils.isBlank(refCollabSub)) {
				throw new Exception("Ligne " + countLine + " en erreur car absence du champs refCollabSub");
			} else {
				ReadsetInfos readsetInfos = new ReadsetInfos();
				readsetInfos.readsetCode = readsetCode;
				readsetInfos.refCollabSub = refCollabSub;
				readsetInfos.oldRefCollabSub = "not defined";
				//logger.debug("kkkkkkkkkkkkkkkkkkkkkk    readsetCode='{}'", readsetInfos.readsetCode);
				//logger.debug("kkkkkkkkkkkkkkkkkkkkkk    refCollabSub='{}'", readsetInfos.refCollabSub);
				listReadsetInfos.add(readsetInfos); 
			} 
		} // end while

		return listReadsetInfos;
	}

	public Map<String, ReadsetInfos> buildMapReadsetInfos(List<ReadsetInfos> listReadsetInfos, ContextValidation ctx) {
		Map<String, ReadsetInfos> mapReadsetInfos = new HashMap<String, ReadsetInfos>();
		for (ReadsetInfos readsetInfos : listReadsetInfos) {
			if (mapReadsetInfos.containsKey(readsetInfos.readsetCode)) {				
				if(! mapReadsetInfos.get(readsetInfos.readsetCode).refCollabSub.equals(readsetInfos.refCollabSub)) {
					// duplication du readsetCode avec incoherence sur le refCollabSub :
					ctx.addError("rs." +  readsetInfos.readsetCode, " duplication dans fichier utilisateur du readsetCode avec refCollabSub differents " + mapReadsetInfos.get(readsetInfos.readsetCode).refCollabSub + " et " + readsetInfos.refCollabSub);
				} else {
					// duplication mais pas d'incoherence
					logger.debug("duplication dans le fichier utilisateur pour {}", readsetInfos.readsetCode);
				}
			} else {
				mapReadsetInfos.put(readsetInfos.readsetCode, readsetInfos);
			}
		}
		return mapReadsetInfos;
	}


	// copie les erreurs du context de validation B vers le context de validation A 
	public void addErreurCxtBInCtxA(ContextValidation ctxA, ContextValidation ctxB) {
		if(ctxB.hasErrors()) {
			// Reporter les erreurs dans intermediaireCtx : 
			//String messages = "";
			for (Map.Entry<String,List<ValidationError>> e : ctxB.getErrors().entrySet()) {
				for (ValidationError validationError : e.getValue()) {
					for (String message : validationError.messages()) {
						ctxA.appendPath(e.getKey()).addError("", message);
					}
				}
			}				
		}
	}
	
	public void userDataValidation(Map<String, ReadsetInfos> mapReadsetInfos, ContextValidation ctx) {
		//		for (Iterator<Entry<String, ReadsetInfos>> iterator = mapReadsetInfos.entrySet().iterator(); iterator.hasNext();) {
		//			Entry<String, ReadsetInfos> entry = iterator.next();
		for (Entry<String, ReadsetInfos> entry : mapReadsetInfos.entrySet()) {

			String readsetCode = entry.getKey();
			String refCollabSub = entry.getValue().refCollabSub;
			//logger.debug("xxxxxxxxxxxxxxxxxxxxxx    refCollabSub='{}'", refCollabSub);
			
			String newKey = "rs." + readsetCode;
			ContextValidation goodCtx = ctx.appendPath(newKey); // bon contexte de validation avec bon path
			
			// Verifier validité des readsets après ajout de refCollabSub :
			ReadSet readset = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readsetCode);
			//logger.debug("readset code {}", readset.code);
			if (readset == null) {
				goodCtx.addError("", "inexistant dans la base");
				continue;
			}
			if( readset.submissionState == null || readset.submissionState.code == null) {
				goodCtx.addError("submissionState.code", "champs obligatoire");
				continue;
			}
			if( ! readset.submissionState.code.equals(autorizedSubmissionStateCode)) {
				goodCtx.addError("submissionState.code", readset.submissionState.code + " dangereux si modification de refCollabSub (voir implication dans NGL-SUB)");
			}

			if (readset.properties != null && readset.properties.get("refCollabSub") != null) {
				String oldRefCollabSub = (String) readset.properties.get("refCollabSub").value;
				if( ! oldRefCollabSub.equals(refCollabSub)) {
					// si refCollabSub existante dans base et differente de la refCollabSub, mettre en erreur :
					goodCtx.addError("refCollabSub", "refCollabSub en base '" + oldRefCollabSub + "' differente de la refCollabSub utilisateur '" + refCollabSub + "'");
				}
				//entry.getValue().oldRefCollabSub = oldRefCollabSub;
			} else {
				//entry.getValue().oldRefCollabSub = "not defined";
			}

			
			readset.properties.put("refCollabSub", new PropertySingleValue(refCollabSub.toString()));
			readset.traceInformation.modifyUser=user;
			readset.traceInformation.modifyDate= new Date();
			//logger.debug("yyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyy                 newKey='{}'", newKey);

			// readset.validate(goodCtx); => declenche une exception si on passe au validate des readsets un ctx avec appendPath
			ContextValidation tmpCtx = ContextValidation.createUpdateContext("ngl-support");
			readset.validate(tmpCtx);
			addErreurCxtBInCtxA(goodCtx, tmpCtx);
		}
	}

	// methode qui met dans le contexte de validation, uniquement les erreurs due à la regle druls refCollabSub
	// methode à utiliser si on force le script et qu'on souhaite ignorer les autres erreurs.
	public void valideRegleRefCollabSubAndCheckNglSubStatus(Map<String, ReadsetInfos> mapReadsetInfos, ContextValidation ctx, ContextValidation warnCtxA) {
		//		for (Iterator<Entry<String, ReadsetInfos>> iterator = mapReadsetInfos.entrySet().iterator(); iterator.hasNext();) {
		//			Entry<String, ReadsetInfos> entry = iterator.next();
		for (Entry<String, ReadsetInfos> entry : mapReadsetInfos.entrySet()) {
			String readsetCode = entry.getKey();
			String refCollabSub = entry.getValue().refCollabSub;
			//logger.debug("xxxxxxxxxxxxxxxxxxxxxx    refCollabSub='{}'", refCollabSub);
			//String newKey = "rs." + readsetCode;
	
			String newKey = "rs.";
			ContextValidation goodCtx = ctx.appendPath(newKey); // bon contexte de validation avec bon path
			// ajout du 28/08/2025 pour avoir warning avec argument force=true
			
			String warnKey = "rs." + readsetCode;
			// Attention a bien declarer nouveau context de validation pour warning (different de l'arg ctxWarnA de la methode), 
			// sinon concatenation des appendpath
			ContextValidation warnCtx = warnCtxA.appendPath(warnKey); // context de validation pour warning.
			// Verifier validité des readsets après ajout de refCollabSub :
			ReadSet readset = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readsetCode);
			//logger.debug("readset code {}", readset.code);
			if (readset == null) {
				goodCtx.addError("", "inexistant dans la base");
				continue;
			}
			if( readset.submissionState == null || readset.submissionState.code == null) {
				goodCtx.addError("submissionState.code", "champs obligatoire non present");
				continue;
			}
			if( ! readset.submissionState.code.equals(autorizedSubmissionStateCode)) {
				warnCtx.addError("submissionState.code", readset.submissionState.code + " warning : ajout ou modification de refCollabSub sur rs soumis ou en cours de soumission");
			}
			if (readset.properties != null && readset.properties.get("refCollabSub") != null) {
				String oldRefCollabSub = (String) readset.properties.get("refCollabSub").value;
				if( ! oldRefCollabSub.equals(refCollabSub)) {
					// si refCollabSub existante dans base et differente de la refCollabSub, mettre en erreur :
					warnCtx.addError("refCollabSub", "refCollabSub en base '" + oldRefCollabSub + "' differente de la refCollabSub utilisateur '" + refCollabSub + "'");
				}
				//entry.getValue().oldRefCollabSub = oldRefCollabSub;
			} else {
				//entry.getValue().oldRefCollabSub = "not defined";
			}

			readset.properties.put("refCollabSub", new PropertySingleValue(refCollabSub.toString()));
			readset.traceInformation.modifyUser=user;
			readset.traceInformation.modifyDate= new Date();
			// readset.validate(goodCtx); => declenche une exception si on passe au validate des readsets un ctx avec appendPath
			ContextValidation tmpCtx = ContextValidation.createUpdateContext("ngl-support");
			readset.validate(tmpCtx);
			addErreurCxtBInCtxA(goodCtx, tmpCtx);			
			//logger.debug("yyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyy                 newKey='{}'", newKey);

			// readset.validate(goodCtx); => declenche une exception si on passe au validate des readsets un ctx avec appendPath

			// lancer la regle de validation druls "Check RefCollabSub in readset"
			// et recuperer messages erreur dans le contexte de validation
//			ArrayList<Object> facts = new ArrayList<>();
//			facts.add(readset);			
//			facts.add(goodCtx);
//			
//			RulesServices6.getInstance()
//					.callRulesByName(ruleName, facts);
		}

	}

	public Set<String> buildNoUpdateAndCompleteMapReadsetInfosForOldRefCollabSub(Map<String, ReadsetInfos> mapReadsetInfos) {
		Set<String> noUpdate = new HashSet<String>();
		for (Entry<String, ReadsetInfos> entry : mapReadsetInfos.entrySet()) {

			String readsetCode = entry.getKey();
			String refCollabSub = entry.getValue().refCollabSub;

			ReadSet readset = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readsetCode);
			//logger.debug("readset code {}", readset.code);
			if (readset == null) {
				continue;
			}

			if (readset.properties != null && readset.properties.get("refCollabSub") != null) {
				String oldRefCollabSub = (String) readset.properties.get("refCollabSub").value;
				if(oldRefCollabSub.equals(entry.getValue().refCollabSub)) {
					// si refCollabSub dans base == refCollabSub donné par utilisateur alors ignorer la ligne
					logger.debug("update du readsetCode '{}' pour refCollabSub '{}' inutile : refCollabSub en base et refCollabSub indique par utilisateur identiques)", readset.code, refCollabSub);
					noUpdate.add(readsetCode);
					continue;
				} 
				entry.getValue().oldRefCollabSub = oldRefCollabSub;
			} else {
				entry.getValue().oldRefCollabSub = "not defined";
			}
		}
		return noUpdate;
	}


	@SuppressWarnings("unchecked")
	public int saveRefCollabSubInBase(Map<String, ReadsetInfos> mapReadsetInfos, Set<String> noUpdate, ContextValidation ctxRecap, String nameObjectRecap) {
		// mise à jour de la base pour tous les readsets avec leur refCollabSub :
		int cp = 0;
		for (Iterator<Entry<String, ReadsetInfos>> iterator = mapReadsetInfos.entrySet().iterator(); iterator.hasNext();) {
			Entry<String, ReadsetInfos> entry = iterator.next();
			String readsetCode = entry.getKey();
			if(noUpdate.contains(readsetCode)) {
				println("update inutile du readset '" + readsetCode +"' pour refCollabSub '" + entry.getValue().refCollabSub + "' (refCollabSub deja present en base)");
			} else {
				cp++;
				ReadsetInfos readsetInfo = entry.getValue();
				ReadSet readset = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readsetCode);
				readset.properties.put("refCollabSub", new PropertySingleValue(readsetInfo.refCollabSub.toString()));
				readset.traceInformation.modifyUser=user;
				readset.traceInformation.modifyDate= new Date();
				MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, readset);
				println("insertion : readsetCode " + readsetCode + ", oldRefCollabSub " + readsetInfo.oldRefCollabSub + ", newRefCollabSub " + readsetInfo.refCollabSub);
				logger.info("insertion : readsetCode {}, oldRefCollabSub {}, newRefCollabSub {}",readsetCode, readsetInfo.oldRefCollabSub, readsetInfo.refCollabSub);
				((List<String>)ctxRecap.getObject(nameObjectRecap)).add(readsetCode + "," + readsetInfo.oldRefCollabSub + "," + readsetInfo.refCollabSub);
			}
		}
		return cp;
	}


	@Override
	@SuppressWarnings("unchecked")
	public void execute(Args args, XSSFWorkbook workbook) throws Exception {
		logger.info("Debut du script UpdateRefCollaSubCNS");
		println("Debut du script UpdateRefCollaSubCNS");
		boolean force = args.force;
		// verifier chemin du fichier de sortie :
		if( ! args.pathFileOut.endsWith(".xls") ) {
			args.pathFileOut = args.pathFileOut + ".xls";
			//logger.debug("fichier de sortie = "  + args.pathFileOut);
		}
		try (OutputStream fileOut = new FileOutputStream(args.pathFileOut)) {
		} catch(Exception e){
			throw new Exception("Impossible d'ecrire dans le fichier " + args.pathFileOut);
		}

		// contexte de validation pour la validation :
		ContextValidation ctx = ContextValidation.createUpdateContext("ngl-support");
		
		// parser le fichier d'entree pour construire les association readsetCode refCollabSub
		List<ReadsetInfos> listReadsetInfos =  parseWorkbook(workbook);

		// construction de la map mapReadsetInfos en verifiant duplications dans les données utilisateur:
		Map<String, ReadsetInfos> mapReadsetInfos = buildMapReadsetInfos(listReadsetInfos, ctx);

		// Construire la liste des readsets ayant meme refCollabSub en base que dans fichier utilisateur (n'ayant pas besoin d'etre mis à jour => noUpdate)
		// et completer mapReadsetInfos pour oldRefCollabSub :
		Set<String> noUpdate = new HashSet<>();
		noUpdate = buildNoUpdateAndCompleteMapReadsetInfosForOldRefCollabSub(mapReadsetInfos);

		// validation des readset avec refCollabSub donnees par les utilisateurs :
		if (! force) {
			userDataValidation(mapReadsetInfos, ctx);
		} else {
			// contexte de validation pour les warning:
			ContextValidation warnCtx = ContextValidation.createUpdateContext("ngl-support");
			valideRegleRefCollabSubAndCheckNglSubStatus(mapReadsetInfos, ctx, warnCtx);
			if(warnCtx.hasErrors()) {
				logger.info("Attention : ");
				warnCtx.displayErrors(logger, "info");
				println("Attention : ");
				println(Json.prettyPrint(app.errorsAsJson(warnCtx.getErrors())));		
			}
		}
		// sortir si erreurs detectees :
		if(ctx.hasErrors()) {
			println(Json.prettyPrint(app.errorsAsJson(ctx.getErrors())));
			println("Fin du traitement UpdateRefCollaSubCNS : Aucun ajout de refCollabSub dans la base");
			ctx.displayErrors(logger, "info");
			logger.info("Fin du traitement UpdateRefCollaSubCNS : Aucun ajout de refCollabSub dans la base");
			return;
		}

		// contexte de validation utilisé pour stocker le recapitulatif des readsets mis à jour avec oldRefCollabSub et refCollabSub
		ContextValidation ctxRecap = ContextValidation.createUndefinedContext("ngl-support");
		String nameObjectRecap = "RecapReadSet";

		ctxRecap.putObject(nameObjectRecap, new ArrayList<String>());
		//Ajout du header du fichier de sortie dans recap :
		((List<String>) ctxRecap.getObject(nameObjectRecap)).add("readsetCode,oldRefCollabSub,newRefCollabSub");

		// mise à jour de la base pour tous les readsets avec leur refCollabSub et renseigner ctxRecap:
		int cpUpdate = saveRefCollabSubInBase(mapReadsetInfos, noUpdate, ctxRecap, nameObjectRecap);

		// Creer le fichier de sortie :
		createExcelFileRecap(args.pathFileOut, ctxRecap);
		logger.info("Fin du traitement UpdateRefCollaSubCNS: update de '{}' readset dans la base pour refCollabSub, voir fichier de sortie '{}'", cpUpdate, args.pathFileOut );
		println("Fin du traitement UpdateRefCollabSubCNS : update de " + cpUpdate + " readset dans la base pour refCollabSub, voir fichier de sortie '" + args.pathFileOut + "'");
	}


	//---------------------------------------------------

	/**
	 * Create excel file in execution directory
	 * File recap : list all objects updated
	 * @param cv
	 */
	@SuppressWarnings("unchecked")
	private void createExcelFileRecap(String pathFile, ContextValidation cv) {
		Workbook wb = new HSSFWorkbook();
		CreationHelper createHelper = wb.getCreationHelper();
		for(String key : cv.getContextObjects().keySet()){
			Sheet sheet = wb.createSheet(key);
			List<String> recaps = (List<String>) cv.getObject(key);
			int nbLine=0;
			for(String recap : recaps){
				//	Logger.debug("recap: "+recap);
				Row row = sheet.createRow(nbLine);
				String[] tabRecap = recap.split(",");
				for(int i=0;i<tabRecap.length;i++){
					row.createCell(i).setCellValue(
							createHelper.createRichTextString(tabRecap[i].trim()));
				}
				nbLine++;
			}
		}

		// Write the output to a file
		try (OutputStream fileOut = new FileOutputStream(pathFile)) {
			wb.write(fileOut);
			fileOut.flush();
		}catch(Exception e){
			Logger.debug(e.getMessage());
		}
	}			

}
