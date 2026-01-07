package fr.cea.ig.ngl.validation;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mongojack.DBQuery.Query;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import fr.cea.ig.DBObject;
import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.MongoDBResult;
import fr.cea.ig.ngl.dao.api.factory.ContainerFactory;
import fr.cea.ig.ngl.dao.api.factory.ProcessFactory;
import fr.cea.ig.ngl.dao.api.factory.StateFactory;
import fr.cea.ig.ngl.utils.TestUtils;
import models.laboratory.common.instance.State;
import models.laboratory.container.instance.Container;
import models.laboratory.processes.instance.Process;
import ngl.refactoring.state.ContainerStateNames;
import validation.ContextValidation;
import validation.common.instance.CommonValidationHelper;
import validation.processes.instance.ProcessValidationHelper;
import validation.utils.ValidationConstants;

/**
 * Test du helper de validation de l'entité PROCESS.
 * 
 * @author jcharpen - Jordi Charpentier - jcharpen@genoscope.cns.fr
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ CommonValidationHelper.class, MongoDBDAO.class })
public class ProcessValidationHelperTest {
    
    /**
	 * Si on veut faire des traitements avant l'exécution de la suite de tests. 
     * Par exemple : initialiser un mock, initialiser une méthode statique, ...
	 */
    @Before
	public void setUp() {
        PowerMockito.mockStatic(CommonValidationHelper.class);
        PowerMockito.mockStatic(MongoDBDAO.class);
	}
    
    // Test sur les changements d'états autorisés.

    // Etat initial : "IW-C".

    @Test
    public void testValidateStateValidPassageIWCVersNState() {
        ContextValidation ctxVal = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);

        State currentState = StateFactory.getInWaitingContainerState();
        State newState = StateFactory.getNewState();

        Process process = ProcessFactory.getRandomProcess(currentState, false);

        ProcessValidationHelper.validateNextState(process, newState, ctxVal);

        assertTrue("Context has error", !ctxVal.hasErrors());
    }

    // Etat initial : "N".

    @Test
    @SuppressWarnings("unchecked")
    public void testValidateStateValidPassageNVersIWCState() {
        ContextValidation ctxVal = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);

        State currentState = StateFactory.getNewState();
        State newState = StateFactory.getInWaitingContainerState();

        Process process = ProcessFactory.getRandomProcess(currentState, false);
        Container container = ContainerFactory.getRandomContainer(ContainerStateNames.UA);

        when(MongoDBDAO.findOne(any(String.class), any(Class.class), any(Query.class))).thenReturn(container);

        ProcessValidationHelper.validateNextState(process, newState, ctxVal);

        assertTrue("Context has error", !ctxVal.hasErrors());
    }

    // Etat initial : "IP".

    @Test
    @SuppressWarnings("unchecked")
    public void testValidateStateValidPassageIPVersFState() {
        ContextValidation ctxVal = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);

        State currentState = StateFactory.getInProgressState();
        State newState = StateFactory.getFinishState();

        Process process = ProcessFactory.getRandomProcess(currentState, true);
        Container container = ContainerFactory.getRandomContainer(ContainerStateNames.UA);

        when(MongoDBDAO.findOne(any(String.class), any(Class.class), any(Query.class))).thenReturn(container);

        ProcessValidationHelper.validateNextState(process, newState, ctxVal);

        assertTrue("Context has error", !ctxVal.hasErrors());
    }

    // Etat initial : "F".

    @Test
    @SuppressWarnings("unchecked")
    public void testValidateStateValidPassageFVersFStateValidOCUCode() {
        ContextValidation ctxVal = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);

        State currentState = StateFactory.getFinishState();
        State newState = StateFactory.getFinishState();

        Process process = ProcessFactory.getRandomProcess(currentState, true, true);
        Container container = ContainerFactory.getRandomContainer(ContainerStateNames.UA);

        when(MongoDBDAO.findOne(any(String.class), any(Class.class), any(Query.class))).thenReturn(container);

        ProcessValidationHelper.validateNextState(process, newState, ctxVal);

        assertTrue("Context has error", !ctxVal.hasErrors());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testValidateStateValidPassageFVersFStateValidICUCode() {
        ContextValidation ctxVal = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);

        State currentState = StateFactory.getFinishState();
        State newState = StateFactory.getFinishState();

        Process process = ProcessFactory.getRandomProcess(currentState, false, true);
        Container container = ContainerFactory.getRandomContainer(ContainerStateNames.UA);

        List<DBObject> containerList = new ArrayList<DBObject>();
        containerList.add(container);

        MongoDBResult<DBObject> result = mock(MongoDBResult.class);
        
        when(MongoDBDAO.find(any(String.class), any(Class.class), any(Query.class))).thenReturn(result);
        when(result.sort(any(String.class))).thenReturn(result);
        when(result.limit(any(Integer.class))).thenReturn(result);

        when(result.toList()).thenReturn(containerList);

        ProcessValidationHelper.validateNextState(process, newState, ctxVal);

        assertTrue("Context has error", !ctxVal.hasErrors());
    }

    // Cas d'erreur.

    @Test
    public void testValidateStateInvalidPassageIPVersNState() {
        ContextValidation ctxVal = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);

        State currentState = StateFactory.getInProgressState();
        State newState = StateFactory.getNewState();

        Process process = ProcessFactory.getRandomProcess(currentState, true);

        ProcessValidationHelper.validateNextState(process, newState, ctxVal);

        assertTrue("Context has no error", ctxVal.hasErrors());

        assertNotNull("Context has not the 'code' error", ctxVal.getErrors().get("state.code"));
        assertNotNull("Error 'code' has no message", ctxVal.getErrors().get("state.code").get(0));
        assertTrue("Message does not contain the right error message", ctxVal.getErrors().get("state.code").get(0).message().contains(ValidationConstants.ERROR_BADSTATE_MSG));
    }

    @Test
    public void testValidateStateInvalidPassageIWCVersNState() {
        ContextValidation ctxVal = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);

        State currentState = StateFactory.getInWaitingContainerState();
        State newState = StateFactory.getNewState();

        Process process = ProcessFactory.getRandomProcess(currentState, true);

        ProcessValidationHelper.validateNextState(process, newState, ctxVal);

        assertTrue("Context has no error", ctxVal.hasErrors());

        assertNotNull("Context has not the 'code' error", ctxVal.getErrors().get("state.code"));
        assertNotNull("Error 'code' has no message", ctxVal.getErrors().get("state.code").get(0));
        assertTrue("Message does not contain the right error message", ctxVal.getErrors().get("state.code").get(0).message().contains(ValidationConstants.ERROR_BADSTATE_MSG));
    }
}