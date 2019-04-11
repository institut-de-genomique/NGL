package models.laboratory.project.description;

import java.util.function.Supplier;

import fr.cea.ig.ngl.utils.SpringSupplier;
import models.laboratory.common.description.AbstractCategory;
import models.laboratory.project.description.dao.ProjectCategoryDAO;
import ngl.refactoring.MiniDAO;

//public class ProjectCategory extends AbstractCategory<ProjectCategory>{
public class ProjectCategory extends AbstractCategory {

//	public static Finder<ProjectCategory> find = new Finder<ProjectCategory>(ProjectCategoryDAO.class.getName());
//	public static final Finder<ProjectCategory,ProjectCategoryDAO> find = new Finder<>(ProjectCategoryDAO.class);
//	public static final ProjectCategoryDAO find = Spring.getBeanOfType(ProjectCategoryDAO.class);
	public static final Supplier<ProjectCategoryDAO> find = new SpringSupplier<>(ProjectCategoryDAO.class);
	public static final Supplier<MiniDAO<ProjectCategory>> miniFind = MiniDAO.createSupplier(find);
	
	// Serialization constructor
	public ProjectCategory() {}
	
	public ProjectCategory(String code, String name) {
		super(code,name);
	}
	
//	public ProjectCategory() {
//		super(ProjectCategoryDAO.class.getName());
//	}
//
//	@Override
//	protected Class<? extends AbstractDAO<ProjectCategory>> daoClass() {
//		return ProjectCategoryDAO.class;
//	}

}
