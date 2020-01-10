package fr.cea.ig.ngl.utils;

import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.codelabels.CodeLabelAPI;
import models.utils.dao.DAOException;

public class NGLJavascriptGeneration {

	// We manually code the covariant overrides for little to no
	// benefit. The covariant definitions are the ones currently needed.
	// The covariant overrides will be done as needs arise.
	public static class Codes extends fr.cea.ig.lfw.utils.JavascriptGeneration.Codes {
		
		public Codes add(CodeLabelAPI a) throws DAOException, APIException {
			add(a.all(), x -> x.tableName, x -> x.code, x -> x.label);
			return this;
		}
		
	}
	
}
