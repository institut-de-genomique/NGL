package nglapps;

import java.util.List;
import java.util.function.Consumer;

import fr.cea.ig.ngl.NGLApplication;
import models.laboratory.common.description.Institute;
import models.laboratory.sample.description.ImportCategory;
import models.laboratory.sample.description.ImportType;
import models.laboratory.sample.description.SampleCategory;
import models.laboratory.sample.description.SampleType;
import services.description.experiment.AbstractExperimentService;
import services.description.instrument.AbstractInstrumentService;
import services.description.process.AbstractProcessService;
import services.description.project.AbstractProjectService;
// import services.description.project.AbstractUmbrellaProjectService;
import services.description.run.AbstractRunService;
import services.description.run.AbstractTreatmentService;
import validation.ContextValidation;

public interface IApplicationData {
	
	List<Institute> getInstitutes();
	
	AbstractInstrumentService getInstrumentService();
	
	List<SampleCategory> getSampleCategories();
	List<SampleType> getSampleTypes();
	
	List<ImportCategory> getImportCategories();
	List<ImportType> getImportTypes();
	 
	AbstractExperimentService getExperimentService();
	AbstractProcessService    getProcessService();
	AbstractProjectService    getProjectService();
	//AbstractUmbrellaProjectService getUmbrellaProjectService();
	AbstractRunService        getRunService();

	AbstractTreatmentService getTreatmentService();
	Consumer<ContextValidation> getResolutionService();
	Consumer<ContextValidation> getProtocolService();
	
	Consumer<NGLApplication> getImportingCronStarter();
	Consumer<NGLApplication> getReportingCronStarter();
	Consumer<ContextValidation> getPrinterService();
	
}
