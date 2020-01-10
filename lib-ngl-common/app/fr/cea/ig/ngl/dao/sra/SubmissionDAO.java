package fr.cea.ig.ngl.dao.sra;

import javax.inject.Inject;

import models.sra.submit.common.instance.Submission;
import models.utils.InstanceConstants;
import fr.cea.ig.ngl.dao.GenericMongoDAO;

public class SubmissionDAO extends GenericMongoDAO<Submission> {
	
	@Inject
	public SubmissionDAO() {
		super(InstanceConstants.SRA_SUBMISSION_COLL_NAME, Submission.class);
	}
}
