package SraValidation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import mail.MailServiceException;
import models.sra.submit.common.instance.Sample;
import models.sra.submit.common.instance.Study;
import models.sra.submit.common.instance.Submission;
import models.sra.submit.sra.instance.Configuration;
import models.sra.submit.sra.instance.Experiment;
import models.sra.submit.util.SraException;
import models.sra.submit.util.VariableSRA;
import models.utils.InstanceConstants;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.run.instance.ReadSet;

import org.junit.Test;

import play.Logger;

import services.FileAcServices;
import services.SubmissionServices;
import validation.ContextValidation;
import fr.cea.ig.MongoDBDAO;
import validation.ContextValidation;
import utils.AbstractTestsSRA;

import org.junit.Assert;
import org.junit.Test;

public class FileAcServicesTest  extends AbstractTestsSRA {

	public static void deleteDataSetForFileAcServices()throws SraException {
		String studyCode = "test_AC_study_AWK";
		String configCode = "test_AC_conf_AWK";
		String submissionCode="test_AC_cns_AWK";
		String readSetCode = "AWK_EMOSW_1_H9YKWADXX.IND1"; // lotSeqName pairé et avec mapping
		String experimentCode = "test_AC_exp_" + readSetCode;
		String sampleCode = "test_AC_sample_AWK_472";
		String runCode = "test_AC_run_" + readSetCode;
		String submissionDirectory = "/env/cns/submit_traces/SRA/NGL_test/tests_AC/";
				MongoDBDAO.deleteByCode(InstanceConstants.SRA_STUDY_COLL_NAME, models.sra.submit.common.instance.Study.class, studyCode);		
		MongoDBDAO.deleteByCode(InstanceConstants.SRA_SAMPLE_COLL_NAME, models.sra.submit.common.instance.Sample.class, sampleCode);		
		MongoDBDAO.deleteByCode(InstanceConstants.SRA_CONFIGURATION_COLL_NAME, models.sra.submit.sra.instance.Configuration.class, configCode);		
		MongoDBDAO.deleteByCode(InstanceConstants.SRA_EXPERIMENT_COLL_NAME, models.sra.submit.sra.instance.Experiment.class, experimentCode);		
		MongoDBDAO.deleteByCode(InstanceConstants.SRA_SUBMISSION_COLL_NAME, models.sra.submit.common.instance.Submission.class, submissionCode);		
	}
	
	public static void createDataSetForFileAcServices()throws SraException {
		// Creation d'une soumission complete en attente des numeros d'accession :
		String status = "inprogress"; // a changer pour waitingAc ?
		String projectCode = "AWK";
		String studyCode = "test_AC_study_AWK";
		String configCode = "test_AC_conf_AWK";
		String submissionCode="test_AC_cns_AWK";
		String readSetCode = "AWK_EMOSW_1_H9YKWADXX.IND1"; // lotSeqName pairé et avec mapping
		String experimentCode = "test_AC_exp_" + readSetCode;
		String sampleCode = "test_AC_sample_AWK_472";
		String runCode = "test_AC_run_" + readSetCode;
		String submissionDirectory = "/env/cns/submit_traces/SRA/NGL_test/tests_AC/";

		// Creer objet config valide avec status = userValidate pour test :
		SubmissionServices SubmissionServices = new SubmissionServices();
		String user = "william";
		ContextValidation contextValidation = new ContextValidation(user);
		contextValidation.setCreationMode();		
		Configuration config = new Configuration();
		config.code = configCode;
		config.projectCodes.add(projectCode);
		config.strategySample = "strategy_sample_taxon";
		config.librarySelection = "random";
		config.librarySource = "genomic";
		config.libraryStrategy = "wgs";
		config.traceInformation = new TraceInformation(); 
		config.traceInformation.setTraceInformation(user);
		config.state = new State("userValidate", user);
		config.validate(contextValidation);
		MongoDBDAO.save(InstanceConstants.SRA_CONFIGURATION_COLL_NAME, config);
		contextValidation.displayErrors(Logger.of("SRA"));
		
		// Creer un study valide avec un status inWaiting (on ne prend pas status userValidate) et le sauver dans mongodb:
		Study study = new Study();
		study.centerName=VariableSRA.centerName;
		study.projectCodes.add(projectCode);
		study.centerProjectName = projectCode;
		study.code = studyCode;
		study.existingStudyType="Metagenomics";
		study.traceInformation.setTraceInformation(user);
		study.state = new State(status, user);
		contextValidation.getContextObjects().put("type", "sra");
		study.validate(contextValidation);
		contextValidation.displayErrors(Logger.of("SRA"));
		MongoDBDAO.save(InstanceConstants.SRA_STUDY_COLL_NAME, study);

		// creer un objet Experiment (avec son run associé) valide avec un status "inWaiting" et le sauver dans mongodb :
		ReadSet readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSetCode);
		String scientificName = "scientificName bidon";
		Experiment experiment = SubmissionServices.createExperimentEntity(readSet, scientificName, "william");
		// changer le code de experiment :
		experiment.code = experimentCode;
		experiment.sampleCode = sampleCode;
		experiment.studyCode = studyCode;
		experiment.run.code = runCode;
		experiment.librarySelection = config.librarySelection;
		experiment.librarySource = config.librarySource;
		experiment.libraryStrategy = config.libraryStrategy;
		experiment.libraryName = "testLibraryName";
		experiment.state = new State(status, user);
		experiment.validate(contextValidation);
		contextValidation.displayErrors(Logger.of("SRA"));
		MongoDBDAO.save(InstanceConstants.SRA_EXPERIMENT_COLL_NAME, experiment);

		// Creer un objet sample avec status inWaiting et le sauver dans mongodb :
		Sample sample = new Sample();
		sample.code = sampleCode;
		sample.taxonId = new Integer(472);
		sample.clone = "Acineto_cDNA_SMARTST_1ng_Ctrl";
		sample.projectCode = projectCode;
		sample.state = new State(status, user);
		sample.traceInformation.setTraceInformation(user);		
		contextValidation.getContextObjects().put("type", "sra");
		sample.validate(contextValidation);
		contextValidation.displayErrors(Logger.of("SRA"));
		MongoDBDAO.save(InstanceConstants.SRA_SAMPLE_COLL_NAME, sample);

		// Creer un objet submission avec status inWaiting et le sauver dans mongodb :
		//Submission submission = MongoDBDAO.findByCode(InstanceConstants.SRA_SUBMISSION_COLL_NAME, models.sra.submission.instance.Submission.class,  submissionCode);
		Submission submission = new Submission(user, config.projectCodes);
		submission.submissionDirectory = submissionDirectory;
		submission.code = submissionCode;
		//System.out.println("submissionCode="+ submissionCode);
		submission.state = new State("new", user);
		submission.configCode = config.code;
		submission.studyCode = studyCode;
		submission.sampleCodes.add(sampleCode);
		submission.experimentCodes.add(experimentCode);
		submission.runCodes.add(runCode);
		submission.state = new State(status, user);
		contextValidation.getContextObjects().put("type", "sra");
		submission.validate(contextValidation);
		contextValidation.displayErrors(Logger.of("SRA"));
		MongoDBDAO.save(InstanceConstants.SRA_SUBMISSION_COLL_NAME, submission);
	}

	//@Test
	public void FileAcServicesSuccess() throws IOException, SraException, MailServiceException {
		// Creation d'une soumission "test_AC_cns_AWK" avec status en attente des numeros d'accession : 
		String submissionCode="test_AC_cns_AWK";
		deleteDataSetForFileAcServices();
		createDataSetForFileAcServices();
		
		File fileEbi = new File("/env/cns/submit_traces/SRA/NGL_test/tests_AC/RESULT_AC_ok");
		//Submission submission = MongoDBDAO.findByCode(InstanceConstants.SRA_SUBMISSION_COLL_NAME, Submission.class, submissionCode);
		
		String user = "william";
		ContextValidation ctxVal = new ContextValidation(user);
		Submission submission = FileAcServices.traitementFileAC(ctxVal, submissionCode, fileEbi); 
		
		Assert.assertTrue(MongoDBDAO.checkObjectExist(InstanceConstants.SRA_SUBMISSION_COLL_NAME, Submission.class, "accession", "ERA000000"));
		deleteDataSetForFileAcServices();
		
	}
	
	
	//@Test
	public void FileAcServicesEchec() throws IOException, SraException, MailServiceException {

		// Creation d'une soumission "test_AC_cns_AWK" avec status en attente des numeros d'accession : 
		String submissionCode="test_AC_cns_AWK";
		deleteDataSetForFileAcServices();
		createDataSetForFileAcServices();
		// Fichier des AC incomplet sans le bon alias pour experiment:
		File fileEbi = new File("/env/cns/submit_traces/SRA/NGL_test/tests_AC/RESULT_AC_ERROR");
		//Submission submission = MongoDBDAO.findByCode(InstanceConstants.SRA_SUBMISSION_COLL_NAME, Submission.class, submissionCode);
		String user = "william";
		ContextValidation ctxVal = new ContextValidation(user);
		FileAcServices.traitementFileAC(ctxVal, submissionCode, fileEbi);
		Assert.assertTrue(!MongoDBDAO.checkObjectExist(InstanceConstants.SRA_SUBMISSION_COLL_NAME, Submission.class, "accession", "ERA000000"));
		deleteDataSetForFileAcServices();

	}
	
	//@Test
	public void FileAcServicesReal() throws IOException, SraException, MailServiceException {
		Submission submission;
		String code = "CNS_CAA_28HF4VOO4";
		String user = "william";
		ContextValidation ctxVal = new ContextValidation(user);
		File fileEbi = new File("/env/cns/submit_traces/SRA/SNTS_output_xml/NGL/CAA/17_08_2017/list_AC_CNS_CAA_28HF4VOO4.txt");
		//Submission submission = FileAcServices.traitementFileAC(ctxVal, code, fileEbi);
		
		code = "CNS_BZZ_CAN_28HD5LO4V";
		ctxVal = new ContextValidation(user);
		fileEbi = new File("/env/cns/submit_traces/SRA/SNTS_output_xml/NGL/BZZ_CAN/17_08_2017/list_AC_CNS_BZZ_CAN_28HD5LO4V.txt");
		//submission = FileAcServices.traitementFileAC(ctxVal, code, fileEbi);
		
		
		code = "CNS_BZZ_CAN_28HD4ZIGG";
		ctxVal = new ContextValidation(user);
		fileEbi = new File("/env/cns/submit_traces/SRA/SNTS_output_xml/NGL/CNS_BZZ_CAN_28HD4ZIGG/17_08_2017/list_AC_CNS_BZZ_CAN_28HD4ZIGG.txt");
		//submission = FileAcServices.traitementFileAC(ctxVal, code, fileEbi);
		

		code = "CNS_BZZ_CAN_28HD59RR8";
		ctxVal = new ContextValidation(user);
		fileEbi = new File("/env/cns/submit_traces/SRA/SNTS_output_xml/NGL/CNS_BZZ_CAN_28HD59RR8/17_08_2017/list_AC_CNS_BZZ_CAN_28HD59RR8.txt");
		submission = FileAcServices.traitementFileAC(ctxVal, code, fileEbi);
			

		code = "CNS_BZZ_CAN_28HD5FO6C";
		ctxVal = new ContextValidation(user);
		fileEbi = new File("/env/cns/submit_traces/SRA/SNTS_output_xml/NGL/CNS_BZZ_CAN_28HD5FO6C/17_08_2017/list_AC_CNS_BZZ_CAN_28HD5FO6C.txt");
		submission = FileAcServices.traitementFileAC(ctxVal, code, fileEbi);
		
		ctxVal.displayErrors(Logger.of("SRA"));

		//Assert.assertTrue(MongoDBDAO.checkObjectExist(InstanceConstants.SRA_SUBMISSION_COLL_NAME, Submission.class, "accession", "ERA960789"));
	}
}
