package fr.cea.ig.ngl.dao.api.factory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import fr.cea.ig.ngl.utils.TestUtils;
import models.laboratory.resolutions.instance.Resolution;
import models.laboratory.resolutions.instance.ResolutionCategory;

/**
 * Factory pour l'entité "Resolution".
 * 
 * @author jcharpen - Jordi Charpentier - jcharpen@genoscope.cns.fr
 */
public final class ResolutionFactory {

	/**
	 * Méthode permettant de générer une liste d'objets "Resolution" aléatoires.
	 * 
	 * @param hasFixedId Booléen permettant de savoir si les résolutions générées
	 * ont un id fixé ou pas (utile dans certains tests).
	 * 
	 * @return Une liste d'objets "Resolution" générée aléatoirement.
	 */
	public static List<Resolution> getRandomResolutionsList(boolean hasFixedId) {
		List<Resolution> res = new ArrayList<>();

		for (int i = 0; i < TestUtils.LIST_SIZE; i++) {
			short displayOrder = (short) (i + 1);
			res.add(getRandomResolution(displayOrder, hasFixedId));
		}

		return res;
	}
	
	/**
	 * Méthode permettant de générer un objet "Resolution" aléatoire.
	 * 
	 * @param displayOrder La valeur du champ displayOrder à fixer pour la résolution.
	 * @param hasFixedId Booléen permettant de savoir si les résolutions générées
	 * ont un id fixé ou pas (utile dans certains tests).
	 * 
	 * @return Un objet "Resolution" aléatoire.
	 */
	public static Resolution getRandomResolution(short displayOrder, boolean hasFixedId) {
		Resolution resolution = new Resolution();

		ResolutionCategory category = new ResolutionCategory();
		category.name = UUID.randomUUID().toString();
		category.displayOrder = displayOrder;

		resolution.category = category;

		if (hasFixedId) {
			resolution.code = "2609aa0b-d4fc-4071-a515-009ecc3fe17d";
		} else {
			resolution.code = UUID.randomUUID().toString();
		}

		resolution.name = UUID.randomUUID().toString();
		resolution.level = UUID.randomUUID().toString();
		resolution.displayOrder = displayOrder;

		return resolution;
	}
}
