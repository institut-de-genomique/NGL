package rules;
/**
 * 
 */
import static org.fest.assertions.Assertions.assertThat;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.callAction;
import static play.test.Helpers.fakeRequest;
import static play.test.Helpers.status;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.run.instance.Analysis;
import models.utils.InstanceConstants;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.mongojack.DBQuery;

import play.Logger;
import play.Play;
import play.mvc.Result;
import rules.services.RulesException;
import rules.services.RulesServices;
import rules.services.RulesServices6;
import utils.AbstractTestsCNS;
import fr.cea.ig.MongoDBDAO;


public class StatBPATest extends AbstractTestsCNS
{
	private static Analysis analysis;
	private static final String codeAnalysis = "BA_BFY_AAKCOSF_1_A7VKN.IND13";
	private static final Integer storedBases = 1097478;
	private static final Integer assemblyContigSize = 1418258;
	
	
	/**
	 * Copy data once from mongo uat to mongo dev
	 * > var db2=connect('mongodev.genoscope.cns.fr:27017/NGL-TESTU')
	 * connecting to: mongodev.genoscope.cns.fr:27017/NGL-TESTU
     * > db2.auth('testu','testu')
 	 * 1
	 * >db.ngl_bi.Analysis.find().limit(1).forEach(function(p) {db2.ngl_bi.Analysis_stat.insert(p);});
	 */
	
	@Before
	public void getAnalysis()
	{
		Logger.info("get Analysis");
	    
		Analysis analysis_stat = MongoDBDAO.findOne("ngl_bi.Analysis_stat", Analysis.class, DBQuery.is("code", codeAnalysis));
		Logger.info("Analysis "+analysis_stat);
		
		
		//Copy analysis object to collection and remove older if stat already compute
		analysis = MongoDBDAO.findOne(InstanceConstants.ANALYSIS_COLL_NAME, Analysis.class, DBQuery.is("code", codeAnalysis));
		
		if(analysis!=null)
			MongoDBDAO.delete(InstanceConstants.ANALYSIS_COLL_NAME, analysis);
		MongoDBDAO.save(InstanceConstants.ANALYSIS_COLL_NAME, analysis_stat);
		analysis=analysis_stat;
		
	}
	
	/**
	 * Should calculate 
	 * analysis.treatments.contigFilterBA.pairs.lostBasesPercent=
	 * ((analysis.treatments.assemblyBA.pairs.assemblyContigSize - analysis.treatments.contigFilterBA.pairs.storedBases) 
	 * / analysis.treatments.assemblyBA.pairs.assemblyContigSize) * 100
	 * @throws RulesException
	 * @throws ParseException 
	 */
	@Test
	public void shouldComputeStat() throws RulesException, ParseException
	{
		RulesServices6 rulesServices = RulesServices6.getInstance();
		List<Object> facts = new ArrayList<Object>();
		facts.add(analysis);
		
		rulesServices.callRules(Play.application().configuration().getString("rules.key"), "BPA_ContigFilter_1", facts);
		
		//Get object after rules
		analysis = MongoDBDAO.findOne(InstanceConstants.ANALYSIS_COLL_NAME, Analysis.class, DBQuery.is("code", codeAnalysis));
		Logger.info("Analysis after rules "+analysis);
		//Check calculate
		
		//Get value storedBases and assemblyContigSize
		Integer assemblyContigSizeDB = ((PropertyValue<Integer>)analysis.treatments.get("assemblyBA").results.get("pairs").get("assemblyContigSize")).value;
		Integer storedBasesDB = ((PropertyValue<Integer>)analysis.treatments.get("contigFilterBA").results.get("pairs").get("storedBases")).value; 
		Assert.assertEquals(assemblyContigSizeDB, assemblyContigSize);
		Assert.assertEquals(storedBasesDB, storedBases);
		
		//Check formula
		double lostBasesPercentExpected = roundValue((double)(assemblyContigSize-storedBases)/assemblyContigSize*100);
		Logger.debug("Value calculated "+lostBasesPercentExpected);
		double lostBasesPercentDB = ((PropertyValue<Double>)analysis.treatments.get("contigFilterBA").results.get("pairs").get("lostBasesPercent")).value;
		Assert.assertEquals(lostBasesPercentExpected, lostBasesPercentDB);
	}
	
	@Test
	public void shouldStatBPAActionOk()
	{
		//With Analyses URL
		Result result = callAction(controllers.analyses.api.routes.ref.Analyses.applyRules(codeAnalysis,"BPA_ContigFilter_1"),fakeRequest());
        assertThat(status(result)).isEqualTo(OK);
        
	}
}
