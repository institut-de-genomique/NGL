package rules;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.AssertTrue;

import junit.framework.Assert;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.property.PropertyObjectListValue;
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

public class StatQCTest extends AbstractTestsCNS{

	
	@BeforeClass
	public static void getReadSetData()
	{
		ReadSet rsInput = MongoDBDAO.findByCode("ngl_bi.ReadSetIllumina_initData", ReadSet.class, "BCM_CFIOSW_4_C3MGGACXX.IND8");
		ReadSet rsOutput = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, "BCM_CFIOSW_4_C3MGGACXX.IND8");
		if(rsOutput != null){
			MongoDBDAO.delete(InstanceConstants.READSET_ILLUMINA_COLL_NAME, rsOutput);
		}
		MongoDBDAO.save(InstanceConstants.READSET_ILLUMINA_COLL_NAME, rsInput);
	}

	
	@Test
	public void testStatQC() throws RulesException, ParseException
	{

		ReadSet rs = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, "BCM_CFIOSW_4_C3MGGACXX.IND8");
		RulesServices6 rulesServices = RulesServices6.getInstance();
		List<Object> facts = new ArrayList<Object>();
		facts.add(rs);
		
		rulesServices.callRules(Play.application().configuration().getString("rules.key"), "F_QC_1", facts);
		
		rs = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, "BCM_CFIOSW_4_C3MGGACXX.IND8");
		
		Treatment taxo = rs.treatments.get("taxonomy");
		PropertyObjectListValue keywordBilan = (PropertyObjectListValue)taxo.results.get("read1").get("keywordBilan");
		boolean isFind = false;
		for(Map<String, ?> m : keywordBilan.value){
			if(m.containsValue("Fungi") && m.get("nbSeq").equals(Integer.valueOf(18745)) ){
				isFind = true;
			}
		}
		Assert.assertTrue(isFind);
	}
	
	
	
}


