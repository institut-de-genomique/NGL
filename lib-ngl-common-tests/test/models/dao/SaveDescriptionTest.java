//package models.dao;
//
//import java.util.ArrayList;
//import java.util.List;
//import models.laboratory.common.description.AbstractCategory;
//import models.laboratory.common.description.CommonInfoType;
//import models.laboratory.common.description.MeasureCategory;
//import models.laboratory.common.description.MeasureUnit;
//import models.laboratory.common.description.ObjectType;
//import models.laboratory.common.description.PropertyDefinition;
//import models.laboratory.common.description.State;
//import models.laboratory.common.description.StateCategory;
//import models.laboratory.common.description.Value;
//import models.laboratory.container.description.ContainerCategory;
//import models.laboratory.container.description.ContainerSupportCategory;
//import models.laboratory.experiment.description.ExperimentCategory;
//import models.laboratory.experiment.description.ExperimentType;
//import models.laboratory.experiment.description.ProtocolCategory;
//import models.laboratory.instrument.description.Instrument;
//import models.laboratory.instrument.description.InstrumentCategory;
//import models.laboratory.instrument.description.InstrumentUsedType;
//import models.laboratory.processes.description.ProcessCategory;
//import models.laboratory.processes.description.ProcessExperimentType;
//import models.laboratory.processes.description.ProcessType;
//import models.laboratory.project.description.ProjectCategory;
//import models.laboratory.project.description.ProjectType;
//// import models.laboratory.reagent.description.ReagentCatalog;
//import models.laboratory.sample.description.ImportCategory;
//import models.laboratory.sample.description.ImportType;
//import models.laboratory.sample.description.SampleCategory;
//import models.laboratory.sample.description.SampleType;
//import models.utils.dao.DAOException;
//import org.junit.Assert;
//import utils.AbstractTests;
//
///**
// * Test sur base vide avec dump.sql
// * 
// * @author ejacoby
// *
// */
//
//public class SaveDescriptionTest extends AbstractTests{
//		
//	
//	
//	/**
//	 * TEST OBJECT_TYPE
//	 * @throws DAOException 
//	 */
//	//@Test
//	public void saveObjectType() throws DAOException {
//		ObjectType objectType = new ObjectType();
//		objectType.code="Test";
//		objectType.generic=false;
//		long id = objectType.save();
//		ObjectType objectTypeDB = ObjectType.find.findById(id);
//		checkObjectType(objectTypeDB);
//		objectTypeDB = ObjectType.find.findById(objectTypeDB.id);
//		Assert.assertTrue(objectType.code.equals(objectTypeDB.code));
//		Assert.assertTrue(objectType.generic.equals(objectTypeDB.generic));
//	}
//
//	private void checkObjectType(ObjectType type) {
//		Assert.assertNotNull(type);
//		Assert.assertNotNull(type.id);
//		Assert.assertNotNull(type.code);
//		Assert.assertNotNull(type.generic);
//	}
//
//
//
//	/**
//	 * TEST STATE_CATEGORY
//	 * @throws DAOException 
//	 */
//	//@Test
//	public void saveStateCategory() throws DAOException {
//		StateCategory stateCategory = createStateCategory("catState1", "catState1");
//		stateCategory.id=stateCategory.save();
//		stateCategory=StateCategory.find.findById(stateCategory.id);
//		checkAbstractCategory(stateCategory);
//	}
//
//	private StateCategory createStateCategory(String code, String name) {
//		StateCategory stateCategory = new StateCategory();
//		stateCategory.code=code;
//		stateCategory.name=name;
//		return stateCategory;
//	}
//	
//	/**
//	 * TEST STATE
//	 * @throws DAOException 
//	 */
//	//@Test
//	public void saveState() throws DAOException {
//		//StateCategory stateCategory = StateCategory.find.findByCode("catState1");
//		//Assert.assertNotNull(stateCategory);
//		StateCategory stateCategory = createStateCategory("catState2", "catState2");
//		State state = createState("state1", "state1", 1, true,"experiment",stateCategory);
//		state.id = state.save();
//		state=State.find.findById(state.id);
//		checkState(state);
//	}
//
//	private State createState(String code, String name, Integer position, boolean active, String level, StateCategory stateCategory) {
//		State state = new State();
//		state.code=code;
//		state.name=name;
//		state.position=position;
//		state.active=active;
//		return state;
//	}
//
//	private void checkState(State state) {
//		Assert.assertNotNull(state);
//		Assert.assertNotNull(state.id);
//		Assert.assertNotNull(state.code);
//		Assert.assertNotNull(state.name);
//		Assert.assertNotNull(state.active);
//		Assert.assertNotNull(state.position);
//	}
//
//
//	/**
//	 * TEST MEASURE_CATEGORY
//	 * @throws DAOException 
//	 */
//	//@Test
//	public void saveMeasureCategory() throws DAOException {
//		MeasureCategory measureCategory = createMeasureCategory("cat1", "cat1");
//		List<MeasureUnit> measureValues = new ArrayList<MeasureUnit>();
//		measureValues.add(createMeasureValue("value1", "value1", true,measureCategory));
//		measureValues.add(createMeasureValue("value2", "value2", false,measureCategory));
//		measureCategory.id = measureCategory.save();
//		checkMeasureCategory(measureCategory);
//	}
//
//	
//	private void checkMeasureCategory(MeasureCategory measureCategory) {
//		Assert.assertNotNull(measureCategory);
//		Assert.assertNotNull(measureCategory.id);
//		Assert.assertNotNull(measureCategory.code);
//		Assert.assertNotNull(measureCategory.name);
//	}
//
//	private MeasureCategory createMeasureCategory(String code, String name)	{
//		MeasureCategory measureCategory = new MeasureCategory();
//		measureCategory.code=code;
//		measureCategory.name=name;
//		return measureCategory;
//	}
//
//	private MeasureUnit createMeasureValue(String code, String value, boolean defaultValue, MeasureCategory measureCategory) {
//		MeasureUnit measureValue = new MeasureUnit();
//		measureValue.code=code;
//		measureValue.value=value;
//		measureValue.defaultUnit=defaultValue;
//		measureValue.category=measureCategory;
//		return measureValue;
//	}
//
//	private void checkMeasureValue(MeasureUnit measureValue) {
//		Assert.assertNotNull(measureValue);
//		Assert.assertNotNull(measureValue.id);
//		Assert.assertNotNull(measureValue.code);
//		Assert.assertNotNull(measureValue.value);
//		Assert.assertNotNull(measureValue.defaultUnit);
//	}
//
//	/**
//	 * TEST REAGENT_TYPE
//	 * @throws DAOException 
//	 */
//	//@Test
//	/*public void saveReagentType() throws DAOException {
//		ReagentCategory reagentType = new ReagentCategory();
//		StateCategory stateCategory = StateCategory.find.findByCode("catState1");
//		List<State> states = new ArrayList<State>();
//		states.add(State.find.findByCode("state1"));
//		states.add(createState("state2", "state2", 2, true,"experiment",stateCategory));
//		List<Resolution> resolutions = new ArrayList<Resolution>();
//		resolutions.add(Resolution.find.findByCode("resol1"));
//		resolutions.add(createResolution("resol2", "resol2"));
//		ObjectType objectType = ObjectType.find.findByCode("Reagent");
//		MeasureCategory measureCategory = createMeasureCategory("cat2", "cat2");
//		MeasureUnit measureValue = createMeasureValue("value2", "value2", true, measureCategory);
//		List<Value> possibleValues = new ArrayList<Value>();
//		possibleValues.add(createValue("value3","value3", true));
//		possibleValues.add(createValue("value4","value4", false));
//		List<Value> possibleValues2 = new ArrayList<Value>();
//		possibleValues2.add(createValue("value5","value5", true));
//		List<PropertyDefinition> propertiesDefinitions = new ArrayList<PropertyDefinition>();
//		propertiesDefinitions.add(createPropertyDefinition("prop1", "prop1", true, true, "default", "descProp1", "format1", 1, "in", "content", true, true, "type1", measureCategory, measureValue, measureValue, possibleValues));
//		propertiesDefinitions.add(createPropertyDefinition("prop2", "prop2", true, true, "default", "descProp2", "format2", 2, "in", "content", true, true, "type2", measureCategory, measureValue, measureValue, possibleValues2));
//		CommonInfoType commonInfoType = createCommonInfoType("reagent1", "reagent1", "reagent1", states, resolutions, propertiesDefinitions, objectType);
//		reagentType.setCommonInfoType(commonInfoType);
//		reagentType.id = reagentType.save();
//		reagentType=ReagentCategory.find.findByCode(reagentType.code);
//		checkCommonInfoType(reagentType);
//	}*/
//
//	private void checkCommonInfoType(CommonInfoType commonInfoType) {
//		Assert.assertNotNull(commonInfoType);
//		Assert.assertNotNull(commonInfoType.id);
//		Assert.assertNotNull(commonInfoType.code);
//		Assert.assertNotNull(commonInfoType.name);
//		
//		Assert.assertNotNull(commonInfoType.propertiesDefinitions);
//		Assert.assertTrue(commonInfoType.propertiesDefinitions.size()>0);
//		for(PropertyDefinition propertyDefinition : commonInfoType.propertiesDefinitions){
//			checkPropertyDefinition(propertyDefinition);
//		}
//		Assert.assertNotNull(commonInfoType.objectType);
//		checkObjectType(commonInfoType.objectType);
//	}
//
//	private CommonInfoType createCommonInfoType(String code, String name, String collectionName, 
//			List<State> variableStates, List<PropertyDefinition> propertiesDefinitions, ObjectType objectType)	{
//		CommonInfoType commonInfoType=new CommonInfoType();
//		commonInfoType.code=code;
//		commonInfoType.name=name;
//		commonInfoType.propertiesDefinitions=propertiesDefinitions;
//		commonInfoType.objectType=objectType;
//		return commonInfoType;
//	}
//
//	private void checkPropertyDefinition(PropertyDefinition propertyDefinition) {
//		Assert.assertNotNull(propertyDefinition);
//		Assert.assertNotNull(propertyDefinition.id);
//		Assert.assertNotNull(propertyDefinition.code);
//		Assert.assertNotNull(propertyDefinition.name);
//		Assert.assertNotNull(propertyDefinition.active);
//		Assert.assertNotNull(propertyDefinition.choiceInList);
//		Assert.assertNotNull(propertyDefinition.defaultValue);
//		Assert.assertNotNull(propertyDefinition.description);
//		Assert.assertNotNull(propertyDefinition.displayFormat);
//		Assert.assertNotNull(propertyDefinition.displayOrder);
//		//Assert.assertNotNull(propertyDefinition.inOut);
//		Assert.assertNotNull(propertyDefinition.levels);
//		Assert.assertNotNull(propertyDefinition.required);
//		Assert.assertNotNull(propertyDefinition.valueType);
//		Assert.assertNotNull(propertyDefinition.measureCategory);
//		Assert.assertNotNull(propertyDefinition.saveMeasureValue);
//		checkMeasureValue(propertyDefinition.saveMeasureValue);
//		Assert.assertNotNull(propertyDefinition.displayMeasureValue);
//		checkMeasureValue(propertyDefinition.displayMeasureValue);
//		Assert.assertNotNull(propertyDefinition.possibleValues);
//		Assert.assertTrue(propertyDefinition.possibleValues.size()>0);
//		for(Value value : propertyDefinition.possibleValues){
//			checkValue(value);
//		}
//	}
//
//	private PropertyDefinition createPropertyDefinition(String code, String name, boolean active, boolean choiceInList, String defaultValue, String description, String displayFormat, Integer displayOrder, String inOut, String level, boolean propagation, boolean required, String type,
//			MeasureCategory measureCategory,MeasureUnit measureValue, MeasureUnit displayMeasureValue, List<Value> possibleValues) {
//		PropertyDefinition propertyDefinition = new PropertyDefinition();
//		propertyDefinition.code=code;
//		propertyDefinition.name=name;
//		propertyDefinition.active=active;
//		propertyDefinition.choiceInList=choiceInList;
//		propertyDefinition.defaultValue=defaultValue;
//		propertyDefinition.description=description;
//		propertyDefinition.displayFormat=displayFormat;
//		propertyDefinition.displayOrder=displayOrder;
//		//propertyDefinition.inOut=inOut;
//		//propertyDefinition.level=level;
//		propertyDefinition.required=required;
//		propertyDefinition.valueType=type;
//		propertyDefinition.measureCategory=measureCategory;
//		propertyDefinition.saveMeasureValue=measureValue;
//		propertyDefinition.displayMeasureValue = displayMeasureValue;
//		propertyDefinition.possibleValues=possibleValues;
//		return propertyDefinition;
//	}
//
//	private Value createValue(String code, String value, boolean defaultValue) {
//		Value newValue = new Value();
//		newValue.code=code;
//		newValue.value=value;
//		newValue.defaultValue=defaultValue;
//		return newValue;
//	}
//
//	private void checkValue(Value value) {
//		Assert.assertNotNull(value);
//		Assert.assertNotNull(value.code);
//		Assert.assertNotNull(value.value);
//		Assert.assertNotNull(value.defaultValue);
//	}
//
//	/**
//	 * TEST PROTOCOL_CATEGORY
//	 * @throws DAOException 
//	 */
//	//@Test
//	public void saveProtocolCategory() throws DAOException {
//		ProtocolCategory protocolCategory = createProtocolCategory("protoCat1", "protoCat1");
//		protocolCategory.id = protocolCategory.save();
//		protocolCategory = ProtocolCategory.find.findById(protocolCategory.id);
//		checkAbstractCategory(protocolCategory);
//	}
//	
//	private void checkAbstractCategory(AbstractCategory<?> abstractCategory) {
//		Assert.assertNotNull(abstractCategory);
//		Assert.assertNotNull(abstractCategory.id);
//		Assert.assertNotNull(abstractCategory.code);
//		Assert.assertNotNull(abstractCategory.name);
//	}
//
//	private ProtocolCategory createProtocolCategory(String code, String name) {
//		ProtocolCategory protocolCategory = new ProtocolCategory();
//		protocolCategory.code=code;
//		protocolCategory.name=name;
//		return protocolCategory;
//	}
//
//	/**
//	 * TEST PROTOCOL
//	 * @throws DAOException 
//	 */
//	//@Test
//	/*public void saveProtocol() throws DAOException {
//		List<ReagentCategory> reagentTypes = new ArrayList<ReagentCategory>();
//		ReagentCategory reagentType = ReagentCategory.find.findByCode("reagent1");
//		reagentTypes.add(reagentType);
//		Protocol protocol = createProtocol("proto1","proto1", "path1", "V1", createProtocolCategory("protoCat2", "protoCat2"), reagentTypes);
//		protocol.id = protocol.save();
//		protocol = Protocol.find.findById(protocol.id);
//		checkProtocol(protocol);
//	}*/
//
//
//	
//
//	/**
//	 * TEST CONTAINER_SUPPORT_CATEGORY
//	 * @throws DAOException 
//	 */
//	//@Test
//	public void saveContainerSupportCategory() throws DAOException {
//		ContainerSupportCategory containerSupportCategory = createContainerSupportCategory("support1", "support1", 10, 10, 10);
//		containerSupportCategory.id = containerSupportCategory.save();
//		containerSupportCategory = ContainerSupportCategory.find.findByCode(containerSupportCategory.code);
//		checkContainerSupportCategory(containerSupportCategory);
//	}
//	
//	private ContainerSupportCategory createContainerSupportCategory(String code, String name, int nbLine, int nbColumn, int nbUsableContainer) {
//		ContainerSupportCategory containerSupportCategory = new ContainerSupportCategory();
//		containerSupportCategory.code=code;
//		containerSupportCategory.name=name;
//		containerSupportCategory.nbLine=nbLine;
//		containerSupportCategory.nbColumn=nbColumn;
//		containerSupportCategory.nbUsableContainer=nbUsableContainer;
//		return containerSupportCategory;
//	}
//
//	private void checkContainerSupportCategory(ContainerSupportCategory containerSupportCategory) {
//		checkAbstractCategory(containerSupportCategory);
//		Assert.assertNotNull(containerSupportCategory.nbLine);
//		Assert.assertNotNull(containerSupportCategory.nbColumn);
//		Assert.assertNotNull(containerSupportCategory.nbUsableContainer);
//	}
//
//	/**
//	 * TEST INSTRUMENT_CATEGORY
//	 * @throws DAOException 
//	 */
//	//@Test
//	public void saveInstrumentCategory() throws DAOException {
//		List<ContainerSupportCategory> inContainerSupportCategories = new ArrayList<ContainerSupportCategory>();
//		inContainerSupportCategories.add(ContainerSupportCategory.find.findByCode("support1"));
//		List<ContainerSupportCategory> outContainerSupportCategories = new ArrayList<ContainerSupportCategory>();
//		outContainerSupportCategories.add(createContainerSupportCategory("support2", "support2", 5, 10, 10));
//		InstrumentCategory instrumentCategory = createInstrumentCategory("InstCat1", "InstCat1", 1, inContainerSupportCategories, 1, outContainerSupportCategories);
//		instrumentCategory.id = instrumentCategory.save();
//		instrumentCategory = InstrumentCategory.find.findById(instrumentCategory.id);
//		checkInstrumentCategory(instrumentCategory);
//		//Assert.assertTrue(instrumentCategory.inContainerSupportCategories.size()==1);
//		//Assert.assertTrue(instrumentCategory.outContainerSupportCategories.size()==1);
//	}
//	
//	private InstrumentCategory createInstrumentCategory(String code, String name, 
//			int nbInContainerSupportCategories, List<ContainerSupportCategory> inContainerSupportCategories,
//			int nbOutContainerSupportCategories, List<ContainerSupportCategory> outContainerSupportCategories) {
//		InstrumentCategory instrumentCategory = new InstrumentCategory();
//		instrumentCategory.code=code;
//		instrumentCategory.name=name;
//		//instrumentCategory.nbInContainerSupports=nbInContainerSupportCategories;
//		//instrumentCategory.inContainerSupportCategories=inContainerSupportCategories;
//		//instrumentCategory.nbOutContainerSupports=nbOutContainerSupportCategories;
//		//instrumentCategory.outContainerSupportCategories=outContainerSupportCategories;
//		return instrumentCategory;
//	}
//	
//	private void checkInstrumentCategory(InstrumentCategory instrumentCategory) {
//		checkAbstractCategory(instrumentCategory);
//		//Assert.assertNotNull(instrumentCategory.nbInContainerSupports);
//	//	Assert.assertNotNull(instrumentCategory.nbOutContainerSupports);
//	//	Assert.assertNotNull(instrumentCategory.inContainerSupportCategories);
//	//	Assert.assertTrue(instrumentCategory.inContainerSupportCategories.size()>0);
//		//for(ContainerSupportCategory containerSupportCategory :instrumentCategory.inContainerSupportCategories){
//		//	checkContainerSupportCategory(containerSupportCategory);
//		//}
//		//Assert.assertNotNull(instrumentCategory.outContainerSupportCategories);
//		//Assert.assertTrue(instrumentCategory.outContainerSupportCategories.size()>0);
//		//for(ContainerSupportCategory containerSupportCategory :instrumentCategory.outContainerSupportCategories){
//		//	checkContainerSupportCategory(containerSupportCategory);
//		//}
//	}
//
//	/**
//	 * TEST INSTRUMENT_USED_TYPE
//	 * @throws DAOException 
//	 */
//	//@Test
//	public void saveInstrumentUsedType() throws DAOException {
//		//Create commonInfoType
//		List<State> states = new ArrayList<State>();
//		states.add(State.find.findByCode("state1"));
//		ObjectType objectType = ObjectType.find.findByCode("Instrument");
//		MeasureCategory measureCategory = MeasureCategory.find.findByCode("cat2");
//		MeasureUnit measureValue = MeasureUnit.find.findByValue("value2");
//		List<Value> possibleValues = new ArrayList<Value>();
//		possibleValues.add(createValue("value6","value6", true));
//		possibleValues.add(createValue("value7","value7", false));
//		List<PropertyDefinition> propertiesDefinitions = new ArrayList<PropertyDefinition>();
//		propertiesDefinitions.add(createPropertyDefinition("prop3", "prop3", true, true, "default", "descProp1", "format1", 1, "in", "content", true, true, "type1", measureCategory, measureValue, measureValue, possibleValues));
//		CommonInfoType commonInfoType = createCommonInfoType("inst1", "inst1", "inst1", states, propertiesDefinitions, objectType);
//
//		//Get instrumentCategory
//		List<ContainerSupportCategory> inContainerSupportCategories = new ArrayList<ContainerSupportCategory>();
//		inContainerSupportCategories.add(createContainerSupportCategory("support5", "support5", 10, 10, 10));
//		List<ContainerSupportCategory> outContainerSupportCategories = new ArrayList<ContainerSupportCategory>();
//		outContainerSupportCategories.add(createContainerSupportCategory("support6", "support6", 10, 10, 10));
//		InstrumentCategory instrumentCategory = createInstrumentCategory("InstCat2", "InstCat2", 10, inContainerSupportCategories, 10, outContainerSupportCategories);
//
//		//Get instrument
//		List<Instrument> instruments = new ArrayList<Instrument>();
//		instruments.add(createInstrument("inst1", "inst1"));
//
//		InstrumentUsedType instrumentUsedType = createInstrumentUsedType(commonInfoType, instrumentCategory, instruments);
//		instrumentUsedType.id = instrumentUsedType.save();
//		instrumentUsedType = InstrumentUsedType.find.findById(instrumentUsedType.id);
//		checkInstrumentUsedType(instrumentUsedType);
//
//	}
//
//	private InstrumentUsedType createInstrumentUsedType(CommonInfoType commonInfoType, InstrumentCategory instrumentCategory,List<Instrument> instruments) {
//		InstrumentUsedType instrumentUsedType = new InstrumentUsedType();
//		instrumentUsedType.setCommonInfoType(commonInfoType);
//		instrumentUsedType.category=instrumentCategory;
//		instrumentUsedType.instruments=instruments;
//		return instrumentUsedType;
//	}
//
//	private void checkInstrumentUsedType(InstrumentUsedType instrumentUsedType) {
//		Assert.assertNotNull(instrumentUsedType);
//		checkCommonInfoType(instrumentUsedType);
//		checkInstrumentCategory(instrumentUsedType.category);
//		Assert.assertNotNull(instrumentUsedType.instruments);
//		Assert.assertTrue(instrumentUsedType.instruments.size()>0);
//		for(Instrument instrument : instrumentUsedType.instruments){
//			checkInstrument(instrument);
//		}
//	}
//	
//	private Instrument createInstrument(String code, String name) {
//		Instrument instrument = new Instrument();
//		instrument.code=code;
//		instrument.name=name;
//		return instrument;
//	}
//	
//	private void checkInstrument(Instrument instrument) {
//		Assert.assertNotNull(instrument);
//		Assert.assertNotNull(instrument.id);
//		Assert.assertNotNull(instrument.code);
//		Assert.assertNotNull(instrument.name);
//	}
//	
//	private void checkAbstractExperiment(ExperimentType experiment) {
//		Assert.assertNotNull(experiment);
//		checkCommonInfoType(experiment);
//		Assert.assertNotNull(experiment.instrumentUsedTypes);
//		Assert.assertTrue(experiment.instrumentUsedTypes.size()>0);
//		for(InstrumentUsedType instrumentUsedType : experiment.instrumentUsedTypes){
//			checkInstrumentUsedType(instrumentUsedType);
//		}
//		
//	}
//
//	/**
//	 * TEST EXPERIMENT_CATEGORY
//	 * @throws DAOException 
//	 */
//	//@Test
//	public void saveExperimentCategory() throws DAOException {
//		ExperimentCategory experimentCategory = createExperimentCategory("expCat1", "expCat2");
//		experimentCategory.id=experimentCategory.save();
//		experimentCategory=ExperimentCategory.find.findById(experimentCategory.id);
//		checkAbstractCategory(experimentCategory);
//	}
//	
//	private ExperimentCategory createExperimentCategory(String code, String name) {
//		ExperimentCategory experimentCategory = new ExperimentCategory();
//		experimentCategory.code=code;
//		experimentCategory.name=name;
//		return experimentCategory;
//	}
//	
//
//	/**
//	 * TEST QUALITY_CONTROL_TYPE
//	 * @throws DAOException 
//	 */
//	//@Test
//	public void saveQualityControlType() throws DAOException {
//		//Create commonInfoType
//		List<State> states = new ArrayList<State>();
//		states.add(State.find.findByCode("state2"));
//		ObjectType objectType = ObjectType.find.findByCode("Experiment");
//		MeasureCategory measureCategory = MeasureCategory.find.findByCode("cat2");
//		MeasureUnit measureValue = MeasureUnit.find.findByValue("value2");
//		List<Value> possibleValues = new ArrayList<Value>();
//		possibleValues.add(createValue("value10","value10", true));
//		List<PropertyDefinition> propertiesDefinitions = new ArrayList<PropertyDefinition>();
//		propertiesDefinitions.add(createPropertyDefinition("prop6", "prop6", true, true, "default", "descProp1", "format1", 1, "in", "content", true, true, "type1", measureCategory, measureValue, measureValue, possibleValues));
//		CommonInfoType commonInfoType = createCommonInfoType("qc1", "qc1", "qc1", states, propertiesDefinitions, objectType);
//
//		//Create list instrument 
//		List<InstrumentUsedType> instrumentUsedTypes = new ArrayList<InstrumentUsedType>();
//		instrumentUsedTypes.add(InstrumentUsedType.find.findByCode("inst1"));
//
//		// List<ReagentCatalog> reagentTypes = new ArrayList<ReagentCatalog>();
//		//ReagentCategory reagentType = ReagentCategory.find.findByCode("reagent1");
//		//reagentTypes.add(reagentType);
//		ExperimentCategory experimentCategory = ExperimentCategory.find.findByCode("expCat1");
//		
//		ExperimentType qualityControlType = createExperimentType(commonInfoType, instrumentUsedTypes,experimentCategory);
//		qualityControlType.id = qualityControlType.save();
//		qualityControlType = ExperimentType.find.findById(qualityControlType.id);
//		checkAbstractExperiment(qualityControlType);
//	}
//	
//	/**
//	 * TEST EXPERIMENT_TYPE
//	 * @throws DAOException 
//	 */
//	//@Test
//	public void saveExperimentType() throws DAOException {
//		//Create commonInfoType
//		List<State> states = new ArrayList<State>();
//		states.add(State.find.findByCode("state2"));
//		ObjectType objectType = ObjectType.find.findByCode("Experiment");
//		MeasureCategory measureCategory = MeasureCategory.find.findByCode("cat2");
//		MeasureUnit measureValue = MeasureUnit.find.findByValue("value2");
//		List<Value> possibleValues = new ArrayList<Value>();
//		possibleValues.add(createValue("value12","value12", true));
//		List<PropertyDefinition> propertiesDefinitions = new ArrayList<PropertyDefinition>();
//		propertiesDefinitions.add(createPropertyDefinition("prop8", "prop8", true, true, "default", "descProp1", "format1", 1, "in", "content", true, true, "type1", measureCategory, measureValue, measureValue, possibleValues));
//		CommonInfoType commonInfoType = createCommonInfoType("exp1", "exp1", "exp1", states, propertiesDefinitions, objectType);
//
//		//Create list instrument 
//		List<InstrumentUsedType> instrumentUsedTypes = new ArrayList<InstrumentUsedType>();
//		instrumentUsedTypes.add(InstrumentUsedType.find.findByCode("inst1"));
//
//		ExperimentType purif = ExperimentType.find.findByCode("purif1");
//		List<ExperimentType> purificationMethodTypes = new ArrayList<ExperimentType>();
//		purificationMethodTypes.add(purif);
//
//		ExperimentType qc = ExperimentType.find.findByCode("qc1");
//		List<ExperimentType> qualityControlTypes = new ArrayList<ExperimentType>();
//		qualityControlTypes.add(qc);
//
//		ExperimentCategory experimentCategory = ExperimentCategory.find.findByCode("expCat1");
//
//		ExperimentType experimentType = createExperimentType(commonInfoType, instrumentUsedTypes, experimentCategory);
//		experimentType.id = experimentType.save();
//		experimentType=ExperimentType.find.findById(experimentType.id);
//		checkExperimentType(experimentType);
//	}
//
//	/**
//	 * TEST PURIFICATION_METHOD_TYPE
//	 * @throws DAOException 
//	 */
//	//@Test
//	public void savePurificationMethodType() throws DAOException {
//		//Create commonInfoType
//		List<State> states = new ArrayList<State>();
//		states.add(State.find.findByCode("state2"));
//		ObjectType objectType = ObjectType.find.findByCode("Experiment");
//		MeasureCategory measureCategory = MeasureCategory.find.findByCode("cat2");
//		MeasureUnit measureValue = MeasureUnit.find.findByValue("value2");
//		List<Value> possibleValues = new ArrayList<Value>();
//		possibleValues.add(createValue("value8", "value8", true));
//		List<PropertyDefinition> propertiesDefinitions = new ArrayList<PropertyDefinition>();
//		propertiesDefinitions.add(createPropertyDefinition("prop4", "prop4", true, true, "default", "descProp1", "format1", 1, "in", "content", true, true, "type1", measureCategory, measureValue, measureValue, possibleValues));
//		CommonInfoType commonInfoType = createCommonInfoType("purif1", "purif1", "purif1", states, propertiesDefinitions, objectType);
//
//		//Create list instrument 
//		List<InstrumentUsedType> instrumentUsedTypes = new ArrayList<InstrumentUsedType>();
//		instrumentUsedTypes.add(InstrumentUsedType.find.findByCode("inst1"));
//
//		//List<ReagentCategory> reagentTypes = new ArrayList<ReagentCategory>();
//		//ReagentCategory reagentType = ReagentCategory.find.findByCode("reagent1");
//		//reagentTypes.add(reagentType);
//		//protocols.add(createProtocol("proto2","proto2", "path2", "V2", createProtocolCategory("protoCat3", "protoCat3"), reagentTypes));
//
//		ExperimentCategory experimentCategory = ExperimentCategory.find.findByCode("expCat1");
//		
//		
//		ExperimentType purificationMethodType = createExperimentType(commonInfoType, instrumentUsedTypes, experimentCategory);
//		purificationMethodType.id = purificationMethodType.save();
//		purificationMethodType = ExperimentType.find.findById(purificationMethodType.id);
//		checkAbstractExperiment(purificationMethodType);
//	}
//
//	private ExperimentType createExperimentType(CommonInfoType commonInfoType, List<InstrumentUsedType> instrumentUsedTypes,
//			ExperimentCategory experimentCategory) {
//		ExperimentType experimentType = new ExperimentType();
//		experimentType.setCommonInfoType(commonInfoType);
//		experimentType.instrumentUsedTypes=instrumentUsedTypes;
//		experimentType.category=experimentCategory;		
//		return experimentType;
//	}
//
//	private void checkExperimentType(ExperimentType experimentType) {
//		checkAbstractExperiment(experimentType);
//	}
//
//	/**
//	 * TEST PROCESS_CATEGORY
//	 * @throws DAOException 
//	 */
//	//@Test
//	public void saveProcessCategory() throws DAOException {
//		ProcessCategory processCategory = createProcessCategory("processCat1", "processCat1");
//		processCategory.id = processCategory.save();
//		processCategory = ProcessCategory.find.findById(processCategory.id);
//		checkAbstractCategory(processCategory);
//	}
//	
//	private ProcessCategory createProcessCategory(String code, String name) {
//		ProcessCategory processCategory = new ProcessCategory();
//		processCategory.code=code;
//		processCategory.name=name;
//		return processCategory;
//	}
//
//	/**
//	 * TEST PROCESS_TYPE
//	 * @throws DAOException 
//	 */
//	//@Test
//	public void saveProcessType() throws DAOException {
//		List<ProcessExperimentType> experimentTypes = new ArrayList<ProcessExperimentType>();
//		ProcessExperimentType expType = new ProcessExperimentType(ExperimentType.find.findByCode("exp1"),0);
//		experimentTypes.add(expType);
//		ProcessCategory processCategory = ProcessCategory.find.findByCode("processCat1");
//		//Create commonInfoType
//		List<State> states = new ArrayList<State>();
//		states.add(State.find.findByCode("state2"));
//		ObjectType objectType = ObjectType.find.findByCode("Process");
//		MeasureCategory measureCategory = MeasureCategory.find.findByCode("cat2");
//		MeasureUnit measureValue = MeasureUnit.find.findByValue("value2");
//		List<Value> possibleValues = new ArrayList<Value>();
//		possibleValues.add(createValue("value16","value16", true));
//		List<PropertyDefinition> propertiesDefinitions = new ArrayList<PropertyDefinition>();
//		propertiesDefinitions.add(createPropertyDefinition("prop12", "prop12", true, true, "default", "descProp1", "format1", 1, "in", "content", true, true, "type1", measureCategory, measureValue, measureValue, possibleValues));
//		CommonInfoType commonInfoType = createCommonInfoType("process1", "process1", "process1", states, propertiesDefinitions, objectType);
//		ProcessType processType = createProcessType(commonInfoType, experimentTypes, processCategory,expType.experimentType,expType.experimentType,expType.experimentType);
//		processType.id = processType.save();
//		processType = ProcessType.find.findById(processType.id);
//		checkProcessType(processType);
//	}
//	
//	private ProcessType createProcessType(CommonInfoType commonInfoType, List<ProcessExperimentType> experimentTypes, ProcessCategory processCategory, 
//			ExperimentType voidExpType, ExperimentType firstExpType, ExperimentType lastExpType) {
//		ProcessType processType = new ProcessType();
//		processType.setCommonInfoType(commonInfoType);
//		processType.experimentTypes=experimentTypes;
//		processType.category=processCategory;
//		processType.voidExperimentType=voidExpType;
//		processType.firstExperimentType=firstExpType;
//		processType.lastExperimentType=lastExpType;
//		return processType;
//	}
//
//	private void checkProcessType(ProcessType processType) {
//		Assert.assertNotNull(processType);
//		checkCommonInfoType(processType);
//		checkAbstractCategory(processType.category);
//		Assert.assertNotNull(processType.experimentTypes);
//		Assert.assertTrue(processType.experimentTypes.size()>0);
//		for(ProcessExperimentType experimentType : processType.experimentTypes){
//			checkExperimentType(experimentType.experimentType);
//		}
//		Assert.assertNotNull(processType.voidExperimentType.id);
//		Assert.assertNotNull(processType.firstExperimentType.id);
//		Assert.assertNotNull(processType.lastExperimentType.id);
//	}
//
//	/**
//	 * TEST PROJECT_CATEGORY
//	 * @throws DAOException 
//	 */
//	//@Test
//	public void saveProjectCategory() throws DAOException {
//		ProjectCategory projectCategory = createProjectCategory("projectCat1", "projectCat1");
//		projectCategory.id = projectCategory.save();
//		projectCategory = ProjectCategory.find.findById(projectCategory.id);
//		checkAbstractCategory(projectCategory);
//	}
//	
//	private ProjectCategory createProjectCategory(String code, String name) {
//		ProjectCategory projectCategory = new ProjectCategory();
//		projectCategory.code=code;
//		projectCategory.name=name;
//		return projectCategory;
//	}
//
//	/**
//	 * TEST PROJECT_TYPE
//	 * @throws DAOException 
//	 */
//	//@Test
//	public void saveProjectType() throws DAOException {
//		ProjectCategory projectCategory = ProjectCategory.find.findByCode("projectCat1");
//		//Create commonInfoType
//		List<State> states = new ArrayList<State>();
//		states.add(State.find.findByCode("state2"));
//		ObjectType objectType = ObjectType.find.findByCode("Project");
//		MeasureCategory measureCategory = MeasureCategory.find.findByCode("cat2");
//		MeasureUnit measureValue = MeasureUnit.find.findByValue("value2");
//		List<Value> possibleValues = new ArrayList<Value>();
//		possibleValues.add(createValue("value17","value17", true));
//		List<PropertyDefinition> propertiesDefinitions = new ArrayList<PropertyDefinition>();
//		propertiesDefinitions.add(createPropertyDefinition("prop13", "prop13", true, true, "default", "descProp1", "format1", 1, "in", "content", true, true, "type1", measureCategory, measureValue, measureValue, possibleValues));
//		CommonInfoType commonInfoType = createCommonInfoType("project1", "project1", "project1", states, propertiesDefinitions, objectType);
//		ProjectType projectType = createProjectType(commonInfoType, projectCategory);
//		projectType.id = projectType.save();
//		projectType = ProjectType.find.findById(projectType.id);
//		checkProjectType(projectType);
//	}
//	
//	private ProjectType createProjectType(CommonInfoType commonInfoType,  ProjectCategory projectCategory) {
//		ProjectType projectType = new ProjectType();
//		projectType.setCommonInfoType(commonInfoType);
//		projectType.category=projectCategory;
//		return projectType;
//	}
//
//	private void checkProjectType(ProjectType projectType) {
//		Assert.assertNotNull(projectType);
//		checkCommonInfoType(projectType);
//		checkAbstractCategory(projectType.category);
//	}
//
//	/**
//	 * TEST SAMPLE_CATEGORY
//	 * @throws DAOException 
//	 */
//	//@Test
//	public void saveSampleCategory() throws DAOException {
//		SampleCategory sampleCategory = createSampleCategory("sampleCat1", "sampleCat1");
//		sampleCategory.id = sampleCategory.save();
//		sampleCategory = SampleCategory.find.findById(sampleCategory.id);
//		checkAbstractCategory(sampleCategory);
//	}
//	
//	private SampleCategory createSampleCategory(String code, String name) {
//		SampleCategory sampleCategory = new SampleCategory();
//		sampleCategory.code=code;
//		sampleCategory.name=name;
//		return sampleCategory;
//	}
//
//	/**
//	 * TEST SAMPLE_TYPE
//	 * @throws DAOException 
//	 */
//	//@Test
//	public void saveSampleType() throws DAOException {
//		SampleCategory sampleCategory = SampleCategory.find.findByCode("sampleCat1");
//		//Create commonInfoType
//		List<State> states = new ArrayList<State>();
//		states.add(State.find.findByCode("state2"));
//		ObjectType objectType = ObjectType.find.findByCode("Project");
//		MeasureCategory measureCategory = MeasureCategory.find.findByCode("cat2");
//		MeasureUnit measureValue = MeasureUnit.find.findByValue("value2");
//		List<Value> possibleValues = new ArrayList<Value>();
//		possibleValues.add(createValue("value18","value18", true));
//		List<PropertyDefinition> propertiesDefinitions = new ArrayList<PropertyDefinition>();
//		propertiesDefinitions.add(createPropertyDefinition("prop14", "prop14", true, true, "default", "descProp1", "format1", 1, "in", "content", true, true, "type1", measureCategory, measureValue, measureValue, possibleValues));
//		CommonInfoType commonInfoType = createCommonInfoType("sample1", "sample1", "sample1", states, propertiesDefinitions, objectType);
//		SampleType sampleType = createSampleType(commonInfoType, sampleCategory);
//		sampleType.id = sampleType.save();
//		sampleType = SampleType.find.findById(sampleType.id);
//		checkSampleType(sampleType);
//	}
//	
//	private SampleType createSampleType(CommonInfoType commonInfoType,  SampleCategory sampleCategory) {
//		SampleType sampleType = new SampleType();
//		sampleType.setCommonInfoType(commonInfoType);
//		sampleType.category=sampleCategory;
//		return sampleType;
//	}
//
//	private void checkSampleType(SampleType sampleType) {
//		Assert.assertNotNull(sampleType);
//		checkCommonInfoType(sampleType);
//		checkAbstractCategory(sampleType.category);
//	}
//
//	//@Test
//	public void saveImportCategory() throws DAOException {
//		ImportCategory importCategory = createImportCategory("import1", "import1");
//		importCategory.id = importCategory.save();
//		importCategory = ImportCategory.find.findById(importCategory.id);
//		checkAbstractCategory(importCategory);
//	}
//
//	//@Test
//	public void saveImportType() throws DAOException {
//		ImportCategory importCategory = ImportCategory.find.findByCode("import1");
//		//Create commonInfoType
//		List<State> states = new ArrayList<State>();
//		states.add(State.find.findByCode("state2"));
//		ObjectType objectType = ObjectType.find.findByCode("Import");
//		MeasureCategory measureCategory = MeasureCategory.find.findByCode("cat2");
//		MeasureUnit measureValue = MeasureUnit.find.findByValue("value2");
//		List<Value> possibleValues = new ArrayList<Value>();
//		possibleValues.add(createValue("value19","value19", true));
//		List<PropertyDefinition> propertiesDefinitions = new ArrayList<PropertyDefinition>();
//		propertiesDefinitions.add(createPropertyDefinition("prop15", "prop15", true, true, "default", "descProp1", "format1", 1, "in", "content", true, true, "type1", measureCategory, measureValue, measureValue, possibleValues));
//		CommonInfoType commonInfoType = createCommonInfoType("import1", "import1", "import1", states, propertiesDefinitions, objectType);
//		ImportType importType = createImportType(commonInfoType, importCategory);
//		importType.id = importType.save();
//		importType = ImportType.find.findById(importType.id);
//		checkImportType(importType);
//	}
//	
//	private ImportType createImportType(CommonInfoType commonInfoType,  ImportCategory importCategory) {
//		ImportType importType = new ImportType();
//		importType.setCommonInfoType(commonInfoType);
//		importType.category=importCategory;
//		return importType;
//	}
//
//	private void checkImportType(ImportType importType) {
//		Assert.assertNotNull(importType);
//		checkCommonInfoType(importType);
//		checkAbstractCategory(importType.category);
//	}
//	
//	//@Test
//	public void saveContainerCategory() throws DAOException {
//		ContainerCategory containerCategory = createContainerCategory("container1", "container1");
//		containerCategory.id = containerCategory.save();
//		containerCategory = ContainerCategory.find.findById(containerCategory.id);
//		checkAbstractCategory(containerCategory);
//	}
//	
//	public ContainerCategory createContainerCategory(String name, String code) {
//		ContainerCategory containerCategory = new ContainerCategory();
//		containerCategory.name=name;
//		containerCategory.code=code;
//		return containerCategory;
//	}
//	
//	public ImportCategory createImportCategory(String name, String code) {
//		ImportCategory importCategory = new ImportCategory();
//		importCategory.name=name;
//		importCategory.code=code;
//		return importCategory;
//	}
//	
//}
