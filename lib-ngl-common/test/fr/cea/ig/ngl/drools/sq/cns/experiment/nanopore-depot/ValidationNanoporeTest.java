import static org.junit.Assert.assertEquals;

import org.junit.*;
import org.junit.runner.RunWith;
import org.kie.api.runtime.KieSession;
import org.powermock.modules.junit4.PowerMockRunner;

import fr.cea.ig.junit.drools.JUnitTest;
import fr.cea.ig.junit.drools.annotations.DroolsFiles;
import fr.cea.ig.junit.drools.annotations.DroolsSession;
import fr.cea.ig.ngl.dao.api.factory.ExperimentFactory;
import fr.cea.ig.ngl.utils.TestUtils;
import models.laboratory.experiment.instance.Experiment;
import validation.ContextValidation;

@RunWith(PowerMockRunner.class)
@DroolsFiles(
	value = {
		"lib-ngl-common/conf/rules/ngl-sq/cns/import.drl",
		"lib-ngl-common/conf/rules/ngl-sq/cns/experiment/nanopore-depot/validations.drl"
	}
)
public class ValidationNanoporeTest extends JUnitTest {

	@DroolsSession
	private KieSession session;
	
	/**
	 * Si on veut faire des traitements avant l'exécution de la suite de tests. 
	 * Par exemple : initialiser un mock, initialiser une méthode statique, ...
	 */
	public ValidationNanoporeTest() {
		// Initialisation de la base Drools.
		Experiment e = ExperimentFactory.getRandomExperimentNanopore();
		ContextValidation ctx = ContextValidation.createCreationContext(TestUtils.CURRENT_USER);
		
		session.insert(e);
		session.insert(ctx);
	}

	@Test
	public void testNumberRules() {		
        int rulesFired = session.fireAllRules();
        
        /*
         *  Combien de règles se sont déclenchées ? 
         *  1 - rule "init inputsupport nanopore"
		 *  2 - rule "only one inputssupport nanopore"
		 *  3 - rule "Validation nanopore-depot"
		 *  4 - rule "Validation code flowcell on exp.instrumentProperties nanopore-depot"
		 *  5 - rule "Validation code flowcell on atm.output.instrumentProperties nanopore-depot"
         */
		assertEquals("Not all the rules were fired", 1, rulesFired);
		
		// TODO : Qu'une seule règle se déclenche (règle 3), faire évoluer le mock.
	}
}