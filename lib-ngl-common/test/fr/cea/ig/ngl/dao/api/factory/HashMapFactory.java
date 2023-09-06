package fr.cea.ig.ngl.dao.api.factory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

import fr.cea.ig.ngl.utils.TestUtils;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.property.PropertyFileValue;
import models.laboratory.common.instance.property.PropertyImgValue;
import models.laboratory.common.instance.property.PropertySingleValue;

/**
 * Factory pour l'entité "HashMap".
 * 
 * @author jcharpen - Jordi Charpentier - jcharpen@genoscope.cns.fr
 */
public final class HashMapFactory {
	
	/**
	 * Méthode permettant de générer une map (type : "HashMap<String, Long>") correspondant à l'objet "MAP_DURATION" du CRON "ReportingData".
	 * 
	 * @return Une HashMap correspondant à l'objet "MAP_DURATION" du CRON "ReportingData".
	 */
	public static HashMap<String, Long> getMapDuration() {
		HashMap<String, Long> res = new HashMap<>();

		for (int i = 0; i < TestUtils.LIST_SIZE; i++) {
			res.put(UUID.randomUUID().toString(), new Random().nextLong());
		}

		return res;
	}

	/**
	 * Méthode permettant de générer une map (type : "HashMap<String, PropertyValue>") du CRON "ReportingData".
	 * 
	 * @return Une HashMap (type : "HashMap<String, PropertyValue>") du CRON "ReportingData".
	 */
	public static HashMap<String, PropertyValue> getMapProperties() {
		HashMap<String, PropertyValue> res = new HashMap<>();

		for (int i = 0; i < TestUtils.LIST_SIZE; i++) {
			String key = UUID.randomUUID().toString();
			String propValue = UUID.randomUUID().toString();

			PropertyValue value = new PropertySingleValue(propValue);
			res.put(key, value);
		}

		return res;
	}

	/**
	 * Méthode permettant de générer une map (type : "HashMap<String, PropertyValue>") du CRON "ReportingData" avec une image et un fichier.
	 * 
	 * @return Une HashMap (type : "HashMap<String, PropertyValue>") du CRON "ReportingData" avec une image et un fichier.
	 * 
	 * @throws IOException Si le nom du fichier généré est incorrect et qu'une exception est levée.
	 */
	public static HashMap<String, PropertyValue> getMapPropertiesWithFileAndImg() throws IOException {
		HashMap<String, PropertyValue> res = getMapProperties();

		PropertyValue pFileValue = new PropertyFileValue();

		res.put("file", pFileValue);

		PropertyValue pImgValue = new PropertyImgValue();

		res.put("img", pImgValue);

		return res;
	}
}