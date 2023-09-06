import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.junit.*;
import org.junit.runner.RunWith;
import org.kie.api.runtime.ClassObjectFilter;
import org.kie.api.runtime.KieSession;
import org.mongojack.DBQuery;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.MongoDBResult;
import fr.cea.ig.junit.drools.JUnitTest;
import fr.cea.ig.junit.drools.annotations.DroolsFiles;
import fr.cea.ig.junit.drools.annotations.DroolsSession;
import fr.cea.ig.ngl.dao.api.factory.ExperimentFactory;
import fr.cea.ig.ngl.utils.TestUtils;
import models.laboratory.experiment.instance.Experiment;
import play.modules.mongojack.MongoDBPlugin;
import validation.ContextValidation;

@RunWith(PowerMockRunner.class)
@DroolsFiles(
	value = {
		"lib-ngl-common/conf/rules/ngl-sq/cns/import.drl",
		"lib-ngl-common/conf/rules/ngl-sq/cns/experiment/bionano-depot/validations.drl"
	}
)
@PrepareForTest({ MongoDBPlugin.class, MongoDBDAO.class })
public class ValidationBionanoTest extends JUnitTest {

	@DroolsSession
	private KieSession session;
	 
	/**
	 * Si on veut faire des traitements avant l'exécution de la suite de tests. 
	 * Par exemple : initialiser un mock, initialiser une méthode statique, ...
	 */
	@Before
	public void setUp() {
		// Mock des classes statiques.
		PowerMockito.mockStatic(MongoDBPlugin.class);
		PowerMockito.mockStatic(MongoDBDAO.class);
		
		// Initialisation de la base Drools.
		Experiment e = ExperimentFactory.getRandomExperimentBionano();
		ContextValidation ctx = ContextValidation.createCreationContext(TestUtils.CURRENT_USER);
		
		session.insert(e);
		session.insert(ctx);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testNumberRules() {						
		// Mock de la recherche.

		MongoDBResult<Experiment> dbRes = mock(MongoDBResult.class);

		when(MongoDBDAO.find(any(String.class), any(Class.class), any(DBQuery.Query.class))).thenReturn(dbRes);
		when(dbRes.toList()).thenReturn(ExperimentFactory.getRandomExperimentBionanoList());

		// Déclenchement de la règle.
        int rulesFired = session.fireAllRules();
        
        /*
         *  Combien de règles se sont déclenchées ? 
         *  1 - rule "Validation chipIteration"
         */
        assertEquals("Not all the rules were fired", 1, rulesFired);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testValidateChipIterationValid() {
		// Mock de la recherche.

		MongoDBResult<Experiment> dbRes = mock(MongoDBResult.class);

		when(MongoDBDAO.find(any(String.class), any(Class.class), any(DBQuery.Query.class))).thenReturn(dbRes);
		when(dbRes.toList()).thenReturn(new ArrayList<>());

		// Déclenchement de la règle.
		session.fireAllRules();
        
        Collection<?> validations = session.getObjects(new ClassObjectFilter(ContextValidation.class));
		Iterator<?> it = validations.iterator();
		boolean hasContext = false;
        
        while (it.hasNext()) {
        	ContextValidation ctxValInRule = (ContextValidation) it.next();
			assertTrue("Context has errors", !ctxValInRule.hasErrors());
			hasContext = true;
		}

		if (!hasContext) {
			fail("No context was recovered.");
		}
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testValidateChipIterationInvalid() {
		// Mock de la recherche.

		MongoDBResult<Experiment> dbRes = mock(MongoDBResult.class);

		when(MongoDBDAO.find(any(String.class), any(Class.class), any(DBQuery.Query.class))).thenReturn(dbRes);
		when(dbRes.toList()).thenReturn(ExperimentFactory.getRandomExperimentBionanoList());

		// Déclenchement de la règle.
		session.fireAllRules();
        
        Collection<?> validations = session.getObjects(new ClassObjectFilter(ContextValidation.class));
		Iterator<?> it = validations.iterator();
		boolean hasContext = false;
        
        while (it.hasNext()) {
        	ContextValidation ctxValInRule = (ContextValidation) it.next();
			assertTrue("Context has no error", ctxValInRule.hasErrors());
			hasContext = true;
		}

		if (!hasContext) {
			fail("No context was recovered.");
		}
	}
}