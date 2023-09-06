package fr.cea.ig.ngl.dao.api.factory;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.RandomStringUtils;

import fr.cea.ig.ngl.utils.TestUtils;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.TraceInformation;
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
	 * @param date La date de création à mettre au readset généré.
	 * 
	 * @return Un objet "Readset" aléatoire.
	 */
	public static ReadSet getRandomReadset(boolean withId, Date date) {
		ReadSet readset = new ReadSet();
		
		TraceInformation traceInformation = new TraceInformation();
		traceInformation.createUser = TestUtils.CURRENT_USER;
		traceInformation.modifyUser = TestUtils.CURRENT_USER;
		traceInformation.creationDate = date;
		traceInformation.modifyDate = date;

		readset.traceInformation = traceInformation;
		
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
		treatments.put("test", TreatmentFactory.getRandomTreatment());
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

	/**
	 * Méthode permettant de générer une liste d'objets "ReadSet" aléatoire.
	 * 
	 * @param date La date de création à mettre pour le readset.
	 * 
	 * @return Une liste d'objets "ReadSet" aléatoire.
	 */
	public static List<ReadSet> getRandomReadsetList(Date date) {
		List<ReadSet> res = new ArrayList<>();

		for (int i = 0; i < TestUtils.LIST_SIZE; i++) {
			res.add(getRandomReadset(true, date));
		}

		return res;
	}

	/**
	 * Méthode permettant de générer un code readset aléatoire en gardant le format "RANDOM CODE PROJET-RANDOM CODE SAMPLE_ILLUMINA-RUN_RANDOM CODE CONTAINER".
	 * "_ILLUMINA-RUN_" n'est pas aléatoire pour le test, mais possible de le faire évoluer.
	 * 
	 * @return Un code readset aléatoire.
	 */
	public static String getRandomReadsetCode() {
		String rsCode = RandomStringUtils.random(3, true, false) + "_" + RandomStringUtils.random(6, true, false) + "_" + RandomStringUtils.random(1, false, true) + "_" + RandomStringUtils.random(9, true, true) + ".IND" + RandomStringUtils.random(1, false, true);

		return rsCode;
	}

	/**
	 * Méthode permettant de générer une liste de code readset aléatoires.
	 * 
	 * @return Une liste de chaîne de caractères correspondant à liste de code readset.
	 */
	public static List<String> getRandomReadsetCodesList() {
		List<String> list = new ArrayList<>();

		for (int i = 0; i < TestUtils.LIST_SIZE; i++) {
			list.add(getRandomReadsetCode());
		}

		return list;
	}
}