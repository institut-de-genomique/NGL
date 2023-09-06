package fr.cea.ig.ngl.dao.experiments;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import org.mongojack.Aggregation;
import org.mongojack.Aggregation.Expression;
import org.mongojack.Aggregation.Pipeline;
import org.mongojack.DBProjection;
import org.mongojack.DBProjection.ProjectionBuilder;
import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;

import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.SubDocumentGenericAPI;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.reagent.instance.ReagentUsed;

public class ExperimentReagentsAPI extends SubDocumentGenericAPI<ReagentUsed, ExperimentsDAO, Experiment> {

	@Inject
	public ExperimentReagentsAPI(ExperimentsDAO dao) {
		super(dao);
	}

	@Override
	public Collection<ReagentUsed> getSubObjects(Experiment objectInDB) {
		return objectInDB.reagents;
	}

	@Override
	public ReagentUsed getSubObject(Experiment objectInDB, String code) {
		for(ReagentUsed ru : objectInDB.reagents) {
			if(code.equals(ru.code)){
				return ru;
			}
		}
		return null;
	}

	@Override
	public Iterable<Experiment> listObjects(String parentCode, DBQuery.Query query) {
		return dao.aggregate(aggregation(query));
	}
	
	
	/**
	 * Construct the Aggregation
	 * @param query 	query object
	 * @return pipeline
	 */
	private Pipeline<Expression<?>> aggregation(DBQuery.Query query){

		List<DBQuery.Query> stages = new ArrayList<>();
		stages.add(DBQuery.exists("reagents.0")); // Only reagents	
		
		ProjectionBuilder pb = DBProjection.include("code");
		pb.put("reagents", new String[]{"$reagents"});

		pb.put("typeCode", 1);
		pb.put("instrument", 1);
		pb.put("projectCodes", 1);
		pb.put("sampleCodes", 1);
		pb.put("traceInformation", 1);
			
		pb.put("protocolCode", 1);
		pb.put("inputContainerSupportCodes", 1);
		pb.put("outputContainerSupportCodes", 1);
		
		query.and(stages.toArray(new DBQuery.Query[stages.size()]));
	
		// Aggregate // Not working on server mongouat.genoscope.cns.fr
		Pipeline<Expression<?>> pipeline = Aggregation.match(query)
				.unwind("reagents")
				.project(pb);
		
		return pipeline;
	}

	@Override
	public ReagentUsed save(Experiment objectInDB, ReagentUsed input, String currentUser) throws APIException {
		throw new APIException(METHOD_NOT_ALLOWED_MESSAGE);
	}

	@Override
	public ReagentUsed update(Experiment objectInDB, ReagentUsed input, String currentUser) throws APIException {
		throw new APIException(METHOD_NOT_ALLOWED_MESSAGE);
	}

	@Override
	public void delete(Experiment objectInDB, String code, String currentUser) throws APIException {
		throw new APIException(METHOD_NOT_ALLOWED_MESSAGE);
	}

	public boolean isAnyReferenceInReagentUseds(String catalogCode) {
		Query isCatalogRef = DBQuery.or(
				DBQuery.is("reagents.kitCatalogCode", catalogCode),
				DBQuery.is("reagents.boxCatalogCode", catalogCode),
				DBQuery.is("reagents.reagentCatalogCode", catalogCode)
				);
		return this.dao.find(isCatalogRef).iterator().hasNext();
	}

}
