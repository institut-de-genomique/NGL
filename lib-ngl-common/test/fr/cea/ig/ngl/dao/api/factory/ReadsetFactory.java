package fr.cea.ig.ngl.dao.api.factory;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.run.instance.SampleOnContainer;
import models.laboratory.run.instance.Treatment;
import play.Logger;

/**
 * Factory pour l'entité "ReadSet".
 * 
 * @author jcharpen - Jordi Charpentier - jcharpen@genoscope.cns.fr
 */
public final class ReadsetFactory {
	
	/**
	 * Méthode permettant de générer un objet "Readset" aléatoire.
	 * 
	 * @param withId Booléen permettant de savoir si le readset généré a un id ou non.
	 * 
	 * @return Un objet "Readset" aléatoire.
	 */
	public static ReadSet getRandomReadset(boolean withId) {
		ReadSet readset = new ReadSet();

		if (withId) {
			readset._id = "5332e1554b931680506cc691";
		}

		readset.code = "ARG_APDOSW_3_64MD3AAXX.IND4";
		readset.runCode = "110913_BISMUTH_64MD3AAXX";
		
		DateFormat df = new SimpleDateFormat();
		
		try {
			readset.runSequencingStartDate = df.parse("2011-09-12T22:00:00.000+0000");
		} catch (ParseException e) {
			Logger.debug("Erreur de date. On met une date par défaut.");
			readset.runSequencingStartDate = new Date();
		}
		
		readset.runTypeCode = "RGAIIx";
		readset.sampleCode = "ARG_APD";
		
		Map<String, Treatment> treatments = new HashMap<String, Treatment>();		
		readset.treatments = treatments;

		readset.projectCode = "BCE";
		
		readset.typeCode = "rsillumina";
		readset.location = "CNS";
		readset.state = StateFactory.getRandomState();
		
		return readset;
	}
	
	/**
	 * Méthode permettant de remplir un readset avec des informations nécessaires au test unitaire.
	 * On remplit un reaset mocké (par exemple) car si on le mock juste avec Mockito, il est vide.
	 * 
	 * @param readset Le readset à remplir.
	 * 
	 * @return Le readset initial "rempli" avec les informations nécessaires au test unitaire.
	 */
	public static ReadSet fillReadset(ReadSet readset) {
		readset = new ReadSet();	
		readset.code = "TESTJORDI";

		// SampleOnContainer.
		
		Map<String,PropertyValue> properties = new HashMap<String, PropertyValue>();
		properties.put("taxonSize", new PropertySingleValue(1000));
		
		SampleOnContainer soc = new SampleOnContainer();
		soc.properties = properties;
		
		readset.sampleOnContainer = soc;
		
		// Traitement 1.
		
		Map<String, PropertyValue> value1 = new HashMap<String, PropertyValue>();
		value1.put("usefulBases", new PropertySingleValue(1234));
		
		Map<String, Map<String, PropertyValue>> results1 = new HashMap<>();
		results1.put("default", value1);
		
		Treatment treatment1 = new Treatment();
		treatment1.results = results1;
		
		// Traitement 2.
		
		Map<String, PropertyValue> value2 = new HashMap<String, PropertyValue>();
		value2.put("removedReadsPercent", new PropertySingleValue(1234.1234));
		
		Map<String, Map<String, PropertyValue>> results2 = new HashMap<>();
		results2.put("pairs", value2);
		
		Treatment treatment2 = new Treatment();
		treatment2.results = results2;
		
		// Traitement 3.
		
		Map<String, PropertyValue> value3 = new HashMap<String, PropertyValue>();
		value3.put("removedReadsPercent", new PropertySingleValue(1234.1234));
		
		Map<String, Map<String, PropertyValue>> results3 = new HashMap<>();
		results3.put("pairs", value3);
		
		Treatment treatment3 = new Treatment();
		treatment3.results = results3;
		
		// Traitement 4.
		
		Map<String, PropertyValue> value4 = new HashMap<String, PropertyValue>();
		value4.put("estimateDuplicatedReadsPercent", new PropertySingleValue(1234.1234));
		
		Map<String, Map<String, PropertyValue>> results4 = new HashMap<>();
		results4.put("pairs", value4);
		
		Treatment treatment4 = new Treatment();
		treatment4.results = results4;
		
		Map<String, Treatment> treatments = new HashMap<>();
		treatments.put("global", treatment1);
		treatments.put("contaminationColi", treatment2);
		treatments.put("contaminationPhiX", treatment3);
		treatments.put("duplicatesRaw", treatment4);
		
		readset.treatments = treatments;
		
		return readset;
	}
}