package models.dao.type;

import org.junit.Assert;
import org.junit.Test;

import fr.cea.ig.util.function.CC1;
import models.laboratory.experiment.description.ExperimentType;
import models.laboratory.instrument.description.InstrumentUsedType;
import models.laboratory.processes.description.ProcessType;
import models.laboratory.project.description.ProjectType;
import models.laboratory.run.description.TreatmentType;
import models.laboratory.sample.description.ImportType;
import models.laboratory.sample.description.SampleType;
import ngl.common.Global;
import play.Application;

public class AbstractTypeDAOTest {

	protected static play.Logger.ALogger logger = play.Logger.of(AbstractTypeDAOTest.class);
	
	private static final CC1<Application> af = Global.afSq.cc1();
	
	@Test
	public void experimentTypeFindTest() throws Exception {
		af.accept(app -> {
			ExperimentType experimentType=ExperimentType.find.get().findAll().get(0);
			Assert.assertNotNull(experimentType);
			ExperimentType expType=ExperimentType.find.get().findByCode(experimentType.code);
			Assert.assertNotNull(expType);
			ExperimentType expTypeId=ExperimentType.find.get().findById(experimentType.id);
			Assert.assertNotNull(expTypeId);
			Assert.assertFalse(ExperimentType.find.get().isCodeExist(""));
			Assert.assertNotNull(ExperimentType.find.get().findAllForList());	
			Assert.assertNotNull(ExperimentType.find.get().findVoidProcessExperimentTypeCode(ProcessType.find.get().findAll().get(0).code));
			Assert.assertNotNull(ExperimentType.find.get().findPreviousExperimentTypeForAnExperimentTypeCode(experimentType.code));
			Assert.assertNotNull(ExperimentType.find.get().findByCategoryCode(""));
			Assert.assertNotNull(ExperimentType.find.get().findByCategoryCodeWithoutOneToVoid("transformation"));
		});
	}

	@Test
	public void projectTypeFindTest() throws Exception {
		af.accept(app -> {
			ProjectType type=ProjectType.find.get().findAll().get(0);
			Assert.assertNotNull(type);
			Assert.assertNotNull(type.code);
			ProjectType cType=ProjectType.find.get().findByCode(type.code);
			Assert.assertNotNull(cType);
			ProjectType cTypeId=ProjectType.find.get().findById(type.id);
			Assert.assertNotNull(cTypeId);
			Assert.assertFalse(ProjectType.find.get().isCodeExist(""));
			Assert.assertNotNull(ProjectType.find.get().findAllForList());
		});
	}

	@Test
	public void processTypeFindTest() throws Exception {
		af.accept(app -> {
			ProcessType type=ProcessType.find.get().findAll().get(0);
			Assert.assertNotNull(type);
			ProcessType cType=ProcessType.find.get().findByCode(type.code);
			Assert.assertNotNull(cType);
			ProcessType cTypeId=ProcessType.find.get().findById(type.id);
			Assert.assertNotNull(cTypeId);
			Assert.assertFalse(ProcessType.find.get().isCodeExist(""));
			Assert.assertNotNull(ProcessType.find.get().findAll());
		});
	}

	@Test
	public void sampleTypeFindTest() throws Exception {
		af.accept(app -> {
			SampleType type=SampleType.find.get().findAll().get(0);
			Assert.assertNotNull(type);
			SampleType cType=SampleType.find.get().findByCode(type.code);
			Assert.assertNotNull(cType);
			SampleType cTypeId=SampleType.find.get().findById(type.id);
			Assert.assertNotNull(cTypeId);
			Assert.assertFalse(SampleType.find.get().isCodeExist(""));
			Assert.assertNotNull(SampleType.find.get().findAllForList());
		});
	}

	@Test
	public void importTypeFindTest() throws Exception {
		af.accept(app -> {
			ImportType type=ImportType.find.get().findAll().get(0);
			Assert.assertNotNull(type);
			ImportType cType=ImportType.find.get().findByCode(type.code);
			Assert.assertNotNull(cType);
			ImportType cTypeId=ImportType.find.get().findById(type.id);
			Assert.assertNotNull(cTypeId);
			Assert.assertFalse(ImportType.find.get().isCodeExist(""));
			Assert.assertNotNull(ImportType.find.get().findAllForList());
		});
	}
	
	@Test
	public void instrumentUsedTypeFindTest() throws Exception {
		af.accept(app -> {
			InstrumentUsedType type=InstrumentUsedType.find.get().findAll().get(0);
			Assert.assertNotNull(type);
			InstrumentUsedType cType=InstrumentUsedType.find.get().findByCode(type.code);
			Assert.assertNotNull(cType);
			InstrumentUsedType cTypeId=InstrumentUsedType.find.get().findById(type.id);
			Assert.assertNotNull(cTypeId);
			Assert.assertFalse(InstrumentUsedType.find.get().isCodeExist(""));
			Assert.assertNotNull(InstrumentUsedType.find.get().findAll());
		});
	}

	@Test
	public void readSetTypeFindTest() throws Exception {
		af.accept(app -> {
			ExperimentType type=ExperimentType.find.get().findAll().get(0);
			Assert.assertNotNull(type);
			ExperimentType cType=ExperimentType.find.get().findByCode(type.code);
			Assert.assertNotNull(cType);
			ExperimentType cTypeId=ExperimentType.find.get().findById(type.id);
			Assert.assertNotNull(cTypeId);
			Assert.assertFalse(ExperimentType.find.get().isCodeExist(""));
			Assert.assertNotNull(ExperimentType.find.get().findAllForList());
		});
	}

	@Test
	public void runTypeFindTest() throws Exception {
		af.accept(app -> {
			ExperimentType type=ExperimentType.find.get().findAll().get(0);
			Assert.assertNotNull(type);
			ExperimentType cType=ExperimentType.find.get().findByCode(type.code);
			Assert.assertNotNull(cType);
			ExperimentType cTypeId=ExperimentType.find.get().findById(type.id);
			Assert.assertNotNull(cTypeId);
			Assert.assertFalse(ExperimentType.find.get().isCodeExist(""));
			Assert.assertNotNull(ExperimentType.find.get().findAllForList());
		});
	}
	
	@Test
	public void treatmentTypeFindTest() throws Exception {
		af.accept(app -> {
			TreatmentType type=TreatmentType.find.get().findAll().get(0);
			Assert.assertNotNull(type);
			TreatmentType cType=TreatmentType.find.get().findByCode(type.code);
			Assert.assertNotNull(cType);
			TreatmentType cTypeId=TreatmentType.find.get().findById(type.id);
			Assert.assertNotNull(cTypeId);
			Assert.assertFalse(TreatmentType.find.get().isCodeExist(""));
			Assert.assertNotNull(TreatmentType.find.get().findAllForList());
		});
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

//package models.dao.type;
//
//import models.laboratory.experiment.description.ExperimentType;
//import models.laboratory.instrument.description.InstrumentUsedType;
//import models.laboratory.processes.description.ProcessType;
//import models.laboratory.project.description.ProjectType;
//import models.laboratory.run.description.TreatmentType;
//import models.laboratory.sample.description.ImportType;
//import models.laboratory.sample.description.SampleType;
//import models.utils.dao.DAOException;
//import org.junit.Assert;
//import org.junit.Test;
//import utils.AbstractTests;
//
//public class AbstractTypeDAOTest extends AbstractTests {
//
//	protected static play.Logger.ALogger logger = play.Logger.of("AbstractTypeDAOTest");
//
//	@Test
//	public void experimentTypeFindTest() throws DAOException {
//		ExperimentType experimentType=ExperimentType.find.get().findAll().get(0);
//		Assert.assertNotNull(experimentType);
//		ExperimentType expType=ExperimentType.find.get().findByCode(experimentType.code);
//		Assert.assertNotNull(expType);
//		ExperimentType expTypeId=ExperimentType.find.get().findById(experimentType.id);
//		Assert.assertNotNull(expTypeId);
//		Assert.assertFalse(ExperimentType.find.get().isCodeExist(""));
//		Assert.assertNotNull(ExperimentType.find.get().findAllForList());	
//		Assert.assertNotNull(ExperimentType.find.get().findVoidProcessExperimentTypeCode(ProcessType.find.get().findAll().get(0).code));
//		Assert.assertNotNull(ExperimentType.find.get().findPreviousExperimentTypeForAnExperimentTypeCode(experimentType.code));
//		Assert.assertNotNull(ExperimentType.find.get().findByCategoryCode(""));
//		Assert.assertNotNull(ExperimentType.find.get().findByCategoryCodeWithoutOneToVoid("transformation"));
//	}
//
//	@Test
//	public void projectTypeFindTest() throws DAOException {
//		ProjectType type=ProjectType.find.get().findAll().get(0);
//		Assert.assertNotNull(type);
//		Assert.assertNotNull(type.code);
//		ProjectType cType=ProjectType.find.get().findByCode(type.code);
//		Assert.assertNotNull(cType);
//		ProjectType cTypeId=ProjectType.find.get().findById(type.id);
//		Assert.assertNotNull(cTypeId);
//		Assert.assertFalse(ProjectType.find.get().isCodeExist(""));
//		Assert.assertNotNull(ProjectType.find.get().findAllForList());
//	}
//
//	@Test
//	public void processTypeFindTest() throws DAOException {
//		ProcessType type=ProcessType.find.get().findAll().get(0);
//		Assert.assertNotNull(type);
//		ProcessType cType=ProcessType.find.get().findByCode(type.code);
//		Assert.assertNotNull(cType);
//		ProcessType cTypeId=ProcessType.find.get().findById(type.id);
//		Assert.assertNotNull(cTypeId);
//		Assert.assertFalse(ProcessType.find.get().isCodeExist(""));
//		Assert.assertNotNull(ProcessType.find.get().findAll());
//	}
//
//	@Test
//	public void sampleTypeFindTest() throws DAOException {
//		SampleType type=SampleType.find.get().findAll().get(0);
//		Assert.assertNotNull(type);
//		SampleType cType=SampleType.find.get().findByCode(type.code);
//		Assert.assertNotNull(cType);
//		SampleType cTypeId=SampleType.find.get().findById(type.id);
//		Assert.assertNotNull(cTypeId);
//		Assert.assertFalse(SampleType.find.get().isCodeExist(""));
//		Assert.assertNotNull(SampleType.find.get().findAllForList());
//	}
//
//	@Test
//	public void importTypeFindTest() throws DAOException {		
//		ImportType type=ImportType.find.get().findAll().get(0);
//		Assert.assertNotNull(type);
//		ImportType cType=ImportType.find.get().findByCode(type.code);
//		Assert.assertNotNull(cType);
//		ImportType cTypeId=ImportType.find.get().findById(type.id);
//		Assert.assertNotNull(cTypeId);
//		Assert.assertFalse(ImportType.find.get().isCodeExist(""));
//		Assert.assertNotNull(ImportType.find.get().findAllForList());
//	}
//	
//	@Test
//	public void instrumentUsedTypeFindTest() throws DAOException {		
//		InstrumentUsedType type=InstrumentUsedType.find.get().findAll().get(0);
//		Assert.assertNotNull(type);
//		InstrumentUsedType cType=InstrumentUsedType.find.get().findByCode(type.code);
//		Assert.assertNotNull(cType);
//		InstrumentUsedType cTypeId=InstrumentUsedType.find.get().findById(type.id);
//		Assert.assertNotNull(cTypeId);
//		Assert.assertFalse(InstrumentUsedType.find.get().isCodeExist(""));
//		Assert.assertNotNull(InstrumentUsedType.find.get().findAll());
//	}
//
//	@Test
//	public void readSetTypeFindTest() throws DAOException {		
//		ExperimentType type=ExperimentType.find.get().findAll().get(0);
//		Assert.assertNotNull(type);
//		ExperimentType cType=ExperimentType.find.get().findByCode(type.code);
//		Assert.assertNotNull(cType);
//		ExperimentType cTypeId=ExperimentType.find.get().findById(type.id);
//		Assert.assertNotNull(cTypeId);
//		Assert.assertFalse(ExperimentType.find.get().isCodeExist(""));
//		Assert.assertNotNull(ExperimentType.find.get().findAllForList());
//	}
//
//	@Test
//	public void runTypeFindTest() throws DAOException {		
//		ExperimentType type=ExperimentType.find.get().findAll().get(0);
//		Assert.assertNotNull(type);
//		ExperimentType cType=ExperimentType.find.get().findByCode(type.code);
//		Assert.assertNotNull(cType);
//		ExperimentType cTypeId=ExperimentType.find.get().findById(type.id);
//		Assert.assertNotNull(cTypeId);
//		Assert.assertFalse(ExperimentType.find.get().isCodeExist(""));
//		Assert.assertNotNull(ExperimentType.find.get().findAllForList());
//	}
//	
//	@Test
//	public void treatmentTypeFindTest() throws DAOException {		
//		TreatmentType type=TreatmentType.find.get().findAll().get(0);
//		Assert.assertNotNull(type);
//		TreatmentType cType=TreatmentType.find.get().findByCode(type.code);
//		Assert.assertNotNull(cType);
//		TreatmentType cTypeId=TreatmentType.find.get().findById(type.id);
//		Assert.assertNotNull(cTypeId);
//		Assert.assertFalse(TreatmentType.find.get().isCodeExist(""));
//		Assert.assertNotNull(TreatmentType.find.get().findAllForList());
//	}
//	
//	//@Test
//	/*public void reagentTypeFindTest() throws DAOException {		
//		ReagentCategory type=ReagentCategory.find.findAll().get(0);
//		Assert.assertNotNull(type);
//		ReagentCategory cType=ReagentCategory.find.findByCode(type.code);
//		Assert.assertNotNull(cType);
//		ReagentCategory cTypeId=ReagentCategory.find.findById(type.id);
//		Assert.assertNotNull(cTypeId);
//		Assert.assertFalse(ReagentCategory.find.isCodeExist(""));
//		Assert.assertNotNull(ReagentCategory.find.findAllForList());
//		//Assert.assertNotNull(ReagentType.find.findByProtocol(0));
//	}*/
//
//}
