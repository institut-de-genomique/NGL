package models.dao.mapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// import junit.framework.Assert;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.dao.EmptyResultDataAccessException;

import models.laboratory.common.description.CommonInfoType;
import models.laboratory.common.description.Institute;
import models.laboratory.common.description.MeasureUnit;
import models.laboratory.common.description.ObjectType;
import models.laboratory.common.description.PropertyDefinition;
import models.laboratory.common.description.State;
import models.laboratory.common.description.dao.InstituteDAO;
import models.laboratory.common.description.dao.PropertyDefinitionDAO;
import models.laboratory.container.description.ContainerSupportCategory;
import models.laboratory.container.description.dao.ContainerSupportCategoryDAO;
import models.laboratory.experiment.description.ExperimentType;
import models.laboratory.experiment.description.dao.ExperimentTypeDAO;
import models.laboratory.instrument.description.Instrument;
import models.laboratory.instrument.description.InstrumentQueryParams;
import models.laboratory.instrument.description.InstrumentUsedType;
import models.laboratory.instrument.description.dao.InstrumentUsedTypeDAO;
import models.laboratory.processes.description.ExperimentTypeNode;
import models.laboratory.processes.description.ProcessType;
import models.laboratory.project.description.ProjectCategory;
import models.laboratory.project.description.ProjectType;
import models.laboratory.run.description.ReadSetType;
import models.laboratory.run.description.RunType;
import models.laboratory.run.description.TreatmentType;
import models.laboratory.sample.description.ImportType;
import models.laboratory.sample.description.SampleType;
import models.utils.ListObject;
import models.utils.dao.DAOException;
//import play.Logger;
//import play.Logger.ALogger;
import play.api.modules.spring.Spring;
import utils.AbstractTests;

public class FindDAOTest extends AbstractTests {
	
	protected static play.Logger.ALogger logger = play.Logger.of(FindDAOTest.class);
	
	@Test
	public void CommonInfoTypeFindTest() throws DAOException {
		CommonInfoType type = CommonInfoType.find.findAll().get(0);
		Assert.assertNotNull(type);
		CommonInfoType cType = CommonInfoType.find.findByCode(type.code);
		Assert.assertNotNull(cType);
		CommonInfoType cTypeId = CommonInfoType.find.findById(type.id);
		Assert.assertNotNull(cTypeId);
		Assert.assertFalse(CommonInfoType.find.isCodeExist(""));
		CommonInfoType typeRun = CommonInfoType.find.findByObjectTypeCode(ObjectType.CODE.Run).get(0);
		Assert.assertNotNull(typeRun);
	}
	
	@Test 
	public void ProcessTypeFindTest() throws DAOException {
		ProcessType pt = ProcessType.find.findAll().get(0);
		Assert.assertNotNull(pt);
		ProcessType pt2 = ProcessType.find.findByCode(pt.code); 
		Assert.assertNotNull(pt2);
		ProcessType pt3 = ProcessType.find.findById(pt.id);
		Assert.assertNotNull(pt3);
		ProcessType pt4 = ProcessType.find.findByProcessCategoryCodes(pt.category.code).get(0);
		Assert.assertNotNull(pt4);
		Assert.assertFalse(ProcessType.find.isCodeExist(""));
		// List<ProcessType> pt5 = 
				ProcessType.find.findByExperimentTypeCode("");
	}

	@Test 
	public void ProjectTypeFindTest() throws DAOException {
		ProjectType pt = ProjectType.find.findAll().get(0);
		Assert.assertNotNull(pt);
		ProjectType pt2 = ProjectType.find.findByCode(pt.code); 
		Assert.assertNotNull(pt2);
		ProjectType pt3 = ProjectType.find.findById(pt.id);
		Assert.assertNotNull(pt3);
		ListObject lo = ProjectType.find.findAllForList().get(0);
		Assert.assertNotNull(lo);
		Assert.assertFalse(ProjectType.find.isCodeExist("")); 
	}
	
	@Test
	public void ProjectCategoryFindTest() throws DAOException {
		ProjectCategory pc = ProjectCategory.find.findAll().get(0);
		Assert.assertNotNull(pc);
		ProjectCategory pc2 = ProjectCategory.find.findByCode(pc.code); 
		Assert.assertNotNull(pc2);
		ProjectCategory pc3 = ProjectCategory.find.findById(pc.id);
		Assert.assertNotNull(pc3);
		Assert.assertFalse(ProjectCategory.find.isCodeExist("")); 		
	}
	
	/*@Test
	public void ReagentTypeFindTest() throws DAOException {
		if (! ReagentCategory.find.findAll().isEmpty()) {
			ReagentCategory rt = ReagentCategory.find.findAll().get(0);
			Assert.assertNotNull(rt);
			ReagentCategory rt2 = ReagentCategory.find.findByCode(rt.code); 
			Assert.assertNotNull(rt2);
			ReagentCategory rt3 = ReagentCategory.find.findById(rt.id);
			Assert.assertNotNull(rt3);
			ListObject lo = ReagentCategory.find.findAllForList().get(0);
			Assert.assertNotNull(lo);
		}
		else {
			ReagentCategory rt2 = ReagentCategory.find.findByCode(""); 
			Assert.assertNull(rt2);
			ReagentCategory rt3 = ReagentCategory.find.findById(Long.valueOf(0));
			Assert.assertNull(rt3);			
		}
		Assert.assertFalse(ReagentCategory.find.isCodeExist(""));
	}*/

	@Test
	public void RunTypeFindTest() throws DAOException {
		RunType rt = RunType.find.findAll().get(0);
		Assert.assertNotNull(rt);
		RunType rt2 = RunType.find.findByCode(rt.code); 
		Assert.assertNotNull(rt2);
		RunType rt3 = RunType.find.findById(rt.id);
		Assert.assertNotNull(rt3);
		ListObject lo = RunType.find.findAllForList().get(0);
		Assert.assertNotNull(lo);
		Assert.assertFalse(RunType.find.isCodeExist(""));
	}
	
	@Test
	public void ReadSetTypeFindTest() throws DAOException {
		ReadSetType rt = ReadSetType.find.findAll().get(0);
		Assert.assertNotNull(rt);
		ReadSetType rt2 = ReadSetType.find.findByCode(rt.code); 
		Assert.assertNotNull(rt2);
		ReadSetType rt3 = ReadSetType.find.findById(rt.id);
		Assert.assertNotNull(rt3);
		ListObject lo = ReadSetType.find.findAllForList().get(0);
		Assert.assertNotNull(lo);
		Assert.assertFalse(ReadSetType.find.isCodeExist(""));
	}

	@Test
	public void TreatmentTypeFindTest() throws DAOException {
		TreatmentType rt = TreatmentType.find.findAll().get(0);
		Assert.assertNotNull(rt);
		TreatmentType rt2 = TreatmentType.find.findByCode(rt.code); 
		Assert.assertNotNull(rt2);
		TreatmentType rt3 = TreatmentType.find.findById(rt.id);
		Assert.assertNotNull(rt3);
		ListObject lo = TreatmentType.find.findAllForList().get(0);
		Assert.assertNotNull(lo);
		Assert.assertFalse(TreatmentType.find.isCodeExist(""));
	}
	
	@Test
	public void ImportTypeFindTest() throws DAOException {
		ImportType rt = ImportType.find.findAll().get(0);
		Assert.assertNotNull(rt);
		ImportType rt2 = ImportType.find.findByCode(rt.code); 
		Assert.assertNotNull(rt2);
		ImportType rt3 = ImportType.find.findById(rt.id);
		Assert.assertNotNull(rt3);
		ListObject lo = ImportType.find.findAllForList().get(0);
		Assert.assertNotNull(lo);
		Assert.assertFalse(ImportType.find.isCodeExist(""));		
	}
		
	@Test
	public void SampleTypeFindTest() throws DAOException {
		SampleType rt = SampleType.find.findAll().get(0);
		Assert.assertNotNull(rt);
		SampleType rt2 = SampleType.find.findByCode(rt.code); 
		Assert.assertNotNull(rt2);
		SampleType rt3 = SampleType.find.findById(rt.id);
		Assert.assertNotNull(rt3);
		ListObject lo = SampleType.find.findAllForList().get(0);
		Assert.assertNotNull(lo);
		Assert.assertFalse(SampleType.find.isCodeExist(""));
	}
	
	@Test
	public void ContainerSupportCategoryFindTest() throws DAOException {
		ContainerSupportCategory type = ContainerSupportCategory.find.findAll().get(0);
		Assert.assertNotNull(type);
		ContainerSupportCategory cType = ContainerSupportCategory.find.findByCode(type.code);
		Assert.assertNotNull(cType);
		ContainerSupportCategory cTypeId = ContainerSupportCategory.find.findById(type.id);
		Assert.assertNotNull(cTypeId);
		Assert.assertFalse(ContainerSupportCategory.find.isCodeExist(""));
		Assert.assertNotNull(ContainerSupportCategory.find.findByContainerCategoryCode(""));
		Assert.assertNotNull(ContainerSupportCategory.find.findInputByExperimentTypeCode(""));
		Assert.assertFalse(ContainerSupportCategory.find.isCodeExist("")); 
		//internal fcts
		ContainerSupportCategoryDAO cscDAO = Spring.getBeanOfType(ContainerSupportCategoryDAO.class); 
		InstrumentUsedType iut =  InstrumentUsedType.find.findByCode("hand");
		Assert.assertNotNull(cscDAO.findInByInstrumentUsedType(iut.id));		
	}

	@Test
	public void ExperimentTypeNodeFindTest() throws DAOException {
		ExperimentTypeNode type = ExperimentTypeNode.find.findAll().get(0);
		Assert.assertNotNull(type);
		ExperimentTypeNode cType = ExperimentTypeNode.find
				.findByCode(type.code);
		Assert.assertNotNull(cType);
		ExperimentTypeNode cTypeId = ExperimentTypeNode.find.findById(type.id);
		Assert.assertNotNull(cTypeId);
		Assert.assertFalse(ExperimentTypeNode.find.isCodeExist(""));
	}
	
	@Test
	public void ExperimentFindTest() throws DAOException {
		ExperimentType et = ExperimentType.find.findAll().get(0);
		Assert.assertNotNull(et);
		ExperimentType et2 = ExperimentType.find.findByCode(et.code);
		Assert.assertNotNull(et2);
		ExperimentType et3 = ExperimentType.find.findById(et.id);
		Assert.assertNotNull(et3);
		ExperimentType et4 = ExperimentType.find.findByCategoryCode(et.category.code).get(0);
		Assert.assertNotNull(et4);
		ProcessType pt = ProcessType.find.findAll().get(0);
		ExperimentType et5 = ExperimentType.find.findByCategoryCodeAndProcessTypeCode(pt.firstExperimentType.category.code, pt.code).get(0);
		Assert.assertNotNull(et5);
		ExperimentType et6 = ExperimentType.find.findPreviousExperimentTypeForAnExperimentTypeCode("fragmentation").get(0);
		Assert.assertNotNull(et6);
		ExperimentType et7 = ExperimentType.find.findByCategoryCodeWithoutOneToVoid("transformation").get(0);
		Assert.assertNotNull(et7);
		List<String> lstr = ExperimentType.find.findVoidProcessExperimentTypeCode("");
		Assert.assertEquals(0, lstr.size()); 
		//internal fct
		ExperimentTypeDAO etDAO = Spring.getBeanOfType(ExperimentTypeDAO.class);
		etDAO.findByProcessTypeCode(pt.code, false);
	}

	@Test
	public void InstituteFindTest() throws DAOException {
		Institute type = Institute.find.findAll().get(0);
		Assert.assertNotNull(type);
		Institute cType = Institute.find.findByCode(type.code);
		Assert.assertNotNull(cType);
		Institute cTypeId = Institute.find.findById(type.id);
		Assert.assertNotNull(cTypeId);
		Assert.assertFalse(Institute.find.isCodeExist(""));
		//internal fcts
		InstituteDAO institDAO = Spring.getBeanOfType(InstituteDAO.class);
		CommonInfoType citInstrument = CommonInfoType.find.findByObjectTypeCode(ObjectType.CODE.Instrument).get(0);
		Assert.assertNotNull(institDAO.findByCommonInfoType(citInstrument.id)); 
	}

	@Test
	public void MeasureUnitFindTest() throws DAOException {
		MeasureUnit type = MeasureUnit.find.findAll().get(0);
		Assert.assertNotNull(type);
		MeasureUnit cType = MeasureUnit.find.findByCode(type.code);
		Assert.assertNotNull(cType);
		MeasureUnit cTypeId = MeasureUnit.find.findById(type.id);
		Assert.assertNotNull(cTypeId);	
		Assert.assertNotNull(MeasureUnit.find.findByValue(type.code));
		Assert.assertFalse(MeasureUnit.find.isCodeExist(""));
	}

	@Test
	public void ObjectTypeFindTest() throws DAOException {
		ObjectType type = ObjectType.find.findAll().get(0);
		Assert.assertNotNull(type);
		ObjectType cType = ObjectType.find.findByCode(type.code);
		Assert.assertNotNull(cType);
		ObjectType cTypeId = ObjectType.find.findById(type.id);
		Assert.assertNotNull(cTypeId);
		Assert.assertFalse(ObjectType.find.isCodeExist(""));
	}

	@Test // (expected=UnsupportedOperationException.class)
	public void PropertyDefinitionFindTest() throws DAOException {
		PropertyDefinition type = PropertyDefinition.find.findAll().get(0);
		// PropertyDefinition cType = 
				PropertyDefinition.find.findByCode(type.code);
		// PropertyDefinition cTypeId = 
				PropertyDefinition.find.findById(type.id);
	}
	
	@Test
	public void PropertyDefinitionFindTest2() throws DAOException {
		Assert.assertNotNull(PropertyDefinition.find.isCodeExist(""));		
		//internal fcts
		CommonInfoType citTreatment = CommonInfoType.find.findByObjectTypeCode(ObjectType.CODE.Treatment).get(0);
		PropertyDefinitionDAO pDAO = Spring.getBeanOfType(PropertyDefinitionDAO.class);
		Assert.assertNotNull(pDAO.findByCommonInfoType(citTreatment.id));
	}
	
	@Test
	public void StateFindTest() throws DAOException {
		State type = State.find.findAll().get(0);
		Assert.assertNotNull(type);
		State cType = State.find.findByCode(type.code);
		Assert.assertNotNull(cType);
		State cTypeId = State.find.findById(type.id);
		Assert.assertNotNull(cTypeId);
		Assert.assertFalse(State.find.isCodeExist(""));
		Assert.assertNotNull(State.find.findAllForContainerList());
		Assert.assertNotNull(State.find.findByCategoryCode(""));
		Assert.assertNotNull(State.find.findByObjectTypeCode(ObjectType.CODE.Sample));
		Assert.assertNotNull(State.find.findByTypeCode(""));
		Assert.assertFalse(State.find.isCodeExistForTypeCode("",""));
	}
	
	@Test(expected=EmptyResultDataAccessException.class)
	public void InstrumentUsedTypeFindTest() throws DAOException {
		InstrumentUsedType type = InstrumentUsedType.find.findAll().get(0);
		Assert.assertNotNull(type);
		InstrumentUsedType cType = InstrumentUsedType.find.findByCode(type.code);
		Assert.assertNotNull(cType);
		InstrumentUsedType cTypeId = InstrumentUsedType.find.findById(type.id);
		Assert.assertNotNull(cTypeId);
		Assert.assertFalse(InstrumentUsedType.find.isCodeExist(""));
		Assert.assertNotNull(InstrumentUsedType.find.findByExperimentTypeCode(""));		
		InstrumentUsedTypeDAO iDAO = Spring.getBeanOfType(InstrumentUsedTypeDAO.class);
		List<InstrumentUsedType> liut = new ArrayList<>();
		liut = iDAO.findByExperimentId(0);
		Assert.assertTrue(liut.size() >= 0);
		Map<String, Object> m = new HashMap<>();
		m = iDAO.findTypeCodeAndCatCode(0);
		Assert.assertTrue(m.size() >= 0);
	}
	
	@Test
	public void InstrumentFindTest() throws DAOException {
		InstrumentQueryParams instrumentQuery = new InstrumentQueryParams();
		instrumentQuery.typeCode = "ARGUS";
		List<Instrument> intruments = Instrument.find.findByQueryParams(instrumentQuery);
		Assert.assertNotNull(intruments);
		
		instrumentQuery = new InstrumentQueryParams();
		instrumentQuery.typeCodes = new ArrayList<>();
		instrumentQuery.typeCodes.add("ARGUS");
		intruments = Instrument.find.findByQueryParams(instrumentQuery);
		Assert.assertNotNull(intruments);
		
		instrumentQuery = new InstrumentQueryParams();
		instrumentQuery.categoryCodes = new ArrayList<>();
		instrumentQuery.categoryCodes.add("covaris");
		intruments = Instrument.find.findByQueryParams(instrumentQuery);
		Assert.assertNotNull(intruments);
		
		instrumentQuery = new InstrumentQueryParams();
		instrumentQuery.typeCode = "ARGUS";
		instrumentQuery.categoryCode = "opt-map-opgen";
		intruments = Instrument.find.findByQueryParams(instrumentQuery);
		Assert.assertNotNull(intruments);
		
		instrumentQuery = new InstrumentQueryParams();
		instrumentQuery.typeCodes = new ArrayList<>();
		instrumentQuery.typeCodes.add("ARGUS");
		instrumentQuery.categoryCodes = new ArrayList<>();
		instrumentQuery.categoryCodes.add("opt-map-opgen");
		intruments = Instrument.find.findByQueryParams(instrumentQuery);
		Assert.assertNotNull(intruments);		
	}

}
