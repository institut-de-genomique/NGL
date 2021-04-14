package fr.cea.ig.ngl.dao.api.factory;

import java.util.UUID;

import models.laboratory.run.instance.InstrumentUsed;

/**
 * Factory pour l'entité "InstrumentUsed".
 * 
 * @author jcharpen - Jordi Charpentier - jcharpen@genoscope.cns.fr
 */
public final class InstrumentUsedFactory {
	
	/**
	 * Méthode permettant de générer un objet "InstrumentUsed" aléatoire.
	 * 
	 * @return Un objet "InstrumentUsed" aléatoire.
	 */
	public static InstrumentUsed getRandomInstrumentUsed() {
		InstrumentUsed instUsed = new InstrumentUsed();

		instUsed.code = UUID.randomUUID().toString();
		instUsed.typeCode = UUID.randomUUID().toString();

		return instUsed;
	}
}
