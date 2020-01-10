package controllers.migration.cns;

import java.util.List;
import java.util.Set;

import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;
import org.mongojack.JacksonDBCollection;

import controllers.CommonController;
import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.MongoDBResult;
import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.Content;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.run.instance.SampleOnContainer;
import models.laboratory.sample.instance.Sample;
import models.utils.InstanceConstants;
import models.utils.InstanceHelpers;
import play.Logger;
import play.mvc.Result;

public class UpdateSampleTypeCodeToContainer  extends CommonController {

	
	private static final String INSTANCE_BCK = InstanceConstants.CONTAINER_COLL_NAME+"_BCK_20140812";
	private static final String READSET_ILLUMINA_BCK = InstanceConstants.READSET_ILLUMINA_COLL_NAME+"_BCK_20140812";

	public static Result migrationContainer(){
		
		JacksonDBCollection<Container, String> contBck = MongoDBDAO.getCollection(INSTANCE_BCK, Container.class);
		if(contBck.count() == 0){
			backupContainer();
		
			List<Container> containers = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class, 
					DBQuery.notExists("contents.sampleTypeCode")).toList();
			
			Logger.info("nb containers ="+containers.size());
			int i = 0;
			for(Container c : containers){
				i++;
				List<Content> contents = c.contents;
				
				for(Content content:contents){
					if(content.sampleCode != null && (content.sampleTypeCode == null || content.sampleCategoryCode == null)){
						//Logger.info("Content must be updated : "+content.sampleCode);
						Sample sample =  MongoDBDAO.findByCode(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, content.sampleCode);
						
						content.sampleCategoryCode = sample.categoryCode;
						content.sampleTypeCode = sample.typeCode;
						
					}else if(content.sampleCode == null){
						Logger.error("sampleCode null : "+c.code);
					}
				}
				
				MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME,  Container.class, DBQuery.is("code", c.code), 
						DBUpdate.set("contents", contents));
				if((i % 100) == 0){
					Logger.info("Save i = "+i);
				}
			}
		}else{
			Logger.info("Migration container support already execute !");
		}
			
		Logger.info("Migration finish");
		return ok("Migration Finish");
	}

	private static void backupContainer() {
		Logger.info("\tCopie "+InstanceConstants.CONTAINER_COLL_NAME+" start");		
		MongoDBDAO.save(INSTANCE_BCK, MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class).toList());
		Logger.info("\tCopie "+InstanceConstants.CONTAINER_COLL_NAME+" end");	
	}

	
	public static Result migrationReadset(){
		
		JacksonDBCollection<ReadSet, String> contBck = MongoDBDAO.getCollection(READSET_ILLUMINA_BCK, ReadSet.class);
		if(contBck.count() == 0){
			backupReadSet();
			
			List<ReadSet> readSets = MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, 
					DBQuery.notExists("sampleOnContainer.sampleTypeCode")).toList();
			
			Logger.info("nb readsets ="+readSets.size());
			int i = 0;
			for(ReadSet readSet : readSets){
				i++;
				migreReadSet(readSet);
				if((i % 1000) == 0){
					Logger.info("Save i = "+i);
				}
			}
			
		}else{
			Logger.info("Migration container support already execute !");
		}
			
		Logger.info("Migration finish");
		return ok("Migration Finish");
	}
	
	
	public static void migreReadSet(ReadSet readSet) {
		SampleOnContainer sampleOnContainer = InstanceHelpers.getSampleOnContainer(readSet);
		if(null != sampleOnContainer){
			MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME,  ReadSet.class, 
					DBQuery.is("code", readSet.code), DBUpdate.set("sampleOnContainer", sampleOnContainer));
		}else{
			Logger.error("sampleOnContainer null for "+readSet.code);
		}
	}
	
	private static void backupReadSet() {
		Logger.info("\tCopie "+InstanceConstants.READSET_ILLUMINA_COLL_NAME+" start");		
		MongoDBResult<ReadSet> r = MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class);
		double nbPage = Math.ceil(r.count() / 500)+1;
		Logger.info("nb readsets ="+r.size()+" / "+nbPage);
		for(int i = 0 ; i < nbPage ; i++){
			MongoDBResult<ReadSet> r2 = MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class);
			List<ReadSet> readSets = r2.page(i, 500).toList();
			Logger.info(i+" Size "+readSets.size());
			MongoDBDAO.save(READSET_ILLUMINA_BCK, readSets);
			r2 = null;readSets=null;
		}
		
		
		Logger.info("\tCopie "+InstanceConstants.READSET_ILLUMINA_COLL_NAME+" end");
		
	}
}

