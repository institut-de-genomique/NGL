package controllers.reagents.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.regex.Pattern;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBCursor;
import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;

import controllers.DBObjectListForm;
import fr.cea.ig.MongoDBDAO;
import models.laboratory.reagent.description.AbstractCatalog;
import models.laboratory.reagent.description.BoxCatalog;
import models.laboratory.reagent.description.ReagentCatalog;
import models.laboratory.reagent.instance.AbstractReception;
import models.utils.InstanceConstants;

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
	public List<String> experimentTypeCodes;	
	
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
		
		if (StringUtils.isNotBlank(this.fromProviderId)) {
			ands.add(DBQuery.is("fromProviderId", this.fromProviderId));
		} else if(CollectionUtils.isNotEmpty(this.fromProviderIds)) {
			ands.add(DBQuery.in("fromProviderId", this.fromProviderIds));
		}
		
		if (StringUtils.isNotBlank(this.experimentTypeCode)) {
			ands.add(this.searchByExperimentTypes(Pattern.compile(this.experimentTypeCode), DBQuery::regex));
		} else if(CollectionUtils.isNotEmpty(this.experimentTypeCodes)) {
			ands.add(this.searchByExperimentTypes(this.experimentTypeCodes, DBQuery::in));
		}

		return ands.isEmpty() ? DBQuery.empty() : DBQuery.and(ands.stream().toArray(Query[]::new));

    }
	
	/**
	 * 
	 * @param <T> string, List, regex...
	 * @param experimentTypeCodes
	 * @param queryBuilder build query from <T> experimentTypeCodes
	 * @return Kit catalogs
	 */
	private <T> DBCursor<AbstractCatalog> getKitCatalogsByExperimentTypeCodes(T experimentTypeCodes, BiFunction<String, T, Query> queryBuilder) {
		return MongoDBDAO.find(InstanceConstants.REAGENT_CATALOG_COLL_NAME, AbstractCatalog.class, 
				DBQuery.and(
					DBQuery.is("category", "Kit"), 
					queryBuilder.apply("experimentTypeCodes", experimentTypeCodes)
				)
			).cursor;
	}
	
	/**
	 * 
	 * @param kitCatalogCodes
	 * @return Reagent and Box catalogs
	 */
	private DBCursor<AbstractCatalog> getCatalogsByKitCatalogCodes(Collection<String> kitCatalogCodes) {
		return MongoDBDAO.find(InstanceConstants.REAGENT_CATALOG_COLL_NAME, AbstractCatalog.class, 
				DBQuery.and(
					DBQuery.in("category", "Box", "Reagent"),
					DBQuery.in("kitCatalogCode", kitCatalogCodes)
				)
			).cursor;
	}
	
	/**
	 * 
	 * @param catalog
	 * @return code
	 */
	private String getKitCatalogCode(AbstractCatalog catalog) {
		if (catalog instanceof BoxCatalog) {
			return ((BoxCatalog) catalog).kitCatalogCode;
		} else if (catalog instanceof ReagentCatalog) {
			return ((ReagentCatalog) catalog).kitCatalogCode;
		} else {
			throw new IllegalStateException("Catalog '" + catalog.code + "' is not Box or Reagent!");
		}
	}
	
	/**
	 * 
	 * @param kitCatalogMap
	 * @return ors queries
	 */
	private List<Query> searchBykitCatalogMap(Map<String, String> kitCatalogMap) {
		
		final List<Query> ors = new ArrayList<>();
		// search reagents and box catalogs with kitCatalogCode
		this.getCatalogsByKitCatalogCodes(kitCatalogMap.keySet())
		// build receptions queries by kitCatalogName
		.forEach((AbstractCatalog catalog) -> {
			ors.add(
					DBQuery.and(
							DBQuery.is("kitCatalogName", kitCatalogMap.get(this.getKitCatalogCode(catalog))),
							DBQuery.is("catalogRefCode", catalog.catalogRefCode)
					)
			);
		});
		
		return ors;
		
	}
	
	/**
	 * Whenever it is used, this Query will return no results
	 */
	private static final Query NO_RESULT = DBQuery.in("catalogRefCode", new Object[0]);
	
	/**
	 * 
	 * @param <T> string, List, regex...
	 * @param experimentTypeCodes
	 * @param query the queryBuilder for experimentTypeCodes
	 * @return query to add in ands
	 */
	private <T> Query searchByExperimentTypes(T experimentTypeCodes, BiFunction<String, T, Query> query) {
		
		final Map<String, String> kitCatalogMap = new HashMap<>();
		// fill the KitCatalogMap
		this.getKitCatalogsByExperimentTypeCodes(experimentTypeCodes, query)
		.forEach((AbstractCatalog catalog) -> {
			kitCatalogMap.put(catalog.code, catalog.name);
		});
		// if no mapping, then query should return no result
		if(kitCatalogMap.isEmpty()) {
			return NO_RESULT;
		}
		// search by mapping
		List<Query> ors = this.searchBykitCatalogMap(kitCatalogMap);
		
		switch(ors.size()) {
		case 0: return NO_RESULT;
		case 1: return ors.get(0);
		default: return DBQuery.or(ors.stream().toArray(Query[]::new));
		}
	}

}
