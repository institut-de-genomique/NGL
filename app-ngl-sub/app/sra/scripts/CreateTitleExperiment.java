package sra.scripts;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptNoArgs;
import services.sra.VariableSRA;


/*
 * Script a lancer pour lister les refCollab utilisées dans les soumissions
 * Exemple de lancement :
 * http://localhost:9000/sra/scripts/run/sra.scripts.CreateTitleExperiment

 * @author sgas
 *
 */
public class CreateTitleExperiment extends ScriptNoArgs {

	   public static <K, V> Set<K> getKeys(Map<K, V> map, V value)
	    {
	        Set<K> keys = new HashSet<>();
	        for (Map.Entry<K, V> entry: map.entrySet())
	        {
	            if (value.equals(entry.getValue())) {
	                keys.add(entry.getKey());
	            }
	        }
	        return keys;
	    }
	 
	    public static void main(String[] args)
	    {
	        Map<String, Integer> hashMap = new HashMap<String, Integer>();
	        hashMap.put("A", 1);
	        hashMap.put("B", 1);
	        hashMap.put("C", 2);
	 
	        System.out.println(getKeys(hashMap, 1));
	    }


	@Override
	public void execute() throws Exception {
		List<String> cnsNameInstrumentModels = new ArrayList<String>(VariableSRA.mapCnsNameInstrumentModel().keySet());
		//List<String> libProcessTypeCodeVals = new ArrayList<String>(VariableSRA.mapLibProcessTypeCodeVal_orientation().keySet());
		//List<String> titleExpKeys = new ArrayList<String>(VariableSRA.mapExperimentTitle().keySet());
		Set<String> uniqTitles = new HashSet<String>(VariableSRA.mapExperimentTitle().values());
		@SuppressWarnings("unused")
		int cp = 0;
//		for (String value : uniqTitles) {
//			cp++;
//			println(value);
//		}
//		println(cp + " titles differents");
//		
//		cp = 0;
// 		for (String titleExpKey : titleExpKeys) {
// 			cp++;
// 			String titre = VariableSRA.mapExperimentTitle().get(titleExpKey);
// 			println( titleExpKey + " : " + titre);
//		}
//		println(cp + " titles");

		cp = 0;
		String pattern_string = "(.*)(_[^_]+)$";
		Pattern p = Pattern.compile(pattern_string);
		println("Liste des conditions de construction des differents titres des experiments:");
		for (String value : uniqTitles) {
			cp ++;
			List<String> cleTitles = new ArrayList<String>(getKeys(VariableSRA.mapExperimentTitle(), value));
			String messCleTitles = "conditions : ";
			for (String cleTitle : cleTitles) {
				//println("XXXXXXXXXXXXXXXXXX cleTitle=" + cleTitle);
				Matcher m = p.matcher(cleTitle);
				if (m.find()) {
					cleTitle = m.group(1) + m.group(2).toUpperCase();
				}
				messCleTitles += cleTitle + ", ";
			}
			messCleTitles.replaceFirst(", $", " ");
			println(messCleTitles);
			println("titre : '" + value + "'\n");
		}
		
		println("Le mot cle INSTRUMENT dans les titres est remplace par le nom du type d'instrument du run (typeName) qui peut prendre les valeurs suivantes :");
		for (String cnsNameInstrumentModel : cnsNameInstrumentModels) {
			println("'" + VariableSRA.mapCnsNameInstrumentModel().get(cnsNameInstrumentModel) + "'");
			//println("'" + VariableSRA.mapCnsNameInstrumentModel().get(cnsNameInstrumentModel) + "'                   " + VariableSRA.mapInstrumentModel().get(cnsNameInstrumentModel));
			//println("cle='"+ cnsNameInstrumentModel + "' value='" + VariableSRA.mapCnsNameInstrumentModel().get(cnsNameInstrumentModel) + "' valueEbi='" + VariableSRA.mapInstrumentModel().get(cnsNameInstrumentModel) + "'");
			
		}
		println("\n\n");
		println("*************** Fin du traitement ***************");

	}		
}
