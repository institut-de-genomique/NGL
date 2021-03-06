package models.laboratory.experiment.description.dao;

import models.laboratory.experiment.description.ProtocolCategory;
import models.utils.dao.AbstractDAODefault;

import org.springframework.stereotype.Repository;

@Repository
public class ProtocolCategoryDAO extends AbstractDAODefault<ProtocolCategory>{

	public ProtocolCategoryDAO() {
		super("protocol_category",ProtocolCategory.class,true);
	}

	
}
