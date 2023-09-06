package scripts;

import java.util.List;

import org.mongojack.DBQuery;
import org.springframework.stereotype.Repository;

import com.mongodb.MongoException;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptNoArgs;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.run.instance.Analysis;
import models.laboratory.run.instance.ReadSet;
import models.utils.InstanceConstants;
import play.Logger;

/**
 * Update contents in the Container (add missing contents, update properties)
 * This migration replaces MigrationTag (scope larger)
 * 
 * @author dnoisett
 * 04/04/2014
 */
@Repository
public class MigrationAnalysisWholeGenome extends ScriptNoArgs {



	@Override
	public void execute() throws Exception {
		//Update expectedCoverage
		List<Analysis> analysis = MongoDBDAO.find(InstanceConstants.ANALYSIS_COLL_NAME, Analysis.class, DBQuery.is("typeCode", "WG-analysis").notExists("properties.expectedCoverage")).toList();
		Logger.debug("Nb analysis to update expectedCoverage "+analysis.size());
		analysis.stream().forEach(a->{
			//get masterReadSet
			String code = a.masterReadSetCodes.iterator().next();
			ReadSet readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, code);
			//get property
			if(readSet!=null){
				if(readSet.sampleOnContainer.properties.containsKey("expectedCoverage")){
					String valueExpectedCoverage = (String)readSet.sampleOnContainer.properties.get("expectedCoverage").getValue();
					if(valueExpectedCoverage!=null){
						try {
							a.properties.put("expectedCoverage", new PropertySingleValue(Double.parseDouble(valueExpectedCoverage.substring(0, valueExpectedCoverage.length()-1))));
							MongoDBDAO.update(InstanceConstants.ANALYSIS_COLL_NAME, a);
						} catch (NumberFormatException e) {
							Logger.debug("Number format exception "+valueExpectedCoverage);
						} catch (MongoException e) {
							Logger.debug("MongoException "+e.getMessage());
						}
					}
				}
			}
		});

		//Update gender
		analysis = MongoDBDAO.find(InstanceConstants.ANALYSIS_COLL_NAME, Analysis.class, DBQuery.is("typeCode", "WG-analysis").notExists("properties.gender")).toList();
		Logger.debug("Nb analysis to update gender "+analysis.size());
		analysis.stream().forEach(a->{
			//get masterReadSet
			String code = a.masterReadSetCodes.iterator().next();
			ReadSet readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, code);
			//get property
			if(readSet!=null){
				if(readSet.sampleOnContainer.properties.containsKey("gender")){
					String valueGender = (String)readSet.sampleOnContainer.properties.get("gender").getValue();
					if(valueGender!=null){
						a.properties.put("gender", new PropertySingleValue(valueGender));
						MongoDBDAO.update(InstanceConstants.ANALYSIS_COLL_NAME, a);
					}
				}
			}
		});
		Logger.debug("End update");
	}



}
