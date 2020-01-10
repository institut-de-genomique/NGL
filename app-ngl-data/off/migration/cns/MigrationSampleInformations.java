package controllers.migration.cns;

import java.util.List;

import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import models.laboratory.container.instance.Container;
import models.laboratory.sample.instance.Sample;
import models.utils.InstanceConstants;
import play.Logger;
import play.mvc.Result;
import services.instance.sample.UpdateSamplePropertiesCNS;
import validation.ContextValidation;
import controllers.CommonController;
import fr.cea.ig.DBObject;
import fr.cea.ig.MongoDBDAO;

public class MigrationSampleInformations extends CommonController {

	public static Result migration() {
	
		updateSampleInformations();
		
		//SUPSQ-2427
		//updateSampleBWX();
		
		return ok("Migration Sample properties finish");
	}

	private static void updateSampleBWX() {
		List<Sample> samples=MongoDBDAO.find(InstanceConstants.SAMPLE_COLL_NAME, Sample.class,DBQuery.in("projectCodes","BWX")).toList();
		Logger.debug("Sample "+samples.size());
		
		for(Sample s:samples){
			Integer taxonSize=Integer.valueOf(s.properties.get("taxonSize").value.toString())*1000;
			s.properties.get("taxonSize").value=taxonSize;
			MongoDBDAO.update(InstanceConstants.SAMPLE_COLL_NAME, Sample.class,DBQuery.is("code",s.code),DBUpdate.set("properties.taxonSize", s.properties.get("taxonSize")));
			
			MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class, DBQuery.is("contents.sampleCode", s.code))
			.cursor.forEach(container -> {
				container.contents.stream()
					.filter(content -> content.sampleCode.equals(s.code))
					.forEach(content -> {
						content.properties.put("taxonSize",s.properties.get("taxonSize"));
					});;
				MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME, container);	
			});;
			
			
		}
	}

	private static void updateSampleInformations() {
		ContextValidation contextError=new ContextValidation("ngl");
		
		Logger.debug("Update from UpdateSample");
		MongoDBDAO.find("UpdateSample",Container.class,DBQuery.empty()).cursor.forEach(o -> {
			if(null != o.code){
				Sample sample = MongoDBDAO.findOne(InstanceConstants.SAMPLE_COLL_NAME,Sample.class,DBQuery.is("code",o.code));
				Logger.debug("update "+sample.code);
				//UpdateSamplePropertiesCNS.updateOneSample(sample, contextError);
			}
		});
		
		Logger.debug("Update from BWX");
		MongoDBDAO.find(InstanceConstants.SAMPLE_COLL_NAME, Sample.class,DBQuery.in("projectCodes","BWX")).cursor.forEach(s -> {
			Long taxonSize=Long.valueOf(s.properties.get("taxonSize").value.toString())*1000;
			s.properties.get("taxonSize").value=taxonSize;
			MongoDBDAO.update(InstanceConstants.SAMPLE_COLL_NAME, Sample.class,DBQuery.is("code",s.code),DBUpdate.set("properties.taxonSize", s.properties.get("taxonSize")));
			Logger.debug("update "+s.code);
			//UpdateSamplePropertiesCNS.updateOneSample(s, contextError);
		});
		/*
		List<Sample> samples = MongoDBDAO.find(InstanceConstants.SAMPLE_COLL_NAME,Sample.class,DBQuery.empty()).toList();
		//List<Sample> samples = MongoDBDAO.find(InstanceConstants.SAMPLE_COLL_NAME,Sample.class,DBQuery.is("code","BKP_EB")).toList();
		for(Sample s:samples){
			UpdateSamplePropertiesCNS.updateOneSample(s, contextError);
		}
		*/
	}
	
	
}


