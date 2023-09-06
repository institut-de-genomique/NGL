package models.laboratory.run.description;

import java.util.function.Supplier;

import fr.cea.ig.ngl.utils.SpringSupplier;
import models.laboratory.common.description.CommonInfoType;
import models.laboratory.run.description.dao.ReadSetTypeDAO;
import ngl.refactoring.MiniDAO;

public class ReadSetType extends CommonInfoType {

	public static final Supplier<ReadSetTypeDAO>       find     = new SpringSupplier<>(ReadSetTypeDAO.class);
	public static final Supplier<MiniDAO<ReadSetType>> miniFind = MiniDAO.createSupplier(find); 
	
//	public List<PropertyDefinition> getPropertiesDefinitionDefaultLevel() {
//		return getPropertyDefinitionByLevel(Level.CODE.ReadSet);
//	}
	
}
