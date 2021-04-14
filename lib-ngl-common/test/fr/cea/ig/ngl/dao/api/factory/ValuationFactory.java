package fr.cea.ig.ngl.dao.api.factory;

import java.util.UUID;

import models.laboratory.common.instance.TBoolean;
import models.laboratory.common.instance.Valuation;

/**
 * Factory pour l'entité "Valuation".
 * 
 * @author jcharpen - Jordi Charpentier - jcharpen@genoscope.cns.fr
 */
public final class ValuationFactory {
	
	/**
	 * Méthode permettant de générer un objet "Valuation" aléatoire.
	 * 
	 * @return Un objet "Valuation" aléatoire.
	 */
	public static Valuation getRandomValuation() {
		Valuation valuation = new Valuation();
		
		valuation.user = UUID.randomUUID().toString();
		valuation.comment = UUID.randomUUID().toString();
		valuation.valid = TBoolean.TRUE;
		
		return valuation;
	}
}
