package experiments.rules;

import static org.fest.assertions.Assertions.assertThat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.Content;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.experiment.instance.InputContainerUsed;
import models.laboratory.experiment.instance.ManyToOneContainer;
import models.laboratory.instrument.instance.InstrumentUsed;

import org.junit.Test;

import play.Logger;
import play.Logger.ALogger;
import utils.AbstractTests;
import utils.Constants;
import validation.ContextValidation;
import validation.experiment.instance.ExperimentValidationHelper;
import experiments.ExperimentTestHelper;

public class PrepaflowcellRulesTests extends AbstractTests {
	
	protected static ALogger logger=Logger.of("PrepaflowcellRulesTests");

	
	@Test
	public void validateExperimentPrepaflowcell() {
		ContextValidation contextValidation = new ContextValidation(Constants.TEST_USER);
		Experiment exp=ExperimentTestHelper.getFakeExperimentWithAtomicExperiment("prepa-flowcell");
		ExperimentValidationHelper.validateRules(exp, contextValidation);
		contextValidation.displayErrors(logger);
		assertThat(contextValidation.hasErrors()).isFalse();

	}

	@Test
	public void validateExperimentSameTagInPosition() {
		ContextValidation contextValidation = new ContextValidation(Constants.TEST_USER);
		Experiment exp=ExperimentTestHelper.getFakeExperimentWithAtomicExperiment("prepa-flowcell");
		Container container =new Container();
		Content content=new Content("CONTENT3", "TYPE", "CATEG");
		content.properties=new HashMap<String, PropertyValue>();
		content.properties.put("tag", new PropertySingleValue("IND1"));
		content.properties.put("tagCategory", new PropertySingleValue("TAGCATEGORIE"));
		container.contents.add(content);

		InputContainerUsed containerUsed=new InputContainerUsed();
		containerUsed.code = container.code;
		containerUsed.volume = container.volume;
		containerUsed.contents=container.contents;
		containerUsed.percentage= 0.0;
		exp.atomicTransfertMethods.get(0).inputContainerUseds.add(containerUsed);

		ExperimentValidationHelper.validateRules(exp, contextValidation);
		contextValidation.displayErrors(logger);
		assertThat(contextValidation.hasErrors()).isTrue();
		assertThat(contextValidation.errors.size()).isEqualTo(1);

	}
	
	
	@Test
	public void validateExperimentSameTagVIDEInPosition() {
		ContextValidation contextValidation = new ContextValidation(Constants.TEST_USER);
		Experiment exp=ExperimentTestHelper.getFakeExperimentWithAtomicExperiment("prepa-flowcell");
		
		ManyToOneContainer atomicTransfert3 = ExperimentTestHelper.getManytoOneContainer();
		atomicTransfert3.line="3";
		atomicTransfert3.column="0";
		
		exp.atomicTransfertMethods.add(2,atomicTransfert3);
		
		InputContainerUsed container3_1=ExperimentTestHelper.getInputContainerUsed("CONTAINER3_1");
		container3_1.percentage=20.0;
		Content content3_1=new Content("CONTENT3","TYPE","CATEGORIE");
		container3_1.contents=new ArrayList<Content>();;
		content3_1.properties=new HashMap<String, PropertyValue>();
		container3_1.contents.add(content3_1);
		

		InputContainerUsed container3_2=ExperimentTestHelper.getInputContainerUsed("CONTAINER3_2");
		container3_2.percentage=80.0;
		Content content3_2=new Content("CONTENT3","TYPE","CATEGORIE");
		container3_2.contents=new ArrayList<Content>();
		content3_2.properties=new HashMap<String, PropertyValue>();
		container3_2.contents.add(content3_2);

		exp.atomicTransfertMethods.get(2).inputContainerUseds.add(container3_1);
		exp.atomicTransfertMethods.get(2).inputContainerUseds.add(container3_2);
		
		ExperimentValidationHelper.validateRules(exp, contextValidation);
		contextValidation.displayErrors(logger);
		assertThat(contextValidation.hasErrors()).isFalse();
		assertThat(contextValidation.errors.size()).isEqualTo(0);

	}


	@Test
	public void validateExperimentManyTagCategory() {
		ContextValidation contextValidation = new ContextValidation(Constants.TEST_USER);
		Experiment exp=ExperimentTestHelper.getFakeExperimentWithAtomicExperiment("prepa-flowcell");
		Container container =new Container();
		Content content=new Content("CONTENT3", "TYPE", "CATEG");
		content.properties=new HashMap<String, PropertyValue>();
		content.properties.put("tag", new PropertySingleValue("IND11"));
		content.properties.put("tagCategory", new PropertySingleValue("OTHERCATEGORIE"));
		container.contents.add(content);

		InputContainerUsed containerUsed=new InputContainerUsed();
		containerUsed.code = container.code;
		containerUsed.volume = container.volume;
		containerUsed.contents=container.contents;
		containerUsed.percentage= 0.0;
		exp.atomicTransfertMethods.get(0).inputContainerUseds.add(containerUsed);

		ExperimentValidationHelper.validateRules(exp, contextValidation);
		contextValidation.displayErrors(logger);
		assertThat(contextValidation.hasErrors()).isTrue();
		assertThat(contextValidation.errors.size()).isEqualTo(1);

	}

	@Test
	public void validateExperimentSumPercentInPutContainer() {
		ContextValidation contextValidation = new ContextValidation(Constants.TEST_USER);
		Experiment exp=ExperimentTestHelper.getFakeExperimentWithAtomicExperiment("prepa-flowcell");
		Container container =new Container();
		Content content=new Content("CONTENT3", "TYPE", "CATEG");
		content.properties=new HashMap<String, PropertyValue>();
		content.properties.put("tag", new PropertySingleValue("IND11"));
		content.properties.put("tagCategory", new PropertySingleValue("TAGCATEGORIE"));
		container.contents.add(content);

		InputContainerUsed containerUsed=new InputContainerUsed();
		containerUsed.code = container.code;
		containerUsed.volume = container.volume;
		containerUsed.contents=container.contents;
		containerUsed.percentage= 10.0;
		exp.atomicTransfertMethods.get(0).inputContainerUseds.add(containerUsed);

		ExperimentValidationHelper.validateRules(exp, contextValidation);
		contextValidation.displayErrors(logger);
		assertThat(contextValidation.hasErrors()).isTrue();
		assertThat(contextValidation.errors.size()).isEqualTo(1);

	}

	@Test
	public void validateExperimentPrepaflowcellLaneNotNull() {
		ContextValidation contextValidation = new ContextValidation(Constants.TEST_USER);
		Experiment exp=ExperimentTestHelper.getFakeExperimentWithAtomicExperiment("prepa-flowcell");
		exp.atomicTransfertMethods.get(0).inputContainerUseds.clear();
		ExperimentValidationHelper.validateRules(exp, contextValidation);
		contextValidation.displayErrors(logger);
		assertThat(contextValidation.hasErrors()).isTrue();
		assertThat(contextValidation.errors.size()).isEqualTo(1);

	}


	@Test
	public void validateExperimentDuplicateContainerInLane() {
		ContextValidation contextValidation = new ContextValidation(Constants.TEST_USER);
		Experiment exp=ExperimentTestHelper.getFakeExperimentWithAtomicExperiment("prepa-flowcell");

		InputContainerUsed container1_1=ExperimentTestHelper.getInputContainerUsed("CONTAINER1_1");
		container1_1.percentage=0.0;
		Content content1_1=new Content("CONTENT1_1","TYPE","CATEGORIE");
		container1_1.contents=new ArrayList<Content>();
		content1_1.properties=new HashMap<String, PropertyValue>();
		content1_1.properties.put("tag", new PropertySingleValue("IND1"));
		content1_1.properties.put("tagCategory", new PropertySingleValue("TAGCATEGORIE"));
		content1_1.properties.put("tag", new PropertySingleValue("IND2"));
		content1_1.properties.put("tagCategory", new PropertySingleValue("TAGCATEGORIE"));
		container1_1.contents.add(content1_1);

		exp.atomicTransfertMethods.get(0).inputContainerUseds.add(container1_1);

		ExperimentValidationHelper.validateRules(exp, contextValidation);
		contextValidation.displayErrors(logger);
		assertThat(contextValidation.hasErrors()).isTrue();
		assertThat(contextValidation.errors.size()).isEqualTo(1);

	}

	@Test
	public void validateExperimentPrepaflowcellInstrumentProperties() {
		ContextValidation contextValidation = new ContextValidation(Constants.TEST_USER);
		Experiment exp=ExperimentTestHelper.getFakeExperimentWithAtomicExperiment("prepa-flowcell");

		exp.instrument=new InstrumentUsed();
		exp.instrument.code="cBot Fluor A";
		exp.instrument.outContainerSupportCategoryCode="flowcell-1";
		exp.instrumentProperties=new HashMap<String, PropertyValue>();
		exp.instrumentProperties.put("control", new PropertySingleValue("3"));

		ExperimentValidationHelper.validateRules(exp, contextValidation);
		contextValidation.displayErrors(logger);
		assertThat(contextValidation.hasErrors()).isTrue();
		assertThat(contextValidation.errors.get("instrument").size()).isEqualTo(2);

	}
	
	@Test
	public void validateCodeFlowcellWithoutBadChars(){
		ContextValidation contextValidation = new ContextValidation(Constants.TEST_USER);
		Experiment exp=ExperimentTestHelper.getFakeExperimentWithAtomicExperiment("prepa-flowcell");

		exp.instrument=new InstrumentUsed();
		exp.instrument.code="cBot Melisse";
		exp.instrument.outContainerSupportCategoryCode="flowcell-1";
		exp.instrumentProperties=new HashMap<String, PropertyValue>();
		exp.instrumentProperties.put("containerSupportCode", new PropertySingleValue("dhb 9846/VBDDJV*65454@ahdkjh"));

		ExperimentValidationHelper.validateRules(exp, contextValidation);
		contextValidation.displayErrors(logger);
		assertThat(contextValidation.hasErrors()).isTrue();
		assertThat(contextValidation.errors.get("instrument").size()).isEqualTo(1);
	}

	
	
	@Test
	public void validateExperimentSameLibProcessTypeCodeFromEmptyTag() {
		ContextValidation contextValidation = new ContextValidation(Constants.TEST_USER);
		Experiment exp=ExperimentTestHelper.getFakeExperimentWithAtomicExperiment("prepa-flowcell");
		
		ManyToOneContainer atomicTransfert3 = ExperimentTestHelper.getManytoOneContainer();
		atomicTransfert3.line="3";
		atomicTransfert3.column="0";
		
		exp.atomicTransfertMethods.add(2,atomicTransfert3);
		
		InputContainerUsed container3_1=ExperimentTestHelper.getInputContainerUsed("CONTAINER3_1");
		container3_1.percentage=20.0;
		Content content3_1=new Content("CONTENT3","TYPE","CATEGORIE");
		container3_1.contents=new ArrayList<Content>();
		content3_1.properties=new HashMap<String, PropertyValue>();
		content3_1.properties.put("libProcessTypeCode", new PropertySingleValue("F"));

		container3_1.contents.add(content3_1);
		

		InputContainerUsed container3_2=ExperimentTestHelper.getInputContainerUsed("CONTAINER3_2");
		container3_2.percentage=80.0;
		Content content3_2=new Content("CONTENT3","TYPE","CATEGORIE");
		container3_2.contents=new ArrayList<Content>();
		content3_2.properties=new HashMap<String, PropertyValue>();
		content3_2.properties.put("libProcessTypeCode", new PropertySingleValue("W"));
		container3_2.contents.add(content3_2);

		exp.atomicTransfertMethods.get(2).inputContainerUseds.add(container3_1);
		exp.atomicTransfertMethods.get(2).inputContainerUseds.add(container3_2);
		
		ExperimentValidationHelper.validateRules(exp, contextValidation);
		contextValidation.displayErrors(logger);
		assertThat(contextValidation.hasErrors()).isTrue();
		assertThat(contextValidation.errors.size()).isEqualTo(1);

	}

	
	@Test
	public void validateExperimenfromExperimentTypeCodeFromEmptyTagAndSameLibProcessTypeCode() {
		ContextValidation contextValidation = new ContextValidation(Constants.TEST_USER);
		Experiment exp=ExperimentTestHelper.getFakeExperimentWithAtomicExperiment("prepa-flowcell");
		
		ManyToOneContainer atomicTransfert3 = ExperimentTestHelper.getManytoOneContainer();
		atomicTransfert3.line="3";
		atomicTransfert3.column="0";
		
		exp.atomicTransfertMethods.add(2,atomicTransfert3);
		
		InputContainerUsed container3_1=ExperimentTestHelper.getInputContainerUsed("CONTAINER3_1");
		container3_1.percentage=20.0;
		container3_1.fromTransformationTypeCodes=new HashSet<String>();
		container3_1.fromTransformationTypeCodes.add("frag");
		Content content3_1=new Content("CONTENT3","TYPE","CATEGORIE");
		container3_1.contents=new ArrayList<Content>();
		content3_1.properties=new HashMap<String, PropertyValue>();
		content3_1.properties.put("libProcessTypeCode", new PropertySingleValue("W"));

		container3_1.contents.add(content3_1);
		

		InputContainerUsed container3_2=ExperimentTestHelper.getInputContainerUsed("CONTAINER3_2");
		container3_2.percentage=80.0;
		container3_2.fromTransformationTypeCodes=new HashSet<String>();
		container3_2.fromTransformationTypeCodes.add("lib");
		Content content3_2=new Content("CONTENT3","TYPE","CATEGORIE");
		container3_2.contents=new ArrayList<Content>();
		content3_2.properties=new HashMap<String, PropertyValue>();
		content3_2.properties.put("libProcessTypeCode", new PropertySingleValue("W"));
		container3_2.contents.add(content3_2);

		exp.atomicTransfertMethods.get(2).inputContainerUseds.add(container3_1);
		exp.atomicTransfertMethods.get(2).inputContainerUseds.add(container3_2);
		
		ExperimentValidationHelper.validateRules(exp, contextValidation);
		contextValidation.displayErrors(logger);
		assertThat(contextValidation.hasErrors()).isTrue();
		assertThat(contextValidation.errors.size()).isEqualTo(1);

	}
	
	
	
	@Test
	public void validateExperimenfromExperimentTypeCodeFromEmptyTag	() {
		ContextValidation contextValidation = new ContextValidation(Constants.TEST_USER);
		Experiment exp=ExperimentTestHelper.getFakeExperimentWithAtomicExperiment("prepa-flowcell");
		
		ManyToOneContainer atomicTransfert3 = ExperimentTestHelper.getManytoOneContainer();
		atomicTransfert3.line="3";
		atomicTransfert3.column="0";
		
		exp.atomicTransfertMethods.add(2,atomicTransfert3);
		
		InputContainerUsed container3_1=ExperimentTestHelper.getInputContainerUsed("CONTAINER3_1");
		container3_1.percentage=20.0;
		container3_1.fromTransformationTypeCodes=new HashSet<String>();
		container3_1.fromTransformationTypeCodes.add("frag");
		Content content3_1=new Content("CONTENT3","TYPE","CATEGORIE");
		container3_1.contents=new ArrayList<Content>();
		content3_1.properties=new HashMap<String, PropertyValue>();
		container3_1.contents.add(content3_1);
		

		InputContainerUsed container3_2=ExperimentTestHelper.getInputContainerUsed("CONTAINER3_2");
		container3_2.percentage=80.0;
		container3_2.fromTransformationTypeCodes=new HashSet<String>();
		container3_2.fromTransformationTypeCodes.add("lib");
		Content content3_2=new Content("CONTENT3","TYPE","CATEGORIE");
		container3_2.contents=new ArrayList<Content>();
		content3_2.properties=new HashMap<String, PropertyValue>();
		container3_2.contents.add(content3_2);

		exp.atomicTransfertMethods.get(2).inputContainerUseds.add(container3_1);
		exp.atomicTransfertMethods.get(2).inputContainerUseds.add(container3_2);
		
		ExperimentValidationHelper.validateRules(exp, contextValidation);
		contextValidation.displayErrors(logger);
		assertThat(contextValidation.hasErrors()).isTrue();
		assertThat(contextValidation.errors.size()).isEqualTo(1);

	}

}
