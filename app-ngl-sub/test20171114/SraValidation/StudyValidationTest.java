package SraValidation;

import java.io.IOException;

import models.laboratory.common.instance.TraceInformation;
import models.sra.submit.common.instance.Study;
import models.sra.submit.util.SraException;
import models.sra.submit.util.VariableSRA;
import models.utils.InstanceConstants;

import org.junit.Assert;
import org.junit.Test;

import validation.ContextValidation;
import fr.cea.ig.MongoDBDAO;
import utils.AbstractTestsSRA;
import play.Logger;


// Pour les tests utilisant MongoDb mettre extents AbstractTests
public class StudyValidationTest extends AbstractTestsSRA {
	//@BeforeClass appelé 1! fois avant tests
	//@Before appelee avant chaque test

	@Test
	public void validationStudySuccess() throws IOException, SraException {
		Study study = new Study();
		// Tests study avec infos obligatoires presentes et bonnes valeurs et mode
		// creation ou update.		
		study.centerName=VariableSRA.centerName;
		study.projectCodes.add("AWK");
		study.centerProjectName = "AWK";
		study.code = "study_AWK_test";
		study.existingStudyType="Metagenomics";
		study.traceInformation = new TraceInformation(); 
		study.traceInformation.setTraceInformation("william");
		ContextValidation contextValidation = new ContextValidation(userTest);
		contextValidation.setCreationMode();
	
		contextValidation.getContextObjects().put("type", "sra");
		study.validate(contextValidation);
		
		MongoDBDAO.save(InstanceConstants.SRA_STUDY_COLL_NAME, study);
		MongoDBDAO.deleteByCode(InstanceConstants.SRA_STUDY_COLL_NAME, models.sra.submit.common.instance.Study.class, study.code);
		System.out.println("contextValidation.errors pour validationStudySuccess :");
		contextValidation.displayErrors(Logger.of("SRA"));
		Assert.assertTrue(contextValidation.errors.size()==0); // si aucune erreur
	}

	@Test
	public void validationStudyEchec() throws IOException, SraException {

		Study study = new Study();
		study.centerName="centerNameToto";
		study.projectCodes.add("centerProjectCodeToto");
		study.centerProjectName = "centerProjectNameToto";
		study.code = "study_test_2";
		study.existingStudyType="MetagenomicsToto";
		ContextValidation contextValidation = new ContextValidation(userTest);
		contextValidation.setCreationMode();
		study.validate(contextValidation);
		MongoDBDAO.save(InstanceConstants.SRA_STUDY_COLL_NAME, study);
		MongoDBDAO.deleteByCode(InstanceConstants.SRA_STUDY_COLL_NAME, models.sra.submit.common.instance.Study.class, study.code);
		System.out.println("contextValidation.errors pour validationStudyEchec :");
		contextValidation.displayErrors(Logger.of("SRA"));
		Assert.assertTrue(contextValidation.errors.size()==5); // On attend 5 erreurs
	}


}
