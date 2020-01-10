package experiments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.container.instance.Content;
import models.laboratory.container.instance.LocationOnContainerSupport;
import models.laboratory.experiment.instance.AtomicTransfertMethod;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.experiment.instance.InputContainerUsed;
import models.laboratory.experiment.instance.ManyToOneContainer;
import models.laboratory.experiment.instance.OneToManyContainer;
import models.laboratory.experiment.instance.OneToOneContainer;
import models.laboratory.experiment.instance.OneToVoidContainer;
import models.laboratory.experiment.instance.OutputContainerUsed;
import models.laboratory.instrument.description.Instrument;
import models.laboratory.instrument.instance.InstrumentUsed;
import models.utils.dao.DAOException;
import play.Logger;

public class ExperimentTestHelper {
	
	public final static String EXP_CODE = "TESTYANNEXP";
	
	public static Experiment getFakeExperiment(){
		Experiment exp = new Experiment(EXP_CODE);
		exp.state = new State("N","ngsrg");
		exp.atomicTransfertMethods = new ArrayList<AtomicTransfertMethod>();
		exp.instrument = new InstrumentUsed();
		exp.instrument.outContainerSupportCategoryCode="tube";
		exp.experimentProperties = new HashMap<String, PropertyValue>();
		exp.instrumentProperties = new HashMap<String, PropertyValue>();
		
		return exp;
		
	}
	
	public static ManyToOneContainer getManytoOneContainer(){
		ManyToOneContainer atomicTransfertMethod = new ManyToOneContainer();
		atomicTransfertMethod.inputContainerUseds = new ArrayList<InputContainerUsed>();
		atomicTransfertMethod.outputContainerUseds = new ArrayList<OutputContainerUsed>();
		return atomicTransfertMethod;
	}
	
	public static OneToOneContainer getOnetoOneContainer(){
		OneToOneContainer atomicTransfertMethod = new OneToOneContainer();
		atomicTransfertMethod.inputContainerUseds = new ArrayList<InputContainerUsed>();
		atomicTransfertMethod.outputContainerUseds = new ArrayList<OutputContainerUsed>();
		
		return atomicTransfertMethod;
	}
	
	public static OneToManyContainer getOnetoManyContainer(){
		OneToManyContainer atomicTransfertMethod = new OneToManyContainer();
		atomicTransfertMethod.inputContainerUseds = new ArrayList<InputContainerUsed>();
		atomicTransfertMethod.outputContainerUseds = new ArrayList<OutputContainerUsed>();
		return atomicTransfertMethod;
	}
	
	
	public static InputContainerUsed getInputContainerUsed(String code){
		InputContainerUsed containerUsed = new InputContainerUsed(code);
		containerUsed.experimentProperties =  new HashMap<String, PropertyValue>();
		containerUsed.instrumentProperties =  new HashMap<String, PropertyValue>();
		containerUsed.locationOnContainerSupport=new LocationOnContainerSupport();
		containerUsed.locationOnContainerSupport.code=code;
		return containerUsed;
	}
	
	
	public static InputContainerUsed getInputContainerUsed(String code, Double percentage){
		InputContainerUsed containerUsed=getInputContainerUsed(code);
		containerUsed.percentage=percentage;
		return containerUsed;
	}
	
	public static OutputContainerUsed getOutputContainerUsed(String code){
		OutputContainerUsed containerUsed = new OutputContainerUsed(code);
		containerUsed.experimentProperties =  new HashMap<String, PropertyValue>();
		containerUsed.instrumentProperties =  new HashMap<String, PropertyValue>();
		containerUsed.locationOnContainerSupport=new LocationOnContainerSupport();
		containerUsed.locationOnContainerSupport.code=code;
		return containerUsed;
	}
	
	public static InstrumentUsed getInstrumentPrepFlowcell(){
		Instrument instrument = new Instrument();
		InstrumentUsed instrumentUsed = new InstrumentUsed();
		try {
			instrument = instrument.find.findByCode("cBot Fluor A");
			instrumentUsed.code = instrument.code;
			instrumentUsed.categoryCode = instrument.categoryCode;
			instrumentUsed.typeCode = instrument.typeCode;
		} catch (DAOException e) {
			// TODO Auto-generated catch block
			Logger.error("DAO error: "+e.getMessage(),e);;
		}
		
		return instrumentUsed;
	};
	
	public static Experiment getFakePrepFlowcell(){
		Random randomGenerator=new Random();
		String code = "TEST-PREPFLOWCELL"+randomGenerator.nextInt(1000);
		Experiment exp = getFakeExperimentWithAtomicExperimentManyToOne("prepa-flowcell");
		exp.code=code;
		exp.categoryCode = "transformation";
		exp.instrument = getInstrumentPrepFlowcell();
		
		return exp;
	}
	
	public static Experiment getFakeExperimentWithAtomicExperiment(String typeCode){
		Experiment exp = ExperimentTestHelper.getFakeExperiment();
		exp.typeCode=typeCode;		
		ManyToOneContainer atomicTransfert1 = ExperimentTestHelper.getManytoOneContainer();
		atomicTransfert1.line="1";
		atomicTransfert1.column="0";
		ManyToOneContainer atomicTransfert2 = ExperimentTestHelper.getManytoOneContainer();
		atomicTransfert2.line="2";
		atomicTransfert2.column="0";
		
		exp.atomicTransfertMethods.add(0,atomicTransfert1);
		exp.atomicTransfertMethods.add(1, atomicTransfert2);
		
		InputContainerUsed container1_1=ExperimentTestHelper.getInputContainerUsed("CONTAINER1_1");
		container1_1.percentage=20.0;
		Content content1_1=new Content("CONTENT1_1","TYPE","CATEGORIE");
		container1_1.contents=new ArrayList<Content>();
		content1_1.properties=new HashMap<String, PropertyValue>();
		content1_1.properties.put("tag", new PropertySingleValue("IND1"));
		content1_1.properties.put("tagCategory", new PropertySingleValue("TAGCATEGORIE"));
		content1_1.properties.put("tag", new PropertySingleValue("IND2"));
		content1_1.properties.put("tagCategory", new PropertySingleValue("TAGCATEGORIE"));
		container1_1.contents.add(content1_1);
		
		InputContainerUsed container1_2=ExperimentTestHelper.getInputContainerUsed("CONTAINER1_2");
		container1_2.percentage= 80.0;
		Content content1_2=new Content("CONTENT1_2","TYPE","CATEGORIE");
		container1_2.contents=new ArrayList<Content>();
		content1_2.properties=new HashMap<String, PropertyValue>();
		content1_2.properties.put("tag", new PropertySingleValue("IND1"));
		content1_2.properties.put("tagCategory", new PropertySingleValue("TAGCATEGORIE"));
		container1_2.contents.add(content1_2);
		
		atomicTransfert1.inputContainerUseds.add(container1_1);
		atomicTransfert1.inputContainerUseds.add(container1_2);
		
		InputContainerUsed container2_2=ExperimentTestHelper.getInputContainerUsed("CONTAINER2_2");
		container2_2.percentage= 100.0;
		Content content2_2=new Content("CONTENT2_2","TYPE","CATEGORIE");
		container2_2.contents=new ArrayList<Content>();
		content2_2.properties=new HashMap<String, PropertyValue>();
		container2_2.contents.add(content2_2);
		atomicTransfert2.inputContainerUseds.add(container2_2);
		return exp;
	}
	
	public static Experiment getFakeExperimentWithAtomicExperimentManyToOne(String typeCode){
		Experiment exp = getFakeExperiment();
		exp.typeCode=typeCode;		
 		ManyToOneContainer atomicTransfert1 = ExperimentTestHelper.getManytoOneContainer();
 		atomicTransfert1.line="1";
		atomicTransfert1.column="0";
		ManyToOneContainer atomicTransfert2 = ExperimentTestHelper.getManytoOneContainer();
		atomicTransfert2.line="2";
		atomicTransfert2.column="0";
		
		exp.atomicTransfertMethods.add(0,atomicTransfert1);
		exp.atomicTransfertMethods.add(1, atomicTransfert2);
		
		InputContainerUsed container1_1=ExperimentTestHelper.getInputContainerUsed("ADI_RD1");
		container1_1.percentage=20.0;
		Content content1_1=new Content("ADI_RD","MeTa-DNA","DNA");
		container1_1.contents=new ArrayList<Content>();
		content1_1.properties=new HashMap<String, PropertyValue>();
/*		content1_1.properties.put("tag", new PropertySingleValue("IND1"));
		content1_1.properties.put("tagCategory", new PropertySingleValue("TAGCATEGORIE"));*/
		content1_1.properties.put("tag", new PropertySingleValue("IND2"));
		content1_1.properties.put("tagCategory", new PropertySingleValue("TAGCATEGORIE"));
		container1_1.contents.add(content1_1);
		
		InputContainerUsed container1_2=ExperimentTestHelper.getInputContainerUsed("C2EV3ACXX_3");
		container1_2.percentage= 80.0;
		Content content1_2=new Content("BFB_AABA","amplicon","amplicon");
		container1_2.contents=new ArrayList<Content>();
		content1_2.properties=new HashMap<String, PropertyValue>();
		content1_2.properties.put("tag", new PropertySingleValue("IND1"));
		content1_2.properties.put("tagCategory", new PropertySingleValue("TAGCATEGORIE"));
		container1_2.contents.add(content1_2);
		
		atomicTransfert1.inputContainerUseds.add(container1_1);
		atomicTransfert1.inputContainerUseds.add(container1_2);
		
		InputContainerUsed container2_2=ExperimentTestHelper.getInputContainerUsed("C2EV3ACXX_5");
		container2_2.percentage= 100.0;
		Content content2_2=new Content("ADI_RD","MeTa-DNA","DNA");
		container2_2.contents=new ArrayList<Content>();
		content2_2.properties=new HashMap<String, PropertyValue>();
		container2_2.contents.add(content2_2);
		atomicTransfert2.inputContainerUseds.add(container2_2);
		return exp;
	}
	
	public static InstrumentUsed getInstrumentSolutionStock(){
		Instrument instrument = new Instrument();
		InstrumentUsed instrumentUsed = new InstrumentUsed();
		try {
			instrument = instrument.find.findByCode("hand");
			instrumentUsed.code = instrument.code;
			instrumentUsed.categoryCode = instrument.categoryCode;
			instrumentUsed.typeCode = instrument.typeCode;
		} catch (DAOException e) {
			// TODO Auto-generated catch block
			Logger.error("DAO error: "+e.getMessage(),e);;
		}
		
		return instrumentUsed;
	}
	
	public static Experiment getFakeSolutionStock(){
		Random randomGenerator=new Random();
		String code = "TEST-SOLUTIONSTOCK"+randomGenerator.nextInt(1000);
		Experiment exp = getFakeExperimentWithAtomicExperimentManyToOne("solution-stock");
		exp.code=code;
		exp.categoryCode = "transformation";
		exp.instrument = getInstrumentSolutionStock();
		
		return exp;
	}
	
	public static Experiment getFakeExperimentWithAtomicExperimentOneToOne(String typeCode){
		Experiment exp = getFakeExperiment();
		exp.typeCode=typeCode;
		OneToOneContainer atomicTransfert1 = ExperimentTestHelper.getOnetoOneContainer();
		OneToOneContainer atomicTransfert2 = ExperimentTestHelper.getOnetoOneContainer();
		
		exp.atomicTransfertMethods.add(0,atomicTransfert1);
		exp.atomicTransfertMethods.add(0,atomicTransfert2);

		return exp;
		
	}
	
	
	public static Experiment getFakeExperimentWithAtomicExperimentOneToVoid(String typeCode){
		Experiment exp = getFakeExperiment();
		exp.typeCode=typeCode;
		OneToVoidContainer atomicTransfert1 = ExperimentTestHelper.getOnetoVoidContainer("oneToVoid",100.0);
		OneToVoidContainer atomicTransfert2 = ExperimentTestHelper.getOnetoVoidContainer("oneToVoid",100.0);		
		exp.atomicTransfertMethods.add(atomicTransfert1);
		exp.atomicTransfertMethods.add(atomicTransfert2);
		return exp;
		
	}

	public static OneToVoidContainer getOnetoVoidContainer(String code,Double percentage) {
		OneToVoidContainer atomicTransfertMethod = new OneToVoidContainer();
		atomicTransfertMethod.inputContainerUseds = new ArrayList<InputContainerUsed>();
		atomicTransfertMethod.inputContainerUseds.add(ExperimentTestHelper.getInputContainerUsed(code,percentage));
		return atomicTransfertMethod;
	}
}
