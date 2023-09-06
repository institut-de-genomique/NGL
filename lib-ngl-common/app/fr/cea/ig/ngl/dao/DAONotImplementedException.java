package fr.cea.ig.ngl.dao;

import models.utils.dao.DAOException;

public class DAONotImplementedException extends DAOException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public DAONotImplementedException() {
		super("not implemented");
	}
	
}
