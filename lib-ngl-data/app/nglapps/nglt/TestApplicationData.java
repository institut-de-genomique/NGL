package nglapps.nglt;

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
import services.description.project.AbstractUmbrellaProjectService;
import services.description.project.ProjectServiceCNS;
import services.description.run.AbstractRunService;
import services.description.run.AbstractTreatmentService;
import services.description.run.RunServiceCNS;
import services.description.run.TreatmentServiceCNS;
import services.description.sample.ImportServiceCNS;
import services.description.sample.SampleServiceCNS;
import validation.ContextValidation;

// FIXME temporary use CNS description for unit tests 
// need to have a description env for testing purpose 
public class TestApplicationData implements IApplicationData {

	@Override
	public List<Institute> getInstitutes() {
		//return Arrays.asList(new Institute("TEST", "Test"));
		return Arrays.asList(new Institute("CNS","Centre National de Séquençage"));
	}

//	@Override
//	public List<InstrumentCategory> getInstrumentCategories() {
//		return new InstrumentServiceTEST().getInstrumentCategories();
//	}
//
//	@Override
//	public List<InstrumentUsedType> getInstrumentUsedTypes() {
//		return new InstrumentServiceTEST().getInstrumentUsedTypes();
//	}

	// CNS ---------------------------------------------------
	
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
    
    //TEST ---------------------------------------------------
    
//	@Override
//	public InstrumentServiceTEST getInstrumentService() {
//		return new InstrumentServiceTEST();
//	}
	
//	@Override
//	public List<SampleCategory> getSampleCategories() {
//		return new SampleServiceTEST().getSampleCategories();
//	}

//	@Override
//	public List<SampleType> getSampleTypes() {
//		return new SampleServiceTEST().getSampleTypes();
//	}

//	@Override
//	public List<ImportCategory> getImportCategories() {
//		throw new RuntimeException("not implemented");
//	}

//	@Override
//	public List<ImportType> getImportTypes() {
//		throw new RuntimeException("not implemented");
//	}

//	@Override
//	public AbstractExperimentService getExperimentService() {
//		return new ExperimentServiceTEST();
//	}

//	@Override
//	public AbstractRunService getRunService() {
//		throw new RuntimeException("not implemented");
//	}

//	@Override
//	public AbstractTreatmentService getTreatmentService() {
//		throw new RuntimeException("not implemented");		
//	}

    // ---------------------------------------------------    
    
    
	@Override
	public Consumer<ContextValidation> getResolutionService() {
		throw new RuntimeException("not implemented");		
	}

	@Override
	public Consumer<ContextValidation> getProtocolService() {
		throw new RuntimeException("not implemented");		
	}

	@Override
	public Consumer<NGLApplication> getImportingCronStarter() {
		throw new RuntimeException("not implemented");		
	}

	@Override
	public Consumer<NGLApplication> getReportingCronStarter() {
		throw new RuntimeException("not implemented");		
	}

	@Override
	public Consumer<ContextValidation> getPrinterService() {
		throw new RuntimeException("not implemented");		
	}

    @Override
    public AbstractUmbrellaProjectService getUmbrellaProjectService() {
       throw new RuntimeException("not implemented");		
    }

}
