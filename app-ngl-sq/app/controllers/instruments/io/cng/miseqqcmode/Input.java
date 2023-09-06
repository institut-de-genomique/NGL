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

/**
 * Fonctionnement initial: le logiciel MSR (MiSeq Reporte) produit 1 seul fichier global CSV à importer 
 * Description du fichier: 
 * 1 ligne d'entete: #,Sample ID,Sample Name,Clusters Raw,%Cluster,%PF,%Aligned,%Mismatch,Median Len,Min Len,Max Len,Observed Diversity,Estimated Diversity,Genome
 *                   0     1          2         3              4    5     6        7          8         9      10         11                    12            13
 */
public class Input extends AbstractInput {
	
	private int icuCodeFound=0;	//FDS ajouter un compteur (propre a chaque instance de classe Input) 
	
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
				
				PropertySingleValue clusterDensity = getOrCreatePSV(icu, "clusterDensity");
				clusterDensity.value = Integer.parseInt(data[3]);
				
				PropertySingleValue measuredInsertSize = getOrCreatePSV(icu, "measuredInsertSize");
				measuredInsertSize.value = Integer.parseInt(data[8]);
				
				//FDS 26/08/2016 ajout des autres colonnes; !! cas des decimaux francais...
				PropertySingleValue clusterPercentage = getOrCreatePSV(icu, "clusterPercentage");
				clusterPercentage.value = Double.parseDouble(data[4].replace (",", "."));
				
				PropertySingleValue passingFilter = getOrCreatePSV(icu, "passingFilter");
				passingFilter.value = Double.parseDouble(data[5].replace (",", "."));
				
				String[] alignedPercentage =data[6].split("/");	
				PropertySingleValue R1AlignedPercentage = getOrCreatePSV(icu, "R1AlignedPercentage");
				R1AlignedPercentage.value = Double.parseDouble(alignedPercentage[0].replace (",", "."));
				
				PropertySingleValue R2AlignedPercentage = getOrCreatePSV(icu, "R2AlignedPercentage");
				R2AlignedPercentage.value = Double.parseDouble(alignedPercentage[1].replace (",", "."));
				
				String[] mismatchPercentage =data[7].split("/");
				PropertySingleValue R1MismatchPercentage = getOrCreatePSV(icu, "R1MismatchPercentage");
				R1MismatchPercentage.value = Double.parseDouble(mismatchPercentage[0].replace (",", "."));
				
				PropertySingleValue R2MismatchPercentage = getOrCreatePSV(icu, "R2MismatchPercentage");
				R2MismatchPercentage.value = Double.parseDouble(mismatchPercentage[1].replace (",", "."));
				
				PropertySingleValue minInsertSize = getOrCreatePSV(icu, "minInsertSize");
				minInsertSize.value = Integer.parseInt(data[9]);
				
				PropertySingleValue maxInsertSize = getOrCreatePSV(icu, "maxInsertSize");
				maxInsertSize.value = Integer.parseInt(data[10]);
				
				// FDS 22/09/2016 !! NGL-1046 et SUPSQCNG-413 dans certains la valeur necessite un double
				PropertySingleValue observedDiversity = getOrCreatePSV(icu, "observedDiversity");
				observedDiversity.value = Double.parseDouble(data[11]);
				
				PropertySingleValue estimatedDiversity = getOrCreatePSV(icu, "estimatedDiversity");
				estimatedDiversity.value = Double.parseDouble(data[12]);
				
				icuCodeFound ++;
			}
		});

		if (icuCodeFound == 0){ contextValidation.addError("Erreurs fichier","experiments.msg.import.data.notmatching");}

		return experiment;
	}
	
}
