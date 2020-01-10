package experiments.rules;

import static org.fest.assertions.Assertions.assertThat;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.experiment.instance.InputContainerUsed;
import models.laboratory.experiment.instance.ManyToOneContainer;
import models.laboratory.experiment.instance.OutputContainerUsed;
import models.utils.instance.ExperimentHelper;

import org.junit.Test;

import controllers.experiments.api.Experiments;
import play.Logger;
import play.Logger.ALogger;
import utils.AbstractTests;
import utils.Constants;
import validation.ContextValidation;
import validation.experiment.instance.ExperimentValidationHelper;
import experiments.ExperimentTestHelper;

public class PoolTubeRulesTest extends AbstractTests {

	protected static ALogger logger=Logger.of("PoolTubeRulesTest");
	
	@Test
	public void validateExperimentPoolTube() {
		
		ContextValidation contextValidation = new ContextValidation(Constants.TEST_USER);
		Experiment exp=ExperimentTestHelper.getFakeExperimentWithAtomicExperiment("pool-tube");
		ExperimentValidationHelper.validateRules(exp, contextValidation);
		contextValidation.displayErrors(logger);
		assertThat(contextValidation.hasErrors()).isFalse();		
	}
	
	@Test
	public void validateTransfertTubeCalculations() {
		Experiment exp = ExperimentTestHelper.getFakeExperiment();
		exp.state.code = "IP";
		exp.typeCode="pool-tube";
		ManyToOneContainer atomicTransfert = ExperimentTestHelper.getManytoOneContainer();
		
		InputContainerUsed containerIn1 = ExperimentTestHelper.getInputContainerUsed("containerUsedIn1");
		containerIn1.percentage = 20.0;
		containerIn1.experimentProperties = null;
		
		InputContainerUsed containerIn2 = ExperimentTestHelper.getInputContainerUsed("containerUsedIn2");
		containerIn2.percentage = 20.0;
		containerIn2.experimentProperties = null;
		
		InputContainerUsed containerIn3 = ExperimentTestHelper.getInputContainerUsed("containerUsedIn3");
		containerIn3.percentage = 20.0;
		containerIn3.experimentProperties = null;
		
		InputContainerUsed containerIn4 = ExperimentTestHelper.getInputContainerUsed("containerUsedIn4");
		containerIn4.percentage = 20.0;
		containerIn4.experimentProperties = null;
		
		InputContainerUsed containerIn5 = ExperimentTestHelper.getInputContainerUsed("containerUsedIn5");
		containerIn5.percentage = 20.0;
		containerIn5.experimentProperties = null;
		
		OutputContainerUsed containerOut1 = ExperimentTestHelper.getOutputContainerUsed("containerUsedOut1");
		containerOut1.volume = new PropertySingleValue(new Double(40.0));
		
		atomicTransfert.inputContainerUseds.add(containerIn1);
		atomicTransfert.inputContainerUseds.add(containerIn2);
		atomicTransfert.inputContainerUseds.add(containerIn3);
		atomicTransfert.inputContainerUseds.add(containerIn4);
		atomicTransfert.inputContainerUseds.add(containerIn5);
		atomicTransfert.outputContainerUseds.add(containerOut1);
		
		exp.atomicTransfertMethods.add(0, atomicTransfert);
		
		ContextValidation contextValidation = new ContextValidation(Constants.TEST_USER);
		contextValidation.setUpdateMode();
		contextValidation.putObject("stateCode", exp.state.code);
		contextValidation.putObject("typeCode", exp.typeCode);

		ExperimentValidationHelper.validateAtomicTransfertMethods(exp.typeCode, exp.instrument, exp.atomicTransfertMethods, contextValidation);

		ExperimentHelper.doCalculations(exp,Experiments.calculationsRules);
		
		ManyToOneContainer atomicTransfertResult = (ManyToOneContainer)exp.atomicTransfertMethods.get(0);
		assertThat(atomicTransfertResult.inputContainerUseds.get(0).experimentProperties.get("inputVolume")).isNotNull();
		assertThat(atomicTransfertResult.inputContainerUseds.get(0).experimentProperties.get("inputVolume").value).isInstanceOf(Double.class);
		assertThat(atomicTransfertResult.inputContainerUseds.get(0).experimentProperties.get("inputVolume").value).isEqualTo(new Double(8.0));
		
		assertThat(atomicTransfertResult.inputContainerUseds.get(1).experimentProperties.get("inputVolume")).isNotNull();
		assertThat(atomicTransfertResult.inputContainerUseds.get(1).experimentProperties.get("inputVolume").value).isInstanceOf(Double.class);
		assertThat(atomicTransfertResult.inputContainerUseds.get(1).experimentProperties.get("inputVolume").value).isEqualTo(new Double(8.0));
		
		assertThat(atomicTransfertResult.inputContainerUseds.get(2).experimentProperties.get("inputVolume")).isNotNull();
		assertThat(atomicTransfertResult.inputContainerUseds.get(2).experimentProperties.get("inputVolume").value).isInstanceOf(Double.class);
		assertThat(atomicTransfertResult.inputContainerUseds.get(2).experimentProperties.get("inputVolume").value).isEqualTo(new Double(8.0));
		
		
		assertThat(atomicTransfertResult.outputContainerUseds.get(0).volume).isNotNull();
		assertThat(atomicTransfertResult.outputContainerUseds.get(0).volume.value).isInstanceOf(Double.class);
		assertThat(atomicTransfertResult.outputContainerUseds.get(0).volume.value).isEqualTo(new Double(40.0));
		
		
	}
	
}
