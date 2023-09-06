package fr.cea.ig.ngl.dao.api.factory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import models.laboratory.common.instance.State;
import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.Content;
import models.laboratory.container.instance.LocationOnContainerSupport;

/**
 * Factory pour l'entité "Container".
 * 
 * @author jcharpen - Jordi Charpentier - jcharpen@genoscope.cns.fr
 */
public final class ContainerFactory {
	
	/**
	 * Méthode permettant de générer un code container aléatoire.
	 * 
	 * @return Un code container aléatoire.
	 */
	public static String getRandomContainerCode() {
		return UUID.randomUUID().toString();
	}
	
	/**
	 * Méthode permettant de générer un objet "Container" aléatoire.
	 * 
	 * @param stateName L'état à mettre pour le container généré.
	 * 
	 * @return Un objet "Container" aléatoire.
	 */
	public static Container getRandomContainer(String stateName) {
		Container container = new Container();
		container.code = "FAA54955_A";
		container.categoryCode = "test-category-code";

		State state = new State();
		state.code = stateName;

		container.state = state;
		
		LocationOnContainerSupport support = new LocationOnContainerSupport();
		support.code = "FAA54955_A";
		support.line = "1";

		container.support = support;
		
		List<Content> contents = new ArrayList<>();

		Content content = new Content();
		content.sampleCode = "AAA";
		contents.add(content);

		container.contents = contents;
		
		return container;
	}
}
