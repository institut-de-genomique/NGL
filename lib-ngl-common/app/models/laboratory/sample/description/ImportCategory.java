package models.laboratory.sample.description;

import java.util.function.Supplier;

import fr.cea.ig.ngl.utils.SpringSupplier;
import models.laboratory.common.description.AbstractCategory;
import models.laboratory.sample.description.dao.ImportCategoryDAO;

/**
 * Category of collaborator informations
 * 
 * @author ejacoby
 *
 */
//public class ImportCategory extends AbstractCategory<ImportCategory> {
public class ImportCategory extends AbstractCategory {

//	public static Finder<ImportCategory> find = new Finder<ImportCategory>(ImportCategoryDAO.class.getName());
//	public static final Finder<ImportCategory,ImportCategoryDAO> find = new Finder<>(ImportCategoryDAO.class);
//	public static final ImportCategoryDAO find = Spring.getBeanOfType(ImportCategoryDAO.class);
	public static final Supplier<ImportCategoryDAO> find = new SpringSupplier<>(ImportCategoryDAO.class);
	
//	public ImportCategory() {
//		super(ImportCategoryDAO.class.getName());
//	}
//
//	@Override
//	protected Class<? extends AbstractDAO<ImportCategory>> daoClass() {
//		return ImportCategoryDAO.class;
//	}

	// Serialization constructor
	public ImportCategory() {}
	
	public ImportCategory(String code, String name) {
		super(code,name);
	}
	
}
