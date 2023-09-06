package fr.cea.ig.ngl.dao.api.factory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import fr.cea.ig.ngl.TestUtils;
import models.laboratory.sample.instance.reporting.SampleProcess;

/**
 * Factory pour l'entité "SampleProcess".
 * 
 * @author jcharpen - Jordi Charpentier - jcharpen@genoscope.cns.fr
 */
public final class SampleProcessFactory {

	/**
	 * Méthode permettant de générer un objet "SampleProcess" aléatoire.
	 * 
	 * @return Un objet "SampleProcess" aléatoire.
	 */
	public static SampleProcess getRandomSampleProcess() {
		SampleProcess sampleProcess = new SampleProcess();

		sampleProcess.categoryCode = UUID.randomUUID().toString();
		sampleProcess.code = UUID.randomUUID().toString();
		sampleProcess.currentExperimentTypeCode = UUID.randomUUID().toString();
		sampleProcess.typeCode = UUID.randomUUID().toString();
		sampleProcess.state = StateFactory.getRandomState();

		sampleProcess.readsets = SampleReadSetFactory.getRandomSampleReadSetList();

		return sampleProcess;
	}

	/**
	 * Méthode permettant de générer une liste d'objets "SampleProcess" aléatoire.
	 * 
	 * @return Une liste d'objets "SampleProcess" aléatoire.
	 */
	public static List<SampleProcess> getRandomSampleProcessList() {
		List<SampleProcess> list = new ArrayList<>();

		for (int i = 0; i < TestUtils.LIST_SIZE; i++) {
			list.add(getRandomSampleProcess());
		}

		return list;
	}
}
