package models.laboratory.run.description;

import java.util.function.Supplier;

import fr.cea.ig.ngl.utils.SpringSupplier;
import models.laboratory.common.description.AbstractCategory;
import models.laboratory.run.description.dao.RunCategoryDAO;
import ngl.refactoring.MiniDAO;

public class RunCategory extends AbstractCategory {
	
	public static final Supplier<RunCategoryDAO> find = new SpringSupplier<>(RunCategoryDAO.class);
	public static final Supplier<MiniDAO<RunCategory>> miniFind = MiniDAO.createSupplier(find); 
		
	// Serialization constructor
	public RunCategory() {}
	
	public RunCategory(String code, String name) {
		super(code,name);
	}
	
}
