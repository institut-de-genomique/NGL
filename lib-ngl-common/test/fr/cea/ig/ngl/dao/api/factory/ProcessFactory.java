package fr.cea.ig.ngl.dao.api.factory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import fr.cea.ig.ngl.utils.TestUtils;
import models.laboratory.processes.instance.Process;

/**
 * Factory pour l'entité "Process".
 * 
 * @author jcharpen - Jordi Charpentier - jcharpen@genoscope.cns.fr
 */
public final class ProcessFactory {
	
	/**
	 * Méthode permettant de générer une liste d'objets "Process" aléatoires.
	 * 
	 * @return Une liste aléatoire d'objets "Process".
	 */
	public static List<Process> getRandomProcessList() {
		List<Process> res = new ArrayList<>();

		for (int i = 0; i < TestUtils.LIST_SIZE; i++) {
			res.add(ProcessFactory.getRandomProcess(true, false));
		}

		return res;
	}

	/**
	 * Méthode permettant de générer un objet "Process" aléatoire.
	 * 
	 * @param withId Si on veut un process avec un _id.
	 * @param hasExpCodes Si on veut un process avec des codes expériences.
	 * 
	 * @return Un objet "Process" aléatoire.
	 */
	public static Process getRandomProcess(boolean withId, boolean hasExpCodes) {
		Process process = new Process();

		if (withId) {
			process._id = UUID.randomUUID().toString();
		}

		process.code = "OPGEN-RUN-BHG-BHG_B-20140711102151572";
		process.typeCode = "opgen-run";
		process.categoryCode = "mapping";
		process.inputContainerCode = "BHG_B1";
		process.inputContainerSupportCode = "BHG_B1";

		Set<String> expCodes = new HashSet<>();

		if (hasExpCodes) {
			expCodes.add("NANOPORE-DEPOT-20200818_190409CBG");
		}

		process.experimentCodes = expCodes;

		process.state = StateFactory.getRandomState();

		return process;
	}
}
