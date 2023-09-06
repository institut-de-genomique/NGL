package models.laboratory.experiment.description;

import java.util.function.Supplier;

import fr.cea.ig.ngl.utils.SpringSupplier;
import models.laboratory.common.description.AbstractCategory;
import models.laboratory.experiment.description.dao.ProtocolCategoryDAO;

//public class ProtocolCategory extends AbstractCategory<ProtocolCategory> {
public class ProtocolCategory extends AbstractCategory {

//	public static Finder<ProtocolCategory> find = new Finder<ProtocolCategory>(ProtocolCategoryDAO.class.getName()); 
//	public static final Finder<ProtocolCategory,ProtocolCategoryDAO> find = new Finder<>(ProtocolCategoryDAO.class); 
//	public static final ProtocolCategoryDAO find = Spring.getBeanOfType(ProtocolCategoryDAO.class); 
	public static final Supplier<ProtocolCategoryDAO> find = new SpringSupplier<>(ProtocolCategoryDAO.class); 
	
	// Serialization constructor
	public ProtocolCategory() {}
	public ProtocolCategory(String code, String name) {
		super(code,name);
	}
	
//	public ProtocolCategory() {
//		super(ProtocolCategoryDAO.class.getName());
//	}
//
//	@Override
//	protected Class<? extends AbstractDAO<ProtocolCategory>> daoClass() {
//		return ProtocolCategoryDAO.class;
//	}
	
}
