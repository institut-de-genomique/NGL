package scripts;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Pattern;

import javax.inject.Inject;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.mongojack.DBQuery;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.chunked.ScriptWithArgsAndBody;
import fr.cea.ig.ngl.NGLConfig;
import controllers.commons.api.Parameters;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.parameter.Parameter;
import models.laboratory.parameter.index.Index;
import models.laboratory.parameter.index.MGIIndex;
import models.laboratory.parameter.index.IlluminaIndex;
import models.laboratory.parameter.index.NanoporeIndex;
import models.laboratory.parameter.index.PacBioIndex;
import models.utils.InstanceConstants;
import models.utils.InstanceHelpers;
import play.mvc.Http.RequestBody;
import validation.ContextValidation;

/**
 * Script permettant d'importer dans la collection Parameter des index de séquencage a partir d'un fichier Excel<br>
 *    A       B           C         D         E           F                    G               H<br>
 * |code   |name   |categoryCode|sequence|shortName|supplierName     |supplier-indexName|groupNames                   | -- header ( 1 ligne)<br>
 * |nfrs-01|Nfrs-01|SINGLE-INDEX|CGTGAT  |IND399   |Bioo Scientifique|PCR Primer 1      |NEXTflex Small RNA V1.1,Test2| -- data ( n lignes)<br>
 * 
 *  code: OBLIG- identifiant UNIQUE!!!<br>
 *  name: OBLIG- nom affiché a l'utilisateur UNIQUE!!!<br>
 *  categoryCode: OBLIG- liste actuelle: SINGLE-INDEX/DUAL-INDEX/POOL-INDEX/MID<br>
 *  sequence: OBLIG peut contenir "-" (1 pour DUAL-INDEX, 3 pour POOL-INDEX)<br>
 *  shortName: OPTIO- automatiquement calculé au CNG pour le séquencage illumina si categoryCode=SINGLE-INDEX, DUAL-INDEX ou MID<br>
 *                    (NB dans ce cas s'il est présent une alerte affiche qu'il sera ignoré)
 *                    doit etre fourni dans les autres cas (erreur si manquant)<br>
 *  supplierName et supplier-indexName peuvent etre manquants tous les 2. mais supplierName peut etre présent seul<br>
 *  groupNames: OBLIG -liste des groupes auquel appartient l'index (kits au CNRGH...): valeurs séparées par ","<br>
 *  
 * Règle additionnelle (NGL-2450): le couple shortName + séquence doit être unique pour une catégorie d'index donné<br>
 * ( cette règle ne s'applique que quand le shortName n'est pas automatiquement calculée...)
 * 
 * L'onglet Excel avec les donneées DOIT s'appeler "index"<br>
 * 
 * Le paramètre typeCode est récuperé dans l'URL : index-illumina-sequencing ou index-nanopore-sequencing ou index-mgi-sequencing ou index-pacbio-sequencing<br>
 * Le fichier est récupéré dans l'élément "xlsx" de la form multipart<br>
 * 
 * booleen simul: pour insertion ou non (introduit avec ticket NGL-3299)
 * booleen auto : pour calcul automatique des shortName pour le CNS introduit avec ticket NGL-3334)
 * 
 * Exemple d'appel avec curl:<br>
 * ( attention le  "ET commercial" ne pas a la javadoc  dans l'url ci dessous remplacer ET par "ET commercial")
 * 
 * curl -X POST -H 'User-Agent: bot' -H 'Content-Type: multipart/form-data' \<br>
 * -F xlsx=@/home/fdsantos/MONGODB/fichiers_index/Index_test_insert.xlsx \<br>
 * -i 'http://localhost:9000/scripts/run/scripts.InsertSequencingIndex?typeCode=index-nanopore-sequencingETsimul=falseETauto=true'<br>
 *
 * @author fdsantos
 */

public class InsertSequencingIndex extends	 ScriptWithArgsAndBody<InsertSequencingIndex.Args> {
	// structure de controle et stockage des arguments attendus dans l'url. Declarer les champs public.
	public static class Args {
		public String typeCode;
		public boolean simul;     // ajout NGL-3299 pour simulation insertion
		public boolean auto;      // ajout NGL-3334 pour calculer les shortName automatiquement au CNS aussi ( toujours vrai pour CNG)
	}
	
	private final NGLConfig config; // pour récupérer l'institute

	// !! pour l'instant les index sont des Parameters...
	@Inject
	public InsertSequencingIndex(Parameters param, NGLConfig config) {
		this.config = config;
	}

	@Override
	public void execute(Args args, RequestBody body) throws Exception {
		
		try {
			// le fichier est récuperé dans le POST MultipartFormData dans 'xlsx'
			File fxlsx = (File) body.asMultipartFormData().getFile("xlsx").getFile();
			FileInputStream fis = new FileInputStream(fxlsx);

			XSSFWorkbook workbook = new XSSFWorkbook (fis);// workbook du fichier
			List<String> failedIndex = new ArrayList<>();  // Liste des index dont les informations sont incorrectes
	
			// note: le nom du fichier n'est pas informatif..... +fxlsx.getName());
			//println("Inserting sequencing index from file");
		
			switch(args.typeCode) {
				case "index-illumina-sequencing" :
				case "index-nanopore-sequencing" :
				case "index-mgi-sequencing" :
				case "index-pacbio-sequencing" : 
					println("typeCode="+args.typeCode);
					break; 
				default : 
					throw new Exception("unsupported typeCode:"+ args.typeCode);
			}
			
			//l'onglet (sheet) DOIT s'appeler "index"
			try {
				XSSFSheet sheet=workbook.getSheet("index");
				if ( null != sheet ) {	
					failedIndex = insertIndexes(sheet, args.typeCode, args.simul, args.auto);
					if(failedIndex.size() != 0) {
						println("Warning: some indexes have not been imported:");
						println(failedIndex.toString()); // affichage de la liste des index non crees
					} else {
						println("All indexes have been imported.");
					}
				} else {
					throw new Exception("sheet 'index' not found.");
				}
			} catch (Exception e) {
				println(e.getMessage());
			}
		} catch (Exception e) {
			// pas un  fichier Excel...
			println(e.getMessage());
		}
	}

	/** 
	 * @param sheet            Onglet Excel a traiter
	 * @param typeCode         type d'index
	 * @param simul            flag pour insertion ou non
	 * @param auto             flag pour calcul automatique des shortNames ( qq soit la valeur, pour CNG toujours vrai !)
	 * @return  liste des index non-insérés
	 */
	private List<String> insertIndexes(XSSFSheet sheet, String typeCode, boolean simul, boolean auto ) {
		List<String> failedIndex = new ArrayList<>();
		final String user = "ngl-admin";
		
		// map pour contournement NGL-2455: doublons de shortName 
		// => pour une sequence forcer un shortName au lieu d'essayer de le trouver dans la base
		Map<String, String> mapNGL2455= new HashMap<>();
		
		mapNGL2455.put("TAAGGCGA-GCGTAAGA","DUAL228");// remplace DUAL313 à supprimer!!
		mapNGL2455.put("CGTACTAG-GCGTAAGA","DUAL336");// remplace DUAL314 à supprimer!!
		mapNGL2455.put("AGGCAGAA-GCGTAAGA","DUAL344");// remplace DUAL315 à supprimer!!
		mapNGL2455.put("TCCTGAGC-GCGTAAGA","DUAL352");// remplace DUAL316 à supprimer!!
		mapNGL2455.put("GGACTCCT-GCGTAAGA","DUAL360");// remplace DUAL317 à supprimer!!
		mapNGL2455.put("TAGGCATG-GCGTAAGA","DUAL368");// remplace DUAL318 à supprimer!!
		mapNGL2455.put("CTCTCTAC-GCGTAAGA","DUAL376");// remplace DUAL319 à supprimer!!
		mapNGL2455.put("CGAGGCTG-GCGTAAGA","DUAL384");// remplace DUAL322 à supprimer!!
		mapNGL2455.put("AAGAGGCA-GCGTAAGA","DUAL392");// remplace DUAL323 à supprimer!!
		mapNGL2455.put("GTAGAGGA-GCGTAAGA","DUAL400");// remplace DUAL324 à supprimer!!
		
		String institute= config.getInstitute();
		////  PB !!! if (institute.equals("CNG")) { auto=true; } // au CNG toujours calculer les shortNames quand c'est possible (IND, DUAL, MID)

		sheet.rowIterator().forEachRemaining(row -> {
			ContextValidation ctxVal = ContextValidation.createCreationContext(user);// doit etre dans la boucle
			
			if (row.getRowNum() == 0) {return; }// sauter la ligne header
			
			int excelRow=row.getRowNum()+1; // pour message avec numéro de ligne explicite
			
			// mieux a faire ???
			Index index =null;
			switch(typeCode) {
					case "index-illumina-sequencing" :
						index = new IlluminaIndex(); 
						break; 
					case "index-nanopore-sequencing" :
						index = new NanoporeIndex(); 
						break; 
					case "index-mgi-sequencing" :
						index = new MGIIndex(); 
						break; 
					case "index-pacbio-sequencing" :
						index = new PacBioIndex(); 
						break; 
					// default : deja traité dans execute....
			}
			
			//-1- récupérer les valeurs dans le fichier
			
			index.code=getValFromRow(row, 0,"code", true);                  if ( null==index.code) { failedIndex.add(index.code); return;}         // col A=0
			println(">> Processing line ("+excelRow+"): code="+index.code);
			
			index.name=getValFromRow(row, 1,"name", true);                  if ( null==index.name) { failedIndex.add(index.code); return;}         // col B=1
			index.categoryCode =getValFromRow(row, 2,"categoryCode", true); if ( null==index.categoryCode) { failedIndex.add(index.code); return;} // col C=2
			index.sequence     =getValFromRow(row, 3,"sequence",true);      if ( null==index.sequence) { failedIndex.add(index.code); return;}     // col D=3
			
			// 17/03/2021 déplacé ici car un cas spécial est apparu pour les index "Chromium" (NGL-3205)
			Boolean isChromium=false;
			// groupNames est obligatoire !!
			if ( null == getValFromRow(row, 7,"groupNames", true)) { failedIndex.add(index.code); return;}                                         //col H=7
			else {
				String[] groups=getValFromRow(row, 7,"groupNames", false).split(",");
				index.groupNames=Arrays.asList(groups);
				
				if (getValFromRow(row, 7,"groupNames", true).matches("(.*)Chromium(.*)")) { 
					isChromium=true;
					println("line ("+excelRow+"): groupNames isChromium !!");
				}
			}
			
			// pas de calcul de shortName pour les POOL-INDEX
			// NGL-3205  pas de calcul non plus pour les index "Chromium" [ !!! il y a des Chromium POOL-INDEX et DUAL-INDEX  attention a l'ordre du test ]
			
			//NGL-3334 faire les attributions automatiques de shortName aussi pour le CNS si auto=true
			if ( (auto || institute.equals("CNG") ) &&
				 typeCode.equals("index-illumina-sequencing") && 
				 ! isChromium &&
				 ! index.categoryCode.equals("POOL-INDEX") ) {
				// si shortName present=> warning...
				if ( null != getValFromRow(row, 4,"shortName",false)) { println("line ("+excelRow+"): Warning ! shortName will be computed, provided ("+ getValFromRow(row, 4,"shortName",false) +") value is ignored.");} // col E=4
				index.shortName=getIlluminaIndexShortName (index.sequence, index.categoryCode, excelRow );
				if ( null == index.shortName) { failedIndex.add(index.code); return;}
			} else {
				// donc on est ici dans les cas :  auto=false
				//                                 typeCode=index-nanopore-sequencing
				//                                 isChromium
				//                                 categoryCode=POOL-INDEX                                 
				// ==> le shortName fourni sera utilisé !!
				
				index.shortName=getValFromRow(row, 4,"shortName",true);   
				if ( null==index.shortName) { failedIndex.add(index.code); return;} // shortName DOIT exister
			} 
			println("line ("+excelRow+"): shortName="+index.shortName);
			
			String suppName = getValFromRow(row, 5,"supplierName",false);                                                                          // col F=5
			String suppIndexName = getValFromRow(row, 6,"index-supplierName",false);                                                               // col G=6
			if ( null != suppName) {  
				index.supplierName=suppName;
				index.supplierIndexName=suppIndexName;
			} else {
				if ( null != suppIndexName) {
					println("line ("+excelRow+"): Index name without supplierName is ignored.");
					failedIndex.add(index.code); 
					return;
				}
			}
			
			//-2- controles...
			// --a-- code doit etre unique donc ne pas déja exister dans la collection
			if (MongoDBDAO.checkObjectExistByCode(InstanceConstants.PARAMETER_COLL_NAME, Parameter.class, index.code)){
				println("line ("+excelRow+"): index code '" + index.code + "' already exists"); 
				failedIndex.add(index.code);
				return;
			}
			
			// --b--  tester unicité du "name" vu que c'est ce que l'utilisateur tape dans les experiences ou il attribue un index a un container....
			if (null != MongoDBDAO.findOne(InstanceConstants.PARAMETER_COLL_NAME, Parameter.class, DBQuery.is("name", index.name)) ) {
				println("line ("+excelRow+"): index name '" + index.name + "' already exists"); 
				failedIndex.add(index.code);
				return;
			}
			
			// --c-- NGL-2450 le couple shortName + sequence doit être unique pour une catégorie d'index donné
			// ne pas utiliser findOne car plusieurs résultats possibles....!!! HISTORIQUE, il y a des erreurs => NGL-2455
			List<Parameter> Parameters= MongoDBDAO.find(InstanceConstants.PARAMETER_COLL_NAME, Parameter.class, 
                   DBQuery.is("sequence", index.sequence).and(DBQuery.is("categoryCode", index.categoryCode)).and(DBQuery.notEquals("shortName", index.shortName))).toList();
			if ( ! Parameters.isEmpty() )
			{
				//=> il y a deja une séquence identique mais sous un autre shortName dans cette catégorie => erreur !!
				println("line ("+excelRow+"): index with same sequence and different shortName already exists in category "+index.categoryCode ); 
				
				////  traiter le cas des doublons listés dans le ticket NGL-2455
				////  si la sequence correspond=> forcer un shortName au lieu d'utiliser celui obtenu plus haut avec getCNGIlluminaIndexShortName
				////     autre solution incorporer la map dans getCNGIlluminaIndexShortName et ne plus faire ce test d'unicité --c--???? oui  mais y avoir d'autres doublons
				////     que ceux connus dans NGL-2455=> continuer a faire ce test !!!
				String NGL2455ShortName=getNGL2455ShortName(index.sequence, mapNGL2455) ;
				if ( null == NGL2455ShortName ) { 
					// on n'est pas dans le cas NGL-2455 sortir en erreur
					failedIndex.add(index.code); 
					return;
				}
				else {
					println("line ("+excelRow+"): forcing shortName="+ NGL2455ShortName);
					index.shortName=NGL2455ShortName;
				}
			}
						
			//NGL-3334 dans le cas ou l'attribution du shortName est décidé par l'utilisateur
			//  ==> 2 sequences différentes on reçu le meme shortName donc le test inverse doit aussi être fait !!!
			Parameters= MongoDBDAO.find(InstanceConstants.PARAMETER_COLL_NAME, Parameter.class, 
	                   DBQuery.is("shortName", index.shortName).and(DBQuery.is("categoryCode", index.categoryCode)).and(DBQuery.notEquals("sequence", index.sequence))).toList();
				if ( ! Parameters.isEmpty() )
				{
					println("line ("+excelRow+"): index with same shortName and different séquence already exists in category "+index.categoryCode ); 
					
					// et le problème NGL-2455 ?????????????
					
					failedIndex.add(index.code); 
					return;
				}
			
			//-3- insertion
			if ( ! simul ) {
				println("=> Inserting index");
			
				index.traceInformation=new TraceInformation();
				index.setTraceCreationStamp(ctxVal, user); 
			
				InstanceHelpers.save(InstanceConstants.PARAMETER_COLL_NAME,index,ctxVal);
				if ( ctxVal.hasErrors() ) {
					println("line ("+excelRow+"): index '" + index.code + "' cannot be inserted"); 
					println("errors:" + ctxVal.getErrors());
					failedIndex.add(index.code);
				}
			} else { println("=> SIMULATION: index not inserted"); }
			
		});
		return failedIndex;
	}	
	
	/**
	 * @param row      ligne de l'onglet Excel
	 * @param colPos   position decimale (0->N )
	 * @param colName  Nom de la colonne (devrait etre le meme que celui du row 0...)
	 * @return   valeur de type String ou null si cellule vide
	 */
	private String getValFromRow (Row row, int colPos, String colName,boolean mandatory) {
		int excelRow=row.getRowNum()+1;
		if ( null != row.getCell(colPos, Row.RETURN_BLANK_AS_NULL) ) { 
			return row.getCell(colPos).getStringCellValue().trim(); 
		} else { 
			if (mandatory ) {
				println( "line ("+excelRow+"), col ("+ (char)(65+colPos)+" / "+colName+ "): missing value"); 
			}
			return null;
		}
	}
	
	/**
	 *  Retourne ShortName CNG pour index-illumina-sequencing
	 *     si la séquence est déja connue retourne le shortName correspondant du premier index trouvé
	 *     si nouvelle séquence incrémenter de +1 la partie numérique du shortName le plus élevé du categoryCode demandé
	 *        (pour l'instant seuls SINGLE-INDEX, DUAL-INDEX, MID ont des shortNames calculables)
	 *  
	 * @param sequence          séquence de l'index
	 * @param categoryCode      category de l'index
	 * @param excelRow          numero de ligne explicite pour messages
	 * @return shortName
	 */
	private String getIlluminaIndexShortName (String sequence, String categoryCode, int excelRow) {
		String shortName=null;
		
		// sequence et category ne sont pas unique=> peut retourner une liste!!
		Iterator<Index> it1 =MongoDBDAO.find(InstanceConstants.PARAMETER_COLL_NAME, Index.class, 
				DBQuery.is("typeCode","index-illumina-sequencing").is("categoryCode", categoryCode).is("sequence", sequence)).cursor.iterator();
		if ( it1.hasNext() ) {
			println("line ("+ excelRow+ "): sequence '"+ sequence +"' already in DB");
			// sequence deja existante =>recuperer le shortname corrrespondant au premier index trouvé ( 1 SEQ = 1 SHORTNAME !! )
			shortName=it1.next().shortName; 
			println("line ("+ excelRow+ "): reusing existing shortName="+ shortName);
		} else {
			println("line ("+ excelRow+ "): new sequence :'"+ sequence+"'" );
			// nouvelle sequence => calculer le  shortname 
			String prefix=null;
			switch(categoryCode) {
				case "SINGLE-INDEX" :
					prefix="IND";
					break; 
				case "DUAL-INDEX" :
					prefix="DUAL";
					break;
				case "MID" :
					prefix="MID";
					break; 
				default : 
					// erreur unsupported categoryCode  !!!!
					println("line ("+ excelRow+ "): '"+ categoryCode + "' unsupported CNG index categoryCode");
					// serait traité a la validation...oui mais plutot on le sait...mieux ca vaut
			}
			
			if ( null != prefix ) {
				// trouver le shortName le plus élevé existant dans la base pour cette categorie
				// !!!! avec .cursor.forEach  il n'est pas possible d'utiliser une variable interne...Nicolas Wiart=> utliser Iterator + while  
				//println("DEBUG/ prefix="+prefix);
				
				int max=0; 
				int next=0;
				//  !!! au CNS les shortNames historique ne suivent pas les règles de nommages 
				// ==> ajouter.regex("shortName", Pattern.compile(prefix)) pour ne prendre que ceux qui matche avec <prefix>
				Iterator<Index> it2 = MongoDBDAO.find(InstanceConstants.PARAMETER_COLL_NAME, Index.class, 
						//DBQuery.is("typeCode","index-illumina-sequencing").is("categoryCode", categoryCode)).cursor.iterator();
						DBQuery.is("typeCode","index-illumina-sequencing").is("categoryCode", categoryCode).regex("shortName", Pattern.compile(prefix))).cursor.iterator();
						
				while(it2.hasNext()) {
					//println("DEBUG/...."+it2.next().shortName );
					String parts[]=it2.next().shortName.split(prefix); //ne marche que si shortName contient <prefix> !!!
					
					// il pourrait y avoir des shortNames mal formés !!!! apres <prefix> il faut  un nombre si on veut pouvoir l'incrémenter
					if ( parts[1].matches("[0-9]+") ){
						int num =Integer.parseInt(parts[1]);  // au CNS on trouve DUAL001, 002   etc   IND40b !!!
						//println("DEBUG/ num="+ num);
						if ( num > max){ max=num;}
					} else {
						//sortir en erreur !!! NON SINON NE MARCHE PLUS !!!
						//println("Incorrectly formed index shortName found in database. Abort computing shortName...");
						//max=0;
						//break;
						println(parts[1]+" :index shortName part cannot be used to compute next value. Skipping...");
					}		
				}
					
				if ( max != 0) {
					next=max+1;
					shortName=prefix+next;
				}
			} 
		}
		
		//println("DEBUG/line ("+ excelRow+ "): shortName='"+shortName+"'");
		return shortName;
	}
	
	// si la séquence est une clé de la map, retourner le shortName correspondant
	private String getNGL2455ShortName (String sequence, Map<String, String> mapNGL2455 ) {
		for (Map.Entry<String, String> entry : mapNGL2455.entrySet()) {
            if (entry.getKey().equals(sequence)) return entry.getValue();
        }
		return null;
	}
}