package fr.cea.ig.ngl.dao.api.factory;

import java.util.Date;
import java.util.UUID;

import models.laboratory.processes.instance.SampleOnInputContainer;

/**
 * Factory pour l'entité "SampleOnInputContainer".
 * 
 * @author jcharpen - Jordi Charpentier - jcharpen@genoscope.cns.fr
 */
public final class SampleOnInputContainerFactory {
	
	/**
	 * Méthode permettant de générer un objet "SampleOnInputContainer" aléatoire.
	 * 
	 * @return Un objet "SampleOnInputContainer" aléatoire.
	 */
	public static SampleOnInputContainer getRandomSampleOnInputContainer() {
		SampleOnInputContainer soic = new SampleOnInputContainer();

		soic.containerCode = UUID.randomUUID().toString();
		soic.containerSupportCode = UUID.randomUUID().toString();
		soic.lastUpdateDate = new Date();
		soic.sampleCode = "test-sample-code";
		soic.sampleTypeCode = UUID.randomUUID().toString();

		return soic;
	}
}
