package fr.cea.ig.ngl.dao.api.factory;

import java.util.UUID;

import models.laboratory.common.instance.State;
import models.laboratory.container.instance.ContainerSupport;

/**
 * Factory pour l'entité "ContainerSupport".
 * 
 * @author jcharpen - Jordi Charpentier - jcharpen@genoscope.cns.fr
 */
public final class ContainerSupportFactory {

	/**
	 * Méthode permettant de générer un objet "ContainerSupport" aléatoire avec un code plus gros que 30 caractères.
	 * Utilisé pour tester la validation de la taille max d'un code support.
	 * 
	 * @return Un objet "ContainerSupport" aléatoire.
	 */
	public static ContainerSupport getRandomContainerSupportWithSizeMoreThan30() {
		ContainerSupport support = getRandomContainerSupport(true, false, null);
		support.code = "ABCDEFGHABCDEFGHABCDEFGHABCDEFGH";

		return support;
	}
	
	/**
	 * Méthode permettant de générer un objet "ContainerSupport" aléatoire.
	 * 
	 * @param hasValidCode
	 * @param hasLongCode
	 * @param state
	 * 
	 * @return Un objet "ContainerSupport" aléatoire.
	 */
	public static ContainerSupport getRandomContainerSupport(boolean hasValidCode, boolean hasLongCode, State state) {
		ContainerSupport support = new ContainerSupport();
		
		if (hasValidCode) {
			support.code = "C1J3KACXX";
		} else {
			if (hasLongCode) {
				support.code = "ABCDEFGH_A11";
			} else {
				support.code = "ABCDEFGH_A1";
			}
		}

		support.categoryCode = UUID.randomUUID().toString();
		support.nbContainers = 1;
		support.nbContents = 1;

		if (state == null) {
			support.state = StateFactory.getRandomState();
		} else {
			support.state = state;
		}
		
		support.storageCode = UUID.randomUUID().toString();
		
		return support;
	}
}
