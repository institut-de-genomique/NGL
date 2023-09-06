package models.utils.instance;

import static fr.cea.ig.play.IGGlobals.configuration;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;

import fr.cea.ig.MongoDBDAO;
import models.laboratory.common.description.Level;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.ContainerSupport;
import models.laboratory.container.instance.Content;
import models.laboratory.sample.description.ImportType;
import models.laboratory.sample.description.SampleType;
import models.laboratory.sample.instance.Sample;
import models.utils.InstanceConstants;
import models.utils.InstanceHelpers;
import models.utils.dao.DAOException;
import rules.services.RulesServices6;
import validation.ContextValidation;
import validation.common.instance.CommonValidationHelper;

public class ContainerHelper extends CommonValidationHelper {

	private static final play.Logger.ALogger logger = play.Logger.of(ContainerHelper.class);
	
	// Unused, would crash
//	public static void addContent(Container container, Sample sample) throws DAOException {
//		addContent(container,sample,null);
//	}

	// Would there be an error in the validation process, it would trigger a null pointer exceptions
	// as the ContextValidation is passed as a null value.
	public static void addContent(Container container, Sample sample, Content content) throws DAOException {
		Content finalContent            = new Content(sample.code, sample.typeCode, sample.categoryCode);
		finalContent.projectCode        = content.projectCode;
		finalContent.percentage         = content.percentage;
		finalContent.referenceCollab    = content.referenceCollab;
		finalContent.taxonCode          = content.taxonCode;
		finalContent.ncbiScientificName = content.ncbiScientificName;
		
//		SampleType sampleType = BusinessValidationHelper.validateExistDescriptionCode(null, sample.typeCode, "typeCode", SampleType.find.get(),true);
//		ImportType importType = BusinessValidationHelper.validateExistDescriptionCode(null, sample.importTypeCode, "importTypeCode", ImportType.find.get(),true);
		SampleType sampleType = validateCodeForeignOptional(null, SampleType.miniFind.get(), sample.typeCode,       "typeCode",       true);
		ImportType importType = validateCodeForeignOptional(null, ImportType.miniFind.get(), sample.importTypeCode, "importTypeCode", true);

		if (importType != null)
			InstanceHelpers.copyPropertyValueFromPropertiesDefinition(importType.getPropertyDefinitionByLevel(Level.CODE.Content), sample.properties,finalContent.properties);
		if (sampleType != null)
			InstanceHelpers.copyPropertyValueFromPropertiesDefinition(sampleType.getPropertyDefinitionByLevel(Level.CODE.Content), sample.properties,finalContent.properties);
		if (content.properties != null)
			finalContent.properties.putAll(content.properties);

		container.contents.add(finalContent);
		container.projectCodes.addAll(sample.projectCodes);
		container.sampleCodes.add(sample.code);

	}
	
	/*
	 * fusion content if same projectCode, sampleCode and tag if exist.
	 * 
	 * the fusion : 
	 * 	- sum the percentage of content 
	 *  - keep properties with same key and same value
	 *  - remove properties  with same key and different value
	 *  - add properties that exists only in one content
	 *  
	 * @param contents
	 * @return
	 */
	public static List<Content> fusionContents(List<Content> contents) {
		
		// group by a key
		Map<String, List<Content>> contentsByKey = contents.stream().collect(Collectors.groupingBy((Content c ) -> getContentKey(c)));
		
		// extract values with only one content
		Map<String, Content> contentsByKeyWithOneValues = contentsByKey.entrySet().stream()
				.filter((Map.Entry<String, List<Content>> e) -> e.getValue().size() == 1)
				.collect(Collectors.toMap((Map.Entry<String, List<Content>> e) -> e.getKey(), (Map.Entry<String, List<Content>> e) -> e.getValue().get(0)));
		
		// extract values with several contents and fusion the contents
		Map<String, Content> contentsByKeyWithSeveralValues = contentsByKey.entrySet().stream()
				.filter((Map.Entry<String, List<Content>> e) -> e.getValue().size() > 1)
				.collect(Collectors.toMap((Map.Entry<String, List<Content>> e) -> e.getKey(), (Map.Entry<String, List<Content>> e) -> fusionSameContents(e.getValue())));
		
		
		contentsByKeyWithOneValues.putAll(contentsByKeyWithSeveralValues);
		
		return new ArrayList<>(contentsByKeyWithOneValues.values());
	}

	/**
	 * Create a shallow clone of a content.
	 * @param content content to clone 
	 * @return        cloned content
	 */
	private static Content cloneContent(Content content) {
		Content clone = new Content();
		clone.projectCode        = content.projectCode;
		clone.sampleCode         = content.sampleCode;
		clone.sampleCategoryCode = content.sampleCategoryCode;
		clone.sampleTypeCode     = content.sampleTypeCode;
		clone.referenceCollab    = content.referenceCollab;
		clone.percentage         = content.percentage;
		clone.properties.putAll(content.properties);
		clone.taxonCode          = content.taxonCode;
		clone.ncbiScientificName = content.ncbiScientificName;
		clone.processProperties  = content.processProperties;
		clone.processComments    = content.processComments;
		return clone;
	}
	
	
	private static Content fusionSameContents(List<Content> contents) {
		Content finalContent = new Content();
		
		finalContent.projectCode        = contents.get(0).projectCode;
		finalContent.sampleCode         = contents.get(0).sampleCode;
		finalContent.sampleCategoryCode = contents.get(0).sampleCategoryCode;
		finalContent.sampleTypeCode     = contents.get(0).sampleTypeCode;
		finalContent.referenceCollab    = contents.get(0).referenceCollab;
		finalContent.taxonCode          = contents.get(0).taxonCode;
		finalContent.ncbiScientificName = contents.get(0).ncbiScientificName;
		finalContent.percentage         = new BigDecimal(contents.stream().mapToDouble((Content c) -> c.percentage).sum()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
		finalContent.processProperties  = new HashMap<>(); // String, PropertyValue>();
		finalContent.processComments    = new ArrayList<>();
				
		for (Content c : contents) {
			if (c.properties != null) { 
				for (String key : c.properties.keySet()) {
					PropertyValue pv = c.properties.get(key);
					finalContent.properties.computeIfAbsent(key, k -> pv);
					finalContent.properties.computeIfPresent(key, (k,v) -> fusionSameProperty(v, pv));					
				}
			}
			if (c.processProperties != null) {
				for (String key : c.processProperties.keySet()) {
					PropertyValue pv = c.processProperties.get(key);
					finalContent.processProperties.computeIfAbsent(key, k -> pv);
					finalContent.processProperties.computeIfPresent(key, (k,v) -> fusionSameProperty(v, pv));
				}
			}
			if (c.processComments != null && c.processComments.size() > 0) {
				finalContent.processComments.addAll(c.processComments);
			}
		}
		// remove properties with #NOT_COMMON_VALUE#
		finalContent.properties = finalContent.properties.entrySet()
										.stream()
										.filter(e -> !e.getValue().value.equals("#NOT_COMMON_VALUE#"))
										.collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
		
		finalContent.processProperties = finalContent.processProperties.entrySet()
				.stream()
				.filter(e -> !e.getValue().value.equals("#NOT_COMMON_VALUE#"))
				.collect(Collectors.toMap(e -> e.getKey(),e -> e.getValue()));

//		if (finalContent.processComments == null || finalContent.processComments.size() == 0) {
//			finalContent.processComments = null;
//		}
		if (CollectionUtils.isEmpty(finalContent.processComments))
			finalContent.processComments = null;
		return finalContent;
	}

	private static  PropertyValue fusionSameProperty(PropertyValue currentPv, PropertyValue newPv) {
		if (currentPv.value.equals(newPv.value)){
			return currentPv;
		} else { 
			return new PropertySingleValue("#NOT_COMMON_VALUE#");
		}		
	}

	private static String getContentKey(Content content) {
		if (content.properties.containsKey(InstanceConstants.TAG_PROPERTY_NAME) && !content.properties.containsKey(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME)) {
			return content.projectCode+"_"+content.sampleCode+"_"+content.properties.get(InstanceConstants.TAG_PROPERTY_NAME).value;
		} else if(!content.properties.containsKey(InstanceConstants.TAG_PROPERTY_NAME) && content.properties.containsKey(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME)){
			return content.projectCode+"_"+content.sampleCode+"_"+content.properties.get(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME).value;
		} else if(content.properties.containsKey(InstanceConstants.TAG_PROPERTY_NAME) && content.properties.containsKey(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME)) {
			return content.projectCode+"_"+content.sampleCode+"_"+content.properties.get(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME).value+"_"+content.properties.get(InstanceConstants.TAG_PROPERTY_NAME).value;
		}else {
			return content.projectCode+"_"+content.sampleCode;
		}		
	}

	/*
	 * clone and compute percentage
	 * @param contents
	 * @param percentage
	 * @return
	 */
	public static List<Content> calculPercentageContent(List<Content> contents, Double percentage) {
		List<Content> newContents = new ArrayList<>(0);
		for (Content cc : contents) {
			Content newContent = cloneContent(cc);
			if (percentage != null && newContent.percentage != null) {
				newContent.percentage = (new BigDecimal((cc.percentage*percentage)/100.00)).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue();
			} else if (percentage != null) {
				newContent.percentage = percentage;
			}
			newContents.add(newContent);
		}
		return newContents;
	}

	public static List<ContainerSupport> createSupportFromContainers(List<Container> containers, Map<String, PropertyValue> mapSupportsCodeSeq, ContextValidation contextValidation){

		HashMap<String,ContainerSupport> mapSupports = new HashMap<>();

		for (Container container : containers) {
			logger.debug(" createSupportFromContainers; container.code "+ container.code );
			
			if (container.support != null) {
				ContainerSupport newSupport = null;
				
				logger.debug(" createSupportFromContainers; creating support "+ container.support.code );
				
				// mapSupportsCodeSeq n'existe que pour les flowcell...
				// NW utilisation d'un operateur ternaire
				newSupport = ContainerSupportHelper.createContainerSupport(container.support.code,
																			(mapSupportsCodeSeq != null ? mapSupportsCodeSeq.get(container.support.code) : null),
																			container.support.categoryCode, "ngl");
				newSupport.projectCodes = new  HashSet<>(container.projectCodes);
				newSupport.sampleCodes  = new  HashSet<>(container.sampleCodes);
				newSupport.state        = container.state;
				newSupport.storageCode  = container.support.storageCode; //FDS ajout 14/10/2015
				
				if (container.fromTransformationTypeCodes != null) {
					newSupport.fromTransformationTypeCodes = new  HashSet<>(container.fromTransformationTypeCodes);
				}
				
				if (!mapSupports.containsKey(newSupport.code)) {
					newSupport.nbContainers = 1;
					newSupport.nbContents = container.contents.size();
					mapSupports.put(newSupport.code, newSupport);
				} else {
//					ContainerSupport oldSupport = (ContainerSupport) mapSupports.get(newSupport.code);
					ContainerSupport oldSupport = mapSupports.get(newSupport.code);
					oldSupport.projectCodes.addAll(newSupport.projectCodes); 
					oldSupport.sampleCodes.addAll(newSupport.sampleCodes);
					oldSupport.nbContainers++;
					oldSupport.nbContents = oldSupport.nbContents + container.contents.size(); // use += ?
					if (newSupport.fromTransformationTypeCodes != null && oldSupport.fromTransformationTypeCodes != null) {
						oldSupport.fromTransformationTypeCodes.addAll(newSupport.fromTransformationTypeCodes);
					}
				}
			}
		}
		return InstanceHelpers.save(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, new ArrayList<>(mapSupports.values()), contextValidation, true);
	}

	public static void updateSupportFromUpdatedContainers(List<Container> updatedContainers, Map<String, PropertyValue> mapSupportsCodeSeq, ContextValidation contextValidation){

		HashMap<String,ContainerSupport> mapSupports = new HashMap<>();
		//NOTE FDS 16/06/2016 les cas de supression de containers dans un support ne sont pas gerees par ce code...

		for (Container container : updatedContainers) {
			if (container.support != null) {
				
				ContainerSupport newSupport = null;
				
				//FDS note 22/06/2015: mapSupportsCodeSeq n'est defini que pour les containers de type lane!!
				//21/01/2016  NW utilisation d'un operateur ternaire
				newSupport = ContainerSupportHelper.createContainerSupport(container.support.code,
																			(mapSupportsCodeSeq != null ? mapSupportsCodeSeq.get(container.support.code) : null),
																			container.support.categoryCode, "ngl");
					
				newSupport.projectCodes = new  HashSet<>(container.projectCodes);
				newSupport.sampleCodes = new  HashSet<>(container.sampleCodes);		
				newSupport.fromTransformationTypeCodes = new  HashSet<>(container.fromTransformationTypeCodes); // FDS ajout 22/01/2016	
				
				//FDS ajout 14/10/2015
				if ( container.support.storageCode != null ){
					newSupport.storageCode=container.support.storageCode;
				}
				
				if (!mapSupports.containsKey(newSupport.code)) {
					mapSupports.put(newSupport.code, newSupport);
					// TEST 16/06/2016
					newSupport.nbContainers = 1;
					newSupport.nbContents = container.contents.size();
				} else {
					// NOTE FDS 16/06/216 comment gerer le remplacement d'une ancienne valeur par une nouvelle ??
					
//					ContainerSupport oldSupport = (ContainerSupport) mapSupports.get(newSupport.code);
					ContainerSupport oldSupport = mapSupports.get(newSupport.code);
					oldSupport.projectCodes.addAll(newSupport.projectCodes); 
					//Logger.debug("[updateSupportFromUpdatedContainers] adding projectCodes " +  newSupport.projectCodes + " to containerSupport " + oldSupport.code);
					
					oldSupport.sampleCodes.addAll(newSupport.sampleCodes); 
					//Logger.debug("[updateSupportFromUpdatedContainers] adding sampleCodes "+ newSupport.sampleCodes + " to containerSupport " + oldSupport.code);

					// et si null ??
					oldSupport.fromTransformationTypeCodes.addAll(newSupport.fromTransformationTypeCodes); 
					//Logger.debug("[updateSupportFromUpdatedContainers] adding fromTransformationTypeCodes "+ newSupport.fromTransformationTypeCodes + " to containerSupport " + oldSupport.code);
					
					//NOTE FDS 16/06/2016  avec ce code nbContainers et nbContents prennnet les valeurs de nbre de container modifiés...
					oldSupport.nbContainers++;
					oldSupport.nbContents = oldSupport.nbContents + container.contents.size();
				}
			}
		}

        // boucler sur les containers supports modifiés plus haut..
		for (Map.Entry<String,ContainerSupport> support : mapSupports.entrySet()) {
			
			//Logger.debug("[updateSupportFromUpdatedContainers] get from mongo ContainerSupport: "+ support.getKey() );
			ContainerSupport dbCs = MongoDBDAO.findByCode(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, ContainerSupport.class, support.getKey());

			ContainerSupport updatedCs = support.getValue();

			// FDS: la constante ngl.app.models.Constants NGL_DATA_USER n'est pas accessible ici...
			updatedCs.traceInformation = TraceInformation.updateOrCreateTraceInformation(dbCs.traceInformation, "ngl-data");

			// FDS 22/01/2016 Pourquoi ce test ??? pourquoi tout containerSupport modifié n'est pas automatiquement supprimé ???
			//     NOTE: projectCodes et sampleCodes sont des listes: il faut faire les tests d'inclusions dans les 2 sens !
			// GA 02/11/2015 prise en compte de la modification du storageCode ( !!peut etre null )
			if (   !dbCs.projectCodes.containsAll(updatedCs.projectCodes)
				|| !updatedCs.projectCodes.containsAll(dbCs.projectCodes) 
				|| !dbCs.sampleCodes.containsAll(updatedCs.sampleCodes) 
				|| !updatedCs.sampleCodes.containsAll(dbCs.sampleCodes) 
				|| !dbCs.fromTransformationTypeCodes.containsAll(updatedCs.fromTransformationTypeCodes) 
				|| !updatedCs.fromTransformationTypeCodes.containsAll(dbCs.fromTransformationTypeCodes) 
				|| (updatedCs.storageCode != null && !updatedCs.storageCode.equals(dbCs.storageCode))
				|| (dbCs.storageCode != null && !dbCs.storageCode.equals(updatedCs.storageCode))) {
				
				//Logger.debug("[updateSupportFromUpdatedContainers] detete from mongo ContainerSupport: "+ support.getKey() );
				MongoDBDAO.deleteByCode(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, ContainerSupport.class, support.getKey());
				
				//Logger.debug("[updateSupportFromUpdatedContainers] save to mongo ContainerSupport: "+ updatedCs.code);
				InstanceHelpers.save(contextValidation, InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, updatedCs, true);
			}
			// pour debug..
			//else { Logger.debug("[updateSupportFromUpdatedContainers] NOT deleting an NOT saving in mongo "+ support.getKey() );}
		}
	}

//	public static Set<Content> contentFromSampleCode(List<Content> contents, String sampleCode) {
//		Set<Content> contentsFind = new HashSet<>();
//		for (Content content:contents) {
//			if (content.sampleCode.equals(sampleCode)) {
//				contentsFind.add(content);
//			}
//		}
//		return contentsFind;
//	}
	
//	public static Set<Content> contentFromSampleCode(List<Content> contents, String sampleCode) {
//		return Iterables.filter(contents, c -> c.sampleCode.equals(sampleCode)).toSet();
//	}

//	@Deprecated
//	public static void save(Container outputContainer, ContextValidation contextValidation) {
//		contextValidation.addKeyToRootKeyName("container["+outputContainer.code+"]");
//		contextValidation.setCreationMode();
//		InstanceHelpers.save(InstanceConstants.CONTAINER_COLL_NAME,outputContainer, contextValidation);
//		contextValidation.removeKeyFromRootKeyName("container["+outputContainer.code+"]");
//	}
	
	public static Double getEquiPercentValue(int size) {
		BigDecimal p = new BigDecimal(100.00/size).setScale(2, RoundingMode.HALF_UP);						
		return p.doubleValue();
	}

	public static void executeContainerValidationRules(Container container, ContextValidation contextValidation) {
		executeRules(container, "containerValidationImport", contextValidation);
	}
	
	private static void executeRules(Container container, String rulesName, ContextValidation contextValidation) {
		ArrayList<Object> facts = new ArrayList<>();
		facts.add(container);
		facts.add(contextValidation);
		RulesServices6.getInstance().callRulesWithGettingFacts(configuration().getString("rules.key"), rulesName, facts);
	}
	
}
