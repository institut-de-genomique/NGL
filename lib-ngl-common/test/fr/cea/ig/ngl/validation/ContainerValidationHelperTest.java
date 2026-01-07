package fr.cea.ig.ngl.validation;

import static org.junit.Assert.*;

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
import validation.ContextValidation;
import validation.common.instance.CommonValidationHelper;
import validation.container.instance.ContainerValidationHelper;
import validation.container.instance.ContentValidationHelper;
import validation.utils.ValidationConstants;

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
    public void testValidateContentsInvalidCasPercentage() {
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
    public void testValidateContentPercentageSumValid() {
        ContextValidation ctxVal = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);
        List<Content> contents = ContentFactory.getRandomContentList(true);
        
        ContainerValidationHelper.validateContentPercentageSum(contents, ctxVal);

        assertTrue("Context has error", !ctxVal.hasErrors());
    }

    @Test
    public void testValidateContentPercentageSumInvalid() {
        ContextValidation ctxVal = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);
        List<Content> contents = ContentFactory.getRandomContentList(false);
        
        ContainerValidationHelper.validateContentPercentageSum(contents, ctxVal);

        assertTrue("Context has no errors", ctxVal.hasErrors());
    }

    // Test sur les changements d'états autorisés.

    // Cas du context "STATE_CONTEXT_WORKFLOW".

    // Etat initial : IW-P.

    @Test
    public void testValidateStateValidPassageIWPVersAStateWorkflow() {
        ContextValidation ctxVal = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);

        State currentState = StateFactory.getInWaitingProcessState();
        State newState = StateFactory.getAvailableState();

        Container container = ContainerFactory.getRandomContainer(currentState.code);
        String context = ContainerValidationHelper.STATE_CONTEXT_WORKFLOW;

        ContainerValidationHelper.validateNextState(ctxVal, container, newState, context);

        assertTrue("Context has error", !ctxVal.hasErrors());
    }

    // Etat initial : A.

    @Test
    public void testValidateStateValidPassageAVersIWEStateWorkflow() {
        ContextValidation ctxVal = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);

        State currentState = StateFactory.getAvailableState();
        State newState = StateFactory.getInWaitingExperimentState();

        Container container = ContainerFactory.getRandomContainer(currentState.code);
        String context = ContainerValidationHelper.STATE_CONTEXT_WORKFLOW;

        ContainerValidationHelper.validateNextState(ctxVal, container, newState, context);

        assertTrue("Context has error", !ctxVal.hasErrors());
    }

    @Test
    public void testValidateStateValidPassageAVersIUStateWorkflow() {
        ContextValidation ctxVal = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);

        State currentState = StateFactory.getAvailableState();
        State newState = StateFactory.getInUseState();

        Container container = ContainerFactory.getRandomContainer(currentState.code);
        String context = ContainerValidationHelper.STATE_CONTEXT_WORKFLOW;

        ContainerValidationHelper.validateNextState(ctxVal, container, newState, context);

        assertTrue("Context has error", !ctxVal.hasErrors());
    }

    @Test
    public void testValidateStateValidPassageAVersIWDStateWorkflow() {
        ContextValidation ctxVal = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);

        State currentState = StateFactory.getAvailableState();
        State newState = StateFactory.getInWaitingDispatchState();

        Container container = ContainerFactory.getRandomContainer(currentState.code);
        String context = ContainerValidationHelper.STATE_CONTEXT_WORKFLOW;

        ContainerValidationHelper.validateNextState(ctxVal, container, newState, context);

        assertTrue("Context has error", !ctxVal.hasErrors());
    }

    // Cas d'erreur avec comme état initial "A".

    @Test
    public void testValidateStateInvalidPassageAVersUAStateWorkflow() {
        ContextValidation ctxVal = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);

        State currentState = StateFactory.getAvailableState();
        State newState = StateFactory.getUnavailableState();

        Container container = ContainerFactory.getRandomContainer(currentState.code);
        String context = ContainerValidationHelper.STATE_CONTEXT_WORKFLOW;

        ContainerValidationHelper.validateNextState(ctxVal, container, newState, context);

        assertTrue("Context has no error", ctxVal.hasErrors());

        assertNotNull("Context has not the 'code' error", ctxVal.getErrors().get("code"));
        assertNotNull("Error 'code' has no message", ctxVal.getErrors().get("code").get(0));
        assertTrue("Message does not contain the right error message", ctxVal.getErrors().get("code").get(0).message().contains(ValidationConstants.ERROR_BADSTATE_MSG));
    }

    // Etat initial : IW-E.

    @Test
    public void testValidateStateValidPassageIWEVersIUStateWorkflow() {
        ContextValidation ctxVal = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);

        State currentState = StateFactory.getInWaitingExperimentState();
        State newState = StateFactory.getInUseState();

        Container container = ContainerFactory.getRandomContainer(currentState.code);
        String context = ContainerValidationHelper.STATE_CONTEXT_WORKFLOW;

        ContainerValidationHelper.validateNextState(ctxVal, container, newState, context);

        assertTrue("Context has error", !ctxVal.hasErrors());
    }

    @Test
    public void testValidateStateValidPassageIWEVersIWDStateWorkflow() {
        ContextValidation ctxVal = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);

        State currentState = StateFactory.getInWaitingExperimentState();
        State newState = StateFactory.getInWaitingDispatchState();

        Container container = ContainerFactory.getRandomContainer(currentState.code);
        String context = ContainerValidationHelper.STATE_CONTEXT_WORKFLOW;

        ContainerValidationHelper.validateNextState(ctxVal, container, newState, context);

        assertTrue("Context has error", !ctxVal.hasErrors());
    }

    @Test
    public void testValidateStateValidPassageIWEVersAStateWorkflow() {
        ContextValidation ctxVal = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);

        State currentState = StateFactory.getInWaitingExperimentState();
        State newState = StateFactory.getAvailableState();

        Container container = ContainerFactory.getRandomContainer(currentState.code);
        String context = ContainerValidationHelper.STATE_CONTEXT_WORKFLOW;

        ContainerValidationHelper.validateNextState(ctxVal, container, newState, context);

        assertTrue("Context has error", !ctxVal.hasErrors());
    }

    // Cas d'erreur avec comme état initial "IW-E".

    @Test
    public void testValidateStateInvalidPassageIWEVersUAStateWorkflow() {
        ContextValidation ctxVal = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);

        State currentState = StateFactory.getInWaitingExperimentState();
        State newState = StateFactory.getUnavailableState();

        Container container = ContainerFactory.getRandomContainer(currentState.code);
        String context = ContainerValidationHelper.STATE_CONTEXT_WORKFLOW;

        ContainerValidationHelper.validateNextState(ctxVal, container, newState, context);

        assertTrue("Context has no error", ctxVal.hasErrors());

        assertNotNull("Context has not the 'code' error", ctxVal.getErrors().get("code"));
        assertNotNull("Error 'code' has no message", ctxVal.getErrors().get("code").get(0));
        assertTrue("Message does not contain the right error message", ctxVal.getErrors().get("code").get(0).message().contains(ValidationConstants.ERROR_BADSTATE_MSG));
    }

    @Test
    public void testValidateStateInvalidPassageIWEVersIWPStateWorkflow() {
        ContextValidation ctxVal = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);

        State currentState = StateFactory.getInWaitingExperimentState();
        State newState = StateFactory.getInWaitingProcessState();

        Container container = ContainerFactory.getRandomContainer(currentState.code);
        String context = ContainerValidationHelper.STATE_CONTEXT_WORKFLOW;

        ContainerValidationHelper.validateNextState(ctxVal, container, newState, context);

        assertTrue("Context has no error", ctxVal.hasErrors());

        assertNotNull("Context has not the 'code' error", ctxVal.getErrors().get("code"));
        assertNotNull("Error 'code' has no message", ctxVal.getErrors().get("code").get(0));
        assertTrue("Message does not contain the right error message", ctxVal.getErrors().get("code").get(0).message().contains(ValidationConstants.ERROR_BADSTATE_MSG));
    }

    // Etat initial : IU.

    @Test
    public void testValidateStateValidPassageIUVersIWDStateWorkflow() {
        ContextValidation ctxVal = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);

        State currentState = StateFactory.getInUseState();
        State newState = StateFactory.getInWaitingDispatchState();

        Container container = ContainerFactory.getRandomContainer(currentState.code);
        String context = ContainerValidationHelper.STATE_CONTEXT_WORKFLOW;

        ContainerValidationHelper.validateNextState(ctxVal, container, newState, context);

        assertTrue("Context has error", !ctxVal.hasErrors());
    }

    // Cas d'erreur avec comme état initial "IU".

    @Test
    public void testValidateStateInvalidPassageIUVersUAStateWorkflow() {
        ContextValidation ctxVal = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);

        State currentState = StateFactory.getInUseState();
        State newState = StateFactory.getUnavailableState();

        Container container = ContainerFactory.getRandomContainer(currentState.code);
        String context = ContainerValidationHelper.STATE_CONTEXT_WORKFLOW;

        ContainerValidationHelper.validateNextState(ctxVal, container, newState, context);

        assertTrue("Context has no error", ctxVal.hasErrors());

        assertNotNull("Context has not the 'code' error", ctxVal.getErrors().get("code"));
        assertNotNull("Error 'code' has no message", ctxVal.getErrors().get("code").get(0));
        assertTrue("Message does not contain the right error message", ctxVal.getErrors().get("code").get(0).message().contains(ValidationConstants.ERROR_BADSTATE_MSG));
    }

    // Cas du context "STATE_CONTEXT_CONTROLLER".

    // Etat initial : "IW-P".

    @Test
    public void testValidateStateValidPassageIWPVersUAStateController() {
        ContextValidation ctxVal = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);

        State currentState = StateFactory.getInWaitingProcessState();
        State newState = StateFactory.getUnavailableState();

        Container container = ContainerFactory.getRandomContainer(currentState.code);
        String context = ContainerValidationHelper.STATE_CONTEXT_CONTROLLER;

        ContainerValidationHelper.validateNextState(ctxVal, container, newState, context);

        assertTrue("Context has error", !ctxVal.hasErrors());
    }

    @Test
    public void testValidateStateValidPassageIWPVersISStateController() {
        ContextValidation ctxVal = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);

        State currentState = StateFactory.getInWaitingProcessState();
        State newState = StateFactory.getInStockState();

        Container container = ContainerFactory.getRandomContainer(currentState.code);
        String context = ContainerValidationHelper.STATE_CONTEXT_CONTROLLER;

        ContainerValidationHelper.validateNextState(ctxVal, container, newState, context);

        assertTrue("Context has error", !ctxVal.hasErrors());
    }

    // Etat initial : "IW-D".

    @Test
    public void testValidateStateValidPassageIWDVersUAStateController() {
        ContextValidation ctxVal = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);

        State currentState = StateFactory.getInWaitingDispatchState();
        State newState = StateFactory.getUnavailableState();

        Container container = ContainerFactory.getRandomContainer(currentState.code);
        String context = ContainerValidationHelper.STATE_CONTEXT_CONTROLLER;

        ContainerValidationHelper.validateNextState(ctxVal, container, newState, context);

        assertTrue("Context has error", !ctxVal.hasErrors());
    }

    @Test
    public void testValidateStateValidPassageIWDVersISStateController() {
        ContextValidation ctxVal = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);

        State currentState = StateFactory.getInWaitingDispatchState();
        State newState = StateFactory.getInStockState();

        Container container = ContainerFactory.getRandomContainer(currentState.code);
        String context = ContainerValidationHelper.STATE_CONTEXT_CONTROLLER;

        ContainerValidationHelper.validateNextState(ctxVal, container, newState, context);

        assertTrue("Context has error", !ctxVal.hasErrors());
    }

    @Test
    public void testValidateStateValidPassageIWDVersAStateController() {
        ContextValidation ctxVal = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);

        State currentState = StateFactory.getInWaitingDispatchState();
        State newState = StateFactory.getAvailableState();

        Container container = ContainerFactory.getRandomContainer(currentState.code);
        String context = ContainerValidationHelper.STATE_CONTEXT_CONTROLLER;

        ContainerValidationHelper.validateNextState(ctxVal, container, newState, context);

        assertTrue("Context has error", !ctxVal.hasErrors());
    }

    @Test
    public void testValidateStateValidPassageIWDVersIWPStateController() {
        ContextValidation ctxVal = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);

        State currentState = StateFactory.getInWaitingDispatchState();
        State newState = StateFactory.getInWaitingProcessState();

        Container container = ContainerFactory.getRandomContainer(currentState.code);
        String context = ContainerValidationHelper.STATE_CONTEXT_CONTROLLER;

        ContainerValidationHelper.validateNextState(ctxVal, container, newState, context);

        assertTrue("Context has error", !ctxVal.hasErrors());
    }

    // Etat initial : "IS".

    @Test
    public void testValidateStateValidPassageISVersUAStateController() {
        ContextValidation ctxVal = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);

        State currentState = StateFactory.getInStockState();
        State newState = StateFactory.getUnavailableState();

        Container container = ContainerFactory.getRandomContainer(currentState.code);
        String context = ContainerValidationHelper.STATE_CONTEXT_CONTROLLER;

        ContainerValidationHelper.validateNextState(ctxVal, container, newState, context);

        assertTrue("Context has error", !ctxVal.hasErrors());
    }

    @Test
    public void testValidateStateValidPassageISVersIWPStateController() {
        ContextValidation ctxVal = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);

        State currentState = StateFactory.getInStockState();
        State newState = StateFactory.getInWaitingProcessState();

        Container container = ContainerFactory.getRandomContainer(currentState.code);
        String context = ContainerValidationHelper.STATE_CONTEXT_CONTROLLER;

        ContainerValidationHelper.validateNextState(ctxVal, container, newState, context);

        assertTrue("Context has error", !ctxVal.hasErrors());
    }

    // Etat initial : "UA".

    @Test
    public void testValidateStateValidPassageUAVersIWPStateController() {
        ContextValidation ctxVal = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);

        State currentState = StateFactory.getUnavailableState();
        State newState = StateFactory.getInWaitingProcessState();

        Container container = ContainerFactory.getRandomContainer(currentState.code);
        String context = ContainerValidationHelper.STATE_CONTEXT_CONTROLLER;

        ContainerValidationHelper.validateNextState(ctxVal, container, newState, context);

        assertTrue("Context has error", !ctxVal.hasErrors());
    }

    @Test
    public void testValidateStateValidPassageUAVersISStateController() {
        ContextValidation ctxVal = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);

        State currentState = StateFactory.getUnavailableState();
        State newState = StateFactory.getInStockState();

        Container container = ContainerFactory.getRandomContainer(currentState.code);
        String context = ContainerValidationHelper.STATE_CONTEXT_CONTROLLER;

        ContainerValidationHelper.validateNextState(ctxVal, container, newState, context);

        assertTrue("Context has error", !ctxVal.hasErrors());
    }

    // Etat initial : "N".

    @Test
    public void testValidateStateValidPassageNVersUAStateController() {
        ContextValidation ctxVal = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);

        State currentState = StateFactory.getNewState();
        State newState = StateFactory.getUnavailableState();

        Container container = ContainerFactory.getRandomContainer(currentState.code);
        String context = ContainerValidationHelper.STATE_CONTEXT_CONTROLLER;

        ContainerValidationHelper.validateNextState(ctxVal, container, newState, context);

        assertTrue("Context has error", !ctxVal.hasErrors());
    }

    @Test
    public void testValidateStateValidPassageNVersISStateController() {
        ContextValidation ctxVal = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);

        State currentState = StateFactory.getNewState();
        State newState = StateFactory.getInStockState();

        Container container = ContainerFactory.getRandomContainer(currentState.code);
        String context = ContainerValidationHelper.STATE_CONTEXT_CONTROLLER;

        ContainerValidationHelper.validateNextState(ctxVal, container, newState, context);

        assertTrue("Context has error", !ctxVal.hasErrors());
    }

    @Test
    public void testValidateStateValidPassageNVersAStateController() {
        ContextValidation ctxVal = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);

        State currentState = StateFactory.getNewState();
        State newState = StateFactory.getAvailableState();

        Container container = ContainerFactory.getRandomContainer(currentState.code);
        String context = ContainerValidationHelper.STATE_CONTEXT_CONTROLLER;

        ContainerValidationHelper.validateNextState(ctxVal, container, newState, context);

        assertTrue("Context has error", !ctxVal.hasErrors());
    }

    @Test
    public void testValidateStateValidPassageNVersIWPStateController() {
        ContextValidation ctxVal = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);

        State currentState = StateFactory.getNewState();
        State newState = StateFactory.getInWaitingProcessState();

        Container container = ContainerFactory.getRandomContainer(currentState.code);
        String context = ContainerValidationHelper.STATE_CONTEXT_CONTROLLER;

        ContainerValidationHelper.validateNextState(ctxVal, container, newState, context);

        assertTrue("Context has error", !ctxVal.hasErrors());
    }

    // Cas d'erreur

    @Test
    public void testValidateStateInValidIWEStateController() {
        ContextValidation ctxVal = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);

        State currentState = StateFactory.getInWaitingExperimentState();
        State newState = StateFactory.getUnavailableState();

        Container container = ContainerFactory.getRandomContainer(currentState.code);
        String context = ContainerValidationHelper.STATE_CONTEXT_CONTROLLER;

        ContainerValidationHelper.validateNextState(ctxVal, container, newState, context);

        assertTrue("Context has no error", ctxVal.hasErrors());

        assertNotNull("Context has not the 'code' error", ctxVal.getErrors().get("code"));
        assertNotNull("Error 'code' has no message", ctxVal.getErrors().get("code").get(0));
        assertTrue("Message does not contain the right error message", ctxVal.getErrors().get("code").get(0).message().contains(ValidationConstants.ERROR_BADSTATE_MSG));
    }

    @Test
    public void testValidateStateInValidIUStateController() {
        ContextValidation ctxVal = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);

        State currentState = StateFactory.getInUseState();
        State newState = StateFactory.getUnavailableState();

        Container container = ContainerFactory.getRandomContainer(currentState.code);
        String context = ContainerValidationHelper.STATE_CONTEXT_CONTROLLER;

        ContainerValidationHelper.validateNextState(ctxVal, container, newState, context);

        assertTrue("Context has no error", ctxVal.hasErrors());

        assertNotNull("Context has not the 'code' error", ctxVal.getErrors().get("code"));
        assertNotNull("Error 'code' has no message", ctxVal.getErrors().get("code").get(0));
        assertTrue("Message does not contain the right error message", ctxVal.getErrors().get("code").get(0).message().contains(ValidationConstants.ERROR_BADSTATE_MSG));
    }
}