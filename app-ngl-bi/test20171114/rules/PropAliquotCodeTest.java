package rules;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mongojack.DBQuery;

import fr.cea.ig.MongoDBDAO;
import junit.framework.Assert;
import models.laboratory.common.instance.property.PropertyListValue;
import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.ContainerSupport;
import models.laboratory.container.instance.Content;
import models.laboratory.run.instance.Run;
import models.utils.InstanceConstants;
import play.Play;
import rules.services.RulesServices6;
import utils.AbstractTestsCNG;

public class PropAliquotCodeTest extends AbstractTestsCNG{

	private final String ruleIPS="IP_S_1";
	private int nbExpectedAliquote;
	@Before
	public void getRunData()
	{
		Run runInput = MongoDBDAO.findByCode("ngl_bi.RunIllumina_initDataCNG", Run.class, "151202_HISEQ7_C834MACXX");
		Run runOutput = MongoDBDAO.findByCode(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, "151202_HISEQ7_C834MACXX");
		if(runOutput != null){
			MongoDBDAO.delete(InstanceConstants.RUN_ILLUMINA_COLL_NAME, runOutput);
		}
		MongoDBDAO.save(InstanceConstants.RUN_ILLUMINA_COLL_NAME, runInput);
		
		ContainerSupport csInput = MongoDBDAO.findByCode("ngl_sq.ContainerSupport_initDataCNG", ContainerSupport.class, runInput.containerSupportCode);
		ContainerSupport csOuput = MongoDBDAO.findByCode(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, ContainerSupport.class, runInput.containerSupportCode);
		if(csOuput != null){
			MongoDBDAO.delete(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, csOuput);
		}
		MongoDBDAO.save(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, csInput);
		
		List<Container> containersInput = MongoDBDAO.find("ngl_sq.Container_initDataCNG", Container.class, DBQuery.is("support.code", runInput.containerSupportCode)).toList();
		nbExpectedAliquote=0;
		for(Container container : containersInput){
			nbExpectedAliquote+=container.contents.size();
			Container containerOutput = MongoDBDAO.findByCode(InstanceConstants.CONTAINER_COLL_NAME, Container.class, container.code);
			if(containerOutput!=null)
				MongoDBDAO.delete(InstanceConstants.CONTAINER_COLL_NAME, containerOutput);
			MongoDBDAO.save(InstanceConstants.CONTAINER_COLL_NAME, container);
		}
	}
	
	@Test
	public void shouldAliquotCodeAddToRun()
	{
		Run run = MongoDBDAO.findByCode(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, "151202_HISEQ7_C834MACXX");
		RulesServices6 rulesServices = RulesServices6.getInstance();
		List<Object> facts = new ArrayList<Object>();
		facts.add(run);
		
		rulesServices.callRules(Play.application().configuration().getString("rules.key"), ruleIPS, facts);
		
		run = MongoDBDAO.findByCode(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, "151202_HISEQ7_C834MACXX");
		//Check properties sampleAliquoteCode
		Assert.assertTrue(run.properties.containsKey("sampleAliquoteCodes"));
		Assert.assertNotNull(run.properties.get("sampleAliquoteCodes"));
		Assert.assertEquals(nbExpectedAliquote, ((PropertyListValue)run.properties.get("sampleAliquoteCodes")).getValue().size());
		
	}
}
