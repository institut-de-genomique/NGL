package nglapps.cns;

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
import services.description.experiment.ExperimentServiceCNS;
import services.description.instrument.InstrumentServiceCNS;
import services.description.process.AbstractProcessService;
import services.description.process.ProcessServiceCNS;
import services.description.project.AbstractProjectService;
// import services.description.project.AbstractUmbrellaProjectService;
import services.description.project.ProjectServiceCNS;
// import services.description.project.UmbrellaProjectServiceCNS;
import services.description.run.AbstractRunService;
import services.description.run.AbstractTreatmentService;
import services.description.run.RunServiceCNS;
import services.description.run.TreatmentServiceCNS;
import services.description.sample.ImportServiceCNS;
import services.description.sample.SampleServiceCNS;
import validation.ContextValidation;

public class CNSApplicationData implements IApplicationData {

	@Override
	public List<Institute> getInstitutes() {
//		return Arrays.asList(newInstitute("Centre National de Séquençage","CNS"));
		return Arrays.asList(new Institute("CNS","Centre National de Séquençage"));
	}

//	@Override
//	public List<InstrumentCategory> getInstrumentCategories() {
//		return new InstrumentServiceCNS().getInstrumentCategories();
//	}
//
//	@Override
//	public List<InstrumentUsedType> getInstrumentUsedTypes() {
//		return new InstrumentServiceCNS().getInstrumentUsedTypes();
//	}
	
	@Override
	public InstrumentServiceCNS getInstrumentService() {
		return new InstrumentServiceCNS();
	}
	
	@Override
	public List<SampleCategory> getSampleCategories() {
		return new SampleServiceCNS().getSampleCategories();
	}

	@Override
	public List<SampleType> getSampleTypes() {
		return new SampleServiceCNS().getSampleTypes();
	}

	@Override
	public List<ImportCategory> getImportCategories() {
		return new ImportServiceCNS().getImportCategories();
	}

	@Override
	public List<ImportType> getImportTypes() {
		return new ImportServiceCNS().getImportTypes();
	}

	@Override
	public AbstractExperimentService getExperimentService() {
		return new ExperimentServiceCNS();
	}

	@Override
	public AbstractProcessService getProcessService() {
		return new ProcessServiceCNS();
	}

	@Override
	public AbstractProjectService getProjectService() {
		return new ProjectServiceCNS();
	}

	@Override
	public AbstractRunService getRunService() {
		return new RunServiceCNS();
	}

	@Override
	public AbstractTreatmentService getTreatmentService() {
		return new TreatmentServiceCNS();
	}

	@Override
	public Consumer<ContextValidation> getResolutionService() {
//		return ResolutionService::saveResolutionsCNS;
	    throw new RuntimeException("not implemented");
	}

	@Override
	public Consumer<ContextValidation> getProtocolService() {
//		return ProtocolServiceCNS::main;
	    throw new RuntimeException("not implemented");
	}

	@Override
	public Consumer<NGLApplication> getImportingCronStarter() {
	    throw new RuntimeException("not implemented");
//		return app -> new ImportDataCNS(app).run();
	}

	@Override
	public Consumer<NGLApplication> getReportingCronStarter() {
	    throw new RuntimeException("not implemented");
//		return app -> new ReportingCNS(app)
//							.startScheduling(Duration.create(ImportDataUtil.nextExecutionInSeconds(8, 0),TimeUnit.SECONDS),
//											 Duration.create(1, TimeUnit.DAYS));
	}

	@Override
	public Consumer<ContextValidation> getPrinterService() {
	    throw new RuntimeException("not implemented");
//		return ctx -> PrinterCNS.main(ctx); 
	}

	// @Override
	// public AbstractUmbrellaProjectService getUmbrellaProjectService() {
	// 	return new UmbrellaProjectServiceCNS();
	// }

}
