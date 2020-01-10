package controllers.migration.cns;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.Content;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.processes.instance.Process;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.sample.instance.Sample;
import models.utils.InstanceConstants;

import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;
import org.mvel2.ast.Instance;

import com.mongodb.BasicDBObject;

import play.Logger;
import play.mvc.Result;
import controllers.CommonController;

import fr.cea.ig.MongoDBDAO;

public class MigrationSampleType extends CommonController {

	public static Result migration() {

		Logger.info(">>>>>>>>>>> Migration NGL in Container, Sample, ReadSet and Process starts");

		//MigrationNGLSEQ.backupOneCollection(InstanceConstants.CONTAINER_COLL_NAME,Container.class);
		updateRefecollabAndTaxonSizeInContainer();
		
		/*BasicDBObject keys=new BasicDBObject();
		keys.put("_id",0 );
		keys.put("code", 1);
		keys.put("sampleOnContainer", 1);
		
		MigrationNGLSEQ.backupOneCollection(InstanceConstants.READSET_ILLUMINA_COLL_NAME,ReadSet.class,keys);
		MigrationNGLSEQ.backupOneCollection(InstanceConstants.SAMPLE_COLL_NAME,Sample.class);
		updateSampleTypeDefault();
		*/
		return ok("Migration Finish");
	}

	private static void updateSampleTypeDefault() {
		//Update sampleTypeCode in ReadSet and Sample
		
		MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class,
				DBQuery.is("sampleOnContainer.sampleTypeCode","default-sample-cns").notIn("projectCode", "AMP","AHG"),
				DBUpdate.set("sampleOnContainer.sampleTypeCode","unknown").set("categoryCode", "unknown"));
		
		MongoDBDAO.update(InstanceConstants.SAMPLE_COLL_NAME, Sample.class
				,DBQuery.is("typeCode","default-sample-cns").notIn("projectCodes", "AMP","AHG")
				,DBUpdate.set("typeCode", "unknown").set("categoryCode", "unknown"));
		
		MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class,
				DBQuery.is("sampleOnContainer.sampleTypeCode","default-sample-cns").in("projectCode", "AMP","AHG"),
				DBUpdate.set("sampleOnContainer.sampleTypeCode","amplicon").set("categoryCode", "amplicon"));
		
		MongoDBDAO.update(InstanceConstants.SAMPLE_COLL_NAME, Sample.class
				,DBQuery.is("typeCode","default-sample-cns").in("projectCodes", "AMP","AHG")
				,DBUpdate.set("typeCode", "amplicon").set("categoryCode", "amplicon"));
		
		MongoDBDAO.update(InstanceConstants.SAMPLE_COLL_NAME, Sample.class
				,DBQuery.exists("properties.taxonSize")
				,DBUpdate.set("properties.taxonSize.unit", "pb"));
	}

	private static void updateRefecollabAndTaxonSizeInContainer() {

		List<Container> containers = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class,DBQuery.is("contents.properties.taxonSize.unit",null)).toList();
		//List<Sample> samples =  MongoDBDAO.find(InstanceConstants.SAMPLE_COLL_NAME, Sample.class).toList();
		
		Logger.debug("Nb de containers to update : "+containers.size());
		
		/*Map<String,String> sampleRefCollab=new HashMap<String, String>();
		for(Sample sample:samples){
			
			sampleRefCollab.put(sample.code, sample.referenceCollab);
		}*/
		
		for(Container container:containers){
		
			for(Content content:container.contents){
			
			/*	content.referenceCollab=sampleRefCollab.get(content.sampleCode);
				
				if(content.sampleTypeCode.equals("default-sample-cns")){
					if(container.projectCodes.contains("AMP") || container.projectCodes.contains("AHG")){
						content.sampleTypeCode="amplicon";
						content.sampleCategoryCode="amplicon";
					}
					else {
						content.sampleTypeCode="unknown";
						content.sampleCategoryCode="unknown";
					}
				}
			*/
			
				 ((PropertySingleValue) content.properties.get("taxonSize")).unit="pb";
			}
			
			MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME, Container.class, DBQuery.is("code",container.code), DBUpdate.set("contents",container.contents));
			
			
		}
		
	}


}
