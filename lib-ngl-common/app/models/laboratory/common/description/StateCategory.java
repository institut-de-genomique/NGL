package models.laboratory.common.description;

import java.util.function.Supplier;

import fr.cea.ig.ngl.utils.SpringSupplier;
import models.laboratory.common.description.dao.StateCategoryDAO;

public class StateCategory extends AbstractCategory {
	
	public static final Supplier<StateCategoryDAO> find = new SpringSupplier<>(StateCategoryDAO.class); 
	
	public enum CODE {
		F,
		IP,
		IW,
		N
	} 
	
	// Serialization constructor
	public StateCategory() {}
	
	public StateCategory(String code, String name) {
		super(code,name);
	}
	
	public StateCategory(CODE code, String name) {
		this(code.name(),name);
	}
	
	public StateCategory(CODE code) {
		this(code.name(), code.name());
	}
	
}
