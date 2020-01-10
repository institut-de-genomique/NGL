package builder.data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;

import fr.cea.ig.MongoDBDAO;
import models.laboratory.run.instance.ReadSet;
import models.sra.submit.common.instance.Sample;
import models.sra.submit.common.instance.Submission;
import models.sra.submit.sra.instance.Configuration;
import models.sra.submit.sra.instance.Experiment;
import models.utils.InstanceConstants;
//import play.Logger;
import utils.AbstractTestController;

public class InitData extends AbstractTestController{
	private static final play.Logger.ALogger logger = play.Logger.of(InitData.class);


	@Test
	public void initDataDevForWorkflow() throws ParseException
	{
		//Create Submission for submission
		String codeSub1 = "codeSub1";
		//Create submission with no zip file
		String codeSub2 = "codeSub2";
		String codeExp1 = "codeExp1";
		String codeExp2 = "codeExp2";
		String codeExp3 = "codeExp3";
		String codeExp4 = "codeExp4";
		String codeSamp1 = "codeSamp1";
		String codeRun1 = "codeRun1";
		String codeRun2 = "codeRun2";
		String codeReadSet ="codeReadSet";
		String codeReadSet2 ="codeReadSet2";
		String codeConfig = "codeConfig";

		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		Date dateSubmission =sdf.parse("26/01/2015");

		MongoDBDAO.deleteByCode(InstanceConstants.SRA_SUBMISSION_COLL_NAME, Submission.class,codeSub1);
		MongoDBDAO.deleteByCode(InstanceConstants.SRA_SUBMISSION_COLL_NAME, Submission.class,codeSub2);
		MongoDBDAO.deleteByCode(InstanceConstants.SRA_EXPERIMENT_COLL_NAME, Experiment.class,codeExp1);
		MongoDBDAO.deleteByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class,codeReadSet);
		MongoDBDAO.deleteByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class,codeReadSet2);
		MongoDBDAO.deleteByCode(InstanceConstants.SRA_EXPERIMENT_COLL_NAME, Submission.class,codeExp2);
		MongoDBDAO.deleteByCode(InstanceConstants.SRA_EXPERIMENT_COLL_NAME, Submission.class,codeExp3);
		MongoDBDAO.deleteByCode(InstanceConstants.SRA_EXPERIMENT_COLL_NAME, Submission.class,codeExp4);
		MongoDBDAO.deleteByCode(InstanceConstants.SRA_SAMPLE_COLL_NAME, Sample.class, codeSamp1);
		MongoDBDAO.deleteByCode(InstanceConstants.SRA_CONFIGURATION_COLL_NAME, Configuration.class, codeConfig);


		Configuration configuration = new ConfigurationBuilder()
				.withCode(codeConfig)
				.withState(new StateBuilder()
						.withCode("userValidate")
						.withUser("userTest")
						.build())
				.build();
		MongoDBDAO.save(InstanceConstants.SRA_CONFIGURATION_COLL_NAME, configuration);

		Submission submission = new SubmissionBuilder()
				.withCode(codeSub1)
				.withProjectCode("BCZ")
				.withSubmissionDirectory(System.getProperty("user.home")+"/NGL-SUB-Test/subDir")
				//.withSubmissionTmpDirectory(System.getProperty("user.home")+"/NGL-SUB-Test/tmpSubDir")
				.withSubmissionDate(dateSubmission)
				.withState(new StateBuilder()
						.withCode("IW-SUB")
						.withUser("ejacoby@genoscope.cns.fr")
						.build())
				.withConfigCode(configuration.code)
				.withTraceInformation(new TraceInformationBuilder()
						.withCreateUser("userTest")
						.withCreationDate(new Date())
						.withModifyUser("userTest")
						.withModifyDate(new Date())
						.build())
				.addExperimentCode(codeExp1)
				.addExperimentCode(codeExp2)
				.addSampleCode(codeSamp1)
				.addRunCode(codeRun1)
				.addRunCode(codeRun2)
				.build();

		MongoDBDAO.save(InstanceConstants.SRA_SUBMISSION_COLL_NAME, submission);

		Submission submission2 = new SubmissionBuilder()
				.withCode(codeSub2)
				.withProjectCode("BCZ")
				.withSubmissionDirectory(System.getProperty("user.home")+"/NGL-SUB-Test/subDir")
				//.withSubmissionTmpDirectory(System.getProperty("user.home")+"/NGL-SUB-Test/tmpSubDir")
				.withSubmissionDate(dateSubmission)
				.withState(new StateBuilder()
						.withCode("IW-SUB")
						.withUser("ejacoby@genoscope.cns.fr")
						.build())
				.withConfigCode(configuration.code)
				.withTraceInformation(new TraceInformationBuilder()
						.withCreateUser("userTest")
						.withCreationDate(new Date())
						.withModifyUser("userTest")
						.withModifyDate(new Date())
						.build())
				.addExperimentCode(codeExp3)
				.addExperimentCode(codeExp4)
				.addSampleCode(codeSamp1)
				.addRunCode(codeRun1)
				.addRunCode(codeRun2)
				.build();
		
		MongoDBDAO.save(InstanceConstants.SRA_SUBMISSION_COLL_NAME, submission2);
		
		Experiment experiment1 = new ExperimentBuilder()
				.withCode(codeExp1)
				.withRun(new RunBuilder()
						.withCode(codeRun1)
						.addRawData(new RawDataBuilder()
								.withRelatifName("file1.fastq.gz").build())
						.addRawData(new RawDataBuilder()
								.withRelatifName("file2.fastq.gz").build())
						.build())
				.withReadSetCode(codeReadSet)
				.withState(new StateBuilder().build())
				.build();
		Experiment experiment2 = new ExperimentBuilder()
				.withCode(codeExp2)
				.withRun(new RunBuilder()
						.withCode(codeRun2)
						.addRawData(new RawDataBuilder()
								.withRelatifName("file3.fastq.gz").build())
						.addRawData(new RawDataBuilder()
								.withRelatifName("file4.fastq.gz").build())
						.build())
				.withReadSetCode(codeReadSet2)
				.withState(new StateBuilder().build())
				.build();
		MongoDBDAO.save(InstanceConstants.SRA_EXPERIMENT_COLL_NAME, experiment2);
		MongoDBDAO.save(InstanceConstants.SRA_EXPERIMENT_COLL_NAME, experiment1);
		
		Experiment experiment3 = new ExperimentBuilder()
				.withCode(codeExp3)
				.withRun(new RunBuilder()
						.withCode(codeRun1)
						.addRawData(new RawDataBuilder()
								.withRelatifName("file5.fastq")
								.withDirectory(System.getProperty("user.home")+"/NGL-SUB-Test/dataDir")
								.withGzipForSubmission(true).build())
						.addRawData(new RawDataBuilder()
								.withRelatifName("file6.fastq.gz").build())
						.build())
				.withReadSetCode(codeReadSet)
				.withState(new StateBuilder().build())
				.build();
		Experiment experiment4 = new ExperimentBuilder()
				.withCode(codeExp4)
				.withRun(new RunBuilder()
						.withCode(codeRun2)
						.addRawData(new RawDataBuilder()
								.withRelatifName("file7.fastq")
								.withDirectory(System.getProperty("user.home")+"/NGL-SUB-Test/dataDir")
								.withGzipForSubmission(true).build())
						.addRawData(new RawDataBuilder()
								.withRelatifName("file8.fastq.gz").build())
						.build())
				.withReadSetCode(codeReadSet2)
				.withState(new StateBuilder().build())
				.build();
		MongoDBDAO.save(InstanceConstants.SRA_EXPERIMENT_COLL_NAME, experiment3);
		MongoDBDAO.save(InstanceConstants.SRA_EXPERIMENT_COLL_NAME, experiment4);

		ReadSet readSet = new ReadSetBuilder()
				.withCode(codeReadSet)
				.withTraceInformation(new TraceInformationBuilder()
						.withCreateUser("ejacoby")
						.build())
				.withState(new StateBuilder().build())
				.withSubmissionState(new StateBuilder().build())
				.build();
		ReadSet readSet2 = new ReadSetBuilder()
				.withCode(codeReadSet2)
				.withTraceInformation(new TraceInformationBuilder()
						.withCreateUser("ejacoby")
						.build())
				.withState(new StateBuilder().build())
				.withSubmissionState(new StateBuilder().build())
				.build();
		MongoDBDAO.save(InstanceConstants.READSET_ILLUMINA_COLL_NAME, readSet);
		MongoDBDAO.save(InstanceConstants.READSET_ILLUMINA_COLL_NAME, readSet2);




		Sample sample1 = new SampleBuilder()
				.withCode(codeSamp1)
				.build();
		MongoDBDAO.save(InstanceConstants.SRA_SAMPLE_COLL_NAME, sample1);

		//Log data submitted
		Submission submissionDB = MongoDBDAO.findByCode(InstanceConstants.SRA_SUBMISSION_COLL_NAME, Submission.class, codeSub1);
		logger.info("Submission "+submissionDB.code+","+submissionDB.submissionDirectory+","+submissionDB.creationDate);

	}
}
