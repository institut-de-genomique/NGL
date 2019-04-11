package scripts;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

import javax.inject.Inject;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.mongojack.DBQuery;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.Script;
import fr.cea.ig.ngl.NGLConfig;
import controllers.commons.api.Parameters;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.parameter.Parameter;
import models.laboratory.parameter.index.Index;
import models.laboratory.parameter.index.IlluminaIndex;
import models.laboratory.parameter.index.NanoporeIndex;
import models.utils.InstanceConstants;
import models.utils.InstanceHelpers;
import play.Logger;
import play.mvc.Http.RequestBody;
import validation.ContextValidation;

/**
 * Script permettant d'importer dans la collection Parameter des index de séquencage a partir d'un fichier Excel<br>
 *    A       B           C         D         E           F            G               H<br>
 * |code   |name   |categoryCode|sequence|shortName|supplierName     |supplier-indexName|groupNames                   | ---- header<br>
 * |nfrs-01|Nfrs-01|SINGLE-INDEX|CGTGAT  |IND399   |Bioo Scientifique|PCR Primer 1      |NEXTflex Small RNA V1.1,Test2| ---- data<br>
 * 
 *  code: OBLIG- identifiant UNIQUE!!! en minuscule<br>
 *  name: OBLIG- nom pour l'utilisateur<br>
 *  categoryCode: OBLIG- CNG: DUAL-INDEX/SINGLE-INDEX/MID; CNS: DUAL-INDEX/SINGLE-INDEX/MID/POOL-INDEX<br>
 *  sequence: OBLIG peut contenir "-"(1 pour DUAL-INDEX) ou plusieurs "-"(3 pour POOL-INDEX)<br>
 *  shortName: OPTIO- automatiquement calculé au CNG pour le sequencage illumina. doit etre fourni dans les autres cas<br>
 *  supplierName et supplier-indexName peuvent etre manquants tous les 2. mais supplierName peut etre present seul<br>
 *  groupNames: OBLIG -liste des groupes auquel appartient l'index (kits au CNRGH...): valeurs separees par ","<br>
 *
 * parametre typeCode recuperé dans l'URL : index-illumina-sequencing  ou index-nanopore-sequencing<br>
 * le fichier est dans l'element "xlsx" de la form multipart<br>
 * l'onglet avec les donneées doit s'appeler "index"
 * 
 * Exemple d'appel avec curl:
 * 
 * curl -X POST -H 'User-Agent: bot' -H 'Content-Type: multipart/form-data' \
 * -F xlsx=@/home/fdsantos/MONGODB/fichiers_index/Index_test_insert.xlsx \
 * -i 'http://localhost:9000/scripts/run/scripts.InsertSequencingIndex?typeCode=index-nanopore-sequencing' 
 *
 * @author fdsantos
 */

public class InsertSequencingIndex extends Script<InsertSequencingIndex.Args> {
	// structure de controle et stockage des arguments attendus dans l'url. Declarer les champs public.
	public static class Args {
		public String typeCode;
	}
	
	//private final Parameters param; // Il faut passer par l'API de Parameters...pas d'API pour Index actuellement
	private final NGLConfig config; // pour recuperer l'institute

	@Inject
	public InsertSequencingIndex(Parameters param, NGLConfig config) {
		//this.param  = param;
		this.config = config;
	}

	// les 2 methodes execute() DOIVENT etre fournies.... sera changé avec modifications des classes faites par Jean...
	@Override
	public void execute(Args args) throws Exception {
		getLogger().info("no simple execution implemented");
	}

	@Override
	public void execute(Args args, RequestBody body) throws Exception {
		
		// le fichier est récuperé dans le POST MultipartFormData dans 'xlsx'
		File fxlsx = (File) body.asMultipartFormData().getFile("xlsx").getFile();
		FileInputStream fis = new FileInputStream(fxlsx);

		XSSFWorkbook workbook = new XSSFWorkbook (fis);// workbook du fichier
		List<String> failedIndex = new ArrayList<>();  // Liste de index dont les informations sont incorrectes
	
		System.out.println("Inserting sequencing index from file");// peut sortir dans un navigateur...
		getLogger().info("Inserting sequencing index from file");
		// note: le nom du fichier n'est pas informatif..... +fxlsx.getName());
		
		switch(args.typeCode) {
		   case "index-illumina-sequencing" :
		   case "index-nanopore-sequencing" :
			   System.out.println("typeCode="+args.typeCode);// peut sortir dans un navigateur...
			   getLogger().info("typeCode="+args.typeCode);
		       break; 
		   default : 
			   ///Logger.error("unsupported typeCode:"+ args.typeCode );
			   throw new Exception("unsupported typeCode:"+ args.typeCode);
		}
		
		//l'onglet (sheet) DOIT s'appeler "index"
		try {
			XSSFSheet sheet=workbook.getSheet("index");
			if ( null != sheet ) {	
				//failedIndex = insertIndexes(ctxVal, sheet, args.typeCode);
				failedIndex = insertIndexes(sheet, args.typeCode);
				if(failedIndex.size() != 0) {
					System.out.println("Some indexes have not been imported:");// peut sortir dans un navigateur...
					System.out.println(failedIndex.toString());
					
					getLogger().warn("Some indexes have not been imported:");
					getLogger().warn(failedIndex.toString()); // affichage de la liste des index non crees
				} else {
					System.out.println("All indexes have been imported.");// peut sortir dans un navigateur...
					getLogger().info("All indexes have been imported.");
				}
			} else {
				throw new Exception("sheet 'index' not found.");
			}
	  } catch (Exception e) {
		getLogger().error(e.getMessage(), e);
		System.out.println(e.getMessage());
	  }
	}

	/** 
	 * @param sheet            Onglet Excel a traiter
	 * @param typeCode         type d'index
	 * @return  liste des index non-inseres
	 */
	private List<String> insertIndexes(XSSFSheet sheet, String typeCode ) {
		List<String> failedIndex = new ArrayList<>();
		final String user = "ngl-admin";
		
		String institute= config.getInstitute();
		//String institute="CNS";                     // pour debug=> ne pas passer dans les methodes specifiques CNG (calcul de shortName)
		
		ContextValidation ctxVal = ContextValidation.createCreationContext(user);

		sheet.rowIterator().forEachRemaining(row -> {
			if (row.getRowNum() == 0) {return; }// skip header 
			
			int excelRow=row.getRowNum()+1; // pour message avec numeros de ligne explicite
			//System.out.println("DEBUG----------------- LIGNE:"+ excelRow +"---------------------");

			// mieux a faire ???
			Index index =null;
			switch(typeCode) {
					case "index-illumina-sequencing" :
						index = new IlluminaIndex(); 
						break; 
					case "index-nanopore-sequencing" :
						index = new NanoporeIndex(); 
						break; 
					// default : deja traité dans execute....
			}
			
			//-1- recuperer les valeurs dans le fichier
			
			index.code=getValFromRow(row, 0,"code", true);                  if ( null==index.code) { failedIndex.add(index.code); return;}         // col A=0
			index.name=getValFromRow(row, 1,"name", true);                  if ( null==index.name) { failedIndex.add(index.code); return;}         // col B=1
			index.categoryCode =getValFromRow(row, 2,"categoryCode", true); if ( null==index.categoryCode) { failedIndex.add(index.code); return;} // col C=2
			index.sequence     =getValFromRow(row, 3,"sequence",true);      if ( null==index.sequence) { failedIndex.add(index.code); return;}     // col D=3
			
			if ( institute.equals("CNG") && typeCode.equals("index-illumina-sequencing") ) {                                                       // col E=4
				index.shortName=getCNGIlluminaIndexShortName (index.sequence, index.categoryCode, excelRow );
				if ( null == index.shortName) { failedIndex.add(index.code); return;}
			} else {
				index.shortName=getValFromRow(row, 4,"shortName",true);   if ( null==index.shortName) { failedIndex.add(index.code); return;} 
			} 
			String suppName = getValFromRow(row, 5,"supplierName",false);                                                                          // col F=5
			String suppIndexName = getValFromRow(row, 6,"index-supplierName",false);                                                               // col G=6
			if ( null != suppName) {  
				Map<String, String> map = new HashMap<>();
				map.put(suppName,suppIndexName);
				index.supplierName=map;
			} else {
				if ( null != suppIndexName) {
					getLogger().warn("line ("+excelRow+"): Index name without supplierName is ignored.");
					failedIndex.add(index.code); 
					return;
				}
			}
			// groupNames est obligatoire !!
			if ( null == getValFromRow(row, 7,"groupNames", true)) { failedIndex.add(index.code); return;}                                         //col H=7
			else {
				String[] groups=getValFromRow(row, 7,"groupNames", false).split(",");
				index.groupNames=Arrays.asList(groups);
			}
			
			//-2- controles...
			// --a-- code doit etre unique donc ne pas deja exister dans la collection
			if (MongoDBDAO.checkObjectExistByCode(InstanceConstants.PARAMETER_COLL_NAME, Parameter.class, index.code)){
				getLogger().error("line ("+excelRow+"): index code'" + index.code + "' already exists"); 
				failedIndex.add(index.code);
				return;
			}
			
			// --b--  tester unicité du "name" vu que c'est ce que l'utilisateur tape dans les experiences ou il attribue un index a un container....
			if (null != MongoDBDAO.findOne(InstanceConstants.PARAMETER_COLL_NAME, Parameter.class, DBQuery.is("name", index.name)) ) {
				getLogger().error("line ("+excelRow+"): index name'" + index.name + "' already exists"); 
				failedIndex.add(index.code);
				return;
			}
			
			// --c-- NGL-2450  shortName et sequence doivent etre unique pour une categorie  d'index donné
			// ne pas utilise findOne car plusieurs resultats possibles....!!! HISTORIQUE, il y a des erreurs ... 
			List<Parameter> Parameters= MongoDBDAO.find(InstanceConstants.PARAMETER_COLL_NAME, Parameter.class, 
                   DBQuery.is("sequence", index.sequence).and(DBQuery.is("categoryCode", index.categoryCode)).and(DBQuery.notEquals("shortName", index.shortName))).toList();
			if ( ! Parameters.isEmpty() )
			{
				//=> il y a deja une sequence identique mais sous un autre shortName dans cette categorie => probleme !!
				getLogger().error("line ("+excelRow+"): index with same sequence and different shortName already exists in category "+index.categoryCode ); 
				failedIndex.add(index.code);
				return;
			}
			
			//-3- insertion
			getLogger().info("line ("+excelRow+") inserting index '"+ index.code+ "'");
			
			index.traceInformation=new TraceInformation();
			index.setTraceCreationStamp(ctxVal, user); 
			
			InstanceHelpers.save(InstanceConstants.PARAMETER_COLL_NAME,index,ctxVal);
			if ( ctxVal.hasErrors() ) {
				getLogger().error("line ("+excelRow+"): index '" + index.code + "' cannot be saved"); 
				getLogger().error("line ("+excelRow+"): index '" + ctxVal.getErrors());
				failedIndex.add(index.code);
			}
			
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
			return row.getCell(colPos).getStringCellValue(); 
		} else { 
			if (mandatory ) {
				getLogger().error( "line ("+excelRow+"), col "+ (char)(65+colPos)+": "+colName+ " missing value"); 
			}
			return null;
		}
	}
	/**
	 *  Retourne le prochain ShortName quand il est calculable
	 *  => uniquement pour index-illumina-sequencing et CNG
	 *  
	 * @param sequence          séquence de l'index
	 * @param categoryCode      category de l'index
	 * @param excelRow          numero de ligne explicite pour messages
	 * @return   shortName calculé
	 */
	private String getCNGIlluminaIndexShortName (String sequence, String categoryCode, int excelRow) {
		String shortName=null;
		
		// sequence et category ne sont pas unique=> peut retourner une liste!!
		Iterator<Index> it1 =MongoDBDAO.find(InstanceConstants.PARAMETER_COLL_NAME, Index.class, 
				DBQuery.is("typeCode","index-illumina-sequencing").is("categoryCode", categoryCode).is("sequence", sequence)).cursor.iterator();
		if ( it1.hasNext() ) {
			getLogger().debug("line ("+ excelRow+ "): sequence '"+ sequence +"'already in DB");
			// sequence deja existante =>recuperer le shortname corrrespondant au premier index trouvé ( 1 SEQ = 1 SHORTNAME !! )
			shortName=it1.next().shortName; 
			getLogger().debug("line ("+ excelRow+ "): reusing existing shortName="+ shortName);
		} else {
			Logger.debug("line ("+ excelRow+ ") new sequence :"+ sequence );
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
					getLogger().error("line ("+ excelRow+ "): '"+ categoryCode + "' unsupported CNG index categoryCode");
					// serait traité a la validation...oui mais plutot on le sait...mieux ca vaut
			}
			
			if ( null != prefix ) {
				// trouver le shortName le plus élevé existant dans la base pour cette categorie
				// !!!! avec .cursor.forEach  il n'est pas possible d'utiliser une variable interne...
				// Nicolas Wiart=> utliser Iterator + while  
				getLogger().debug("prefix="+prefix);
				
				int max=0; 
				int next=0;
				Iterator<Index> it2 = MongoDBDAO.find(InstanceConstants.PARAMETER_COLL_NAME, Index.class, 
						DBQuery.is("typeCode","index-illumina-sequencing").is("categoryCode", categoryCode)).cursor.iterator();
				while(it2.hasNext()) {
					String tmp[]=it2.next().shortName.split(prefix);
					// il pourrait y avoir des shortNames mal formés !!!! apres <prefix> il faut imperativement un nombre..
					if ( tmp[1].matches("[0-9]+") ){
						int num =Integer.parseInt(tmp[1]);
						//Logger.debug("num="+ num);
						if ( num > max){ max=num;}
					} else {
						//sortir en erreur !!!
						getLogger().info ("Incorrectly formed index shortName found in collection. Abort computing shortName...");
						max=0;
						break;
					}
				}
					
				if ( max != 0) {
					next=max+1;
					shortName=prefix+next;
				}
			} 
		}
		
		getLogger().info ("line ("+ excelRow+ ") CNG shortName="+shortName);
		return shortName;
	}
	
}
