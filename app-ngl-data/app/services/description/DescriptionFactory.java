package services.description;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import models.laboratory.common.description.AbstractCategory;
import models.laboratory.common.description.Institute;
import models.laboratory.common.description.Level;
import models.laboratory.common.description.MeasureCategory;
import models.laboratory.common.description.MeasureUnit;
import models.laboratory.common.description.ObjectType;
import models.laboratory.common.description.PropertyDefinition;
import models.laboratory.common.description.State;
import models.laboratory.common.description.StateCategory;
import models.laboratory.common.description.StateHierarchy;
import models.laboratory.common.description.Value;
import models.laboratory.container.description.ContainerCategory;
import models.laboratory.container.description.ContainerSupportCategory;
import models.laboratory.experiment.description.ExperimentCategory;
import models.laboratory.experiment.description.ExperimentType;
import models.laboratory.instrument.description.Instrument;
import models.laboratory.instrument.description.InstrumentCategory;
import models.laboratory.instrument.description.InstrumentUsedType;
import models.laboratory.processes.description.ExperimentTypeNode;
import models.laboratory.processes.description.ProcessCategory;
import models.laboratory.processes.description.ProcessExperimentType;
import models.laboratory.processes.description.ProcessType;
import models.laboratory.project.description.ProjectCategory;
import models.laboratory.project.description.ProjectType;
import models.laboratory.resolutions.instance.ResolutionCategory;
import models.laboratory.run.description.AnalysisType;
import models.laboratory.run.description.ReadSetType;
import models.laboratory.run.description.RunCategory;
import models.laboratory.run.description.RunType;
import models.laboratory.run.description.TreatmentCategory;
import models.laboratory.run.description.TreatmentContext;
import models.laboratory.run.description.TreatmentType;
import models.laboratory.run.description.TreatmentTypeContext;
import models.laboratory.sample.description.ImportCategory;
import models.laboratory.sample.description.ImportType;
import models.laboratory.sample.description.SampleCategory;
import models.laboratory.sample.description.SampleType;
import models.utils.dao.DAOException;
import play.Logger;


public class DescriptionFactory {

	/*
	 * 
	 * @param classCat
	 * @param name
	 * @param code
	 * @return
	 */
	public static <T extends AbstractCategory<T>> T newSimpleCategory(Class<T> classCat, String name, String code){
		try {
			T cat = classCat.newInstance();
			cat.code = code;
			cat.name = name;
			return cat; 
		} catch (InstantiationException e) {
			Logger.error(e.getMessage(),e);
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			Logger.error(e.getMessage(),e);
			throw new RuntimeException(e);
		}
	}


	
	
	public static ResolutionCategory newResolutionCategory(String name, Short displayOrder) {		
		ResolutionCategory  rc = new ResolutionCategory();
		rc.name = name;
		rc.displayOrder = displayOrder;
		return rc; 
	}

	
	public static PropertyDefinition newPropertiesDefinition(String name, String code, List<Level> levels, Class<?> type, Boolean required, String requiredState,
			List<Value> values, String propertyValueType, Integer displayOrder, Boolean editable, String defaultValue, String displayFormat) {
		return newPropertiesDefinition(name, code, levels, type, required, requiredState, values, null, null, null, propertyValueType, displayOrder, editable, defaultValue, displayFormat);
	}
	
	
	public static PropertyDefinition newPropertiesDefinition(String name, String code, List<Level> levels, Class<?> type, Boolean required, String requiredState,
			List<Value> values, MeasureCategory measureCategory, MeasureUnit displayMeasureUnit, MeasureUnit saveMeasureUnit,
			String propertyValueType, Integer displayOrder, Boolean editable,String defaultValue, String displayFormat) {
		PropertyDefinition pd = new PropertyDefinition();		
		pd.name = name;
		pd.code = code;
		pd.active = true;
		pd.levels = levels;
		pd.valueType = type.getName();
		if(null == required)required=Boolean.FALSE;
		pd.required = required;
		pd.requiredState = requiredState;
		
		pd.choiceInList = Boolean.FALSE;
		if(values!=null){ 
			pd.choiceInList=Boolean.TRUE;
			pd.possibleValues = values;
		}
		pd.measureCategory = measureCategory;
		pd.displayMeasureValue = displayMeasureUnit;
		pd.saveMeasureValue = saveMeasureUnit;	
		pd.propertyValueType = propertyValueType;
		pd.displayOrder=displayOrder;
		if(null == editable)editable=Boolean.TRUE;
		pd.editable=editable;
		pd.defaultValue=defaultValue;
		pd.displayFormat = displayFormat;
		pd.description = null;
		return pd;
	}
	
	/*
	 * 
	 * @param name
	 * @param code
	 * @param levels
	 * @param type
	 * @param required
	 * @param propertyValueType TODO
	 * @return
	 * @throws DAOException
	 */
	// TODO: suggest fix
	// @Deprecated
	public static PropertyDefinition newPropertiesDefinition(String name, String code, List<Level> levels, Class<?> type, Boolean required, String propertyValueType) throws DAOException{
		PropertyDefinition pd = new PropertyDefinition();		
		pd.name = name;
		pd.code = code;
		pd.active = true;
		pd.levels = levels;
		pd.valueType = type.getName();
		pd.required = required;
		pd.choiceInList = false;
		pd.propertyValueType = propertyValueType;
		return pd;
	}
	
	/*
	 * 
	 * @param name
	 * @param code
	 * @param levels
	 * @param type
	 * @param required
	 * @param propertyValueType TODO
	 * @param displayOrder
	 * @return
	 * @throws DAOException
	 */
	// TODO: suggest a fix
	// @Deprecated
	public static PropertyDefinition newPropertiesDefinition(String name, String code, List<Level> levels, Class<?> type, Boolean required, String propertyValueType, int displayOrder) throws DAOException{
		PropertyDefinition pd = new PropertyDefinition();		
		pd.name = name;
		pd.code = code;
		pd.active = true;
		pd.levels = levels;
		pd.valueType = type.getName();
		pd.required = required;
		pd.choiceInList = false;
		pd.propertyValueType = propertyValueType;
		pd.displayOrder=displayOrder;
		return pd;
	}

	/*
	 * 
	 * @param name
	 * @param code
	 * @param levels
	 * @param type
	 * @param required
	 * @param propertyValueType TODO
	 * @param displayOrder
	 * @return
	 * @throws DAOException
	 */
	// TODO: suggest a fix
	// @Deprecated
	public static PropertyDefinition newPropertiesDefinition(String name, String code, List<Level> levels, Class<?> type, Boolean required, String propertyValueType, int displayOrder, String defaultValue) throws DAOException{
		PropertyDefinition pd = new PropertyDefinition();		
		pd.name = name;
		pd.code = code;
		pd.active = true;
		pd.levels = levels;
		pd.valueType = type.getName();
		pd.required = required;
		pd.choiceInList = false;
		pd.propertyValueType = propertyValueType;
		pd.defaultValue=defaultValue;
		pd.displayOrder=displayOrder;
		return pd;
	}
	
	/*
	 * 
	 * @param name
	 * @param code
	 * @param levels
	 * @param type
	 * @param required
	 * @param propertyValueType TODO
	 * @param displayOrder
	 * @return
	 * @throws DAOException
	 */
	// TODO: suggest a fix
	// @Deprecated
	public static PropertyDefinition newPropertiesDefinition(String name, String code, List<Level> levels, Class<?> type, Boolean required, String propertyValueType, String defaultValue) throws DAOException{
		PropertyDefinition pd = new PropertyDefinition();		
		pd.name = name;
		pd.code = code;
		pd.active = true;
		pd.levels = levels;
		pd.valueType = type.getName();
		pd.required = required;
		pd.choiceInList = false;
		pd.propertyValueType = propertyValueType;
		pd.defaultValue=defaultValue;
		return pd;
	}
	
	// TODO: suggest a fix
	// @Deprecated
	public static PropertyDefinition newPropertiesDefinition(String name, String code, List<Level> levels, Class<?> type, Boolean required, Boolean editable,  String propertyValueType, int displayOrder) throws DAOException{
		PropertyDefinition pd = new PropertyDefinition();		
		pd.name = name;
		pd.code = code;
		pd.active = true;
		pd.levels = levels;
		pd.editable = editable;
		pd.valueType = type.getName();
		pd.required = required;
		pd.choiceInList = false;
		pd.propertyValueType = propertyValueType;
		pd.displayOrder=displayOrder;
		return pd;
	}
	
	/*
	 * 
	 * @param name
	 * @param code
	 * @param levels
	 * @param type
	 * @param required
	 * @param values
	 * @return
	 * @throws DAOException
	 */
	// TODO: suggest a fix
	// @Deprecated
	public static PropertyDefinition newPropertiesDefinition(String name, String code, List<Level> levels, Class<?> type, Boolean required, List<Value> values, String defaultValue, String propertyValueType) {
		
		PropertyDefinition pd = new PropertyDefinition();		
		pd.name = name;
		pd.code = code;
		pd.active = true;
		pd.levels = levels;
		pd.valueType = type.getName();
		pd.required = required;
		pd.choiceInList = true;		
		pd.possibleValues = values;
		pd.defaultValue=defaultValue;
		pd.propertyValueType = propertyValueType;
		return pd;
	}

	
	/*
	 * 
	 * @param name
	 * @param code
	 * @param levels
	 * @param type
	 * @param required
	 * @param values
	 * @param displayOrder
	 * @return
	 * @throws DAOException
	 */
	// TODO: suggest a fix 
	// @Deprecated
	public static PropertyDefinition newPropertiesDefinition(String name, String code, List<Level> levels, Class<?> type, Boolean required, List<Value> values, String defaultValue, String propertyValueType, int displayOrder) {
		PropertyDefinition pd = new PropertyDefinition();		
		pd.name = name;
		pd.code = code;
		pd.active = true;
		pd.levels = levels;
		pd.valueType = type.getName();
		pd.required = required;
		pd.choiceInList = true;		
		pd.possibleValues = values;
		pd.defaultValue=defaultValue;
		pd.propertyValueType = propertyValueType;
		pd.displayOrder=displayOrder;
		return pd;
	}
	
	/*
	 * 
	 * @param name
	 * @param code
	 * @param levels
	 * @param type
	 * @param values
	 * @param propertyValueType TODO
	 * @return
	 * @throws DAOException
	 */
	// TODO: suggest a fix
	// @Deprecated
	public static PropertyDefinition newPropertiesDefinition(String name, String code, List<Level> levels, Class<?> type, Boolean required, List<Value> values, String propertyValueType) throws DAOException{
		PropertyDefinition pd = new PropertyDefinition();		
		pd.name = name;
		pd.code = code;
		pd.active = true;
		pd.levels = levels;
		pd.valueType = type.getName();
		pd.required = required;
		pd.choiceInList = true;		
		pd.possibleValues = values;
		pd.propertyValueType = propertyValueType;
		return pd;
	}
	
	/*
	 * 
	 * @param name
	 * @param code
	 * @param levels
	 * @param type
	 * @param values
	 * @param propertyValueType TODO
	 * @param displayOrder
	 * @return
	 * @throws DAOException
	 */
	// TODO: suggest fix
	// @Deprecated
	public static PropertyDefinition newPropertiesDefinition(String name, String code, List<Level> levels, Class<?> type, Boolean required, List<Value> values, String propertyValueType, int displayOrder) throws DAOException{
		PropertyDefinition pd = new PropertyDefinition();		
		pd.name = name;
		pd.code = code;
		pd.active = true;
		pd.levels = levels;
		pd.valueType = type.getName();
		pd.required = required;
		pd.choiceInList = true;		
		pd.possibleValues = values;
		pd.propertyValueType = propertyValueType;
		pd.displayOrder = displayOrder;
		return pd;
	}
	
	/*
	 * 
	 * @param name
	 * @param code
	 * @param levels
	 * @param type
	 * @param required
	 * @param measureCategory
	 * @param displayMeasureUnit
	 * @param saveMeasureUnit
	 * @param propertyValueType TODO
	 * @return
	 * @throws DAOException
	 */
	// TODO: suggest a fix
	// @Deprecated
	public static PropertyDefinition newPropertiesDefinition(String name, String code, List<Level> levels, Class<?> type, Boolean required,
			MeasureCategory measureCategory, MeasureUnit displayMeasureUnit, MeasureUnit saveMeasureUnit, String propertyValueType) throws DAOException{
		PropertyDefinition pd = new PropertyDefinition();		
		pd.name = name;
		pd.code = code;
		pd.active = true;
		pd.levels = levels;
		pd.valueType = type.getName();		
		pd.required = required;
		pd.choiceInList = false;	
		pd.measureCategory = measureCategory;
		pd.displayMeasureValue = displayMeasureUnit;
		pd.saveMeasureValue = saveMeasureUnit;
		pd.propertyValueType = propertyValueType;
		return pd;
	}

	
	/*
	 * 
	 * @param name
	 * @param code
	 * @param levels
	 * @param type
	 * @param required
	 * @param values
	 * @param measureCategory
	 * @param displayMeasureUnit
	 * @param saveMeasureUnit
	 * @param propertyValueType TODO
	 * @param displayOrder
	 * @return
	 * @throws DAOException
	 */
	// TODO: suggest fix
	// @Deprecated
	public static PropertyDefinition newPropertiesDefinition(String name, String code, List<Level> levels, Class<?> type, Boolean required, 
			List<Value> values, MeasureCategory measureCategory, MeasureUnit displayMeasureUnit, MeasureUnit saveMeasureUnit, String propertyValueType, int displayOrder) throws DAOException{
		PropertyDefinition pd = new PropertyDefinition();		
		pd.name = name;
		pd.code = code;
		pd.active = true;
		pd.levels = levels;
		pd.valueType = type.getName();
		pd.required = required;
		pd.choiceInList = false;
		if(values!=null){ 
			pd.choiceInList=true;
			pd.possibleValues = values;
		}
		pd.measureCategory = measureCategory;
		pd.displayMeasureValue = displayMeasureUnit;
		pd.saveMeasureValue = saveMeasureUnit;	
		pd.propertyValueType = propertyValueType;
		pd.displayOrder=displayOrder;
		return pd;
	}
	
	/*
	 * 
	 * @param name
	 * @param code
	 * @param levels
	 * @param type
	 * @param required
	 * @param values
	 * @param defaultValue
	 * @param measureCategory
	 * @param displayMeasureUnit
	 * @param saveMeasureUnit
	 * @param propertyValueType TODO
	 * @param displayOrder
	 * @return
	 * @throws DAOException
	 */
	// TODO: suggest fix
	// @Deprecated
	public static PropertyDefinition newPropertiesDefinition(String name, String code, List<Level> levels, Class<?> type, Boolean required, 
			List<Value> values, String defaultValue, MeasureCategory measureCategory, MeasureUnit displayMeasureUnit, MeasureUnit saveMeasureUnit, String propertyValueType, int displayOrder) throws DAOException{
		PropertyDefinition pd = new PropertyDefinition();		
		pd.name = name;
		pd.code = code;
		pd.active = true;
		pd.levels = levels;
		pd.valueType = type.getName();
		pd.required = required;
		pd.choiceInList = false;
		if(values!=null){ 
			pd.choiceInList=true;
			pd.possibleValues = values;
		}
		pd.defaultValue = defaultValue;
		pd.measureCategory = measureCategory;
		pd.displayMeasureValue = displayMeasureUnit;
		pd.saveMeasureValue = saveMeasureUnit;	
		pd.propertyValueType = propertyValueType;
		pd.displayOrder=displayOrder;
		return pd;
	}
	
	// TODO: propose fix
	// @Deprecated
	public static PropertyDefinition newPropertiesDefinition(String name, 
			                                                 String code, 
			                                                 List<Level> levels, 
			                                                 Class<?> type, 
			                                                 Boolean required, 
			                                                 List<Value> values, 
			                                                 MeasureCategory measureCategory, 
			                                                 MeasureUnit displayMeasureUnit, 
			                                                 MeasureUnit saveMeasureUnit, 
			                                                 String propertyValueType, 
			                                                 int displayOrder, 
			                                                 Boolean editable) throws DAOException{
		PropertyDefinition pd = new PropertyDefinition();		
		pd.name = name;
		pd.code = code;
		pd.active = true;
		pd.levels = levels;
		pd.valueType = type.getName();
		pd.required = required;
		pd.choiceInList = false;
		if (values!=null) { 
			pd.choiceInList=true;
			pd.possibleValues = values;
		}
		pd.measureCategory = measureCategory;
		pd.displayMeasureValue = displayMeasureUnit;
		pd.saveMeasureValue = saveMeasureUnit;	
		pd.propertyValueType = propertyValueType;
		pd.displayOrder=displayOrder;
		pd.editable=editable;
		return pd;
	}
	
	// TODO: suggest fix
	// @Deprecated
	public static PropertyDefinition newPropertiesDefinition(String name, String code, List<Level> levels, Class<?> type, Boolean required, 
			List<Value> values, MeasureCategory measureCategory, MeasureUnit displayMeasureUnit, MeasureUnit saveMeasureUnit,
			String propertyValueType, int displayOrder, Boolean editable,String defaultValue, String displayFormat) throws DAOException{
		PropertyDefinition pd = new PropertyDefinition();		
		pd.name = name;
		pd.code = code;
		pd.active = true;
		pd.levels = levels;
		pd.valueType = type.getName();
		pd.required = required;
		pd.choiceInList = false;
		if(values!=null){ 
			pd.choiceInList=true;
			pd.possibleValues = values;
		}
		pd.measureCategory = measureCategory;
		pd.displayMeasureValue = displayMeasureUnit;
		pd.saveMeasureValue = saveMeasureUnit;	
		pd.propertyValueType = propertyValueType;
		pd.displayOrder=displayOrder;
		pd.editable=editable;
		pd.defaultValue=defaultValue;
		pd.displayFormat = displayFormat;
		
		return pd;
	}
	
	/*
	 * 
	 * @param codes
	 * @return
	 */
	public static List<Value> newValues(String...codes) {
		List<Value> l = new ArrayList<>(codes.length);
		for(String v : codes){
			Value value = new Value();
			value.value = v;
			value.code = v;
			value.name = v;
			value.defaultValue = false;
			l.add(value);
		}
		return l;
	}
	
	
	public static Value newValue(String code, String name){
		return newValue(code, name, false);
	}
	
	public static Value newValue(String code, String name, boolean isDefault){
		Value value = new Value();
		value.value = code;
		value.code = code;
		value.name = name;
		value.defaultValue = isDefault;
		return value;
	}
	
	/*
	 * 
	 * @param defaultValue
	 * @param code
	 * @return
	 */
	public static List<Value> newValuesWithDefault(String defaultValue, String...code) {
		List<Value> l = new ArrayList<>(code.length);
		for(String v : code){
			Value value = new Value();
			value.value = v;
			value.code = v;
			value.name = v;
			if(v.equals(defaultValue)){
				value.defaultValue = true;
			}else{
				value.defaultValue = false;
			}
			l.add(value);
		}
		return l;
	}

	/*
	 * 
	 * @param name
	 * @param code
	 * @return
	 */
	public static Level newLevel(String name, String code) {
		Level l = new Level();
		l.code = code;
		l.name = name;
		return l;
	}
	
	/*
	 * 
	 * @param code
	 * @return
	 */
	public static ObjectType newDefaultObjectType(String code) {
		ObjectType l = new ObjectType();
		l.code = code;
		l.generic = false;

		return l;
	}
	
	/*
	 * 
	 * @param code
	 * @param generic
	 * @param states
	 * @return
	 */
	public static ObjectType setStatesToObjectType(String code, Boolean generic, List<State> states) {
		ObjectType l = new ObjectType();
		try {
			l = ObjectType.find.findByCode(code);
		} catch (DAOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		l.generic = generic;
		return l;
	}
	
//	/*
//	 * 
//	 * @param code
//	 * @param states
//	 * @return
//	 */
//	public static CommonInfoType setStatesToCommonInfoType(String code, List<State> states) {
//		CommonInfoType cit = new CommonInfoType();
//		try {
//			cit = CommonInfoType.find.findByCode(code);
//		} catch (DAOException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//		cit.states = states;
//		return cit;
//	}
	
	/*
	 * 
	 * @param code
	 * @param value
	 * @param defaultUnit
	 * @param category
	 * @return
	 */
	public static MeasureUnit newMeasureUnit(String code, String value,
			boolean defaultUnit, MeasureCategory category) {
		MeasureUnit measureUnit = new MeasureUnit();
		measureUnit.code = code;
		measureUnit.value = value;
		measureUnit.defaultUnit = defaultUnit;
		measureUnit.category = category;
		
		return measureUnit;
	}

	
	/*
	 * define institute
	 * @param name
	 * @param code
	 * @return
	 */
	public static Institute newInstitute(String name, String code) {
			Institute i = new Institute();
			i.code = code;
			i.name = name;
			return i;
		}
	
	/*
	 * 
	 * @param name
	 * @param code
	 * @param active
	 * @param position
	 * @return
	 */
	public static State newState(String name, String code, boolean active,	int position) {
		State s = new State();
		s.code = code;
		s.name = name;
		s.active =active;
		s.position = position;
		return s;
	}

	/*
	 * 
	 * @param name
	 * @param code
	 * @param active
	 * @param position
	 * @param categories
	 * @param objTypes
	 * @return
	 */
	public static State newState(String name, String code, boolean active,	int position, StateCategory category, List<ObjectType> objTypes, boolean display, String functionnalGroup) {
		State s = new State();
		s.code = code;
		s.name = name;
		s.active =active;
		s.position = position;
		s.category = category;
		s.objectTypes = objTypes; 
		s.display = display;
		s.functionnalGroup = functionnalGroup;
		return s;
	}
	
	
	public static StateHierarchy newStateHierarchy(String childCode, String parentCode, String objectTypeCode) {
		StateHierarchy sh = new StateHierarchy();
		sh.code = childCode + "_" + objectTypeCode.toUpperCase();
		sh.childStateCode = childCode;
		sh.parentStateCode = parentCode;
		sh.objectTypeCode = objectTypeCode;
		return sh;
	}

	/*
	 * 
	 * @param name
	 * @param code
	 * @param nbLine
	 * @param nbColumn
	 * @param nbUsableContainer
	 * @param containerCategory
	 * @return
	 */
	public static ContainerSupportCategory newContainerSupportCategory(String name, String code, int nbLine, int nbColumn, int nbUsableContainer, ContainerCategory containerCategory) {
		ContainerSupportCategory csc = DescriptionFactory.newSimpleCategory(ContainerSupportCategory.class, name, code);
		csc.nbLine = nbLine;
		csc.nbColumn = nbColumn;
		csc.nbUsableContainer = nbUsableContainer;	
		csc.containerCategory = containerCategory;
		return csc;
	}

	
	
	/*
	 * 
	 * @param name
	 * @param code
	 * @param category
	 * @param propertiesDefinitions
	 * @param protocols
	 * @param instrumentUsedTypes
	 * @param atomicTransfertMethod
	 * @param institutes
	 * @return
	 * @throws DAOException
	 */
	public static ExperimentType newExperimentType(String name, String code, String shortCode, Integer displayOrder, ExperimentCategory category, List<PropertyDefinition> propertiesDefinitions, List<InstrumentUsedType> instrumentUsedTypes,String atomicTransfertMethod, List<SampleType> sampleTypes, Boolean active, List<Institute> institutes) throws DAOException {
		ExperimentType et = new ExperimentType();
		et.code =code.toLowerCase();
		if(null != shortCode)et.shortCode = shortCode.toUpperCase();
		et.name =name;
		et.displayOrder = displayOrder;
		et.category = category;
		et.objectType = ObjectType.find.findByCode(ObjectType.CODE.Experiment.name());
		et.propertiesDefinitions = propertiesDefinitions;
		//et.protocols = protocols;
		et.instrumentUsedTypes = instrumentUsedTypes;
		et.states = State.find.findByObjectTypeCode(ObjectType.CODE.Experiment);
		//et.resolutions = models.laboratory.common.description.Resolution.find.findByObjectTypeCode(ObjectType.CODE.Experiment);
		et.atomicTransfertMethod=atomicTransfertMethod;		
		et.institutes = institutes;
		et.active = active;
		if(sampleTypes!=null && sampleTypes.size()!=0){
			et.newSample=true;
			et.sampleTypes=sampleTypes;
		}
		return et;
	}
	
	public static ExperimentType newExperimentType(String name, String code, String shortCode, Integer displayOrder, ExperimentCategory category, List<PropertyDefinition> propertiesDefinitions, List<InstrumentUsedType> instrumentUsedTypes,String atomicTransfertMethod, Boolean active, List<Institute> institutes) throws DAOException {
		return newExperimentType(name, code,  shortCode,  displayOrder,  category,  propertiesDefinitions,  instrumentUsedTypes, atomicTransfertMethod, null, active,institutes);	
	}
	
	public static ExperimentType newExperimentType(String name, String code, String shortCode, Integer displayOrder, ExperimentCategory category, List<PropertyDefinition> propertiesDefinitions, List<InstrumentUsedType> instrumentUsedTypes,String atomicTransfertMethod, List<Institute> institutes) throws DAOException {
		return newExperimentType(name, code, shortCode, displayOrder, category, propertiesDefinitions, instrumentUsedTypes, atomicTransfertMethod, true, institutes);
	}
	
	/*
	 * 
	 * @param code
	 * @param experimentType
	 * @param mandatoryPurif
	 * @param mandatoryQC
	 * @param mandatoryQC
	 * @param previousExp
	 * @param purifTypes
	 * @param qcTypes
	 * @param transfertTypes
	 * @return
	 */
	public static ExperimentTypeNode newExperimentTypeNode(String code, ExperimentType experimentType, boolean mandatoryPurif, boolean mandatoryQC,boolean mandatoryTransfert, List<ExperimentTypeNode> previousExp, List<ExperimentType> purifTypes, List<ExperimentType> qcTypes,List<ExperimentType> transfertTypes) {
		ExperimentTypeNode etn = new ExperimentTypeNode();
		etn.code = code;
		etn.experimentType = experimentType;
		etn.doPurification = (purifTypes != null && purifTypes.size() > 0)?true:false;
		etn.mandatoryPurification = mandatoryPurif;
		etn.doQualityControl = (qcTypes != null && qcTypes.size() > 0)?true:false;
		etn.mandatoryQualityControl = mandatoryQC;
		etn.doQualityControl = (qcTypes != null && qcTypes.size() > 0)?true:false;
		etn.mandatoryTransfert=mandatoryTransfert;
		etn.doTransfert = (transfertTypes != null && transfertTypes.size() > 0)?true:false;
		etn.possiblePurificationTypes = purifTypes;
		etn.possibleQualityControlTypes = qcTypes;
		etn.possibleTransferts=transfertTypes;
		etn.previousExperimentTypeNodes = previousExp;
		
		if(ExperimentCategory.CODE.voidprocess.name().equals(experimentType.category.code)){
			etn.doPurification = true;
			etn.doQualityControl = true;
			etn.doTransfert = true;
		}
		
		return etn;
	}
	
	/*
	 * 
	 * @param code
	 * @param experimentType
	 * @param mandatoryPurif
	 * @param mandatoryQC
	 * @param previousExp
	 * @param purifTypes
	 * @param qcTypes
	 * @return
	 */
	public static ExperimentTypeNode newExperimentTypeNode(String code, ExperimentType experimentType, boolean mandatoryPurif, boolean mandatoryQC, List<ExperimentTypeNode> previousExp, List<ExperimentType> purifTypes, List<ExperimentType> qcTypes) {
		return newExperimentTypeNode(code, experimentType, mandatoryPurif, mandatoryQC, false, previousExp, purifTypes, qcTypes, null);
	}

	/*
	 * 
	 * @param name
	 * @param code
	 * @return
	 */
	public static InstrumentCategory newInstrumentCategory(String name, String code) {
		InstrumentCategory ic = DescriptionFactory.newSimpleCategory(InstrumentCategory.class,name, code);
		return ic;
	}

	/*
	 * 
	 * @param name
	 * @param code
	 * @param category
	 * @param propertiesDefinitions
	 * @param instruments
	 * @param inContainerSupportCategories
	 * @param outContainerSupportCategories
	 * @param institutes
	 * @return
	 * @throws DAOException
	 */
	public static InstrumentUsedType newInstrumentUsedType(String name, String code, InstrumentCategory category, List<PropertyDefinition> propertiesDefinitions, List<Instrument> instruments,
			List<ContainerSupportCategory> inContainerSupportCategories, List<ContainerSupportCategory> outContainerSupportCategories, List<Institute> institutes) throws DAOException{
		InstrumentUsedType iut = new InstrumentUsedType();
		iut.code =code;
		iut.name =name;
		iut.category = category;
		iut.objectType = ObjectType.find.findByCode(ObjectType.CODE.Instrument.name());
		iut.propertiesDefinitions = propertiesDefinitions;
		iut.instruments = instruments;
		iut.inContainerSupportCategories = inContainerSupportCategories;
		iut.outContainerSupportCategories = outContainerSupportCategories;
		iut.institutes = institutes;
		iut.states = State.find.findByObjectTypeCode(ObjectType.CODE.Instrument);
		return iut; 
	}

	/*
	 * 
	 * @param name
	 * @param code
	 * @param category
	 * @param displayOrder TODO
	 * @param propertiesDefinitions
	 * @param experimentTypes
	 * @param firstExperimentType
	 * @param lastExperimentType
	 * @param voidExperimentType
	 * @param institutes
	 * @return
	 * @throws DAOException
	 */
	public static ProcessType newProcessType(String name, String code, ProcessCategory category, Integer displayOrder, 
			List<PropertyDefinition> propertiesDefinitions, List<ProcessExperimentType> experimentTypes, ExperimentType firstExperimentType, ExperimentType lastExperimentType, ExperimentType voidExperimentType, List<Institute> institutes) throws DAOException {
		
		return newProcessType(name, code, category, displayOrder, propertiesDefinitions, experimentTypes, firstExperimentType, lastExperimentType, voidExperimentType, institutes, true);
	}

	public static ProcessType newProcessType(String name, String code, ProcessCategory category, Integer displayOrder, 
			List<PropertyDefinition> propertiesDefinitions, List<ProcessExperimentType> experimentTypes, ExperimentType firstExperimentType, ExperimentType lastExperimentType, ExperimentType voidExperimentType, List<Institute> institutes, boolean isActive) throws DAOException {
		ProcessType pt = new ProcessType();
		pt.code =code.toLowerCase();
		pt.name =name;
		pt.category = category;
		pt.objectType = ObjectType.find.findByCode(ObjectType.CODE.Process.name());
		pt.propertiesDefinitions = propertiesDefinitions;
		pt.states = State.find.findByObjectTypeCode(ObjectType.CODE.Process);
		//pt.resolutions = models.laboratory.common.description.Resolution.find.findByObjectTypeCode(ObjectType.CODE.Process);
		pt.firstExperimentType = firstExperimentType;
		pt.lastExperimentType = lastExperimentType;
		pt.voidExperimentType = voidExperimentType;
		pt.experimentTypes = experimentTypes;
		pt.institutes = institutes;
		pt.displayOrder = displayOrder;
		pt.active=isActive;
		
		return pt;
	}
	
	/*
	 * 
	 * @param name
	 * @param code
	 * @param category
	 * @param propertiesDefinitions
	 * @param institutes
	 * @return
	 * @throws DAOException
	 */
	public static SampleType newSampleType(String name, String code, SampleCategory category, List<PropertyDefinition> propertiesDefinitions, List<Institute> institutes) throws DAOException{
		SampleType st = new SampleType();
		st.code = code;
		st.name = name;
		st.category = category;
		st.objectType = ObjectType.find.findByCode(ObjectType.CODE.Sample.name());
		st.propertiesDefinitions = propertiesDefinitions;
		st.institutes = institutes;
		st.states = State.find.findByObjectTypeCode(ObjectType.CODE.Sample); 
		return st;
	}
	
	/*
	 * 
	 * @param name
	 * @param code
	 * @param category
	 * @param propertiesDefinitions
	 * @param institutes
	 * @return
	 * @throws DAOException
	 */
	public static ImportType newImportType(String name, String code, ImportCategory category, List<PropertyDefinition> propertiesDefinitions, List<Institute> institutes) throws DAOException{
		ImportType it = new ImportType();
		it.code = code.toLowerCase();
		it.name = name;
		it.category = category;
		it.objectType = ObjectType.find.findByCode(ObjectType.CODE.Import.name());
		it.propertiesDefinitions = propertiesDefinitions;
		it.institutes = institutes;
		it.states = State.find.findByObjectTypeCode(ObjectType.CODE.Sample); 
		return it;
	}
	
	/*
	 * 
	 * @param name
	 * @param code
	 * @param category
	 * @param propertiesDefinitions
	 * @param institutes
	 * @return
	 * @throws DAOException
	 */
	public static ProjectType newProjectType(String name, String code, ProjectCategory category, List<PropertyDefinition> propertiesDefinitions, List<Institute> institutes) throws DAOException{
		ProjectType pt = new ProjectType();
		pt.code = code.toLowerCase();
		pt.name = name;
		pt.category = category;
		pt.objectType = ObjectType.find.findByCode(ObjectType.CODE.Project.name());
		pt.propertiesDefinitions = propertiesDefinitions;
		pt.institutes = institutes;
		pt.states = State.find.findByObjectTypeCode(ObjectType.CODE.Project);
		return pt;
	}
	
	/*
	 * 
	 * @param name
	 * @param code
	 * @param propertiesDefinitions
	 * @param valCriterias
	 * @param institutes
	 * @return
	 * @throws DAOException
	 */
	public static ReadSetType newReadSetType(String name, String code, List<PropertyDefinition> propertiesDefinitions, List<Institute> institutes) throws DAOException {
		ReadSetType rt = new ReadSetType();
		rt.code =code;
		rt.name =name;
		rt.objectType = ObjectType.find.findByCode(ObjectType.CODE.ReadSet.name());
		rt.propertiesDefinitions = propertiesDefinitions;
		rt.states = State.find.findByObjectTypeCode(ObjectType.CODE.ReadSet);
		rt.institutes = institutes; 
		//rt.criterias = valCriterias;
		return rt;
	}
	
	/*
	 * 
	 * @param name
	 * @param code
	 * @param propertiesDefinitions
	 * @param valCriterias
	 * @param institutes
	 * @return
	 * @throws DAOException
	 */
	public static AnalysisType newAnalysisType(String name, String code, List<PropertyDefinition> propertiesDefinitions, List<Institute> institutes) throws DAOException {
		AnalysisType rt = new AnalysisType();
		rt.code =code;
		rt.name =name;
		rt.objectType = ObjectType.find.findByCode(ObjectType.CODE.Analysis.name());
		rt.propertiesDefinitions = propertiesDefinitions;
		rt.states = State.find.findByObjectTypeCode(ObjectType.CODE.Analysis);
		rt.institutes = institutes; 
		//rt.resolutions = models.laboratory.common.description.Resolution.find.findByObjectTypeCode(ObjectType.CODE.Analysis);
		//rt.criterias = valCriterias;
		return rt;
	}
	
	/*
	 * 
	 * @param name
	 * @param code
	 * @param nbLanes
	 * @param category
	 * @param propertiesDefinitions
	 * @param valCriterias
	 * @param institutes
	 * @return
	 * @throws DAOException
	 */
	public static RunType newRunType(String name, String code, Integer nbLanes, RunCategory category, List<PropertyDefinition> propertiesDefinitions, List<Institute> institutes) throws DAOException {
		RunType rt = new RunType();
		rt.code = code;
		rt.name = name;
		rt.nbLanes = nbLanes;
		rt.category = category;
		rt.objectType = ObjectType.find.findByCode(ObjectType.CODE.Run.name());
		rt.propertiesDefinitions = propertiesDefinitions;
		rt.states = State.find.findByObjectTypeCode(ObjectType.CODE.Run);
		rt.institutes = institutes;
		//rt.resolutions = models.laboratory.common.description.Resolution.find.findByObjectTypeCode(ObjectType.CODE.Run);
		//rt.criterias = valCriterias;
		return rt;
	}
	
	/*
	 * 
	 * @param name
	 * @param code
	 * @return
	 * @throws DAOException
	 */
	public static TreatmentContext newTreatmentContext(String name, String code) throws DAOException {
		TreatmentContext tc = new TreatmentContext();
		tc.code = code.toLowerCase();
		tc.name = name;
		return tc;
	}
	
	/*
	 * 
	 * @param name
	 * @param code
	 * @param category
	 * @param names
	 * @param propertiesDefinitions
	 * @param contexts
	 * @param institutes
	 * @param displayOrders
	 * @return
	 * @throws DAOException
	 */
	public static TreatmentType newTreatmentType(String name, String code, TreatmentCategory category, String names, List<PropertyDefinition> propertiesDefinitions, List<TreatmentTypeContext>  contexts, List<Institute> institutes, String displayOrders) throws DAOException {
		TreatmentType tt = new TreatmentType();
		tt.code = code.toLowerCase();
		tt.name = name;
		tt.category = category;
		tt.objectType = ObjectType.find.findByCode(ObjectType.CODE.Treatment.name());
		tt.propertiesDefinitions = propertiesDefinitions; 
		tt.names = names;
		tt.contexts = contexts;
		tt.institutes = institutes;
		tt.displayOrders = displayOrders;
		return tt;
	}
	
	/*
	 * 
	 * @param codes
	 * @return
	 * @throws DAOException
	 */
	public static List<Institute> getInstitutes(Constants.CODE...codes) throws DAOException {
		List<Institute> institutes = new ArrayList<>();
		for(Constants.CODE code : codes){
			institutes.add(Institute.find.findByCode(code.name()));
		}
		return institutes;
	}
	
	/*
	 * 
	 * @param codes
	 * @return
	 * @throws DAOException
	 */
	public static List<State> getStates(String...codes) throws DAOException {
		List<State> states = new ArrayList<>();
		for(String code: codes){
			states.add(State.find.findByCode(code));
		}
		return states;
	}


	/*
	 * 
	 * @param codes
	 * @return
	 * @throws DAOException
	 */
	public static List<ObjectType> getObjectTypes(String...codes) throws DAOException {
		List<ObjectType> objectTypes = new ArrayList<>();
		for(String code: codes){
			objectTypes.add(ObjectType.find.findByCode(code));
		}
		return objectTypes;
	}
	
	
	public static HashMap<ObjectType, State> getHierarchies(String...hierarchies)  throws DAOException {
		HashMap<ObjectType, State> hm = new HashMap<>();
		for (String hierarchy: hierarchies) {
			String str[]=hierarchy.split(":");
			String objectTypeCode = str[0];
			String stateCode = str[1];
			hm.put(ObjectType.find.findByCode(objectTypeCode), State.find.findByCode(stateCode));
		}
		return hm;
		
	}
	

}
