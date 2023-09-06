package models.laboratory.common.description;

import java.util.function.Supplier;

import fr.cea.ig.ngl.utils.SpringSupplier;
import models.laboratory.common.description.dao.MeasureCategoryDAO;

public class MeasureCategory extends AbstractCategory {
		
	public static final Supplier<MeasureCategoryDAO> find = new SpringSupplier<>(MeasureCategoryDAO.class); 
	
	// Serialization constructor
	public MeasureCategory() {}
	
	public MeasureCategory(String code, String name) {
		super(code,name);
	}
	
}
