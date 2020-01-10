package models.laboratory.container.description;

import java.util.function.Supplier;

import fr.cea.ig.ngl.utils.SpringSupplier;
import models.laboratory.common.description.AbstractCategory;
import models.laboratory.container.description.dao.ContainerCategoryDAO;
import ngl.refactoring.MiniDAO;

// This link : {@link models.laboratory.container.description.ContainerCategory}

/**
 * Container category.
 * <p>
 * It is referenced by {@link ContainerSupportCategory} to specify the type
 * of containers for the support category.
 */
public class ContainerCategory extends AbstractCategory {

	public static final Supplier<ContainerCategoryDAO>       find     = new SpringSupplier<>(ContainerCategoryDAO.class); 
	public static final Supplier<MiniDAO<ContainerCategory>> miniFind = MiniDAO.createSupplier(find);
			
	// Serialization constructor
	public ContainerCategory() { }
	
	public ContainerCategory(String code, String name) {
		super(code,name);
	}
	
}
