package fr.cea.ig.ngl.dao.api.factory.propertyfilevalue;

import com.google.common.io.Files;

import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.property.PropertyFileValue;

public class PropertyFileValueFactory {
	
	/**
	 * Méthode permettant de générer un objet "PropertyFileValue" aléatoire.
	 * 
	 * @param fullname Valeur du champ fullname.
	 * 
	 * @return Un objet "PropertyFileValue" aléatoire.
	 */
	public static PropertyFileValue getRandomPropertyFileValue(String fullname) {
		PropertyFileValue pfv = new PropertyFileValue();

		pfv.fullname = fullname;
		pfv.extension = Files.getFileExtension(fullname);
		pfv._type = PropertyValue.fileType;

		return pfv;
	}
	
	public static PropertyFileValue getRandomPropertyFileValue(String fullname, String text) {
		PropertyFileValue pfv = getRandomPropertyFileValue(fullname);
		pfv.value = text.getBytes();

		return pfv;
	}

}
