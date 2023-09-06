package fr.cea.ig.ngl.dao.api.factory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import org.apache.commons.lang.RandomStringUtils;

import fr.cea.ig.ngl.TestUtils;
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
			res.add(ProcessFactory.getRandomProcess(true, false, true));
		}

		return res;
	}

	/**
	 * Méthode permettant de générer un objet "Process" aléatoire.
	 * 
	 * @param withId Si on veut un process avec un _id.
	 * @param hasExpCodes Si on veut un process avec des codes expériences.
	 * @param hasOcuCodes Si on veut un process avec des codes output container.
	 * 
	 * @return Un objet "Process" aléatoire.
	 */
	public static Process getRandomProcess(boolean withId, boolean hasExpCodes, boolean hasOcuCodes) {
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

		Set<String> ocuCodes = new HashSet<>();

		if (hasOcuCodes) {
			ocuCodes.add(ContainerFactory.getRandomContainerCode());
		}

		process.outputContainerCodes = ocuCodes;

		Set<String> ocusCodes = new HashSet<>();

		if (hasOcuCodes) {
			ocusCodes.add(ContainerFactory.getRandomContainerCode());
		}

		process.outputContainerSupportCodes = ocusCodes;

		process.sampleCodes = SampleFactory.getRandomSampleCodesHashset();

		Set<String> projectCodes = new HashSet<>();
		projectCodes.add("CEA");
		
		process.projectCodes = projectCodes;

		process.state = StateFactory.getRandomState();

		return process;
	}
	
	/**
	 * Méthode permettant de générer un code process aléatoire en gardant le format "RANDOM CODE PROJET-RANDOM CODE SAMPLE_ILLUMINA-RUN_RANDOM CODE CONTAINER".
	 * "_ILLUMINA-RUN_" n'est pas aléatoire pour le test, mais possible de le faire évoluer.
	 * 
	 * @return Un code process aléatoire.
	 */
	public static String getRandomProcessCode() {
		String processCode = RandomStringUtils.random(3, true, false) + "_" + RandomStringUtils.random(3, true, false) + "_ILLUMINA-RUN_" + RandomStringUtils.random(9, true, true);

		return processCode;
	}

	/**
	 * Méthode permettant de générer une liste de code process aléatoires.
	 * 
	 * @return Une liste de chaîne de caractères correspondant à liste de code process.
	 */
	public static TreeSet<String> getRandomProcessCodesList() {
		TreeSet<String> list = new TreeSet<>();

		for (int i = 0; i < TestUtils.LIST_SIZE; i++) {
			list.add(getRandomProcessCode());
		}

		return list;
	}
}