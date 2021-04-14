package scripts;



import java.io.File;

import java.util.List;
import javax.inject.Inject;
import fr.cea.ig.lfw.controllers.scripts.chunked.ScriptWithArgs;
import scripts.tools.ValuationCriteriaTools;
import scripts.tools.Tools;

/**
 * Ecrit par ordre alphabetique, dans le fichier de sortie, les ValuationCriteria de la table ngl_common.ValuationCriteria 
 * avec ses properties et expressions associées (format plus compact que le json). Le parametre fileCodes permet d'indiquer 
 * les codes des valuationCriterias que l'on veut voir apparaitre dans le fichier de sortie. Si le fichier fileCodes n'existe pas
 * alors ce sont toutes les valuationCriterias de la collection qui seront dans le fichier de sortie.
 * En faisant tourner le script en dev et en prod, on peut ensuite comparer les fichiers de sortie
 * avec winMerge. Comparaison de ces fichiers plus lisible que la comparaison les fichiers json des 2 bases.
 * 
 * Attention à indiquer le chemin d'un fichier de sortie accessible en ecriture pour cnsnglapps si on
 * fait tourner le script sur un serveur (En localhost, le script tourne sous l'identité du user 
 * alors que sur les autres serveurs, il tourne sous l'identité de cnsnglapps)
 * Pour ecrire la collection complete indiquez en parametre fileCodes=null, 
 * sinon indiquez le chemin complet du fichier avec les codes de valuationCriteria à ecrire.
 * 
 * ex de fichier de sortie :
 * <pre>{@code
 *  VC-RMISEQ-PE-301-v2.treatments.ngsrg.default.nbBase.value
 *		-  danger : pValue <= 13200000000
 * 		- success : pValue > 13200000000
 *	VC-RMISEQ-PE-301-v2.treatments.ngsrg.default.percentClusterIlluminaFilter.value
 *		-  danger : pValue <= 80
 *		- success : pValue > 80
 * }</pre>

 * #--------------------------------------------
 * # Bilan des valuationCriterias : 
 * #--------------------------------------------
 * # Nombre de valuationCriteria :    51
 * # Nombre de properties        :   634
 * # Nombre expressions          :  1305
 *
 * 
 * <pre>{@code
 * ex de lancement :
 * http://localhost:9000/scripts/run/scripts.WriteValuationCriteriaExpressions?fileCodes=C:\Users\sgas\valuationCriteria\202101\novaseqCodes.txt&outputFilePath=C:\Users\sgas\valuationCriteria\VC_TEST.txt 
 * http://localhost:9000/scripts/run/scripts.WriteValuationCriteriaExpressions?fileCodes=null&outputFilePath=C:\Users\sgas\valuationCriteria\VC_dev_cns_20200920.txt
 * http://appdev.genoscope.cns.fr:9004/scripts/run/scripts.WriteValuationCriteriaExpressions?fileCodes=null&outputFilePath=/env/cns/tmp/VC_dev_cns_20200920.txt
 * http://appuat.genoscope.cns.fr:9004/scripts/run/scripts.WriteValuationCriteriaExpressions?fileCodes=null&outputFilePath=/env/cns/tmp/VC_uat_cns_20200920.txt
 * http://appuatisoprod.genoscope.cns.fr:9104/scripts/run/scripts.WriteValuationCriteriaExpressions?fileCodes=null&outputFilePath=/env/cns/tmp/VC_uatisoprod_cns_20200920.txt
 * http://ngl-bi.genoscope.cns.fr/scripts/run/scripts.WriteValuationCriteriaExpressions?fileCodes=null&outputFilePath=/env/cns/tmp/VC_PROD_cns_20200920.txt  
 * }</pre>
 * @author sgas
 *
 */
	public class WriteValuationCriteriaExpressions extends ScriptWithArgs<WriteValuationCriteriaExpressions.MyParam> {
		private final ValuationCriteriaTools valuationCriteriaTools;		
		// constructeur avec injections :
		@Inject
		public WriteValuationCriteriaExpressions(
				ValuationCriteriaTools     valuationCriteriaTools) {
			this.valuationCriteriaTools       = valuationCriteriaTools;
		}
		
		// structure de controle et stockage des arguments de l'url
		public static class MyParam {
			public String outputFilePath;
			public String fileCodes;
		}
		
		@Override
		public void execute(MyParam args) throws Exception {
			File outputFile = new File (args.outputFilePath);
			println("args.fileCodes = '" + args.fileCodes + "'");
			List <String> userCodes = new Tools().parseFileCodes(new File(args.fileCodes));
			valuationCriteriaTools.writeDbValuationCriteria(outputFile, userCodes);
			println("Fin du traitement");
			
		}

}

