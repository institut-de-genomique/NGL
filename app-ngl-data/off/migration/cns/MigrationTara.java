package controllers.migration.cns;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import models.Constants;
import models.LimsCNSDAO;
import models.TaraDAO;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.container.instance.Container;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.sample.description.ImportType;
import models.laboratory.sample.instance.Sample;
import models.util.DataMappingCNS;
import models.utils.InstanceConstants;
import models.utils.instance.SampleHelper;

import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import play.Logger;
import play.Logger.ALogger;
import play.api.modules.spring.Spring;
import play.mvc.Result;
import services.instance.sample.UpdateSampleCNS;
import validation.ContextValidation;
import validation.utils.BusinessValidationHelper;
import validation.utils.ValidationHelper;

import com.mongodb.BasicDBObject;

import controllers.migration.AbstractMigration;
import fr.cea.ig.MongoDBDAO;

public class MigrationTara extends AbstractMigration {
	
	static ALogger logger=Logger.of("MigrationTara");
	protected static TaraDAO  taraServices = Spring.getBeanOfType(TaraDAO.class);
	protected static LimsCNSDAO  limsServices = Spring.getBeanOfType(LimsCNSDAO.class);
	
	public static Result migration() {
		
		backupOneCollection(InstanceConstants.SAMPLE_COLL_NAME,Sample.class);
		backupOneCollection(InstanceConstants.CONTAINER_COLL_NAME,Container.class);
		
		BasicDBObject keys = new BasicDBObject();
		keys.put("code", 1);
		keys.put("sampleOnContainer", 1);
		backupOneCollection(InstanceConstants.READSET_ILLUMINA_COLL_NAME,ReadSet.class,keys);

		
		ContextValidation contextValidation=new ContextValidation(Constants.NGL_DATA_USER);
		Boolean adaptater;
		List<Sample> samples=MongoDBDAO.find(InstanceConstants.SAMPLE_COLL_NAME,Sample.class,DBQuery.exists("properties.taraStation").notExists("properties.taraDepthCode")).toList();
		Logger.debug("Nb de samples Tara :"+samples.size());
		for(Sample sample:samples){
			Logger.debug("Sample :"+sample.code);
			if(!sample.importTypeCode.contains("tara")){
				if( sample.properties.containsKey("isAdapters") && Boolean.valueOf(sample.properties.get("isAdapters").toString())){
					adaptater=true;
				}else {adaptater=false;}
				sample.importTypeCode=DataMappingCNS.getImportTypeCode(true,adaptater);
				MongoDBDAO.update(InstanceConstants.SAMPLE_COLL_NAME, Sample.class,DBQuery.is("code",sample.code),DBUpdate.set("importTypeCode",sample.importTypeCode));
			}
			Map<String, PropertyValue> taraProperties=taraServices.findTaraSampleFromLimsCode(Integer.valueOf(sample.properties.get("limsCode").value.toString()), contextValidation);
			ImportType importType=BusinessValidationHelper.validateRequiredDescriptionCode(contextValidation, sample.importTypeCode,"importTypeCode", ImportType.find,true);
			if(sample.typeCode!=null && importType!=null){
				ValidationHelper.validateProperties(contextValidation,taraProperties, importType.getPropertiesDefinitionSampleLevel());
			}
			//SampleHelper.updateSampleProperties(sample.code, taraProperties, contextValidation);
		}
		
		contextValidation.displayErrors(logger);
		if(contextValidation.hasErrors()){
			return badRequest("Error Migration Tara");
		}else {
			return ok("End migration Tara");
		}
	}

	
	public static Result deleteSampleNotInLims() {
		ContextValidation contextValidation=new ContextValidation(Constants.NGL_DATA_USER);
		List<String> sampleCodesToUpdate = new ArrayList<String>();
		BasicDBObject keys=new BasicDBObject();
		keys.put("code",1);
		keys.put("properties.limsCode.value",1);
		List<Sample> samples=MongoDBDAO.find(InstanceConstants.SAMPLE_COLL_NAME,Sample.class,DBQuery.exists("properties.taraStation").notExists("properties.taraDepthCode"),keys).toList();

		for(Sample sample:samples){
			Integer count=limsServices.jdbcTemplate.queryForObject("select count(*) from Adnmateriel where adnco= ?", Integer.class,  new Object[]{Integer.valueOf(sample.properties.get("limsCode").value.toString())} );
			if(count.equals(0)){
				Logger.debug("Sample To delete "+sample.code);
				if(!SampleHelper.deleteSample(sample.code,contextValidation)){
					Logger.debug("Sample Not delete but Update "+sample.code);
					sampleCodesToUpdate.add(sample.code);
				}
			}
			
		}
		
		if(sampleCodesToUpdate.size()>0){
			try {
				//UpdateSampleCNS.updateSampleFromTara(contextValidation, sampleCodesToUpdate);
			} catch (Exception e) {
				e.getMessage();
				return badRequest("Error Delete Sample Not in Lims");
			}
		}
		contextValidation.displayErrors(logger);
		return ok("End sample to delete");
	}

	
}
