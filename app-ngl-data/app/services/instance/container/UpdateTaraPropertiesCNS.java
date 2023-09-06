package services.instance.container;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.mongojack.DBQuery;

import com.mongodb.MongoException;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.ngl.NGLApplication;
import models.Constants;
import models.LimsCNSDAO;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.sample.instance.Sample;
import models.util.DataMappingCNS;
import models.utils.InstanceConstants;
import models.utils.dao.DAOException;
import rules.services.RulesException;
import services.instance.AbstractImportDataCNS;
import validation.ContextValidation;

public class UpdateTaraPropertiesCNS extends AbstractImportDataCNS{

	@Inject
	public UpdateTaraPropertiesCNS(NGLApplication app) {
		super("UpdateTara", app);
	}

//	@Override
//	public void runImport() throws SQLException, DAOException, MongoException, RulesException {
//		updateSampleFromTara(contextError, null);
//	}
	
	@Override
	public void runImport(ContextValidation contextError) throws SQLException, DAOException, MongoException, RulesException {
		updateSampleFromTara(contextError, null);
	}
	
	public void updateSampleFromTara(ContextValidation contextError, List<String> limsCodes) throws SQLException, DAOException {		
		List<Map<String, PropertyValue>> taraPropertyList = taraServices.findTaraSampleUpdated(limsCodes);
		for (Map<String,PropertyValue> taraProperties : taraPropertyList) {
			if (!taraProperties.containsKey(LimsCNSDAO.LIMS_CODE)) {
				contextError.addError(LimsCNSDAO.LIMS_CODE,"error.codeNotExist","");
			} else {
				Integer limsCode = Integer.valueOf(taraProperties.get(LimsCNSDAO.LIMS_CODE).value.toString());
				logger.debug("Tara lims Code : {}", limsCode);
				
				List<Sample> samples = MongoDBDAO.find(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, DBQuery.is("properties.limsCode.value",limsCode).notExists("life")).toList();
	
				if (samples.size() == 1) {
					Sample sample = samples.get(0);
	
					Boolean adaptater;
					if (sample.properties.get("isAdapters") == null) {
						adaptater = false;
					} else {
						adaptater = (Boolean) sample.properties.get("isAdapters").value;
					}
					
					String importTypeCode = DataMappingCNS.getImportTypeCode(true,adaptater);
					
					/* NEW ALGO */
					sample.properties.putAll(taraProperties);
					sample.importTypeCode = importTypeCode;
					sample.traceInformation.setTraceInformation(Constants.NGL_DATA_USER);
					contextError.setUpdateMode();
					sample.validate(contextError);
					if (!contextError.hasErrors()) {
						MongoDBDAO.update(InstanceConstants.SAMPLE_COLL_NAME, sample);
					}
				} 
			}
		}
	}

}
