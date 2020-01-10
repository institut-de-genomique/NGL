package models.laboratory.sample.description;

import java.util.function.Supplier;

import fr.cea.ig.ngl.utils.SpringSupplier;
import models.laboratory.common.description.AbstractCategory;
import models.laboratory.sample.description.dao.SampleCategoryDAO;
import ngl.refactoring.MiniDAO;

//public class SampleCategory extends AbstractCategory<SampleCategory> {
public class SampleCategory extends AbstractCategory {

//	public static Finder<SampleCategory> find = new Finder<SampleCategory>(SampleCategoryDAO.class.getName());
//	public static final Finder<SampleCategory,SampleCategoryDAO> find = new Finder<>(SampleCategoryDAO.class);
//	public static final SampleCategoryDAO find = Spring.getBeanOfType(SampleCategoryDAO.class);
	public static final Supplier<SampleCategoryDAO> find = new SpringSupplier<>(SampleCategoryDAO.class);
	public static final Supplier<MiniDAO<SampleCategory>>  miniFind = MiniDAO.createSupplier(find);
	
	// Serialization constructor
	public SampleCategory() {}
	
	public SampleCategory(String code, String name) {
		super(code,name);
	}
	
//	public SampleCategory() {
//		super(SampleCategoryDAO.class.getName());
//	}
//
//	@Override
//	protected Class<? extends AbstractDAO<SampleCategory>> daoClass() {
//		return SampleCategoryDAO.class;
//	}

}
