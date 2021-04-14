package fr.cea.ig.ngl.dao.api.factory;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.cea.ig.ngl.utils.TestUtils;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.run.instance.Analysis;
import models.laboratory.run.instance.Treatment;

/**
 * Factory pour l'entité "Analysis".
 * 
 * @author jcharpen - Jordi Charpentier - jcharpen@genoscope.cns.fr
 */
public final class AnalysisFactory {
	
	/**
	 * Méthode permettant de mettre à jour un mock de "Analysis".
	 * Utile dans certains cas bien précis où on doit mocker une analyse mais avec des valeurs.
	 * 
	 * @param analysis L'objet à mettre à jour.
	 * 
	 * @return Un objet "Analysis" mis à jour.
	 */
	public static Analysis fillRandomAnalysis(Analysis analysis) {
		analysis = getRandomAnalysis();

		return analysis;
	}

	/**
	 * Méthode permettant de générer un objet "Analysis" aléatoire.
	 * 
	 * @return Un objet "Analysis" aléatoire.
	 */
	public static Analysis getRandomAnalysis() {
		Analysis analysis = new Analysis();
		analysis.code = "BA.BFY_AAAAOSF_1_A737Y.IND1";

		analysis.traceInformation = new TraceInformation();
		analysis.traceInformation.createUser = TestUtils.CURRENT_USER;
		analysis.traceInformation.creationDate = new Date();
		
		List<String> masterRSCodes = new ArrayList<String>();
		masterRSCodes.add("BFY_AAAAOSF_1_A737Y.IND1");
		
		analysis.masterReadSetCodes = masterRSCodes;
		
		Map<String, Treatment> treatments = new HashMap<String, Treatment>();
		
		Treatment t1 = new Treatment();
		Map<String, Map<String, PropertyValue>> result1 = new HashMap<String, Map<String,PropertyValue>>();
		Map<String, PropertyValue> val1 = new HashMap<String, PropertyValue>();
		val1.put("storedBases", new PropertySingleValue(1));
		result1.put("pairs", val1);
		t1.results = result1;
		treatments.put("contigFilterBA", t1);
		
		Treatment t2 = new Treatment();
		Map<String, Map<String, PropertyValue>> result2 = new HashMap<String, Map<String,PropertyValue>>();
		Map<String, PropertyValue> val2 = new HashMap<String, PropertyValue>();
		val2.put("assemblyContigSize", new PropertySingleValue(1));
		result2.put("pairs", val2);
		t2.results = result2;
		treatments.put("assemblyBA", t2);
		
		analysis.treatments = treatments;
		
		return analysis;
	}
}
