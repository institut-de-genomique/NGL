package fr.cea.ig.ngl.validation.experiment;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

import fr.cea.ig.ngl.dao.api.factory.ContainerFactory;
import fr.cea.ig.ngl.dao.api.factory.InputContainerUsedFactory;
import fr.cea.ig.ngl.utils.TestUtils;
import models.laboratory.container.instance.Container;
import models.laboratory.experiment.instance.InputContainerUsed;
import ngl.refactoring.state.ContainerStateNames;
import validation.ContextValidation;
import validation.experiment.instance.ContainerUsedValidationHelper;

/**
 * Test du helper de validation de l'entité CONTAINERUSED.
 * 
 * @author jcharpen - Jordi Charpentier - jcharpen@genoscope.cns.fr
 */
@RunWith(PowerMockRunner.class)
public class ContainerUsedValidationHelperTest {
    
    @Test
    public void testValidateInputContainerMatchesContainerValid() {
        ContextValidation ctxVal = ContextValidation.createCreationContext(TestUtils.CURRENT_USER);
        InputContainerUsed inputContainer = InputContainerUsedFactory.getRandomInputContainerUsed();
        Container container = ContainerFactory.getRandomContainer(ContainerStateNames.A);

        ContainerUsedValidationHelper.validateInputContainerMatchesContainer(ctxVal, inputContainer, container);

        assertTrue("Context has errors", !ctxVal.hasErrors());
    }

    @Test
    public void testValidateInputContainerMatchesContainerInvalid() {
        ContextValidation ctxVal = ContextValidation.createCreationContext(TestUtils.CURRENT_USER);
        InputContainerUsed inputContainer = InputContainerUsedFactory.getRandomInputContainerUsed();
        Container container = ContainerFactory.getRandomContainer(ContainerStateNames.IW_P);

        ContainerUsedValidationHelper.validateInputContainerMatchesContainer(ctxVal, inputContainer, container);

        assertTrue("Context has no error", ctxVal.hasErrors());
    }
}