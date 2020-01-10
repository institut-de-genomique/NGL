package rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import models.laboratory.alert.instance.Alert;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.run.instance.Lane;
import models.laboratory.run.instance.Run;
import models.laboratory.run.instance.Treatment;
import models.utils.InstanceConstants;

import org.drools.runtime.StatefulKnowledgeSession;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mongojack.DBQuery;

import play.Logger;
import play.Play;
import rules.services.RulesException;
import rules.services.RulesServices;
import rules.services.RulesServices6;
import utils.AbstractTestsCNG;
import alert.AlertSAVInfo;
import fr.cea.ig.MongoDBDAO;

public class AlertSAVTests extends AbstractTestsCNG{

	private static Run runData ;

	@BeforeClass
	public static void getRunData()
	{
		runData = MongoDBDAO.findOne("ngl_bi.RunIllumina_initData", Run.class, DBQuery.is("code", "121203_HISEQ7_D1DAGACXX"));
	}
	
	@AfterClass
	public static void deleteAlert()
	{
		List<Alert> alerts = MongoDBDAO.find(InstanceConstants.ALERT_COLL_NAME, Alert.class).toList();
		for(Alert alert : alerts){
			MongoDBDAO.delete(InstanceConstants.ALERT_COLL_NAME, alert);
		}
		
	}

	@Test
	public void testCompletBadAndFlag() throws RulesException
	{
		//Create run data with all bad
		Run runBad = runData;

		Lane lane1 = null;
		Lane lane2 = null;
		for(Lane lane : runBad.lanes){
			if(lane.number==1)
				lane1 = lane;
			else if(lane.number==2)
				lane2=lane;
		}
		//Lane 1
		Treatment treatSAV1 = lane1.treatments.get("sav");
		//Read1
		Map<String, PropertyValue> result1Read1 = treatSAV1.results.get("read1");
		//Read2
		Map<String, PropertyValue> result1Read2 = treatSAV1.results.get("read2");
		//Lane 2
		Treatment treatSAV2 = lane2.treatments.get("sav");
		//Read1
		Map<String, PropertyValue> result2Read1 = treatSAV2.results.get("read1");
		//Read2
		Map<String, PropertyValue> result2Read2 = treatSAV2.results.get("read2");

		//BAD result
		//clusterDensity <200 for read1
		result1Read1.put("clusterDensity", new PropertySingleValue(new Long(150), "unit"));
		//clusterDensity >1000 for read2
		result1Read2.put("clusterDensity", new PropertySingleValue(new Long(1010), "unit"));
		//clusterPFPerc < 75%
		result1Read1.put("clusterPFPerc", new PropertySingleValue(new Double(70), "unit"));
		//greaterQ30Perc < 80
		result1Read1.put("greaterQ30Perc", new PropertySingleValue(new Double(75), "unit"));
		//phasing > 0.5
		result1Read1.put("phasing", new PropertySingleValue(new Double(0.6), "unit"));
		//prephasing > 0.5
		result1Read1.put("prephasing", new PropertySingleValue(new Double(0.6), "unit"));
		//alignedPerc < 0.1
		result1Read1.put("alignedPerc", new PropertySingleValue(new Double(0.05), "unit"));
		//errorRatePerc > 1
		result1Read1.put("errorRatePerc", new PropertySingleValue(new Double(1.1), "unit"));
		//errorRatePercCycle35 > 1
		result1Read1.put("errorRatePercCycle35", new PropertySingleValue(new Double(1.1),"unit"));
		//errorRatePercCycle75 > 1
		result1Read1.put("errorRatePercCycle75", new PropertySingleValue(new Double(1.1), "unit"));
		//errorRatePercCycle100 > 1
		result1Read1.put("errorRatePercCycle100", new PropertySingleValue(new Double(1.1), "unit"));

		//FLAG result
		//clusterPFPerc [75,80]
		result1Read2.put("clusterPFPerc", new PropertySingleValue(new Double(77), "unit"));
		//greaterQ30Perc [80,85]
		result1Read2.put("greaterQ30Perc", new PropertySingleValue(new Double(82), "unit"));
		//phasing [0.3,0.5]
		result1Read2.put("phasing", new PropertySingleValue(new Double(0.4), "unit"));
		//prephasing [0.3,0.5]
		result1Read2.put("prephasing", new PropertySingleValue(new Double(0.4), "unit"));
		//alignedPerc [0.1,0.4]
		result1Read2.put("alignedPerc", new PropertySingleValue(new Double(0.3), "unit"));
		//errorRatePerc [0.5,1]
		result1Read2.put("errorRatePerc", new PropertySingleValue(new Double(0.6), "unit"));
		//errorRatePercCycle35 [0.4,1]
		result1Read2.put("errorRatePercCycle35", new PropertySingleValue(new Double(0.6), "unit"));
		//errorRatePercCycle75 [0.6,1]
		result1Read2.put("errorRatePercCycle75", new PropertySingleValue(new Double(0.7), "unit"));
		//clusterDensity [200,250]
		result2Read1.put("clusterDensity", new PropertySingleValue(new Long(225), "unit"));
		//clusterDensity [850,1000]
		result2Read2.put("clusterDensity", new PropertySingleValue(new Long(900), "unit"));

		treatSAV1.results.put("read1", result1Read1);
		treatSAV1.results.put("read2", result1Read2);
		treatSAV2.results.put("read1", result2Read1);
		treatSAV2.results.put("read2", result2Read2);


		List<Object> facts = new ArrayList<Object>();
		facts.add(runBad);

		RulesServices6 rulesServices = RulesServices6.getInstance();

		List<Object> factsAfterRules = rulesServices.callRulesWithGettingFacts(Play.application().configuration().getString("rules.key"), "sav_1", facts);

		//Get AlertInfo to send mail after rules alert done
		for(Object object : factsAfterRules){
			if(object instanceof AlertSAVInfo){
				AlertSAVInfo alertInfo = (AlertSAVInfo)object;
				Assert.assertTrue(alertInfo.getAllResults().size()>0);
				Map<String,String> resultBADRead1Lane1 = alertInfo.getAllResults().get("read1").get("1").get(AlertSAVInfo.alertBAD);
				Assert.assertTrue(resultBADRead1Lane1.get("clusterDensity").equals("150"));
				Assert.assertTrue(resultBADRead1Lane1.get("clusterPFPerc").equals("70.0"));
				Assert.assertTrue(resultBADRead1Lane1.get("greaterQ30Perc").equals("75.0"));
				Assert.assertTrue(resultBADRead1Lane1.get("phasing").equals("0.6"));
				Assert.assertTrue(resultBADRead1Lane1.get("prephasing").equals("0.6"));
				Assert.assertTrue(resultBADRead1Lane1.get("alignedPerc").equals("0.05"));
				Assert.assertTrue(resultBADRead1Lane1.get("errorRatePerc").equals("1.1"));
				Assert.assertTrue(resultBADRead1Lane1.get("errorRatePercCycle35").equals("1.1"));
				Assert.assertTrue(resultBADRead1Lane1.get("errorRatePercCycle75").equals("1.1"));
				Assert.assertTrue(resultBADRead1Lane1.get("errorRatePercCycle100").equals("1.1"));
				
				Map<String,String> resultBADRead2Lane1 = alertInfo.getAllResults().get("read2").get("1").get(AlertSAVInfo.alertBAD);
				Assert.assertTrue(resultBADRead2Lane1.get("clusterDensity").equals("1010"));
				Map<String,String> resultFlagRead2Lane1 = alertInfo.getAllResults().get("read2").get("1").get(AlertSAVInfo.alertFLAG);
				Assert.assertTrue(resultFlagRead2Lane1.get("clusterPFPerc").equals("77.0"));
				Assert.assertTrue(resultFlagRead2Lane1.get("greaterQ30Perc").equals("82.0"));
				Assert.assertTrue(resultFlagRead2Lane1.get("phasing").equals("0.4"));
				Assert.assertTrue(resultFlagRead2Lane1.get("prephasing").equals("0.4"));
				Assert.assertTrue(resultFlagRead2Lane1.get("alignedPerc").equals("0.3"));
				Assert.assertTrue(resultFlagRead2Lane1.get("errorRatePerc").equals("0.6"));
				Assert.assertTrue(resultFlagRead2Lane1.get("errorRatePercCycle35").equals("0.6"));
				Assert.assertTrue(resultFlagRead2Lane1.get("errorRatePercCycle75").equals("0.7"));
				
				Map<String,String> resultFlagRead1Lane2 = alertInfo.getAllResults().get("read1").get("2").get(AlertSAVInfo.alertFLAG);
				Assert.assertTrue(resultFlagRead1Lane2.get("clusterDensity").equals("225"));
				Map<String,String> resultFlagRead2Lane2 = alertInfo.getAllResults().get("read2").get("2").get(AlertSAVInfo.alertFLAG);
				Assert.assertTrue(resultFlagRead2Lane2.get("clusterDensity").equals("900"));

			}
		}
		
		//Check alert info in database
		List<Alert> alerts = MongoDBDAO.find(InstanceConstants.ALERT_COLL_NAME,Alert.class).toList();
		Assert.assertTrue(alerts.size()>0);
		int nbCheck=0;
		for(Alert alert : alerts){
			Assert.assertTrue(alert.code.contains(runData.code));
			Assert.assertTrue(alert.code.contains("read"));
			Assert.assertEquals(alert.ruleName,"sav_1");
			if(alert.code.equals(runData.code+".1.read1")){
				Logger.debug("Check alert "+alert.code);
				Assert.assertTrue(alert.propertiesAlert.containsKey(AlertSAVInfo.alertBAD));
				List<String> keyBadAlerts = alert.propertiesAlert.get(AlertSAVInfo.alertBAD);
				Assert.assertTrue(keyBadAlerts.contains("clusterDensity"));
				Assert.assertTrue(keyBadAlerts.contains("clusterPFPerc"));
				Assert.assertTrue(keyBadAlerts.contains("greaterQ30Perc"));
				Assert.assertTrue(keyBadAlerts.contains("phasing"));
				Assert.assertTrue(keyBadAlerts.contains("prephasing"));
				Assert.assertTrue(keyBadAlerts.contains("alignedPerc"));
				Assert.assertTrue(keyBadAlerts.contains("errorRatePerc"));
				Assert.assertTrue(keyBadAlerts.contains("errorRatePercCycle35"));
				Assert.assertTrue(keyBadAlerts.contains("errorRatePercCycle100"));
				nbCheck++;
			}else if(alert.code.equals(runData.code+".1.read2")){
				Logger.debug("Check alert "+alert.code);
				Assert.assertTrue(alert.propertiesAlert.containsKey(AlertSAVInfo.alertBAD));
				List<String> keyBadAlerts = alert.propertiesAlert.get(AlertSAVInfo.alertBAD);
				Assert.assertTrue(keyBadAlerts.contains("clusterDensity"));
				Assert.assertTrue(alert.propertiesAlert.containsKey(AlertSAVInfo.alertFLAG));
				List<String> keyFlagAlerts = alert.propertiesAlert.get(AlertSAVInfo.alertFLAG);
				Assert.assertTrue(keyFlagAlerts.contains("clusterPFPerc"));
				Assert.assertTrue(keyFlagAlerts.contains("greaterQ30Perc"));
				Assert.assertTrue(keyFlagAlerts.contains("phasing"));
				Assert.assertTrue(keyFlagAlerts.contains("prephasing"));
				Assert.assertTrue(keyFlagAlerts.contains("alignedPerc"));
				Assert.assertTrue(keyFlagAlerts.contains("errorRatePerc"));
				Assert.assertTrue(keyFlagAlerts.contains("errorRatePercCycle35"));
				Assert.assertTrue(keyFlagAlerts.contains("errorRatePercCycle75"));
				nbCheck++;
			}else if(alert.code.equals(runData.code+".2.read1")){
				Logger.debug("Check alert "+alert.code);
				Assert.assertTrue(alert.propertiesAlert.containsKey(AlertSAVInfo.alertFLAG));
				List<String> keyFlagAlerts = alert.propertiesAlert.get(AlertSAVInfo.alertFLAG);
				Assert.assertTrue(keyFlagAlerts.contains("clusterDensity"));
				nbCheck++;
			}else if(alert.code.equals(runData.code+".2.read2")){
				Logger.debug("Check alert "+alert.code);
				Assert.assertTrue(alert.propertiesAlert.containsKey(AlertSAVInfo.alertFLAG));
				List<String> keyFlagAlerts = alert.propertiesAlert.get(AlertSAVInfo.alertFLAG);
				Assert.assertTrue(keyFlagAlerts.contains("clusterDensity"));
				nbCheck++;
			}
		}
		Assert.assertEquals(nbCheck,4);
	}

}
