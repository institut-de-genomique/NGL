package fr.cea.ig.ngl.dao.api.factory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import fr.cea.ig.ngl.utils.TestUtils;
import models.laboratory.container.instance.Content;

/**
 * Factory pour l'entité "Content".
 * 
 * @author jcharpen - Jordi Charpentier - jcharpen@genoscope.cns.fr
 */
public final class ContentFactory {
	
	/**
	 * Méthode permettant de générer un objet "Content" aléatoire.
	 * 
	 * @param hasValidPercentage Booléen permettant de savoir si on veut générer des content 
	 * avec un pourcentage valide ou non (selon les cas de tests).
	 * 
	 * @return Un objet "Content" aléatoire.
	 */
	public static Content getRandomContent(boolean hasValidPercentage) {
		Content content = new Content();

		content.ncbiScientificName = UUID.randomUUID().toString();
		content.projectCode = UUID.randomUUID().toString();
		content.referenceCollab = UUID.randomUUID().toString();
		content.taxonCode = UUID.randomUUID().toString();
		content.sampleTypeCode = UUID.randomUUID().toString();
		content.sampleCategoryCode = UUID.randomUUID().toString();
		content.sampleCode = UUID.randomUUID().toString();

		Double percentage = 0.0;
			
		if (hasValidPercentage) {
			percentage = 100.0 / TestUtils.LIST_SIZE;
		} else {
			percentage = 30.0;
		}

		content.percentage = percentage;

		return content;
	}

	/**
	 * Méthode permettant de générer une liste de content aléatoires.
	 * 
	 * @param hasValidPercentage Booléen permettant de savoir si on veut générer des content 
	 * avec un pourcentage valide ou non (selon les cas de tests).
	 * 
	 * @return Une liste de content aléatoire.
	 */
	public static List<Content> getRandomContentList(boolean hasValidPercentage) {
		List<Content> res = new ArrayList<>();

		for (int i = 0; i < TestUtils.LIST_SIZE; i++) {
			res.add(getRandomContent(hasValidPercentage));
		}

		return res;
	}
}
