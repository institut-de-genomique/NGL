package fr.cea.ig.ngl.dao.readsets;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.jongo.Aggregate;
import org.jongo.MongoCollection;
import org.jongo.Aggregate.ResultsIterator;
import org.mongojack.DBQuery.Query;

import com.mongodb.AggregationOptions;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.ngl.dao.GenericMongoDAO;
import models.laboratory.run.instance.ReadSet;
import models.utils.InstanceConstants;
import play.modules.jongo.MongoDBPlugin;

@Singleton
public class ReadSetsDAO extends GenericMongoDAO<ReadSet> {
	
	@Inject
	public ReadSetsDAO() {
		super(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class);
	}

    public void deleteObject(Query query) {
        MongoDBDAO.delete(getCollectionName(), getElementClass(), query);
    }

    @Override
    public ResultsIterator<ReadSet> findByAggregate(String query) {
        MongoCollection collection = MongoDBPlugin.getCollection(getCollectionName());
        String[] pipeline = getAggregatePipeline(query);
        Aggregate aggregateQuery = collection.aggregate("{"+pipeline[0]+"}");
        if(pipeline.length>1){
            for(int i=1; i<pipeline.length;i++){
                aggregateQuery.and("{"+pipeline[i]+"}");
            }
        }
       ResultsIterator<ReadSet> all = aggregateQuery.options(AggregationOptions.builder()
                                                                               .outputMode(AggregationOptions.OutputMode.CURSOR)
                                                                               .build())
                                                    .as(getElementClass());
        return all;
    }
    
    private String[] getAggregatePipeline(String reportingQuery){
        reportingQuery=reportingQuery.replaceAll("\n", "");
        reportingQuery=reportingQuery.substring(1, reportingQuery.length()-1);
        String[] pipeline = reportingQuery.split("\\},\\{");
        return pipeline;   
    }
    
}
