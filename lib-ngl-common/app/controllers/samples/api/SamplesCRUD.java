package controllers.samples.api;

import static fr.cea.ig.mongo.DBQueryBuilder.addDays;
import static fr.cea.ig.mongo.DBQueryBuilder.and;
import static fr.cea.ig.mongo.DBQueryBuilder.elemMatch;
import static fr.cea.ig.mongo.DBQueryBuilder.first;
import static fr.cea.ig.mongo.DBQueryBuilder.generateQueriesForExistingProperties;
import static fr.cea.ig.mongo.DBQueryBuilder.generateQueriesForProperties;
import static fr.cea.ig.mongo.DBQueryBuilder.greaterThanEquals;
import static fr.cea.ig.mongo.DBQueryBuilder.in;
import static fr.cea.ig.mongo.DBQueryBuilder.is;
import static fr.cea.ig.mongo.DBQueryBuilder.lessThan;
import static fr.cea.ig.mongo.DBQueryBuilder.notEquals;
import static fr.cea.ig.mongo.DBQueryBuilder.query;
import static fr.cea.ig.mongo.DBQueryBuilder.regex;

import java.util.Arrays;
import java.util.List;

import org.mongojack.DBQuery;

import controllers.AbstractCRUDAPIController;
import controllers.ListForm;
import controllers.authorisation.Permission;
import fr.cea.ig.play.migration.NGLContext;
import models.laboratory.common.description.Level;
import models.laboratory.sample.instance.Sample;
import models.utils.InstanceConstants;
import models.utils.dao.DAOException;
import play.mvc.Result;
import validation.ContextValidation;

public class SamplesCRUD extends AbstractCRUDAPIController<Sample> {
		
//	private static final play.Logger.ALogger logger = play.Logger.of(SamplesCRUD.class);
	
	private static final List<String> authorizedUpdateFields = 
			Arrays.asList("comments");

	public SamplesCRUD(NGLContext ctx) {
		super(ctx,InstanceConstants.SAMPLE_COLL_NAME, Sample.class, null);
//		super(ctx,InstanceConstants.SAMPLE_COLL_NAME, Sample.class, Samples2.defaultKeys);
		// Probably an early initialization and validation of the meta would be good. 
	}
	
	// -------- creation -------------
	
	@Permission(value={"writing"})
	public Result save() throws DAOException {
		return create();
	}
	
	// --------- read -----------------
	// This may fail badly as the expected get method is not the standard read method.
	@Override
	@Permission(value={"reading"})
	public Result get(String code) throws DAOException {
		return read(code); 
	}
	
	// --------- update -----------------
	
	@Override
	@Permission(value={"writing"})
	public Result update(String code) throws DAOException {
		return super.update(code);
		// throw new RuntimeException("not implemented");
	}
	
	// ------------ delete ---------------
	
	@Override
	@Permission(value={"writing"})
	public Result delete(String code) throws DAOException {
		throw new RuntimeException("not implemented");
	}
	
	// ------------ query-----------------
	
	@Permission(value={"reading"})
	public Result list() {
		return list(SamplesSearchForm.class);
	}
	
	// TODO: log errors
	@Override
	public DBQuery.Query getQuery(ContextValidation ctx, ListForm form) {
		if (form == null) {
			ctx.addError("internal", "provided search form in %s is null", getClass().getName());
			return null;			
		}
		if (!(form instanceof SamplesSearchForm)) {
			ctx.addError("internal", "search in %s does not support form %s", getClass().getName(), form.getClass().getName());
			return null;
		}
		SamplesSearchForm samplesSearch = (SamplesSearchForm)form;
		// We use the old school query to avoid testing the new one.
//		if (true)
//			Samples2.getQuery(samplesSearch);
		
		return query(
			and(first(in   ("code", samplesSearch.codes),
				      is   ("code", samplesSearch.code),
				      regex("code", samplesSearch.codeRegex)),
				in   ("typeCode",       samplesSearch.typeCodes),
				regex("referenceCollab",samplesSearch.referenceCollabRegex),
				is   ("projectCodes",   samplesSearch.projectCode),
				in   ("projectCodes",   samplesSearch.projectCodes),
				regex("life.path",      samplesSearch.treeOfLifePathRegex),
				greaterThanEquals("traceInformation.creationDate", samplesSearch.fromDate),
				lessThan("traceInformation.creationDate", addDays(samplesSearch.toDate, 1)),
				first(in("traceInformation.createUser", samplesSearch.createUsers),
				      is("traceInformation.createUser", samplesSearch.createUser)),
				elemMatch("comments", regex("comment", samplesSearch.commentRegex)),
				is("taxonCode", samplesSearch.taxonCode),
				regex("ncbiScientificName", samplesSearch.ncbiScientificNameRegex),
				first(
					and(elemMatch("processes", is("typeCode",samplesSearch.existingProcessTypeCode)),
						is       ("experiments.typeCode", samplesSearch.existingTransformationTypeCode),
						notEquals("experiments.typeCode", samplesSearch.notExistingTransformationTypeCode)),
					and(is       ("processes.experiments.typeCode", samplesSearch.existingTransformationTypeCode),
						notEquals("processes.experiments.typeCode", samplesSearch.notExistingTransformationTypeCode)),
					elemMatch("processes", and(is("typeCode",            samplesSearch.existingProcessTypeCode),
										       is("experiments.typeCode",samplesSearch.existingTransformationTypeCode))),
					elemMatch("processes", and(is       ("typeCode",           samplesSearch.existingProcessTypeCode),
										       notEquals("experiments.typeCode",samplesSearch.notExistingTransformationTypeCode))),		
					is       ("processes.typeCode",            samplesSearch.existingProcessTypeCode),
					notEquals("processes.typeCode",            samplesSearch.notExistingProcessTypeCode),
					is       ("processes.experiments.typeCode",samplesSearch.existingTransformationTypeCode),
					notEquals("processes.experiments.typeCode",samplesSearch.notExistingTransformationTypeCode)),
				in("processes.experiments.protocolCode",samplesSearch.experimentProtocolCodes),
				generateQueriesForProperties(samplesSearch.properties,Level.CODE.Sample, "properties"),
				generateQueriesForProperties(samplesSearch.experimentProperties,Level.CODE.Experiment, "processes.experiments.properties"),
				generateQueriesForExistingProperties(samplesSearch.existingFields)));
	}

	@Override
	public List<String> getAuthorizedUpdateFields() {
		return authorizedUpdateFields;
	}
	
}


