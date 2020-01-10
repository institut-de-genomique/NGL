package fr.cea.ig.ngl.dao.codelabels;

import javax.inject.Inject;
import javax.inject.Singleton;

import models.laboratory.common.description.CodeLabel;
import models.utils.dao.DAOException;
import play.api.modules.spring.Spring;

@Singleton
public class CodeLabelDAO {

	// Not needed, placeholder
	@Inject
	public CodeLabelDAO() {
	}
	
	public Iterable<CodeLabel> all() throws DAOException {
		return Spring.getBeanOfType(models.laboratory.common.description.dao.CodeLabelDAO.class).findAll();
	}
	
}
