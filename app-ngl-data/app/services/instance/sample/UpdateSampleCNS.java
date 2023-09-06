package services.instance.sample;

import java.sql.SQLException;
import java.util.List;

import javax.inject.Inject;

import com.mongodb.MongoException;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.ngl.NGLApplication;
import models.Constants;
import models.laboratory.sample.instance.Sample;
import models.utils.InstanceConstants;
import models.utils.dao.DAOException;
import rules.services.RulesException;
import validation.ContextValidation;

public class UpdateSampleCNS extends UpdateSamplePropertiesCNS {

	@Inject
	public UpdateSampleCNS(NGLApplication app) {
		super("UpdateSample", app);
	}

//	@Override
//	public void runImport() throws SQLException, DAOException, MongoException, RulesException {
//		updateSampleFromTara(contextError, null);
//	}
	
	@Override
	public void runImport(ContextValidation contextError) throws SQLException, DAOException, MongoException, RulesException {
		updateSampleFromTara(contextError, null);
	}

	public void updateSampleFromTara(ContextValidation contextError, List<String> sampleCodes) throws SQLException, DAOException {

		List<String> results = limsServices.findSampleUpdated(sampleCodes);

		for (String sampleCode : results) {
			try {
				Sample sample = limsServices.findSampleToCreate(contextError, sampleCode);
				ContextValidation contextValidation = ContextValidation.createUndefinedContext(Constants.NGL_DATA_USER);
				
				Sample dbSample = MongoDBDAO.findByCode(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, sample.code);
				if (dbSample != null) {
					logger.info("update sample "+dbSample.code);
					updateSample(contextValidation, dbSample, sample);
					dbSample.validate(contextValidation);
					if(!contextValidation.hasErrors()){
						MongoDBDAO.save(InstanceConstants.SAMPLE_COLL_NAME,dbSample);
						super.updateOneSample(dbSample, contextError);
						limsServices.updateMaterielLims(dbSample, contextError);						
					}
				} else {
					contextError.getErrors().putAll(contextValidation.getErrors());
				}
			} catch(Exception e){
				logger.error("Problème to update sample "+sampleCode+" "+e.getMessage());
			}
		}
	}

	private void updateSample(ContextValidation contextValidation, Sample dbSample, Sample sample) {
		dbSample.traceInformation = sample.traceInformation; //use to keep the creation date
		dbSample.setTraceModificationStamp(contextValidation, contextValidation.getUser());
		dbSample.properties = sample.properties;
		dbSample.comments = sample.comments;
		dbSample.referenceCollab = sample.referenceCollab;

		if (!dbSample.taxonCode.equals(sample.taxonCode)) {
			dbSample.taxonCode = sample.taxonCode;
			dbSample.ncbiLineage = null;
			dbSample.ncbiScientificName = null;
		}
	}

}
