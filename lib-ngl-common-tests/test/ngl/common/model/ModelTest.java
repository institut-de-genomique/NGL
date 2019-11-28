package ngl.common.model;

import java.util.List;
import java.util.function.Supplier;

import org.junit.Test;

import models.administration.authorisation.Permission;
import models.administration.authorisation.Role;
import models.administration.authorisation.User;
import models.laboratory.common.description.CommonInfoType;
import models.laboratory.common.description.Institute;
import models.laboratory.common.description.Level;
import models.laboratory.common.description.MeasureCategory;
import models.laboratory.common.description.MeasureUnit;
import models.laboratory.common.description.ObjectType;
import models.laboratory.common.description.PropertyDefinition;
import models.laboratory.common.description.State;
import models.laboratory.common.description.StateCategory;
import models.laboratory.common.description.StateHierarchy;
import models.laboratory.common.description.Value;
import models.laboratory.container.description.ContainerCategory;
import models.laboratory.container.description.ContainerSupportCategory;
import models.laboratory.experiment.description.ExperimentCategory;
import models.laboratory.experiment.description.ExperimentType;
import models.laboratory.experiment.description.ProtocolCategory;
import models.laboratory.instrument.description.InstrumentCategory;
import models.laboratory.instrument.description.InstrumentUsedType;
import models.laboratory.processes.description.ExperimentTypeNode;
import models.laboratory.processes.description.ProcessCategory;
import models.laboratory.processes.description.ProcessType;
import models.laboratory.project.description.ProjectCategory;
import models.laboratory.project.description.ProjectType;
import models.laboratory.run.description.AnalysisType;
import models.laboratory.run.description.ReadSetType;
import models.laboratory.run.description.RunCategory;
import models.laboratory.run.description.RunType;
import models.laboratory.run.description.TreatmentCategory;
import models.laboratory.run.description.TreatmentContext;
import models.laboratory.run.description.TreatmentType;
import models.laboratory.sample.description.ImportCategory;
import models.laboratory.sample.description.ImportType;
import models.laboratory.sample.description.SampleCategory;
import models.laboratory.sample.description.SampleType;
import models.utils.ModelDAOs;
import models.utils.dao.AbstractDAO;
import ngl.common.Global;


// Tests the proper Model DAO boot for all the Model subclasses.

// Test write order is alphabetical with a split 
//   AbstractCategory and subclasses
//   CommonInfoType and subclasses
//   Model subclasses

public class ModelTest {

	private static final play.Logger.ALogger logger = play.Logger.of(ModelTest.class);
	
	private <T> void doList(Supplier<? extends AbstractDAO<T>> dao, Class<T> type) throws Exception {
		Global.af.run(app -> {
			// Those are duplicate access to the same collection but we first
			// try to access the collection through the DAO and then test access through
			// the ModelDAOs. The 2 lists should be identical.
			dao.get().findAll();
			List<T> ts = ModelDAOs.instance.get().findAll(type);
			for (T t : ts) 
				logger.debug("  {} {}",type,t);
		});
	}
	
	// -- Abstract category
	@Test
	public void readContainerCategories() throws Exception {
		doList(ContainerCategory.find, ContainerCategory.class);
	}
		
	@Test
	public void readContainerSupportCategories() throws Exception {
		doList(ContainerSupportCategory.find, ContainerSupportCategory.class);
	}
		
	@Test
	public void readExperimentCategories() throws Exception {
		doList(ExperimentCategory.find, ExperimentCategory.class);
	}
	
	@Test
	public void readImportCategories() throws Exception {
		doList(ImportCategory.find, ImportCategory.class);
	}
	
	@Test
	public void readInstrumentCategories() throws Exception {
		doList(InstrumentCategory.find, InstrumentCategory.class);
	}
	
	@Test
	public void readMeasureCategories() throws Exception {
		doList(MeasureCategory.find, MeasureCategory.class);
	}
	
	@Test
	public void readProcessCategories() throws Exception {
		doList(ProcessCategory.find, ProcessCategory.class);
	}
	
	@Test
	public void readProjectCategories() throws Exception {
		doList(ProjectCategory.find, ProjectCategory.class);
	}

	@Test
	public void readProtocolCategories() throws Exception {
		doList(ProtocolCategory.find, ProtocolCategory.class);
	}
	
	@Test
	public void readRunCategories() throws Exception {
		doList(RunCategory.find, RunCategory.class);
	}
	
	@Test
	public void readSampleCategories() throws Exception {
		doList(SampleCategory.find, SampleCategory.class);
	}
	
	@Test
	public void readStateCategories() throws Exception {
		doList(StateCategory.find, StateCategory.class);
	}
	
	@Test
	public void readTreatmentCategories() throws Exception {
		doList(TreatmentCategory.find, TreatmentCategory.class);
	}
	
	// -- Common info type
	
	@Test
	public void readCommonInfoTypes() throws Exception {
		doList(CommonInfoType.find, CommonInfoType.class);
	}
	
	@Test
	public void readAnalysisTypes() throws Exception {
		doList(AnalysisType.find, AnalysisType.class);
	}

	@Test
	public void readExperimentTypes() throws Exception {
		doList(ExperimentType.find, ExperimentType.class);
	}

	@Test
	public void readImportTypes() throws Exception {
		doList(ImportType.find, ImportType.class);
	}
	
	@Test
	public void readInstrumentUsedTypes() throws Exception {
		doList(InstrumentUsedType.find, InstrumentUsedType.class);
	}
	
	@Test
	public void readProcessTypeTypes() throws Exception {
		doList(ProcessType.find, ProcessType.class);
	}
	
	@Test
	public void readProjectTypes() throws Exception {
		doList(ProjectType.find, ProjectType.class);
	}
	
	@Test
	public void readReadSetTypes() throws Exception {
		doList(ReadSetType.find, ReadSetType.class);
	}
	
	@Test
	public void readRunTypes() throws Exception {
		doList(RunType.find, RunType.class);
	}
	
	@Test
	public void readSampleTypes() throws Exception {
		doList(SampleType.find, SampleType.class);
	}
	
	@Test
	public void readTreatmentTypes() throws Exception {
		doList(TreatmentType.find, TreatmentType.class);
	}
	
	// -- Model
	
	@Test
	public void readExperimentTypeNodes() throws Exception {
		doList(ExperimentTypeNode.find, ExperimentTypeNode.class);
	}

	@Test
	public void readInstitutes() throws Exception {
		doList(Institute.find, Institute.class);
	}

	@Test
	public void readLevels() throws Exception {
		doList(Level.find, Level.class);
	}

	@Test
	public void readMeasureUnits() throws Exception {
		doList(MeasureUnit.find, MeasureUnit.class);
	}
	
	@Test
	public void readObjectTypes() throws Exception {
		doList(ObjectType.find, ObjectType.class);
	}
	
	// @Test
	public void readPermissions() throws Exception {
		doList(Permission.find, Permission.class);
	}
	
	@Test
	public void readPropertyDefinitions() throws Exception {
		doList(PropertyDefinition.find, PropertyDefinition.class);
	}
	
	// @Test
	public void readRoles() throws Exception {
		doList(Role.find, Role.class);
	}
	
	@Test
	public void readStates() throws Exception {
		doList(State.find, State.class);
	}
	
	@Test
	public void readStateHierarchies() throws Exception {
		doList(StateHierarchy.find, StateHierarchy.class);
	}
	
	@Test
	public void readTreatmentContexts() throws Exception {
		doList(TreatmentContext.find, TreatmentContext.class);
	}

	// @Test
	public void readUsers() throws Exception {
		doList(User.find, User.class);
	}

	// error : java.lang.UnsupportedOperationException: Value can be listed,
	// @Test
	public void readValues() throws Exception {
		doList(Value.find, Value.class);
	}
	
}
