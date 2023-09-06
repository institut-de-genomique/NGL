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
	 
	/**
	 * Attention cette méthode ne sauvegarde que les nouveaux instruments déclarés dans 
	 * la classe InstrumentServiceCNS
	 * On ne supprime pas tous les instruments de la base avant de les re-créer => pas 
	 * de mise a jour d'instrument existants (notamment sur la notion de active false / true)
	 * @see  api/instruments/<code> pour faire les mise a jour
	 * 
	 * @param errors
	 */
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
		data.getExperimentService().main(errors);
	}

	public void saveProcessData(Map<String,List<ValidationError>> errors) {
		data.getProcessService().main(errors);
	}

	public void saveProjectData(Map<String,List<ValidationError>> errors) {
		data.getProjectService().main(errors);
		data.getUmbrellaProjectService().main(errors);
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
