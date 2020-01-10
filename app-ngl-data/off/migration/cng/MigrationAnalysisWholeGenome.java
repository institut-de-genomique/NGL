package controllers.migration.cng;


import java.util.List;

import models.Constants;
import models.LimsCNGDAO;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.run.instance.Analysis;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.sample.instance.Sample;
import models.utils.InstanceConstants;
import models.utils.dao.DAOException;
import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;
import org.mongojack.JacksonDBCollection;
import org.mongojack.WriteResult;

import org.springframework.stereotype.Repository;

import org.apache.commons.lang3.StringUtils;

import play.Logger;
import play.api.libs.json.Reads;
import play.api.modules.spring.Spring;
import play.mvc.Result;
import validation.ContextValidation;
import controllers.CommonController;
import fr.cea.ig.MongoDBDAO;

/**
 * Update contents in the Container (add missing contents, update properties)
 * This migration replaces MigrationTag (scope larger)
 * 
 * @author dnoisett
 * 04/04/2014
 */
@Repository
public class MigrationAnalysisWholeGenome extends CommonController {


	public static Result migration() {

		//Get all analysis whole genome
		List<Analysis> analysis = MongoDBDAO.find(InstanceConstants.ANALYSIS_COLL_NAME, Analysis.class, DBQuery.is("typeCode", "WG-analysis")).toList();
		analysis.stream().forEach(a->{
			//get masterReadSet
			String code = a.masterReadSetCodes.iterator().next();
			ReadSet readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, code);
			//get property
			if(readSet!=null){
				if(readSet.sampleOnContainer.properties.containsKey("expectedCoverage")){
					String valueExpectedCoverage = (String)readSet.sampleOnContainer.properties.get("expectedCoverage").getValue();
					a.properties.put("expectedCoverage", new PropertySingleValue(Double.parseDouble(valueExpectedCoverage.substring(0, valueExpectedCoverage.length()-1))));
					MongoDBDAO.update(InstanceConstants.ANALYSIS_COLL_NAME, a);
				}
			}
		});


		return ok();	
	}



}
