package SraValidation;
import java.io.IOException;

import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.run.instance.ReadSet;
import models.sra.submit.common.instance.Submission;
import models.sra.submit.sra.instance.Configuration;
import models.sra.submit.sra.instance.Run;
import models.sra.submit.util.SraException;
import models.utils.InstanceConstants;

import org.junit.Assert;
import org.junit.Test;

import play.Logger;
import fr.cea.ig.MongoDBDAO;
import services.SubmissionServices;
import utils.AbstractTestsSRA;
import validation.ContextValidation;

public class SubmissionValidationTest extends AbstractTestsSRA {
	
	@Test
	public void validationSubmissionSuccess() throws IOException, SraException {
		ContextValidation contextValidation = new ContextValidation(userTest);
		String projectCode = "AWK";
		String codeReadSet = "AWK_EMOSW_1_H9YKWADXX.IND1"; // lotSeqName pairé et avec mapping
		ReadSet readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, codeReadSet);
		SubmissionServices submissionServices = new SubmissionServices();
		Configuration config = new Configuration();
		config.code = "conf_AWK_test";
		config.projectCodes.add("AWK");
		config.strategySample = "strategy_sample_taxon";
		config.librarySelection = "random";
		config.librarySource = "genomic";
		config.libraryStrategy = "wgs";
		config.traceInformation = new TraceInformation(); 
		config.traceInformation.setTraceInformation(userTest);
		config.state = new State("userValidate", userTest);
/*
		MongoDBDAO.save(InstanceConstants.SRA_CONFIGURATION_COLL_NAME, config);
		contextValidation.getContextObjects().put("type", "sra");
		contextValidation.setCreationMode();
		Submission submission = submissionServices.createSubmissionEntity(projectCode, config.code, userTest);
		MongoDBDAO.deleteByCode(InstanceConstants.SRA_CONFIGURATION_COLL_NAME, models.sra.submit.sra.instance.Configuration.class, config.code);
		submission.studyCode = "study_AWK_test";
		submission.traceInformation = new TraceInformation(); 
		submission.traceInformation.setTraceInformation(userTest);
		submission.validate(contextValidation);
		System.out.println("\ndisplayErrors pour validationSubmissionSuccess :");
		contextValidation.displayErrors(Logger.of("SRA"));
		MongoDBDAO.deleteByCode(InstanceConstants.SRA_CONFIGURATION_COLL_NAME, models.sra.submit.sra.instance.Configuration.class, config.code);
		Assert.assertTrue(contextValidation.errors.size()==0); // si aucune erreur
	*/	
	}
}
