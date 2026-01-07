package fr.cea.ig.ngl.validation;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import fr.cea.ig.ngl.dao.api.factory.ContainerSupportFactory;
import fr.cea.ig.ngl.dao.api.factory.StateFactory;
import fr.cea.ig.ngl.utils.TestUtils;
import models.laboratory.common.instance.State;
import models.laboratory.container.instance.ContainerSupport;
import validation.ContextValidation;
import validation.common.instance.CommonValidationHelper;
import validation.container.instance.ContainerSupportValidationHelper;
import validation.utils.ValidationConstants;

/**
 * Test du helper de validation de l'entité CONTAINER SUPPORT.
 * 
 * @author jcharpen - Jordi Charpentier - jcharpen@genoscope.cns.fr
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ CommonValidationHelper.class })
public class ContainerSupportValidationHelperTest {
    
    /**
	 * Si on veut faire des traitements avant l'exécution de la suite de tests. 
     * Par exemple : initialiser un mock, initialiser une méthode statique, ...
	 */
    @Before
	public void setUp() {
        PowerMockito.mockStatic(CommonValidationHelper.class);
	}
    
    @Test
    public void testValidateCodeImportFileValid() {
        ContextValidation ctxVal = ContextValidation.createCreationContext(TestUtils.CURRENT_USER);
        ContainerSupport support = ContainerSupportFactory.getRandomContainerSupport(true, false, StateFactory.getRandomState());
        
        ContainerSupportValidationHelper.validateCodeImportFile(ctxVal, support.code);
        
        assertTrue("Context has errors", ctxVal.getErrors().isEmpty());
    }

    @Test
    public void testValidateCodeImportFileInvalidShort() {
        ContextValidation ctxVal = ContextValidation.createCreationContext(TestUtils.CURRENT_USER);
        ContainerSupport support = ContainerSupportFactory.getRandomContainerSupport(false, false, StateFactory.getRandomState());
        
        ContainerSupportValidationHelper.validateCodeImportFile(ctxVal, support.code);
        
        assertTrue("Context has no errors", ctxVal.getErrors().size() == 1);
    }

    @Test
    public void testValidateCodeImportFileInvalidLong() {
        ContextValidation ctxVal = ContextValidation.createCreationContext(TestUtils.CURRENT_USER);
        ContainerSupport support = ContainerSupportFactory.getRandomContainerSupport(false, true, StateFactory.getRandomState());
        
        ContainerSupportValidationHelper.validateCodeImportFile(ctxVal, support.code);
        
        assertTrue("Context has no errors", ctxVal.getErrors().size() == 1);
    }

    // Test sur les changements d'états autorisés.

    // Cas du context "workflow".

    // Etat initial : "IW-P".

    @Test
    public void testValidateStateValidPassageIWPVersAStateWorkflow() throws Exception {
        ContextValidation ctxVal = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);

        State currentState = StateFactory.getInWaitingProcessState();
        State newState = StateFactory.getAvailableState();

        ContainerSupport containerSupport = ContainerSupportFactory.getRandomContainerSupport(true, false, currentState);
        String context = "workflow";

        ContainerSupportValidationHelper.validateNextState(ctxVal, containerSupport, newState, context);

        assertTrue("Context has error", !ctxVal.hasErrors());
    }

    // Cas d'erreur avec comme état initial "IW-P".

    @Test
    public void testValidateStateInvalidPassageIWPVersUAStateWorkflow() {
        ContextValidation ctxVal = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);

        State currentState = StateFactory.getInWaitingProcessState();
        State newState = StateFactory.getUnavailableState();

        ContainerSupport containerSupport = ContainerSupportFactory.getRandomContainerSupport(true, false, currentState);
        String context = "workflow";

        ContainerSupportValidationHelper.validateNextState(ctxVal, containerSupport, newState, context);

        assertTrue("Context has no error", ctxVal.hasErrors());

        assertNotNull("Context has not the 'code' error", ctxVal.getErrors().get("code"));
        assertNotNull("Error 'code' has no message", ctxVal.getErrors().get("code").get(0));
        assertTrue("Message does not contain the right error message", ctxVal.getErrors().get("code").get(0).message().contains(ValidationConstants.ERROR_BADSTATE_MSG));
    }

    // Etat initial : "A".

    @Test
    public void testValidateStateValidPassageAVersAStateWorkflow() throws Exception {
        ContextValidation ctxVal = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);

        State currentState = StateFactory.getAvailableState();
        State newState = StateFactory.getAvailableState();

        ContainerSupport containerSupport = ContainerSupportFactory.getRandomContainerSupport(true, false, currentState);
        String context = "workflow";

        ContainerSupportValidationHelper.validateNextState(ctxVal, containerSupport, newState, context);

        assertTrue("Context has error", !ctxVal.hasErrors());
    }

    @Test
    public void testValidateStateValidPassageAVersIWDStateWorkflow() throws Exception {
        ContextValidation ctxVal = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);

        State currentState = StateFactory.getAvailableState();
        State newState = StateFactory.getInWaitingDispatchState();

        ContainerSupport containerSupport = ContainerSupportFactory.getRandomContainerSupport(true, false, currentState);
        String context = "workflow";

        ContainerSupportValidationHelper.validateNextState(ctxVal, containerSupport, newState, context);

        assertTrue("Context has error", !ctxVal.hasErrors());
    }

    @Test
    public void testValidateStateValidPassageAVersIWEStateWorkflow() throws Exception {
        ContextValidation ctxVal = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);

        State currentState = StateFactory.getAvailableState();
        State newState = StateFactory.getInWaitingExperimentState();

        ContainerSupport containerSupport = ContainerSupportFactory.getRandomContainerSupport(true, false, currentState);
        String context = "workflow";

        ContainerSupportValidationHelper.validateNextState(ctxVal, containerSupport, newState, context);

        assertTrue("Context has error", !ctxVal.hasErrors());
    }

    @Test
    public void testValidateStateValidPassageAVersIUStateWorkflow() throws Exception {
        ContextValidation ctxVal = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);

        State currentState = StateFactory.getAvailableState();
        State newState = StateFactory.getInUseState();

        ContainerSupport containerSupport = ContainerSupportFactory.getRandomContainerSupport(true, false, currentState);
        String context = "workflow";

        ContainerSupportValidationHelper.validateNextState(ctxVal, containerSupport, newState, context);

        assertTrue("Context has error", !ctxVal.hasErrors());
    }

    // Etat initial : "IW-E".

    @Test
    public void testValidateStateValidPassageIWEVersIUStateWorkflow() throws Exception {
        ContextValidation ctxVal = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);

        State currentState = StateFactory.getInWaitingExperimentState();
        State newState = StateFactory.getInUseState();

        ContainerSupport containerSupport = ContainerSupportFactory.getRandomContainerSupport(true, false, currentState);
        String context = "workflow";

        ContainerSupportValidationHelper.validateNextState(ctxVal, containerSupport, newState, context);

        assertTrue("Context has error", !ctxVal.hasErrors());
    }

    @Test
    public void testValidateStateValidPassageIWEVersIWDStateWorkflow() throws Exception {
        ContextValidation ctxVal = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);

        State currentState = StateFactory.getInWaitingExperimentState();
        State newState = StateFactory.getInWaitingDispatchState();

        ContainerSupport containerSupport = ContainerSupportFactory.getRandomContainerSupport(true, false, currentState);
        String context = "workflow";

        ContainerSupportValidationHelper.validateNextState(ctxVal, containerSupport, newState, context);

        assertTrue("Context has error", !ctxVal.hasErrors());
    }

    @Test
    public void testValidateStateValidPassageIWEVersAStateWorkflow() throws Exception {
        ContextValidation ctxVal = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);

        State currentState = StateFactory.getInWaitingExperimentState();
        State newState = StateFactory.getAvailableState();

        ContainerSupport containerSupport = ContainerSupportFactory.getRandomContainerSupport(true, false, currentState);
        String context = "workflow";

        ContainerSupportValidationHelper.validateNextState(ctxVal, containerSupport, newState, context);

        assertTrue("Context has error", !ctxVal.hasErrors());
    }

    // Etat initial : "IU".

    @Test
    public void testValidateStateValidPassageIUVersIWDStateWorkflow() throws Exception {
        ContextValidation ctxVal = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);

        State currentState = StateFactory.getInUseState();
        State newState = StateFactory.getInWaitingDispatchState();

        ContainerSupport containerSupport = ContainerSupportFactory.getRandomContainerSupport(true, false, currentState);
        String context = "workflow";

        ContainerSupportValidationHelper.validateNextState(ctxVal, containerSupport, newState, context);

        assertTrue("Context has error", !ctxVal.hasErrors());
    }

    @Test
    public void testValidateStateValidPassageIUVersAStateWorkflow() throws Exception {
        ContextValidation ctxVal = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);

        State currentState = StateFactory.getInUseState();
        State newState = StateFactory.getAvailableState();

        ContainerSupport containerSupport = ContainerSupportFactory.getRandomContainerSupport(true, false, currentState);
        String context = "workflow";

        ContainerSupportValidationHelper.validateNextState(ctxVal, containerSupport, newState, context);

        assertTrue("Context has error", !ctxVal.hasErrors());
    }

     // Cas du context "controllers".

     // Etat initial : "IW-P".

    @Test
    public void testValidateStateValidPassageIWPVersUAStateController() {
        ContextValidation ctxVal = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);

        State currentState = StateFactory.getInWaitingProcessState();
        State newState = StateFactory.getUnavailableState();

        ContainerSupport containerSupport = ContainerSupportFactory.getRandomContainerSupport(true, false, currentState);
        String context = "controllers";

        ContainerSupportValidationHelper.validateNextState(ctxVal, containerSupport, newState, context);

        assertTrue("Context has error", !ctxVal.hasErrors());
    }

    @Test
    public void testValidateStateValidPassageIWPVersISStateController() {
        ContextValidation ctxVal = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);

        State currentState = StateFactory.getInWaitingProcessState();
        State newState = StateFactory.getInStockState();

        ContainerSupport containerSupport = ContainerSupportFactory.getRandomContainerSupport(true, false, currentState);
        String context = "controllers";

        ContainerSupportValidationHelper.validateNextState(ctxVal, containerSupport, newState, context);

        assertTrue("Context has error", !ctxVal.hasErrors());
    }

    // Etat initial : "IW-D".

    @Test
    public void testValidateStateValidPassageIWDVersUAStateController() {
        ContextValidation ctxVal = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);

        State currentState = StateFactory.getInWaitingDispatchState();
        State newState = StateFactory.getUnavailableState();

        ContainerSupport containerSupport = ContainerSupportFactory.getRandomContainerSupport(true, false, currentState);
        String context = "controllers";

        ContainerSupportValidationHelper.validateNextState(ctxVal, containerSupport, newState, context);

        assertTrue("Context has error", !ctxVal.hasErrors());
    }

    @Test
    public void testValidateStateValidPassageIWDVersISStateController() {
        ContextValidation ctxVal = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);

        State currentState = StateFactory.getInWaitingDispatchState();
        State newState = StateFactory.getInStockState();

        ContainerSupport containerSupport = ContainerSupportFactory.getRandomContainerSupport(true, false, currentState);
        String context = "controllers";

        ContainerSupportValidationHelper.validateNextState(ctxVal, containerSupport, newState, context);

        assertTrue("Context has error", !ctxVal.hasErrors());
    }

    @Test
    public void testValidateStateValidPassageIWDVersAStateController() {
        ContextValidation ctxVal = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);

        State currentState = StateFactory.getInWaitingDispatchState();
        State newState = StateFactory.getAvailableState();

        ContainerSupport containerSupport = ContainerSupportFactory.getRandomContainerSupport(true, false, currentState);
        String context = "controllers";

        ContainerSupportValidationHelper.validateNextState(ctxVal, containerSupport, newState, context);

        assertTrue("Context has error", !ctxVal.hasErrors());
    }

    @Test
    public void testValidateStateValidPassageIWDVersIWPStateController() {
        ContextValidation ctxVal = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);

        State currentState = StateFactory.getInWaitingDispatchState();
        State newState = StateFactory.getInWaitingProcessState();

        ContainerSupport containerSupport = ContainerSupportFactory.getRandomContainerSupport(true, false, currentState);
        String context = "controllers";

        ContainerSupportValidationHelper.validateNextState(ctxVal, containerSupport, newState, context);

        assertTrue("Context has error", !ctxVal.hasErrors());
    }

    // Etat initial : "IS".

    @Test
    public void testValidateStateValidPassageISVersUAStateController() {
        ContextValidation ctxVal = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);

        State currentState = StateFactory.getInStockState();
        State newState = StateFactory.getUnavailableState();

        ContainerSupport containerSupport = ContainerSupportFactory.getRandomContainerSupport(true, false, currentState);
        String context = "controllers";

        ContainerSupportValidationHelper.validateNextState(ctxVal, containerSupport, newState, context);

        assertTrue("Context has error", !ctxVal.hasErrors());
    }

    @Test
    public void testValidateStateValidPassageISVersIWPStateController() {
        ContextValidation ctxVal = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);

        State currentState = StateFactory.getInStockState();
        State newState = StateFactory.getInWaitingProcessState();

        ContainerSupport containerSupport = ContainerSupportFactory.getRandomContainerSupport(true, false, currentState);
        String context = "controllers";

        ContainerSupportValidationHelper.validateNextState(ctxVal, containerSupport, newState, context);

        assertTrue("Context has error", !ctxVal.hasErrors());
    }

    // Etat initial : "UA".

    @Test
    public void testValidateStateValidPassageUAVersIWPStateController() {
        ContextValidation ctxVal = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);

        State currentState = StateFactory.getUnavailableState();
        State newState = StateFactory.getInWaitingProcessState();

        ContainerSupport containerSupport = ContainerSupportFactory.getRandomContainerSupport(true, false, currentState);
        String context = "controllers";

        ContainerSupportValidationHelper.validateNextState(ctxVal, containerSupport, newState, context);

        assertTrue("Context has error", !ctxVal.hasErrors());
    }

    @Test
    public void testValidateStateValidPassageUAVersISStateController() {
        ContextValidation ctxVal = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);

        State currentState = StateFactory.getUnavailableState();
        State newState = StateFactory.getInStockState();

        ContainerSupport containerSupport = ContainerSupportFactory.getRandomContainerSupport(true, false, currentState);
        String context = "controllers";

        ContainerSupportValidationHelper.validateNextState(ctxVal, containerSupport, newState, context);

        assertTrue("Context has error", !ctxVal.hasErrors());
    }

    // Etat initial : "N".

    @Test
    public void testValidateStateValidPassageNVersUAStateController() {
        ContextValidation ctxVal = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);

        State currentState = StateFactory.getNewState();
        State newState = StateFactory.getUnavailableState();

        ContainerSupport containerSupport = ContainerSupportFactory.getRandomContainerSupport(true, false, currentState);
        String context = "controllers";

        ContainerSupportValidationHelper.validateNextState(ctxVal, containerSupport, newState, context);

        assertTrue("Context has error", !ctxVal.hasErrors());
    }

    @Test
    public void testValidateStateValidPassageNVersISStateController() {
        ContextValidation ctxVal = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);

        State currentState = StateFactory.getNewState();
        State newState = StateFactory.getInStockState();

        ContainerSupport containerSupport = ContainerSupportFactory.getRandomContainerSupport(true, false, currentState);
        String context = "controllers";

        ContainerSupportValidationHelper.validateNextState(ctxVal, containerSupport, newState, context);

        assertTrue("Context has error", !ctxVal.hasErrors());
    }

    @Test
    public void testValidateStateValidPassageNVersAStateController() {
        ContextValidation ctxVal = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);

        State currentState = StateFactory.getNewState();
        State newState = StateFactory.getAvailableState();

        ContainerSupport containerSupport = ContainerSupportFactory.getRandomContainerSupport(true, false, currentState);
        String context = "controllers";

        ContainerSupportValidationHelper.validateNextState(ctxVal, containerSupport, newState, context);

        assertTrue("Context has error", !ctxVal.hasErrors());
    }

    @Test
    public void testValidateStateValidPassageNVersIWPStateController() {
        ContextValidation ctxVal = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);

        State currentState = StateFactory.getNewState();
        State newState = StateFactory.getInWaitingProcessState();

        ContainerSupport containerSupport = ContainerSupportFactory.getRandomContainerSupport(true, false, currentState);
        String context = "controllers";

        ContainerSupportValidationHelper.validateNextState(ctxVal, containerSupport, newState, context);

        assertTrue("Context has error", !ctxVal.hasErrors());
    }

    // Cas d'erreur

    @Test
    public void testValidateStateInValidIWEStateController() {
        ContextValidation ctxVal = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);

        State currentState = StateFactory.getInWaitingExperimentState();
        State newState = StateFactory.getUnavailableState();

        ContainerSupport containerSupport = ContainerSupportFactory.getRandomContainerSupport(true, false, currentState);
        String context = "controllers";

        ContainerSupportValidationHelper.validateNextState(ctxVal, containerSupport, newState, context);

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

        ContainerSupport containerSupport = ContainerSupportFactory.getRandomContainerSupport(true, false, currentState);
        String context = "controllers";

        ContainerSupportValidationHelper.validateNextState(ctxVal, containerSupport, newState, context);

        assertTrue("Context has no error", ctxVal.hasErrors());

        assertNotNull("Context has not the 'code' error", ctxVal.getErrors().get("code"));
        assertNotNull("Error 'code' has no message", ctxVal.getErrors().get("code").get(0));
        assertTrue("Message does not contain the right error message", ctxVal.getErrors().get("code").get(0).message().contains(ValidationConstants.ERROR_BADSTATE_MSG));
    }

}