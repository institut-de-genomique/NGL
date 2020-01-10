package fr.cea.ig.ngl.dao.sra;

import javax.inject.Inject;

import models.sra.submit.common.instance.Study;
import models.utils.InstanceConstants;
import fr.cea.ig.ngl.dao.GenericMongoDAO;

public class StudyDAO extends GenericMongoDAO<Study> {
	
	@Inject
	public StudyDAO() {
		super(InstanceConstants.SRA_STUDY_COLL_NAME, Study.class);
	}
	
}
