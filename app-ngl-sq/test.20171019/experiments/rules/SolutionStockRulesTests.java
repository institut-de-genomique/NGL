package experiments.rules;

import static org.fest.assertions.Assertions.assertThat;

import java.util.ArrayList;

import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.experiment.instance.InputContainerUsed;
import models.laboratory.experiment.instance.OneToOneContainer;
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

public class SolutionStockRulesTests extends AbstractTests {
	
	protected static ALogger logger=Logger.of("SolutionStockRulesTests");
	
	@Test
	public void validateExperimentSolutionStock() {
		ContextValidation contextValidation = new ContextValidation(Constants.TEST_USER);
		Experiment exp=ExperimentTestHelper.getFakeExperimentWithAtomicExperiment("solution-stock");
		ExperimentValidationHelper.validateRules(exp, contextValidation);
		contextValidation.displayErrors(logger);
		assertThat(contextValidation.hasErrors()).isFalse();
	}
	
	@Test
	public void validateFinalVolumeNotNull(){
		Experiment exp = ExperimentTestHelper.getFakeExperiment();
		exp.state.code = "IP";
		exp.typeCode="solution-stock";
		OneToOneContainer atomicTransfert = ExperimentTestHelper.getOnetoOneContainer();
		
		InputContainerUsed containerIn1 = ExperimentTestHelper.getInputContainerUsed("containerUsedIn1");		
		containerIn1.concentration = new PropertySingleValue(new Double(19.23)); 
		containerIn1.volume = new PropertySingleValue(new Double(25.00));
		containerIn1.percentage = 100.0;
		containerIn1.experimentProperties=null;
		
		OutputContainerUsed containerOut1 = ExperimentTestHelper.getOutputContainerUsed("containerUsedOut1");
		containerOut1.volume =  null;
		containerOut1.concentration = new PropertySingleValue( new Double(10.0));
		
		atomicTransfert.inputContainerUseds = new ArrayList<InputContainerUsed>();
		atomicTransfert.inputContainerUseds.add(containerIn1);
		atomicTransfert.outputContainerUseds.add(containerOut1);		

		exp.atomicTransfertMethods.add(0, atomicTransfert);
		
		ContextValidation contextValidation = new ContextValidation(Constants.TEST_USER);
		contextValidation.setUpdateMode();
		contextValidation.putObject("stateCode", exp.state.code);
		contextValidation.putObject("typeCode", exp.typeCode);
		
		ExperimentValidationHelper.validateRules(exp, contextValidation);
		
		OneToOneContainer atomicTransfertResult = (OneToOneContainer)exp.atomicTransfertMethods.get(0);	
		
		contextValidation.displayErrors(logger);
		assertThat(contextValidation.hasErrors()).isTrue();
		assertThat(contextValidation.errors.size()).isEqualTo(1);		
		assertThat(atomicTransfertResult.outputContainerUseds.get(0).volume).isNull();		
		assertThat(atomicTransfertResult.outputContainerUseds.get(0).concentration).isNotNull();
		assertThat(atomicTransfertResult.outputContainerUseds.get(0).concentration.value).isInstanceOf(Double.class);
		assertThat(atomicTransfertResult.outputContainerUseds.get(0).concentration.value).isEqualTo(new Double(10.0)); 
	}
	
	@Test
	public void validateFinalConcentrationNotNull(){
		Experiment exp = ExperimentTestHelper.getFakeExperiment();
		exp.state.code = "IP";
		exp.typeCode="solution-stock";
		OneToOneContainer atomicTransfert = ExperimentTestHelper.getOnetoOneContainer();
		
		InputContainerUsed containerIn1 = ExperimentTestHelper.getInputContainerUsed("containerUsedIn1");		
		containerIn1.concentration = new PropertySingleValue(new Double(19.23)); 
		containerIn1.volume = new PropertySingleValue(new Double(25.00));
		containerIn1.percentage = 100.0;
		containerIn1.experimentProperties=null;
		
		OutputContainerUsed containerOut1 = ExperimentTestHelper.getOutputContainerUsed("containerUsedOut1");
		containerOut1.volume = new PropertySingleValue(new Double(30.0));
		containerOut1.concentration =  null;
		
		atomicTransfert.inputContainerUseds = new ArrayList<InputContainerUsed>();
		atomicTransfert.inputContainerUseds.add(containerIn1);
		atomicTransfert.outputContainerUseds.add(containerOut1);
		atomicTransfert.line = "1";
		atomicTransfert.column = "0";
		exp.atomicTransfertMethods.add(0, atomicTransfert);
		
		ContextValidation contextValidation = new ContextValidation(Constants.TEST_USER);
		contextValidation.setUpdateMode();
		contextValidation.putObject("stateCode", exp.state.code);
		contextValidation.putObject("typeCode", exp.typeCode);
		
		ExperimentValidationHelper.validateRules(exp, contextValidation);
		
		OneToOneContainer atomicTransfertResult = (OneToOneContainer)exp.atomicTransfertMethods.get(0);		
		
		contextValidation.displayErrors(logger);
		assertThat(contextValidation.hasErrors()).isTrue();
		assertThat(contextValidation.errors.size()).isEqualTo(1);	
		assertThat(atomicTransfertResult.outputContainerUseds.get(0).volume).isNotNull();
		assertThat(atomicTransfertResult.outputContainerUseds.get(0).volume.value).isInstanceOf(Double.class);
		assertThat(atomicTransfertResult.outputContainerUseds.get(0).volume.value).isEqualTo(new Double(30.0));
		assertThat(atomicTransfertResult.outputContainerUseds.get(0).concentration).isNull();
		
	} 
	
	@Test
	public void validateSolutionStockCalculations() {
		Experiment exp = ExperimentTestHelper.getFakeExperiment();
		exp.state.code = "IP";
		exp.typeCode="solution-stock";
		OneToOneContainer atomicTransfert = ExperimentTestHelper.getOnetoOneContainer();

		InputContainerUsed containerIn1 = ExperimentTestHelper.getInputContainerUsed("containerUsedIn1");		
		containerIn1.concentration = new PropertySingleValue(new Double(19.23)); 
		containerIn1.volume = new PropertySingleValue(new Double(25.00)); 
		containerIn1.experimentProperties=null;
		
		OutputContainerUsed containerOut1 = ExperimentTestHelper.getOutputContainerUsed("containerUsedOut1");
		containerOut1.volume =  new PropertySingleValue(new Double(30.0));
		containerOut1.concentration = new PropertySingleValue( new Double(10.0));		
		containerOut1.experimentProperties.put("requiredVolume", new PropertySingleValue());
		containerOut1.experimentProperties.put("bufferVolume", new PropertySingleValue());

		atomicTransfert.inputContainerUseds = new ArrayList<InputContainerUsed>();
		atomicTransfert.inputContainerUseds.add(containerIn1);
		atomicTransfert.outputContainerUseds.add(containerOut1);

		exp.atomicTransfertMethods.add(0, atomicTransfert);

		ContextValidation contextValidation = new ContextValidation(Constants.TEST_USER);
		contextValidation.setUpdateMode();
		contextValidation.putObject("stateCode", exp.state.code);
		contextValidation.putObject("typeCode", exp.typeCode);

		ExperimentValidationHelper.validateAtomicTransfertMethods(exp.typeCode, exp.instrument, exp.atomicTransfertMethods, contextValidation);

		ExperimentHelper.doCalculations(exp,Experiments.calculationsRules);

		OneToOneContainer atomicTransfertResult = (OneToOneContainer)exp.atomicTransfertMethods.get(0);	
		assertThat(atomicTransfertResult.inputContainerUseds.get(atomicTransfertResult.inputContainerUseds.indexOf(containerIn1)).experimentProperties.get("requiredVolume")).isNotNull();		
		assertThat(atomicTransfertResult.inputContainerUseds.get(atomicTransfertResult.inputContainerUseds.indexOf(containerIn1)).experimentProperties.get("requiredVolume").value).isInstanceOf(Double.class);
		assertThat(atomicTransfertResult.inputContainerUseds.get(atomicTransfertResult.inputContainerUseds.indexOf(containerIn1)).experimentProperties.get("requiredVolume").value).isEqualTo(new Double(15.60));
		assertThat(atomicTransfertResult.inputContainerUseds.get(atomicTransfertResult.inputContainerUseds.indexOf(containerIn1)).experimentProperties.get("bufferVolume")).isNotNull();		
		assertThat(atomicTransfertResult.inputContainerUseds.get(atomicTransfertResult.inputContainerUseds.indexOf(containerIn1)).experimentProperties.get("bufferVolume").value).isInstanceOf(Double.class);
		assertThat(atomicTransfertResult.inputContainerUseds.get(atomicTransfertResult.inputContainerUseds.indexOf(containerIn1)).experimentProperties.get("bufferVolume").value).isEqualTo(new Double(14.40));
			
		assertThat(atomicTransfertResult.outputContainerUseds.get(0).volume).isNotNull();
		assertThat(atomicTransfertResult.outputContainerUseds.get(0).volume.value).isInstanceOf(Double.class);
		assertThat(atomicTransfertResult.outputContainerUseds.get(0).volume.value).isEqualTo(new Double(30.0));
		assertThat(atomicTransfertResult.outputContainerUseds.get(0).concentration).isNotNull();
		assertThat(atomicTransfertResult.outputContainerUseds.get(0).concentration.value).isInstanceOf(Double.class);
		assertThat(atomicTransfertResult.outputContainerUseds.get(0).concentration.value).isEqualTo(new Double(10.0)); 
		

	}
	

}
