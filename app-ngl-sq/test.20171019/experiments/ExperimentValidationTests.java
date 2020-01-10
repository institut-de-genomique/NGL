package experiments;

import static org.fest.assertions.Assertions.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import models.laboratory.common.description.PropertyDefinition;
import models.laboratory.common.instance.property.PropertyImgValue;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.experiment.instance.InputContainerUsed;
import models.laboratory.experiment.instance.ManyToOneContainer;
import models.laboratory.experiment.instance.OutputContainerUsed;
import models.utils.InstanceConstants;
import models.utils.instance.ExperimentHelper;

import org.junit.Test;
import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import play.data.validation.ValidationError;
import utils.AbstractTests;
import utils.Constants;
import validation.ContextValidation;
import validation.experiment.instance.ExperimentValidationHelper;
import controllers.experiments.api.Experiments;
import fr.cea.ig.MongoDBDAO;

public class ExperimentValidationTests extends AbstractTests {
	
	
	@Test
	public void validatePropertiesFileImgErr() {
		Experiment exp = ExperimentTestHelper.getFakeExperiment();

		PropertyImgValue pImgValue = new  PropertyImgValue();
		byte[] data = new byte[] { (byte)0xe0, 0x4f, (byte)0xd0,
				0x20, (byte)0xea, 0x3a, 0x69, 0x10, (byte)0xa2, (byte)0xd8, 0x08, 0x00, 0x2b,
				0x30, 0x30, (byte)0x9d };
		pImgValue.value = data;
		pImgValue.fullname = "phylogeneticTree2.jpg";
		pImgValue.extension = "jpg";
		pImgValue.width = 250;
		pImgValue.height = 250;

		ContextValidation cv = new ContextValidation(Constants.TEST_USER); 
		cv.putObject("stateCode", "IP");

		PropertyDefinition pDef = getPropertyImgDefinition();

		Map<String, PropertyDefinition> hm= new HashMap<String, PropertyDefinition>();
		hm.put("restrictionEnzyme", pDef);

		cv.putObject("propertyDefinitions", hm.values());

		pImgValue.validate(cv);

		exp.instrumentProperties.put("enzymeChooser", pImgValue);

		showErrors(cv);

		MongoDBDAO.save(InstanceConstants.EXPERIMENT_COLL_NAME, exp);

		Experiment expBase = MongoDBDAO.findByCode(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, "TESTYANNEXP");

		assertThat(expBase.instrumentProperties.get("enzymeChooser").value);

		ExperimentValidationHelper.validateInstrumentUsed(exp.instrument,exp.instrumentProperties,cv);

		pImgValue.fullname = "test";

		expBase.instrumentProperties.clear();
		expBase.instrumentProperties.put("enzymeChooser", pImgValue);

		MongoDBDAO.update(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, 
				DBQuery.is("code", expBase.code),
				DBUpdate.set("instrumentProperties",expBase.instrumentProperties));

		Experiment expBase2 = MongoDBDAO.findByCode(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, ExperimentTestHelper.EXP_CODE);

		MongoDBDAO.deleteByCode(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, "TESTYANNEXP");

		assertThat(expBase2.instrumentProperties.get("enzymeChooser").value);

	}
	
	@Test
	public void validateFlowCellCalculations() {
		Experiment exp = ExperimentTestHelper.getFakeExperiment();
		exp.state.code = "IP";
		exp.typeCode="prepa-flowcell";
		ManyToOneContainer atomicTransfert = ExperimentTestHelper.getManytoOneContainer();

		InputContainerUsed containerIn1 = ExperimentTestHelper.getInputContainerUsed("containerUsedIn1");
		containerIn1.percentage = 50.0;
		containerIn1.concentration = new PropertySingleValue(new Integer(10)); 
		containerIn1.experimentProperties.put("NaOHVolume", new PropertySingleValue(new Double(1)));
		containerIn1.experimentProperties.put("NaOHConcentration", new PropertySingleValue(new Double(20)));
		containerIn1.experimentProperties.put("finalConcentration1", new PropertySingleValue(new Double(2)));
		containerIn1.experimentProperties.put("finalVolume1", new PropertySingleValue(new Double(20)));
		containerIn1.experimentProperties.put("phixConcentration", new PropertySingleValue(new Double(0.020)));
		containerIn1.experimentProperties.put("finalConcentration2", new PropertySingleValue(new Double(0.014)));
		containerIn1.experimentProperties.put("finalVolume2", new PropertySingleValue(new Double(1000)));

		OutputContainerUsed containerOut1 = ExperimentTestHelper.getOutputContainerUsed("containerUsedOut1");
		containerOut1.experimentProperties.put("phixPercent", new PropertySingleValue(new Double(1)));
		containerOut1.experimentProperties.put("finalVolume", new PropertySingleValue(new Double(120)));

		atomicTransfert.inputContainerUseds.add(containerIn1);
		atomicTransfert.outputContainerUseds.add(containerOut1);

		exp.atomicTransfertMethods.add(0, atomicTransfert);

		ContextValidation contextValidation = new ContextValidation(Constants.TEST_USER);
		contextValidation.setUpdateMode();
		contextValidation.putObject("stateCode", exp.state.code);
		contextValidation.putObject("typeCode", exp.typeCode);

		ExperimentValidationHelper.validateAtomicTransfertMethods(exp.typeCode, exp.instrument, exp.atomicTransfertMethods, contextValidation);

		ExperimentHelper.doCalculations(exp,Experiments.calculationsRules);

		ManyToOneContainer atomicTransfertResult = (ManyToOneContainer)exp.atomicTransfertMethods.get(0);
		assertThat(atomicTransfertResult.inputContainerUseds.get(0).experimentProperties.get("requiredVolume1")).isNotNull();
		assertThat(atomicTransfertResult.inputContainerUseds.get(0).experimentProperties.get("requiredVolume1").value).isInstanceOf(Double.class);
		assertThat(atomicTransfertResult.inputContainerUseds.get(0).experimentProperties.get("requiredVolume1").value).isEqualTo(new Double(4));
		assertThat(atomicTransfertResult.inputContainerUseds.get(0).experimentProperties.get("NaOHConcentration")).isNotNull();
		assertThat(atomicTransfertResult.inputContainerUseds.get(0).experimentProperties.get("NaOHConcentration").value).isInstanceOf(Double.class);
		assertThat(atomicTransfertResult.inputContainerUseds.get(0).experimentProperties.get("NaOHConcentration").value).isEqualTo(new Double(20));
		assertThat(atomicTransfertResult.inputContainerUseds.get(0).experimentProperties.get("EBVolume")).isNotNull();
		assertThat(atomicTransfertResult.inputContainerUseds.get(0).experimentProperties.get("EBVolume").value).isInstanceOf(Double.class);
		assertThat(atomicTransfertResult.inputContainerUseds.get(0).experimentProperties.get("EBVolume").value).isEqualTo(new Double(15));
		assertThat(atomicTransfertResult.inputContainerUseds.get(0).experimentProperties.get("requiredVolume2")).isNotNull();
		assertThat(atomicTransfertResult.inputContainerUseds.get(0).experimentProperties.get("requiredVolume2").value).isInstanceOf(Double.class);
		assertThat(atomicTransfertResult.inputContainerUseds.get(0).experimentProperties.get("requiredVolume2").value).isEqualTo(new Double(7));
		assertThat(atomicTransfertResult.inputContainerUseds.get(0).experimentProperties.get("phixVolume")).isNotNull();
		assertThat(atomicTransfertResult.inputContainerUseds.get(0).experimentProperties.get("phixVolume").value).isInstanceOf(Double.class);
		assertThat(atomicTransfertResult.inputContainerUseds.get(0).experimentProperties.get("phixVolume").value).isEqualTo(new Double(7));
		assertThat(atomicTransfertResult.inputContainerUseds.get(0).experimentProperties.get("HT1Volume")).isNotNull();
		assertThat(atomicTransfertResult.inputContainerUseds.get(0).experimentProperties.get("HT1Volume").value).isInstanceOf(Double.class);
		assertThat(atomicTransfertResult.inputContainerUseds.get(0).experimentProperties.get("HT1Volume").value).isEqualTo(new Double(986)); 
		assertThat(atomicTransfertResult.inputContainerUseds.get(0).experimentProperties.get("requiredVolume3")).isNotNull();
		assertThat(atomicTransfertResult.inputContainerUseds.get(0).experimentProperties.get("requiredVolume3").value).isInstanceOf(Double.class);
		assertThat(atomicTransfertResult.inputContainerUseds.get(0).experimentProperties.get("requiredVolume3").value).isEqualTo(new Double(60)); 

	}
	
	public PropertyDefinition getPropertyImgDefinition() {
		PropertyDefinition pDef = new PropertyDefinition();
		pDef.code = "restrictionEnzyme";
		pDef.name = "restrictionEnzyme";		
		pDef.active = true;
		pDef.required = true;
		pDef.valueType = "File";
		//pDef.propertyType = "Img";
		return pDef;
	}

	private void showErrors(ContextValidation cv) {
		if(cv.errors.size() > 0){
			for(Entry<String, List<ValidationError>> e : cv.errors.entrySet()){
				System.out.println(e);
			}
		}
	}

}
