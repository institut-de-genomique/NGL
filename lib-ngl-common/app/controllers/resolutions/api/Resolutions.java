package controllers.resolutions.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;

import controllers.DocumentController;
import fr.cea.ig.MongoDBResult;
import fr.cea.ig.mongo.DBQueryBuilder;
import fr.cea.ig.ngl.NGLApplication;
import models.laboratory.resolutions.instance.Resolution;
import models.laboratory.resolutions.instance.ResolutionConfiguration;
import models.utils.InstanceConstants;
import play.data.Form;
import play.libs.Json;
import play.mvc.Result;

/**
 * Controller around ResolutionConfigurations object
 *
 */
public class Resolutions extends DocumentController<ResolutionConfiguration> {

	private final Form<ResolutionConfigurationsSearchForm> searchForm; 

//	@Inject
//	public Resolutions(NGLContext ctx) {
//		super(ctx,InstanceConstants.RESOLUTION_COLL_NAME, ResolutionConfiguration.class);	
//		searchForm = ctx.form(ResolutionConfigurationsSearchForm.class);
////		resolutionConfigurationsForm = ctx.form(ResolutionConfiguration.class);
//	}

	@Inject
	public Resolutions(NGLApplication ctx) {
		super(ctx,InstanceConstants.RESOLUTION_COLL_NAME, ResolutionConfiguration.class);	
		searchForm = ctx.form(ResolutionConfigurationsSearchForm.class);
	}

	public Result list() {
		Form<ResolutionConfigurationsSearchForm> filledForm = filledFormQueryString(searchForm, ResolutionConfigurationsSearchForm.class);
		ResolutionConfigurationsSearchForm form = filledForm.get();
		Query q = getQuery(form);
		MongoDBResult<ResolutionConfiguration> results = mongoDBFinder(form, q);			
		List<ResolutionConfiguration> resolutionConfigurations = results.toList();
		if (form.distinct) {
			Map<String, Resolution> map = new HashMap<>();			
			for (ResolutionConfiguration rc: resolutionConfigurations) {
				for (Resolution reso : rc.resolutions) {
					if (!map.containsKey(reso.code)) {
						map.put(reso.code, reso);					
					}
				}	
			}
			return ok(Json.toJson(map.values()));
		}
		return ok(Json.toJson(toListResolutions(resolutionConfigurations)));
	}

	private List<Resolution> toListResolutions(List<ResolutionConfiguration> resolutionConfigurations){
		List<Resolution> resos = new ArrayList<>();
		for(ResolutionConfiguration rc : resolutionConfigurations){
			for (Resolution reso : rc.resolutions) {
				resos.add(reso);
			}
		}
		Collections.sort(resos);
		return resos;
	}

	private Query getQuery(ResolutionConfigurationsSearchForm form) {
		List<Query> queries = new ArrayList<>();

		if (StringUtils.isNotBlank(form.typeCode)) { 
			queries.add(DBQuery.in("typeCodes", form.typeCode));
		} else if (CollectionUtils.isNotEmpty(form.typeCodes)) { 
			queries.add(DBQuery.in("typeCodes", form.typeCodes));
		}

		if (StringUtils.isNotBlank(form.objectTypeCode)) { 
			queries.add(DBQuery.is("objectTypeCode", form.objectTypeCode));
		} else if (CollectionUtils.isNotEmpty(form.objectTypeCodes)) { 
			queries.add(DBQuery.in("objectTypeCode", form.objectTypeCodes));
		}

//		Query query = null;		
//		if(queries.size() > 0){
//			query = DBQuery.and(queries.toArray(new Query[queries.size()]));
//		}
//		return query;
		return DBQueryBuilder.query(DBQueryBuilder.and(queries));
	}

}