package fr.cea.ig.ngl.dao.sra;

import javax.inject.Inject;

import models.sra.submit.common.instance.AbstractStudy;
import models.utils.InstanceConstants;
import fr.cea.ig.ngl.dao.GenericMongoDAO;

public class AbstractStudyDAO extends GenericMongoDAO<AbstractStudy> {

	@Inject
	public AbstractStudyDAO() {
		super(InstanceConstants.SRA_STUDY_COLL_NAME, AbstractStudy.class);
	}

}
