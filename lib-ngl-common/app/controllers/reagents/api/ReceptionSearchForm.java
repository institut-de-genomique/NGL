package controllers.reagents.api;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;

import controllers.DBObjectListForm;
import models.laboratory.reagent.instance.AbstractReception;

public class ReceptionSearchForm extends DBObjectListForm<AbstractReception> {

	public String category;
	public List<String> categories;
	
	public String code;
	public List<String> codes;

	public Date fromReceptionDate;
	public Date toReceptionDate;

	public String catalogRefCode;
	public List<String> catalogRefCodes;
	
	public String kitCatalogName;
	public List<String> kitCatalogNames;
	
	public String workLabel;
	public List<String> workLabels;

	public String batchNumber;
	public List<String> batchNumbers;

	public String providerCode;
	public List<String> providerCodes;
	
	public String fromProviderId;
	public List<String> fromProviderIds;

	public Date fromExpirationDate;
	public Date toExpirationDate;

	public String stateCode;
	public List<String> stateCodes;

	public Date fromCreationDate;
	public Date toCreationDate;
	
	public String createUser;

	public String experimentTypeCode;
	
	
	@Override
    public Query getQuery() {

		final List<Query> ands = new ArrayList<>(16);

		if(StringUtils.isNotBlank(this.category)) {
			ands.add(DBQuery.is("category", this.category));
    	} else if(CollectionUtils.isNotEmpty(this.categories)) {
			ands.add(DBQuery.in("category", this.categories));
    	}
    	
    	if (StringUtils.isNotBlank(this.code)) {
			ands.add(DBQuery.is("code", this.code));
		} else if(CollectionUtils.isNotEmpty(this.codes)) {
			ands.add(DBQuery.in("code", this.codes));
		}

		if (this.fromReceptionDate != null) {
			ands.add(DBQuery.greaterThanEquals("receptionDate", this.fromReceptionDate));
		}

		if (this.toReceptionDate != null) {
			ands.add(DBQuery.lessThanEquals("receptionDate", this.toReceptionDate));
		}

		if (StringUtils.isNotBlank(this.catalogRefCode)) {
			ands.add(DBQuery.is("catalogRefCode", this.catalogRefCode));
		} else if(CollectionUtils.isNotEmpty(this.catalogRefCodes)) {
			ands.add(DBQuery.in("catalogRefCode", this.catalogRefCodes));
		}
		
		if (StringUtils.isNotBlank(this.kitCatalogName)) {
			ands.add(DBQuery.regex("kitCatalogName", Pattern.compile(this.kitCatalogName)));
		} else if(CollectionUtils.isNotEmpty(this.kitCatalogNames)) {
			ands.add(DBQuery.in("kitCatalogName", this.kitCatalogNames));
		}
		
		if (StringUtils.isNotBlank(this.workLabel)) {
			ands.add(DBQuery.regex("workLabel", Pattern.compile(this.workLabel)));
		} else if(CollectionUtils.isNotEmpty(this.workLabels)) {
			ands.add(DBQuery.in("workLabel", this.workLabels));
		}

		if (StringUtils.isNotBlank(this.batchNumber)) {
			ands.add(DBQuery.is("batchNumber", this.batchNumber));
		} else if(CollectionUtils.isNotEmpty(this.batchNumbers)) {
			ands.add(DBQuery.in("batchNumber", this.batchNumbers));
		}

		if (StringUtils.isNotBlank(this.providerCode)) {
			ands.add(DBQuery.is("providerCode", this.providerCode));
		} else if(CollectionUtils.isNotEmpty(this.providerCodes)) {
			ands.add(DBQuery.in("providerCode", this.providerCodes));
		}

		if (StringUtils.isNotBlank(this.fromProviderId)) {
			ands.add(DBQuery.is("fromProviderId", this.fromProviderId));
		} else if(CollectionUtils.isNotEmpty(this.fromProviderIds)) {
			ands.add(DBQuery.in("fromProviderId", this.fromProviderIds));
		}

		if (this.fromExpirationDate != null) {
			ands.add(DBQuery.greaterThanEquals("expirationDate", this.fromExpirationDate));
		}

		if (this.toExpirationDate != null) {
			ands.add(DBQuery.lessThanEquals("expirationDate", this.toExpirationDate));
		}

		if (StringUtils.isNotBlank(this.stateCode)) {
			ands.add(DBQuery.is("state.code", this.stateCode));
		} else if(CollectionUtils.isNotEmpty(this.stateCodes)) {
			ands.add(DBQuery.in("state.code", this.stateCodes));
		}

		if (this.fromCreationDate != null) {
			ands.add(DBQuery.greaterThanEquals("traceInformation.creationDate", this.fromCreationDate));
		}

		if (this.toCreationDate != null) {
			ands.add(DBQuery.lessThan("traceInformation.creationDate", (DateUtils.addDays(this.toCreationDate, 1))));
		}
		
		if (StringUtils.isNotBlank(this.createUser)) {
			ands.add(DBQuery.is("traceInformation.createUser", this.createUser));
		}

		return ands.isEmpty() ? DBQuery.empty() : DBQuery.and(ands.stream().toArray(Query[]::new));

    }

}
