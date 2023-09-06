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
import validation.container.instance.ContainerSupportValidationHelper;

/**
 * Test du helper de validation de l'entité CONTAINER SUPPORT.
 * 
 * @author jcharpen - Jordi Charpentier - jcharpen@genoscope.cns.fr
 */
@RunWith(PowerMockRunner.class)
public class ContainerSupportValidationHelperTest {
    
    /**
	 * Si on veut faire des traitements avant l'exécution de la suite de tests. 
     * Par exemple : initialiser un mock, initialiser une méthode statique, ...
	 */
    @Before
	public void setUp() {

	}
    
    @Test
    public void testValidateCodeImportFileValid() {
        ContextValidation ctxVal = ContextValidation.createCreationContext(TestUtils.CURRENT_USER);
        ContainerSupport support = ContainerSupportFactory.getRandomContainerSupport(true, false);
        
        ContainerSupportValidationHelper.validateCodeImportFile(ctxVal, support.code);
        
        assertTrue("Context has errors", ctxVal.getErrors().isEmpty());
    }

    @Test
    public void testValidateCodeImportFileInvalidShort() {
        ContextValidation ctxVal = ContextValidation.createCreationContext(TestUtils.CURRENT_USER);
        ContainerSupport support = ContainerSupportFactory.getRandomContainerSupport(false, false);
        
        ContainerSupportValidationHelper.validateCodeImportFile(ctxVal, support.code);
        
        assertTrue("Context has no errors", ctxVal.getErrors().size() == 1);
    }

    @Test
    public void testValidateCodeImportFileInvalidLong() {
        ContextValidation ctxVal = ContextValidation.createCreationContext(TestUtils.CURRENT_USER);
        ContainerSupport support = ContainerSupportFactory.getRandomContainerSupport(false, true);
        
        ContainerSupportValidationHelper.validateCodeImportFile(ctxVal, support.code);
        
        assertTrue("Context has no errors", ctxVal.getErrors().size() == 1);
    }
}