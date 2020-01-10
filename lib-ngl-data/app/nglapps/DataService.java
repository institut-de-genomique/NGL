package nglapps;

import java.util.List;
import java.util.Map;

import com.google.inject.Inject;

import models.laboratory.common.description.Institute;
//import models.laboratory.experiment.description.ExperimentCategory;
//import models.laboratory.experiment.description.ExperimentType;
//import models.laboratory.experiment.description.ProtocolCategory;
//import models.laboratory.instrument.description.InstrumentCategory;
//import models.laboratory.instrument.description.InstrumentUsedType;
//import models.laboratory.processes.description.ExperimentTypeNode;
//import models.laboratory.processes.description.ProcessType;
//import models.laboratory.processes.description.dao.ExperimentTypeNodeDAO;
import models.laboratory.sample.description.ImportCategory;
import models.laboratory.sample.description.ImportType;
import models.laboratory.sample.description.SampleCategory;
import models.laboratory.sample.description.SampleType;
import models.utils.ModelDAOs;
//import models.utils.dao.DAOHelpers;
//import play.api.modules.spring.Spring;
import play.data.validation.ValidationError;
import validation.ContextValidation;

public class DataService {
	
	private final ModelDAOs        mdao;
	private final IApplicationData data;
	
	@Inject
	public DataService(ModelDAOs mdao, IApplicationData data) {
		this.mdao = mdao;
		this.data = data;
	}
	
	public void saveInstitutes(Map<String,List<ValidationError>> errors) {
		mdao.removeAll(Institute.class);
		mdao.saveModels(Institute.class, data.getInstitutes(), errors);
	}
	 
	public void saveInstrumentData(Map<String,List<ValidationError>> errors) {
		// There is a dependency from instruments that prevent the table clearing.
//		mdao.removeAll(InstrumentUsedType.class);
//		mdao.removeAll(InstrumentCategory.class);
//		mdao.saveModels(InstrumentCategory.class, data.getInstrumentCategories(), errors);	
//		mdao.saveModels(InstrumentUsedType.class, data.getInstrumentUsedTypes(),  errors);
		data.getInstrumentService().main(errors);
	}
	
	public void saveSampleData(Map<String,List<ValidationError>> errors) {
		mdao.removeAll(SampleType    .class);
		mdao.removeAll(SampleCategory.class);
		mdao.saveModels(SampleCategory.class, data.getSampleCategories(), errors);
		mdao.saveModels(SampleType    .class, data.getSampleTypes(), errors);
	}
	
	public void saveImportData(Map<String,List<ValidationError>> errors) {
		mdao.removeAll(ImportType    .class);
		mdao.removeAll(ImportCategory.class);
		mdao.saveModels(ImportCategory.class, data.getImportCategories(), errors);
		mdao.saveModels(ImportType    .class, data.getImportTypes(),      errors);
	}
	
	public void saveExperimentData(Map<String,List<ValidationError>> errors) {
//		// Cleaned but not rebuilt...
//		mdao.removeAll(ProcessType.class);
//		//	remove all previous before delete ExpTypeNode
//		Spring.getBeanOfType(ExperimentTypeNodeDAO.class).removeAllPrevious();
//		mdao.removeAll(ExperimentTypeNode.class);
//		mdao.removeAll(ExperimentType.class);
//		mdao.removeAll(ExperimentCategory.class);
//
//		// Not cleaned but built
//		mdao.saveModels(ProtocolCategory  .class, data.getProtocolCategories(),   errors);
//		mdao.saveModels(ExperimentCategory.class, data.getExperimentCategories(), errors);
//		mdao.saveModels(ExperimentType    .class, data.getExperimentTypes(),      errors);
//		mdao.saveModels(ExperimentTypeNode.class, data.getExperimentTypeNodes(),  errors);
		data.getExperimentService().main(errors);
	}

	public void saveProcessData(Map<String,List<ValidationError>> errors) {
		data.getProcessService().main(errors);
	}

	public void saveProjectData(Map<String,List<ValidationError>> errors) {
		data.getProjectService().main(errors);
	}

	public void saveRunData(Map<String,List<ValidationError>> errors) {
		data.getRunService().main(errors);
	}

	public void saveTreatmentData(Map<String,List<ValidationError>> errors) {
		data.getTreatmentService().main(errors);
	}
	
	public void saveResolutionData(ContextValidation ctx) {
		data.getResolutionService().accept(ctx);
	}
	
	public void saveProtocolData(ContextValidation ctx) {
		data.getProtocolService().accept(ctx);
	}
	
}
