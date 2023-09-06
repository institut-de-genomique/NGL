package models.laboratory.project.description.dao;

import models.laboratory.project.description.UmbrellaProjectCategory;
import models.utils.dao.AbstractDAODefault;

import java.util.List;

import org.springframework.stereotype.Repository;

@Repository
public class UmbrellaProjectCategoryDAO extends AbstractDAODefault<UmbrellaProjectCategory> {

	public UmbrellaProjectCategoryDAO() {
		super("umbrella_project_category",UmbrellaProjectCategory.class,true);
	}
	
	@Override
	protected List<String> getColumns() {
		return enumColumns;
	}
	
}
