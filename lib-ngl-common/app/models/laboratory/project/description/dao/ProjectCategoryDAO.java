package models.laboratory.project.description.dao;

import models.laboratory.project.description.ProjectCategory;
import models.utils.dao.AbstractDAODefault;

import java.util.List;

import org.springframework.stereotype.Repository;

@Repository
public class ProjectCategoryDAO extends AbstractDAODefault<ProjectCategory> {

	public ProjectCategoryDAO() {
		super("project_category",ProjectCategory.class,true);
	}
	
	@Override
	protected List<String> getColumns() {
		return enumColumns;
	}
	
}
