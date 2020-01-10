package models.dao;

import org.junit.Assert;

import models.laboratory.common.description.MeasureCategory;
import models.laboratory.common.description.StateCategory;
import models.laboratory.container.description.ContainerSupportCategory;
import models.laboratory.experiment.description.ExperimentCategory;
import models.laboratory.experiment.description.ProtocolCategory;
import models.laboratory.instrument.description.InstrumentCategory;
import models.laboratory.processes.description.ProcessCategory;
import models.laboratory.project.description.ProjectCategory;
import models.laboratory.sample.description.ImportCategory;
import models.laboratory.sample.description.SampleCategory;
import models.laboratory.sample.description.dao.SampleCategoryDAO;
import models.utils.dao.DAOException;
import play.api.modules.spring.Spring;
import utils.AbstractTests;

/**
 * Test sur base vide avec dump.sql
 * @author ejacoby
 *
 */
public class RemoveCatDescriptionTest extends AbstractTests {

	//@Test
	public void removeContainerSupportCategory() throws DAOException
	{
		ContainerSupportCategory containerSupportCategory = ContainerSupportCategory.find.findByCode("support1");
		containerSupportCategory.remove();
		Assert.assertNull(ContainerSupportCategory.find.findByCode("support1"));
		containerSupportCategory = ContainerSupportCategory.find.findByCode("support2");
		containerSupportCategory.remove();
		containerSupportCategory = ContainerSupportCategory.find.findByCode("support3");
		containerSupportCategory.remove();
		containerSupportCategory = ContainerSupportCategory.find.findByCode("support4");
		containerSupportCategory.remove();
		containerSupportCategory = ContainerSupportCategory.find.findByCode("support5");
		containerSupportCategory.remove();
		containerSupportCategory = ContainerSupportCategory.find.findByCode("support6");
		containerSupportCategory.remove();
	}

	//@Test
	public void removeInstrumentCategory() throws DAOException
	{
		InstrumentCategory instrumentCategory = InstrumentCategory.find.findByCode("InstCat1");
		instrumentCategory.remove();
		Assert.assertNull(InstrumentCategory.find.findByCode("InstCat1"));
		instrumentCategory = InstrumentCategory.find.findByCode("InstCat2");
		instrumentCategory.remove();
	}
	
	//@Test
	public void removeProtocolCategory() throws DAOException
	{
		ProtocolCategory protocolCategory = ProtocolCategory.find.findByCode("protoCat1");
		protocolCategory.remove();
		Assert.assertNull(ProtocolCategory.find.findByCode("protoCat1"));
		protocolCategory = ProtocolCategory.find.findByCode("protoCat2");
		protocolCategory.remove();
		protocolCategory = ProtocolCategory.find.findByCode("protoCat3");
		protocolCategory.remove();
		protocolCategory = ProtocolCategory.find.findByCode("protoCat4");
		protocolCategory.remove();

	}

	//@Test
	public void removeStateCategory() throws DAOException
	{
		StateCategory stateCategory = StateCategory.find.findByCode("catState1");
		stateCategory.remove();
		Assert.assertNull(StateCategory.find.findByCode("catState1"));
	}
		
	//@Test
	public void removeExperimentCategory() throws DAOException
	{
		ExperimentCategory experimentCategory = ExperimentCategory.find.findByCode("expCat1");
		experimentCategory.remove();
		Assert.assertNull(ExperimentCategory.find.findByCode("expCat1"));
	}

	//@Test
	public void removeProcessCategory() throws DAOException
	{
		ProcessCategory processCategory = ProcessCategory.find.findByCode("processCat1");
		processCategory.remove();
		Assert.assertNull(ProcessCategory.find.findByCode("processCat1"));
	}

	//@Test
	public void removeSampleCategory() throws DAOException
	{
		SampleCategoryDAO sampleCategoryDAO = Spring.getBeanOfType(SampleCategoryDAO.class);
		SampleCategory sampleCategory = sampleCategoryDAO.findByCode("sampleCat1");
		sampleCategoryDAO.remove(sampleCategory);
		Assert.assertNull(sampleCategoryDAO.findByCode("sampleCat1"));
	}
	
	//@Test
	public void removeProjectCategory() throws DAOException
	{
		ProjectCategory projectCategory = ProjectCategory.find.findByCode("projectCat1");
		projectCategory.remove();
		Assert.assertNull(ProjectCategory.find.findByCode("projectCat1"));
	}

	//@Test
	public void removeImportCategory() throws DAOException
	{
		ImportCategory importCategory = ImportCategory.find.findByCode("import1");
		importCategory.remove();
		Assert.assertNull(ImportCategory.find.findByCode("sampleCat1"));
	}
	
	//@Test
	public void removeMeasureCategory() throws DAOException
	{
		MeasureCategory measureCategory = MeasureCategory.find.findByCode("cat1");
		//List<MeasureUnit> measureValues = measureCategory.measurePossibleValues;
		measureCategory.remove();
		measureCategory = MeasureCategory.find.findByCode("cat1");
		Assert.assertNull(measureCategory);
		//Check measure Values
		/*
		for(MeasureUnit measureValue : measureValues){
			Assert.assertNull(MeasureUnit.find.findById(measureValue.id));
		}
		*/
	}
	
}
