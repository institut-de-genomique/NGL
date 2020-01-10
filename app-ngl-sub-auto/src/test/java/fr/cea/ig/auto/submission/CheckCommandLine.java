package fr.cea.ig.auto.submission;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import fr.genoscope.lis.devsi.birds.api.entity.Job;
import fr.genoscope.lis.devsi.birds.api.entity.TreatmentSpecification;
import fr.genoscope.lis.devsi.birds.api.exception.BirdsException;
import fr.genoscope.lis.devsi.birds.api.exception.FatalException;
import fr.genoscope.lis.devsi.birds.api.exception.PersistenceException;
import fr.genoscope.lis.devsi.birds.api.persistence.EntityManagerHelper;
import fr.genoscope.lis.devsi.birds.api.persistence.PersistenceServiceFactory;
import fr.genoscope.lis.devsi.birds.api.test.GenericTest;
import fr.genoscope.lis.devsi.birds.impl.factory.JobServiceFactory;
import fr.genoscope.lis.devsi.birds.impl.factory.TreatmentSpecificationServiceFactory;

public class CheckCommandLine extends GenericTest{

	/**
	 * Check command Line for old version : complete version with zip and md5
	 */
	private static Logger log = Logger.getLogger(CheckCommandLine.class);

	private final String urlProd = "http://appprod.genoscope.cns.fr:90??/";
	private final String urlDev = "http://appdev.genoscope.cns.fr:90??/";
	private final String workspace = "/env/cns/home/ejacoby/testU/app-NGL-sub-auto";
	@Before
	public void addDeclaration() throws PersistenceException, BirdsException, FatalException
	{
		addConfig("changesets/changesets.xml", "declarations/admin.xml", "declarations/specification.xml", "SRA");
		//Changer le project directory pour ne pas mettre les output dans le repertoire de prod
		replaceProjectDirectory("SRA", workspace);
	}

	
	@Test
	public void shouldCreateFtpCommandLine() throws FatalException, BirdsException
	{
		//replace device by abstractDevice
		replaceDevice("internalRefSubmission", "submissionDevice", "fr.cea.ig.auto.submission.test.referential.SubmissionDevice");

		//Building job simulation
		jobsBuildingSimulation("transfertRawData", "SRA");

		//Get jobs
		EntityManagerHelper em = PersistenceServiceFactory.getInstance().createEntityManagerHelper();
		em.beginTransaction();
		//Get one job 
		Job job = JobServiceFactory.getInstance().getAllJobs(em.getEm()).iterator().next();
		log.debug(job);
		log.debug("Command line "+job.getUnixCommand());
		String[] extensions = new String[] { "fastq.gz", "sff" , "srf" };
		String commandLineExpected = "ncftpput -u era-drop-9 -p Axqw16nI -R -t 60 -V ftp.sra.ebi.ac.uk / "+SRAFilesUtil.getLocalDirectoryParameter(job.getUniqueJobResource("rawDataDir").getProperty("submissionDirectory"), extensions);
		Assert.assertTrue(job.getUnixCommand().equals(commandLineExpected));
		em.endTransaction();
	}


	@Test
	public void shouldCreateXMLCommandLine() throws FatalException, BirdsException
	{
		//replace device by abstractDevice
		replaceDevice("internalRefSubmission", "submissionDevice", "fr.cea.ig.auto.submission.test.referential.SubmissionDevice");

		//Replace API REST Service NGL Sub by dev services
		EntityManagerHelper em = PersistenceServiceFactory.getInstance().createEntityManagerHelper();
		em.beginTransaction();
		TreatmentSpecification treatSpec = TreatmentSpecificationServiceFactory.getInstance().getTreatmentSpecification("createXML", "SRA",em.getEm());
		treatSpec.getExecutableSpecification().getExecutable().setExecutablePath(treatSpec.getExecutableSpecification().getExecutable().getExecutablePath().replace(urlProd, urlDev));
		em.endTransaction();

		//Building job simulation
		jobsBuildingSimulation("createXML", "SRA");

		//Get job
		em = PersistenceServiceFactory.getInstance().createEntityManagerHelper();
		em.beginTransaction();
		Job job = JobServiceFactory.getInstance().getAllJobs(em.getEm()).iterator().next();
		log.debug(job);
		log.debug("Command line "+job.getUnixCommand());
		Assert.assertTrue(job.getUnixCommand().equals("curl "+urlDev+"api/submissions/"+job.getUniqueJobResource("subToXML").getProperty("code")));
		em.endTransaction();

	}

	@Test
	public void shouldCreateSendXMLCommandLine() throws FatalException, BirdsException
	{
		//replace device by abstractDevice
		replaceDevice("internalRefSubmissionXML", "submissionDeviceXML", "fr.cea.ig.auto.submission.test.referential.SubmissionDeviceXML");

		//Building job simulation
		jobsBuildingSimulation("sendXML", "SRA");

		//Get job
		EntityManagerHelper em = PersistenceServiceFactory.getInstance().createEntityManagerHelper();
		em.beginTransaction();
		Job job = JobServiceFactory.getInstance().getAllJobs(em.getEm()).iterator().next();
		log.debug(job);
		log.debug("Command line "+job.getUnixCommand());
		String startCommandLineExpected = "curl https://www.ebi.ac.uk/ena/submit/drop-box/submit/?auth=ERA%20era-drop-9%20N7mo%2B8F4aHH%2BrCjLTuMo59xwfFo%3D -k ";
		Assert.assertTrue(job.getUnixCommand().startsWith(startCommandLineExpected));
		Assert.assertTrue(job.getUnixCommand().contains("-F \"SUBMISSION=@"+job.getUniqueJobResource("subToSend").getProperty("submissionXML")+"\""));
		Assert.assertTrue(job.getUnixCommand().contains("-F \"SAMPLE=@"+job.getUniqueJobResource("subToSend").getProperty("sampleXML")+"\""));
		Assert.assertTrue(job.getUnixCommand().contains("-F \"RUN=@"+job.getUniqueJobResource("subToSend").getProperty("runXML")+"\""));
		Assert.assertTrue(job.getUnixCommand().contains("-F \"STUDY=@"+job.getUniqueJobResource("subToSend").getProperty("studyXML")+"\""));
		Assert.assertTrue(job.getUnixCommand().contains("-F \"EXPERIMENT=@"+job.getUniqueJobResource("subToSend").getProperty("experimentXML")+"\""));
		
		//End command line redirect to AC files
		Assert.assertTrue(job.getUnixCommand().endsWith("> listAC_"+job.getUniqueJobResource("subToSend").getProperty("submissionDate")+".txt"));
		em.endTransaction();
	}

	@Test
	public void shouldCreateSendXMLCommandLineWithNoStudy() throws FatalException, BirdsException
	{
		//replace device by abstractDevice
		replaceDevice("internalRefSubmissionXML", "submissionDeviceXML", "fr.cea.ig.auto.submission.test.referential.SubmissionDeviceXMLWithNullValue");

		//Building job simulation
		jobsBuildingSimulation("sendXML", "SRA");

		//Get job
		EntityManagerHelper em = PersistenceServiceFactory.getInstance().createEntityManagerHelper();
		em.beginTransaction();
		Job job = JobServiceFactory.getInstance().getAllJobs(em.getEm()).iterator().next();
		log.debug(job);
		log.debug("Command line "+job.getUnixCommand());
		Assert.assertFalse(job.getUnixCommand().contains("-F \"STUDY=@"));
	}
	
}
