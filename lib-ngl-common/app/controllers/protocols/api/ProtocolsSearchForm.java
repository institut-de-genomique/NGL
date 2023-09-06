package controllers.protocols.api;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;

import com.fasterxml.jackson.annotation.JsonIgnore;

import controllers.DBObjectListForm;
import controllers.ListObject;
import fr.cea.ig.mongo.DBQueryBuilder;
import models.laboratory.protocol.instance.Protocol;

public class ProtocolsSearchForm extends DBObjectListForm<Protocol> {
	public String experimentTypeCode;
	public List<String> experimentTypeCodes;
	public String code;
	public List<String> codes;
	public Boolean isActive;
	
	@Override
	@JsonIgnore
	public Query getQuery() {
		List<DBQuery.Query> queryElts = new ArrayList<>();

		if (CollectionUtils.isNotEmpty(this.experimentTypeCodes)) {
			queryElts.add(DBQuery.in("experimentTypeCodes",this.experimentTypeCodes));
		} else if(StringUtils.isNotBlank(this.experimentTypeCode)) {
			queryElts.add(DBQuery.in("experimentTypeCodes",this.experimentTypeCode));
		}
		
		if (CollectionUtils.isNotEmpty(this.codes)) {
			queryElts.add(DBQuery.in("code",this.codes));
		} else if(StringUtils.isNotBlank(this.code)) {
			queryElts.add(DBQuery.in("code",this.code));
		}

		if (this.isActive != null) {
			queryElts.add(DBQuery.is("active", this.isActive));
		}
		return DBQueryBuilder.query(DBQueryBuilder.and(queryElts));
	}

	@Override
	@JsonIgnore
	public Function<Protocol, ListObject> conversion() {
		return o -> { return new ListObject(o.code, o.name); };
	}

}
