package fr.cea.ig.ngl.validation.experiment;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mongojack.DBQuery;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.ngl.utils.TestUtils;
import models.laboratory.common.instance.TBoolean;
import models.laboratory.common.instance.Valuation;
import validation.ContextValidation;
import validation.experiment.instance.ExperimentValidationHelper;

import static org.mockito.Mockito.*;

import static org.junit.Assert.*;

/**
 * Test du helper de validation de l'entité EXPERIMENT.
 * 
 * @author jcharpen - Jordi Charpentier - jcharpen@genoscope.cns.fr
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ MongoDBDAO.class })
public class ExperimentValidationHelperTest {
	
	/**
	 * Si on veut faire des traitements avant l'exécution de la suite de tests. 
	 * Par exemple : initialiser un mock, initialiser une méthode statique, ...
	 */
	@Before
	public void setUp() {
		PowerMockito.mockStatic(MongoDBDAO.class);
	}
	
	@Test
	public void testValidateStatusRequiredValid() {			
		ContextValidation ctxVal = ContextValidation.createCreationContext(TestUtils.CURRENT_USER);
		
		Valuation status = new Valuation();
		status.valid = TBoolean.TRUE;
		
		ctxVal.putObject("stateCode" , "F");
		
		ExperimentValidationHelper.validateStatusRequired(ctxVal, "EXEMPLE", status);
		
		assertTrue("Context has no errors", ctxVal.hasErrors());
	}
	
	@Test
	public void testValidateStatusRequiredInvalid() {	
		ContextValidation ctxVal = ContextValidation.createCreationContext(TestUtils.CURRENT_USER);
		
		Valuation status = new Valuation();
		status.valid = TBoolean.UNSET;
		
		ctxVal.putObject("stateCode" , "F");
		
		ExperimentValidationHelper.validateStatusRequired(ctxVal, "EXEMPLE", status);
		
		assertTrue("Context has no errors", ctxVal.hasErrors());
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void testValidateProtocolCodeStateNoNew() throws IllegalArgumentException, IllegalAccessException {		
		ContextValidation ctxVal = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);
		
		when(MongoDBDAO.checkObjectExist(any(String.class), any(Class.class), any(DBQuery.Query.class))).thenReturn(Boolean.TRUE);
		
		ExperimentValidationHelper.validateProtocolCode(ctxVal, "illumina-depot", "hiseq2000_illumina", "IP");
		
		assertTrue("Context has errors", !ctxVal.hasErrors());
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void testValidateProtocolCodeStateNoNewInvalid() throws IllegalArgumentException, IllegalAccessException {		
		ContextValidation ctxVal = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);
		
		when(MongoDBDAO.checkObjectExist(any(String.class), any(Class.class), any(DBQuery.Query.class))).thenReturn(Boolean.FALSE);
		
		ExperimentValidationHelper.validateProtocolCode(ctxVal, "illumina-depot", "hiseq2000_illumina", "IP");
		
		assertTrue("Context has no error", ctxVal.hasErrors());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testValidateProtocolCodeStateNew() throws IllegalArgumentException, IllegalAccessException {		
		ContextValidation ctxVal = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);
		
		when(MongoDBDAO.checkObjectExist(any(String.class), any(Class.class), any(DBQuery.Query.class))).thenReturn(Boolean.TRUE);
		
		ExperimentValidationHelper.validateProtocolCode(ctxVal, "illumina-depot", "hiseq2000_illumina", "N");
		
		assertTrue("Context has errors", !ctxVal.hasErrors());
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void testValidateProtocolCodeStateNewInvalid() throws IllegalArgumentException, IllegalAccessException {		
		ContextValidation ctxVal = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);
		
		when(MongoDBDAO.checkObjectExist(any(String.class), any(Class.class), any(DBQuery.Query.class))).thenReturn(Boolean.FALSE);
		
		ExperimentValidationHelper.validateProtocolCode(ctxVal, "illumina-depot", "hiseq2000_illumina", "N");
		
		assertTrue("Context has no error", ctxVal.hasErrors());
	}
}