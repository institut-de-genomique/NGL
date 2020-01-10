package models.laboratory.sample.description;

import java.util.function.Supplier;

import fr.cea.ig.ngl.utils.SpringSupplier;
import models.laboratory.common.description.CommonInfoType;
import models.laboratory.sample.description.dao.ImportTypeDAO;
import ngl.refactoring.MiniDAO;

/**
 * Additional information collaborator.
 * 
 * @author ejacoby
 *
 */
public class ImportType extends CommonInfoType {

	@SuppressWarnings("hiding")
	public static final Supplier<ImportTypeDAO>       find     = new SpringSupplier<>(ImportTypeDAO.class);
	public static final Supplier<MiniDAO<ImportType>> miniFind = MiniDAO.createSupplier(find);
	
	public ImportCategory category;

	// Moved to super class
//	public List<PropertyDefinition> getPropertiesDefinitionSampleLevel() {
//		return getPropertyDefinitionByLevel(Level.CODE.Sample);
//	}
//	
//	public List<PropertyDefinition> getPropertiesDefinitionContainerLevel() {
//		return getPropertyDefinitionByLevel(Level.CODE.Container);
//	}
//	
//	public List<PropertyDefinition> getPropertiesDefinitionContentLevel() {
//		return getPropertyDefinitionByLevel(Level.CODE.Content);
//	}

}
