package fr.cea.ig.ngl.validation;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import fr.cea.ig.ngl.dao.api.factory.ContainerFactory;
import fr.cea.ig.ngl.dao.api.factory.ContentFactory;
import fr.cea.ig.ngl.dao.api.factory.StateFactory;
import fr.cea.ig.ngl.utils.TestUtils;
import models.laboratory.common.instance.State;
import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.Content;
import ngl.refactoring.state.ContainerStateNames;
import validation.ContextValidation;
import validation.common.instance.CommonValidationHelper;
import validation.container.instance.ContainerValidationHelper;
import validation.container.instance.ContentValidationHelper;

/**
 * Test du helper de validation de l'entité CONTAINER.
 * 
 * @author jcharpen - Jordi Charpentier - jcharpen@genoscope.cns.fr
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ ContentValidationHelper.class, CommonValidationHelper.class })
public class ContainerValidationHelperTest {
    
    /**
	 * Si on veut faire des traitements avant l'exécution de la suite de tests. 
     * Par exemple : initialiser un mock, initialiser une méthode statique, ...
	 */
    @Before
	public void setUp() {
        PowerMockito.mockStatic(ContentValidationHelper.class);
        PowerMockito.mockStatic(CommonValidationHelper.class);
	}
    
    @Test
    public void testValidateContentsValid() {
        ContextValidation ctxVal = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);
        List<Content> contents = ContentFactory.getRandomContentList(true);
        String importTypeCode = "importTypeCode-TEST";
        
        ContainerValidationHelper.validateContents(ctxVal, contents, importTypeCode);

        assertTrue("Context has errors", !ctxVal.hasErrors());
    }

    @Test
    public void testValidateContentsInvalidPercentage() {
        ContextValidation ctxVal = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);
        List<Content> contents = ContentFactory.getRandomContentList(false);
        String importTypeCode = "importTypeCode-TEST";
        
        ContainerValidationHelper.validateContents(ctxVal, contents, importTypeCode);

        assertTrue("Context has no errors", ctxVal.hasErrors());
    }

    @Test
    public void testValidateContainerStateRequiredValid() {
        ContextValidation ctxVal = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);
        State state = StateFactory.getRandomState();

        ContainerValidationHelper.validateContainerStateRequired(ctxVal, state);

        assertTrue("Context has errors", !ctxVal.hasErrors());
    }

    @Test
    public void testValidateContainerStateRequiredInvalid() {
        ContextValidation ctxVal = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);
        State state = StateFactory.getRandomInvalidState();

        ContainerValidationHelper.validateContainerStateRequired(ctxVal, state);

        assertTrue("Context has no errors", ctxVal.hasErrors());
    }

    @Test
    public void testValidateNextStateValid() {
        // On valide pour le moment uniquement le passage IW_P -> A.

        ContextValidation ctxVal = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);
        String context = ContainerValidationHelper.STATE_CONTEXT_WORKFLOW;
        Container container = ContainerFactory.getRandomContainer(ContainerStateNames.IW_P);
        
        State nextState = new State();
        nextState.code = "A";

        ContainerValidationHelper.validateNextState(ctxVal, container, nextState, context);

        assertTrue("Context has errors", !ctxVal.hasErrors());
    }

    @Test
    public void testValidateNextStateInvalid() {
        // On valide pour le moment uniquement le passage IW_P -> IW_E.

        ContextValidation ctxVal = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);
        String context = ContainerValidationHelper.STATE_CONTEXT_WORKFLOW;
        Container container = ContainerFactory.getRandomContainer(ContainerStateNames.IW_P);
        
        State nextState = new State();
        nextState.code = "IW_E";

        ContainerValidationHelper.validateNextState(ctxVal, container, nextState, context);

        assertTrue("Context has no errors", ctxVal.hasErrors());
    }

    // validateContentPercentageSum testée par le test de la méthode validateContents.

    // validateState testée par le test de la méthode validateContainerStateRequired.
}