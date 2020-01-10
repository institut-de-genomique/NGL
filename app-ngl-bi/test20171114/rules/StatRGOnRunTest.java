package rules;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mongojack.DBQuery;

import fr.cea.ig.MongoDBDAO;
import models.laboratory.common.instance.property.PropertyObjectValue;
import models.laboratory.run.instance.Run;
import models.utils.InstanceConstants;
import play.Play;
import rules.services.RulesServices6;
import utils.AbstractTestsCNS;

public class StatRGOnRunTest extends AbstractTestsCNS{

	private static Run runData;
	private static String codeRun = "151117_MN15794_FAA71079_A";
	
	@BeforeClass
	public static void getReadSetData()
	{
		Run runInit = MongoDBDAO.findOne("ngl_bi.RunIllumina_initData", Run.class, DBQuery.is("code", codeRun));
		runData = MongoDBDAO.findOne(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, DBQuery.is("code", codeRun));
		if(runData!=null){
			MongoDBDAO.delete(InstanceConstants.RUN_ILLUMINA_COLL_NAME, runData);
		}
		MongoDBDAO.save(InstanceConstants.RUN_ILLUMINA_COLL_NAME, runInit);
		runData=runInit;
		
	}
	
	@Test
	public void shouldReadsBasesPercentsCalculated()
	{
		RulesServices6 rulesServices = RulesServices6.getInstance();
		List<Object> facts = new ArrayList<Object>();
		facts.add(runData);
		
		rulesServices.callRules(Play.application().configuration().getString("rules.key"), "F_RG_1", facts);
		
		//Check data in database
		Run runDB = MongoDBDAO.findOne(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, DBQuery.is("code", codeRun));
		Assert.assertNotNull(runDB);
		
		Double valueReadsPercent = (Double)((PropertyObjectValue)runDB.treatments.get("ngsrg").results.get("default").get("useful")).value.get("readsPercent");
		Assert.assertNotNull(valueReadsPercent);
		Double valueBasesPercent = (Double)((PropertyObjectValue)runDB.treatments.get("ngsrg").results.get("default").get("useful")).value.get("basesPercent");
		Assert.assertNotNull(valueBasesPercent);
		
		valueReadsPercent = (Double)((PropertyObjectValue)runDB.treatments.get("ngsrg").results.get("default").get("1DReverse")).value.get("readsPercent");
		Assert.assertNotNull(valueReadsPercent);
		valueBasesPercent = (Double)((PropertyObjectValue)runDB.treatments.get("ngsrg").results.get("default").get("1DReverse")).value.get("basesPercent");
		Assert.assertNotNull(valueBasesPercent);
		
		valueReadsPercent = (Double)((PropertyObjectValue)runDB.treatments.get("ngsrg").results.get("default").get("1DForward")).value.get("readsPercent");
		Assert.assertNotNull(valueReadsPercent);
		valueBasesPercent = (Double)((PropertyObjectValue)runDB.treatments.get("ngsrg").results.get("default").get("1DForward")).value.get("basesPercent");
		Assert.assertNotNull(valueBasesPercent);
		
		valueReadsPercent = (Double)((PropertyObjectValue)runDB.treatments.get("ngsrg").results.get("default").get("2DPass")).value.get("readsPercent");
		Assert.assertNotNull(valueReadsPercent);
		valueBasesPercent = (Double)((PropertyObjectValue)runDB.treatments.get("ngsrg").results.get("default").get("2DPass")).value.get("basesPercent");
		Assert.assertNotNull(valueBasesPercent);
	}
}
