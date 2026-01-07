package fr.cea.ig.ngl.validation;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.ngl.dao.api.factory.ReadsetFactory;
import fr.cea.ig.ngl.utils.TestUtils;
import models.laboratory.run.instance.ReadSet;
import validation.ContextValidation;
import validation.run.instance.LaneValidationHelper;
import validation.utils.ValidationConstants;

/**
 * Test du helper de validation de l'entité LANE.
 * 
 * @author jcharpen - Jordi Charpentier - jcharpen@genoscope.cns.fr
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ MongoDBDAO.class })
public class LaneValidationHelperTest {
    
    /**
	 * Si on veut faire des traitements avant l'exécution de la suite de tests. 
     * Par exemple : initialiser un mock, initialiser une méthode statique, ...
	 */
    @Before
	public void setUp() {
        PowerMockito.mockStatic(MongoDBDAO.class);
	}
    
    @Test
    @SuppressWarnings("unchecked")
    public void testValidateLaneReadSetCodesValid() {
        ContextValidation ctxVal = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);
        List<String> readSetCodes = ReadsetFactory.getRandomReadsetCodesList();
        ReadSet readset = ReadsetFactory.getRandomReadset(true, new Date(), 1);

        when(MongoDBDAO.findByCode(any(String.class), any(Class.class), any(String.class))).thenReturn(readset);

        LaneValidationHelper.validateLaneReadSetCodes(ctxVal, 1, readSetCodes);

        assertTrue("Context has error", !ctxVal.hasErrors());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testValidateLaneReadSetCodesInvalid() {
        ContextValidation ctxVal = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);
        List<String> readSetCodes = ReadsetFactory.getRandomReadsetCodesList();
        ReadSet readset = ReadsetFactory.getRandomReadset(true, new Date(), 1);

        when(MongoDBDAO.findByCode(any(String.class), any(Class.class), any(String.class))).thenReturn(readset);

        LaneValidationHelper.validateLaneReadSetCodes(ctxVal, 2, readSetCodes);

        assertTrue("Context has no error", ctxVal.hasErrors());
        assertNotNull("Error 'code' has no message", ctxVal.getErrors().get("readSetCodes[0]").get(0));
        assertTrue("Message does not contain the right error message", ctxVal.getErrors().get("readSetCodes[0]").get(0).message().contains(ValidationConstants.ERROR_CODE_NOTEXISTS_MSG));
        
    }
}