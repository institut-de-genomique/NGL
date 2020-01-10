package models.dao.type;

//import static play.test.Helpers.fakeApplication;
//import java.util.Map;
import models.laboratory.experiment.description.ExperimentType;
import models.laboratory.instrument.description.InstrumentUsedType;
import models.laboratory.processes.description.ProcessType;
import models.laboratory.project.description.ProjectType;
//import models.laboratory.reagent.description.ReagentCatalog;
import models.laboratory.run.description.TreatmentType;
import models.laboratory.sample.description.ImportType;
import models.laboratory.sample.description.SampleType;
//import models.utils.DescriptionHelper;
import models.utils.dao.DAOException;
//import org.junit.AfterClass;
import org.junit.Assert;
//import org.junit.BeforeClass;
import org.junit.Test;
//import play.Logger;
//import play.Logger.ALogger;
//import play.test.FakeApplication;
//import play.test.Helpers;
import utils.AbstractTests;

public class AbstractTypeDAOTest extends AbstractTests {

	protected static play.Logger.ALogger logger = play.Logger.of("AbstractTypeDAOTest");

	@Test
	public void experimentTypeFindTest() throws DAOException {
		ExperimentType experimentType=ExperimentType.find.findAll().get(0);
		Assert.assertNotNull(experimentType);
		ExperimentType expType=ExperimentType.find.findByCode(experimentType.code);
		Assert.assertNotNull(expType);
		ExperimentType expTypeId=ExperimentType.find.findById(experimentType.id);
		Assert.assertNotNull(expTypeId);
		Assert.assertFalse(ExperimentType.find.isCodeExist(""));
		Assert.assertNotNull(ExperimentType.find.findAllForList());	
		Assert.assertNotNull(ExperimentType.find.findVoidProcessExperimentTypeCode(ProcessType.find.findAll().get(0).code));
		Assert.assertNotNull(ExperimentType.find.findPreviousExperimentTypeForAnExperimentTypeCode(experimentType.code));
		Assert.assertNotNull(ExperimentType.find.findByCategoryCode(""));
		Assert.assertNotNull(ExperimentType.find.findByCategoryCodeWithoutOneToVoid("transformation"));
	}

	@Test
	public void projectTypeFindTest() throws DAOException {
		ProjectType type=ProjectType.find.findAll().get(0);
		Assert.assertNotNull(type);
		Assert.assertNotNull(type.code);
		ProjectType cType=ProjectType.find.findByCode(type.code);
		Assert.assertNotNull(cType);
		ProjectType cTypeId=ProjectType.find.findById(type.id);
		Assert.assertNotNull(cTypeId);
		Assert.assertFalse(ProjectType.find.isCodeExist(""));
		Assert.assertNotNull(ProjectType.find.findAllForList());
	}

	@Test
	public void processTypeFindTest() throws DAOException {
		ProcessType type=ProcessType.find.findAll().get(0);
		Assert.assertNotNull(type);
		ProcessType cType=ProcessType.find.findByCode(type.code);
		Assert.assertNotNull(cType);
		ProcessType cTypeId=ProcessType.find.findById(type.id);
		Assert.assertNotNull(cTypeId);
		Assert.assertFalse(ProcessType.find.isCodeExist(""));
		Assert.assertNotNull(ProcessType.find.findAll());
	}

	@Test
	public void sampleTypeFindTest() throws DAOException {
		SampleType type=SampleType.find.findAll().get(0);
		Assert.assertNotNull(type);
		SampleType cType=SampleType.find.findByCode(type.code);
		Assert.assertNotNull(cType);
		SampleType cTypeId=SampleType.find.findById(type.id);
		Assert.assertNotNull(cTypeId);
		Assert.assertFalse(SampleType.find.isCodeExist(""));
		Assert.assertNotNull(SampleType.find.findAllForList());
	}

	@Test
	public void importTypeFindTest() throws DAOException {		
		ImportType type=ImportType.find.findAll().get(0);
		Assert.assertNotNull(type);
		ImportType cType=ImportType.find.findByCode(type.code);
		Assert.assertNotNull(cType);
		ImportType cTypeId=ImportType.find.findById(type.id);
		Assert.assertNotNull(cTypeId);
		Assert.assertFalse(ImportType.find.isCodeExist(""));
		Assert.assertNotNull(ImportType.find.findAllForList());
	}
	
	@Test
	public void instrumentUsedTypeFindTest() throws DAOException {		
		InstrumentUsedType type=InstrumentUsedType.find.findAll().get(0);
		Assert.assertNotNull(type);
		InstrumentUsedType cType=InstrumentUsedType.find.findByCode(type.code);
		Assert.assertNotNull(cType);
		InstrumentUsedType cTypeId=InstrumentUsedType.find.findById(type.id);
		Assert.assertNotNull(cTypeId);
		Assert.assertFalse(InstrumentUsedType.find.isCodeExist(""));
		Assert.assertNotNull(InstrumentUsedType.find.findAll());
	}

	@Test
	public void readSetTypeFindTest() throws DAOException {		
		ExperimentType type=ExperimentType.find.findAll().get(0);
		Assert.assertNotNull(type);
		ExperimentType cType=ExperimentType.find.findByCode(type.code);
		Assert.assertNotNull(cType);
		ExperimentType cTypeId=ExperimentType.find.findById(type.id);
		Assert.assertNotNull(cTypeId);
		Assert.assertFalse(ExperimentType.find.isCodeExist(""));
		Assert.assertNotNull(ExperimentType.find.findAllForList());
	}

	@Test
	public void runTypeFindTest() throws DAOException {		
		ExperimentType type=ExperimentType.find.findAll().get(0);
		Assert.assertNotNull(type);
		ExperimentType cType=ExperimentType.find.findByCode(type.code);
		Assert.assertNotNull(cType);
		ExperimentType cTypeId=ExperimentType.find.findById(type.id);
		Assert.assertNotNull(cTypeId);
		Assert.assertFalse(ExperimentType.find.isCodeExist(""));
		Assert.assertNotNull(ExperimentType.find.findAllForList());
	}
	
	@Test
	public void treatmentTypeFindTest() throws DAOException {		
		TreatmentType type=TreatmentType.find.findAll().get(0);
		Assert.assertNotNull(type);
		TreatmentType cType=TreatmentType.find.findByCode(type.code);
		Assert.assertNotNull(cType);
		TreatmentType cTypeId=TreatmentType.find.findById(type.id);
		Assert.assertNotNull(cTypeId);
		Assert.assertFalse(TreatmentType.find.isCodeExist(""));
		Assert.assertNotNull(TreatmentType.find.findAllForList());
	}
	
	//@Test
	/*public void reagentTypeFindTest() throws DAOException {		
		ReagentCategory type=ReagentCategory.find.findAll().get(0);
		Assert.assertNotNull(type);
		ReagentCategory cType=ReagentCategory.find.findByCode(type.code);
		Assert.assertNotNull(cType);
		ReagentCategory cTypeId=ReagentCategory.find.findById(type.id);
		Assert.assertNotNull(cTypeId);
		Assert.assertFalse(ReagentCategory.find.isCodeExist(""));
		Assert.assertNotNull(ReagentCategory.find.findAllForList());
		//Assert.assertNotNull(ReagentType.find.findByProtocol(0));
	}*/

}
