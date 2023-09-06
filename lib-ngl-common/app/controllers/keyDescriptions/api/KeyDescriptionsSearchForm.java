package controllers.keyDescriptions.api;

import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;

import controllers.DBObjectListForm;
import models.administration.authorisation.instance.KeyDescription;

public class KeyDescriptionsSearchForm extends DBObjectListForm<KeyDescription>{

	@Override
	public Query getQuery() {
		return DBQuery.empty();
	}

}
