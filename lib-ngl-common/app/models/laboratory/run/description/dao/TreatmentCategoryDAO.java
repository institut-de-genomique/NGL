package models.laboratory.run.description.dao;

import java.util.List;

import org.springframework.stereotype.Repository;
import models.laboratory.run.description.TreatmentCategory;
import models.utils.dao.AbstractDAODefault;

@Repository
public class TreatmentCategoryDAO extends AbstractDAODefault<TreatmentCategory> {

	public TreatmentCategoryDAO() {
		super("treatment_category",TreatmentCategory.class,true);
	}

	@Override
	protected List<String> getColumns() {
		return enumColumns;
	}

}

