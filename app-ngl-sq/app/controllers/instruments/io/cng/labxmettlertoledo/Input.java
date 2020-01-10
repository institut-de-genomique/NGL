package controllers.instruments.io.cng.labxmettlertoledo;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mongojack.DBQuery;

import controllers.instruments.io.utils.AbstractInput;
import fr.cea.ig.MongoDBDAO;
import models.laboratory.common.instance.property.PropertyFileValue;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.reagent.description.BoxCatalog;
import models.laboratory.reagent.description.KitCatalog;
import models.laboratory.reagent.description.ReagentCatalog;
import models.laboratory.reagent.instance.ReagentUsed;
import models.utils.InstanceConstants;
import validation.ContextValidation;

public class Input extends AbstractInput {

   /* 25/10/2017 NGL-1326
    * Description du fichier à importer: fichier CSV ";"  delimité generé par le Logiciel LabX de Mettler Toledo 
    * LabX fait lui même des controles, donc certains cas ne doivent pas etre gérés ici: codes barres manquants ou mal formés...
    * 
    * 22/11/2017 NGL-1710 modifications... 
    * le fichier Mettler V2 n'est pas pret. Repartir du fichier V1 ( 2 lignes LOT et RGT pour les boîtes et "réactifs" non pesés )
    * => on ne peut plus créer les RU apres le traitement d'une ligne, il faut les stocker en attente dans des Hash et les creer tous a la fin
    * Structuration du code pour le rendre plus lisible et modulaire
    * Attention le flag "active" pour les reagents est toujours a true dans la base mongo, NE PAS L'UTILISER
    * 
    * 09/01/2018 ajout Exception et try/catch
    *            algo V3: n'ajouter les réactifs dans le hash qu'une fois la boîte trouvee dans le fichier et pas dans new ExperimentCatalog
    *            creation des réactifs (s'il sont uniques) même en abscence de la boîte parent dans le fichier          
    */
	
	private class CatalogException extends Exception {
		/**
		 * Eclipse requested.
		 */
		private static final long serialVersionUID = 1L;

		public CatalogException(String message) { super(message); }
	}

	@Override
	public Experiment importFile(Experiment experiment, PropertyFileValue pfv, ContextValidation contextValidation) throws Exception {		
		
		//-1-------- Récupérer la liste des kits actifs / boîtes actives 
		//Logger.debug ("--GET CATALOGS INFO --");
		
		// currCatalog est le catalog pour le type d'experience en cours, otherCatalog est le catalogue pour l'autre experience egalement traitee dans le fichier
		// si type experience en cours = "prepa-fc-ordered" alors l'autre est "illumina-depot" (et vice versa)
		ExperimentCatalog currCatalog=null;
		ExperimentCatalog otherCatalog=null;
		String otherExperimentTypeCode=null;
		
		try {
			if ( experiment.typeCode.equals("prepa-fc-ordered") ) {
				otherExperimentTypeCode="illumina-depot";

				currCatalog= new ExperimentCatalog ("prepa-fc-ordered"); 
				otherCatalog= new ExperimentCatalog (otherExperimentTypeCode);
			} else {
				otherExperimentTypeCode="prepa-fc-ordered";
			
				currCatalog= new ExperimentCatalog ("illumina-depot");
				otherCatalog= new ExperimentCatalog (otherExperimentTypeCode);
			}
		} catch (CatalogException e) {
			contextValidation.addError("Erreurs Catalogue",e.getMessage());
			return experiment;
		}	
		
		//-2-------- Parsing
		logger.debug ("------ START PARSING METTLER FILE ------");
		
//		byte[] ibuf = pfv.value;
		byte[] ibuf = pfv.byteValue();
		
		// le fichier CSV sorti par le logiciel Labx est en ISO-8859 ( valeur retournee par la commande "file" Linux)
		String charset = "ISO-8859-15";
		
		// charset detection (N. Wiart)
		// String charset = "UTF-8"; //par defaut, convient aussi pour de l'ASCII pur
		// si le fichier commence par les 2 bytes ff/fe  alors le fichier est encodé en UTF-16 little endian
		// if (ibuf.length >= 2 && (0xff & ibuf[0]) == 0xff && (ibuf[1] & 0xff) == 0xfe) {
		//	charset = "UTF-16LE";
		// }

		// HashMap pour les reagentUsed valides au cours du parsing
		Map<String,ReagentUsed> parsedReagentsMap = new HashMap<>(0);

		// 30/11/2017 creer un hashMap distinct pour les box permet de ne pas creer les box a la fin
		Map<String,ReagentUsed> parsedBoxesMap = new HashMap<>(0);

		try (InputStream is = new ByteArrayInputStream(ibuf);
			 BufferedReader reader = new BufferedReader(new InputStreamReader(is, charset))) {
		
			int n = 0;
			boolean lastResult=false;
			String line="";	

			while (((line = reader.readLine()) != null) && !lastResult ){	 

				// attention si le fichier vient d'une machine avec LOCALE = FR les décimaux utilisent la virgule!!!
				String[] cols = line.replace (",", ".").split(";");

				if (n == 0) {	
					if (!firstLineCorrect(cols, contextValidation) ){
						break;
					}
				} else if (n == 5) {
					if (!fifthLineCorrect(cols, experiment, contextValidation) ){
						break;
					}
				} else if (n > 5 ) {
					// ligne "Résultats principaux..." = fin des données a parser
					if ( cols[0].trim().matches("Résultats principaux(.*)")){
						lastResult=true;
					} else {
						if ( !cols[0].trim().equals("") ){
							// section boîtes
							processBoxSectionLine(n, cols, currCatalog, otherCatalog, parsedReagentsMap, parsedBoxesMap, contextValidation ) ;
						} else if ( !cols[1].trim().equals("Position") ){
							//section resultats
							processPositionSectionLine(n, cols, currCatalog, otherCatalog, parsedReagentsMap, parsedBoxesMap, contextValidation);
						}
					}
				}
				n++;
			}//end while
		}
		
		logger.debug ("------ END PARSING METTLER FILE --------");
			
		//-3---------- creer les reagents used parsés
		if ( ! contextValidation.hasErrors() ) {
			logger.debug ("------  CREATION DES REAGENTS USED ------");
			// 30/11/2017 avec 2 map distincts on peut ne creer que les reagent et pas les box !!
			// 17/01/2018 retour a la creation de toutes les boites sinon celles qui n'ont pas de reactifs n'apparaissent plus.....
			//
			for (Map.Entry<String, ReagentUsed> pair : parsedBoxesMap.entrySet()) {
				//Logger.debug("parsedBOX... :"+ pair.getKey());
				//if ( (pair.getKey().equals("HiSeq X Flow Cell")) || (pair.getKey().equals("HiSeq 3000/4000 PE Flow Cell"))|| (pair.getKey().matches("(.*)Manifold(.*)")) ){
				logger.debug(" création boîte seule: "+ pair.getKey());
				experiment.reagents.add(pair.getValue());
				//}
			}
			for (Map.Entry<String, ReagentUsed> pair : parsedReagentsMap.entrySet()) {
                logger.debug(" création réactif: "+ pair.getKey());
                experiment.reagents.add(pair.getValue());
            }
		}
		
		return experiment;
	}
	
	private static boolean firstLineCorrect( String[] cols, ContextValidation contextValidation) {
		//Logger.debug (">>>firstLineCorrect:"+ cols[0]);
		
		// verifier que c'est bien un fichier CSV ;delimited...
		
		//Logger.info ("ligne "+n+" nbre colonne="+cols.length);
		/* marche pas....length=2 pour un fichier EXcel mais =1 pour un fichier CSV !!!!!!!
		if (cols.length != 15) {
			// pas un fichier CSV ?????
			contextValidation.addErrors("Erreurs fichier","Le fichier ne semble pas être au format CSV / ;");
			Logger.info ("col 0="+cols[0]);
			return false; // si ce n'est pas le bon type de fichier la suite va sortir des erreurs incomprehensibles...terminer		
		}
		*/
			
		//  verifier que c'est bien un fichier LabX pour sequençage
		if ( !cols[0].trim().equals("Compte rendu de séquençage") ) {
			contextValidation.addError("Erreurs fichier","experiments.msg.import.header-label.missing","1", "Compte rendu de séquençage");
			return false ; // si ce n'est pas le bon type de fichier la suite va sortir des erreurs incomprehensibles...terminer
		}
		return true;
	}
	
	private static boolean fifthLineCorrect( String[] cols, Experiment experiment, ContextValidation contextValidation) {
		//Logger.debug (">>>fifthLineCorrect:" + cols[0]);
		
		// en 6eme ligne (n=5) il y actuellement le nom d'un kit "de Flow Cell" qui permet de vérifier que le fichier correspond au type d'expérience
		//HARDCODED !!!
		if ( cols[0].trim().equals("HiSeq X Flow Cell") || cols[0].trim().equals("HiSeq 3000/4000 PE Flow Cell")) {
			// verifier le code flow cell
			String flowcellId=cols[8].trim();
			//Logger.info ("flowcellId="+flowcellId);

			if ( experiment.typeCode.equals("prepa-fc-ordered") ) {	
				// barcode FC dans propriete d'instrument (ou container out c'est pareil)
				if ( !experiment.instrumentProperties.get("containerSupportCode").value.equals(flowcellId))  {
				    contextValidation.addError("Erreurs fichier", "ligne 6: Le code flowcell ("+flowcellId+") ne correspond pas à celui de l'expérience.");
					return false;
				}
			} else {
				// ("illumina-depot").equals.experiment.typeCode
				// barcode FC dans container in inputContainerSupportcodes
				if ( ! experiment.inputContainerSupportCodes.contains(flowcellId) )  {
				    contextValidation.addError("Erreurs fichier", "ligne 6: Le code flowcell ("+flowcellId+") ne correspond pas à celui de l'expérience.");
					return false;
				}
			}		               
			
			return true;
			
		} else {	
			contextValidation.addError("Erreurs fichier","ligne 6:'"+ cols[0].trim() + "': type de fichier LabX non pris en charge.");
			return false;
		} 
	}
	
	/* Traitement de la section qui comence en ligne 6: contient a la fois des boîtes et des reactifs
	 * les reactifs de cette section n'on pas de poids entree, poids sortie et difference
	 * la colonne 0 contient un label
	 */
//	private static void processBoxSectionLine( int n, String[] cols, ExperimentCatalog currCatalog, ExperimentCatalog otherCatalog, Map<String,ReagentUsed> parsedReagentsMap, Map<String,ReagentUsed> parsedBoxesMap, ContextValidation contextValidation) {
	private void processBoxSectionLine( int n, String[] cols, ExperimentCatalog currCatalog, ExperimentCatalog otherCatalog, Map<String,ReagentUsed> parsedReagentsMap, Map<String,ReagentUsed> parsedBoxesMap, ContextValidation contextValidation) {
		// 07/11/2017:  pour la flowcell => LOT; pour le manifold => SN; pour le reste =>RGT
		// 23/11/2017: en V1 on a LOT *ET* RGT  => on va trouver 2 lignes pour chaque item !!! il faut concaténer les valeurs trouvées pour la création du reagent used
		
		//Logger.debug ("processing ligne "+ n +" section boîtes");
		String item[]= null;
		String fileItemName=null;
		String fileItemCode=null;
		
		if      ( cols[0].trim().matches("LOT (.*)") ) { item = cols[0].trim().split("LOT"); }
		else if ( cols[0].trim().matches("SN (.*)")  ) { item = cols[0].trim().split("SN");  }
		else if ( cols[0].trim().matches("RGT (.*)") ) { item = cols[0].trim().split("RGT"); }
		
		if (item == null) {
			// ignorer (ligne Code Barre ou ligne Résultat ou imprevue...)
			//Logger.debug ("skip line ("+ n +") :"+ cols[0].trim() );
			return;	
		} 
		
		fileItemName = item[1].trim();
		fileItemCode = cols[8].trim();
			
		//-1- chercher si existe dans TOUT le catalogue !!
		//Logger.debug ("ligne "+ n +" chercher :" +fileItemName + " dans TOUT le catalogue");	
		
		// Pas trouvé comment chercher sur "name" uniquement, il faut 2 requetes, sinon pb de class
		List<ReagentCatalog> matchListReagent = MongoDBDAO.find(InstanceConstants.REAGENT_CATALOG_COLL_NAME, ReagentCatalog.class, (DBQuery.is("category", "Reagent").and(DBQuery.is("name",fileItemName)))).toList();
		List<BoxCatalog> matchListBox = MongoDBDAO.find(InstanceConstants.REAGENT_CATALOG_COLL_NAME, BoxCatalog.class, (DBQuery.is("category", "Box").and(DBQuery.is("name", fileItemName)))).toList();
		
		if (matchListReagent.size() == 0 && matchListBox.size() == 0 ) {
			contextValidation.addError("Erreurs fichier","ligne "+ (n+1) +": '"+ fileItemName+ "': n'existe pas dans le catalogue (ni boîte, ni réactif).");
		} else if (matchListReagent.size() == 0 && matchListBox.size() > 0 ) {  // c'est une boîte; 
			//Logger.debug(fileItemName+ "=BOITE...");
			createBoxtypeReagentUsed( n, fileItemName, fileItemCode, currCatalog, otherCatalog, parsedBoxesMap, contextValidation);
		} else if ( matchListBox.size() == 0 && matchListReagent.size() > 0 ) { // c'est un réactif
			//Logger.debug(fileItemName+ "=REACTIF...");
			createReagtypeReagentUsed( n, null, fileItemName, fileItemCode, currCatalog, otherCatalog, parsedReagentsMap, matchListReagent, contextValidation);
		} else {
			// boîte ET reactif de même nom
			contextValidation.addError("Erreurs fichier","ligne "+ (n+1) +": '"+ fileItemName+ "': un réactif et une boîte ont le même nom.");
		}
		return; 
	}
	
	/* Traitement de la section qui commence a la ligne "Resultats"
	 * dans cette section ne doivent se trouver QUE des reactifs (pas de boîtes)
	 * la colonne 0 est vide et la colonne 1 contient la position ( 1-->N )
	 */
//	private static void processPositionSectionLine( int n, String[] cols, ExperimentCatalog currCatalog, ExperimentCatalog otherCatalog, Map<String,ReagentUsed> parsedReagentsMap, Map<String,ReagentUsed> parsedBoxesMap, ContextValidation contextValidation ) {
	private void processPositionSectionLine( int n, String[] cols, ExperimentCatalog currCatalog, ExperimentCatalog otherCatalog, Map<String,ReagentUsed> parsedReagentsMap, Map<String,ReagentUsed> parsedBoxesMap, ContextValidation contextValidation ) {
		logger.debug ("processing ligne "+ n +" section position...");		
		
		// !!! si des colonnes sont vides il y a coalescence et on obtient un arrayOutOfBonds....
		if ( cols.length < 16) {
			contextValidation.addError("Erreurs fichier","ligne "+ (n+1) +": nombre de colonne incorrect.");
			return;
		}
		
		String fileReagentName = cols[2].trim();
		String fileReagentCode = cols[5].trim();

		//-1- chercher si existe dans TOUT le catalogue !!	
		//Logger.debug ("ligne "+ n +" chercher :"+fileReagentName + " dans TOUT le catalogue");	
		
		// ici on peut ajouter le fitre type dans la requete (evite l'erreur: Class models.laboratory.reagent.description.BoxCatalog not subtype of [simple type, class models.laboratory.reagent.description.ReagentCatalog]
		// s'il y a autre chose (kit ou boîte) de même nom !!!
		List<ReagentCatalog>  matchListReagent= MongoDBDAO.find(InstanceConstants.REAGENT_CATALOG_COLL_NAME, ReagentCatalog.class, DBQuery.is("category", "Reagent").and(DBQuery.is("name", fileReagentName))).toList();

		if (matchListReagent.size() == 0) {
			contextValidation.addError("Erreurs fichier","ligne "+ (n+1) +": '"+ fileReagentName+ "': ce réactif n'existe pas dans le catalogue.");
			return;
		// 08/01/2018 suppression unicité dans le catalogue car on le recherche dans les boîtes trouvees
		//} 
		//else if ( testList.size() > 1 ) {
		//	contextValidation.addErrors("Erreurs fichier","ligne "+ (n+1) +":'"+ fileReagentName+ "': réactif trouvé plusieurs fois dans le catalogue.");
		//	return ;
		} else {
			//Logger.debug (fileReagentName + " existe...");
			createReagtypeReagentUsed( n, cols, fileReagentName, fileReagentCode, currCatalog, otherCatalog, parsedReagentsMap, matchListReagent, contextValidation);
		}
		return; 
	}
	
	private class ExperimentCatalog {
		
		// attributs	
		private Map<String,BoxCatalog> boxMap = new HashMap<>(0);
		private Map<String,ReagentCatalog> reagentMap = new HashMap<>(0);
		private Map<String,String> boxCodeMap = new HashMap<>(0); // hashmap pour recuperer le code d'une box d'apres son nom..
		
		//constructeur 
		public ExperimentCatalog (String experimentTypeCode) throws CatalogException {
			getCatalogInfoExperiment (experimentTypeCode, boxMap, reagentMap, boxCodeMap);
		}

		// methodes
		private void getCatalogInfoExperiment(String experimentTypeCode, Map<String,BoxCatalog> boxMap,Map<String,ReagentCatalog> reagentMap, Map<String,String> boxCodeMap ) throws CatalogException {
			logger.debug (" -- getCatalogInfoExperiment for "+experimentTypeCode+ "--");
			
			List<KitCatalog> kitList = MongoDBDAO.find(InstanceConstants.REAGENT_CATALOG_COLL_NAME, KitCatalog.class, 
					DBQuery.is("category", "Kit").and(DBQuery.is("active", true)).and(DBQuery.in("experimentTypeCodes", experimentTypeCode))).toList();
			
			if ( kitList.isEmpty() ) {	
				//Logger.debug ("PAS DE KIT ASSOCIE !!!");	
				throw new CatalogException("Aucun kit actif pour l'expérience "+ experimentTypeCode);
			} else {
				for (KitCatalog kit : kitList) {
					//Logger.debug ("KIT: '"+kit.name + "'("+ kit.code+")");
					// trouver les boîtes de chacun des kits actifs
					List<BoxCatalog> kitBoxList = MongoDBDAO.find(InstanceConstants.REAGENT_CATALOG_COLL_NAME, BoxCatalog.class, 
							DBQuery.is("category", "Box").and(DBQuery.is("active", true)).and(DBQuery.is("kitCatalogCode", kit.code))).toList();
					for(BoxCatalog box:  kitBoxList){
						logger.debug (" boîte ACTIVE:'"+ box.name + "'("+box.code+")");
						// 09/01/2018 verifier que la même boîte n'existe pas deja dans un autre  kit actif !!!!
						if (boxMap.containsKey(box.name) ){
							logger.debug ("BOITE EN DOUBLON :"+ box.name + " dans boxMap !!");
							throw new CatalogException("Kit '"+ kit.name +"': une boîte active de même nom '"+ box.name +"' existe déjà dans un autre kit actif pour ce type d'expérience.");
						}	 
						//Logger.debug ("  ...ajoutee dans boxMap");
						boxMap.put(box.name, box);
					}
			     }
		    }	
		}
	} // fin private class ExperimentCatalog
	
	// ajouter les reactifs d'une boîte qui a ete trouvee dans le fichier
//	private static void updateExperimentCatalog (String boxCatalogCode, ExperimentCatalog currCatalog, ContextValidation contextValidation)  {
	private void updateExperimentCatalog (String boxCatalogCode, ExperimentCatalog currCatalog, ContextValidation contextValidation)  {
		//Logger.debug ("   update reagentMap de la boîte : "+ boxCatalogCode);

		//trouver la liste des reagent ce la boîte les ajouter dans currCatalog.reagentMap
		// !! le flag "active" des reactifs est toujours "true" inutile de l'utiliser !!!
		List<ReagentCatalog>  boxReagentList = MongoDBDAO.find(InstanceConstants.REAGENT_CATALOG_COLL_NAME, ReagentCatalog.class, 
        		 DBQuery.is("category", "Reagent").and(DBQuery.is("active", true)).and(DBQuery.is("boxCatalogCode", boxCatalogCode))).toList();
		
		for (ReagentCatalog reagent : boxReagentList) {
			logger.debug ("reactif "+reagent.name + "'("+reagent.code+ ").....");
			if ( ! currCatalog.reagentMap.containsKey(reagent.name) ){
				//Logger.debug ("   ....REACTIF ACTIF AJOUTE");
				currCatalog.reagentMap.put(reagent.name, reagent);
			} else {
				// le même reactif existe deja dans une autre boîte active !!
				//Logger.debug ("   ....REACTIF EN DOUBLON");
				contextValidation.addError("Erreurs catalogue","le réactif '"+ reagent.name+ "' existe dans plusieurs boîtes actives pour ce type d'expérience.");	
			}
		}
		return;
	}
	
	// creation d'un reagent Used de type boîte
	// private static void createBoxtypeReagentUsed (int n, String name, String code, ExperimentCatalog currCatalog, ExperimentCatalog otherCatalog, Map<String,ReagentUsed> parsedBoxesMap , ContextValidation contextValidation) {
	private void createBoxtypeReagentUsed (int n, String name, String code, ExperimentCatalog currCatalog, ExperimentCatalog otherCatalog, Map<String,ReagentUsed> parsedBoxesMap , ContextValidation contextValidation) {
	
		//-1- chercher dans current boxMap
		if ( currCatalog.boxMap.containsKey(name) ) {	
			//Logger.debug ("  boîte '"+name+" TROUVEE DANS CURR BOX HASHMAP...");

			BoxCatalog currBc = currCatalog.boxMap.get(name); 
			// construire un reagentUsed s'il n'est pas deja dans le hash, le completer sinon... necessaire en V1 car il y a 2 lignes par boîte/reactif 
			ReagentUsed ru=null;
			if ( ! parsedBoxesMap.containsKey(name) ){
				ru = new ReagentUsed();  
				ru.kitCatalogCode=currBc.kitCatalogCode;  
				ru.boxCatalogCode=currBc.code; 
				ru.boxCode=code+"_" ;  // !!!! les codes doivent se terminer par "_" pour etre filtrables par la suite
			
				// rafraichir la liste des reactifs succeptibles d'etre trouvés grace a cette nouvelle boîte d'apres son code
				//Logger.debug("mise a jour des reactifs de la boîte :"+ name + "("+ currBc.code + ")" );
				updateExperimentCatalog(currBc.code, currCatalog, contextValidation );
			
			} else {
				//Logger.debug ("boîte deja connue; mise a jour du code de boîte:"+ name + "("+ currBc.code + ")" );
				ru= parsedBoxesMap.get(name);
				ru.boxCode=ru.boxCode+code+"_" ;
			}

			parsedBoxesMap.put(name, ru);
		
			//!! utiliser le boxCode mis a jour !! pour ses reagents plus tard...
			//Logger.debug(" ajouter boîte :'" +name+ "'("+ currBc.code+ ") dans currCatalog boxCodeMap");
			currCatalog.boxCodeMap.put(currBc.code,ru.boxCode);
		
			//DEBUG
			//Logger.debug(">>BOX:kitCatalogCode="+ru.kitCatalogCode);
			//Logger.debug(">>BOX:boxCatalogCode="+ru.boxCatalogCode);
			//Logger.debug(">>BOX:boxCode="+ ru.boxCode); 
		} 
		//-2- chercher dans other BoxMap
		else if ( otherCatalog.boxMap.containsKey(name) ){
			//Logger.debug ("  boîte '"+name+"' TROUVEE DANS OTHER BOX HASHMAP...");
			// rien a faire, sera traité lors de l'autre import....
		}
		else
		{
			//  boîte inactive soit dans kit inactif  soit pas relié aux 2 expériences impliquees...
			contextValidation.addError("Erreurs fichier","ligne "+ (n+1) +": '"+ name+ "': boîte inactive ou dans un kit inactif ou non relié aux type d'expériences attendus.");
		}
	
	return ;
	
	} // fin method
	
	
	/* creation d'un reagent Used de type reactif
	 * si on appelle depuis la section Boite cols est null
	 */
	private static void createReagtypeReagentUsed ( int n, String[] cols, String name, String code, ExperimentCatalog currCatalog, ExperimentCatalog otherCatalog, Map<String,ReagentUsed> parsedReagentsMap, List<ReagentCatalog> matchListReagent, ContextValidation contextValidation ) {
		//Logger.debug ("createReagtypeReagentUsed "+ name);
		
		String position=null;
		String fileInputReagentWeight =null;
		String fileOutputReagentWeight =null;
		String fileDiffReagentWeight =null;
		String reagParsedKey=null;
		
		if ( null != cols ) {
			// appel depuis la section Position	
			fileInputReagentWeight = cols[6].trim();
			fileOutputReagentWeight = cols[11].trim();
			fileDiffReagentWeight = cols[15].trim();
			position= cols[1].trim();
			reagParsedKey=name+"/"+position; // le même reactif peut etre present a plusieurs positions !!
		} else {
			reagParsedKey=name;
		}
		
		//-1- chercher dans current ReagentMap
		if ( currCatalog.reagentMap.containsKey(name) ){	
			//Logger.debug ("  reactif '"+name+"' TROUVE DANS CURR REAGENT HASH...");
				
			ReagentCatalog currRc= currCatalog.reagentMap.get(name);	
		
			// construire un reagentUsed s'il n'est pas deja dans le hash, le completer sinon... necessaire en V1 car il y a 2 lignes par boîte/reactif
			// pour les reactifs de la section boîtes....
			ReagentUsed ru=null;
			if (! parsedReagentsMap.containsKey(reagParsedKey) ){	
				ru=new ReagentUsed();  
				ru.kitCatalogCode=currRc.kitCatalogCode;  // code NGL du kit parent
				ru.boxCatalogCode=currRc.boxCatalogCode;  // code NGL de la boîte parent
				ru.reagentCatalogCode=currRc.code;        // code NGL du reactif
				ru.boxCode=currCatalog.boxCodeMap.get(currRc.boxCatalogCode); // barcode de la boîte parent
				ru.code=code+"_" ; // !!!! les codes doivent se terminer par "_" pour etre filtrables par la suite
				
				if ( null != cols ) {
					// appel depuis la section position
					// même si a priori la recherche n'est pas possible actuellement sur le champs description, utiliser aussi "_" pour separer les items
					ru.description=fileInputReagentWeight +"_"+ fileOutputReagentWeight +"_"+ fileDiffReagentWeight;	
				}		
			} else {
					//Logger.debug ("mise a jour code de réactif");
					ru= parsedReagentsMap.get(reagParsedKey);
					ru.code=ru.code +code+"_" ;
			}
			
			// ajouter/ecraser dans la map
			parsedReagentsMap.put(reagParsedKey, ru);
				
			//DEBUG	
			//Logger.debug(">>REAG:kitCatalogCode="+ru.kitCatalogCode);
			//Logger.debug(">>REAG:boxCatalogCode="+ru.boxCatalogCode);
			//Logger.debug(">>REAG:reagentCatalogCode="+ru.reagentCatalogCode);
			//Logger.debug(">>REAG:boxCode="+ru.boxCode); 
			//Logger.debug(">>REAG:code="+ ru.code); 
		}
		//-2- chercher dans other ReagentMap	
		else if ( otherCatalog.reagentMap.containsKey(name) ){
			//Logger.debug (" reactif '"+name+"' TROUVE DANS OTHER REAGENT HASHMAP..."); 
			// rien a faire, sera traité lors de l'autre import....
		} 
		else
		{
			//Logger.debug (name+ ": NI DANS CURRENT HASHMAP NI DANS OTHER REAGENT HASHMAP..."); 
			//    1- soit boîte inactive soit dans kit inactif  soit pas relié aux 2 expériences impliquees...
			// ou 2- la boîte n'a pas ete trouvee dans le fichier donc updateExperimentCatalog n'a pas pu faire son travail
				
			if (matchListReagent.size() == 1 ){
				//Logger.debug("1 seul reactif avec ce nom...");
				// un seul reactif avec ce nom, trouver sa boîte parent et verifier si elle est legitime=> dans currCatalog.boxMap 
				// si oui construire un reagentUsed...
				
				for(ReagentCatalog reagent:  matchListReagent) {	
					//Logger.debug("sa boîte est :"+ reagent.boxCatalogCode);
					boolean foundBoxMap=false;
					// !!!! la cle est le nom est pas le code !!!
					 for (Map.Entry<String, BoxCatalog> pair : currCatalog.boxMap.entrySet() ) {
				         //Logger.debug((">>>>"+ pair.getKey()  +  pair.getValue().code);          
				         if  ( pair.getValue().code.equals( reagent.boxCatalogCode ) ) {
				                foundBoxMap=true;
				                break;
				         }
				     }
					 
					 // construire un reagentUsed s'il n'est pas deja dans le hash, le completer sinon... necessaire en V1 car il y a 2 lignes par boîte/reactif 
					 if ( foundBoxMap ) {
						//Logger.debug("boîte "+ reagent.boxCatalogCode +" trouvee dans currCatalog.boxMap => creer le reagentUsed");
						
						ReagentUsed ru=null;
						if (! parsedReagentsMap.containsKey(reagParsedKey) ){	
							ru=new ReagentUsed();  
							ru.kitCatalogCode=reagent.kitCatalogCode;   // code NGL du kit parent
							ru.boxCatalogCode=reagent.boxCatalogCode;   // code NGL de la boîte parent
							ru.reagentCatalogCode=reagent.code;         // code NGL du reactif
							ru.boxCode=null  ;                          // barcode de la boîte parent => mettre null car n'est pas dans le fichier !!
							ru.code=code+"_" ; // !!!! les codes doivent se terminer par "_" pour etre filtrables par la suite
							
							if ( null != cols ) {
								// appel depuis la section position
								// même si a priori la recherche n'est pas possible actuellement sur le champs description, utiliser aussi "_" pour separer les items
								ru.description=fileInputReagentWeight +"_"+ fileOutputReagentWeight +"_"+ fileDiffReagentWeight;		
							}		
						} else {
							//Logger.debug ("mise a jour code de réactif");
							ru= parsedReagentsMap.get(reagParsedKey);
							ru.code=ru.code +code+"_" ;
						}
						
						// ajouter/ecraser dans la map
						parsedReagentsMap.put(reagParsedKey, ru);
							
						//DEBUG	
						//Logger.debug(">>REAG:kitCatalogCode="+ru.kitCatalogCode);
						//Logger.debug(">>REAG:boxCatalogCode="+ru.boxCatalogCode);
						//Logger.debug(">>REAG:reagentCatalogCode="+ru.reagentCatalogCode);
						//Logger.debug(">>REAG:boxCode="+ru.boxCode); 
						//Logger.debug(">>REAG:code="+ ru.code); 
					
					} else {
						//Logger.debug("Boite "+ reagent.boxCatalogCode +"pas trouvee dans currCatalog.boxMap");
						contextValidation.addError("Erreurs fichier","ligne "+ (n+1) +": '"+ name+ "': boîte du réactif inactive ou dans un kit inactif ou non relié aux types d'expériences attendus.");
					}
				}	
			} else {
				//Logger.debug("plusieurs reactifs avec ce nom...");
				contextValidation.addError("Erreurs fichier","ligne "+ (n+1) +": '"+ name+ "': plusieurs réactifs de même nom dans le catalogue et boîte manquante dans le fichier.");
			}
		}
		
		return;
		
	}  // fin method
	
}