package models.laboratory.processes.description;

import java.util.function.Supplier;

import fr.cea.ig.ngl.utils.SpringSupplier;
import models.laboratory.common.description.AbstractCategory;
import models.laboratory.processes.description.dao.ProcessCategoryDAO;
import ngl.refactoring.MiniDAO;

//public class ProcessCategory extends AbstractCategory<ProcessCategory> {
public class ProcessCategory extends AbstractCategory {

//	public static Finder<ProcessCategory> find = new Finder<ProcessCategory>(ProcessCategoryDAO.class.getName());
//	public static final Finder<ProcessCategory,ProcessCategoryDAO> find = new Finder<>(ProcessCategoryDAO.class);
//	public static final ProcessCategoryDAO find = Spring.getBeanOfType(ProcessCategoryDAO.class);
	public static final Supplier<ProcessCategoryDAO> find = new SpringSupplier<>(ProcessCategoryDAO.class);
	public static final Supplier<MiniDAO<ProcessCategory>> miniFind = MiniDAO.createSupplier(find);
	
	// Serialization constructor
	public ProcessCategory() {}
	
	public ProcessCategory(String code, String name) {
		super(code,name);
	}
	
//	public ProcessCategory() {
//		super(ProcessCategoryDAO.class.getName());
//	}
//
//	@Override
//	protected Class<? extends AbstractDAO<ProcessCategory>> daoClass() {
//		return ProcessCategoryDAO.class;
//	}

}
