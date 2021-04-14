package fr.cea.ig.ngl.dao.api.factory;

import java.util.UUID;

import models.laboratory.common.description.PropertyDefinition;

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
}
