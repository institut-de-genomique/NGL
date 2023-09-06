package models.laboratory.container.description;

import java.util.function.Supplier;

import fr.cea.ig.ngl.utils.SpringSupplier;
import models.laboratory.common.description.AbstractCategory;
import models.laboratory.container.description.dao.ContainerSupportCategoryDAO;
import ngl.refactoring.MiniDAO;

// This link : {@link models.laboratory.container.description ContainerSupportCategory}

/**
 * Container support category. Defines the topology of containers of this category (type).  
 */
public class ContainerSupportCategory extends AbstractCategory {
	
	public static final Supplier<ContainerSupportCategoryDAO>       find     = new SpringSupplier<>(ContainerSupportCategoryDAO.class); 
	public static final Supplier<MiniDAO<ContainerSupportCategory>> miniFind = MiniDAO.createSupplier(find);
	
	/**
	 * Usable container count, most probably line times columns.
	 */
	public Integer           nbUsableContainer;
	
	/**
	 * Number of lines, greater than 0. 
	 */
	public Integer           nbLine;
	
	/**
	 * Number of columns, greater than 0.
	 */
	public Integer           nbColumn;
	
	/**
	 * Type of containers for this support (e.g: well for a plate).
	 */
	public ContainerCategory containerCategory;
	
	// Serialization constructor
	public ContainerSupportCategory() {}
	
	public ContainerSupportCategory(String code, String name) {
		super(code,name);
	}
	
	public ContainerSupportCategory(String code, String name, Integer nbUsableContainer, Integer nbLine, Integer nbColumn, ContainerCategory containerCategory) {
		this(code,name);
		this.nbUsableContainer = nbUsableContainer;
		this.nbLine            = nbLine;
		this.nbColumn          = nbColumn;
		this.containerCategory = containerCategory;
	}
	
}
