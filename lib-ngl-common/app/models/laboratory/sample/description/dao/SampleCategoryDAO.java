package models.laboratory.sample.description.dao;

import models.laboratory.sample.description.SampleCategory;
import models.utils.dao.AbstractDAODefault;

import java.util.List;

import org.springframework.stereotype.Repository;

@Repository
public class SampleCategoryDAO extends AbstractDAODefault<SampleCategory> {

	public SampleCategoryDAO() {
		super("sample_category",SampleCategory.class,true);
	}

	@Override
	protected List<String> getColumns() {
		return enumColumns;
	}

}
