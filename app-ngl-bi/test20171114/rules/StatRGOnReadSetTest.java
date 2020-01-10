package rules;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mongojack.DBQuery;

import fr.cea.ig.MongoDBDAO;
import models.laboratory.common.instance.property.PropertyObjectValue;
import models.laboratory.run.instance.ReadSet;
import models.utils.InstanceConstants;
import play.Play;
import rules.services.RulesServices6;
import utils.AbstractTestsCNS;

public class StatRGOnReadSetTest extends AbstractTestsCNS{

	private static ReadSet readSetData;
	private static String codeReadSet = "BCM_ABA_ONT_1_FCacpool_A";
	
	@BeforeClass
	public static void getReadSetData()
	{
		ReadSet readSetInit = MongoDBDAO.findOne("ngl_bi.ReadSetIllumina_initData", ReadSet.class, DBQuery.is("code", codeReadSet));
		readSetData = MongoDBDAO.findOne(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.is("code", codeReadSet));
		if(readSetData!=null){
			MongoDBDAO.delete(InstanceConstants.READSET_ILLUMINA_COLL_NAME, readSetData);
		}
		MongoDBDAO.save(InstanceConstants.READSET_ILLUMINA_COLL_NAME, readSetInit);
		readSetData=readSetInit;
		
	}
	
	@Test
	public void should2DPassReadPercentsCalculated()
	{
		RulesServices6 rulesServices = RulesServices6.getInstance();
		List<Object> facts = new ArrayList<Object>();
		facts.add(readSetData);
		
		rulesServices.callRules(Play.application().configuration().getString("rules.key"), "F_RG_1", facts);
		
		//Check data in database
		ReadSet readSetDB = MongoDBDAO.findOne(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.is("code", codeReadSet));
		Assert.assertNotNull(readSetDB);
		Double valueReadsPercent = (Double)((PropertyObjectValue)readSetDB.treatments.get("ngsrg").results.get("default").get("2DPass")).value.get("readsPercent");
		Assert.assertNotNull(valueReadsPercent);
	}
}
