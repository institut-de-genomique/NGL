package nglapps.cng;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import javax.inject.Inject;

import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.NGLConfig;
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
import services.instance.ImportDataCNG;
import services.instance.protocol.ProtocolServiceCNG;
import services.instance.resolution.ResolutionService;
import validation.ContextValidation;

public class CNGApplicationData implements IApplicationData {

	private NGLConfig config;

	@Inject
	public CNGApplicationData(NGLConfig config) {
		this.config = config;
	}
	
	@Override
	public List<Institute> getInstitutes() {
		return Arrays.asList(new Institute("CNG", "Centre National de Génomique"));
	}
	
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
		return ResolutionService::saveResolutionsCNG;
	}

	@Override
	public Consumer<ContextValidation> getProtocolService() {
		return ProtocolServiceCNG::main;
	}

	@Override
	public Consumer<NGLApplication> getImportingCronStarter() {
		return ctx -> new ImportDataCNG(ctx, config).run();
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
	// 	return null;
	// }
	
}
