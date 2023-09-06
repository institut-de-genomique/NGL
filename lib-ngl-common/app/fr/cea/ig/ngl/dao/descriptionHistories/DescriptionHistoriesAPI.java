package fr.cea.ig.ngl.dao.descriptionHistories;
import java.util.List;

import javax.inject.Inject;

import org.mongojack.DBQuery;

import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.api.GenericAPI;
import models.laboratory.descriptionHistory.instance.DescriptionHistory;
import validation.ContextValidation;



public class DescriptionHistoriesAPI extends GenericAPI<DescriptionHistoriesDAO,DescriptionHistory> {
    @Inject
	public DescriptionHistoriesAPI(DescriptionHistoriesDAO dao) {
		super(dao);
	}
  

	@Override
	protected List<String> authorizedUpdateFields() {
		return null;
	}

	@Override
	protected List<String> defaultKeys() {
		return null;
	}

	@Override
	public DescriptionHistory create(DescriptionHistory input, String currentUser)
			throws APIValidationException, APIException {
				ContextValidation ctxVal = ContextValidation.createCreationContext(currentUser); 
				input.validate(ctxVal);
			if (ctxVal.hasErrors()){
				throw new APIValidationException(INVALID_INPUT_ERROR_MSG, ctxVal.getErrors());
			};
					
		return dao.saveObject(input);
	}

	@Override
	public DescriptionHistory update(DescriptionHistory input, String currentUser)
			throws APIException, APIValidationException {
		return null;
	}

	@Override
	public DescriptionHistory update(DescriptionHistory input, String currentUser, List<String> fields)
			throws APIException, APIValidationException {
		return null;
	}

	public List<DescriptionHistory> getAllHistory(String code) {
		return dao.findAsList(DBQuery.is("code", code));
	}
}
