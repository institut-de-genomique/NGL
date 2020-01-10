package fr.cea.ig.auto.submission;

import java.io.IOException;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import fr.genoscope.lis.devsi.birds.api.device.JSONDevice;
import fr.genoscope.lis.devsi.birds.api.entity.Job;
import fr.genoscope.lis.devsi.birds.api.entity.ResourceProperties;
import fr.genoscope.lis.devsi.birds.api.exception.BirdsException;
import fr.genoscope.lis.devsi.birds.api.exception.FatalException;
import fr.genoscope.lis.devsi.birds.api.exception.PersistenceException;
import fr.genoscope.lis.devsi.birds.api.persistence.EntityManagerHelper;
import fr.genoscope.lis.devsi.birds.api.persistence.PersistenceServiceFactory;
import fr.genoscope.lis.devsi.birds.api.test.GenericTest;
import fr.genoscope.lis.devsi.birds.impl.factory.CoreJobServiceFactory;
import fr.genoscope.lis.devsi.birds.impl.factory.CoreTreatmentSpecificationServiceFactory;
import fr.genoscope.lis.devsi.birds.impl.properties.ProjectProperties;
import junit.framework.Assert;

public class CheckWorkflowDev extends GenericTest{

	private static Logger log = Logger.getLogger(CheckWorkflowDev.class);
	private final String scriptEcho = "/env/cns/home/ejacoby/testU/app-NGL-sub-auto/bin/scriptEcho.sh";
	private final String scriptCreateXML = "/env/cns/home/ejacoby/testU/app-NGL-sub-auto/bin/scriptCreateXML";
	private final String workspace = "/env/cns/home/ejacoby/testU/app-NGL-sub-auto/log";

	/**
	 * Call initData of NGL-SUB unit test (builder.data)
	 * And run NGL-SUB on localhost
	 * @throws PersistenceException
	 * @throws BirdsException
	 * @throws FatalException
	 */
	@Before
	public void addDeclaration() throws PersistenceException, BirdsException, FatalException
	{
		addConfig("changesets/changesets.xml", "declarations/admin.xml", "declarations/specification_dev.xml", "SRA");
		//Changer le project directory pour ne pas mettre les output dans le repertoire de prod
		replaceProjectDirectory("SRA", workspace);
	}
	
	@After
	public void initSubmission() throws PersistenceException, BirdsException, FatalException
	{
		//Get submission
		String newState = "{\"code\":\"IW-SUB\"}";
		JSONDevice jsonDevice = new JSONDevice();
		jsonDevice.httpPut(ProjectProperties.getProperty("server")+"/sra/submissions/codeSub1/state", newState);
			  	
	}


	@Test
	public void shouldExecuteWorkflowWithNGLSUB() throws PersistenceException, BirdsException, FatalException, IOException
	{
		JSONDevice jsonDevice = new JSONDevice();

		//Replace executable of createXML
		replaceExecutable("createXML", "SRA", CoreTreatmentSpecificationServiceFactory.getInstance().getTreatmentSpecification("createXML", "SRA").getExecutableSpecification().getExecutable().getExecutablePath().replace("apppdev.genoscope.cns.fr:9005", "localhost:9000"));

		log.debug("FIRST ROUND");
		executeBirdsCycle("SRA", "WF_Submission");

		//Check group
		EntityManagerHelper em = PersistenceServiceFactory.getInstance().createEntityManagerHelper();
		em.beginTransaction();
		Set<Job> jobs = CoreJobServiceFactory.getInstance().getAllJobs(em.getEm());
		Assert.assertEquals(jobs.size(), 1);
		Job groupJob = jobs.iterator().next();
		ResourceProperties rp = groupJob.getUniqueJobResource("subData").getResourceProperties();
		String codeSubmission = rp.get("code");
		Assert.assertNotNull(codeSubmission);
		Assert.assertEquals(rp.get("state.code"),"IW-SUB");
		Assert.assertNotNull(rp.get("submissionDirectory"));
		Assert.assertNotNull(rp.get("submissionDate"));
		for(String key : rp.keysSet()){
			log.debug("key:"+key+"="+rp.get(key));
		}
		em.endTransaction();

		log.debug("SECOND ROUND");
		executeBirdsCycle("SRA", "WF_Submission");

		//Check group update state in transfert ressource
		//Get submission in waiting ==0
		Set<ResourceProperties> setRP = jsonDevice.httpGetJSON(ProjectProperties.getProperty("server")+"/api/sra/submissions?stateCode=IW-SUB","bot");
		Assert.assertEquals(setRP.size(),0);


		//Check CreateXML
		//Check transfertRawData
		em = PersistenceServiceFactory.getInstance().createEntityManagerHelper();
		em.beginTransaction();
		Job jobCreateXML = CoreJobServiceFactory.getInstance().getJobBySpecification("createXML", em.getEm()).iterator().next();
		Assert.assertNotNull(jobCreateXML);
		//Check output ressources
		ResourceProperties rpOut = jobCreateXML.getOutputUniqueJobResource("outputSubXML").getResourceProperties();
		log.debug("Resource properties out createXML : "+rpOut);
		Assert.assertNotNull(rpOut.get("xmlSamples"));
		Assert.assertEquals(rpOut.get("xmlSamples"), "sample.xml");
		Assert.assertNotNull(rpOut.get("xmlRuns"));
		Assert.assertEquals(rpOut.get("xmlRuns"), "run.xml");
		Assert.assertNotNull(rpOut.get("xmlSubmission"));
		Assert.assertEquals(rpOut.get("xmlSubmission"), "submission.xml");
		Assert.assertNotNull(rpOut.get("xmlStudys"));
		Assert.assertEquals(rpOut.get("xmlStudys"),"null");
		
		Assert.assertTrue(CoreJobServiceFactory.getInstance().getJobBySpecification("transfertRawData",em.getEm()).size()==0);
		
		em.endTransaction();

	  	

	}

	
}
