package fr.cea.ig.ngl.dao.api.factory;

import java.util.UUID;

import models.laboratory.run.instance.Treatment;

/**
 * Factory pour l'entité "Treatment".
 * 
 * @author jcharpen - Jordi Charpentier - jcharpen@genoscope.cns.fr
 */
public final class TreatmentFactory {
	
	/**
	 * Méthode permettant de générer un traitement aléatoire.
	 * 
	 * @return Un objet "Treatment" aléaoire.
	 */
	public static Treatment getRandomTreatment() {
		Treatment tr = new Treatment();
		
		tr.code = UUID.randomUUID().toString();
		tr.categoryCode = UUID.randomUUID().toString();
		tr.typeCode = UUID.randomUUID().toString();

		return tr;
	}
}
