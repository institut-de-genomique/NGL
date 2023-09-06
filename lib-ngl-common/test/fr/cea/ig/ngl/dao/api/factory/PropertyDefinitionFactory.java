package fr.cea.ig.ngl.dao.api.factory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import models.laboratory.common.description.PropertyDefinition;
import models.laboratory.common.description.Value;
import scala.util.Random;

/**
 * Factory pour l'objet "PropertyDefinition".
 * 
 * @author jcharpen - Jordi Charpentier - jcharpen@genoscope.cns.fr
 */
public final class PropertyDefinitionFactory {

	/**
	 * Méthode permettant de générer un objet "PropertyDefinition" aléatoire.
	 * 
	 * @param isActive Valeur du champ isActive à forcer dans la propertyDefinition.
	 * 
	 * @return Un objet "PropertyDefinition" aléatoire.
	 */
	public static PropertyDefinition getRandomPropertyDefinition(boolean isActive) {
		PropertyDefinition pDef = new PropertyDefinition();

		pDef.code = UUID.randomUUID().toString();
		pDef.description = UUID.randomUUID().toString();
		pDef.displayFormat = UUID.randomUUID().toString();
		pDef.required = Boolean.TRUE;
		pDef.active = isActive;

		return pDef;
	}

	/**
	 * 
	 * 
	 * @return
	 */
	public static List<PropertyDefinition> getPropertyDefinitionListWithAnalysisTypes() {
		List<PropertyDefinition> propDef = new ArrayList<PropertyDefinition>();

		PropertyDefinition propDef1 = new PropertyDefinition();
		propDef1.code = "analysisTypes";

		List<Value> lValues = new ArrayList<>();
		Value value = getValue();
		lValues.add(value);

		propDef1.possibleValues = lValues;

		propDef.add(propDef1);

		return propDef;
	}

	/**
	 * Méthode permettant de générer un objet 'Value' qui représente la valeur d'une propriété.
	 * 
	 * @return Un objet 'Value' représentant la valeur d'une propriété.
	 */
	public static Value getValue() {
		Value value = new Value();

		value.code = UUID.randomUUID().toString();
		value.defaultValue = Boolean.TRUE;
		value.name = UUID.randomUUID().toString();
		value.id = new Random().nextLong();

		return value;
	}
}
