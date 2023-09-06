package services.io.reception;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import fr.cea.ig.DBObject;
import models.laboratory.common.instance.property.PropertyFileValue;
import models.laboratory.container.description.ContainerSupportCategory;
import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.ContainerSupport;
import models.laboratory.container.instance.Content;
import models.laboratory.reception.instance.AbstractFieldConfiguration;
import models.laboratory.reception.instance.DoubleExcelFieldConfiguration;
import models.laboratory.reception.instance.ExcelFieldConfiguration;
import models.laboratory.reception.instance.ObjectFieldConfiguration;
import models.laboratory.reception.instance.PropertiesFieldConfiguration;
import models.laboratory.reception.instance.PropertyValueFieldConfiguration;
import models.laboratory.reception.instance.ReceptionConfiguration;
import models.laboratory.reception.instance.ReceptionConfiguration.Action;
import models.laboratory.reception.instance.TagExcelFieldConfiguration;
import models.laboratory.sample.instance.Sample;
import models.utils.CodeHelper;
import services.io.reception.mapping.ContainerMapping;
import services.io.reception.mapping.SampleMapping;
import services.io.reception.mapping.SupportMapping;
import validation.ContextValidation;
import validation.container.instance.ContainerSupportValidationHelper;

/**
 * Kind of file processor ({@link PropertyFileValue} processor).
 * The processor interprets the file content according to a
 * {@link ReceptionConfiguration}.
 * <p>
 * The processor stores the evaluation context.  
 */
public abstract class FileService {

	private static final play.Logger.ALogger logger = play.Logger.of(FileService.class);
	
	// Input 
	protected ReceptionConfiguration                   configuration;
	protected PropertyFileValue                        fileValue;
	protected ContextValidation                        contextValidation;
	// Internal
	private   Map<String, Mapping<? extends DBObject>> mappings           = new HashMap<>();
	// Built data
	protected Map<Integer, String>                     headerByIndex      = new HashMap<>();
	private   Map<String, Map<String, DBObject>>       objects            = new HashMap<>();
	
	protected FileService(ReceptionConfiguration configuration,
                          PropertyFileValue      fileValue, 
                          ContextValidation      contextValidation) {
		this.configuration     = configuration;
		this.fileValue         = fileValue;
		this.contextValidation = contextValidation;
//		configuration.configs.keySet()
//			.stream()
//			.forEach(s -> {
//				objects.put(s, new TreeMap<String, DBObject>());
//				mappings.put(s, mappingFactory(s));						
//			});
		for (String s : configuration.configs.keySet()) {
			objects.put(s, new TreeMap<String, DBObject>());
			mappings.put(s, mappingFactory(s));						
		}
	}

	public Map<String, Map<String, DBObject>> getObjects() {
		return objects;
	}

//	private Mapping<? extends DBObject> mappingFactory(String objectType) {
//		if (Mapping.Keys.sample.toString().equals(objectType)) {
//			return new SampleMapping(objects, configuration.configs.get(objectType), configuration.action, contextValidation);
//		} else if(Mapping.Keys.support.toString().equals(objectType)) {
//			return new SupportMapping(objects, configuration.configs.get(objectType), configuration.action, contextValidation);
//		} else if(Mapping.Keys.container.toString().equals(objectType)) {
//			return new ContainerMapping(objects, configuration.configs.get(objectType), configuration.action, contextValidation);
//		} else {
//			contextValidation.addError("Error", "Mapping : " + objectType);
//			throw new UnsupportedOperationException("Mapping : " + objectType);
//		}
//	}
	private Mapping<? extends DBObject> mappingFactory(String objectType) {
		switch (Mapping.Keys.valueOf(objectType)) {
		case sample    : return new SampleMapping   (objects, configuration.configs.get(objectType), configuration.action, contextValidation);
		case support   : return new SupportMapping  (objects, configuration.configs.get(objectType), configuration.action, contextValidation);
		case container : return new ContainerMapping(objects, configuration.configs.get(objectType), configuration.action, contextValidation);
		default        :
			contextValidation.addError("Error", "Mapping : " + objectType);
			throw new UnsupportedOperationException("Mapping : " + objectType);
		}
	}

	/*
	 * analyse one line of the file
	 * @param rowMap
	 * @param contextValidation2 
	 */
//	protected void treatLine(Map<Integer, String> rowMap) {
//
//		Set<String> objectTypes = configuration.configs.keySet();
//		Map<String, DBObject> objectInLine = new HashMap<>(0);
//		objectTypes.stream().forEach(s -> {
//			try {
//				DBObject dbObject = mappings.get(s).convertToDBObject(rowMap);
//				objectInLine.put(s, dbObject);
//			} catch (Exception e) {
//				throw new RuntimeException(e);
//			}
//		});
//
//		consolidateCodesOnLineObject(objectInLine);			
//
//		objectInLine.entrySet()
//		.stream().forEach(entry -> {
//			String s = entry.getKey();
//			DBObject dbObject = entry.getValue();
//			if (dbObject.code != null && !objects.get(s).containsKey(dbObject.code)) {
//				objects.get(s).put(dbObject.code, dbObject);
//			} else if (dbObject.code != null) {
//				logger.warn(s+" already load from another line");
//			} else {
//				throw new RuntimeException("no code found for "+s);
//			}
//		});		
//	}
	protected void treatLine(Map<Integer, String> rowMap) throws Exception {

		Set<String> objectTypes = configuration.configs.keySet();
		Map<String, DBObject> objectInLine = new HashMap<>(0);
		for (String s : objectTypes) 
			objectInLine.put(s, mappings.get(s).convertToDBObject(rowMap));
		
		consolidateCodesOnLineObject(objectInLine);			

		objectInLine.entrySet()
		.stream().forEach(entry -> {
			String s = entry.getKey();
			DBObject dbObject = entry.getValue();
			if (dbObject.code != null && !objects.get(s).containsKey(dbObject.code)) {
				objects.get(s).put(dbObject.code, dbObject);
			} else if (dbObject.code != null) {
				logger.warn("{} already load from another line", s);
			} else {
				throw new RuntimeException("no code found for "+s);
			}
		});		
	}

	private void consolidateCodesOnLineObject(Map<String, DBObject> objectInLine) {
		Container container = (Container)objectInLine.get(Mapping.Keys.container.toString());
		if (container != null) {
			if (Action.save.equals(configuration.action)) {
				//allready one sample by line
				Sample sample = (Sample)objectInLine.get(Mapping.Keys.sample.toString());
				if (sample != null && sample.code != null) {
					List<Content> contents = container.contents.stream().filter(c -> (c.sampleCode == null)).collect(Collectors.toList());
					if (contents.size() == 1) {
						contents.get(0).sampleCode = sample.code;																
					} else if (contents.size() > 1) {
						contextValidation.addError("container.contents", "several contents without sampleCode");
					}
				}
				ContainerSupport support = (ContainerSupport)objectInLine.get(Mapping.Keys.support.toString());
				if (support != null && support.code == null) {
					support.code = CodeHelper.getInstance().generateContainerSupportCode(); 
					//update content sampleCode
					if (container.support.code == null) {
						container.support.code = support.code;				
					} else {
						contextValidation.addError("container.support.code", "not null during support code generation : "+container.support.code);
					}

					ContainerSupportValidationHelper.validateCodeImportFile(contextValidation, support.code);
				}
			}
			//compute container code from support code and line and column
			ContainerSupport support = (ContainerSupport)objectInLine.get(Mapping.Keys.support.toString());
			if (support != null) {
				String containerCode = getContainerCode(support, container);
				if (containerCode != null && container.code == null) {
					container.code = containerCode;
				} else if (containerCode == null) {
					throw new RuntimeException("null container code");
				} else if (!containerCode.equals(container.code)) {
					contextValidation.addError("container.code", "error during container code generation : "+containerCode+" / "+container.code);
				}

				ContainerSupportValidationHelper.validateCodeImportFile(contextValidation, support.code);
			}
		}		
	}

	/*
	 * compute container code with the support code in case of container.code is null
	 */
	private String getContainerCode(ContainerSupport support, Container container) {
		ContainerSupportCategory csc = ContainerSupportCategory.find.get().findByCode(container.support.categoryCode);
		String code = null;
		if (csc.nbLine == 1 && csc.nbColumn == 1) {
			code = support.code;
		} else if(csc.nbLine > 1 && csc.nbColumn == 1) {
			container.support.line = container.support.line.toUpperCase();
			code = support.code+"_"+container.support.line;
		} else if(csc.nbLine > 1 && csc.nbColumn > 1) {
			container.support.line = container.support.line.toUpperCase();
			container.support.column = container.support.column.toUpperCase();	
			code = support.code+"_"+container.support.line+container.support.column;
		}
		return code;
	}

	/**
	 * Consolidate the object obtain after file parsing
	 */
	protected void consolidateObjects() {
		//First consolidate container
		if (configuration.configs.containsKey(Mapping.Keys.container.toString())) {
			Map<String, DBObject> containers = objects.get(Mapping.Keys.container.toString());
			containers.values().forEach(c -> {
				((ContainerMapping)mappings.get(Mapping.Keys.container.toString())).consolidate((Container)c);
			});
		}
		//Second consolidate support
		if (configuration.configs.containsKey(Mapping.Keys.support.toString())) {
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
		if (Action.save.equals(configuration.action)) {
			contextValidation.setCreationMode();
		} else {
			contextValidation.setUpdateMode();
		}
		if (saveObjectsForKey(Mapping.Keys.sample.toString())) {
			if (saveObjectsForKey(Mapping.Keys.support.toString())) {
				saveObjectsForKey(Mapping.Keys.container.toString());		
				rollbackObjectIFNeeded(Mapping.Keys.sample.toString(),Mapping.Keys.support.toString());
			} else {
				rollbackObjectIFNeeded(Mapping.Keys.sample.toString()); //??? Good idea ???
			}			
		}

	}

//	private void rollbackObjectIFNeeded(String... keys) {
//		Arrays.asList(keys).forEach(key -> {
//			if (contextValidation.hasErrors() && configuration.configs.containsKey(key)) {
//				Map<String, DBObject> dbobjects = objects.get(key);
//				Mapping<? extends DBObject> mapping = mappings.get(key);
//				dbobjects.values().forEach(o -> {
//					mapping.rollbackInMongoDB(o);				
//				});
//			}
//		});
//	}
	private void rollbackObjectIFNeeded(String... keys) {
		for (String key : keys) {
			if (contextValidation.hasErrors() && configuration.configs.containsKey(key)) {
				Map<String, DBObject> dbobjects = objects.get(key);
				Mapping<? extends DBObject> mapping = mappings.get(key);
				dbobjects.values().forEach(o -> {
					mapping.rollbackInMongoDB(o);				
				});
			}
		}
	}

	/**
	 * Save objects only if no error.
	 * @param  key key
	 * @return     success status
	 */
//	private boolean saveObjectsForKey(String key) {
//		if (!contextValidation.hasErrors() && configuration.configs.containsKey(key)) {
//			Map<String, ? extends DBObject> dbobjects = objects.get(key);
//			Mapping<? extends DBObject>     mapping   = mappings.get(key);
//			contextValidation.addKeyToRootKeyName(key);
//			dbobjects.values().forEach(o -> {
//				mapping.validate(o);				
//			});
//			contextValidation.removeKeyFromRootKeyName(key);
//
//			if (!contextValidation.hasErrors()) {
//				dbobjects.values().forEach(c -> {
//					mapping.synchronizeMongoDB(c);					
//				});
//			}
//		}
//		return !contextValidation.hasErrors();
//	}
	private boolean saveObjectsForKey(String key) {
		if (!contextValidation.hasErrors() && configuration.configs.containsKey(key)) {
			Collection<? extends DBObject> dbobjects = objects.get(key).values();
			Mapping<? extends DBObject>    mapping   = mappings.get(key);
			contextValidation.addKeyToRootKeyName(key);
			for (DBObject o : dbobjects)
				mapping.validate(o);				
			contextValidation.removeKeyFromRootKeyName(key);

			if (!contextValidation.hasErrors()) 
				for (DBObject c : dbobjects)
					mapping.synchronizeMongoDB(c);					
		}
		return !contextValidation.hasErrors();
	}

	/**
	 * Process file content method (name could be 'process' or 'run').
	 */
	public abstract void analyse();

	// ------------------------------------------------------------------------
	// polymorphic implementation of updateHeaderConfiguration
	
	/**
	 * Update HeaderLabel in ExcelFieldConfiguration to have a good error message
	 */
//	protected void updateHeaderConfiguration() {
//		Set<String> objectTypes = configuration.configs.keySet();
//		objectTypes.stream().forEach(s -> {
//			Map<String, ? extends AbstractFieldConfiguration> fieldConfigurations = configuration.configs.get(s);
//			Set<String> propertyNames = configuration.configs.get(s).keySet();
//			propertyNames.stream().forEach(pName ->{
//				updateAbstractFieldConfigurationHeader(fieldConfigurations.get(pName));
//			});
//		});
//	}
	protected void updateHeaderConfiguration() {
		for (String s : configuration.configs.keySet()) {
			Map<String, ? extends AbstractFieldConfiguration> fieldConfigurations = configuration.configs.get(s);
			for (String pName : configuration.configs.get(s).keySet())
				updateAbstractFieldConfigurationHeader(fieldConfigurations.get(pName));
		}
	}

	// Should be equivalent to the isAssignableForm implmentation.
	protected void updateAbstractFieldConfigurationHeader_(AbstractFieldConfiguration afc) {
		afc.updateFromHeader(contextValidation, headerByIndex);
	}
	
	private void updateAbstractFieldConfigurationHeader(AbstractFieldConfiguration afc) {
		if (ExcelFieldConfiguration.class.isAssignableFrom(afc.getClass())) {
			updateExcelConfigurationHeader((ExcelFieldConfiguration)afc);
		} else if (DoubleExcelFieldConfiguration.class.isAssignableFrom(afc.getClass())) {
			updateDoubleExcelConfigurationHeader((DoubleExcelFieldConfiguration)afc);
		} else if (PropertiesFieldConfiguration.class.isAssignableFrom(afc.getClass())) {
			PropertiesFieldConfiguration pfc = (PropertiesFieldConfiguration)afc;
			Set<String> propertyNames = pfc.configs.keySet();
			propertyNames.stream().forEach(_pName -> {
				updateAbstractFieldConfigurationHeader(pfc.configs.get(_pName));
			});
		} else if (PropertyValueFieldConfiguration.class.isAssignableFrom(afc.getClass())) {
			PropertyValueFieldConfiguration pvfc = (PropertyValueFieldConfiguration)afc;
			updateAbstractFieldConfigurationHeader(pvfc.value);
			if (pvfc.unit != null)
				updateAbstractFieldConfigurationHeader(pvfc.unit);
		} else if (ObjectFieldConfiguration.class.isAssignableFrom(afc.getClass())) {
			ObjectFieldConfiguration<?> ofc = (ObjectFieldConfiguration<?>)afc;
			Set<String> propertyNames = ofc.configs.keySet();
			propertyNames.stream().forEach(_pName -> {
				updateAbstractFieldConfigurationHeader(ofc.configs.get(_pName));
			});
		} else if (TagExcelFieldConfiguration.class.isAssignableFrom(afc.getClass())) {
			updateTagExcelConfigurationHeader((TagExcelFieldConfiguration)afc);
		}
	}

	// FDS 04/07/2017 remplacer cellCode par cellName
	private void updateTagExcelConfigurationHeader(TagExcelFieldConfiguration efc) {
		if (headerByIndex.containsKey(efc.cellSequence) && headerByIndex.containsKey(efc.cellName)) {
			efc.headerValue = headerByIndex.get(efc.cellSequence) + " / " + headerByIndex.get(efc.cellName);
		} else if (headerByIndex.containsKey(efc.cellSequence)) {
			efc.headerValue = headerByIndex.get(efc.cellSequence);
		} else {
			contextValidation.addError("Headers","not found header for cell position "+efc.cellSequence);
		}
	}
	
	private void updateExcelConfigurationHeader(ExcelFieldConfiguration efc) {
		if (headerByIndex.containsKey(efc.cellPosition)) {
			efc.headerValue = headerByIndex.get(efc.cellPosition);
		} else {
			contextValidation.addError("Headers","not found header for cell position " + efc.cellPosition);
		}
	}

	private void updateDoubleExcelConfigurationHeader(DoubleExcelFieldConfiguration efc) {
		if (headerByIndex.containsKey(efc.cellPosition1) && headerByIndex.containsKey(efc.cellPosition2)) {
			efc.headerValue = headerByIndex.get(efc.cellPosition1) + " / " + headerByIndex.get(efc.cellPosition2);
		} else {
			contextValidation.addError("Headers","not found header for cell position "+efc.cellPosition1);
		}
	}

}
