package fr.cea.ig.ngl.validation.utils;

import static org.junit.Assert.*;

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

	@Test
	public void testDynamicCastSame() {
		String value = "test";

		Object result = ValidationHelper.dynamicCast("java.lang.String", value);

		assertEquals("Result is not equals to initial value", value, result);
	}

	@Test
	public void testDynamicCastNull() {
		String value = null;

		Object result = ValidationHelper.dynamicCast("java.lang.String", value);

		assertNull("Result is not null", result);
	}

	@Test
	public void testIsEmptyStringValid() {
		String value = "test";

		boolean result = ValidationHelper.isEmpty(value);

		assertFalse("Result is true", result);
	}

	@Test
	public void testIsEmptyStringInvalid() {
		String value = "";

		boolean result = ValidationHelper.isEmpty(value);

		assertTrue("Result is false", result);
	}

	@Test
	public void testCleanValueNotNull() {
		String testValue = "test";

		Object result = ValidationHelper.cleanValue(testValue);

		assertNotNull("Result is null", result);
	}

	@Test
	public void testCleanValueNull() {
		String testValue = null;

		Object result = ValidationHelper.cleanValue(testValue);

		assertNull("Result is not null", result);
	}

	@Test
	public void testValidateNotEmptyStringValid() {
		ContextValidation ctxVal = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);
		String errorMsg = "errorMsg";
		String objVal = "test";

		ValidationHelper.validateNotEmpty(ctxVal, objVal, errorMsg);

		assertTrue("Context has errors", !ctxVal.hasErrors());
	}

	@Test
	public void testValidateNotEmptyStringInvalid() {
		ContextValidation ctxVal = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);
		String errorMsg = "errorMsg";
		String objVal = "";

		ValidationHelper.validateNotEmpty(ctxVal, objVal, errorMsg);

		assertTrue("Context has no error", ctxVal.hasErrors());
	}
}