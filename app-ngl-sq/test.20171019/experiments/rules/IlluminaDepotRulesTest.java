package experiments.rules;

import static org.fest.assertions.Assertions.assertThat;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.experiment.instance.OneToVoidContainer;

import org.junit.Test;

import play.Logger;
import play.Logger.ALogger;
import utils.AbstractTests;
import utils.Constants;
import validation.ContextValidation;
import validation.experiment.instance.ExperimentValidationHelper;
import experiments.ExperimentTestHelper;

public class IlluminaDepotRulesTest extends AbstractTests {
	
protected static ALogger logger=Logger.of("IlluminaDepotRulesTest");
	
	@Test
	public void validateExperimentIlluminaDepot() {
		ContextValidation contextValidation = new ContextValidation(Constants.TEST_USER);
		Experiment exp=ExperimentTestHelper.getFakeExperimentWithAtomicExperimentOneToVoid("illumina-depot");
		ExperimentValidationHelper.validateRules(exp, contextValidation);
		contextValidation.displayErrors(logger);
		assertThat(contextValidation.hasErrors()).isFalse();		
	}

	
	@Test
	public void validateExperimentManySupportIlluminaDepot() {
		ContextValidation contextValidation = new ContextValidation(Constants.TEST_USER);
		Experiment exp=ExperimentTestHelper.getFakeExperimentWithAtomicExperimentOneToVoid("illumina-depot");
		OneToVoidContainer atomicTransfert3 = ExperimentTestHelper.getOnetoVoidContainer("error",100.0);
		exp.atomicTransfertMethods.add(atomicTransfert3);
		ExperimentValidationHelper.validateRules(exp, contextValidation);
		contextValidation.displayErrors(logger);
		assertThat(contextValidation.hasErrors()).isTrue();		
	}

}
