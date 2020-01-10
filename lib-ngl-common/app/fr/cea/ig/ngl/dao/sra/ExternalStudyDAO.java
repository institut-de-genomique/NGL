package fr.cea.ig.ngl.dao.sra;

import javax.inject.Inject;

import fr.cea.ig.ngl.dao.GenericMongoDAO;
import models.sra.submit.common.instance.ExternalStudy;
import models.utils.InstanceConstants;

public class ExternalStudyDAO extends GenericMongoDAO<ExternalStudy> {

	@Inject
	public ExternalStudyDAO() {
		super(InstanceConstants.SRA_STUDY_COLL_NAME, ExternalStudy.class);
	}

}
