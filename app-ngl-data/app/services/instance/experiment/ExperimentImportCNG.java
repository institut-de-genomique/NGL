package services.instance.experiment;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.ngl.NGLApplication;
import models.laboratory.experiment.instance.Experiment;
import models.utils.InstanceConstants;
import models.utils.InstanceHelpers;
import models.utils.dao.DAOException;
import services.instance.AbstractImportDataCNG;
import validation.ContextValidation;

public class ExperimentImportCNG extends AbstractImportDataCNG {
	
	@Inject
	public ExperimentImportCNG(NGLApplication app) {
		super("ExperimentImportCNG", app);
	}

//	@Override
//	public void runImport() throws SQLException, DAOException {		
//		updateExperimentDepots();
//	}
	
	@Override
	public void runImport(ContextValidation contextError) throws SQLException, DAOException {		
		updateExperimentDepots(contextError);
	}
	
	public void updateExperimentDepots(ContextValidation contextError) throws SQLException, DAOException {
		logger.debug("start loading experiments of type 'depot'");
		
		List<Experiment> experiments = limsServices.findIlluminaDepotExperiment(contextError, "sop_depot_1");		
		List<Experiment> existingExperiments=MongoDBDAO.find(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class).toList();
		
		List<Experiment> experimentsToUpdate = new ArrayList<>();
		for (Experiment experiment : experiments) {
			if (existingExperiments.contains(experiment)) {
				experimentsToUpdate.add(experiment);
			}
		}
		
		List<Experiment> experimentsToCreate = new ArrayList<>();
		for (Experiment experiment : experiments) {
			if (!experimentsToUpdate.contains(experiment)) {
				experimentsToCreate.add(experiment);
			}
		}
		
		for (Experiment experiment : experimentsToUpdate) {
			MongoDBDAO.deleteByCode(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, experiment.code);
		}
		List<Experiment> expsU=InstanceHelpers.save(InstanceConstants.EXPERIMENT_COLL_NAME, experimentsToUpdate, contextError, true);
		
		List<Experiment> expsC=InstanceHelpers.save(InstanceConstants.EXPERIMENT_COLL_NAME, experimentsToCreate, contextError, true);
			
		limsServices.updateLimsDepotExperiment(expsU, contextError, "update");
		limsServices.updateLimsDepotExperiment(expsC, contextError, "creation");
		
		logger.debug("end loading experiments of type 'depot'");
	}

}
