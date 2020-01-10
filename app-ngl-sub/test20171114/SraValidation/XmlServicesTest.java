package SraValidation;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.run.instance.ReadSet;
import models.sra.submit.common.instance.Study;
import models.sra.submit.common.instance.Submission;
import models.sra.submit.sra.instance.Configuration;
import models.sra.submit.sra.instance.Experiment;
import models.sra.submit.util.SraException;
import models.sra.submit.util.VariableSRA;
import models.utils.InstanceConstants;

import org.junit.Assert;
import org.junit.Test;

import play.Logger;
import services.SubmissionServices;
import services.XmlServices;
import utils.AbstractTestsSRA;
import validation.ContextValidation;
import fr.cea.ig.MongoDBDAO;
public class XmlServicesTest extends AbstractTestsSRA {
	
	@Test
	public void validationXmlServicesSuccess() throws IOException, SraException {

		SubmissionServices submissionServices = new SubmissionServices();
		ContextValidation contextValidation = new ContextValidation(userTest);
		contextValidation.setCreationMode();
		contextValidation.getContextObjects().put("type", "sra");
		String projectCode = "AWK";
		// Creer une config et la sauver dans mongodb :
		Configuration config = new Configuration();
		config.code = "conf_AWK_test_xml";
		config.projectCodes.add(projectCode);
		config.strategySample = "strategy_sample_taxon";
		config.librarySelection = "random";
		config.librarySource = "genomic";
		config.libraryStrategy = "wgs";
		config.traceInformation = new TraceInformation(); 
		config.traceInformation.setTraceInformation(userTest);
		config.state = new State("userValidate", userTest);
		config.validate(contextValidation);
		MongoDBDAO.save(InstanceConstants.SRA_CONFIGURATION_COLL_NAME, config);
		contextValidation.displayErrors(Logger.of("SRA"));
		
		// Creer un study validé par utilisateur et le sauver dans mongodb:
		Study study = new Study();
		study.centerName=VariableSRA.centerName;
		study.projectCodes.add("AWK");
		study.centerProjectName = "AWK";
		study.code = "study_AWK_test_xml";
		study.existingStudyType="Metagenomics";
		study.traceInformation.setTraceInformation(userTest);
		study.state = new State("userValidate", userTest);
		contextValidation = new ContextValidation(userTest);
		contextValidation.setCreationMode();
		contextValidation.getContextObjects().put("type", "sra");
		study.validate(contextValidation);
		contextValidation.displayErrors(Logger.of("SRA"));
		MongoDBDAO.save(InstanceConstants.SRA_STUDY_COLL_NAME, study);
		
		
		String codeReadSet1 = "AUP_COSW_4_D09BTACXX.IND7";   // equivalent lotSeqName 
		String codeReadSet2 = "AUP_NAOSW_5_C0UW4ACXX.IND10"; // equivalent lotSeqName 
		String codeReadSet3 = "AKL_ABOSA_1_80MJ3ABXX"; // equivalent lotSeqName 
		String codeReadSet4 = "AWK_EMOSW_1_H9YKWADXX.IND1"; // lotSeqName pairé et avec mapping
		
	
		System.out.println("READSET4="+codeReadSet4);
		List<String> readSetCodes = new ArrayList<String>();
		
		readSetCodes.add(codeReadSet4);

		System.out.println("Create new submission for readSet " + codeReadSet4);
		contextValidation = new ContextValidation(userTest);
		contextValidation.setCreationMode();
		contextValidation.getContextObjects().put("type", "sra");
		//String submissionCode = submissionServices.initNewSubmission(readSetCodes, study.code, config.code, null, null, null, contextValidation);

		String submissionCode = submissionServices.initPrimarySubmission(readSetCodes, study.code, config.code, null, null, null, null, null, contextValidation);

		Submission submission = MongoDBDAO.findByCode(InstanceConstants.SRA_SUBMISSION_COLL_NAME, models.sra.submit.common.instance.Submission.class,  submissionCode);
		System.out.println("Submission " + submission.code);

		// Simuler la fonction SubmissionServices.activate en ajoutant studyCode et sampleCodes 
		// dans la liste des objets à soumettre
		submission.studyCode = submission.refStudyCodes.get(0);
		for (String sampleCode: submission.refSampleCodes) {
			submission.sampleCodes.add(sampleCode);
		}        
		// Sauver la soumission avec les objets study et sample à soumettre
		// (Les objets experiments et run sont forcement à soumettre)
		MongoDBDAO.save(InstanceConstants.SRA_SUBMISSION_COLL_NAME, submission);

		String resultDirectory = "/env/cns/submit_traces/SRA/NGL_test/tests_xml";

		File dataRep = new File(resultDirectory);
		if (!dataRep.exists()){
			dataRep.mkdirs();	
		}		
					
		File studyFile = new File(resultDirectory+"study.xml");
		File sampleFile = new File(resultDirectory+"sample.xml");
		File experimentFile = new File(resultDirectory+"experiment.xml");
		File runFile = new File(resultDirectory+"run.xml");
		File submissionFile = new File(resultDirectory+"submission.xml");
		if (studyFile.exists()){
			studyFile.delete();
		}
		if (sampleFile.exists()){
			sampleFile.delete();
		}
		if (experimentFile.exists()){
			experimentFile.delete();
		}
		if (runFile.exists()){
			runFile.delete();
		}
		if (submissionFile.exists()){
			submissionFile.delete();
		}
		if (dataRep.exists()){
			dataRep.delete();
		}
		
		dataRep.mkdirs();
		/*
		XmlServices.writeStudyXml(submission, studyFile);
		XmlServices.writeSampleXml(submission, sampleFile);
		XmlServices.writeExperimentXml(submission, experimentFile);
		XmlServices.writeRunXml(submission, runFile);
		XmlServices.writeSubmissionXml(submission, submissionFile);
		*/
		
		XmlServices.writeAllXml(submissionCode, resultDirectory);
		MongoDBDAO.deleteByCode(InstanceConstants.SRA_STUDY_COLL_NAME, models.sra.submit.common.instance.Study.class, study.code);
		MongoDBDAO.deleteByCode(InstanceConstants.SRA_CONFIGURATION_COLL_NAME, models.sra.submit.sra.instance.Configuration.class, config.code);
		SubmissionServices.cleanDataBase(submission.code,contextValidation);
		System.out.println("\ndisplayErrors pour validationXmlServicesSuccess :");
		contextValidation.displayErrors(Logger.of("SRA"));		
		Assert.assertTrue(contextValidation.errors.size()==0); 

	}
}
