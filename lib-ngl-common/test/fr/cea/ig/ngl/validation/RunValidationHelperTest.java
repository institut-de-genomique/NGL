package fr.cea.ig.ngl.validation;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.ngl.dao.api.factory.SampleFactory;
import fr.cea.ig.ngl.dao.api.factory.run.RunFactory;
import fr.cea.ig.ngl.utils.TestUtils;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.sample.instance.Sample;
import models.utils.InstanceConstants;
import validation.ContextValidation;
import validation.run.instance.RunValidationHelper;

/**
 * Test du helper de validation de l'entité RUN.
 * 
 * @author jcharpen - Jordi Charpentier - jcharpen@genoscope.cns.fr
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ MongoDBDAO.class  })
public class RunValidationHelperTest {
    
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
    public void testValidateRunSampleCodesValid() {
        ContextValidation ctxVal = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);
        String runCode = RunFactory.getRandomRunCode();
        String sampleCode = SampleFactory.getRandomSampleCode();

        Set<String> sampleCodes = new HashSet<>();
        sampleCodes.add(sampleCode);

        when(MongoDBDAO.checkObjectExist(any(String.class), any(Class.class), any(Query.class))).thenReturn(true);

        RunValidationHelper.validateRunSampleCodes(ctxVal, runCode, sampleCodes);

        assertTrue("Context has errors", !ctxVal.hasErrors());
    }

    @Test
    public void testValidateRunSampleCodesInvalidRSAndSample() {
        ContextValidation ctxVal = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);
        String runCode = RunFactory.getRandomRunCode();
        String sampleCode = SampleFactory.getRandomSampleCode();

        Set<String> sampleCodes = new HashSet<>();
        sampleCodes.add(sampleCode);

        when(MongoDBDAO.checkObjectExist(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, DBQuery.is("code", sampleCode))).thenReturn(false);
        when(MongoDBDAO.checkObjectExist(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.and(DBQuery.is("runCode", runCode), DBQuery.is("sampleCode", sampleCode)))).thenReturn(false);

        RunValidationHelper.validateRunSampleCodes(ctxVal, runCode, sampleCodes);

        assertTrue("Context has no error", ctxVal.hasErrors());
    }

    @Test
    public void testValidateRunSampleCodesInvalidRS() {
        ContextValidation ctxVal = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);
        String runCode = RunFactory.getRandomRunCode();
        String sampleCode = SampleFactory.getRandomSampleCode();

        Set<String> sampleCodes = new HashSet<>();
        sampleCodes.add(sampleCode);

        when(MongoDBDAO.checkObjectExist(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, DBQuery.is("code", sampleCode))).thenReturn(true);
        when(MongoDBDAO.checkObjectExist(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.and(DBQuery.is("runCode", runCode), DBQuery.is("sampleCode", sampleCode)))).thenReturn(false);

        RunValidationHelper.validateRunSampleCodes(ctxVal, runCode, sampleCodes);

        assertTrue("Context has no error", ctxVal.hasErrors());
    }

    @Test
    public void testValidateRunSampleCodesInvalidSample() {
        ContextValidation ctxVal = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);
        String runCode = RunFactory.getRandomRunCode();
        String sampleCode = SampleFactory.getRandomSampleCode();

        Set<String> sampleCodes = new HashSet<>();
        sampleCodes.add(sampleCode);

        when(MongoDBDAO.checkObjectExist(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, DBQuery.is("code", sampleCode))).thenReturn(false);
        when(MongoDBDAO.checkObjectExist(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.and(DBQuery.is("runCode", runCode), DBQuery.is("sampleCode", sampleCode)))).thenReturn(true);

        RunValidationHelper.validateRunSampleCodes(ctxVal, runCode, sampleCodes);

        assertTrue("Context has no error", ctxVal.hasErrors());
    }
}