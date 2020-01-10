package rules;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.run.instance.Lane;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.run.instance.Run;
import models.laboratory.run.instance.Treatment;
import models.utils.InstanceConstants;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mongojack.DBQuery;

import play.Play;
import rules.services.RulesException;
import rules.services.RulesServices;
import rules.services.RulesServices6;
import utils.AbstractTestsCNG;
import utils.AbstractTestsCNS;
import fr.cea.ig.MongoDBDAO;

public class StatRGTest extends AbstractTestsCNS{

	private static Run runData ;

	@BeforeClass
	public static void getRunData()
	{
		Run runDataInit = MongoDBDAO.findOne("ngl_bi.RunIllumina_initData", Run.class, DBQuery.is("code", "121107_HISEQ1_D094VACXX"));
		runData = MongoDBDAO.findOne(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, DBQuery.is("code", "121107_HISEQ1_D094VACXX"));
		if(runData!=null){
			MongoDBDAO.delete(InstanceConstants.RUN_ILLUMINA_COLL_NAME, runData);
		}
		MongoDBDAO.save(InstanceConstants.RUN_ILLUMINA_COLL_NAME, runDataInit);
		runData=runDataInit;
		
		for(Lane lane : runData.lanes){
			for(String readSetCode : lane.readSetCodes){
				ReadSet readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSetCode);
				if(readSet!=null){
					MongoDBDAO.delete(InstanceConstants.READSET_ILLUMINA_COLL_NAME, readSet);
				}
				//Get readSet from ReadSet_initData and copy in ReadSet collection
				ReadSet readSetInitData = MongoDBDAO.findByCode("ngl_bi.ReadSetIllumina_initData", ReadSet.class, readSetCode);
				MongoDBDAO.save(InstanceConstants.READSET_ILLUMINA_COLL_NAME, readSetInitData);
			}
		}
	}

	
	@Test
	public void testStatQC() throws RulesException, ParseException
	{


		RulesServices6 rulesServices = RulesServices6.getInstance();
		List<Object> facts = new ArrayList<Object>();
		facts.add(runData);
		
		rulesServices.callRules(Play.application().configuration().getString("rules.key"), "rg_1", facts);
		
		runData = MongoDBDAO.findOne(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, DBQuery.is("code", "121107_HISEQ1_D094VACXX"));
		
		//Check percentClusterIlluminaFilter for run
		//Calculate percentClusterIlluminaFilter
		long nbClusterIlluminaFilter = ((PropertyValue<Long>)runData.treatments.get("ngsrg").results.get("default").get("nbClusterIlluminaFilter")).value;
		long nbClusterTotal = ((PropertyValue<Long>)runData.treatments.get("ngsrg").results.get("default").get("nbClusterTotal")).value;
		double percentClusterForRun = roundValue((double)nbClusterIlluminaFilter/nbClusterTotal*100);
		double percentDB = ((PropertyValue<Double>)runData.treatments.get("ngsrg").results.get("default").get("percentClusterIlluminaFilter")).value;
		Assert.assertEquals(percentClusterForRun, percentDB);
		
		//Check value for lane 5
		for(Lane lane : runData.lanes){
			Treatment treatRG = lane.treatments.get("ngsrg");
			Map<String,PropertyValue> results = treatRG.results.get("default");
			//Get percentClusterIlluminaFilter
			Assert.assertTrue(results.containsKey("seqLossPercent"));
			if(lane.number==5){
				//Check for readSetCode E421_FE_B00FS5U_5_D094VACXX.IND1
				ReadSet readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, "E421_FE_B00FS5U_5_D094VACXX.IND1");
				Treatment treatReadSetRG = readSet.treatments.get("ngsrg");
				Map<String, PropertyValue> resultsRS = treatReadSetRG.results.get("default");
				Assert.assertEquals(((Double)resultsRS.get("validSeqPercent").value).doubleValue(),31.27);
				double valueSeqLossPercent = ((Double)results.get("seqLossPercent").value).doubleValue();
				Assert.assertEquals(valueSeqLossPercent,1.33);
			}
		}
		
		
	}
	
	
	
	
}


