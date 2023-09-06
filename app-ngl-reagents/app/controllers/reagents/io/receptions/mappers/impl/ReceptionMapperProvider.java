package controllers.reagents.io.receptions.mappers.impl;

import controllers.reagents.io.receptions.mappers.AbstractReceptionMapper;
import fr.cea.ig.ngl.dao.api.APIException;
import validation.ContextValidation;

public class ReceptionMapperProvider {
	
	private void require(ContextValidation contextValidation) throws APIException {
		if(contextValidation == null) {
			throw new APIException("ContextValidation cannot be null");
		}
	}
	
	public IlluminaReceptionMapper illuminaReceptionMapper(ContextValidation contextValidation) throws APIException {
		require(contextValidation);
		return new IlluminaReceptionMapper(contextValidation);
	}
	
	public NanoporeReceptionMapper nanoporeReceptionMapper(ContextValidation contextValidation) throws APIException {
		require(contextValidation);
		return new NanoporeReceptionMapper(contextValidation);
	}
	
	public AbstractReceptionMapper getReceptionMapper(String importFormat, ContextValidation contextValidation) throws APIException {
		switch(importFormat) {
		case IlluminaReceptionMapper.IMPORT_TYPE_FILE: return illuminaReceptionMapper(contextValidation);
		case NanoporeReceptionMapper.IMPORT_TYPE_FILE: return nanoporeReceptionMapper(contextValidation);
		default: throw new APIException("Unknow import format : " + String.valueOf(importFormat));
		}
	}

}
