package SraValidation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.run.instance.ReadSet;
import models.sra.submit.common.instance.Study;
import models.sra.submit.sra.instance.Configuration;
import models.sra.submit.sra.instance.Experiment;
import models.sra.submit.util.SraCodeHelper;
import models.sra.submit.util.SraException;
import models.sra.submit.util.VariableSRA;
import models.utils.InstanceConstants;

import org.junit.Test;

import services.DbUtil;
import services.SubmissionServices;
import utils.AbstractTests;
import utils.AbstractTestsSRA;
import validation.ContextValidation;
import validation.IValidation;
import fr.cea.ig.DBObject;
import fr.cea.ig.MongoDBDAO;
import play.Logger;

public class SubmissionTest extends AbstractTestsSRA {
/*	@AfterClass
	public void finalize()
	{
	
	}
	*/
	//@Test
	/*public void testSubmission() throws IOException, SraException
	{
		SubmissionServices submissionServices = new SubmissionServices();
		DbUtil sraDbServices = new DbUtil();
		String user = "william";
		String projectCode = "BCZ";
		ContextValidation contextValidation = new ContextValidation(user);
		contextValidation.setCreationMode();		
		Configuration config = new Configuration();
		config.code = SraCodeHelper.getInstance().generateSubmissionCode(projectCode);
		config.projectCode = projectCode;
		config.strategySample = "strategy_sample_taxon";
		config.librarySelection = "random";
		config.librarySource = "genomic";
		config.libraryStrategy = "wgs";
		config.traceInformation = new TraceInformation(); 
		config.traceInformation.setTraceInformation(user);
		config.state = new State("userValidate", user);
		config.validate(contextValidation);
		MongoDBDAO.save(InstanceConstants.SRA_CONFIGURATION_COLL_NAME, config);
		System.out.println("contextValidation.errors pour submissionTest.configuration :");
		contextValidation.displayErrors(Logger.of("SRA"));

		
		// Creer un study validé par utilisateur et le sauver dans mongodb:
		contextValidation = new ContextValidation(user);
		Study study = new Study();
		study.centerName=VariableSRA.centerName;
		study.projectCode = projectCode;
		study.centerProjectName = projectCode;
		study.code = SraCodeHelper.getInstance().generateStudyCode(projectCode);
		study.existingStudyType="Metagenomics";
		study.traceInformation.setTraceInformation(user);
		study.state = new State("userValidate", user);
		contextValidation.setCreationMode();
		contextValidation.getContextObjects().put("type", "sra");
		
		study.validate(contextValidation);
		
		System.out.println("contextValidation.errors pour submissionTest.study :");
		contextValidation.displayErrors(Logger.of("SRA"));

		MongoDBDAO.save(InstanceConstants.SRA_STUDY_COLL_NAME, study);
		
		
		
		String codeReadSet1 = "AUP_COSW_4_D09BTACXX.IND7";   // equivalent lotSeqName 
		String codeReadSet2 = "AUP_NAOSW_5_C0UW4ACXX.IND10"; // equivalent lotSeqName 
		String codeReadSet3 = "AKL_ABOSA_1_80MJ3ABXX"; // equivalent lotSeqName 
		String codeReadSet4 = "AWK_EMOSW_1_H9YKWADXX.IND1"; // lotSeqName pairé et avec mapping

		String codeReadSet5 = "BCZ_BGOSW_2_H9M6KADXX.IND15"; 
		String codeReadSet6 = "BCZ_BIOSW_2_H9M6KADXX.IND19"; 
		String codeReadSet7 = "BCZ_BAAOSW_1_A7PE4.IND8"; 
		// ex de donnée illumina single : AUP_COSW_4_D09BTACXX.IND7
		// ex de donnée illumina paired : AUP_NAOSW_5_C0UW4ACXX.IND10, AKL_ABOSA_1_80MJ3ABXX

		System.out.println("READSET="+codeReadSet5);
		List<String> readSetCodes = new ArrayList<String>();
		
		
		//readSetCodes.add(codeReadSet5);
		//readSetCodes.add(codeReadSet6);
		readSetCodes.add(codeReadSet7);
		System.out.println("Create new submission for readSet " + readSetCodes.get(0));
		String submissionCode = null;
		try {
			contextValidation = new ContextValidation(userTest);
			contextValidation.setCreationMode();
			contextValidation.getContextObjects().put("type", "sra");
			for (String readSetCode : readSetCodes) {
				System.out.println("dans submissionTest        readSetCode = " + readSetCode);
			}
			submissionCode = submissionServices.initPrimarySubmission(readSetCodes, study.code, config.code, null, null, null, contextValidation);
			System.out.println("Enregistrement du submissionCode : " + submissionCode); 
			System.out.println("contextValidation.errors pour submissionTest :" + submissionCode);
			contextValidation.displayErrors(Logger.of("SRA"));
			System.out.println("Appel depuis SubmissionTest de cleanDataBase");
			SubmissionServices.cleanDataBase(submissionCode,contextValidation);
			MongoDBDAO.deleteByCode(InstanceConstants.SRA_STUDY_COLL_NAME, models.sra.submit.common.instance.Study.class, study.code);
			MongoDBDAO.deleteByCode(InstanceConstants.SRA_CONFIGURATION_COLL_NAME, models.sra.submit.sra.instance.Configuration.class, config.code);
		} catch (IOException e) {
			System.out.println("Exception de type IO: " + e.getMessage());
		} catch (SraException e) {
			System.out.println("Exception de type SRA: " +e.getMessage());
		} 
	}
*/

}
