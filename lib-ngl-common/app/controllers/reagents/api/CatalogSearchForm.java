package controllers.reagents.api;

import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;

import controllers.DBObjectListForm;
import models.laboratory.reagent.description.AbstractCatalog;

public class CatalogSearchForm extends DBObjectListForm<AbstractCatalog> {

	@Override
	public Query getQuery() {
		return DBQuery.empty();
	}

}
