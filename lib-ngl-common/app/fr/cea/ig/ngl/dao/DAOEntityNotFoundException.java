package fr.cea.ig.ngl.dao;

import models.utils.dao.DAOException;

public class DAOEntityNotFoundException extends DAOException  {

	/**
	 * Eclipse requested. 
	 */
	private static final long serialVersionUID = 1L;

	public DAOEntityNotFoundException() {}
	public DAOEntityNotFoundException(String message) { super(message); }
	public DAOEntityNotFoundException(String message, Throwable t) { super(message,t); }
	
}
