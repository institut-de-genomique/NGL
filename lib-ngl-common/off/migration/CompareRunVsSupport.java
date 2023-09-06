package controllers.migration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.ContainerSupport;
import models.laboratory.container.instance.Content;
import models.laboratory.run.instance.Lane;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.run.instance.Run;
import models.utils.InstanceConstants;

import org.mongojack.DBQuery;

import play.Logger;
import play.Logger.ALogger;
import play.mvc.Result;
import validation.ContextValidation;
import controllers.CommonController;
import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.MongoDBResult;

public class CompareRunVsSupport extends CommonController {
	
	
	
	protected static ALogger logger=Logger.of("Migration");
	public static Result migration(Boolean checkReadSets, Boolean checkSupport, Boolean update){
		
		if(update){
			Logger.info("update");
		}
		//Load run
		MongoDBResult<Run> runs = MongoDBDAO.find(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class);
		runs.sort("runSequencingStartDate");
		ContextValidation cv = new ContextValidation("ngl");
		cv.addKeyToRootKeyName("runs");
		while(runs.cursor.hasNext()){
			 Run run = runs.cursor.next();
			 if(!run.state.code.equals("IP-S") && !run.state.code.equals("FE-S") && !run.state.code.equals("IP-RG")){
				 String runId = run.code+" ("+run.state.code+" - "+run.valuation.valid+")";
				 
				 if(checkReadSets){
					 ContextValidation cvReadSets = new ContextValidation("ngl");
					 cvReadSets.addKeyToRootKeyName(runId);
					 compareRunVsReadSet(run, cvReadSets);
					 if(cvReadSets.hasErrors() && update){
						 Logger.info("update run ??? : ");
						 
					 }
					 cv.addErrors(cvReadSets.errors);				
				 }
				 
				 
				 if(checkSupport){
					 ContextValidation cvSupport = new ContextValidation("ngl");
					 cvSupport.addKeyToRootKeyName(runId);
					 ContainerSupport updateContainerSupport = compareRunVsSupport(run, cvSupport);
					 if(cvSupport.hasErrors() && update){
						 Logger.info("update support : "+updateContainerSupport.code);
						 MongoDBDAO.update(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, updateContainerSupport);
					 }
					 cv.addErrors(cvSupport.errors);
					 
					 ContextValidation cvContainer = new ContextValidation("ngl");
					 cvContainer.addKeyToRootKeyName(runId);
					 List<Container> updateContainers = compareRunVsContainer(run, cvContainer);
					 if(cvContainer.hasErrors() && update){
						 Logger.info("update containers : "+updateContainers.stream().map(c -> c.code).collect(Collectors.toSet()));
						 for(Container uCont : updateContainers){
							 MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME, uCont);
						 }
					 }
					 cv.addErrors(cvContainer.errors);
				 }
				 
				 
				 cv.removeKeyFromRootKeyName(runId);
			 }
		 }
		
		cv.displayErrors(logger);
		return ok();
	}
	
	
	private static void compareRunVsReadSet(Run run, ContextValidation cv) {
		cv.addKeyToRootKeyName("readsets");
		
		List<ReadSet> readsets = MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.is("runCode", run.code)).sort("code").toList();
		
		Set<String> runProjectCodes = run.projectCodes.stream().sorted().collect(Collectors.toSet());
		Set<String> runSampleCodes = run.sampleCodes.stream().sorted().collect(Collectors.toSet());
		
		Set<String> rsProjectCodes = readsets.stream().map(r -> r.projectCode).sorted().collect(Collectors.toSet());		
		Set<String> rsSampleCodes = readsets.stream().map(r -> r.sampleCode).sorted().collect(Collectors.toSet());
		
		if(!runProjectCodes.equals(rsProjectCodes)){
			 cv.addErrors("projectCodes", "readsets projectCodes not match run : "+runProjectCodes+" / "+rsProjectCodes );
		 }
		 
		if(!runSampleCodes.equals(rsSampleCodes)){
			cv.addErrors("sampleCodes", "readsets sampleCodes not match run : "+runSampleCodes+" / "+rsSampleCodes);
		}
		if(run.lanes != null){
			Map<String, Set<String>> mReadSets = readsets.stream()
					 .collect(Collectors.groupingBy((ReadSet r) -> r.laneNumber.toString(), Collectors.mapping((ReadSet r) -> r.code, Collectors.toSet())));
			Map<String, Set<String>> mReadSetsLanes = run.lanes
					.stream()
					.collect(Collectors.toMap((Lane l)->(l.number!=null)?l.number.toString():"NULL" , 
							(Lane l)->(null!=l.readSetCodes)?l.readSetCodes.stream().sorted().collect(Collectors.toSet()):new TreeSet<String>()));
			
			if(mReadSets.size() != mReadSetsLanes.size()){
				cv.addErrors("lanes", "Bad lane number between run and readsets "+mReadSets.size()+" / "+mReadSetsLanes.size());
			}else{
				for(String laneNumber : mReadSetsLanes.keySet()){
					cv.addKeyToRootKeyName(laneNumber);
					if(mReadSets.containsKey(laneNumber)){
						if(!mReadSetsLanes.get(laneNumber).equals(mReadSets.get(laneNumber))){
							cv.addErrors("readSetCodes", "lanes readsetCodes not match readsets : "+mReadSets.get(laneNumber)+" != "+mReadSetsLanes.get(laneNumber));
						}
					}else{
						 cv.addErrors("laneNumber", "laneNumber not exist in readsets "+laneNumber);
					}
					cv.removeKeyFromRootKeyName(laneNumber);
					
				}
			}
		}else{
			cv.addErrors("lanes","run lanes is null");
		}
		cv.removeKeyFromRootKeyName("readsets");
	}
	
	private static ContainerSupport compareRunVsSupport(Run run, ContextValidation cv) {		
		 cv.addKeyToRootKeyName("support");
		 String supportCode = run.containerSupportCode;
		 cv.addKeyToRootKeyName(supportCode);
		 ContainerSupport support = MongoDBDAO.findOne(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, ContainerSupport.class, DBQuery.is("code", supportCode));
		 if(null != support && run.lanes != null){
			 //compare flowcell category
			 String flowcellCat = "flowcell-"+run.lanes.size();
			 if(!flowcellCat.equals(support.categoryCode)){
				 cv.addErrors("categoryCode","Bad Flowcell category "+flowcellCat+" / "+support.categoryCode);
				 support.categoryCode=flowcellCat;
			 }
			 
			 Set<String> supportProjectCodes = support.projectCodes.stream().collect(Collectors.toSet());
			 Set<String> supportSampleCodes = support.sampleCodes.stream().collect(Collectors.toSet());
			 
			 Set<String> runProjectCodes = run.projectCodes.stream().collect(Collectors.toSet());
			 Set<String> runSampleCodes = run.sampleCodes.stream().collect(Collectors.toSet());
				
			 
			 //lims vs support
			 if(!runProjectCodes.equals(supportProjectCodes)){
				 cv.addErrors("projectCodes", "support projectCodes not match run : "+runProjectCodes+" / "+ supportProjectCodes);
				 support.projectCodes = new HashSet<String>(runProjectCodes);
			 }
			 
			 if(!runSampleCodes.equals(supportSampleCodes)){
				 cv.addErrors("sampleCodes", "support sampleCodes not match run : "+runSampleCodes+" / "+supportSampleCodes);
				 support.sampleCodes = new HashSet<String>(runSampleCodes);
			 }
			 
			 
		 }else{
			 cv.addErrors("support", "Support not found or run.lane null : "+supportCode);
		 }
		  cv.removeKeyFromRootKeyName(supportCode);
		  cv.removeKeyFromRootKeyName("support");
		  
		  return support;
	}
	
	private static List<Container> compareRunVsContainer(Run run, ContextValidation cv) {
		cv.addKeyToRootKeyName("containers");
		List<Container> modifyContainers = new ArrayList<Container>();
		
		List<Container> containers = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class, DBQuery.is("support.code", run.containerSupportCode)).toList();
		 //put in map with the key is lane number
		 Map<String, List<Container>> mapContainers = containers.stream()
				 .collect(Collectors.groupingBy(c -> c.support.line));
		
		 List<ReadSet> readsets = MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.is("runCode", run.code)).sort("code").toList();
		 Map<String, List<ReadSet>> mReadSets = readsets.stream()
				 .collect(Collectors.groupingBy((ReadSet r) -> r.laneNumber.toString()));	
		 
		 if(mReadSets.size() != mapContainers.size()){
			 cv.addErrors("lanes", "Bad lane number between readsets and containers "+mReadSets.size()+" / "+mapContainers.size());
		 }else {
			 for(String laneNumber : mReadSets.keySet()){
				cv.addKeyToRootKeyName(laneNumber);
				Set<String> rsSampleCodes = mReadSets.get(laneNumber).stream().map((ReadSet rs) -> rs.sampleCode).collect(Collectors.toSet());
				Set<String> rsProjectCodes = mReadSets.get(laneNumber).stream().map((ReadSet rs) -> rs.projectCode).collect(Collectors.toSet());
				
				if(mapContainers.get(laneNumber).size() != 1)Logger.error("containers size for one lane != 1 : "+mapContainers.get(laneNumber).size());
				Container currentContainer = mapContainers.get(laneNumber).get(0);
				
				Set<String> cSampleCodes = currentContainer.sampleCodes.stream().collect(Collectors.toSet());
				Set<String> cProjectCodes = currentContainer.projectCodes.stream().collect(Collectors.toSet());
				
				if (!rsProjectCodes.equals(cProjectCodes)) {
					cv.addErrors("projectCodes","containers projectCodes not match readsets : "+ rsProjectCodes + " != " + cProjectCodes);
					currentContainer.projectCodes = new HashSet<String>(rsProjectCodes);
				}
				
				if (!rsSampleCodes.equals(cSampleCodes)) {
					cv.addErrors("sampleCodes",	"containers sampleCodes not match readsets : "+ rsSampleCodes + " != " + cSampleCodes);
					currentContainer.sampleCodes = new HashSet<String>(rsSampleCodes);
				}
				cv.addKeyToRootKeyName("contents");
				Map<String, List<Content>> scContents = currentContainer.contents.stream().collect(Collectors.groupingBy((Content c ) -> c.sampleCode));
				Map<String, List<ReadSet>> scReadSet = mReadSets.get(laneNumber).stream().collect(Collectors.groupingBy((ReadSet rs ) -> rs.sampleCode));
				
				List<Content> modifyContents = new ArrayList<Content>();
				
				for(String sampleCode : scReadSet.keySet()){
					cv.addKeyToRootKeyName(sampleCode);
					 if(scContents.containsKey(sampleCode)){
						 List<Content> contents = scContents.get(sampleCode);
						 List<ReadSet> readSets = scReadSet.get(sampleCode);
						 
						 if(contents.size() == 1 && readSets.size() == 1){
							 Content currentContent = contents.get(0);
							 ReadSet readSet= readSets.get(0);
							 
							 if(!readSet.projectCode.equals(currentContent.projectCode)){
								 cv.addErrors("projectCode", "content project code not match lims : "+readSet.projectCode +" != "+currentContent.projectCode);
								 currentContent.projectCode = readSet.projectCode;
							 }
							 
							 String[] tags = readSet.code.split("\\.");
							 if(tags.length == 2){
								 String tagRs = tags[1];
								 String tagContent = (currentContent.properties.get("tag") != null && currentContent.properties.get("tag").value != null)?(String)currentContent.properties.get("tag").value:"NULL";
								 if(!tagRs.equals(tagContent)){
									 cv.addErrors("tag", "content tag not match readset : "+ tagRs+" != "+tagContent);
								 }
							 }
							 modifyContents.add(currentContent);					 									
						 }else if(contents.size() == readSets.size()){
							 
							 List<String> badContents = contents.stream().filter((Content c)->c.properties.get("tag") == null || c.properties.get("tag").value == null).map((Content c) -> c.sampleCode).collect(Collectors.toList());
							 List<String> badReadSets = readSets.stream().filter((ReadSet r)-> r.code.split("\\.").length == 0).map((ReadSet r)-> r.code).collect(Collectors.toList());
							 
							 if(badReadSets.size() == 0 && badContents.size() == 0){
								 Map<String, String> iReadSets = readSets.stream().collect(Collectors.toMap((ReadSet r)-> r.code.split("\\.")[1], (ReadSet r)-> r.projectCode));									 
								 Map<String, Content> iContents = contents.stream().collect(Collectors.toMap((Content c)->c.properties.get("tag").value.toString(), (Content c)->c));
								 
								 for(String tag : iReadSets.keySet()){
									 if(iContents.containsKey(tag)){
										 Content currentContent = iContents.get(tag);
										 if(!iReadSets.get(tag).equals(currentContent.projectCode) ){
											 cv.addErrors("projectCode", "not same content projectCode as readset for tag :"+tag+" - '"+iReadSets.get(tag)+" != "+currentContent.projectCode);
										 }
										 modifyContents.add(currentContent);	
									 }else{
										 cv.addErrors("tag", "not found in readsets :"+tag);
									 }
								 }	
							 }else{
								 cv.addErrors("readsets","without tag : "+badReadSets);
							 }
											 
						 }else{
							 cv.addErrors("sampleCode","not same readsets number and lims library number : "+sampleCode);
							 
						 }							 
					 }else{
						 cv.addErrors("sampleCode","not found in container or solexa : "+sampleCode);							
					 }
					 cv.removeKeyFromRootKeyName(sampleCode);
				 }
				
				 cv.removeKeyFromRootKeyName("contents");
				 cv.removeKeyFromRootKeyName(laneNumber);
				 
				 if(modifyContents.size() == currentContainer.contents.size()){
					 currentContainer.contents = modifyContents;
					 modifyContainers.add(currentContainer);
				 }else{
					 Logger.error("not same content number for container : "+currentContainer.code);
				 }
				 
				 
			 }
			
		 }
		cv.removeKeyFromRootKeyName("containers");
		return modifyContainers;
	}
}
