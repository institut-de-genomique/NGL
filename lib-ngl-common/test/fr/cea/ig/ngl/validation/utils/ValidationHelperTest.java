package fr.cea.ig.ngl.validation.utils;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

import fr.cea.ig.ngl.dao.api.factory.PropertyDefinitionFactory;
import fr.cea.ig.ngl.utils.TestUtils;
import validation.ContextValidation;
import validation.utils.ValidationHelper;

/**
 * Test du helper de validation.
 * 
 * @author jcharpen - Jordi Charpentier - jcharpen@genoscope.cns.fr
 */
@RunWith(PowerMockRunner.class)
public class ValidationHelperTest {

	@Test
	public void testCheckIfActiveValid() {
		ContextValidation ctxVal = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);

		ValidationHelper.checkIfActive(ctxVal, PropertyDefinitionFactory.getRandomPropertyDefinition(Boolean.TRUE));

		assertTrue("Context has errors", !ctxVal.hasErrors());
	}

	@Test
	public void testCheckIfActiveInvalid() {
		ContextValidation ctxVal = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);

		ValidationHelper.checkIfActive(ctxVal, PropertyDefinitionFactory.getRandomPropertyDefinition(Boolean.FALSE));

		assertTrue("Context has no error", ctxVal.hasErrors());
	}
}