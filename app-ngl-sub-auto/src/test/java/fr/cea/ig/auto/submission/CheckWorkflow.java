package fr.cea.ig.auto.submission;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import fr.genoscope.lis.devsi.birds.api.device.JSONDevice;
import fr.genoscope.lis.devsi.birds.api.entity.Job;
import fr.genoscope.lis.devsi.birds.api.entity.JobResource;
import fr.genoscope.lis.devsi.birds.api.entity.ResourceProperties;
import fr.genoscope.lis.devsi.birds.api.exception.BirdsException;
import fr.genoscope.lis.devsi.birds.api.exception.FatalException;
import fr.genoscope.lis.devsi.birds.api.exception.JSONDeviceException;
import fr.genoscope.lis.devsi.birds.api.exception.PersistenceException;
import fr.genoscope.lis.devsi.birds.api.persistence.EntityManagerHelper;
import fr.genoscope.lis.devsi.birds.api.persistence.PersistenceServiceFactory;
import fr.genoscope.lis.devsi.birds.api.test.GenericTest;
import fr.genoscope.lis.devsi.birds.impl.factory.CoreJobServiceFactory;
import fr.genoscope.lis.devsi.birds.impl.factory.CoreTreatmentSpecificationServiceFactory;
import fr.genoscope.lis.devsi.birds.impl.properties.ProjectProperties;
import junit.framework.Assert;

public class CheckWorkflow extends GenericTest{

	private static Logger log = Logger.getLogger(CheckWorkflow.class);
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
		addConfig("changesets/changesets.xml", "declarations/admin.xml", "declarations/specification.xml", "SRA");
		//Changer le project directory pour ne pas mettre les output dans le repertoire de prod
		replaceProjectDirectory("SRA", workspace);
	}


	@Test
	public void shouldExecuteWorkflowWithNGLSUB() throws PersistenceException, BirdsException, FatalException, IOException
	{
		JSONDevice jsonDevice = new JSONDevice();

		//Replace executable of createXML
		replaceExecutable("createXML", "SRA", CoreTreatmentSpecificationServiceFactory.getInstance().getTreatmentSpecification("createXML", "SRA").getExecutableSpecification().getExecutable().getExecutablePath().replace("appprod.genoscope.cns.fr:9005", "localhost:9000"));

		//Replace executable of transfertRawData 
		//TODO voir si existe url de test au NCBI
		replaceExecutable("transfertRawData", "SRA", scriptEcho);

		//Replace executable of sendXML 
		//TODO voir si existe url de test au NCBI
		replaceExecutable("sendXML", "SRA", scriptEcho);

		//replaceExecutable("ZipFile", "SRA", scriptEcho);

		log.debug("FIRST ROUND");
		executeBirdsCycle("SRA", "WF_Submission");

		//Check group
		EntityManagerHelper em = PersistenceServiceFactory.getInstance().createEntityManagerHelper();
		em.beginTransaction();
		Set<Job> jobs = CoreJobServiceFactory.getInstance().getAllJobs(em.getEm());
		Assert.assertEquals(jobs.size(), 2);
		//2 workflow sub1 (donnees zipée) sub2 (donnees non zipées)
		long idSub1=0;
		long idSub2=0;
		for(Job groupJob : jobs){
			log.debug("GroupJob "+groupJob);
			ResourceProperties rp = groupJob.getUniqueJobResource("subData").getResourceProperties();
			log.debug("code submission "+rp.getProperty("code"));
			if(rp.getProperty("code").equals("codeSub1"))
				idSub1=groupJob.getId();
			else
				idSub2=groupJob.getId();
			String codeSubmission = rp.get("code");
			Assert.assertNotNull(codeSubmission);
			Assert.assertEquals(rp.get("state.code"),"IW-SUB");
			Assert.assertNotNull(rp.get("submissionDirectory"));
			Assert.assertNotNull(rp.get("submissionDate"));
			for(String key : rp.keysSet()){
				log.debug("key:"+key+"="+rp.get(key));
			}
		}
		em.endTransaction();

		log.debug("SECOND ROUND");
		executeBirdsCycle("SRA", "WF_Submission");

		//Check group update state in transfert ressource
		//Get submission in waiting ==0
		Set<ResourceProperties> setRP = jsonDevice.httpGetJSON(ProjectProperties.getProperty("server")+"/api/sra/submissions?stateCode=IW-SUB","bot");
		Assert.assertEquals(setRP.size(),0);

		//Check first submission sub1 with data zipped
		//Check CreateXML
		//Check transfertRawData
		em = PersistenceServiceFactory.getInstance().createEntityManagerHelper();
		em.beginTransaction();
		Job groupJobSub1 = CoreJobServiceFactory.getInstance().getJob(idSub1, em.getEm());
		log.debug("Group job sub 1"+groupJobSub1);
		Job jobCreateXML = groupJobSub1.getSubJobs("createXML").iterator().next();
		//Job jobCreateXML = CoreJobServiceFactory.getInstance().getJobBySpecification("createXML", em.getEm()).iterator().next();
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

		Job jobTransfertRawData = groupJobSub1.getSubJobs("transfertRawData").iterator().next();
		//Job jobTransfertRawData = CoreJobServiceFactory.getInstance().getJobBySpecification("transfertRawData",em.getEm()).iterator().next();
		Assert.assertNotNull(jobTransfertRawData);
		log.debug("Command aspera "+jobTransfertRawData.getUnixCommand());
		Assert.assertTrue(jobTransfertRawData.getUnixCommand().contains("-i ~/.ssh/ebi.sra -T -k 2 -l 300M"));
		Assert.assertTrue(jobTransfertRawData.getUnixCommand().endsWith(" --mode=send --host=webin.ebi.ac.uk --user=Webin-9 ."));
		Assert.assertTrue(jobTransfertRawData.getUnixCommand().contains(jobTransfertRawData.getUniqueJobResource("rawDataDir").getProperty("submissionDirectory")+"/list_aspera_WGS"));

		//Check file WGS
		File file = new File(jobTransfertRawData.getParameterValue("fileList").getValue());
		Assert.assertTrue(file.exists());
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line = null;
		int nbLine=0;
		while((line=br.readLine())!=null){
			log.debug("Line "+line);
			nbLine++;
		}
		br.close();
		Assert.assertEquals(nbLine, 4);
		Assert.assertTrue(file.delete());
		ResourceProperties rpOutRawData = jobTransfertRawData.getOutputUniqueJobResource("outputRawData").getResourceProperties();
		Assert.assertNotNull(rpOutRawData.get("fileList"));

		//Check workflow for submission with data unzipped
		Job groupJobSub2 = CoreJobServiceFactory.getInstance().getJob(idSub2, em.getEm());
		Job groupJobZipMd5 = groupJobSub2.getSubJobs("WF_ZipMd5Process").iterator().next();
		Assert.assertNotNull(groupJobZipMd5);
		ResourceProperties rpZipMd5 = groupJobZipMd5.getUniqueJobResource("inputZipMd5").getResourceProperties();
		Assert.assertEquals("codeSub2", rpZipMd5.get("code"));
		Assert.assertEquals("true", rpZipMd5.get("gzipForSubmission"));
		Assert.assertTrue(rpZipMd5.containsKey("idGroupJob"));
		Assert.assertTrue(rpZipMd5.getProperty("experimentCodes").contains("codeExp3"));
		Assert.assertTrue(rpZipMd5.getProperty("experimentCodes").contains("codeExp4"));
		em.endTransaction();


		log.debug("THIRD ROUND");
		executeBirdsCycle("SRA", "WF_Submission");
		//Check firs submission with data zipped
		//Check send XML
		em = PersistenceServiceFactory.getInstance().createEntityManagerHelper();
		em.beginTransaction();
		groupJobSub1 = CoreJobServiceFactory.getInstance().getJob(idSub1, em.getEm());
		Job jobSendXML = groupJobSub1.getSubJobs("sendXML").iterator().next();
		//Job jobSendXML = CoreJobServiceFactory.getInstance().getJobBySpecification("sendXML",em.getEm()).iterator().next();
		log.debug("job send XML "+jobSendXML);
		Assert.assertNotNull(jobSendXML);

		Assert.assertTrue(jobSendXML.getUnixCommand().contains("-F \"SUBMISSION=@"+jobSendXML.getUniqueJobResource("subToSend").getProperty("xmlSubmission")+"\""));
		Assert.assertTrue(jobSendXML.getUnixCommand().contains("-F \"SAMPLE=@"+jobSendXML.getUniqueJobResource("subToSend").getProperty("xmlSamples")+"\""));
		Assert.assertTrue(jobSendXML.getUnixCommand().contains("-F \"RUN=@"+jobSendXML.getUniqueJobResource("subToSend").getProperty("xmlRuns")+"\""));
		Assert.assertTrue(jobSendXML.getUnixCommand().contains("-F \"STUDY=@"+jobSendXML.getUniqueJobResource("subToSend").getProperty("xmlStudys")+"\""));
		Assert.assertTrue(jobSendXML.getUnixCommand().contains("-F \"EXPERIMENT=@"+jobSendXML.getUniqueJobResource("subToSend").getProperty("xmlExperiments")+"\""));

		//End command line redirect to AC files
		Assert.assertTrue(jobSendXML.getUnixCommand().endsWith("> listAC_"+jobSendXML.getUniqueJobResource("subToSend").getProperty("submissionDate")+".txt"));

		//Check input
		Assert.assertEquals(jobSendXML.getInputValue("subToSend").getInputJobResourceValues().size(),1);
		Assert.assertEquals(jobSendXML.getInputValue("rawDataSend").getInputJobResourceValues().size(),1);

		//Check second submission with data non zipped
		groupJobSub2 = CoreJobServiceFactory.getInstance().getJob(idSub2, em.getEm());
		groupJobZipMd5 = groupJobSub2.getSubJobs("WF_ZipMd5Process").iterator().next();
		Set<Job> jobsZip = groupJobZipMd5.getSubJobs("ZipFile");
		Assert.assertEquals(2, jobsZip.size());
		for(Job jobZip : jobsZip){
			log.debug("Job zip "+jobZip);
			log.debug("Job unix command "+jobZip.getUnixCommand());
			log.debug("Job execution state "+jobZip.getExecutionState());
			Assert.assertTrue(jobZip.getUnixCommand().startsWith("gzip -c /env/cns/home/ejacoby/NGL-SUB-Test/dataDir/"+jobZip.getUniqueJobResource("inputRawDataZip").getProperty("fileName")));
			Assert.assertTrue(jobZip.getUnixCommand().endsWith(">/env/cns/home/ejacoby/NGL-SUB-Test/tmpSubDir/"+jobZip.getUniqueJobResource("inputRawDataZip").getProperty("fileName")+".gz"));
			Assert.assertEquals(Job.DONE_STATUS, jobZip.getExecutionState());
		}
		em.endTransaction();


		log.debug("FOURTH ROUND");
		executeBirdsCycle("SRA", "WF_Submission");
		em = PersistenceServiceFactory.getInstance().createEntityManagerHelper();
		em.beginTransaction();
		//Check group with files zipped workflow is exited because sendXML is exited
		groupJobSub1 = CoreJobServiceFactory.getInstance().getJob(idSub1, em.getEm());
		for(Job subJob1 : groupJobSub1.getSubJobs()){
			if(subJob1.getTreatmentSpecification().getName().equals("sendXML"))
				Assert.assertEquals(Job.ERROR_STATUS, subJob1.getExecutionState());
			else
				Assert.assertEquals(Job.DONE_STATUS, subJob1.getExecutionState());
			log.debug("Job "+subJob1.getUnixCommand()+"="+subJob1.getExecutionState());
		}
		//Assert.assertEquals(Job.DONE_STATUS, groupJobSub1.getExecutionState());

		//Check second submission with data non zipped
		groupJobSub2 = CoreJobServiceFactory.getInstance().getJob(idSub2, em.getEm());
		groupJobZipMd5 = groupJobSub2.getSubJobs("WF_ZipMd5Process").iterator().next();
		Set<Job> jobsMd5 = groupJobZipMd5.getSubJobs("Md5File");
		Assert.assertEquals(2, jobsMd5.size());
		for(Job jobMd5 : jobsMd5){
			log.debug("Job zip "+jobMd5);
			log.debug("Job unix command "+jobMd5.getUnixCommand());
			log.debug("Job execution state "+jobMd5.getExecutionState());
			Assert.assertTrue(jobMd5.getUnixCommand().startsWith("md5sum /env/cns/home/ejacoby/NGL-SUB-Test/tmpSubDir/"+jobMd5.getUniqueJobResource("inputRawDataMd5").getProperty("fileName")+".gz"));
			Assert.assertEquals(Job.DONE_STATUS, jobMd5.getExecutionState());
			
			log.debug("Job stdout "+jobMd5.getProperty(Job.STDOUT));
			BufferedReader read = new BufferedReader(new FileReader(new File(jobMd5.getProperty(Job.STDOUT))));
			String md5 = read.readLine().split(" ")[0];
			log.debug("MD5 = "+md5);
			read.close();
			
			//TODO check update md5 in NGL 
			JobResource jobResource = jobMd5.getUniqueJobResource("inputRawDataMd5");
			ResourceProperties rp = jsonDevice.httpGetJSON(ProjectProperties.getProperty("server")+"/api/sra/experiments/"+jobResource.getProperty("experimentCode")+"/rawDatas/"+jobResource.getProperty("fileName"),"bot").iterator().next();
			Assert.assertEquals(md5, rp.get("submittedMd5"));
		}
		
		em.endTransaction();
		log.debug("FIFTH ROUND");
		executeBirdsCycle("SRA", "WF_Submission");
		em = PersistenceServiceFactory.getInstance().createEntityManagerHelper();
		em.beginTransaction();
		//Check group with files zipped workflow is exited because sendXML is exited
		groupJobSub1 = CoreJobServiceFactory.getInstance().getJob(idSub1, em.getEm());
		for(Job subJob1 : groupJobSub1.getSubJobs()){
			if(subJob1.getTreatmentSpecification().getName().equals("sendXML"))
				Assert.assertEquals(Job.ERROR_STATUS, subJob1.getExecutionState());
			else
				Assert.assertEquals(Job.DONE_STATUS, subJob1.getExecutionState());
			log.debug("Job "+subJob1.getUnixCommand()+"="+subJob1.getExecutionState());
		}
		//Assert.assertEquals(Job.DONE_STATUS, groupJobSub1.getExecutionState());

		//Check second submission with data non zipped
		groupJobSub2 = CoreJobServiceFactory.getInstance().getJob(idSub2, em.getEm());
		groupJobZipMd5 = groupJobSub2.getSubJobs("WF_ZipMd5Process").iterator().next();
		Assert.assertEquals(Job.DONE_STATUS, groupJobZipMd5.getExecutionState());
		
		for(Job subJob : groupJobSub2.getSubJobs()){
			log.debug("Job "+subJob);
		}
		
		em.endTransaction();
		
		log.debug("SIXTH ROUND");
		executeBirdsCycle("SRA", "WF_Submission");
		em = PersistenceServiceFactory.getInstance().createEntityManagerHelper();
		em.beginTransaction();
		//Check group with files zipped workflow is exited because sendXML is exited
		groupJobSub1 = CoreJobServiceFactory.getInstance().getJob(idSub1, em.getEm());
		for(Job subJob1 : groupJobSub1.getSubJobs()){
			if(subJob1.getTreatmentSpecification().getName().equals("sendXML"))
				Assert.assertEquals(Job.ERROR_STATUS, subJob1.getExecutionState());
			else
				Assert.assertEquals(Job.DONE_STATUS, subJob1.getExecutionState());
			log.debug("Job "+subJob1.getUnixCommand()+"="+subJob1.getExecutionState());
		}
		//Assert.assertEquals(Job.DONE_STATUS, groupJobSub1.getExecutionState());

		//Check second submission with data non zipped
		groupJobSub2 = CoreJobServiceFactory.getInstance().getJob(idSub2, em.getEm());
		groupJobZipMd5 = groupJobSub2.getSubJobs("WF_ZipMd5Process").iterator().next();
		Assert.assertEquals(Job.DONE_STATUS, groupJobZipMd5.getExecutionState());
		jobCreateXML = groupJobSub2.getSubJobs("createXML").iterator().next();
		Assert.assertNotNull(jobCreateXML);
		Assert.assertEquals(Job.DONE_STATUS, jobCreateXML.getExecutionState());
		jobTransfertRawData = groupJobSub2.getSubJobs("transfertRawData").iterator().next();
		Assert.assertNotNull(jobTransfertRawData);
		Assert.assertEquals(Job.DONE_STATUS, jobTransfertRawData.getExecutionState());
		
		em.endTransaction();
		
		log.debug("SEVENTH ROUND");
		executeBirdsCycle("SRA", "WF_Submission");
		em = PersistenceServiceFactory.getInstance().createEntityManagerHelper();
		em.beginTransaction();
		//Check group with files zipped workflow is exited because sendXML is exited
		groupJobSub1 = CoreJobServiceFactory.getInstance().getJob(idSub1, em.getEm());
		for(Job subJob1 : groupJobSub1.getSubJobs()){
			if(subJob1.getTreatmentSpecification().getName().equals("sendXML"))
				Assert.assertEquals(Job.ERROR_STATUS, subJob1.getExecutionState());
			else
				Assert.assertEquals(Job.DONE_STATUS, subJob1.getExecutionState());
			log.debug("Job "+subJob1.getUnixCommand()+"="+subJob1.getExecutionState());
		}
		//Assert.assertEquals(Job.DONE_STATUS, groupJobSub1.getExecutionState());

		//Check second submission with data non zipped
		groupJobSub2 = CoreJobServiceFactory.getInstance().getJob(idSub2, em.getEm());
		groupJobZipMd5 = groupJobSub2.getSubJobs("WF_ZipMd5Process").iterator().next();
		Assert.assertEquals(Job.DONE_STATUS, groupJobZipMd5.getExecutionState());
		jobSendXML = groupJobSub2.getSubJobs("sendXML").iterator().next();
		Assert.assertNotNull(jobSendXML);
		Assert.assertEquals(Job.ERROR_STATUS, jobSendXML.getExecutionState());
		
		em.endTransaction();
		
		log.debug("EIGHTH ROUND");
		executeBirdsCycle("SRA", "WF_Submission");
		em = PersistenceServiceFactory.getInstance().createEntityManagerHelper();
		em.beginTransaction();
		//Check group with files zipped workflow is exited because sendXML is exited
		groupJobSub1 = CoreJobServiceFactory.getInstance().getJob(idSub1, em.getEm());
		Assert.assertEquals(3, groupJobSub1.getSubJobs().size());
		log.debug("Status group sub1 "+groupJobSub1.getExecutionState());
		//Assert.assertEquals(Job.DONE_STATUS, groupJobSub1.getExecutionState());

		//Check second submission with data non zipped
		groupJobSub2 = CoreJobServiceFactory.getInstance().getJob(idSub2, em.getEm());
		Assert.assertEquals(4, groupJobSub2.getSubJobs().size());
		groupJobZipMd5 = groupJobSub2.getUniqueSubJob("WF_ZipMd5Process");
		Assert.assertEquals(4, groupJobZipMd5.getSubJobs().size());
		log.debug("status group sub 2 "+groupJobSub2.getExecutionState());
		em.endTransaction();
		//Get Submission from database

		//TODO change to FE-SUB
		Set<ResourceProperties> setRPSub = jsonDevice.httpGetJSON(ProjectProperties.getProperty("server")+"/api/sra/submissions?stateCode=FE-SUB","bot");
		log.debug("Set RPub "+setRPSub);
		Assert.assertTrue(setRPSub.size()==2);
		for(ResourceProperties rp : setRPSub){
			Assert.assertTrue(rp.get("state.code").equals("FE-SUB"));String newState = "{\"code\":\"IW-SUB\"}";
			jsonDevice.httpPut(ProjectProperties.getProperty("server")+"/sra/submissions/"+rp.get("code")+"/state", newState, "bot");
		}
		
		//TODO remove gz files and md5 property
		Assert.assertTrue(new File("/env/cns/home/ejacoby/NGL-SUB-Test/tmpSubDir/file5.fastq.gz").delete());
		Assert.assertTrue(new File("/env/cns/home/ejacoby/NGL-SUB-Test/tmpSubDir/file7.fastq.gz").delete());
		
		String JSONRawData = jsonDevice.httpGet(ProjectProperties.getProperty("server")+"/api/sra/experiments/codeExp3/rawDatas/file5.fastq","bot");
		String JSONRawDataModify = jsonDevice.modifyJSON(JSONRawData, "submittedMd5", "");
		String jsonResult = jsonDevice.httpPut(ProjectProperties.getProperty("server")+"/api/sra/experiments/codeExp3/rawDatas",JSONRawDataModify, "bot");
		log.debug("JSON result "+jsonResult);
		JSONRawData = jsonDevice.httpGet(ProjectProperties.getProperty("server")+"/api/sra/experiments/codeExp4/rawDatas/file7.fastq","bot");
		JSONRawDataModify = jsonDevice.modifyJSON(JSONRawData, "submittedMd5", "");
		jsonResult = jsonDevice.httpPut(ProjectProperties.getProperty("server")+"/api/sra/experiments/codeExp4/rawDatas",JSONRawDataModify, "bot");
		log.debug("JSON result "+jsonResult);
	}
	
	//@Test
	public void testListExperiment() throws JSONDeviceException, FatalException
	{
		JSONDevice device = new JSONDevice();
		Map<String,String> keyArrays = new HashMap<String, String>();
		keyArrays.put("listRawData", "relatifName");
		Set<ResourceProperties> setResourceProperties = device.httpGetJSON(ProjectProperties.getProperty("server")+"/api/sra/submissions/codeSub1",null,null,"bot");
		//String JSON = device.httpGet(ProjectProperties.getProperty("server")+"/api/sra/submissions/codeSub1","bot");
		//log.debug("JSON "+JSON);
		//Set<ResourceProperties> setRP = device.parseJSONFromString(JSON,keyArrays);
		for(ResourceProperties rp : setResourceProperties){
			log.debug("RESOURCE ");
			for(String key : rp.keysSet()){
				log.debug(key+"="+rp.get(key));
			}
		}
		
	}
	

}
