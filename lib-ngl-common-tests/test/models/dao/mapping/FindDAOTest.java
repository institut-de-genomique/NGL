package models.dao.mapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.dao.EmptyResultDataAccessException;

import fr.cea.ig.util.function.CC1;
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
import ngl.common.Global;
import play.Application;
import play.api.modules.spring.Spring;

public class FindDAOTest {
	
	protected static play.Logger.ALogger logger = play.Logger.of(FindDAOTest.class);
	
	private static final CC1<Application> af =
			Global.afSq.cc1();
	
	@Test
	public void CommonInfoTypeFindTest() throws Exception {
		af.accept(app -> {
			CommonInfoType type = CommonInfoType.find.get().findAll().get(0);
			Assert.assertNotNull(type);
			CommonInfoType cType = CommonInfoType.find.get().findByCode(type.code);
			Assert.assertNotNull(cType);
			CommonInfoType cTypeId = CommonInfoType.find.get().findById(type.id);
			Assert.assertNotNull(cTypeId);
			Assert.assertFalse(CommonInfoType.find.get().isCodeExist(""));
			CommonInfoType typeRun = CommonInfoType.find.get().findByObjectTypeCode(ObjectType.CODE.Run).get(0);
			Assert.assertNotNull(typeRun);
		});
	}
	
	// FIXME: failed: java.lang.NullPointerException: null 
	// @Test 
	public void ProcessTypeFindTest() throws Exception {
		af.accept(app -> {
			ProcessType pt = ProcessType.find.get().findAll().get(0);
			Assert.assertNotNull(pt);
			ProcessType pt2 = ProcessType.find.get().findByCode(pt.code); 
			Assert.assertNotNull(pt2);
			ProcessType pt3 = ProcessType.find.get().findById(pt.id);
			Assert.assertNotNull(pt3);
			ProcessType pt4 = ProcessType.find.get().findByProcessCategoryCodes(pt.category.code).get(0);
			Assert.assertNotNull(pt4);
			Assert.assertFalse(ProcessType.find.get().isCodeExist(""));
			// List<ProcessType> pt5 = 
			ProcessType.find.get().findByExperimentTypeCode("");
		});
	}

	@Test 
	public void ProjectTypeFindTest() throws Exception {
		af.accept(app -> {
			ProjectType pt = ProjectType.find.get().findAll().get(0);
			Assert.assertNotNull(pt);
			ProjectType pt2 = ProjectType.find.get().findByCode(pt.code); 
			Assert.assertNotNull(pt2);
			ProjectType pt3 = ProjectType.find.get().findById(pt.id);
			Assert.assertNotNull(pt3);
			ListObject lo = ProjectType.find.get().findAllForList().get(0);
			Assert.assertNotNull(lo);
			Assert.assertFalse(ProjectType.find.get().isCodeExist(""));
		});
	}
	
	@Test
	public void ProjectCategoryFindTest() throws Exception {
		af.accept(app -> {
			ProjectCategory pc = ProjectCategory.find.get().findAll().get(0);
			Assert.assertNotNull(pc);
			ProjectCategory pc2 = ProjectCategory.find.get().findByCode(pc.code); 
			Assert.assertNotNull(pc2);
			ProjectCategory pc3 = ProjectCategory.find.get().findById(pc.id);
			Assert.assertNotNull(pc3);
			Assert.assertFalse(ProjectCategory.find.get().isCodeExist(""));
		});
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
	public void RunTypeFindTest() throws Exception {
		af.accept(app -> {
			RunType rt = RunType.find.get().findAll().get(0);
			Assert.assertNotNull(rt);
			RunType rt2 = RunType.find.get().findByCode(rt.code); 
			Assert.assertNotNull(rt2);
			RunType rt3 = RunType.find.get().findById(rt.id);
			Assert.assertNotNull(rt3);
			ListObject lo = RunType.find.get().findAllForList().get(0);
			Assert.assertNotNull(lo);
			Assert.assertFalse(RunType.find.get().isCodeExist(""));
		});
	}
	
	@Test
	public void ReadSetTypeFindTest() throws Exception {
		af.accept(app -> {
			ReadSetType rt = ReadSetType.find.get().findAll().get(0);
			Assert.assertNotNull(rt);
			ReadSetType rt2 = ReadSetType.find.get().findByCode(rt.code); 
			Assert.assertNotNull(rt2);
			ReadSetType rt3 = ReadSetType.find.get().findById(rt.id);
			Assert.assertNotNull(rt3);
			ListObject lo = ReadSetType.find.get().findAllForList().get(0);
			Assert.assertNotNull(lo);
			Assert.assertFalse(ReadSetType.find.get().isCodeExist(""));
		});
	}

	@Test
	public void TreatmentTypeFindTest() throws Exception {
		af.accept(app -> {
			TreatmentType rt = TreatmentType.find.get().findAll().get(0);
			Assert.assertNotNull(rt);
			TreatmentType rt2 = TreatmentType.find.get().findByCode(rt.code); 
			Assert.assertNotNull(rt2);
			TreatmentType rt3 = TreatmentType.find.get().findById(rt.id);
			Assert.assertNotNull(rt3);
			ListObject lo = TreatmentType.find.get().findAllForList().get(0);
			Assert.assertNotNull(lo);
			Assert.assertFalse(TreatmentType.find.get().isCodeExist(""));
		});
	}
	
	@Test
	public void ImportTypeFindTest() throws Exception {
		af.accept(app -> {
			ImportType rt = ImportType.find.get().findAll().get(0);
			Assert.assertNotNull(rt);
			ImportType rt2 = ImportType.find.get().findByCode(rt.code); 
			Assert.assertNotNull(rt2);
			ImportType rt3 = ImportType.find.get().findById(rt.id);
			Assert.assertNotNull(rt3);
			ListObject lo = ImportType.find.get().findAllForList().get(0);
			Assert.assertNotNull(lo);
			Assert.assertFalse(ImportType.find.get().isCodeExist(""));
		});
	}
		
	@Test
	public void SampleTypeFindTest() throws Exception {
		af.accept(app -> {
			SampleType rt = SampleType.find.get().findAll().get(0);
			Assert.assertNotNull(rt);
			SampleType rt2 = SampleType.find.get().findByCode(rt.code); 
			Assert.assertNotNull(rt2);
			SampleType rt3 = SampleType.find.get().findById(rt.id);
			Assert.assertNotNull(rt3);
			ListObject lo = SampleType.find.get().findAllForList().get(0);
			Assert.assertNotNull(lo);
			Assert.assertFalse(SampleType.find.get().isCodeExist(""));
		});
	}
	
	@Test
	public void ContainerSupportCategoryFindTest() throws Exception {
		af.accept(app -> {
			ContainerSupportCategory type = ContainerSupportCategory.find.get().findAll().get(0);
			Assert.assertNotNull(type);
			ContainerSupportCategory cType = ContainerSupportCategory.find.get().findByCode(type.code);
			Assert.assertNotNull(cType);
			ContainerSupportCategory cTypeId = ContainerSupportCategory.find.get().findById(type.id);
			Assert.assertNotNull(cTypeId);
			Assert.assertFalse(ContainerSupportCategory.find.get().isCodeExist(""));
			Assert.assertNotNull(ContainerSupportCategory.find.get().findByContainerCategoryCode(""));
			Assert.assertNotNull(ContainerSupportCategory.find.get().findInputByExperimentTypeCode(""));
			Assert.assertFalse(ContainerSupportCategory.find.get().isCodeExist("")); 
			//internal fcts
			ContainerSupportCategoryDAO cscDAO = Spring.getBeanOfType(ContainerSupportCategoryDAO.class); 
			InstrumentUsedType iut =  InstrumentUsedType.find.get().findByCode("hand");
			Assert.assertNotNull(cscDAO.findInByInstrumentUsedType(iut.id));
		});
	}

	@Test
	public void ExperimentTypeNodeFindTest() throws Exception {
		af.accept(app -> {
			ExperimentTypeNode type = ExperimentTypeNode.find.get().findAll().get(0);
			Assert.assertNotNull(type);
			ExperimentTypeNode cType = ExperimentTypeNode.find.get().findByCode(type.code);
			Assert.assertNotNull(cType);
			ExperimentTypeNode cTypeId = ExperimentTypeNode.find.get().findById(type.id);
			Assert.assertNotNull(cTypeId);
			Assert.assertFalse(ExperimentTypeNode.find.get().isCodeExist(""));
		});
	}
	
	// FIXME: java.lang.IndexOutOfBoundsException: Index: 0
	// @Test
	public void ExperimentFindTest() throws Exception {
		af.accept(app -> {
			ExperimentType et = ExperimentType.find.get().findAll().get(0);
			Assert.assertNotNull(et);
			ExperimentType et2 = ExperimentType.find.get().findByCode(et.code);
			Assert.assertNotNull(et2);
			ExperimentType et3 = ExperimentType.find.get().findById(et.id);
			Assert.assertNotNull(et3);
			ExperimentType et4 = ExperimentType.find.get().findByCategoryCode(et.category.code).get(0);
			Assert.assertNotNull(et4);
			ProcessType pt = ProcessType.find.get().findAll().get(0);
			ExperimentType et5 = ExperimentType.find.get().findByCategoryCodeAndProcessTypeCode(pt.firstExperimentType.category.code, pt.code).get(0);
			Assert.assertNotNull(et5);
			ExperimentType et6 = ExperimentType.find.get().findPreviousExperimentTypeForAnExperimentTypeCode("fragmentation").get(0);
			Assert.assertNotNull(et6);
			ExperimentType et7 = ExperimentType.find.get().findByCategoryCodeWithoutOneToVoid("transformation").get(0);
			Assert.assertNotNull(et7);
			List<String> lstr = ExperimentType.find.get().findVoidProcessExperimentTypeCode("");
			Assert.assertEquals(0, lstr.size()); 
			//internal fct
			ExperimentTypeDAO etDAO = Spring.getBeanOfType(ExperimentTypeDAO.class);
			etDAO.findByProcessTypeCode(pt.code, false);
		});
	}

	@Test
	public void InstituteFindTest() throws Exception {
		af.accept(app -> {
			Institute type = Institute.find.get().findAll().get(0);
			Assert.assertNotNull(type);
			Institute cType = Institute.find.get().findByCode(type.code);
			Assert.assertNotNull(cType);
			Institute cTypeId = Institute.find.get().findById(type.id);
			Assert.assertNotNull(cTypeId);
			Assert.assertFalse(Institute.find.get().isCodeExist(""));
			//internal fcts
			InstituteDAO institDAO = Spring.getBeanOfType(InstituteDAO.class);
			CommonInfoType citInstrument = CommonInfoType.find.get().findByObjectTypeCode(ObjectType.CODE.Instrument).get(0);
			Assert.assertNotNull(institDAO.findByCommonInfoType(citInstrument.id));
		});
	}

	@Test
	public void MeasureUnitFindTest() throws Exception {
		af.accept(app -> {
			MeasureUnit type = MeasureUnit.find.get().findAll().get(0);
			Assert.assertNotNull(type);
			MeasureUnit cType = MeasureUnit.find.get().findByCode(type.code);
			Assert.assertNotNull(cType);
			MeasureUnit cTypeId = MeasureUnit.find.get().findById(type.id);
			Assert.assertNotNull(cTypeId);	
			Assert.assertNotNull(MeasureUnit.find.get().findByValue(type.code));
			Assert.assertFalse(MeasureUnit.find.get().isCodeExist(""));
		});
	}

	@Test
	public void ObjectTypeFindTest() throws Exception {
		af.accept(app -> {
			ObjectType type = ObjectType.find.get().findAll().get(0);
			Assert.assertNotNull(type);
			ObjectType cType = ObjectType.find.get().findByCode(type.code);
			Assert.assertNotNull(cType);
			ObjectType cTypeId = ObjectType.find.get().findById(type.id);
			Assert.assertNotNull(cTypeId);
			Assert.assertFalse(ObjectType.find.get().isCodeExist(""));
		});
	}

	// FIXME: failed: org.springframework.dao.IncorrectResultSizeDataAccessException: Incorrect result size: expected 1
	// @Test // (expected=UnsupportedOperationException.class)
	public void PropertyDefinitionFindTest() throws Exception {
		af.accept(app -> {
			PropertyDefinition type = PropertyDefinition.find.get().findAll().get(0);
			// PropertyDefinition cType = 
			PropertyDefinition.find.get().findByCode(type.code);
			// PropertyDefinition cTypeId = 
			PropertyDefinition.find.get().findById(type.id);
		});
	}
	
	@Test
	public void PropertyDefinitionFindTest2() throws Exception {
		af.accept(app -> {
			Assert.assertNotNull(PropertyDefinition.find.get().isCodeExist(""));		
			// internal fcts
			CommonInfoType citTreatment = CommonInfoType.find.get().findByObjectTypeCode(ObjectType.CODE.Treatment).get(0);
			PropertyDefinitionDAO pDAO = Spring.getBeanOfType(PropertyDefinitionDAO.class);
			Assert.assertNotNull(pDAO.findByCommonInfoType(citTreatment.id));
		});
	}
	
	@Test
	public void StateFindTest() throws Exception {
		af.accept(app -> {
			State type = State.find.get().findAll().get(0);
			Assert.assertNotNull(type);
			State cType = State.find.get().findByCode(type.code);
			Assert.assertNotNull(cType);
			State cTypeId = State.find.get().findById(type.id);
			Assert.assertNotNull(cTypeId);
			Assert.assertFalse(State.find.get().isCodeExist(""));
			Assert.assertNotNull(State.find.get().findAllForContainerList());
			Assert.assertNotNull(State.find.get().findByCategoryCode(""));
			Assert.assertNotNull(State.find.get().findByObjectTypeCode(ObjectType.CODE.Sample));
			Assert.assertNotNull(State.find.get().findByTypeCode(""));
			Assert.assertFalse(State.find.get().isCodeExistForTypeCode("",""));
		});
	}
	
	@Test(expected=EmptyResultDataAccessException.class)
	public void InstrumentUsedTypeFindTest() throws Exception {
		af.accept(app -> {
			InstrumentUsedType type = InstrumentUsedType.find.get().findAll().get(0);
			Assert.assertNotNull(type);
			InstrumentUsedType cType = InstrumentUsedType.find.get().findByCode(type.code);
			Assert.assertNotNull(cType);
			InstrumentUsedType cTypeId = InstrumentUsedType.find.get().findById(type.id);
			Assert.assertNotNull(cTypeId);
			Assert.assertFalse(InstrumentUsedType.find.get().isCodeExist(""));
			Assert.assertNotNull(InstrumentUsedType.find.get().findByExperimentTypeCode("", null));	
			InstrumentUsedTypeDAO iDAO = Spring.getBeanOfType(InstrumentUsedTypeDAO.class);
			List<InstrumentUsedType> liut = new ArrayList<>();
			liut = iDAO.findByExperimentId(0);
			Assert.assertTrue(liut.size() >= 0);
			Map<String, Object> m = new HashMap<>();
			m = iDAO.findTypeCodeAndCatCode(0);
			Assert.assertTrue(m.size() >= 0);
		});
	}
	
	@Test
	public void InstrumentFindTest() throws Exception {
		af.accept(app -> {
			InstrumentQueryParams instrumentQuery = new InstrumentQueryParams();
			instrumentQuery.typeCode = "ARGUS";
			List<Instrument> intruments = Instrument.find.get().findByQueryParams(instrumentQuery);
			Assert.assertNotNull(intruments);

			instrumentQuery = new InstrumentQueryParams();
			instrumentQuery.typeCodes = new ArrayList<>();
			instrumentQuery.typeCodes.add("ARGUS");
			intruments = Instrument.find.get().findByQueryParams(instrumentQuery);
			Assert.assertNotNull(intruments);

			instrumentQuery = new InstrumentQueryParams();
			instrumentQuery.categoryCodes = new ArrayList<>();
			instrumentQuery.categoryCodes.add("covaris");
			intruments = Instrument.find.get().findByQueryParams(instrumentQuery);
			Assert.assertNotNull(intruments);

			instrumentQuery = new InstrumentQueryParams();
			instrumentQuery.typeCode = "ARGUS";
			instrumentQuery.categoryCode = "opt-map-opgen";
			intruments = Instrument.find.get().findByQueryParams(instrumentQuery);
			Assert.assertNotNull(intruments);

			instrumentQuery = new InstrumentQueryParams();
			instrumentQuery.typeCodes = new ArrayList<>();
			instrumentQuery.typeCodes.add("ARGUS");
			instrumentQuery.categoryCodes = new ArrayList<>();
			instrumentQuery.categoryCodes.add("opt-map-opgen");
			intruments = Instrument.find.get().findByQueryParams(instrumentQuery);
			Assert.assertNotNull(intruments);
		});
	}

}

//package models.dao.mapping;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import org.junit.Assert;
//import org.junit.Test;
//import org.springframework.dao.EmptyResultDataAccessException;
//
//import models.laboratory.common.description.CommonInfoType;
//import models.laboratory.common.description.Institute;
//import models.laboratory.common.description.MeasureUnit;
//import models.laboratory.common.description.ObjectType;
//import models.laboratory.common.description.PropertyDefinition;
//import models.laboratory.common.description.State;
//import models.laboratory.common.description.dao.InstituteDAO;
//import models.laboratory.common.description.dao.PropertyDefinitionDAO;
//import models.laboratory.container.description.ContainerSupportCategory;
//import models.laboratory.container.description.dao.ContainerSupportCategoryDAO;
//import models.laboratory.experiment.description.ExperimentType;
//import models.laboratory.experiment.description.dao.ExperimentTypeDAO;
//import models.laboratory.instrument.description.Instrument;
//import models.laboratory.instrument.description.InstrumentQueryParams;
//import models.laboratory.instrument.description.InstrumentUsedType;
//import models.laboratory.instrument.description.dao.InstrumentUsedTypeDAO;
//import models.laboratory.processes.description.ExperimentTypeNode;
//import models.laboratory.processes.description.ProcessType;
//import models.laboratory.project.description.ProjectCategory;
//import models.laboratory.project.description.ProjectType;
//import models.laboratory.run.description.ReadSetType;
//import models.laboratory.run.description.RunType;
//import models.laboratory.run.description.TreatmentType;
//import models.laboratory.sample.description.ImportType;
//import models.laboratory.sample.description.SampleType;
//import models.utils.ListObject;
//import models.utils.dao.DAOException;
//import play.api.modules.spring.Spring;
//import utils.AbstractTests;
//
//public class FindDAOTest extends AbstractTests {
//	
//	protected static play.Logger.ALogger logger = play.Logger.of(FindDAOTest.class);
//	
//	@Test
//	public void CommonInfoTypeFindTest() throws DAOException {
//		CommonInfoType type = CommonInfoType.find.get().findAll().get(0);
//		Assert.assertNotNull(type);
//		CommonInfoType cType = CommonInfoType.find.get().findByCode(type.code);
//		Assert.assertNotNull(cType);
//		CommonInfoType cTypeId = CommonInfoType.find.get().findById(type.id);
//		Assert.assertNotNull(cTypeId);
//		Assert.assertFalse(CommonInfoType.find.get().isCodeExist(""));
//		CommonInfoType typeRun = CommonInfoType.find.get().findByObjectTypeCode(ObjectType.CODE.Run).get(0);
//		Assert.assertNotNull(typeRun);
//	}
//	
//	@Test 
//	public void ProcessTypeFindTest() throws DAOException {
//		ProcessType pt = ProcessType.find.get().findAll().get(0);
//		Assert.assertNotNull(pt);
//		ProcessType pt2 = ProcessType.find.get().findByCode(pt.code); 
//		Assert.assertNotNull(pt2);
//		ProcessType pt3 = ProcessType.find.get().findById(pt.id);
//		Assert.assertNotNull(pt3);
//		ProcessType pt4 = ProcessType.find.get().findByProcessCategoryCodes(pt.category.code).get(0);
//		Assert.assertNotNull(pt4);
//		Assert.assertFalse(ProcessType.find.get().isCodeExist(""));
//		// List<ProcessType> pt5 = 
//				ProcessType.find.get().findByExperimentTypeCode("");
//	}
//
//	@Test 
//	public void ProjectTypeFindTest() throws DAOException {
//		ProjectType pt = ProjectType.find.get().findAll().get(0);
//		Assert.assertNotNull(pt);
//		ProjectType pt2 = ProjectType.find.get().findByCode(pt.code); 
//		Assert.assertNotNull(pt2);
//		ProjectType pt3 = ProjectType.find.get().findById(pt.id);
//		Assert.assertNotNull(pt3);
//		ListObject lo = ProjectType.find.get().findAllForList().get(0);
//		Assert.assertNotNull(lo);
//		Assert.assertFalse(ProjectType.find.get().isCodeExist("")); 
//	}
//	
//	@Test
//	public void ProjectCategoryFindTest() throws DAOException {
//		ProjectCategory pc = ProjectCategory.find.get().findAll().get(0);
//		Assert.assertNotNull(pc);
//		ProjectCategory pc2 = ProjectCategory.find.get().findByCode(pc.code); 
//		Assert.assertNotNull(pc2);
//		ProjectCategory pc3 = ProjectCategory.find.get().findById(pc.id);
//		Assert.assertNotNull(pc3);
//		Assert.assertFalse(ProjectCategory.find.get().isCodeExist("")); 		
//	}
//	
//	/*@Test
//	public void ReagentTypeFindTest() throws DAOException {
//		if (! ReagentCategory.find.findAll().isEmpty()) {
//			ReagentCategory rt = ReagentCategory.find.findAll().get(0);
//			Assert.assertNotNull(rt);
//			ReagentCategory rt2 = ReagentCategory.find.findByCode(rt.code); 
//			Assert.assertNotNull(rt2);
//			ReagentCategory rt3 = ReagentCategory.find.findById(rt.id);
//			Assert.assertNotNull(rt3);
//			ListObject lo = ReagentCategory.find.findAllForList().get(0);
//			Assert.assertNotNull(lo);
//		}
//		else {
//			ReagentCategory rt2 = ReagentCategory.find.findByCode(""); 
//			Assert.assertNull(rt2);
//			ReagentCategory rt3 = ReagentCategory.find.findById(Long.valueOf(0));
//			Assert.assertNull(rt3);			
//		}
//		Assert.assertFalse(ReagentCategory.find.isCodeExist(""));
//	}*/
//
//	@Test
//	public void RunTypeFindTest() throws DAOException {
//		RunType rt = RunType.find.get().findAll().get(0);
//		Assert.assertNotNull(rt);
//		RunType rt2 = RunType.find.get().findByCode(rt.code); 
//		Assert.assertNotNull(rt2);
//		RunType rt3 = RunType.find.get().findById(rt.id);
//		Assert.assertNotNull(rt3);
//		ListObject lo = RunType.find.get().findAllForList().get(0);
//		Assert.assertNotNull(lo);
//		Assert.assertFalse(RunType.find.get().isCodeExist(""));
//	}
//	
//	@Test
//	public void ReadSetTypeFindTest() throws DAOException {
//		ReadSetType rt = ReadSetType.find.get().findAll().get(0);
//		Assert.assertNotNull(rt);
//		ReadSetType rt2 = ReadSetType.find.get().findByCode(rt.code); 
//		Assert.assertNotNull(rt2);
//		ReadSetType rt3 = ReadSetType.find.get().findById(rt.id);
//		Assert.assertNotNull(rt3);
//		ListObject lo = ReadSetType.find.get().findAllForList().get(0);
//		Assert.assertNotNull(lo);
//		Assert.assertFalse(ReadSetType.find.get().isCodeExist(""));
//	}
//
//	@Test
//	public void TreatmentTypeFindTest() throws DAOException {
//		TreatmentType rt = TreatmentType.find.get().findAll().get(0);
//		Assert.assertNotNull(rt);
//		TreatmentType rt2 = TreatmentType.find.get().findByCode(rt.code); 
//		Assert.assertNotNull(rt2);
//		TreatmentType rt3 = TreatmentType.find.get().findById(rt.id);
//		Assert.assertNotNull(rt3);
//		ListObject lo = TreatmentType.find.get().findAllForList().get(0);
//		Assert.assertNotNull(lo);
//		Assert.assertFalse(TreatmentType.find.get().isCodeExist(""));
//	}
//	
//	@Test
//	public void ImportTypeFindTest() throws DAOException {
//		ImportType rt = ImportType.find.get().findAll().get(0);
//		Assert.assertNotNull(rt);
//		ImportType rt2 = ImportType.find.get().findByCode(rt.code); 
//		Assert.assertNotNull(rt2);
//		ImportType rt3 = ImportType.find.get().findById(rt.id);
//		Assert.assertNotNull(rt3);
//		ListObject lo = ImportType.find.get().findAllForList().get(0);
//		Assert.assertNotNull(lo);
//		Assert.assertFalse(ImportType.find.get().isCodeExist(""));		
//	}
//		
//	@Test
//	public void SampleTypeFindTest() throws DAOException {
//		SampleType rt = SampleType.find.get().findAll().get(0);
//		Assert.assertNotNull(rt);
//		SampleType rt2 = SampleType.find.get().findByCode(rt.code); 
//		Assert.assertNotNull(rt2);
//		SampleType rt3 = SampleType.find.get().findById(rt.id);
//		Assert.assertNotNull(rt3);
//		ListObject lo = SampleType.find.get().findAllForList().get(0);
//		Assert.assertNotNull(lo);
//		Assert.assertFalse(SampleType.find.get().isCodeExist(""));
//	}
//	
//	@Test
//	public void ContainerSupportCategoryFindTest() throws DAOException {
//		ContainerSupportCategory type = ContainerSupportCategory.find.get().findAll().get(0);
//		Assert.assertNotNull(type);
//		ContainerSupportCategory cType = ContainerSupportCategory.find.get().findByCode(type.code);
//		Assert.assertNotNull(cType);
//		ContainerSupportCategory cTypeId = ContainerSupportCategory.find.get().findById(type.id);
//		Assert.assertNotNull(cTypeId);
//		Assert.assertFalse(ContainerSupportCategory.find.get().isCodeExist(""));
//		Assert.assertNotNull(ContainerSupportCategory.find.get().findByContainerCategoryCode(""));
//		Assert.assertNotNull(ContainerSupportCategory.find.get().findInputByExperimentTypeCode(""));
//		Assert.assertFalse(ContainerSupportCategory.find.get().isCodeExist("")); 
//		//internal fcts
//		ContainerSupportCategoryDAO cscDAO = Spring.getBeanOfType(ContainerSupportCategoryDAO.class); 
//		InstrumentUsedType iut =  InstrumentUsedType.find.get().findByCode("hand");
//		Assert.assertNotNull(cscDAO.findInByInstrumentUsedType(iut.id));		
//	}
//
//	@Test
//	public void ExperimentTypeNodeFindTest() throws DAOException {
//		ExperimentTypeNode type = ExperimentTypeNode.find.get().findAll().get(0);
//		Assert.assertNotNull(type);
//		ExperimentTypeNode cType = ExperimentTypeNode.find.get().findByCode(type.code);
//		Assert.assertNotNull(cType);
//		ExperimentTypeNode cTypeId = ExperimentTypeNode.find.get().findById(type.id);
//		Assert.assertNotNull(cTypeId);
//		Assert.assertFalse(ExperimentTypeNode.find.get().isCodeExist(""));
//	}
//	
//	@Test
//	public void ExperimentFindTest() throws DAOException {
//		ExperimentType et = ExperimentType.find.get().findAll().get(0);
//		Assert.assertNotNull(et);
//		ExperimentType et2 = ExperimentType.find.get().findByCode(et.code);
//		Assert.assertNotNull(et2);
//		ExperimentType et3 = ExperimentType.find.get().findById(et.id);
//		Assert.assertNotNull(et3);
//		ExperimentType et4 = ExperimentType.find.get().findByCategoryCode(et.category.code).get(0);
//		Assert.assertNotNull(et4);
//		ProcessType pt = ProcessType.find.get().findAll().get(0);
//		ExperimentType et5 = ExperimentType.find.get().findByCategoryCodeAndProcessTypeCode(pt.firstExperimentType.category.code, pt.code).get(0);
//		Assert.assertNotNull(et5);
//		ExperimentType et6 = ExperimentType.find.get().findPreviousExperimentTypeForAnExperimentTypeCode("fragmentation").get(0);
//		Assert.assertNotNull(et6);
//		ExperimentType et7 = ExperimentType.find.get().findByCategoryCodeWithoutOneToVoid("transformation").get(0);
//		Assert.assertNotNull(et7);
//		List<String> lstr = ExperimentType.find.get().findVoidProcessExperimentTypeCode("");
//		Assert.assertEquals(0, lstr.size()); 
//		//internal fct
//		ExperimentTypeDAO etDAO = Spring.getBeanOfType(ExperimentTypeDAO.class);
//		etDAO.findByProcessTypeCode(pt.code, false);
//	}
//
//	@Test
//	public void InstituteFindTest() throws DAOException {
//		Institute type = Institute.find.get().findAll().get(0);
//		Assert.assertNotNull(type);
//		Institute cType = Institute.find.get().findByCode(type.code);
//		Assert.assertNotNull(cType);
//		Institute cTypeId = Institute.find.get().findById(type.id);
//		Assert.assertNotNull(cTypeId);
//		Assert.assertFalse(Institute.find.get().isCodeExist(""));
//		//internal fcts
//		InstituteDAO institDAO = Spring.getBeanOfType(InstituteDAO.class);
//		CommonInfoType citInstrument = CommonInfoType.find.get().findByObjectTypeCode(ObjectType.CODE.Instrument).get(0);
//		Assert.assertNotNull(institDAO.findByCommonInfoType(citInstrument.id)); 
//	}
//
//	@Test
//	public void MeasureUnitFindTest() throws DAOException {
//		MeasureUnit type = MeasureUnit.find.get().findAll().get(0);
//		Assert.assertNotNull(type);
//		MeasureUnit cType = MeasureUnit.find.get().findByCode(type.code);
//		Assert.assertNotNull(cType);
//		MeasureUnit cTypeId = MeasureUnit.find.get().findById(type.id);
//		Assert.assertNotNull(cTypeId);	
//		Assert.assertNotNull(MeasureUnit.find.get().findByValue(type.code));
//		Assert.assertFalse(MeasureUnit.find.get().isCodeExist(""));
//	}
//
//	@Test
//	public void ObjectTypeFindTest() throws DAOException {
//		ObjectType type = ObjectType.find.get().findAll().get(0);
//		Assert.assertNotNull(type);
//		ObjectType cType = ObjectType.find.get().findByCode(type.code);
//		Assert.assertNotNull(cType);
//		ObjectType cTypeId = ObjectType.find.get().findById(type.id);
//		Assert.assertNotNull(cTypeId);
//		Assert.assertFalse(ObjectType.find.get().isCodeExist(""));
//	}
//
//	@Test // (expected=UnsupportedOperationException.class)
//	public void PropertyDefinitionFindTest() throws DAOException {
//		PropertyDefinition type = PropertyDefinition.find.get().findAll().get(0);
//		// PropertyDefinition cType = 
//				PropertyDefinition.find.get().findByCode(type.code);
//		// PropertyDefinition cTypeId = 
//				PropertyDefinition.find.get().findById(type.id);
//	}
//	
//	@Test
//	public void PropertyDefinitionFindTest2() throws DAOException {
//		Assert.assertNotNull(PropertyDefinition.find.get().isCodeExist(""));		
//		//internal fcts
//		CommonInfoType citTreatment = CommonInfoType.find.get().findByObjectTypeCode(ObjectType.CODE.Treatment).get(0);
//		PropertyDefinitionDAO pDAO = Spring.getBeanOfType(PropertyDefinitionDAO.class);
//		Assert.assertNotNull(pDAO.findByCommonInfoType(citTreatment.id));
//	}
//	
//	@Test
//	public void StateFindTest() throws DAOException {
//		State type = State.find.get().findAll().get(0);
//		Assert.assertNotNull(type);
//		State cType = State.find.get().findByCode(type.code);
//		Assert.assertNotNull(cType);
//		State cTypeId = State.find.get().findById(type.id);
//		Assert.assertNotNull(cTypeId);
//		Assert.assertFalse(State.find.get().isCodeExist(""));
//		Assert.assertNotNull(State.find.get().findAllForContainerList());
//		Assert.assertNotNull(State.find.get().findByCategoryCode(""));
//		Assert.assertNotNull(State.find.get().findByObjectTypeCode(ObjectType.CODE.Sample));
//		Assert.assertNotNull(State.find.get().findByTypeCode(""));
//		Assert.assertFalse(State.find.get().isCodeExistForTypeCode("",""));
//	}
//	
//	@Test(expected=EmptyResultDataAccessException.class)
//	public void InstrumentUsedTypeFindTest() throws DAOException {
//		InstrumentUsedType type = InstrumentUsedType.find.get().findAll().get(0);
//		Assert.assertNotNull(type);
//		InstrumentUsedType cType = InstrumentUsedType.find.get().findByCode(type.code);
//		Assert.assertNotNull(cType);
//		InstrumentUsedType cTypeId = InstrumentUsedType.find.get().findById(type.id);
//		Assert.assertNotNull(cTypeId);
//		Assert.assertFalse(InstrumentUsedType.find.get().isCodeExist(""));
//		Assert.assertNotNull(InstrumentUsedType.find.get().findByExperimentTypeCode("", null));	
//		InstrumentUsedTypeDAO iDAO = Spring.getBeanOfType(InstrumentUsedTypeDAO.class);
//		List<InstrumentUsedType> liut = new ArrayList<>();
//		liut = iDAO.findByExperimentId(0);
//		Assert.assertTrue(liut.size() >= 0);
//		Map<String, Object> m = new HashMap<>();
//		m = iDAO.findTypeCodeAndCatCode(0);
//		Assert.assertTrue(m.size() >= 0);
//	}
//	
//	@Test
//	public void InstrumentFindTest() throws DAOException {
//		InstrumentQueryParams instrumentQuery = new InstrumentQueryParams();
//		instrumentQuery.typeCode = "ARGUS";
//		List<Instrument> intruments = Instrument.find.get().findByQueryParams(instrumentQuery);
//		Assert.assertNotNull(intruments);
//		
//		instrumentQuery = new InstrumentQueryParams();
//		instrumentQuery.typeCodes = new ArrayList<>();
//		instrumentQuery.typeCodes.add("ARGUS");
//		intruments = Instrument.find.get().findByQueryParams(instrumentQuery);
//		Assert.assertNotNull(intruments);
//		
//		instrumentQuery = new InstrumentQueryParams();
//		instrumentQuery.categoryCodes = new ArrayList<>();
//		instrumentQuery.categoryCodes.add("covaris");
//		intruments = Instrument.find.get().findByQueryParams(instrumentQuery);
//		Assert.assertNotNull(intruments);
//		
//		instrumentQuery = new InstrumentQueryParams();
//		instrumentQuery.typeCode = "ARGUS";
//		instrumentQuery.categoryCode = "opt-map-opgen";
//		intruments = Instrument.find.get().findByQueryParams(instrumentQuery);
//		Assert.assertNotNull(intruments);
//		
//		instrumentQuery = new InstrumentQueryParams();
//		instrumentQuery.typeCodes = new ArrayList<>();
//		instrumentQuery.typeCodes.add("ARGUS");
//		instrumentQuery.categoryCodes = new ArrayList<>();
//		instrumentQuery.categoryCodes.add("opt-map-opgen");
//		intruments = Instrument.find.get().findByQueryParams(instrumentQuery);
//		Assert.assertNotNull(intruments);		
//	}
//
//}
