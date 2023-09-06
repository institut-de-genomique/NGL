package services.instance.sample;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.inject.Inject;

import org.mongojack.DBQuery;

import com.mongodb.MongoException;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.MongoDBResult;
import fr.cea.ig.ngl.NGLApplication;
import models.laboratory.common.description.Level;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.sample.description.ImportType;
import models.laboratory.sample.description.SampleType;
import models.laboratory.sample.instance.Sample;
import models.utils.InstanceConstants;
import models.utils.InstanceHelpers;
import models.utils.dao.DAOException;
import rules.services.RulesException;
import services.instance.AbstractImportDataCNS;
import validation.ContextValidation;
import validation.common.instance.CommonValidationHelper;

public class UpdateSamplePropertiesCNS extends AbstractImportDataCNS {

	@Inject
	public UpdateSamplePropertiesCNS(NGLApplication app) {
		this("UpdatePropertiesSampleCNS", app);
	}

	public UpdateSamplePropertiesCNS(String string, NGLApplication app) {
		super(string, app);
	}

	//	@Override
	//	public void runImport() throws SQLException, DAOException, MongoException, RulesException {
	//		// Récupère tous les samples modifiés les dernieres 24h
	//		updateSampleModifySince(-1,contextError);
	//	}

	@Override
	public void runImport(ContextValidation contextError) throws SQLException, DAOException, MongoException, RulesException {
		// Récupère tous les samples modifiés les dernieres 24h
		updateSampleModifySince(-1,contextError);
	}

	private void updateSampleModifySince(int nbDays, ContextValidation contextError) {
		//		Integer skip = 0;
		int skip = 0;
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_YEAR, nbDays);
		Date date =  calendar.getTime();

		MongoDBResult<Sample> result = MongoDBDAO.find(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, DBQuery.greaterThanEquals("traceInformation.modifyDate", date).notExists("life"));
		Integer nbResult = result.count(); 
		logger.info("nbResult : "+nbResult);
		while (skip < nbResult) {
			try {
				long t1 = System.currentTimeMillis();
				List<Sample> cursor = MongoDBDAO.find(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, DBQuery.greaterThanEquals("traceInformation.modifyDate", date).notExists("life"))
						.sort("code").skip(skip).limit(1000)
						.toList();

				cursor.forEach(sample -> {
					if (!sample.projectCodes.contains("CEA")) {
						try {
							updateOneSample(sample,contextError);
						} catch (Throwable e) {
							logger.error("Sample : "+sample.code+" - "+e,e);
							if (e.getMessage() != null)
								contextError.addError(sample.code, e.getMessage());
							else
								contextError.addError(sample.code, "null");
						}
					} 
				});
				skip = skip + 1000;
				long t2 = System.currentTimeMillis();
				logger.debug("time "+skip+" - "+((t2-t1)/1000));
			} catch(Throwable e) {
				logger.error("Error : "+e,e);
				if (e.getMessage() != null)
					contextError.addError("Error", e.getMessage());
				else
					contextError.addError("Error", "null");
			}
		}
		logger.info("end of UpdateSamplePropertiesCNS" );
	}

	public void updateOneSample(Sample sample, ContextValidation contextError) {

		logger.debug("Update sample {}", sample.code);

		Map<String, PropertyValue> updatedProperties = new HashMap<>(); // String, PropertyValue>();
		Set<String> deletedPropertyCodes = new TreeSet<>();
		//		SampleType sampleType = BusinessValidationHelper.validateExistDescriptionCode(null, sample.typeCode, "typeCode", SampleType.find.get(),true);
		//		ImportType importType = BusinessValidationHelper.validateExistDescriptionCode(null, sample.importTypeCode, "importTypeCode", ImportType.find.get(),true);
		// FIXME: why a null validation context instead of contextError ?
		SampleType sampleType = CommonValidationHelper.validateCodeForeignOptional(null, SampleType.miniFind.get(), sample.typeCode, "typeCode",true);
		ImportType importType = CommonValidationHelper.validateCodeForeignOptional(null, ImportType.miniFind.get(), sample.importTypeCode, "importTypeCode", true);

		if (importType != null) {
			InstanceHelpers.copyPropertyValueFromPropertiesDefinition(importType.getPropertyDefinitionByLevel(Level.CODE.Sample, Level.CODE.Content), sample.properties,updatedProperties);
			deletedPropertyCodes.addAll(InstanceHelpers.getDeletedPropertyDefinitionCode(importType.getPropertyDefinitionByLevel(Level.CODE.Sample, Level.CODE.Content), sample.properties));
		}
		if (sampleType != null) {
			InstanceHelpers.copyPropertyValueFromPropertiesDefinition(sampleType.getPropertyDefinitionByLevel(Level.CODE.Sample, Level.CODE.Content), sample.properties,updatedProperties);
			deletedPropertyCodes.addAll(InstanceHelpers.getDeletedPropertyDefinitionCode(sampleType.getPropertyDefinitionByLevel(Level.CODE.Sample, Level.CODE.Content), sample.properties));
		}
		logger.warn("property will be deleted " + deletedPropertyCodes);
		updateCollectionsFromSample(sample, updatedProperties, deletedPropertyCodes,contextError);
	} 

	private void updateCollectionsFromSample(Sample sample, Map<String, PropertyValue> updatedProperties, Set<String> deletedPropertyCodes, ContextValidation contextError) {
		logger.info("Update son samples, containers, readSets, process from sample : {}", sample.code);
		InstanceHelpers.updateContentProperties(sample, updatedProperties, deletedPropertyCodes, contextError);
		// Updates on samples
		List<Sample> samples = MongoDBDAO.find(InstanceConstants.SAMPLE_COLL_NAME, Sample.class,
				DBQuery.is("life.from.sampleCode",sample.code).in("life.from.projectCode", sample.projectCodes)).toList();
		//BUG timeout cursor

		//	MongoDBDAO.find(InstanceConstants.SAMPLE_COLL_NAME, Sample.class,
		//			DBQuery.is("life.from.sampleCode",sample.code).in("life.from.projectCode", sample.projectCodes))
		//			.cursor
		samples.forEach(sonSample -> {
			updateCollectionsFromSample(sonSample, updatedProperties, deletedPropertyCodes, contextError);
		});

	}

}
