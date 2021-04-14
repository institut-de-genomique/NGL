package fr.cea.ig.ngl.validation;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.ngl.dao.api.factory.ResolutionFactory;
import fr.cea.ig.ngl.utils.TestUtils;
import models.laboratory.resolutions.instance.Resolution;
import models.laboratory.resolutions.instance.ResolutionConfiguration;
import validation.ContextValidation;
import validation.resolution.instance.ResolutionValidationHelper;

/**
 * Test du helper de validation de l'entité RESOLUTION.
 * 
 * @author jcharpen - Jordi Charpentier - jcharpen@genoscope.cns.fr
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ MongoDBDAO.class })
public class ResolutionValidationHelperTest {
    
    /**
	 * Si on veut faire des traitements avant l'exécution de la suite de tests. 
     * Par exemple : initialiser un mock, initialiser une méthode statique, ...
	 */
    @Before
	public void setUp() {
        PowerMockito.mockStatic(MongoDBDAO.class);
	}
    
    @Test
    public void testValidateResolutionsValid() {
        ContextValidation ctxVal = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);
        List<Resolution> resolutions = ResolutionFactory.getRandomResolutionsList(false);

        ResolutionValidationHelper.validateResolutions(ctxVal, resolutions);

        assertTrue("Context has errors", !ctxVal.hasErrors());
    }

    @Test
    public void testValidateResolutionsInvalid() {
        ContextValidation ctxVal = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);
        List<Resolution> resolutions = ResolutionFactory.getRandomResolutionsList(true);

        when(MongoDBDAO.checkObjectExist("ngl_common.ResolutionConfiguration", ResolutionConfiguration.class, "code", "2609aa0b-d4fc-4071-a515-009ecc3fe17d")).thenReturn(false);

        ResolutionValidationHelper.validateResolutions(ctxVal, resolutions);

        assertTrue("Context has no error", ctxVal.hasErrors());
    }
}