package models.dao.other;

import java.util.Arrays;
import java.util.List;

import models.laboratory.common.description.Level;
import models.laboratory.common.description.MeasureCategory;
import models.laboratory.common.description.PropertyDefinition;
import models.laboratory.common.description.StateCategory;
import models.laboratory.common.description.Value;
import models.laboratory.common.description.dao.LevelDAO;
import models.laboratory.common.description.dao.MeasureCategoryDAO;
import models.laboratory.common.description.dao.StateCategoryDAO;
import models.laboratory.common.description.dao.ValueDAO;
import models.laboratory.container.description.ContainerCategory;
import models.laboratory.container.description.dao.ContainerCategoryDAO;
import models.laboratory.experiment.description.ExperimentCategory;
import models.laboratory.experiment.description.ProtocolCategory;
import models.laboratory.experiment.description.dao.ExperimentCategoryDAO;
import models.laboratory.experiment.description.dao.ProtocolCategoryDAO;
import models.laboratory.instrument.description.Instrument;
import models.laboratory.instrument.description.InstrumentCategory;
import models.laboratory.instrument.description.InstrumentQueryParams;
import models.laboratory.instrument.description.dao.InstrumentCategoryDAO;
import models.laboratory.processes.description.ProcessCategory;
import models.laboratory.processes.description.dao.ProcessCategoryDAO;
import models.laboratory.project.description.ProjectCategory;
import models.laboratory.project.description.dao.ProjectCategoryDAO;
import models.laboratory.run.description.RunCategory;
import models.laboratory.run.description.TreatmentCategory;
import models.laboratory.run.description.TreatmentContext;
import models.laboratory.run.description.TreatmentType;
import models.laboratory.run.description.TreatmentTypeContext;
import models.laboratory.run.description.dao.RunCategoryDAO;
import models.laboratory.run.description.dao.TreatmentCategoryDAO;
import models.laboratory.run.description.dao.TreatmentContextDAO;
import models.laboratory.sample.description.ImportCategory;
import models.laboratory.sample.description.SampleCategory;
import models.laboratory.sample.description.dao.ImportCategoryDAO;
import models.laboratory.sample.description.dao.SampleCategoryDAO;
import models.utils.dao.DAOException;

import org.junit.Assert;
import org.junit.Test;

import play.api.modules.spring.Spring;
import utils.AbstractTests;

public class FindDAOTest extends AbstractTests {
	

	@Test
	public void RunCategoryFindTest() throws DAOException {
		RunCategory rt = RunCategory.find.findAll().get(0);
		Assert.assertNotNull(rt);
		RunCategory rt2 = RunCategory.find.findByCode(rt.code); 
		Assert.assertNotNull(rt2);
		RunCategory rt3 = RunCategory.find.findById(rt.id);
		Assert.assertNotNull(rt3);
		Assert.assertFalse(RunCategory.find.isCodeExist(""));
	}

	@Test
	public void TreatmentCategoryFindTest() throws DAOException {
		TreatmentCategory rt = TreatmentCategory.find.findAll().get(0);
		Assert.assertNotNull(rt);
		TreatmentCategory rt2 = TreatmentCategory.find.findByCode(rt.code); 
		Assert.assertNotNull(rt2);
		TreatmentCategory rt3 = TreatmentCategory.find.findById(rt.id);
		Assert.assertNotNull(rt3);
		Assert.assertFalse(TreatmentCategory.find.isCodeExist(""));
	}

	@Test
	public void TreatmentContextFindTest() throws DAOException {
		TreatmentContext rt = TreatmentContext.find.findAll().get(0);
		Assert.assertNotNull(rt);
		TreatmentContext rt2 = TreatmentContext.find.findByCode(rt.code); 
		Assert.assertNotNull(rt2);
		TreatmentContext rt3 = TreatmentContext.find.findById(rt.id);
		Assert.assertNotNull(rt3);
		Assert.assertFalse(TreatmentContext.find.isCodeExist(""));
	}

	
	@Test
	public void TreatmentTypeContextFindTest() throws DAOException {
		Assert.assertFalse(TreatmentTypeContext.find.isCodeExist(""));
		TreatmentType tt = TreatmentType.find.findAll().get(0);
		TreatmentTypeContext rt4 = TreatmentTypeContext.find.findByTreatmentTypeId(tt.id).get(0);
		Assert.assertNotNull(rt4);
		TreatmentTypeContext rt5 = TreatmentTypeContext.find.findByTreatmentTypeId(rt4.code, tt.id);
		Assert.assertNotNull(rt5);
	}
	
	
	@Test
	public void ImportCategoryFindTest() throws DAOException {
		ImportCategory rt = ImportCategory.find.findAll().get(0);
		Assert.assertNotNull(rt);
		ImportCategory rt2 = ImportCategory.find.findByCode(rt.code); 
		Assert.assertNotNull(rt2);
		ImportCategory rt3 = ImportCategory.find.findById(rt.id);
		Assert.assertNotNull(rt3);
		Assert.assertFalse(ImportCategory.find.isCodeExist(""));
	}

	@Test
	public void SampleCategoryFindTest() throws DAOException {
		SampleCategory rt = SampleCategory.find.findAll().get(0);
		Assert.assertNotNull(rt);
		SampleCategory rt2 = SampleCategory.find.findByCode(rt.code); 
		Assert.assertNotNull(rt2);
		SampleCategory rt3 = SampleCategory.find.findById(rt.id);
		Assert.assertNotNull(rt3);
		Assert.assertFalse(SampleCategory.find.isCodeExist(""));
	}

	@Test
	public void containerCategoryFindTest() throws DAOException {
		ContainerCategory type = ContainerCategory.find.findAll().get(0);
		Assert.assertNotNull(type);
		ContainerCategory cType = ContainerCategory.find.findByCode(type.code);
		Assert.assertNotNull(cType);
		ContainerCategory cTypeId = ContainerCategory.find.findById(type.id);
		Assert.assertNotNull(cTypeId);
		Assert.assertFalse(ContainerCategory.find.isCodeExist(""));
		
		ContainerCategoryDAO typeDAO = Spring
				.getBeanOfType(ContainerCategoryDAO.class);
		Assert.assertNotNull(typeDAO.findAllForList());
	}

	@Test
	public void experimentCategoryFindTest() throws DAOException {
		ExperimentCategory type = ExperimentCategory.find.findAll().get(0);
		Assert.assertNotNull(type);
		ExperimentCategory cType = ExperimentCategory.find.findByCode(type.code);
		Assert.assertNotNull(cType);
		ExperimentCategory cTypeId = ExperimentCategory.find.findById(type.id);
		Assert.assertNotNull(cTypeId);
		Assert.assertFalse(ExperimentCategory.find.isCodeExist(""));
		List<ExperimentCategory> lec = ExperimentCategory.find.findByProcessTypeCode("");
		Assert.assertEquals(0, lec.size());
		
		ExperimentCategoryDAO typeDAO = Spring.getBeanOfType(ExperimentCategoryDAO.class);
		Assert.assertNotNull(typeDAO.findAllForList());
	}

	@Test
	public void importCategoryFindTest() throws DAOException {
		ImportCategory type = ImportCategory.find.findAll().get(0);
		Assert.assertNotNull(type);
		ImportCategory cType = ImportCategory.find.findByCode(type.code);
		Assert.assertNotNull(cType);
		ImportCategory cTypeId = ImportCategory.find.findById(type.id);
		Assert.assertNotNull(cTypeId);
		Assert.assertFalse(ImportCategory.find.isCodeExist(""));
		ImportCategoryDAO typeDAO = Spring
				.getBeanOfType(ImportCategoryDAO.class);
		Assert.assertNotNull(typeDAO.findAllForList());
	}

	@Test
	public void instrumentCategoryFindTest() throws DAOException {
		InstrumentCategory type = InstrumentCategory.find.findAll().get(0);
		Assert.assertNotNull(type);
		InstrumentCategory cType = InstrumentCategory.find
				.findByCode(type.code);
		Assert.assertNotNull(cType);
		InstrumentCategory cTypeId = InstrumentCategory.find.findById(type.id);
		Assert.assertNotNull(cTypeId);
		Assert.assertFalse(InstrumentCategory.find.isCodeExist(""));
		InstrumentCategoryDAO typeDAO = Spring
				.getBeanOfType(InstrumentCategoryDAO.class);
		Assert.assertNotNull(typeDAO.findAllForList());
	}

	@Test
	public void instrumentFindTest() throws DAOException {
		Instrument type = Instrument.find.findAll().get(0);
		Assert.assertNotNull(type);
		Instrument cType = Instrument.find.findByCode("MELISSE"); // not MISEQ1 because in double
		Assert.assertNotNull(cType);
		Instrument cTypeId = Instrument.find.findById(type.id);
		Assert.assertNotNull(cTypeId);
		Assert.assertFalse(Instrument.find.isCodeExist(""));	
		InstrumentQueryParams instrumentsQueryParams = new InstrumentQueryParams();
		Assert.assertNotNull(Instrument.find.findByQueryParams(instrumentsQueryParams));
		instrumentsQueryParams.active = Boolean.TRUE;
		Assert.assertNotNull(Instrument.find.findByQueryParams(instrumentsQueryParams));
		instrumentsQueryParams.categoryCode = "seq-illumina";
		Assert.assertNotNull(Instrument.find.findByQueryParams(instrumentsQueryParams));
		instrumentsQueryParams.categoryCode = null;
		instrumentsQueryParams.categoryCodes = Arrays.asList("seq-illumina");
		Assert.assertNotNull(Instrument.find.findByQueryParams(instrumentsQueryParams));
		instrumentsQueryParams.typeCode = "HISEQ2000";
		Assert.assertNotNull(Instrument.find.findByQueryParams(instrumentsQueryParams));
		instrumentsQueryParams.typeCode = null;		
		instrumentsQueryParams.typeCodes = Arrays.asList("HISEQ2000");
		Assert.assertNotNull(Instrument.find.findByQueryParams(instrumentsQueryParams));
		instrumentsQueryParams.typeCodes = Arrays.asList("HISEQ9999");
		Assert.assertEquals(Instrument.find.findByQueryParams(instrumentsQueryParams).size(), 0);
		
	}

	@Test
	public void levelFindTest() throws DAOException {
		Level type = Level.find.findAll().get(0);
		Assert.assertNotNull(type);
		Level cType = Level.find.findByCode(type.code);
		Assert.assertNotNull(cType);
		Level cTypeId = Level.find.findById(type.id);
		Assert.assertNotNull(cTypeId);
		Assert.assertFalse(Level.find.isCodeExist(""));
		LevelDAO levelDAO = Spring.getBeanOfType(LevelDAO.class);
		Assert.assertNotNull(levelDAO.findAllForList());
		
		//PropertyDefinition pdef =  PropertyDefinition.find.findAll().get(0);
		//Assert.assertNotNull(levelDAO.findByPropertyDefinitionID(pdef.id));
	}

	@Test
	public void measureCategoryFindTest() throws DAOException {
		MeasureCategory type = MeasureCategory.find.findAll().get(0);
		Assert.assertNotNull(type);
		MeasureCategory cType = MeasureCategory.find.findByCode(type.code);
		Assert.assertNotNull(cType);
		MeasureCategory cTypeId = MeasureCategory.find.findById(type.id);
		Assert.assertNotNull(cTypeId);
		Assert.assertFalse(MeasureCategory.find.isCodeExist(""));
		MeasureCategoryDAO typeDAO = Spring
				.getBeanOfType(MeasureCategoryDAO.class);
		Assert.assertNotNull(typeDAO.findAllForList());
	}

	@Test
	public void processCategoryFindTest() throws DAOException {
		ProcessCategory type = ProcessCategory.find.findAll().get(0);
		Assert.assertNotNull(type);
		ProcessCategory cType = ProcessCategory.find.findByCode(type.code);
		Assert.assertNotNull(cType);
		ProcessCategory cTypeId = ProcessCategory.find.findById(type.id);
		Assert.assertNotNull(cTypeId);
		Assert.assertFalse(ProcessCategory.find.isCodeExist(""));
		ProcessCategoryDAO typeDAO = Spring
				.getBeanOfType(ProcessCategoryDAO.class);
		Assert.assertNotNull(typeDAO.findAllForList());
	}

	@Test
	public void projectCategoryFindTest() throws DAOException {
		ProjectCategory type = ProjectCategory.find.findAll().get(0);
		Assert.assertNotNull(type);
		ProjectCategory cType = ProjectCategory.find.findByCode(type.code);
		Assert.assertNotNull(cType);
		ProjectCategory cTypeId = ProjectCategory.find.findById(type.id);
		Assert.assertNotNull(cTypeId);
		Assert.assertFalse(ProjectCategory.find.isCodeExist(""));
		ProjectCategoryDAO typeDAO = Spring
				.getBeanOfType(ProjectCategoryDAO.class);
		Assert.assertNotNull(typeDAO.findAllForList());
	}

	@Test
	public void protocolCategoryFindTest() throws DAOException {
		ProtocolCategory type = ProtocolCategory.find.findAll().get(0);
		Assert.assertNotNull(type);
		ProtocolCategory cType = ProtocolCategory.find.findByCode(type.code);
		Assert.assertNotNull(cType);
		ProtocolCategory cTypeId = ProtocolCategory.find.findById(type.id);
		Assert.assertNotNull(cTypeId);
		Assert.assertFalse(ProtocolCategory.find.isCodeExist(""));
		ProtocolCategoryDAO typeDAO = Spring
				.getBeanOfType(ProtocolCategoryDAO.class);
		Assert.assertNotNull(typeDAO.findAllForList());
	}


	@Test
	public void runCategoryFindTest() throws DAOException {
		RunCategory type = RunCategory.find.findAll().get(0);
		Assert.assertNotNull(type);
		RunCategory cType = RunCategory.find.findByCode(type.code);
		Assert.assertNotNull(cType);
		RunCategory cTypeId = RunCategory.find.findById(type.id);
		Assert.assertNotNull(cTypeId);
		Assert.assertFalse(RunCategory.find.isCodeExist(""));
		RunCategoryDAO typeDAO = Spring.getBeanOfType(RunCategoryDAO.class);
		Assert.assertNotNull(typeDAO.findAllForList());
	}

	@Test
	public void sampleCategoryFindTest() throws DAOException {
		SampleCategory type = SampleCategory.find.findAll().get(0);
		Assert.assertNotNull(type);
		SampleCategory cType = SampleCategory.find.findByCode(type.code);
		Assert.assertNotNull(cType);
		SampleCategory cTypeId = SampleCategory.find.findById(type.id);
		Assert.assertNotNull(cTypeId);
		Assert.assertFalse(SampleCategory.find.isCodeExist(""));
		SampleCategoryDAO typeDAO = Spring
				.getBeanOfType(SampleCategoryDAO.class);
		Assert.assertNotNull(typeDAO.findAllForList());
	}

	@Test
	public void stateCategoryFindTest() throws DAOException {
		StateCategory type = StateCategory.find.findAll().get(0);
		Assert.assertNotNull(type);
		StateCategory cType = StateCategory.find.findByCode(type.code);
		Assert.assertNotNull(cType);
		StateCategory cTypeId = StateCategory.find.findById(type.id);
		Assert.assertNotNull(cTypeId);
		Assert.assertFalse(StateCategory.find.isCodeExist(""));
		StateCategoryDAO typeDAO = Spring.getBeanOfType(StateCategoryDAO.class);
		Assert.assertNotNull(typeDAO.findAllForList());
	}

	@Test
	public void treatmentCategoryFindTest() throws DAOException {
		TreatmentCategory type = TreatmentCategory.find.findAll().get(0);
		Assert.assertNotNull(type);
		TreatmentCategory cType = TreatmentCategory.find.findByCode(type.code);
		Assert.assertNotNull(cType);
		TreatmentCategory cTypeId = TreatmentCategory.find.findById(type.id);
		Assert.assertNotNull(cTypeId);
		Assert.assertFalse(TreatmentCategory.find.isCodeExist(""));
		TreatmentCategoryDAO typeDAO = Spring
				.getBeanOfType(TreatmentCategoryDAO.class);
		Assert.assertNotNull(typeDAO.findAllForList());
	}

	@Test
	public void treatmentContextFindTest() throws DAOException {
		TreatmentContext type = TreatmentContext.find.findAll().get(0);
		Assert.assertNotNull(type);
		TreatmentContext cType = TreatmentContext.find.findByCode(type.code);
		Assert.assertNotNull(cType);
		TreatmentContext cTypeId = TreatmentContext.find.findById(type.id);
		Assert.assertNotNull(cTypeId);
		Assert.assertFalse(TreatmentContext.find.isCodeExist(""));
		TreatmentContextDAO typeDAO = Spring
				.getBeanOfType(TreatmentContextDAO.class);
		Assert.assertNotNull(typeDAO.findAllForList());
	}

	@Test(expected = RuntimeException.class)
	public void treatmentTypeContextFindTest() throws DAOException {
		// TreatmentTypeContext type = 
				TreatmentTypeContext.find.findAll().get(0);
	}

	@Test(expected=UnsupportedOperationException.class)
	public void valueFindTest() throws DAOException {
		Value type = Value.find.findAll().get(0);
		Assert.assertNotNull(type);
		Value cType = Value.find.findByCode(type.code);
		Assert.assertNotNull(cType);
		Value cTypeId = Value.find.findById(type.id);
		Assert.assertNotNull(cTypeId);
		Assert.assertFalse(Value.find.isCodeExist(""));

		//internal fcts
		ValueDAO typeDAO = Spring.getBeanOfType(ValueDAO.class);
		Assert.assertNotNull(typeDAO.findAllForList());
		PropertyDefinition pdef = PropertyDefinition.find.findByCode("runType");
		typeDAO.findByPropertyDefinition(pdef.id);
	}

}
