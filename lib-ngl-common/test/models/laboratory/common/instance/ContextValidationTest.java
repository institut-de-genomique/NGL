package models.laboratory.common.instance;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import fr.cea.ig.ngl.dao.api.factory.ContainerSupportFactory;
import fr.cea.ig.ngl.dao.api.factory.StateFactory;
import fr.cea.ig.ngl.utils.TestUtils;
import models.laboratory.container.instance.ContainerSupport;
import validation.ContextValidation;
import validation.container.instance.ContainerSupportValidationHelper;

public class ContextValidationTest {

	// Les méthodes sont assez simples, mais à date 90% des tests existants se basent sur le bon fonctionnement de ces méthodes. Autant les tester.

	// Par défaut un contexte ne doit pas avoir d'erreur.
	@Test
	public void testHasErrorsEmpty() {
		ContextValidation ctxValidation = ContextValidation.createCreationContext(TestUtils.CURRENT_USER);

		assertTrue("Context has error", !ctxValidation.hasErrors());
	}

	// On ajoute une erreur de validation et on s'attend à avoir une erreur dans le contexte.
	@Test
	public void testHasErrorsNotEmpty() {
		ContextValidation ctxValidation = ContextValidation.createCreationContext(TestUtils.CURRENT_USER);

		// On prend le test d'une autre méthode où on est sûr qu'on a une erreur dans le contexte.
		
       ContainerSupport support = ContainerSupportFactory.getRandomContainerSupport(false, false, StateFactory.getRandomState());
        
        ContainerSupportValidationHelper.validateCodeImportFile(ctxValidation, support.code);

		assertTrue("Context has no error", ctxValidation.hasErrors());
		assertTrue("Context has no error", ctxValidation.getErrors().size() == 1);
	}

	// On ajoute une erreur de validation et on s'attend à avoir une erreur dans le contexte.
	@Test
	public void testAddError() {
		ContextValidation ctxValidation = ContextValidation.createCreationContext(TestUtils.CURRENT_USER);
		ctxValidation.addError("testProperty", "testMessage");

		assertTrue("Context has no error", ctxValidation.hasErrors());
		assertTrue("Context has no error", ctxValidation.getErrors().size() == 1);
	}
}
