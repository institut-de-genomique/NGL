package fr.cea.ig.ngl.validation;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

import fr.cea.ig.ngl.dao.api.factory.ContainerSupportFactory;
import fr.cea.ig.ngl.utils.TestUtils;
import models.laboratory.container.instance.ContainerSupport;
import validation.ContextValidation;
import validation.common.instance.CommonValidationHelper;

/**
 * Test du helper de validation commun.
 * 
 * @author jcharpen - Jordi Charpentier - jcharpen@genoscope.cns.fr
 */
@RunWith(PowerMockRunner.class)
public class CommonValidationHelperTest {
    
    /**
	 * Si on veut faire des traitements avant l'exécution de la suite de tests. 
     * Par exemple : initialiser un mock, initialiser une méthode statique, ...
	 */
    @Before
	public void setUp() {

	}
    
    @Test
    public void testValidateCodeSizeValid() {
        ContextValidation contextValidation = ContextValidation.createCreationContext(TestUtils.CURRENT_USER);
        ContainerSupport containerSupport = ContainerSupportFactory.getRandomContainerSupport(true, false);

        CommonValidationHelper.validateFieldMaxSize(contextValidation, "code", containerSupport.code, 30);

        assertTrue("Context has errors", !contextValidation.hasErrors());
    }

    @Test
    public void testValidateCodeSizeInvalid() {
        ContextValidation contextValidation = ContextValidation.createCreationContext(TestUtils.CURRENT_USER);
        ContainerSupport containerSupport = ContainerSupportFactory.getRandomContainerSupportWithSizeMoreThan30();

        CommonValidationHelper.validateFieldMaxSize(contextValidation, "code", containerSupport.code, 30);

        assertTrue("Context has no errors", contextValidation.hasErrors());
    }
}