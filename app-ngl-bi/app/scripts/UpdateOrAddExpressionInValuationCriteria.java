package scripts;


import java.io.File;
import java.util.List;
import javax.inject.Inject;

import fr.cea.ig.lfw.controllers.scripts.chunked.ScriptWithArgs;
import models.laboratory.valuation.instance.ValuationCriteria;
import scripts.tools.ValuationCriteriaTools;


/**
 * Script permettant de mettre à jour ou d'ajouter une property dans la table ngl-common.ValuationCriteria à 
 * partir d'un fichier utilisateur de valuationCriteria au format suivant :
 * <pre>{@code
 * valuationCriteriaCode.Propertie.name
 *  - expression_1.result : expression_1.rule
 *  - expression_2.result : expression_2.rule
 *  
 *  Les lignes commencant par # sont ignorées.
 *  
 *  Ex de fichier d'entree :
 *  VC-RMISEQ-PE-301-v2.treatments.ngsrg.default.nbBase.value
 *		-  danger : pValue <= 13200000000
 * 		- success : pValue > 13200000000
 *	VC-RMISEQ-PE-301-v2.treatments.ngsrg.default.percentClusterIlluminaFilter.value
 *		-  danger : pValue <= 80
 *		- success : pValue > 80
 *	VC-Readset-ARC-ARD-v1.treatments.duplicatesRaw.pairs.estimateDuplicatedReadsPercent.value
 *		-  danger : (context.sampleOnContainer.percentage <= 25) ? pValue > 20 : false
 *		- success : (context.sampleOnContainer.percentage <= 25) ? pValue <= 20 : false
 *		- success : (context.sampleOnContainer.percentage > 25) ? pValue <= 20 : false
 *		- warning : (context.sampleOnContainer.percentage > 25) ? pValue > 20 : false
 * 
 * 
 * Declenche une exception si ValuationCriteria fournit par l'utilisateur non valide pour 
 * une mise à jour dans la collection ngl_common.ValuationCriteria
 * 
 * 
 * ex de lancement :
 * http://localhost:9000/scripts/run/scripts.UpdateOrAddExpressionInValuationCriteria?user=sgas&inputFilePath=C:\Users\sgas\sgas_VC_test.txt
 * http://appdev.genoscope.cns.fr:9004/scripts/run/scripts.UpdateOrAddExpressionInValuationCriteria?user=sgas&inputFilePath=/env/cns/tmp/sgas_VC_2021_01_24.txt
 * http://appuat.genoscope.cns.fr:9004/scripts/run/scripts.UpdateOrAddExpressionInValuationCriteria?user=sgas&inputFilePath=/env/cns/tmp/sgas_VC_2020_20_09.txt
 * http://appuatisoprod.genoscope.cns.fr:9104/scripts/run/scripts.UpdateOrAddExpressionInValuationCriteria?user=sgas&inputFilePath=/env/cns/tmp/sgas_VC_2020_20_09.txt
 * http://ngl-bi.genoscope.cns.fr/scripts/run/scripts.UpdateOrAddExpressionInValuationCriteria?user=sgas&inputFilePath=/env/cns/tmp/sgas_VC_2020_20_09.txt
 * }</pre>
 * 
 * @author sgas
 *
 */
public class UpdateOrAddExpressionInValuationCriteria extends ScriptWithArgs<UpdateOrAddExpressionInValuationCriteria.MyParam> {
	private final ValuationCriteriaTools valuationCriteriaTools;

	// constructeur avec injections :
	@Inject
	public UpdateOrAddExpressionInValuationCriteria(
			ValuationCriteriaTools     valuationCriteriaTools) {
		this.valuationCriteriaTools       = valuationCriteriaTools;
	}
	
	// structure de controle et stockage des arguments de l'url
	public static class MyParam {
		public String user;
		public String inputFilePath;
		//public String outputDirPath;
	}
	
		
	@Override
	public void execute(MyParam args) {
		File inputFile = new File (args.inputFilePath);
		
//		if(!args.outputDirPath.endsWith(File.separator)) {
//			args.outputDirPath = args.outputDirPath + File.separator;
//		}
		String user = args.user; 
		//LocalDateTime ldt = LocalDateTime.now();
		//String chaine_dateJour = DateTimeFormatter.ofPattern("yyyy_MM_dd", Locale.FRANCE).format(ldt);
		//List <String> userCodes = new ArrayList<String>();
		try {
			List<ValuationCriteria> listUserValuationCriteria = valuationCriteriaTools.parseUserFileValuationCriteria(inputFile);
			//File outputFile = new File (args.outputDirPath + "ngl_common_ValuationCriteria_avant_modifs_" + chaine_dateJour + "_" + user + ".txt");
			//valuationCriteriaTools.writeDbValuationCriteria(outputFile, userCodes);
			//println("Ecriture du fichier" + outputFile.getPath());
			valuationCriteriaTools.updateOrAddExpressionInValuationCriteria(listUserValuationCriteria, user);
			//outputFile = new File (args.outputDirPath + "ngl_common_ValuationCriteria_apres_modifs_" + chaine_dateJour + "_"  + user +".txt");
			//valuationCriteriaTools.writeDbValuationCriteria(outputFile, userCodes);
			//println("Ecriture du fichier" + outputFile.getPath());
		} catch(Exception e) {
			println("Exception : ");
			println(e.getMessage());
			return;
		}
		println("Fin du traitement");
	}





}
