package controllers.projects.api;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;

import com.fasterxml.jackson.annotation.JsonIgnore;

import controllers.DBObjectListForm;
import controllers.ListObject;
import controllers.NGLControllerHelper;
import models.laboratory.common.description.Level;
import models.laboratory.project.instance.UmbrellaProject;

public class UmbrellaProjectsSearchForm  extends DBObjectListForm<UmbrellaProject> {

	public List<String> codes;
	public String code;
	public List<String> names;
	public String name;
	public String applicant;
	public Date fromDate;
	
	public Date toDate;
	public Map<String, List<String>> properties = new HashMap<>();

	@Override
	@JsonIgnore
	public Query getQuery() {
		List<Query> queries = new ArrayList<>();
		Query query = null;
		
		if (CollectionUtils.isNotEmpty(codes)) {
			queries.add(DBQuery.in("code", codes));
		} else if(StringUtils.isNotBlank(code)) {
			queries.add(DBQuery.is("code", code));
		}

		if (CollectionUtils.isNotEmpty(names)) {
			queries.add(DBQuery.in("name", names));
		} else if(StringUtils.isNotBlank(name)) {
			queries.add(DBQuery.is("name", name));
		}

		if(StringUtils.isNotBlank(applicant)) {
			queries.add(DBQuery.is("properties.applicant.value", applicant));
		}

		if(null != this.fromDate){
			queries.add(DBQuery.greaterThanEquals("traceInformation.creationDate", this.fromDate));
		}

		if(null != this.toDate){
			queries.add(DBQuery.lessThan("traceInformation.creationDate", (DateUtils.addDays(this.toDate, 1))));
		}

		queries.addAll(NGLControllerHelper.generateQueriesForProperties(this.properties, Level.CODE.Project, "properties"));
		
		if(queries.size() > 0){
			query = DBQuery.and(queries.toArray(new Query[queries.size()]));
		}

		return query;
	}

	@Override
	@JsonIgnore
	public Function<UmbrellaProject, ListObject> conversion() {
		return o -> { return new ListObject(o.code, o.name); };
	}
}
