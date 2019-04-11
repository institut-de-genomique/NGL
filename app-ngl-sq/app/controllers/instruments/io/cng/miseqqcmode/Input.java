package controllers.instruments.io.cng.miseqqcmode;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import au.com.bytecode.opencsv.CSVReader;
import controllers.instruments.io.utils.AbstractInput;
import models.laboratory.common.instance.property.PropertyFileValue;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.experiment.instance.InputContainerUsed;
import validation.ContextValidation;

public class Input extends AbstractInput {
	
	private int icuCodeFound=0;	//FDS ajouter un compteur (propre a chaque instance de classe Input) 
	
	/* Description du fichier a traiter: CSV généré par logiciel du Miseq 
	 * 1 ligne d'entete, colonne 2 ="sample name"
	 * */
	
	@Override
	public Experiment importFile(Experiment experiment,PropertyFileValue pfv, ContextValidation contextValidation) throws Exception {	
			
		Map<String, String[]> allMap = new HashMap<>();
		try (InputStream is = new ByteArrayInputStream(pfv.byteValue());
		     CSVReader reader = new CSVReader(new InputStreamReader(is))) {

			List<String[]> all = reader.readAll();

			all.forEach(array -> {
				//Logger.debug(Arrays.asList(array).toString());
				allMap.put(array[2], array);   //colonne 2 de la ligne=>clé du hash, toute ligne=> value du hash
			});
		}
		
		// reinitialiser le compteur a chaque import
		icuCodeFound=0;
		
		experiment.atomicTransfertMethods.forEach(atm -> {
			InputContainerUsed icu = atm.inputContainerUseds.get(0);
			
			if (allMap.containsKey(icu.code)) {
				String[] data = allMap.get(icu.code);
				
				PropertySingleValue clusterDensity = getPSV(icu, "clusterDensity");
				clusterDensity.value = Integer.parseInt(data[3]);
				
				PropertySingleValue measuredInsertSize = getPSV(icu, "measuredInsertSize");
				measuredInsertSize.value = Integer.parseInt(data[8]);
				
				//FDS 26/08/2016 ajout des autres colonnes; !! cas des decimaux francais...
				PropertySingleValue clusterPercentage = getPSV(icu, "clusterPercentage");
				clusterPercentage.value = Double.parseDouble(data[4].replace (",", "."));
				
				PropertySingleValue passingFilter = getPSV(icu, "passingFilter");
				passingFilter.value = Double.parseDouble(data[5].replace (",", "."));
				
				String[] alignedPercentage =data[6].split("/");	
				PropertySingleValue R1AlignedPercentage = getPSV(icu, "R1AlignedPercentage");
				R1AlignedPercentage.value = Double.parseDouble(alignedPercentage[0].replace (",", "."));
				
				PropertySingleValue R2AlignedPercentage = getPSV(icu, "R2AlignedPercentage");
				R2AlignedPercentage.value = Double.parseDouble(alignedPercentage[1].replace (",", "."));
				
				String[] mismatchPercentage =data[7].split("/");
				PropertySingleValue R1MismatchPercentage = getPSV(icu, "R1MismatchPercentage");
				R1MismatchPercentage.value = Double.parseDouble(mismatchPercentage[0].replace (",", "."));
				
				PropertySingleValue R2MismatchPercentage = getPSV(icu, "R2MismatchPercentage");
				R2MismatchPercentage.value = Double.parseDouble(mismatchPercentage[1].replace (",", "."));
				
				PropertySingleValue minInsertSize = getPSV(icu, "minInsertSize");
				minInsertSize.value = Integer.parseInt(data[9]);
				
				PropertySingleValue maxInsertSize = getPSV(icu, "maxInsertSize");
				maxInsertSize.value = Integer.parseInt(data[10]);
				
				// FDS 22/09/2016 !! NGL-1046 et SUPSQCNG-413 dans certains la valeur necessite un double
				PropertySingleValue observedDiversity = getPSV(icu, "observedDiversity");
				observedDiversity.value = Double.parseDouble(data[11]);
				
				PropertySingleValue estimatedDiversity = getPSV(icu, "estimatedDiversity");
				estimatedDiversity.value = Double.parseDouble(data[12]);
				
				icuCodeFound ++;
			}
		});

		if (icuCodeFound == 0){ contextValidation.addError("Erreurs fichier","experiments.msg.import.data.notmatching");}

		return experiment;
	}
	
}
