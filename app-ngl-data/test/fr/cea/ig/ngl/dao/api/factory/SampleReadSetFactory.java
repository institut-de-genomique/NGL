package fr.cea.ig.ngl.dao.api.factory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import fr.cea.ig.ngl.TestUtils;
import models.laboratory.sample.instance.reporting.SampleReadSet;

/**
 * Factory pour l'entité "SampleReadSetFactory".
 * 
 * @author jcharpen - Jordi Charpentier - jcharpen@genoscope.cns.fr
 */
public final class SampleReadSetFactory {

	/**
	 * Méthode permettant de générer un objet "SampleReadSet" aléatoire.
	 * 
	 * @return Un objet "SampleReadSet" aléatoire.
	 */
	public static SampleReadSet getRandomSampleReadSet() {
		SampleReadSet sampleReadSet = new SampleReadSet();

		sampleReadSet.code = UUID.randomUUID().toString();
		sampleReadSet.runCode = UUID.randomUUID().toString();
		sampleReadSet.runTypeCode = UUID.randomUUID().toString();
		sampleReadSet.state = StateFactory.getRandomState();
		sampleReadSet.typeCode = UUID.randomUUID().toString();

		return sampleReadSet;
	}

	/**
	 * Méthode permettant de générer une liste d'objets "SampleReadSet" aléatoire.
	 * 
	 * @return Une liste d'objets "SampleReadSet" aléatoire.
	 */
	public static List<SampleReadSet> getRandomSampleReadSetList() {
		List<SampleReadSet> list = new ArrayList<>();

		for (int i = 0; i < TestUtils.LIST_SIZE; i++) {
			list.add(getRandomSampleReadSet());
		}

		return list;
	}
}
