package models.laboratory.project.description;

import java.util.function.Supplier;

import fr.cea.ig.ngl.utils.SpringSupplier;
import models.laboratory.common.description.AbstractCategory;
import models.laboratory.project.description.dao.UmbrellaProjectCategoryDAO;
import ngl.refactoring.MiniDAO;

public class UmbrellaProjectCategory extends AbstractCategory {

	public static final Supplier<UmbrellaProjectCategoryDAO> find = new SpringSupplier<>(UmbrellaProjectCategoryDAO.class);
	public static final Supplier<MiniDAO<UmbrellaProjectCategory>> miniFind = MiniDAO.createSupplier(find);
	
	// Serialization constructor
	public UmbrellaProjectCategory() {}
	
	public UmbrellaProjectCategory(String code, String name) {
		super(code,name);
	}

}
