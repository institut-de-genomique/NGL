package fr.cea.ig.ngl.dao.api.factory;

import java.util.ArrayList;
import java.util.List;

import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TBoolean;
import models.laboratory.container.instance.Content;
import models.laboratory.experiment.instance.InputContainerUsed;
import ngl.refactoring.state.ContainerStateNames;

/**
 * Factory pour l'entité "InputContainerUsed".
 * 
 * @author jcharpen - Jordi Charpentier - jcharpen@genoscope.cns.fr
 */
public final class InputContainerUsedFactory {
	
	/**
	 * Méthode permettant de générer un objet "InputContainerUsed" aléatoire.
	 * 
	 * @return Un objet "InputContainerUsed" aléatoire.
	 */
	public static InputContainerUsed getRandomInputContainerUsed() {
		InputContainerUsed inputContainerUsed = new InputContainerUsed();

		inputContainerUsed.categoryCode = "test-category-code";
		inputContainerUsed.code = "FAA54955_A";
		inputContainerUsed.copyValuationToInput = TBoolean.TRUE;
		inputContainerUsed.percentage = 10.0;

		List<Content> contents = new ArrayList<>();

		Content content = new Content();
		content.sampleCode = "AAA";
		contents.add(content);
		
		inputContainerUsed.contents = contents;

		State state = new State();
		state.code = ContainerStateNames.A;

		inputContainerUsed.state = state;

		return inputContainerUsed;
	}
}
