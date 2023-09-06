package fr.cea.ig.ngl.dao.api.factory;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.RandomStringUtils;

import fr.cea.ig.ngl.utils.TestUtils;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.sample.instance.Sample;

/**
 * Factory pour l'entité "Sample".
 * 
 * @author jcharpen - Jordi Charpentier - jcharpen@genoscope.cns.fr
 */
public final class SampleFactory {

	/**
	 * Méthode permettant de générer un code sample aléatoire.
	 * 
	 * @return Une chaine de caractères aléatoire ayant le format d'une code sample.
	 */
	public static String getRandomSampleCode() {
		return RandomStringUtils.randomAlphabetic(3) + "_" + RandomStringUtils.randomAlphabetic(4);
	}
	
	/**
	 * Méthode permettant de mettre à jour un mock de "Sample".
	 * Utile dans certains cas bien précis où on doit mocker un sample mais avec des valeurs.
	 * 
	 * @param sample L'objet à mettre à jour.
	 * 
	 * @return Un objet "Sample" mis à jour.
	 */
	public static Sample fillRandomSample(Sample sample) {
		sample = new Sample();
		
		sample.code = "AEG_CR";
		sample.typeCode = "total-RNA";
		sample.categoryCode = "RNA";
		sample.name = "CR";
		sample.referenceCollab = "Acinetobacter_baylyi_ADP1";
		sample.taxonCode = "472";
		sample.ncbiLineage = "cellular organisms; Bacteria; Proteobacteria; Gammaproteobacteria; Pseudomonadales; Moraxellaceae; Acinetobacter";
		sample.ncbiScientificName = "Acinetobacter sp.";

		Set<String> pc = new HashSet<String>();
		pc.add("AEG");

		sample.projectCodes = pc;
		
		return sample;
	}

	/**
	 * Méthode permettant de générer une liste d'objets "Sample" aléatoire.
	 * 
	 * @return Une liste d'objets "Sample" aléatoire.
	 */
	public static List<Sample> getRandomSampleList() {
		List<Sample> res = new ArrayList<>();

		for (int i = 0; i < TestUtils.LIST_SIZE; i++) {
			res.add(getRandomSample(new Date(), true));
		}

		return res;
	}

	/**
	 * Méthode permettant de générer un objet "Sample" aléatoire.
	 * 
	 * @param date La date de création à mettre au sample.
	 * @param emptyProcess Booléen permettant de savoir si on doit générer un "process" ou pas.
	 * 
	 * @return Un objet "Sample" aléatoire.
	 */
	public static Sample getRandomSample(Date date, boolean emptyProcess) {
		Sample sample = new Sample();

		TraceInformation traceInformation = new TraceInformation();
		traceInformation.createUser = TestUtils.CURRENT_USER;
		traceInformation.modifyUser = TestUtils.CURRENT_USER;
		traceInformation.creationDate = date;
		traceInformation.modifyDate = date;

		sample.traceInformation = traceInformation;
		
		sample.code = "AEG_CR";
		sample.typeCode = "total-RNA";
		sample.categoryCode = "RNA";
		sample.name = "CR";
		sample.referenceCollab = "Acinetobacter_baylyi_ADP1";
		sample.taxonCode = "472";
		sample.ncbiLineage = "cellular organisms; Bacteria; Proteobacteria; Gammaproteobacteria; Pseudomonadales; Moraxellaceae; Acinetobacter";
		sample.ncbiScientificName = "Acinetobacter sp.";

		Set<String> pc = new HashSet<String>();
		pc.add("AEG");

		sample.projectCodes = pc;

		if (!emptyProcess) {
			sample.processes = SampleProcessFactory.getRandomSampleProcessList();
		} 
		
		return sample;
	}

	/**
	 * Méthode permettant de générer une liste de code sample aléatoires.
	 * 
	 * @return Une liste de chaîne de caractères correspondant à liste de code sample.
	 */
	public static List<String> getRandomSampleCodesList() {
		List<String> list = new ArrayList<>();

		for (int i = 0; i < TestUtils.LIST_SIZE; i++) {
			list.add(getRandomSampleCode());
		}

		return list;
	}

	/**
	 * Méthode permettant de générer un Hashset de code sample aléatoires.
	 * 
	 * @return Un Hashset de chaîne de caractères correspondant à liste de code sample.
	 */
	public static HashSet<String> getRandomSampleCodesHashset() {
		HashSet<String> list = new HashSet<>();

		for (int i = 0; i < TestUtils.LIST_SIZE; i++) {
			list.add(getRandomSampleCode());
		}

		return list;
	}
}
