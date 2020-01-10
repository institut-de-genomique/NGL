package models.dao;


import java.util.List;

import models.laboratory.common.description.State;
import models.laboratory.experiment.description.ExperimentType;
import models.laboratory.instrument.description.InstrumentUsedType;
import models.laboratory.processes.description.ProcessType;
import models.laboratory.project.description.ProjectType;
import models.laboratory.sample.description.ImportType;
import models.laboratory.sample.description.SampleType;
import models.utils.dao.DAOException;

import org.junit.Assert;

import utils.AbstractTests;


/**
 * Test sur base vide avec dump.sql
 * @author ejacoby
 *
 */

public class RemoveDescriptionTest extends AbstractTests{
	
	
	
	
	//@Test
	public void removeProcessType() throws DAOException
	{
		ProcessType processType = ProcessType.find.findByCode("process1");
		processType.remove();
		Assert.assertNull(ProcessType.find.findByCode("process1"));
	}

	//@Test
	public void removeProjectType() throws DAOException
	{
		ProjectType projectType = ProjectType.find.findByCode("project1");
		projectType.remove();
		Assert.assertNull(ProjectType.find.findByCode("project1"));
	}
	
	//@Test
	public void removeSampleType() throws DAOException
	{
		SampleType sampleType = SampleType.find.findByCode("sample1");
		sampleType.remove();
		Assert.assertNull(SampleType.find.findByCode("sample1"));
	}

	//@Test
	public void removeImportType() throws DAOException
	{
		ImportType importType = ImportType.find.findByCode("import1");
		importType.remove();
		Assert.assertNull(ImportType.find.findByCode("import1"));
	}

	
	//@Test
	public void removeState() throws DAOException
	{
		State state = State.find.findByCode("state1");
		state.remove();
		state = State.find.findByCode("state1");
		Assert.assertNull(state);
	}

	//@Test
	public void removeInstrumentUsedType() throws DAOException
	{
		InstrumentUsedType instrumentUsedType = InstrumentUsedType.find.findByCode("inst1");
		instrumentUsedType.remove();
		Assert.assertNull(InstrumentUsedType.find.findByCode("inst1"));

	}
	
	
	
	//@Test
	public void removeExperimentType() throws DAOException
	{
		List<ExperimentType> experimentTypes = ExperimentType.find.findAll();
		for(ExperimentType et : experimentTypes){
			et.remove();
			Assert.assertNull(ExperimentType.find.findByCode(et.name));	
		}				
	}


	//@Test
	/*public void removeReagentType() throws DAOException
	{
		ReagentCategory reagentType = ReagentCategory.find.findByCode("reagent1");
		reagentType.remove();
		Assert.assertNull(ReagentCategory.find.findByCode("reagent1"));
	}*/

	
}
