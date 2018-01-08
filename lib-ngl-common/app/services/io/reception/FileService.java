package services.io.reception;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import play.Logger;
import models.laboratory.common.instance.property.PropertyFileValue;
import models.laboratory.container.description.ContainerSupportCategory;
import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.ContainerSupport;
import models.laboratory.container.instance.Content;
import models.laboratory.experiment.instance.OutputContainerUsed;
import models.laboratory.project.instance.Project;
import models.laboratory.reception.instance.AbstractFieldConfiguration;
import models.laboratory.reception.instance.DoubleExcelFieldConfiguration;
import models.laboratory.reception.instance.ExcelFieldConfiguration;
import models.laboratory.reception.instance.ObjectFieldConfiguration;
import models.laboratory.reception.instance.PropertiesFieldConfiguration;
import models.laboratory.reception.instance.PropertyValueFieldConfiguration;
import models.laboratory.reception.instance.ReceptionConfiguration;
import models.laboratory.reception.instance.TagExcelFieldConfiguration;
import models.laboratory.reception.instance.ReceptionConfiguration.Action;
import models.laboratory.sample.instance.Sample;
import models.utils.CodeHelper;
import models.utils.InstanceConstants;
import services.io.reception.mapping.ContainerMapping;
import services.io.reception.mapping.SampleMapping;
import services.io.reception.mapping.SupportMapping;
import validation.ContextValidation;
import fr.cea.ig.DBObject;
import fr.cea.ig.MongoDBDAO;

public abstract class FileService {

	protected Map<Integer, String> headerByIndex = new HashMap<Integer,String>();

	protected ReceptionConfiguration configuration;
	protected PropertyFileValue fileValue;
	protected ContextValidation contextValidation;

	private Map<String, Project> lastSampleCodeForProjects = new HashMap<String, Project>(0);


	private Map<String, Mapping<? extends DBObject>> mappings = new HashMap<String,Mapping<? extends DBObject>>();

	private Map<String, Map<String, DBObject>> objects = new HashMap<String,Map<String, DBObject>>();

	public Map<String, Map<String, DBObject>> getObjects() {
		return objects;
	}

	protected FileService(ReceptionConfiguration configuration,
			PropertyFileValue fileValue, ContextValidation contextValidation) {
		this.configuration = configuration;
		this.fileValue = fileValue;
		this.contextValidation = contextValidation;
		Set<String> objectTypes = configuration.configs.keySet();
		objectTypes.stream().forEach(s -> {
			objects.put(s, new TreeMap<String, DBObject>());
			mappings.put(s, mappingFactory(s));						
		});

	}

	private Mapping<? extends DBObject> mappingFactory(String objectType) {

		if(Mapping.Keys.sample.toString().equals(objectType)){
			return new SampleMapping(objects, configuration.configs.get(objectType), configuration.action, contextValidation);
		}else if(Mapping.Keys.support.toString().equals(objectType)){
			return new SupportMapping(objects, configuration.configs.get(objectType), configuration.action, contextValidation);
		}else if(Mapping.Keys.container.toString().equals(objectType)){
			return new ContainerMapping(objects, configuration.configs.get(objectType), configuration.action, contextValidation);
		}
		else{
			contextValidation.addErrors("Error", "Mapping : "+objectType);
			throw new UnsupportedOperationException("Mapping : "+objectType);
		}
	}

	/**
	 * analyse one line of the file
	 * @param rowMap
	 * @param contextValidation2 
	 */
	protected void treatLine(Map<Integer, String> rowMap) {

		Set<String> objectTypes = configuration.configs.keySet();
		Map<String, DBObject> objectInLine = new HashMap<String, DBObject>(0);
		objectTypes.stream().forEach(s -> {
			try {
				DBObject dbObject = mappings.get(s).convertToDBObject(rowMap);
				objectInLine.put(s, dbObject);

			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});

		consolidateCodesOnLineObject(objectInLine);			

		objectInLine.entrySet()
		.stream().forEach(entry -> {
			String s = entry.getKey();
			DBObject dbObject = entry.getValue();
			if (null != dbObject.code && !objects.get(s).containsKey(dbObject.code)){
				objects.get(s).put(dbObject.code, dbObject);
			}else if(null != dbObject.code){
				Logger.warn(s+" already load from another line");
			}else{
				throw new RuntimeException("no code found for "+s);
			}
		});		
	}


	private void consolidateCodesOnLineObject(Map<String, DBObject> objectInLine) {
		Container container = (Container)objectInLine.get(Mapping.Keys.container.toString());
		if(null != container){
			if(Action.save.equals(configuration.action)){
				//allready one sample by line
				Sample sample = (Sample)objectInLine.get(Mapping.Keys.sample.toString());
				if(null != sample && null == sample.code && sample.projectCodes != null && sample.projectCodes.size() == 1){
					sample.code = generateSampleCode(sample); 
					//update content sampleCode
					
					List<Content> contents = container.contents.stream().filter(c -> (c.sampleCode == null)).collect(Collectors.toList());
					if(contents.size() == 1){
						contents.get(0).sampleCode = sample.code;																
					}else if(contents.size() > 1){
						contextValidation.addErrors("container.contents", "several contents without sampleCode");
					}
					
						
				}else if(null != sample && null != sample.code){
					List<Content> contents = container.contents.stream().filter(c -> (c.sampleCode == null)).collect(Collectors.toList());
					if(contents.size() == 1){
						contents.get(0).sampleCode = sample.code;																
					}else if(contents.size() > 1){
						contextValidation.addErrors("container.contents", "several contents without sampleCode");
					}

				}else if(null != sample && null == sample.code && sample.projectCodes != null && sample.projectCodes.size() == 0){
					contextValidation.addErrors("sample.projectCodes", "no project code found for sample code generation");
				}

				ContainerSupport support = (ContainerSupport)objectInLine.get(Mapping.Keys.support.toString());
				if(null != support && null == support.code){
					support.code = CodeHelper.getInstance().generateContainerSupportCode(); 
					//update content sampleCode
					if(container.support.code == null){
						container.support.code = support.code;				
					}else{
						contextValidation.addErrors("container.support.code", "not null during support code generation : "+container.support.code);
					};			
				}
			}
			//compute container code from support code and line and column
			ContainerSupport support = (ContainerSupport)objectInLine.get(Mapping.Keys.support.toString());
			if(null != support){
				String containerCode = getContainerCode(support, container);
				if(null != containerCode && null == container.code){
					container.code = containerCode;
				}else if(!containerCode.equals(container.code)){
					contextValidation.addErrors("container.code", "error during container code generation : "+containerCode+" / "+container.code);
				}
			}
		}		
	}

	private String generateSampleCode(Sample sample) {
		String projectCode = sample.projectCodes.iterator().next();

		if(!lastSampleCodeForProjects.containsKey(projectCode)){
			Project project = MongoDBDAO.findByCode(InstanceConstants.PROJECT_COLL_NAME, Project.class, projectCode);
			lastSampleCodeForProjects.put(projectCode, project);
		}
		Project project = lastSampleCodeForProjects.get(projectCode);
		if(null != project){
			project.lastSampleCode = CodeHelper.getInstance().generateSampleCode(project, false);
			return project.lastSampleCode;
		}else{
			return null;
		}
	}
	/*
	 * compute container code with the support code in case of container.code is null
	 */
	private String getContainerCode(ContainerSupport support,
			Container container) {
		ContainerSupportCategory csc = ContainerSupportCategory.find.findByCode(container.support.categoryCode);
		String code = null;
		if(csc.nbLine == 1 && csc.nbColumn == 1){
			code= support.code;
		}else if(csc.nbLine > 1 && csc.nbColumn == 1){
			container.support.line = container.support.line.toUpperCase();
			code=support.code+"_"+container.support.line;

		}else if(csc.nbLine > 1 && csc.nbColumn > 1){
			container.support.line = container.support.line.toUpperCase();
			container.support.column = container.support.column.toUpperCase();
			
//			code=support.code+"_"+container.support.line+container.support.column;
			code=support.code+":"+container.support.line+container.support.column;
		}

		return code;
	}

	/**
	 * Consolidate the object obtain after file parsing
	 */
	protected void consolidateObjects() {
		//First consolidate container
		if(configuration.configs.containsKey(Mapping.Keys.container.toString())){
			Map<String, DBObject> containers = objects.get(Mapping.Keys.container.toString());
			containers.values().forEach(c -> {
				((ContainerMapping)mappings.get(Mapping.Keys.container.toString())).consolidate((Container)c);

			});
		}
		//Second consolidate support
		if(configuration.configs.containsKey(Mapping.Keys.support.toString())){
			Map<String, DBObject> supports = objects.get(Mapping.Keys.support.toString());
			supports.values().forEach(c -> {
				((SupportMapping)mappings.get(Mapping.Keys.support.toString())).consolidate((ContainerSupport)c);

			});
		}

		


	}
	/**
	 * Save or update objects in mongodb
	 */
	protected void saveObjects() {
		//First sampe if needed
		if(Action.save.equals(configuration.action)){
			contextValidation.setCreationMode();
		}else{
			contextValidation.setUpdateMode();
		}
		if(saveObjectsForKey(Mapping.Keys.sample.toString())){
			if(saveObjectsForKey(Mapping.Keys.support.toString())){
				saveObjectsForKey(Mapping.Keys.container.toString());		
				rollbackObjectIFNeeded(Mapping.Keys.sample.toString(),Mapping.Keys.support.toString());
			}else{
				rollbackObjectIFNeeded(Mapping.Keys.sample.toString()); //??? Good idea ???
			}			
		}

	}

	private void rollbackObjectIFNeeded(String...keys) {
		Arrays.asList(keys).forEach(key ->{
			if(contextValidation.hasErrors() && configuration.configs.containsKey(key)){
				Map<String, DBObject> dbobjects = objects.get(key);
				Mapping<? extends DBObject> mapping = mappings.get(key);
				dbobjects.values().forEach(o -> {
					mapping.rollbackInMongoDB(o);				
				});
			}
		});

	}

	/**
	 * Save objects only it not error
	 * @param key
	 */
	private boolean saveObjectsForKey(String key) {
		if(!contextValidation.hasErrors() && configuration.configs.containsKey(key)){
			Map<String, DBObject> dbobjects = objects.get(key);
			Mapping<? extends DBObject> mapping = mappings.get(key);

			contextValidation.addKeyToRootKeyName(key);
			dbobjects.values().forEach(o -> {
				mapping.validate(o);				
			});
			contextValidation.removeKeyFromRootKeyName(key);

			if(!contextValidation.hasErrors()){
				dbobjects.values().forEach(c -> {
					mapping.synchronizeMongoDB(c);					
				});
			}
		}
		return !contextValidation.hasErrors();
	}

	/**
	 * Update HeaderLabel in ExcelFieldConfiguration to have a good error message
	 */
	protected void updateHeaderConfiguration() {
		Set<String> objectTypes = configuration.configs.keySet();
		objectTypes.stream().forEach(s -> {
			Map<String, ? extends AbstractFieldConfiguration> fieldConfigurations = configuration.configs.get(s);
			Set<String> propertyNames = configuration.configs.get(s).keySet();
			propertyNames.stream().forEach(pName ->{
				updateAbstractFieldConfigurationHeader(fieldConfigurations.get(pName));
			});
		});
	}

	private void updateAbstractFieldConfigurationHeader(AbstractFieldConfiguration afc) {
		if(ExcelFieldConfiguration.class.isAssignableFrom(afc.getClass())){
			updateExcelConfigurationHeader((ExcelFieldConfiguration)afc);
		}else if(DoubleExcelFieldConfiguration.class.isAssignableFrom(afc.getClass())){
			updateDoubleExcelConfigurationHeader((DoubleExcelFieldConfiguration)afc);
		}else if(PropertiesFieldConfiguration.class.isAssignableFrom(afc.getClass())){
			PropertiesFieldConfiguration pfc = (PropertiesFieldConfiguration)afc;
			Set<String> propertyNames = pfc.configs.keySet();
			propertyNames.stream().forEach(_pName ->{
				updateAbstractFieldConfigurationHeader(pfc.configs.get(_pName));
			});
		}else if(PropertyValueFieldConfiguration.class.isAssignableFrom(afc.getClass())){
			PropertyValueFieldConfiguration pvfc = (PropertyValueFieldConfiguration)afc;
			updateAbstractFieldConfigurationHeader(pvfc.value);
			if(null != pvfc.unit)
				updateAbstractFieldConfigurationHeader(pvfc.unit);
		}else if(ObjectFieldConfiguration.class.isAssignableFrom(afc.getClass())){
			@SuppressWarnings("rawtypes")
			ObjectFieldConfiguration ofc = (ObjectFieldConfiguration)afc;
			Set<String> propertyNames = ofc.configs.keySet();
			propertyNames.stream().forEach(_pName ->{
				updateAbstractFieldConfigurationHeader((AbstractFieldConfiguration) ofc.configs.get(_pName));
			});
		}else if(TagExcelFieldConfiguration.class.isAssignableFrom(afc.getClass())){
			updateTagExcelConfigurationHeader((TagExcelFieldConfiguration)afc);
		}
	}

	//FDS 04/07/2017 remplacer cellCode par cellName
	private void updateTagExcelConfigurationHeader(TagExcelFieldConfiguration efc) {
		if(this.headerByIndex.containsKey(efc.cellSequence) && this.headerByIndex.containsKey(efc.cellName)){
			efc.headerValue = this.headerByIndex.get(efc.cellSequence)+" / "+this.headerByIndex.get(efc.cellName);
		}else if(this.headerByIndex.containsKey(efc.cellSequence)){
			efc.headerValue = this.headerByIndex.get(efc.cellSequence);
		}else{
			contextValidation.addErrors("Headers","not found header for cell position "+efc.cellSequence);
		}
	}
	
	private void updateExcelConfigurationHeader(ExcelFieldConfiguration efc) {
		if(this.headerByIndex.containsKey(efc.cellPosition)){
			efc.headerValue = this.headerByIndex.get(efc.cellPosition);
		}else{
			contextValidation.addErrors("Headers","not found header for cell position "+efc.cellPosition);
		}
	}

	private void updateDoubleExcelConfigurationHeader(DoubleExcelFieldConfiguration efc) {
		if(this.headerByIndex.containsKey(efc.cellPosition1)&&this.headerByIndex.containsKey(efc.cellPosition2)){
			efc.headerValue = this.headerByIndex.get(efc.cellPosition1)+" / "+this.headerByIndex.get(efc.cellPosition2);
		}else{
			contextValidation.addErrors("Headers","not found header for cell position "+efc.cellPosition1);
		}
	}

	
	
	public abstract void analyse();

}
