package models.laboratory.common.description.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import models.laboratory.common.description.StateCategory;
import models.utils.dao.AbstractDAODefault;

@Repository
public class StateCategoryDAO extends AbstractDAODefault<StateCategory> {

	protected StateCategoryDAO() {
		super("state_category", StateCategory.class, true);
	}
	
	@Override
	protected List<String> getColumns() {
		return enumColumns;
	}
	
}


