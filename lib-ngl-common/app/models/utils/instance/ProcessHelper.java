package models.utils.instance;

import static fr.cea.ig.play.IGGlobals.configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import controllers.processes.api.Processes;
import fr.cea.ig.MongoDBDAO;
import models.laboratory.common.instance.State;
import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.ContainerSupport;
import models.laboratory.processes.description.ProcessType;
import models.laboratory.processes.instance.Process;
import models.utils.CodeHelper;
import models.utils.InstanceConstants;
import models.utils.InstanceHelpers;
import rules.services.RulesServices6;
import validation.ContextValidation;
import validation.common.instance.CommonValidationHelper;

public class ProcessHelper {
	
	public static final play.Logger.ALogger logger = play.Logger.of(ProcessHelper.class);
	
	private static final String rulesKey() {
		return configuration().getString("rules.key");	
	}
	
	@Deprecated
	public static void updateContainer(Container container, String typeCode, Set<String> codes, ContextValidation contextValidation) {
		if (container.fromTransformationTypeCodes == null || container.fromTransformationTypeCodes.size() == 0) {
			container.fromTransformationTypeCodes = new HashSet<>();
			ProcessType processType;
//			try {
				processType = ProcessType.find.get().findByCode(typeCode);
				container.fromTransformationTypeCodes.add(processType.voidExperimentType.code);
//			} catch (DAOException e) {
//				throw new RuntimeException();
//			}
		}
		container.processTypeCodes = Collections.singleton(typeCode);
		if (container.processCodes == null) { 
			container.processCodes = new HashSet<>();
		}
		container.processCodes.addAll(codes);
		
		if (!contextValidation.hasErrors()) {
			MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME, Container.class,
					          DBQuery.is("code", container.code),
					          DBUpdate.set("processCodes",                container.processCodes)
					                  .set("processTypeCodes",            container.processTypeCodes)
					                  .set("fromTransformationTypeCodes", container.fromTransformationTypeCodes));
		}
	}

	@Deprecated
	public static void updateContainerSupportFromContainer(Container container, ContextValidation contextValidation){
		ContainerSupport containerSupport=MongoDBDAO.findByCode(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, ContainerSupport.class, container.support.code);
		if (containerSupport != null) {
			if (containerSupport.fromTransformationTypeCodes == null) {
				containerSupport.fromTransformationTypeCodes = new HashSet<>();
			}
			containerSupport.fromTransformationTypeCodes.addAll(container.fromTransformationTypeCodes);
			contextValidation.putObject(CommonValidationHelper.FIELD_STATE_CODE, container.state.code);
			CommonValidationHelper.validateExperimentTypeCodes(contextValidation, containerSupport.fromTransformationTypeCodes);
			if (!contextValidation.hasErrors()) {
				MongoDBDAO.update(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME,ContainerSupport.class,
						DBQuery.is("code", container.support.code)
						,DBUpdate.set("fromTransformationTypeCodes",container.fromTransformationTypeCodes));
			}
		} else {
			logger.error("Support container not exist = "+container.support.code);
		}
	}

	public static Process applyRules(Process proc, ContextValidation ctx, String rulesName) {
		ArrayList<Object> facts = new ArrayList<>();
		facts.add(proc);
		facts.add(ctx);
		RulesServices6.getInstance().callRulesWithGettingFacts(rulesKey(), rulesName, facts);
		return proc;
	}
	
	public static List<Process> applyRules(List<Process> processes, ContextValidation ctx, String rulesName) {
		ArrayList<Object> facts = new ArrayList<>();
		facts.add(ctx);
		facts.addAll(processes);		
		RulesServices6.getInstance().callRulesWithGettingFacts(rulesKey(), rulesName, facts);
		return processes;
	}	
	
//	public static List<Process> getNewProcessList(ContextValidation contextValidation, Process input, String from) {
//		if ("from-container".equals(from)) {
//			Container container = MongoDBDAO.findByCode(InstanceConstants.CONTAINER_COLL_NAME, Container.class, input.inputContainerCode);
//			return container.contents.parallelStream().map(content ->{
//					Process process = input.cloneCommon();
//					process.sampleCodes = SampleHelper.getSampleParent(content.sampleCode);
//					process.projectCodes = SampleHelper.getProjectParent(process.sampleCodes);
//					process.sampleOnInputContainer = InstanceHelpers.getSampleOnInputContainer(content, container);
//					//need sampleOnInputContainer to generate code
//					process.code = CodeHelper.getInstance().generateProcessCode(process);
//					
//					process.state = new State();
//					process.state.code = "N";
//					process.state.user = contextValidation.getUser();
//					process.state.date = new Date();
//					
//					return process;
//				}).collect(Collectors.toList());
//		} else if ("from-sample".equals(from)) {
//			Process process = input.cloneCommon();
//			process.projectCodes = SampleHelper.getProjectParent(process.sampleCodes);
//			if(process.sampleCodes.size() == 1){
//				process.sampleOnInputContainer = InstanceHelpers.getSampleOnInputContainer(process.sampleCodes.iterator().next());
//				
//			}else{
//				logger.error("not only one sample code during process creation from sample");
//				return null;
//			}
//			
//			process.state = new State();
//			process.state.code = "IW-C";
//			process.state.user = contextValidation.getUser();
//			process.state.date = new Date();
//			// need sampleOnInputContainer to generate code
//			process.code = CodeHelper.getInstance().generateProcessCode(process);
//			return Arrays.asList(process);
//		} else {
//			throw new RuntimeException("from :" + from + " not managed for the processes creation");
//		}
//	}
	
	// ----------------------------------------------------------------------------
	
	@Deprecated
	public static List<Process> getNewProcessList(ContextValidation contextValidation, Process input, String from) {
//		if ("from-container".equals(from)) {
		if (Processes.FROM_CONTAINER.equals(from)) {
//			Container container = MongoDBDAO.findByCode(InstanceConstants.CONTAINER_COLL_NAME, Container.class, input.inputContainerCode);
//			return container.contents.parallelStream().map(content -> {
//					Process process                = input.cloneCommon();
//					process.sampleCodes            = SampleHelper   .getSampleParent          (content.sampleCode);
//					process.projectCodes           = SampleHelper   .getProjectParent         (process.sampleCodes);
//					process.sampleOnInputContainer = InstanceHelpers.getSampleOnInputContainer(content, container);
//					// need sampleOnInputContainer to generate code
//					process.code = CodeHelper.getInstance().generateProcessCode(process);
//					
//					process.state = new State();
//					process.state.code = "N";
//					process.state.user = contextValidation.getUser();
//					process.state.date = new Date();
//					
//					return process;
//				}).collect(Collectors.toList());
			return newProcessListFromContainer(contextValidation, input);
		} else if (Processes.FROM_SAMPLE.equals(from)) {
//			Process process = input.cloneCommon();
//			process.projectCodes = SampleHelper.getProjectParent(process.sampleCodes);
//			if(process.sampleCodes.size() == 1){
//				process.sampleOnInputContainer = InstanceHelpers.getSampleOnInputContainer(process.sampleCodes.iterator().next());
//				
//			}else{
//				logger.error("not only one sample code during process creation from sample");
//				return null;
//			}
//			
//			process.state = new State();
//			process.state.code = "IW-C";
//			process.state.user = contextValidation.getUser();
//			process.state.date = new Date();
//			// need sampleOnInputContainer to generate code
//			process.code = CodeHelper.getInstance().generateProcessCode(process);
//			return Arrays.asList(process);
			return newProcessListFromSample(contextValidation, input);
		} else {
			throw new RuntimeException("from :" + from + " not managed for the processes creation");
		}
	}
	
	public static List<Process> newProcessListFromContainer(ContextValidation contextValidation, Process input) {
		Container container = MongoDBDAO.findByCode(InstanceConstants.CONTAINER_COLL_NAME, Container.class, input.inputContainerCode);
		return container.contents.parallelStream().map(content -> {
			Process process                = input.cloneCommon();
			process.sampleCodes            = SampleHelper   .getSampleParent          (content.sampleCode);
			process.projectCodes           = SampleHelper   .getProjectParent         (process.sampleCodes);
			process.sampleOnInputContainer = InstanceHelpers.getSampleOnInputContainer(content, container);
			// need sampleOnInputContainer to generate code
			process.code = CodeHelper.getInstance().generateProcessCode(process);

			process.state = new State();
			process.state.code = "N";
			process.state.user = contextValidation.getUser();
			process.state.date = new Date();

			return process;
		}).collect(Collectors.toList());
	}
	
	public static List<Process> newProcessListFromSample(ContextValidation contextValidation, Process input) {
		Process process = input.cloneCommon();
		process.projectCodes = SampleHelper.getProjectParent(process.sampleCodes);
		if (process.sampleCodes.size() == 1) {
			process.sampleOnInputContainer = InstanceHelpers.getSampleOnInputContainer(process.sampleCodes.iterator().next());
		} else {
			logger.error("not only one sample code during process creation from sample");
			return null;
		}
		process.state = new State();
		process.state.code = "IW-C";
		process.state.user = contextValidation.getUser();
		process.state.date = new Date();
		// need sampleOnInputContainer to generate code
		process.code = CodeHelper.getInstance().generateProcessCode(process);
		return Arrays.asList(process);
	}
	
}

//package models.utils.instance;
//
//import static fr.cea.ig.play.IGGlobals.configuration;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.Date;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Set;
//import java.util.stream.Collectors;
//
//import org.mongojack.DBQuery;
//import org.mongojack.DBUpdate;
//
//import controllers.processes.api.Processes;
//import fr.cea.ig.MongoDBDAO;
//import models.laboratory.common.instance.State;
//import models.laboratory.container.instance.Container;
//import models.laboratory.container.instance.ContainerSupport;
//import models.laboratory.processes.description.ProcessType;
//import models.laboratory.processes.instance.Process;
//import models.utils.CodeHelper;
//import models.utils.InstanceConstants;
//import models.utils.InstanceHelpers;
//import rules.services.RulesServices6;
//import validation.ContextValidation;
//import validation.common.instance.CommonValidationHelper;
//import validation.container.instance.ContainerSupportValidationHelper;
//
//public class ProcessHelper {
//	
//	public static final play.Logger.ALogger logger = play.Logger.of(ProcessHelper.class);
//	
//	private static final String rulesKey() {
//		return configuration().getString("rules.key");	
//	}
//	
//	@Deprecated
//	public static void updateContainer(Container container, String typeCode, Set<String> codes, ContextValidation contextValidation) {
//		if (container.fromTransformationTypeCodes == null || container.fromTransformationTypeCodes.size() == 0) {
//			container.fromTransformationTypeCodes = new HashSet<>();
//			ProcessType processType;
////			try {
//				processType = ProcessType.find.get().findByCode(typeCode);
//				container.fromTransformationTypeCodes.add(processType.voidExperimentType.code);
////			} catch (DAOException e) {
////				throw new RuntimeException();
////			}
//		}
//		container.processTypeCodes = Collections.singleton(typeCode);
//		if (container.processCodes == null) { 
//			container.processCodes = new HashSet<>();
//		}
//		container.processCodes.addAll(codes);
//		
//		if (!contextValidation.hasErrors()) {
//			MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME, Container.class,
//					          DBQuery.is("code", container.code),
//					          DBUpdate.set("processCodes",                container.processCodes)
//					                  .set("processTypeCodes",            container.processTypeCodes)
//					                  .set("fromTransformationTypeCodes", container.fromTransformationTypeCodes));
//		}
//	}
//
//	@Deprecated
//	public static void updateContainerSupportFromContainer(Container container, ContextValidation contextValidation){
//		ContainerSupport containerSupport=MongoDBDAO.findByCode(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, ContainerSupport.class, container.support.code);
//		if (containerSupport != null) {
//			if (containerSupport.fromTransformationTypeCodes == null) {
//				containerSupport.fromTransformationTypeCodes = new HashSet<>();
//			}
//			containerSupport.fromTransformationTypeCodes.addAll(container.fromTransformationTypeCodes);
//			contextValidation.putObject(CommonValidationHelper.FIELD_STATE_CODE, container.state.code);
//			ContainerSupportValidationHelper.validateExperimentTypeCodes(contextValidation, containerSupport.fromTransformationTypeCodes);
//			if (!contextValidation.hasErrors()) {
//				MongoDBDAO.update(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME,ContainerSupport.class,
//						DBQuery.is("code", container.support.code)
//						,DBUpdate.set("fromTransformationTypeCodes",container.fromTransformationTypeCodes));
//			}
//		} else {
//			logger.error("Support container not exist = "+container.support.code);
//		}
//	}
//
//	public static Process applyRules(Process proc, ContextValidation ctx ,String rulesName){
//		ArrayList<Object> facts = new ArrayList<>();
//		facts.add(proc);
//		facts.add(ctx);
//		//List<Object> factsAfterRules = RulesServices6.getInstance().callRulesWithGettingFacts(Play.application().configuration().getString("rules.key"), rulesName, facts);
//		RulesServices6.getInstance().callRulesWithGettingFacts(rulesKey(), rulesName, facts);
//		return proc;
//	}
//	
//	public static List<Process>  applyRules(List<Process> processes, ContextValidation ctx ,String rulesName){
//		ArrayList<Object> facts = new ArrayList<>();
//		facts.add(ctx);
//		facts.addAll(processes);		
//		// List<Object> factsAfterRules = RulesServices6.getInstance().callRulesWithGettingFacts(Play.application().configuration().getString("rules.key"), rulesName, facts);
//		RulesServices6.getInstance().callRulesWithGettingFacts(rulesKey(), rulesName, facts);
//		return processes;
//	}	
//	
//	// ----------------------------------------------------------------------------
//	// This could be 2 methods:
//	//  - newProcessListFromContainer
//	//  - newProcessListFromSample
//	
//	@Deprecated
//	public static List<Process> getNewProcessList(ContextValidation contextValidation, Process input, String from) {
////		if ("from-container".equals(from)) {
//		if (Processes.FROM_CONTAINER.equals(from)) {
//			Container container = MongoDBDAO.findByCode(InstanceConstants.CONTAINER_COLL_NAME, Container.class, input.inputContainerCode);
//			return container.contents.parallelStream().map(content -> {
//					Process process                = input.cloneCommon();
//					process.sampleCodes            = SampleHelper   .getSampleParent          (content.sampleCode);
//					process.projectCodes           = SampleHelper   .getProjectParent         (process.sampleCodes);
//					process.sampleOnInputContainer = InstanceHelpers.getSampleOnInputContainer(content, container);
//					// need sampleOnInputContainer to generate code
//					process.code = CodeHelper.getInstance().generateProcessCode(process);
//					
//					process.state = new State();
//					process.state.code = "N";
//					process.state.user = contextValidation.getUser();
//					process.state.date = new Date();
//					
//					return process;
//				}).collect(Collectors.toList());
//		} else if (Processes.FROM_SAMPLE.equals(from)) {
//			Process process      = input.cloneCommon();
//			process.projectCodes = SampleHelper.getProjectParent(process.sampleCodes);
//			process.state        = new State();
//			process.state.code   = "IW-C";
//			process.state.user   = contextValidation.getUser();
//			process.state.date   = new Date();
//			// need sampleOnInputContainer to generate code
//			process.code         = CodeHelper.getInstance().generateProcessCode(process);
//			return Arrays.asList(process);
//		} else {
//			throw new RuntimeException("from :" + from + " not managed for the processes creation");
//		}
//	}
//	
//	public static List<Process> newProcessListFromContainer(ContextValidation contextValidation, Process input) {
//		Container container = MongoDBDAO.findByCode(InstanceConstants.CONTAINER_COLL_NAME, Container.class, input.inputContainerCode);
//		return container.contents.parallelStream().map(content -> {
//			Process process                = input.cloneCommon();
//			process.sampleCodes            = SampleHelper   .getSampleParent          (content.sampleCode);
//			process.projectCodes           = SampleHelper   .getProjectParent         (process.sampleCodes);
//			process.sampleOnInputContainer = InstanceHelpers.getSampleOnInputContainer(content, container);
//			// need sampleOnInputContainer to generate code
//			process.code = CodeHelper.getInstance().generateProcessCode(process);
//
//			process.state = new State();
//			process.state.code = "N";
//			process.state.user = contextValidation.getUser();
//			process.state.date = new Date();
//
//			return process;
//		}).collect(Collectors.toList());
//	}
//	
//	public static List<Process> newProcessListFromSample(ContextValidation contextValidation, Process input) {
//		Process process      = input.cloneCommon();
//		process.projectCodes = SampleHelper.getProjectParent(process.sampleCodes);
//		process.state        = new State();
//		process.state.code   = "IW-C";
//		process.state.user   = contextValidation.getUser();
//		process.state.date   = new Date();
//		// need sampleOnInputContainer to generate code
//		process.code         = CodeHelper.getInstance().generateProcessCode(process);
//		return Arrays.asList(process);
//	}
//	
//}
