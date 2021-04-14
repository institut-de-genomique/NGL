package fr.cea.ig.ngl.dao.api.factory;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.RandomStringUtils;

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
	 * Méthode permettant de générer un objet "Sample" aléatoire.
	 * 
	 * @return Un objet "Sample" aléatoire.
	 */
	public static Sample getRandomSample() {
		Sample sample = new Sample();
		
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
}
