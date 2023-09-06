package controllers.migration.cns;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.mongojack.DBQuery;

import models.LimsCNSDAO;
import models.laboratory.sample.instance.Sample;
import models.utils.InstanceConstants;
import play.Logger;
import play.Logger.ALogger;
import play.api.modules.spring.Spring;
import play.mvc.Result;
import services.instance.sample.UpdateSamplePropertiesCNS;
import validation.ContextValidation;
import controllers.CommonController;
import fr.cea.ig.MongoDBDAO;

public class MigrationUpdateSampleProperties  extends CommonController{

	protected static ALogger logger=Logger.of("MigrationUpdateSampleProperties");

	public static Result migration() {
		ContextValidation contextError=new ContextValidation("ngl-sq");
		
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, -1);
		Date date =  calendar.getTime();

		List<Sample> samples = MongoDBDAO.find(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, DBQuery.greaterThanEquals("traceInformation.modifyDate", date).notExists("life")).toList();
		Logger.info("Nb samples to update :"+samples.size());
		samples.parallelStream().forEach(sample -> {
			//Logger.debug("Sample "+sample.code);
			//UpdateSamplePropertiesCNS.updateOneSample(sample,contextError);
		});
		
		
		return ok("Migration update sample Finish");
	}
}
