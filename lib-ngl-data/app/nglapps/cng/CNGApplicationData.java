package nglapps.cng;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import fr.cea.ig.ngl.NGLApplication;
import models.laboratory.common.description.Institute;
import models.laboratory.sample.description.ImportCategory;
import models.laboratory.sample.description.ImportType;
import models.laboratory.sample.description.SampleCategory;
import models.laboratory.sample.description.SampleType;
import nglapps.IApplicationData;
import services.description.experiment.AbstractExperimentService;
import services.description.experiment.ExperimentServiceCNG;
import services.description.instrument.InstrumentServiceCNG;
import services.description.process.AbstractProcessService;
import services.description.process.ProcessServiceCNG;
import services.description.project.AbstractProjectService;
// import services.description.project.AbstractUmbrellaProjectService;
import services.description.project.ProjectServiceCNG;
import services.description.run.AbstractRunService;
import services.description.run.AbstractTreatmentService;
import services.description.run.RunServiceCNG;
import services.description.run.TreatmentServiceCNG;
import services.description.sample.ImportServiceCNG;
import services.description.sample.SampleServiceCNG;
import validation.ContextValidation;

public class CNGApplicationData implements IApplicationData {

	@Override
	public List<Institute> getInstitutes() {
//		return Arrays.asList(newInstitute("Centre National de Génomique","CNG"));
		return Arrays.asList(new Institute("CNG", "Centre National de Génomique"));
	}

//	@Override
//	public List<InstrumentCategory> getInstrumentCategories() {
//		return new InstrumentServiceCNG().getInstrumentCategories();
//	}
//
//	@Override
//	public List<InstrumentUsedType> getInstrumentUsedTypes() {
//		return new InstrumentServiceCNG().getInstrumentUsedTypes();
//	}
	
	@Override
	public InstrumentServiceCNG getInstrumentService() {
		return new InstrumentServiceCNG();
	}
	
	@Override
	public List<SampleCategory> getSampleCategories() {
		return new SampleServiceCNG().getSampleCategories();
	}

	@Override
	public List<SampleType> getSampleTypes() {
		return new SampleServiceCNG().getSampleTypes();
	}

	@Override
	public List<ImportCategory> getImportCategories() {
		return new ImportServiceCNG().getImportCategories();
	}

	@Override
	public List<ImportType> getImportTypes() {
		return new ImportServiceCNG().getImportTypes();
	}

	@Override
	public AbstractExperimentService getExperimentService() {
		return new ExperimentServiceCNG();
	}

	@Override
	public AbstractProcessService getProcessService() {
		return new ProcessServiceCNG();
	}

	@Override
	public AbstractProjectService getProjectService() {
		return new ProjectServiceCNG();
	}

	@Override
	public AbstractRunService getRunService() {
		return new RunServiceCNG();
	}

	@Override
	public AbstractTreatmentService getTreatmentService() {
		return new TreatmentServiceCNG();
	}

	@Override
	public Consumer<ContextValidation> getResolutionService() {
	    throw new RuntimeException("not implemented");
//		return ResolutionService::saveResolutionsCNG;
	}

	@Override
	public Consumer<ContextValidation> getProtocolService() {
	    throw new RuntimeException("not implemented");
//		return ProtocolServiceCNG::main;
	}

	@Override
	public Consumer<NGLApplication> getImportingCronStarter() {
	    throw new RuntimeException("not implemented");
//		return ctx -> new ImportDataCNG(ctx).run();
	}

	@Override
	public Consumer<NGLApplication> getReportingCronStarter() {
		return ctx -> {};
	}

	@Override
	public Consumer<ContextValidation> getPrinterService() {
		return ctx -> {};
	}

	// @Override
	// public AbstractUmbrellaProjectService getUmbrellaProjectService() {
	// 	throw new RuntimeException("not implemented");
	// }
	
}
