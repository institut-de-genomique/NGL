//package models.dao;
//
//import java.util.ArrayList;
//import java.util.List;
//import models.laboratory.common.description.AbstractCategory;
//import models.laboratory.common.description.CommonInfoType;
//import models.laboratory.common.description.Level;
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
////import models.laboratory.reagent.description.ReagentCatalog;
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
//public class UpdateDescriptionTest extends AbstractTests {
//		
//	//@Test
//	public void updateObjectType() throws DAOException {
//		ObjectType objectType = ObjectType.find.findByCode("Test");
//		objectType.code="UpdateTest";
//		objectType.update();
//		objectType =  ObjectType.find.findById(objectType.id);
//		Assert.assertTrue(objectType.code.equals("UpdateTest"));
//		checkObjectType(objectType);
//	}
//	
//	private void checkObjectType(ObjectType type) {
//		Assert.assertNotNull(type);
//		Assert.assertNotNull(type.id);
//		Assert.assertNotNull(type.code);
//		Assert.assertNotNull(type.generic);
//	}
//
//	//@Test
//	public void updateStateCategory() throws DAOException {
//		StateCategory stateCategory = StateCategory.find.findByCode("catState1");
//		checkAbstractCategory(stateCategory);
//		Assert.assertTrue(stateCategory.code.equals("catState1"));
//		stateCategory.name="updateCatStat1";
//		stateCategory.update();
//		stateCategory = StateCategory.find.findById(stateCategory.id);
//		checkAbstractCategory(stateCategory);
//		Assert.assertTrue(stateCategory.name.equals("updateCatStat1"));
//	}
//	
//	////////////@Test
//	public void updateState() throws DAOException {
//		State state = State.find.findByCode("state1");
//		checkState(state);
//		Assert.assertTrue(state.code.equals("state1"));
//		state.name="updateState1";
//		state.update();
//		state = State.find.findById(state.id);
//		checkState(state);
//		Assert.assertTrue(state.name.equals("updateState1"));
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
//	private void checkMeasureValue(MeasureUnit measureValue) {
//		Assert.assertNotNull(measureValue);
//		Assert.assertNotNull(measureValue.id);
//		Assert.assertNotNull(measureValue.code);
//		Assert.assertNotNull(measureValue.value);
//		Assert.assertNotNull(measureValue.defaultUnit);
//	}
//
//	//@Test
//	/*public void updateReagentType() throws DAOException
//	{
//		ReagentCategory reagentType = ReagentCategory.find.findByCode("reagent1");
//		reagentType.name="updateReagent1";
//		reagentType.update();
//		reagentType = ReagentCategory.find.findByCode("reagent1");
//		Assert.assertTrue(reagentType.name.equals("updateReagent1"));
//	}*/
//
//	private void checkCommonInfoType(CommonInfoType commonInfoType)	{
//		Assert.assertNotNull(commonInfoType);
//		Assert.assertNotNull(commonInfoType.id);
//		Assert.assertNotNull(commonInfoType.code);
//		Assert.assertNotNull(commonInfoType.name);
//		Assert.assertNotNull(commonInfoType.propertiesDefinitions);
//		Assert.assertTrue(commonInfoType.propertiesDefinitions.size()>0);
//		for(PropertyDefinition propertyDefinition : commonInfoType.propertiesDefinitions) {
//			checkPropertyDefinition(propertyDefinition);
//		}
//		Assert.assertNotNull(commonInfoType.objectType);
//		checkObjectType(commonInfoType.objectType);
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
//	private void checkValue(Value value) {
//		Assert.assertNotNull(value);
//		Assert.assertNotNull(value.code);
//		Assert.assertNotNull(value.value);
//		Assert.assertNotNull(value.defaultValue);
//	}
//
//	//@Test
//	public void updateProtocolCategory() throws DAOException {
//		ProtocolCategory protocolCategory = ProtocolCategory.find.findByCode("protoCat1");
//		protocolCategory.name="updateProtoCat1";
//		protocolCategory.update();
//		protocolCategory = ProtocolCategory.find.findByCode("protoCat1");
//		checkAbstractCategory(protocolCategory);
//		Assert.assertTrue(protocolCategory.name.equals("updateProtoCat1"));
//	}
//
//	private void checkAbstractCategory(AbstractCategory<?> abstractCategory) {
//		Assert.assertNotNull(abstractCategory);
//		Assert.assertNotNull(abstractCategory.id);
//		Assert.assertNotNull(abstractCategory.code);
//		Assert.assertNotNull(abstractCategory.name);
//	}
//
//	//@Test
//	public void updateContainerSupportCategory() throws DAOException {
//		ContainerSupportCategory containerSupportCategory = ContainerSupportCategory.find.findByCode("support1");
//		checkContainerSupportCategory(containerSupportCategory);
//		containerSupportCategory.name="updateSupport1";
//		containerSupportCategory.nbLine=5;
//		containerSupportCategory.update();
//		containerSupportCategory = ContainerSupportCategory.find.findByCode("support1");
//		Assert.assertTrue(containerSupportCategory.name.equals("updateSupport1"));
//		Assert.assertTrue(containerSupportCategory.nbLine==5);
//	}
//	
//	private void checkContainerSupportCategory(ContainerSupportCategory containerSupportCategory) {
//		checkAbstractCategory(containerSupportCategory);
//		Assert.assertNotNull(containerSupportCategory.nbLine);
//		Assert.assertNotNull(containerSupportCategory.nbColumn);
//		Assert.assertNotNull(containerSupportCategory.nbUsableContainer);
//	}
//
//	//@Test
//	public void updateInstrumentCategory() throws DAOException {
//		// InstrumentCategory instrumentCategory = 
//		InstrumentCategory.find.findByCode("InstCat1");
//		/*
//		checkInstrumentCategory(instrumentCategory);
//		instrumentCategory.name="UpdateInstCat1";
//		instrumentCategory.inContainerSupportCategories.add(createContainerSupportCategory("support3", "support3", 10, 10, 10));
//		instrumentCategory.outContainerSupportCategories.add(createContainerSupportCategory("support4", "support4", 10, 10, 10));
//		instrumentCategory.update();
//		instrumentCategory = InstrumentCategory.find.findById(instrumentCategory.id);
//		checkInstrumentCategory(instrumentCategory);
//		Assert.assertTrue(instrumentCategory.name.equals("UpdateInstCat1"));
//		Assert.assertTrue(instrumentCategory.inContainerSupportCategories.size()==2);
//		Assert.assertTrue(instrumentCategory.outContainerSupportCategories.size()==2);
//*/
//	}
//
//	private void checkInstrumentCategory(InstrumentCategory instrumentCategory)	{
//		checkAbstractCategory(instrumentCategory);
//		/*
//		Assert.assertNotNull(instrumentCategory.nbInContainerSupports);
//		Assert.assertNotNull(instrumentCategory.nbOutContainerSupports);
//		Assert.assertNotNull(instrumentCategory.inContainerSupportCategories);
//		Assert.assertTrue(instrumentCategory.inContainerSupportCategories.size()>0);
//		for(ContainerSupportCategory containerSupportCategory :instrumentCategory.inContainerSupportCategories){
//			checkContainerSupportCategory(containerSupportCategory);
//		}
//		Assert.assertNotNull(instrumentCategory.outContainerSupportCategories);
//		Assert.assertTrue(instrumentCategory.outContainerSupportCategories.size()>0);
//		for(ContainerSupportCategory containerSupportCategory :instrumentCategory.outContainerSupportCategories){
//			checkContainerSupportCategory(containerSupportCategory);
//		}
//		*/
//	}
//
//	//@Test
//	public void updateInstrumentUsedType() throws DAOException {
//		InstrumentUsedType instrumentUsedType = InstrumentUsedType.find.findByCode("inst1");
//		checkInstrumentUsedType(instrumentUsedType);
//		instrumentUsedType.name="updateInst1";
//		instrumentUsedType.instruments.add(createInstrument("inst2", "inst2"));
//		instrumentUsedType.update();
//		instrumentUsedType=InstrumentUsedType.find.findById(instrumentUsedType.id);
//		checkInstrumentUsedType(instrumentUsedType);
//		Assert.assertTrue(instrumentUsedType.name.equals("updateInst1"));
//		Assert.assertTrue(instrumentUsedType.instruments.size()==2);
//	}
//
//	private void checkInstrumentUsedType(InstrumentUsedType instrumentUsedType)	{
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
//	private void checkInstrument(Instrument instrument)	{
//		Assert.assertNotNull(instrument);
//		Assert.assertNotNull(instrument.id);
//		Assert.assertNotNull(instrument.code);
//		Assert.assertNotNull(instrument.name);
//	}
//	
//	private void checkAbstractExperiment(ExperimentType experiment)	{
//		Assert.assertNotNull(experiment);
//		checkCommonInfoType(experiment);
//		Assert.assertNotNull(experiment.instrumentUsedTypes);
//		Assert.assertTrue(experiment.instrumentUsedTypes.size()>0);
//		for(InstrumentUsedType instrumentUsedType : experiment.instrumentUsedTypes)
//			checkInstrumentUsedType(instrumentUsedType);
//	}
//
//	//@Test
//	public void updateExperimentCategory() throws DAOException {
//		ExperimentCategory experimentCategory = ExperimentCategory.find.findByCode("expCat1");
//		checkAbstractCategory(experimentCategory);
//		experimentCategory.name="updateExpCat1";
//		experimentCategory.update();
//		experimentCategory = ExperimentCategory.find.findById(experimentCategory.id);
//		checkAbstractCategory(experimentCategory);
//		Assert.assertTrue(experimentCategory.name.equals("updateExpCat1"));
//	}
//	
//	//@Test
//	public void updateQualityControlType() throws DAOException {
//		ExperimentType qualityControlType = ExperimentType.find.findByCode("qc1");
//		checkAbstractExperiment(qualityControlType);
//		qualityControlType.name="updateQC1";
//
//		//Add experiment type
//		//Create commonInfoType
//		List<State> states = new ArrayList<State>();
//		states.add(State.find.findByCode("state1"));
//		ObjectType objectType = ObjectType.find.findByCode("Instrument");
//		MeasureCategory measureCategory = MeasureCategory.find.findByCode("cat2");
//		MeasureUnit measureValue = MeasureUnit.find.findByValue("value2");
//		List<Value> possibleValues = new ArrayList<Value>();
//		possibleValues.add(createValue("value11","value11", true));
//		List<PropertyDefinition> propertiesDefinitions = new ArrayList<PropertyDefinition>();
//		propertiesDefinitions.add(createPropertyDefinition("prop7", "prop7", true, true, "default", "descProp1", "format1", 1, "in", Level.CODE.Content, true, true, "type1", measureCategory, measureValue, measureValue, possibleValues));
//		CommonInfoType commonInfoType = createCommonInfoType("inst4", "inst4", "inst4", states, propertiesDefinitions, objectType);
//
//		//Get instrumentCategory
//		InstrumentCategory instrumentCategory = InstrumentCategory.find.findByCode("InstCat1");
//
//		//Get instrument
//		List<Instrument> instruments = new ArrayList<Instrument>();
//		instruments.add(createInstrument("inst4", "inst4"));
//
//		qualityControlType.instrumentUsedTypes.add(createInstrumentUsedType(commonInfoType, instrumentCategory, instruments));
//		//List<ReagentCategory> reagentTypes = new ArrayList<ReagentCategory>();
//		//ReagentCategory reagentType = ReagentCategory.find.findByCode("reagent1");
//		//reagentTypes.add(reagentType);
//		//qualityControlType.protocols.add(createProtocol("proto4","proto4", "path4", "V2", ProtocolCategory.find.findByCode("protoCat2"), reagentTypes));
//
//		qualityControlType.update();
//		qualityControlType = ExperimentType.find.findById(qualityControlType.id);
//		checkAbstractExperiment(qualityControlType);
//		Assert.assertTrue(qualityControlType.name.equals("updateQC1"));
//		Assert.assertTrue(qualityControlType.instrumentUsedTypes.size()==2);
//	}
//
//	//@Test
//	public void updateExperimentType() throws DAOException {
//		ExperimentType experimentType = ExperimentType.find.findByCode("exp1");
//		checkExperimentType(experimentType);
//		experimentType.name="updateExp1";
//		//Create purification
//		//Create commonInfoType
//		List<State> states = new ArrayList<State>();
//		states.add(State.find.findByCode("state2"));
//		ObjectType objectType = ObjectType.find.findByCode("Experiment");
//		MeasureCategory measureCategory = MeasureCategory.find.findByCode("cat2");
//		MeasureUnit measureValue = MeasureUnit.find.findByValue("value2");
//		List<Value> possibleValues = new ArrayList<Value>();
//		possibleValues.add(createValue("value13","value13", true));
//		List<PropertyDefinition> propertiesDefinitions = new ArrayList<PropertyDefinition>();
//		propertiesDefinitions.add(createPropertyDefinition("prop9", "prop9", true, true, "default", "descProp1", "format1", 1, "in", Level.CODE.Content, true, true, "type1", measureCategory, measureValue, measureValue, possibleValues));
//		// CommonInfoType commonInfoType =	
//		createCommonInfoType("purif2", "purif2", "purif2", states, propertiesDefinitions, objectType);
//
//		//Create list instrument 
//		List<InstrumentUsedType> instrumentUsedTypes = new ArrayList<InstrumentUsedType>();
//		instrumentUsedTypes.add(InstrumentUsedType.find.findByCode("inst1"));
//
//		//Create commonInfoType
//		states = new ArrayList<State>();
//		states.add(State.find.findByCode("state2"));
//		objectType = ObjectType.find.findByCode("Experiment");
//
//		possibleValues = new ArrayList<Value>();
//		possibleValues.add(createValue("value14","value14", true));
//		propertiesDefinitions = new ArrayList<PropertyDefinition>();
//		propertiesDefinitions.add(createPropertyDefinition("prop10", "prop10", true, true, "default", "descProp1", "format1", 1, "in", Level.CODE.Content, true, true, "type1", measureCategory, measureValue, measureValue, possibleValues));
//		//commonInfoType = 
//		createCommonInfoType("qc2", "qc2", "qc2", states, propertiesDefinitions, objectType);
//
//		//Create list instrument 
//		instrumentUsedTypes = new ArrayList<InstrumentUsedType>();
//		instrumentUsedTypes.add(InstrumentUsedType.find.findByCode("inst1"));
//
//		//Create liste protocol
//		experimentType.update();
//		experimentType=ExperimentType.find.findById(experimentType.id);
//		checkExperimentType(experimentType);
//		Assert.assertTrue(experimentType.name.equals("updateExp1"));		
//
//	}
//
//	//@Test
//	public void updatePurificationMethodType() throws DAOException {
//		ExperimentType purificationMethodType = ExperimentType.find.findByCode("purif1");
//		checkAbstractExperiment(purificationMethodType);
//		purificationMethodType.name="updatePurif1";
//
//		//Add experiment type
//		//Create commonInfoType
//		List<State> states = new ArrayList<State>();
//		states.add(State.find.findByCode("state1"));
//		ObjectType objectType = ObjectType.find.findByCode("Instrument");
//		MeasureCategory measureCategory = MeasureCategory.find.findByCode("cat2");
//		MeasureUnit measureValue = MeasureUnit.find.findByValue("value2");
//		List<Value> possibleValues = new ArrayList<Value>();
//		possibleValues.add(createValue("value9", "value9", true));
//		List<PropertyDefinition> propertiesDefinitions = new ArrayList<PropertyDefinition>();
//		propertiesDefinitions.add(createPropertyDefinition("prop5", "prop5", true, true, "default", "descProp1", "format1", 1, "in", Level.CODE.Content, true, true, "type1", measureCategory, measureValue, measureValue, possibleValues));
//		CommonInfoType commonInfoType = createCommonInfoType("inst3", "inst3", "inst3", states, propertiesDefinitions, objectType);
//
//		InstrumentCategory instrumentCategory = InstrumentCategory.find.findByCode("InstCat1");
//		//Get instrument
//		List<Instrument> instruments = new ArrayList<Instrument>();
//		instruments.add(createInstrument("inst3", "inst3"));
//
//
//		purificationMethodType.instrumentUsedTypes.add(createInstrumentUsedType(commonInfoType, instrumentCategory, instruments));
//		// List<ReagentCatalog> reagentTypes = new ArrayList<ReagentCatalog>();
//		//ReagentCategory reagentType = ReagentCategory.find.findByCode("reagent1");
//		//reagentTypes.add(reagentType);
//
//	
//		purificationMethodType.update();
//		purificationMethodType = ExperimentType.find.findById(purificationMethodType.id);
//		checkAbstractExperiment(purificationMethodType);
//		Assert.assertTrue(purificationMethodType.name.equals("updatePurif1"));
//		Assert.assertTrue(purificationMethodType.instrumentUsedTypes.size()==2);
//	}
//
//	private void checkExperimentType(ExperimentType experimentType) {
//		checkAbstractExperiment(experimentType);
//	}
//
//	//@Test
//	public void updateProcessCategory() throws DAOException	{
//		ProcessCategory processCategory = ProcessCategory.find.findByCode("processCat1");
//		checkAbstractCategory(processCategory);
//		processCategory.name="updateProcessCat1";
//		processCategory.update();
//		processCategory = ProcessCategory.find.findById(processCategory.id);
//		checkAbstractCategory(processCategory);
//		Assert.assertTrue(processCategory.name.equals("updateProcessCat1"));
//	}
//
//	//@Test
//	public void updateProcessType() throws DAOException	{
//		ProcessType processType = ProcessType.find.findByCode("process1");
//		checkProcessType(processType);
//		processType.name="updateProcess1";		
//		processType.update();
//		processType = ProcessType.find.findById(processType.id);
//		checkProcessType(processType);
//		Assert.assertTrue(processType.name.equals("updateProcess1"));
//		Assert.assertTrue(processType.experimentTypes.size()==1);
//
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
//	//@Test
//	public void updateProjectCategory() throws DAOException	{
//		ProjectCategory projectCategory = ProjectCategory.find.findByCode("projectCat1");
//		checkAbstractCategory(projectCategory);
//		projectCategory.name="updateProjectCat1";
//		projectCategory.update();
//		projectCategory = ProjectCategory.find.findById(projectCategory.id);
//		checkAbstractCategory(projectCategory);
//		Assert.assertTrue(projectCategory.name.equals("updateProjectCat1"));
//	}
//
//	//@Test
//	public void updateProjectType() throws DAOException	{
//		ProjectType projectType = ProjectType.find.findByCode("project1");
//		checkProjectType(projectType);
//		projectType.name="updateProject1";
//		projectType.update();
//		projectType = ProjectType.find.findById(projectType.id);
//		checkProjectType(projectType);
//		Assert.assertTrue(projectType.name.equals("updateProject1"));
//	}
//
//	private void checkProjectType(ProjectType projectType) {
//		Assert.assertNotNull(projectType);
//		checkCommonInfoType(projectType);
//		checkAbstractCategory(projectType.category);
//	}
//
//	//@Test
//	public void updateSampleCategory() throws DAOException {
//		SampleCategory sampleCategory = SampleCategory.find.findByCode("sampleCat1");
//		checkAbstractCategory(sampleCategory);
//		sampleCategory.name="updateSampleCat1";
//		sampleCategory.update();
//		sampleCategory = SampleCategory.find.findById(sampleCategory.id);
//		checkAbstractCategory(sampleCategory);
//		Assert.assertTrue(sampleCategory.name.equals("updateSampleCat1"));
//	}
//
//	//@Test
//	public void updateSampleType() throws DAOException {
//		SampleType sampleType = SampleType.find.findByCode("sample1");
//		checkSampleType(sampleType);
//		sampleType.name="updateSample1";
//		sampleType.update();
//		sampleType = SampleType.find.findById(sampleType.id);
//		checkSampleType(sampleType);
//		Assert.assertTrue(sampleType.name.equals("updateSample1"));
//	}
//
//	private void checkSampleType(SampleType sampleType) {
//		Assert.assertNotNull(sampleType);
//		checkCommonInfoType(sampleType);
//		checkAbstractCategory(sampleType.category);
//	}
//
//	//@Test
//	public void updateImportCategory() throws DAOException {
//		ImportCategory importCategory = ImportCategory.find.findByCode("import1");
//		checkAbstractCategory(importCategory);
//		importCategory.name="updateImport1";
//		importCategory.update();
//		importCategory = ImportCategory.find.findById(importCategory.id);
//		checkAbstractCategory(importCategory);
//		Assert.assertTrue(importCategory.name.equals("updateImport1"));
//	}
//
//	//@Test
//	public void updateImportType() throws DAOException {
//		ImportType importType = ImportType.find.findByCode("import1");
//		checkImportType(importType);
//		importType.name="updateImport1";
//		importType.update();
//		importType = ImportType.find.findById(importType.id);
//		checkImportType(importType);
//		Assert.assertTrue(importType.name.equals("updateImport1"));
//	}
//	
//	private void checkImportType(ImportType importType)	{
//		Assert.assertNotNull(importType);
//		checkCommonInfoType(importType);
//		checkAbstractCategory(importType.category);
//	}
//	
//	//@Test
//	public void updateContainerCategory() throws DAOException {
//		ContainerCategory containerCategory = ContainerCategory.find.findByCode("container1");
//		checkAbstractCategory(containerCategory);
//		containerCategory.name="updateContainer1";
//		containerCategory.update();
//		containerCategory = ContainerCategory.find.findById(containerCategory.id);
//		checkAbstractCategory(containerCategory);
//		Assert.assertTrue(containerCategory.name.equals("updateContainer1"));
//	}
//
//	private PropertyDefinition createPropertyDefinition(String code, String name, boolean active, boolean choiceInList, String defaultValue, String description, String displayFormat, Integer displayOrder, String inOut, Level.CODE level, boolean propagation, boolean required, String type,
//			MeasureCategory measureCategory,MeasureUnit measureValue, MeasureUnit displayMeasureValue, List<Value> possibleValues) throws DAOException {
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
//		propertyDefinition.levels=getLevels(level);
//		propertyDefinition.required=required;
//		propertyDefinition.valueType=type;
//		propertyDefinition.measureCategory=measureCategory;
//		propertyDefinition.saveMeasureValue=measureValue;
//		propertyDefinition.displayMeasureValue = displayMeasureValue;
//		propertyDefinition.possibleValues=possibleValues;
//		return propertyDefinition;
//	}
//
//	public static List<Level> getLevels(Level.CODE...codes) throws DAOException {
//		List<Level> levels = new ArrayList<Level>();
//		for(Level.CODE code: codes){
//			levels.add(Level.find.findByCode(code.name()));
//		}
//		return levels;
//	}
//
//	private CommonInfoType createCommonInfoType(String code, String name, String collectionName, 
//			List<State> variableStates,List<PropertyDefinition> propertiesDefinitions, ObjectType objectType) {
//		CommonInfoType commonInfoType=new CommonInfoType();
//		commonInfoType.code=code;
//		commonInfoType.name=name;
//		commonInfoType.propertiesDefinitions=propertiesDefinitions;
//		commonInfoType.objectType=objectType;
//		return commonInfoType;
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
//	/*
//	private ContainerSupportCategory createContainerSupportCategory(String code, String name, int nbLine, int nbColumn, int nbUsableContainer) {
//		ContainerSupportCategory containerSupportCategory = new ContainerSupportCategory();
//		containerSupportCategory.code=code;
//		containerSupportCategory.name=name;
//		containerSupportCategory.nbLine=nbLine;
//		containerSupportCategory.nbColumn=nbColumn;
//		containerSupportCategory.nbUsableContainer=nbUsableContainer;
//		return containerSupportCategory;
//	}
//	*/
//	
//	private Instrument createInstrument(String code, String name) {
//		Instrument instrument = new Instrument();
//		instrument.code=code;
//		instrument.name=name;
//		return instrument;
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
//	/*
//	private ProtocolCategory createProtocolCategory(String code, String name) {
//		ProtocolCategory protocolCategory = new ProtocolCategory();
//		protocolCategory.code=code;
//		protocolCategory.name=name;
//		return protocolCategory;
//	}
//	*/
//	
//}
