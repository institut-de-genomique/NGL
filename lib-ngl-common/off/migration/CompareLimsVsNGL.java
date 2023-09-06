package controllers.migration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import lims.cns.dao.LimsAbandonDAO;
import lims.models.experiment.illumina.Flowcell;
import lims.models.experiment.illumina.Library;
import lims.models.runs.LimsFile;
import lims.services.ILimsRunServices;
import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.ContainerSupport;
import models.laboratory.container.instance.Content;
import models.laboratory.run.instance.File;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.run.instance.Run;
import models.utils.InstanceConstants;

import org.mongojack.DBQuery;

import play.Logger;
import play.Logger.ALogger;
import play.api.modules.spring.Spring;
import play.mvc.Result;
import validation.ContextValidation;
import controllers.CommonController;
import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.MongoDBResult;

public class CompareLimsVsNGL extends CommonController {
	
	
	
	protected static ALogger logger=Logger.of("Migration");
	public static Result compareSupport(Boolean update){
		ILimsRunServices  limsRunServices = Spring.getBeanOfType(ILimsRunServices.class);
		MongoDBResult<ContainerSupport> supports = MongoDBDAO.find(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, ContainerSupport.class, DBQuery.in("categoryCode", "flowcell-8","flowcell-4","flowcell-2","flowcell-1"));
		
		ContextValidation cv = new ContextValidation("ngl");
		cv.addKeyToRootKeyName("supports");
		while(supports.cursor.hasNext()){
			ContainerSupport cs = supports.cursor.next();
			Flowcell containerSupportLims = (Flowcell)limsRunServices.getContainerSupport(cs.code);
			if(null != containerSupportLims){
				ContextValidation cvSupport = new ContextValidation("ngl");
				cvSupport.addKeyToRootKeyName(cs.code);
				ContainerSupport updateContainerSupport = compareLimsVsSupport(containerSupportLims, cs, cvSupport);
				if(cvSupport.hasErrors() && update){
					 Logger.info("update support : "+updateContainerSupport.code);
					 MongoDBDAO.update(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, updateContainerSupport);
				}
				cv.addErrors(cvSupport.errors);
				
				ContextValidation cvContainer = new ContextValidation("ngl");
				cvContainer.addKeyToRootKeyName(cs.code);
				List<Container> updateContainers = compareLimsVsContainer(containerSupportLims, cvContainer);
				if(cvContainer.hasErrors() && update){
					 Logger.info("update containers : "+updateContainers.stream().map(c -> c.code).collect(Collectors.toSet()));
					 for(Container uCont : updateContainers){
						 MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME, uCont);
					 }
				}
				cv.addErrors(cvContainer.errors);
			}
		}
		cv.displayErrors(logger);
		return ok();
	}
	
	
	
	public static Result compareRun(Boolean checkRun, Boolean checkSupport, Boolean update){
		ILimsRunServices  limsRunServices = Spring.getBeanOfType(ILimsRunServices.class);  		
		if(checkSupport){
			 Logger.info("check support / containers");
		}
		if(checkRun){
			Logger.info("check run / readsets");
		}
		if(update){
			Logger.info("update from lims");
		}
		//Load run
		MongoDBResult<Run> runs = MongoDBDAO.find(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class);
		runs.sort("runSequencingStartDate");
		ContextValidation cv = new ContextValidation("ngl");
		cv.addKeyToRootKeyName("runs");
		while(runs.cursor.hasNext()){
			 Run run = runs.cursor.next();
			 
			 String runId = run.code+" ("+run.state.code+" - "+run.valuation.valid+")";
			 
			 Flowcell containerSupportLims = (Flowcell)limsRunServices.getContainerSupport(run.containerSupportCode);
			 
			 if(containerSupportLims != null && checkSupport){
				 ContextValidation cvSupport = new ContextValidation("ngl");
				 cvSupport.addKeyToRootKeyName(runId);
				 ContainerSupport updateContainerSupport = compareLimsVsSupport(containerSupportLims, null, cvSupport);
				 if(cvSupport.hasErrors() && update){
					 Logger.info("update support : "+updateContainerSupport.code);
					 MongoDBDAO.update(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, updateContainerSupport);
				 }
				 cv.addErrors(cvSupport.errors);
				 
				 ContextValidation cvContainer = new ContextValidation("ngl");
				 cvContainer.addKeyToRootKeyName(runId);
				 List<Container> updateContainers = compareLimsVsContainer(containerSupportLims, cvContainer);
				 if(cvContainer.hasErrors() && update){
					 Logger.info("update containers : "+updateContainers.stream().map(c -> c.code).collect(Collectors.toSet()));
					 for(Container uCont : updateContainers){
						 MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME, uCont);
					 }
				 }
				 cv.addErrors(cvContainer.errors);
			 }
			 
			 //Logger.info("treat run : "+run.code+" / "+run.containerSupportCode);
			 if(containerSupportLims != null && run.lanes != null &&  checkRun && !run.state.code.equals("IP-S") && !run.state.code.equals("FE-S") && !run.state.code.equals("IP-RG")){
				 cv.addKeyToRootKeyName(runId);
				compareLimsVsRun(containerSupportLims, run, cv);
				compareLimsVsReadSet(containerSupportLims, run, cv);					
			 }
			 cv.removeKeyFromRootKeyName(runId);
		 }
		
		cv.displayErrors(logger);
		return ok();
	}
	
	

	private static void compareLimsVsRun(Flowcell containerSupportSolexa, Run run, ContextValidation cv) {
		 cv.addKeyToRootKeyName("_run");
		 
		 if(containerSupportSolexa.lanes.size() != run.lanes.size()){
			 cv.addErrors("lanes", "Bad lane number between lims and run "+containerSupportSolexa.lanes.size()+" / "+run.lanes.size());
		 }
		 
		 Set<String> solexaProjectCodes = containerSupportSolexa.lanes.stream()
				 	.map((lims.models.experiment.illumina.Lane l) -> l.librairies)
				 	.flatMap(List::stream).map((Library l) -> l.projectCode)
				 	.collect(Collectors.toSet());
		 
		 Set<String> solexaSamplesCodes = containerSupportSolexa.lanes.stream()
				 	.map((lims.models.experiment.illumina.Lane l) -> l.librairies)
				 	.flatMap(List::stream).map((lims.models.experiment.illumina.Library l) -> l.sampleContainerCode)
				 	.collect(Collectors.toSet());
		 
		 //lims vs run
		 if(!solexaProjectCodes.equals(run.projectCodes)){
			 cv.addErrors("projectCodes", "run projectCodes not match lims : "+solexaProjectCodes+" / "+run.projectCodes );
		 }
		 
		 if(!solexaSamplesCodes.equals(run.sampleCodes)){
			 cv.addErrors("sampleCodes", "run sampleCodes not match lims : "+solexaSamplesCodes+" / "+run.sampleCodes);
		 }
		 		  
		 cv.removeKeyFromRootKeyName("_run");
	}
	
	
	
	private static void compareLimsVsReadSet(Flowcell containerSupportLims, Run run, 
			ContextValidation cv) {
		cv.addKeyToRootKeyName("readsets");
		
		List<ReadSet> readsets = MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.is("runCode", run.code)).toList();
		
		Map<String, List<ReadSet>> mReadSets = readsets.stream()
				 .collect(Collectors.groupingBy((ReadSet r) -> r.laneNumber.toString()));
		 
		 Map<String, List<lims.models.experiment.illumina.Lane>> mSolexaLibrary = containerSupportLims.lanes.stream()
				 .collect(Collectors.groupingBy((lims.models.experiment.illumina.Lane l) -> l.number.toString()));
		 
		 if(mSolexaLibrary.size() != mReadSets.size()){
			 cv.addErrors("lanes", "Bad lane number between lims and readsets "+mSolexaLibrary.size()+" / "+mReadSets.size());
		 }else{
			 
		 
		 
		 for(String laneNumber : mSolexaLibrary.keySet()){
			cv.addKeyToRootKeyName(laneNumber);
			Set<String> solSampleCodes = mSolexaLibrary.get(laneNumber).get(0).librairies.stream().map((Library sl) -> sl.sampleContainerCode).collect(Collectors.toSet());
			Set<String> solProjectCodes = mSolexaLibrary.get(laneNumber).get(0).librairies.stream().map((Library sl) -> sl.projectCode).collect(Collectors.toSet());
			
			Set<String> rsSampleCodes = mReadSets.get(laneNumber).stream().map(rs -> rs.sampleCode).collect(Collectors.toSet());
			Set<String> rsProjectCodes = mReadSets.get(laneNumber).stream().map(rs -> rs.projectCode).collect(Collectors.toSet());
			
			if (!rsProjectCodes.equals(solProjectCodes)) {
				cv.addErrors("projectCodes","readsets projectCodes not match lims : "+ rsProjectCodes + " != " + solProjectCodes);
			}
			
			if (!solSampleCodes.equals(rsSampleCodes)) {
				cv.addErrors("sampleCodes",	"readsets sampleCodes not match lims : "+ rsSampleCodes + " != " + solSampleCodes);
			}
			cv.addKeyToRootKeyName("contents");
			Map<String, List<ReadSet>> scReadSet = mReadSets.get(laneNumber).stream().collect(Collectors.groupingBy((ReadSet r ) -> r.sampleCode));
			Map<String, List<Library>> scSolexa = mSolexaLibrary.get(laneNumber).get(0).librairies.stream().collect(Collectors.groupingBy((Library l ) -> l.sampleContainerCode));
			
			for(String sampleCode : scSolexa.keySet()){
				cv.addKeyToRootKeyName(sampleCode);
				 if(scReadSet.containsKey(sampleCode)){
					 List<ReadSet> rss = scReadSet.get(sampleCode);
					 List<Library> librairies = scSolexa.get(sampleCode);
					 
					 if(rss.size() == 1 && librairies.size() == 1){
						 ReadSet rs = rss.get(0);
						 Library l = librairies.get(0);
						 
						 if(!l.projectCode.equals(rs.projectCode)){
							 cv.addErrors("projectCode", "readset project code not match lims : "+l.projectCode +" != "+rs.projectCode);
						 }
						 String tagSolexa = l.tagName;
						 if(null != tagSolexa){
							 String[] tags = rs.code.split("\\.");
							 if(tags.length == 2){
								 String tagRs = tags[1];
								 if(!tagSolexa.equals(tagRs)){
									 cv.addErrors("tag", "readset tag not match lims : "+ tagSolexa+" != "+tagRs);
								 }
							 }else{
								 cv.addErrors("tag", "no tag for readset btu exist for lims : "+ tagSolexa);
							 }
						 }
						 
						 									 
					 }else if(rss.size() == librairies.size()){
						 
						 List<String> badReadSets = rss.stream().filter((ReadSet r)-> r.code.split("\\.").length == 0).map((ReadSet r)-> r.code).collect(Collectors.toList());
						 
						 if(badReadSets.size() == 0){
							 Map<String, String> iReadSets = rss.stream().collect(Collectors.toMap((ReadSet r)-> r.code.split("\\.")[1], (ReadSet r)-> r.projectCode));									 
							 Map<String, String> iLibrary = librairies.stream().collect(Collectors.toMap((Library l)-> l.tagName, (Library l)-> l.projectCode));									 
							 
							 for(String tag : iLibrary.keySet()){
								 if(iReadSets.containsKey(tag)){
									 if(!iLibrary.get(tag).equals(iReadSets.get(tag)) ){
										 cv.addErrors("projectCode", "not same readset projectCode as lims for tag :"+tag+" - '"+iLibrary.get(tag)+" != "+iReadSets.get(tag));
									 }
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
		 }
		 }
		
		cv.removeKeyFromRootKeyName("readsets");
	}
	
	private static ContainerSupport compareLimsVsSupport(Flowcell containerSupportSolexa, ContainerSupport support, ContextValidation cv) {		
		 cv.addKeyToRootKeyName("support");
		 String supportCode = containerSupportSolexa.containerSupportCode;
		 cv.addKeyToRootKeyName(supportCode);
		 if(null == support)
			 support = MongoDBDAO.findOne(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, ContainerSupport.class, DBQuery.is("code", supportCode));
		 
		 if(null != support){
			 //compare flowcell category
			 String flowcellCat = "flowcell-"+containerSupportSolexa.lanes.size();
			 if(!flowcellCat.equals(support.categoryCode)){
				 cv.addErrors("categoryCode","Bad Flowcell category "+flowcellCat+" / "+support.categoryCode);
				 support.categoryCode=flowcellCat;
			 }
			 
			 Set<String> supportProjectCodes = support.projectCodes.stream().collect(Collectors.toSet());
			 Set<String> supportSampleCodes = support.sampleCodes.stream().collect(Collectors.toSet());
			 
			 Set<String> solexaProjectCodes = containerSupportSolexa.lanes.stream()
					 	.map((lims.models.experiment.illumina.Lane l) -> l.librairies)
					 	.flatMap(List::stream).map((Library l) -> l.projectCode)
					 	.collect(Collectors.toSet());
			 
			 Set<String> solexaSamplesCodes = containerSupportSolexa.lanes.stream()
					 	.map((lims.models.experiment.illumina.Lane l) -> l.librairies)
					 	.flatMap(List::stream).map((lims.models.experiment.illumina.Library l) -> l.sampleContainerCode)
					 	.collect(Collectors.toSet());
			 
			 //lims vs support
			 if(!solexaProjectCodes.equals(supportProjectCodes)){
				 cv.addErrors("projectCodes", "support projectCodes not match lims : "+solexaProjectCodes+" / "+ supportProjectCodes);
				 support.projectCodes = new HashSet<String>(solexaProjectCodes);
			 }
			 
			 if(!solexaSamplesCodes.equals(supportSampleCodes)){
				 cv.addErrors("sampleCodes", "support sampleCodes not match lims : "+solexaSamplesCodes+" / "+supportSampleCodes);
				 support.sampleCodes = new HashSet<String>(solexaSamplesCodes);
			 }
			 
			 
		 }else{
			 cv.addErrors("support", "Support not found : "+supportCode);
		 }
		  cv.removeKeyFromRootKeyName(supportCode);
		  cv.removeKeyFromRootKeyName("support");
		  
		  return support;
	}
	
	
	
	private static List<Container> compareLimsVsContainer(Flowcell containerSupportLims, ContextValidation cv) {
		cv.addKeyToRootKeyName("containers");
		List<Container> modifyContainers = new ArrayList<Container>();
		
		List<Container> containers = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class, DBQuery.is("support.code", containerSupportLims.containerSupportCode)).toList();
		 //put in map with the key is lane number
		 Map<String, List<Container>> mapContainers = containers.stream()
				 .collect(Collectors.groupingBy(c -> c.support.line));
		
		 
		 Map<String, List<lims.models.experiment.illumina.Lane>> mSolexaLibrary = containerSupportLims.lanes.stream()
				 .collect(Collectors.groupingBy((lims.models.experiment.illumina.Lane l) -> l.number.toString()));
		 
		 if(mSolexaLibrary.size() != mapContainers.size()){
			 cv.addErrors("lanes", "Bad lane number between lims and containers "+mSolexaLibrary.size()+" / "+mapContainers.size());
		 }else {
			 for(String laneNumber : mSolexaLibrary.keySet()){
				cv.addKeyToRootKeyName(laneNumber);
				Set<String> solSampleCodes = mSolexaLibrary.get(laneNumber).get(0).librairies.stream().map((Library sl) -> sl.sampleContainerCode).collect(Collectors.toSet());
				Set<String> solProjectCodes = mSolexaLibrary.get(laneNumber).get(0).librairies.stream().map((Library sl) -> sl.projectCode).collect(Collectors.toSet());
				
				if(mapContainers.get(laneNumber).size() != 1)Logger.error("containers size for one lane != 1 : "+mapContainers.get(laneNumber).size());
				Container currentContainer = mapContainers.get(laneNumber).get(0);
				
				Set<String> cSampleCodes = currentContainer.sampleCodes.stream().collect(Collectors.toSet());
				Set<String> cProjectCodes = currentContainer.projectCodes.stream().collect(Collectors.toSet());
				
				if (!solProjectCodes.equals(cProjectCodes)) {
					cv.addErrors("projectCodes","containers projectCodes not match lims : "+ solProjectCodes + " != " + cProjectCodes);
					currentContainer.projectCodes = new HashSet<String>(solProjectCodes);
				}
				
				if (!solSampleCodes.equals(cSampleCodes)) {
					cv.addErrors("sampleCodes",	"containers sampleCodes not match lims : "+ solSampleCodes + " != " + cSampleCodes);
					currentContainer.sampleCodes = new HashSet<String>(solSampleCodes);
				}
				cv.addKeyToRootKeyName("contents");
				Map<String, List<Content>> scContents = currentContainer.contents.stream().collect(Collectors.groupingBy((Content c ) -> c.sampleCode));
				Map<String, List<Library>> scSolexa = mSolexaLibrary.get(laneNumber).get(0).librairies.stream().collect(Collectors.groupingBy((Library l ) -> l.sampleContainerCode));
				
				List<Content> modifyContents = new ArrayList<Content>();
				
				for(String sampleCode : scSolexa.keySet()){
					cv.addKeyToRootKeyName(sampleCode);
					 if(scContents.containsKey(sampleCode)){
						 List<Content> contents = scContents.get(sampleCode);
						 List<Library> librairies = scSolexa.get(sampleCode);
						 
						 if(contents.size() == 1 && librairies.size() == 1){
							 Content currentContent = contents.get(0);
							 Library l = librairies.get(0);
							 
							 if(!l.projectCode.equals(currentContent.projectCode)){
								 cv.addErrors("projectCode", "content project code not match lims : "+l.projectCode +" != "+currentContent.projectCode);
								 currentContent.projectCode = l.projectCode;
							 }
							 
							 String tagSolexa = l.tagName;
							 if(null != tagSolexa){
								 String tagContent = (String)currentContent.properties.get("tag").value;
								 if(tagContent != null){
									if(!tagSolexa.equals(tagContent)){
										 cv.addErrors("tag", "readset tag not match lims : "+ tagSolexa+" != "+tagContent);
										 currentContent.properties.get("tag").value = tagSolexa;
									 }
								 }else{
									 cv.addErrors("tag", "no tag for content but exist for lims : "+ tagSolexa);
								 }
							 }
							 modifyContents.add(currentContent);					 									
						 }else if(contents.size() == librairies.size()){
							 
							 List<String> badContents = contents.stream().filter((Content c)->c.properties.get("tag") == null || c.properties.get("tag").value == null).map((Content c) -> c.sampleCode).collect(Collectors.toList());
							  
							 if(badContents.size() == 0){
								 Map<String, Content> iContents = contents.stream().collect(Collectors.toMap((Content c)->c.properties.get("tag").value.toString(), (Content c)->c));
								 Map<String, Library> iLibrary = librairies.stream().collect(Collectors.toMap((Library l)-> l.tagName, (Library l)-> l));									 
								 
								 for(String tag : iLibrary.keySet()){
									 if(iContents.containsKey(tag)){
										 Content currentContent = iContents.get(tag);
										 Library l = iLibrary.get(tag);
										 
										 if(!l.projectCode.equals(currentContent.projectCode)){
											 cv.addErrors("projectCode", "content project code not match limsfor tag :"+tag+" - '"+l.projectCode +" != "+currentContent.projectCode);
											 currentContent.projectCode = l.projectCode;
										 }
										 modifyContents.add(currentContent);
									 }else{
										 cv.addErrors("tag", "not found in readsets :"+tag);
									 }
								 }	
							 }else{
								 cv.addErrors("contents","without tag : "+badContents);
							 }							 
						 }else{
							 cv.addErrors("sampleCode","not same content number and lims library number : "+sampleCode);
							 
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
					 Logger.error("not same content number for container : "+currentContainer.code+" - "+currentContainer.contents.size()+" / "+modifyContents.size());
				 }
				 
				 
			 }
			
		 }
		cv.removeKeyFromRootKeyName("containers");
		return modifyContainers;
	}
	
	public static Result compareFile(Boolean update) {
		
		LimsAbandonDAO  limsAbandonDAO = Spring.getBeanOfType(LimsAbandonDAO.class);
		MongoDBResult<ReadSet> readsets = MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.in("typeCode", Arrays.asList("default-readset","rsillumina")));
		
		ContextValidation cv = new ContextValidation("ngl");
		cv.addKeyToRootKeyName("readsets");
		while(readsets.cursor.hasNext()){
			ReadSet rs = readsets.cursor.next();
			ContextValidation cvrs = new ContextValidation("ngl");
			cvrs.addKeyToRootKeyName(rs.code+" ("+rs.state.code+")");
			List<LimsFile> limsFiles = limsAbandonDAO.getFiles(rs.code);
			List<File> nglFiles = rs.files;
			
			if(null == nglFiles)nglFiles=new ArrayList<File>(0);
			if(null == limsFiles)limsFiles=new ArrayList<LimsFile>(0);
			
			
			if(limsFiles.size() != nglFiles.size()){
				cvrs.addErrors("nbFiles","nbFiles is different between readset and lims : "+ nglFiles.size() +" != "+limsFiles.size());
			}
			
			for(File nglF: nglFiles){
				boolean find = false;
				for(LimsFile limsFile: limsFiles){
					if(nglF.fullname.equals(limsFile.fullname)){
						find=true;
						break;
					}
				}
				if(!find){
					cvrs.addErrors("fullname","not found file : "+nglF.fullname);
				}
			}
			
			cvrs.removeKeyFromRootKeyName(rs.code+" ("+rs.state.code+")");
			if(cvrs.hasErrors() && update && (rs.state.code.equals("A") || rs.state.code.equals("UA")) && limsAbandonDAO.isLseqco(rs)){
				 Logger.info("update readset : "+rs.code);
				 try{
					 limsAbandonDAO.insertFiles(rs,true);
				 }catch(Throwable t){
					 Logger.error("probleme to synchonize files in dblims for "+rs.code,t);
				 }
			}
			
			if(cvrs.hasErrors() && (rs.state.code.equals("A") || rs.state.code.equals("UA"))  && limsAbandonDAO.isLseqco(rs)){
				cv.addErrors(cvrs.errors); 
			}else if(!limsAbandonDAO.isLseqco(rs)){
				Logger.error("ReadSet not exist in dblims "+rs.code);
			}
		}
		cv.displayErrors(logger);
		return ok();
	}
	
}
