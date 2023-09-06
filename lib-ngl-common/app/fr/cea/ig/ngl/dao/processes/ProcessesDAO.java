package fr.cea.ig.ngl.dao.processes;

import javax.inject.Inject;

import fr.cea.ig.ngl.dao.GenericMongoDAO;
import models.laboratory.processes.instance.Process;
import models.utils.InstanceConstants;

public class ProcessesDAO extends GenericMongoDAO<Process> {

	@Inject
	public ProcessesDAO() {
		super(InstanceConstants.PROCESS_COLL_NAME, Process.class);
	}

}
