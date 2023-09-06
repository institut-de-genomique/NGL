package fr.cea.ig.ngl.dao.parameters;

import fr.cea.ig.ngl.dao.GenericMongoDAO;
import models.utils.InstanceConstants;
import models.laboratory.parameter.Parameter;



public class ParametersDAO extends GenericMongoDAO<Parameter> {
	
	public ParametersDAO() {
		super(InstanceConstants.PARAMETER_COLL_NAME, Parameter.class);
	}

}
