import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.api.runtime.ClassObjectFilter;
import org.kie.api.runtime.KieSession;
import org.mongojack.DBQuery;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.junit.drools.JUnitTest;
import fr.cea.ig.junit.drools.annotations.DroolsFiles;
import fr.cea.ig.junit.drools.annotations.DroolsSession;
import fr.cea.ig.ngl.dao.api.factory.ExperimentFactory;
import fr.cea.ig.ngl.utils.TestUtils;
import fr.cea.ig.play.IGGlobals;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.run.instance.Run;
import models.utils.InstanceHelpers;
import play.inject.Injector;
import play.modules.mongojack.MongoDBPlugin;
import validation.ContextValidation;
import workflows.run.RunWorkflows;

import java.util.Collection;
import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@DroolsFiles(
	value = {
		"lib-ngl-common/conf/rules/ngl-sq/cns/import.drl",
		"lib-ngl-common/conf/rules/ngl-sq/cns/experiment/nanopore-depot/workflow.drl"
	}
)
@PrepareForTest({ InstanceHelpers.class, IGGlobals.class, MongoDBPlugin.class, MongoDBDAO.class })
public class RunNanoporeTest extends JUnitTest {

	@DroolsSession
	private KieSession session;

	/**
	 * Si on veut faire des traitements avant l'exécution de la suite de tests. 
	 * Par exemple : initialiser un mock, initialiser une méthode statique, ...
	 */
	@Before
	@SuppressWarnings("unchecked")
	public void setUp() {		
		// Mock des classes statiques.
		PowerMockito.mockStatic(InstanceHelpers.class);
		PowerMockito.mockStatic(MongoDBPlugin.class);
		PowerMockito.mockStatic(IGGlobals.class);
		PowerMockito.mockStatic(MongoDBDAO.class);
		
		// Initialisation de la base Drools.
		Experiment e = ExperimentFactory.getRandomExperimentNanopore();
		ContextValidation ctx = ContextValidation.createCreationContext(TestUtils.CURRENT_USER);
		
		session.insert(e);
		session.insert(ctx);
		
		for (int i = 0; i < e.atomicTransfertMethods.size(); i++) {
			session.insert(e.atomicTransfertMethods.get(i));
		}
		
		// Mock d'IGGlobals et d'Injector.
		
		RunWorkflows rwf = mock(RunWorkflows.class);
		Injector inj = mock(Injector.class);

		try {
			PowerMockito.field(IGGlobals.class, "injector").set(Injector.class, inj);
		} catch (IllegalArgumentException err) {
			err.printStackTrace();
		} catch (IllegalAccessException err) {
			err.printStackTrace();
		}
		
		when(IGGlobals.injector()).thenReturn(inj);
		when(IGGlobals.injector().instanceOf(RunWorkflows.class)).thenReturn(rwf);
		
		// Mock de la recherche de run.
		
		when(MongoDBDAO.findOne(any(String.class), any(Class.class), any(DBQuery.Query.class))).thenReturn(null);
	}
	
	@Test
	public void testNumberRules() {						
        int rulesFired = session.fireAllRules();
        
        /*
         *  Combien de règles se sont déclenchées ? 
         *  1 - rule "Create Run"
         *  2 - rule "Update type Run RMINION"
         *  3 - rule "Save Run Nanopore"
         *  4 - rule "Update Workflow Run Bionano"
         */
        assertEquals("Not all the rules were fired", 4, rulesFired);
	}
	
	@Test
	public void testGeneratedRun() {						
        session.fireAllRules();
        
        Collection<?> validations = session.getObjects(new ClassObjectFilter(Run.class));
        Iterator<?> it = validations.iterator();
        
        while (it.hasNext()) {
        	Run runInRule = (Run) it.next();
        	
        	assertEquals("Code is not as expected", "121221_MN02670_FAA54955_A", runInRule.code);
        	assertEquals("Typecode is not as expected", "RMINION", runInRule.typeCode);
        	assertEquals("Generated run has not the right state", "N", runInRule.state.code);
        }
	}
}