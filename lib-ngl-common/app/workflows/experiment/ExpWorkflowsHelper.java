package workflows.experiment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.ngl.NGLApplication;
import models.laboratory.common.description.Level;
//import models.laboratory.common.description.Level.CODE;
import models.laboratory.common.description.PropertyDefinition;
import models.laboratory.common.instance.Comment;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TBoolean;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.container.description.ContainerSupportCategory;
import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.ContainerSupport;
import models.laboratory.container.instance.Content;
import models.laboratory.container.instance.QualityControlResult;
import models.laboratory.container.instance.StorageHistory;
import models.laboratory.container.instance.tree.From;
import models.laboratory.container.instance.tree.ParentContainers;
import models.laboratory.container.instance.tree.TreeOfLifeNode;
import models.laboratory.experiment.description.ExperimentCategory;
import models.laboratory.experiment.description.ExperimentType;
import models.laboratory.experiment.instance.AbstractContainerUsed;
import models.laboratory.experiment.instance.AtomicTransfertMethod;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.experiment.instance.InputContainerUsed;
import models.laboratory.experiment.instance.OutputContainerUsed;
import models.laboratory.instrument.description.InstrumentUsedType;
import models.laboratory.processes.description.ProcessType;
import models.laboratory.processes.instance.Process;
import models.laboratory.protocol.instance.Protocol;
import models.laboratory.sample.description.SampleType;
import models.laboratory.sample.instance.Sample;
import models.laboratory.sample.instance.tree.SampleLife;
import models.utils.CodeHelper;
import models.utils.InstanceConstants;
import models.utils.InstanceHelpers;
import models.utils.instance.ContainerHelper;
import models.utils.instance.ExperimentHelper;
import models.utils.instance.SampleHelper;
import play.Logger;
import rules.services.IDrools6Actor;
import validation.ContextValidation;
import validation.container.instance.ContainerValidationHelper;
import workflows.container.ContSupportWorkflows;
import workflows.container.ContWorkflows;
import workflows.process.ProcWorkflows;

@Singleton
public class ExpWorkflowsHelper {

	private static final play.Logger.ALogger logger = play.Logger.of(ExpWorkflowsHelper.class);

	private static final String NEW_PROCESS_CODES = "NEW_PROCESS_CODES";
	private static final String NEW_SAMPLE_CODES  = "NEW_SAMPLE_CODES";

	private final NGLApplication       app; 
	private final ContWorkflows        containerWorkflows;
	private final ContSupportWorkflows containerSupportWorkflows;
	private final ProcWorkflows        processWorkflows;
	//	private final LazyRules6Actor      rulesActor;
	private final IDrools6Actor        rulesActor;

	@Inject
	public ExpWorkflowsHelper(NGLApplication app, ContWorkflows containerWorkflows, ContSupportWorkflows containerSupportWorkflows, ProcWorkflows processWorkflows/*, ContentHelper contentHelper*/) {
		this.app                       = app;
		this.containerWorkflows        = containerWorkflows;
		this.containerSupportWorkflows = containerSupportWorkflows;
		this.processWorkflows          = processWorkflows;
		this.rulesActor                = app.rules6Actor();	
	}

	public void updateXCodes(Experiment exp) {
		Set<String> sampleCodes                      = new HashSet<>();
		Set<String> projectCodes                     = new HashSet<>();
		Set<String> inputContainerSupportCodes       = new HashSet<>();
		Set<String> inputContainerCodes              = new HashSet<>();
		Set<String> inputProcessCodes                = new HashSet<>();
		Set<String> inputFromTransformationTypeCodes = new HashSet<>();
		Set<String> inputProcessTypeCodes            = new HashSet<>();

		exp.atomicTransfertMethods
		.stream()
		//			.map(atm -> atm.inputContainerUseds)
		//			.flatMap(List::stream)
		.flatMap(atm -> atm.inputContainerUseds.stream())
		.forEach(inputContainer -> {
			inputContainerCodes             .add   (inputContainer.code);
			projectCodes                    .addAll(inputContainer.projectCodes);
			sampleCodes                     .addAll(inputContainer.sampleCodes);
			inputContainerSupportCodes      .add   (inputContainer.locationOnContainerSupport.code);
			inputProcessCodes               .addAll(inputContainer.processCodes);
			inputFromTransformationTypeCodes.addAll(inputContainer.fromTransformationTypeCodes);
			inputProcessTypeCodes           .addAll(inputContainer.processTypeCodes);
		});

		ExperimentType experimentType = ExperimentType.find.get().findByCode(exp.typeCode);
		if (experimentType.newSample) {
			exp.atomicTransfertMethods
			.stream()
			//				.map(atm -> atm.outputContainerUseds)
			//				.flatMap(List::stream)
			.flatMap(atm -> atm.outputContainerUseds.stream())
			.forEach(ocu -> {
				Map<String,PropertyValue> experimentProperties = ocu.experimentProperties;
				if (experimentProperties.containsKey("projectCode") 
						&& StringUtils.isNotBlank((String)experimentProperties.get("projectCode").value)) {
					projectCodes.add(experimentProperties.get("projectCode").value.toString());
				}

				if (experimentProperties.containsKey("sampleCode")
						&& StringUtils.isNotBlank((String)experimentProperties.get("sampleCode").value)) {
					logger.debug(" updateXCodes add "+ experimentProperties.get("sampleCode").value.toString() + " to experiment");
					sampleCodes.add(experimentProperties.get("sampleCode").value.toString());
				}						
			});
		}
		exp.projectCodes                     = projectCodes;		
		exp.sampleCodes                      = sampleCodes;
		exp.inputContainerSupportCodes       = inputContainerSupportCodes;		
		exp.inputContainerCodes              = inputContainerCodes;
		exp.inputProcessCodes                = inputProcessCodes;
		exp.inputProcessTypeCodes            = inputProcessTypeCodes;
		exp.inputFromTransformationTypeCodes = inputFromTransformationTypeCodes;
		MongoDBDAO.update(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, 
				DBQuery.is("code", exp.code),
				DBUpdate.set("projectCodes",                     exp.projectCodes)
				.set("sampleCodes",                      exp.sampleCodes)
				.set("inputContainerSupportCodes",       exp.inputContainerSupportCodes)
				.set("inputContainerCodes",              exp.inputContainerCodes)
				.set("inputProcessCodes",                exp.inputProcessCodes)
				.set("inputProcessTypeCodes",            exp.inputProcessTypeCodes)
				.set("inputFromTransformationTypeCodes", exp.inputFromTransformationTypeCodes));
	}

	/**
	 * Update the output container and output container support codes for the given
	 * experiment in the database.
	 * @param exp experiment to update
	 */
	public void updateOutputContainerCodes(Experiment exp) {
		Set<String> outputContainerSupportCodes = new HashSet<>();
		Set<String> outputContainerCodes        = new HashSet<>();

		exp.atomicTransfertMethods
		.stream()
		.filter(atm -> atm.outputContainerUseds != null)
		.map(atm -> atm.outputContainerUseds)
		.flatMap(List::stream)
		.forEach(outputContainer -> {
			outputContainerCodes       .add(outputContainer.code);
			outputContainerSupportCodes.add(outputContainer.locationOnContainerSupport.code);
		});
		if (CollectionUtils.isNotEmpty(outputContainerCodes)) {
			exp.outputContainerCodes        = outputContainerCodes;
			exp.outputContainerSupportCodes = outputContainerSupportCodes;

			MongoDBDAO.update(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, 
					DBQuery.is("code", exp.code),
					DBUpdate.set("outputContainerSupportCodes", exp.outputContainerSupportCodes)
					.set("outputContainerCodes", exp.outputContainerCodes));
		}
	}

	// -----------------------------------------------------------------------------

	/**
	 * Update experiment input containers.
	 * @param exp       experiment
	 * @param nextState next state
	 * @param ctxVal    validation context
	 * @deprecated use {@link #updateStateOfInputContainers(ContextValidation, Experiment, State)}
	 */
	@Deprecated
	public void updateStateOfInputContainers(Experiment exp, State nextState, ContextValidation ctxVal) {
		updateStateOfInputContainers(ctxVal, exp, nextState);
	}

	/**
	 * Update experiment input containers.
	 * @param ctxVal    validation context
	 * @param exp       experiment
	 * @param nextState next state
	 */
	@Deprecated
	public void updateStateOfInputContainers(ContextValidation ctxVal, Experiment exp, State nextState) {
		// ctxVal.putObject(CommonValidationHelper.FIELD_STATE_CONTAINER_CONTEXT, "workflow");
		//		MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class, DBQuery.in("code", exp.inputContainerCodes))
		//		          .cursor
		//		          .forEach(c -> containerWorkflows.setState(ctxVal, c, nextState, "workflow"));		
		//		ctxVal.removeObject(CommonValidationHelper.FIELD_STATE_CONTAINER_CONTEXT);
		boolean updateContainerSupports = Boolean.TRUE.equals(ctxVal.getObject(ContainerValidationHelper.FIELD_UPDATE_CONTAINER_SUPPORT_STATE));
		updateStateOfInputContainers(ctxVal, exp, nextState, updateContainerSupports);
	}

	public void updateStateOfInputContainers(ContextValidation ctxVal, Experiment exp, State nextState, boolean updateContainerSupports) {
		MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class, DBQuery.in("code", exp.inputContainerCodes))
		.cursor
		.forEach(c -> containerWorkflows.setState(ctxVal, c, nextState, "workflow", updateContainerSupports));		
		ctxVal.removeObject(ContainerValidationHelper.FIELD_STATE_CONTAINER_CONTEXT);
	}

	// -----------------------------------------------------------------------------

	public void updateStateOfInputContainerSupports(Experiment exp, ContextValidation ctxVal) {
		ctxVal.putObject(ContainerValidationHelper.FIELD_STATE_CONTAINER_CONTEXT, "workflow");
		MongoDBDAO.find(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, ContainerSupport.class, DBQuery.in("code", exp.inputContainerSupportCodes))
		.cursor
		.forEach(c -> containerSupportWorkflows.setStateFromContainers(ctxVal, c, "workflow"));
		ctxVal.removeObject(ContainerValidationHelper.FIELD_STATE_CONTAINER_CONTEXT);
	}

	public void updateStateOfProcesses(Experiment exp, State nextState, ContextValidation ctxVal) {
		MongoDBDAO.find(InstanceConstants.PROCESS_COLL_NAME, Process.class,DBQuery.in("code", exp.inputProcessCodes))
		.cursor
		.forEach(c -> processWorkflows.setState(ctxVal, c, nextState));				
	}

	public void linkExperimentWithProcesses(Experiment exp, ContextValidation validation) {
		MongoDBDAO.update(InstanceConstants.PROCESS_COLL_NAME,Process.class,
				DBQuery.in("code", exp.inputProcessCodes)
				.notEquals("state.code", "F")
				.notIn("experimentCodes", exp.code), 
				DBUpdate.set("currentExperimentTypeCode", exp.typeCode)
				.push("experimentCodes", exp.code),
				true);

	}

	// _CTX_PARAM
	public void updateAddContainersToExperiment(Experiment expFromUser, ContextValidation ctxVal, State nextState) {
		boolean updateContainerSupports = Boolean.TRUE.equals(ctxVal.getObject(ContainerValidationHelper.FIELD_UPDATE_CONTAINER_SUPPORT_STATE));
		updateAddContainersToExperiment(expFromUser, ctxVal, nextState, updateContainerSupports);
	}

	public void updateAddContainersToExperiment(Experiment expFromUser, ContextValidation ctxVal, State nextState, boolean updateContainerSupports) {
		Experiment expFromDB = MongoDBDAO.findByCode(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, expFromUser.code);

		List<String> newContainerCodes = getNewContainerCodes(expFromDB, expFromUser);
		if (newContainerCodes.size() > 0) {
			Set<String> newContainerSupportCodes = new TreeSet<>();
			Set<String> newProcessCodes          = new TreeSet<>();
			ctxVal.putObject(ContainerValidationHelper.FIELD_STATE_CONTAINER_CONTEXT, "workflow");
			MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class, DBQuery.in("code", newContainerCodes))
			.cursor
			.forEach(c -> {				
				newContainerSupportCodes.add(c.support.code);
				newProcessCodes.addAll(c.processCodes);
				containerWorkflows.setState(ctxVal, c, nextState, "workflow", updateContainerSupports);
			});

			MongoDBDAO.find(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, ContainerSupport.class,DBQuery.in("code", newContainerSupportCodes))
			.cursor
			.forEach(c -> {				
				containerSupportWorkflows.setStateFromContainers(ctxVal, c, "workflow");
			});

			MongoDBDAO.update(InstanceConstants.PROCESS_COLL_NAME, Process.class, 
					DBQuery.in("code", newProcessCodes)
					.notEquals("state.code", "F"), 
					DBUpdate.set("currentExperimentTypeCode", expFromDB.typeCode)
					.push("experimentCodes", expFromDB.code));			

			ctxVal.removeObject(ContainerValidationHelper.FIELD_STATE_CONTAINER_CONTEXT);
		}
	}

	public void updateRemoveContainersFromExperiment(Experiment expFromUser,	ContextValidation ctxVal, State containerNextState) {
		Experiment expFromDB = MongoDBDAO.findByCode(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, expFromUser.code);

		Set<String> removeContainerCodes = getRemoveContainerCodes(expFromDB, expFromUser);
		if (removeContainerCodes.size() > 0) {
			rollbackOnContainers(ctxVal, containerNextState, expFromDB.code,	removeContainerCodes);
		}
	}

	// _CTX_PARAM
	public void rollbackOnContainers(ContextValidation ctxVal, State containerNextState, String expCode, Set<String> removeContainerCodes) {
		boolean updateContainerSupports = Boolean.TRUE.equals(ctxVal.getObject(ContainerValidationHelper.FIELD_UPDATE_CONTAINER_SUPPORT_STATE));
		rollbackOnContainers(ctxVal, containerNextState, expCode, removeContainerCodes, updateContainerSupports);
	}

	public void rollbackOnContainers(ContextValidation ctxVal, State containerNextState, String expCode, Set<String> removeContainerCodes, boolean updateContainerSupports) {
		Set<String> removeContainerSupportCodes = new TreeSet<>();
		Set<String> removeProcessCodes = new TreeSet<>();
		ctxVal.putObject(ContainerValidationHelper.FIELD_STATE_CONTAINER_CONTEXT, "workflow");
		MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class, DBQuery.in("code", removeContainerCodes)).cursor
		.forEach(c -> {
			removeContainerSupportCodes.add(c.support.code);
			removeProcessCodes.addAll(c.processCodes);
			containerWorkflows.setState(ctxVal, c, containerNextState, "workflow", updateContainerSupports);
		});

		MongoDBDAO.find(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, ContainerSupport.class, DBQuery.in("code", removeContainerSupportCodes)).cursor
		.forEach(c -> containerSupportWorkflows.setStateFromContainers(ctxVal, c, "workflow"));

		MongoDBDAO.update(InstanceConstants.PROCESS_COLL_NAME, Process.class, 
				DBQuery.in("code", removeProcessCodes).notEquals("state.code", "F"), 
				DBUpdate.unset("currentExperimentTypeCode").pull("experimentCodes", expCode));
		ctxVal.removeObject(ContainerValidationHelper.FIELD_STATE_CONTAINER_CONTEXT);
	}

	// ----------------------------------------------------------------------
	// There is a way to write the difference in a simpler way.

	/**
	 * Get the container codes that are in the new experiment and not in the
	 * old.
	 * @param expFromDB   old experiment (from DB)
	 * @param expFromUser new experiment (from user) 
	 * @return            list of new container codes
	 */
	private List<String> getNewContainerCodes(Experiment expFromDB, Experiment expFromUser) {
		List<String> containerCodesFromDB   = 
				ExperimentHelper.getAllInputContainers(expFromDB)
				.stream()
				.map((InputContainerUsed c) -> c.code)
				.collect(Collectors.toList());
		List<String> containerCodesFromUser = 
				ExperimentHelper.getAllInputContainers(expFromUser)
				.stream()
				.map((InputContainerUsed c) -> c.code)
				.collect(Collectors.toList());

		List<String> newContainersCodes = new ArrayList<>();
		for (String codeFromDB : containerCodesFromUser) {
			if (!containerCodesFromDB.contains(codeFromDB)) {
				newContainersCodes.add(codeFromDB);
			}
		}		
		return newContainersCodes;
	}

	/**
	 * Get the container codes that are in the new experiment and not in the
	 * old.
	 * @param expFromDB   old experiment (from DB)
	 * @param expFromUser new experiment (from user) 
	 * @return            list of new container codes
	 */
	protected List<String> getNewContainerCodes_(Experiment expFromDB, Experiment expFromUser) {
		Set<String> oldCodes   = ExperimentHelper.getAllInputContainers(expFromDB)
				.stream()
				.map((InputContainerUsed c) -> c.code)
				.collect(Collectors.toSet());
		return ExperimentHelper.getAllInputContainers(expFromUser)
				.stream()
				.map(c -> c.code)
				.filter(c -> !oldCodes.contains(c))
				.collect(Collectors.toList());
	}

	// ------------------------------------------------------------------

	private Set<String> getRemoveContainerCodes(Experiment expFromDB, Experiment expFromUser) {
		List<String> containerCodesFromDB = ExperimentHelper.getAllInputContainers(expFromDB).stream().map((InputContainerUsed c) -> c.code).collect(Collectors.toList());
		List<String> containerCodesFromUser = ExperimentHelper.getAllInputContainers(expFromUser).stream().map((InputContainerUsed c) -> c.code).collect(Collectors.toList());

		Set<String> removeContainersCodes = new TreeSet<>();
		for(String codeFromDB:containerCodesFromDB){
			if(!containerCodesFromUser.contains(codeFromDB)){
				removeContainersCodes.add(codeFromDB);
			}
		}

		return removeContainersCodes;
	}

	/*
	 * Update ouput container code but not generate if null
	 * Used when user change plate line or column
	 * @param exp
	 */
	public void updateOutputContainerCode(Experiment exp) {
		ContainerSupportCategory outputCsc = ContainerSupportCategory.find.get().findByCode(exp.instrument.outContainerSupportCategoryCode);
		exp.atomicTransfertMethods.forEach((AtomicTransfertMethod atm) -> atm.updateOutputCodeIfNeeded(outputCsc, null));
	}

	/*
	 * Update only content
	 * @param exp
	 */
	public void updateATMContainerContents(Experiment exp) {
		exp.atomicTransfertMethods.forEach(atm -> {
			if (ExperimentCategory.CODE.qualitycontrol.toString().equals(exp.categoryCode)) {
				updateInputContainerUsedContents(exp, atm);
			} else {
				updateOutputContainerUsedContents(exp, atm);					
			}
		});					
	}

	/*
	 * Update OutputContainerUsed :
	 * 		- generate ContainerSupportCode and ContainerCode if needed
	 * 		- populate content, projectCodes, sampleCodes, fromTransformationTypeCodes, processTypeCodes, inputProcessCodes
	 * 		- remove empty volume, quantity, concentration
	 * 
	 * !! missing populate properties on container !!		
	 * 
	 * @param exp
	 * @param validation
	 */
	public void updateATMs(Experiment exp, boolean justContainerCode) {
		ContainerSupportCategory outputCsc = ContainerSupportCategory.find.get().findByCode(exp.instrument.outContainerSupportCategoryCode);
		//TODO EJ appel même méthode condition inutile
		if (outputCsc.nbLine.equals(Integer.valueOf(1)) && outputCsc.nbColumn.equals(Integer.valueOf(1))) {
			exp.atomicTransfertMethods.forEach(atm -> updateOutputContainerUsed(exp, atm, outputCsc, CodeHelper.getInstance().generateContainerSupportCode(), justContainerCode));
		} else if (!outputCsc.nbLine.equals(Integer.valueOf(1)) || !outputCsc.nbColumn.equals(Integer.valueOf(1))) {
			String supportCode = CodeHelper.getInstance().generateContainerSupportCode();
			exp.atomicTransfertMethods.forEach(atm -> updateOutputContainerUsed(exp, atm, outputCsc, supportCode, justContainerCode));
		}	

		MongoDBDAO.update(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, DBQuery.is("code", exp.code), DBUpdate.set("atomicTransfertMethods", exp.atomicTransfertMethods));
	}

	private void updateOutputContainerUsed(Experiment exp, AtomicTransfertMethod atm, ContainerSupportCategory outputCsc, String supportCode, boolean justContainerCode) {
		if (atm.outputContainerUseds != null) {
			atm.updateOutputCodeIfNeeded(outputCsc, supportCode);
			if (!justContainerCode) {
				updateOutputContainerUsedContents(exp, atm);
			}
		}		
	}

	private void updateOutputContainerUsedContents(Experiment exp, AtomicTransfertMethod atm) {
		if (atm.outputContainerUseds != null) {
			atm.outputContainerUseds.forEach((OutputContainerUsed ocu) -> {
				ocu.contents = getContents(exp, atm, ocu);				
			});
		}
	}

	private void updateInputContainerUsedContents(Experiment exp, AtomicTransfertMethod atm) {
		if (atm.inputContainerUseds != null) {
			atm.inputContainerUseds.forEach((InputContainerUsed icu) -> {
				Map<String, PropertyValue> newContentProperties = getCommonPropertiesForALevelWithICU(exp, icu, Level.CODE.Content);
				icu.contents.forEach(content -> {
					content.properties.putAll(newContentProperties);
				});
			});
		}
	}

	private Set<String> getFromTransformationTypeCodes(Experiment exp, AtomicTransfertMethod atm) {
		Set<String> _fromExperimentTypeCodes = new HashSet<>(0);
		if (ExperimentCategory.CODE.transformation.equals(ExperimentCategory.CODE.valueOf(exp.categoryCode))) {
			_fromExperimentTypeCodes.add(exp.typeCode);
		} else {
			_fromExperimentTypeCodes = atm.inputContainerUseds.stream().map((InputContainerUsed icu) -> icu.fromTransformationTypeCodes).flatMap(Set::stream).collect(Collectors.toSet());
		}
		return _fromExperimentTypeCodes;
	}

	private Set<String> getFromTransformationCodes(Experiment exp, AtomicTransfertMethod atm) {
		Set<String> _fromExperimentCodes = new HashSet<>(0);
		if (ExperimentCategory.CODE.transformation.equals(ExperimentCategory.CODE.valueOf(exp.categoryCode))) {
			_fromExperimentCodes.add(exp.code);
		} else {
			_fromExperimentCodes = atm.inputContainerUseds.stream()
					.filter((InputContainerUsed icu) -> icu.fromTransformationCodes != null)
					.map((InputContainerUsed icu) -> icu.fromTransformationCodes)
					.flatMap(Set::stream)
					.collect(Collectors.toSet());
		}
		return _fromExperimentCodes;
	}

	private String getFromSatTypeCode(Experiment exp, ExperimentCategory.CODE code) {
		if (code.equals(ExperimentCategory.CODE.valueOf(exp.categoryCode))) {
			return exp.typeCode;
		}
		return null;
	}

	private String getFromSatCode(Experiment exp, ExperimentCategory.CODE code) {
		if (code.equals(ExperimentCategory.CODE.valueOf(exp.categoryCode))) {
			return exp.code;
		}
		return null;
	}

	private List<Content> getContents(Experiment exp, AtomicTransfertMethod atm, OutputContainerUsed ocu) {
		List<Content> contents =  atm.inputContainerUseds.stream()
				.map((InputContainerUsed icu) -> {
					Map<String, PropertyValue> contentProperties = getInputPropertiesForALevel(exp, icu, Level.CODE.Content);
					List<Content> newContents = ContainerHelper.calculPercentageContent(icu.contents, icu.percentage);
					newContents.forEach(c -> c.properties.putAll(contentProperties));
					return newContents;
				})
				.flatMap(List::stream)
				.collect(Collectors.toCollection(ArrayList::new));
		contents = ContainerHelper.fusionContents(contents);
		Map<String, PropertyValue> newContentProperties = getCommonPropertiesForALevel(exp, Level.CODE.Content);
		newContentProperties.putAll(getOutputPropertiesForALevel(exp, ocu, Level.CODE.Content));
		contents.forEach(c -> c.properties.putAll(newContentProperties));
		return contents;
	}

	/*
	 * Generate Support and container and save it in MongoDB
	 * Add the support code inside processes
	 * Il only one error during validation process all object are delete from MongoDB
	 * @param exp
	 * @param validation
	 */
	public void createOutputContainerSupports(Experiment exp, ContextValidation validation) {
		app.parallelRun(() -> { 
			Logger.info("create outputContainerSupport in DB for exp "+exp.code);
			TraceInformation traceInformation = new TraceInformation(validation.getUser());
			validation.putObject(NEW_PROCESS_CODES, new HashSet<String>());

			Map<String, List<Container>> containersBySupportCode = exp.atomicTransfertMethods
					.parallelStream()
					.map(atm -> createOutputContainers(exp, atm, validation))
					.flatMap(List::stream)
					.collect(Collectors.groupingBy(c -> c.support.code));

			//soit 1 seul support pour tous les atm
			//soit autant de support que d'atm

			ContextValidation supportsValidation = ContextValidation.createCreationContext(validation.getUser());
			ExperimentType experimentType = ExperimentType.find.get().findByCode(exp.typeCode);
			List<String> processusWithNewProjectCode = new ArrayList<>();

			containersBySupportCode.entrySet().forEach(entry -> {
				List<Container> containers = entry.getValue();
				ContainerSupport support = createContainerSupport(entry.getKey(), containers, validation);
				// GA: extract only properties from exp and inst not from atm => must be improve
				support.properties = getCommonPropertiesForALevel(exp, Level.CODE.ContainerSupport); 
				support.validate(supportsValidation);

				if (!supportsValidation.hasErrors()) {
					support.traceInformation = traceInformation;
					MongoDBDAO.save(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, support);
					containers.parallelStream()
					.forEach(container -> {
						ContextValidation containerValidation = ContextValidation.createCreationContext(validation.getUser());
						container.validate(containerValidation, null, null);
						if(!containerValidation.hasErrors()){
							container.traceInformation = traceInformation;
							MongoDBDAO.save(InstanceConstants.CONTAINER_COLL_NAME, container);
							MongoDBDAO.update(InstanceConstants.PROCESS_COLL_NAME, Process.class, 
									DBQuery.in("code", container.processCodes).notIn("outputContainerSupportCodes", container.support.code),
									DBUpdate.push("outputContainerSupportCodes",container.support.code));
							MongoDBDAO.update(InstanceConstants.PROCESS_COLL_NAME, Process.class, 
									DBQuery.in("code", container.processCodes).notIn("outputContainerCodes", container.code),
									DBUpdate.push("outputContainerCodes",container.code));

							if (experimentType.newSample) {
								boolean projectCodeNotExist = MongoDBDAO.checkObjectExist(InstanceConstants.PROCESS_COLL_NAME, Process.class, 
										DBQuery.in("code", container.processCodes).notIn("projectCodes", container.projectCodes));

								if (projectCodeNotExist) {
									processusWithNewProjectCode.add(container.code);
									MongoDBDAO.update(InstanceConstants.PROCESS_COLL_NAME, Process.class, 
											DBQuery.in("code", container.processCodes).notIn("projectCodes", container.projectCodes),
											DBUpdate.push("projectCodes",container.projectCodes.iterator().next()));
								}
								MongoDBDAO.update(InstanceConstants.PROCESS_COLL_NAME, Process.class, 
										DBQuery.in("code", container.processCodes),
										DBUpdate.push("sampleCodes",container.sampleCodes.iterator().next()));
							}
						} else {
							supportsValidation.addErrors(containerValidation.getErrors());
						}

					});
				}	
			});		
			//delete all supports and containers if only one error
			if (supportsValidation.hasErrors()) {
				containersBySupportCode.entrySet().parallelStream()
				.forEach(entry -> {
					MongoDBDAO.update(InstanceConstants.PROCESS_COLL_NAME, Process.class,
							DBQuery.in("outputContainerSupportCodes", entry.getKey()),
							DBUpdate.pull("outputContainerSupportCodes",entry.getKey()));
					MongoDBDAO.delete(InstanceConstants.CONTAINER_COLL_NAME, Container.class, DBQuery.is("support.code", entry.getKey()));
					MongoDBDAO.delete(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, ContainerSupport.class, DBQuery.is("code", entry.getKey()));

					List<Container> containers = entry.getValue();
					containers.parallelStream()
					.forEach(container -> {
						//remove outputContainerCodes from Process
						MongoDBDAO.update(InstanceConstants.PROCESS_COLL_NAME, Process.class,
								DBQuery.in("code", container.processCodes).in("outputContainerCodes",container.code),
								DBUpdate.pull("outputContainerCodes",container.code));
						//Retract sampleCode/projectCode of new container from a new sample
						if (experimentType.newSample ){
							MongoDBDAO.update(InstanceConstants.PROCESS_COLL_NAME, Process.class, 
									DBQuery.in("code", container.processCodes),
									DBUpdate.pull("sampleCodes",container.sampleCodes.iterator().next()));
							if(processusWithNewProjectCode.contains(container.code)){
								logger.debug("Pull container "+container.code+", projectCodes "+container.projectCodes.iterator().next());
								MongoDBDAO.update(InstanceConstants.PROCESS_COLL_NAME, Process.class, 
										DBQuery.in("code", container.processCodes),
										DBUpdate.pull("projectCodes",container.projectCodes.iterator().next()));
							}
						}
					});

				});
				Set<String> newProcessCodes = validation.getTypedObject(NEW_PROCESS_CODES);
				if (newProcessCodes != null) {
					MongoDBDAO.delete(InstanceConstants.PROCESS_COLL_NAME, Process.class, DBQuery.in("code", newProcessCodes));
				}
				validation.addErrors(supportsValidation.getErrors());
			}			
		});
	}

	public void deleteOutputContainerSupports(Experiment exp, ContextValidation validation) {
		if(null != exp.outputContainerSupportCodes){
			exp.outputContainerSupportCodes.forEach(code -> {
				MongoDBDAO.update(InstanceConstants.PROCESS_COLL_NAME, Process.class,
						DBQuery.in("outputContainerSupportCodes", code),
						DBUpdate.pull("outputContainerSupportCodes",code));
				MongoDBDAO.delete(InstanceConstants.CONTAINER_COLL_NAME, Container.class, DBQuery.is("support.code", code));
				MongoDBDAO.delete(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, ContainerSupport.class, DBQuery.is("code", code));
			});

			exp.outputContainerCodes.forEach(code -> {
				MongoDBDAO.update(InstanceConstants.PROCESS_COLL_NAME, Process.class,
						DBQuery.in("outputContainerCodes", code),
						DBUpdate.pull("outputContainerCodes",code));				
			});

			//			Set<String> newProcessCodes = (Set<String>)validation.getObject(NEW_PROCESS_CODES);
			Set<String> newProcessCodes = validation.getTypedObject(NEW_PROCESS_CODES);
			if (newProcessCodes != null && newProcessCodes.size() > 0) {
				MongoDBDAO.delete(InstanceConstants.PROCESS_COLL_NAME, Process.class, DBQuery.in("code", newProcessCodes));
			}
		}
	}

	public void deleteSamplesIfNeeded(Experiment exp, ContextValidation validation) {
		ExperimentType experimentType=ExperimentType.find.get().findByCode(exp.typeCode);
		if(experimentType.newSample){	
			//Get only new sample created in current state
			Set<String> newSampleCodes = validation.getTypedObject(NEW_SAMPLE_CODES);
			if (newSampleCodes != null && newSampleCodes.size() > 0) {
				logger.debug("Nb newSampleCodes :"+newSampleCodes.size());
				List<Sample> samples = MongoDBDAO.find(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, DBQuery.in("code", newSampleCodes)).toList();
				logger.debug("newSampleCodes list:"+ samples);

				Set<String> projectCodes = samples.stream().map(s -> s.projectCodes).flatMap(Set::stream).collect(Collectors.toSet());
				MongoDBDAO.delete(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, DBQuery.in("code", newSampleCodes));
				projectCodes.parallelStream().forEach(projectCode -> {
					CodeHelper.getInstance().updateProjectSampleCodeWithLastSampleCode(projectCode);				
				});
				newSampleCodes.forEach(newSampleCode -> {
					MongoDBDAO.update(InstanceConstants.PROCESS_COLL_NAME, Process.class, DBQuery.in("sampleCodes", newSampleCode), DBUpdate.pull("sampleCodes", newSampleCode));
				});

				// GA: Analyse if need to update lastSampleCode on project with the real exist sample. 

				//NGL-2830 : Clean the output container and replace new sample by old sample
				//clean only newCreatedSample in current state workflow 
				exp.atomicTransfertMethods.forEach(atm->{
					//Get inputContainerUsed size must be one for Experiment type newSample
					if (atm.inputContainerUseds.size() == 1 && atm.inputContainerUseds.get(0).contents.size() == 1) {
						String inputContentSampleCode = atm.inputContainerUseds.get(0).contents.get(0).sampleCode;
						String inputContentProjectCode = atm.inputContainerUseds.get(0).contents.get(0).projectCode;
						atm.outputContainerUseds.forEach(ocu->{
							if(ocu.experimentProperties.containsKey("sampleCode") && newSampleCodes.contains(ocu.experimentProperties.get("sampleCode").value.toString())){
								//Remove experimentproperties sampleCode and projectCode for creation new sample
								//ocu.experimentProperties.remove("projectCode");
								ocu.experimentProperties.remove("sampleCode");
								//Update content sampleCode and projectCode for validation experiment before creation new sample
								ocu.contents.forEach(c->{
									c.sampleCode=inputContentSampleCode;
									c.projectCode=inputContentProjectCode;
								});
							}
						});
					}

				});

				//Update experiment cleaned
				MongoDBDAO.update(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, DBQuery.is("code", exp.code), 
						DBUpdate.set("atomicTransfertMethods", exp.atomicTransfertMethods));
			}
		}


	}


	private ContainerSupport createContainerSupport(String code, List<Container> containers, ContextValidation validation) {
		validation.addKeyToRootKeyName("creation.outputSupport."+code);
		ContainerSupport support = new ContainerSupport();
		support.code = code;
		support.state = new State("N", validation.getUser());
		support.traceInformation  = new TraceInformation(validation.getUser());
		support.categoryCode = getSupportCategoryCode(containers, validation);
		support.storageCode = getSupportStorageCode(containers, validation);
		support.storages = getNewStorages(support, validation);
		support.projectCodes = containers.stream().map(c -> c.projectCodes).flatMap(Set::stream).collect(Collectors.toSet());
		support.sampleCodes = containers.stream().map(c -> c.sampleCodes).flatMap(Set::stream).collect(Collectors.toSet());
		support.fromTransformationTypeCodes = containers.stream().map(c -> c.fromTransformationTypeCodes).flatMap(Set::stream).collect(Collectors.toSet());

		support.nbContainers = containers.size();
		support.nbContents = containers.stream().mapToInt(c -> c.contents.size()).sum();

		validation.removeKeyFromRootKeyName("creation.outputSupport."+code);
		return support;
	}

	private static List<StorageHistory> getNewStorages(ContainerSupport support, ContextValidation validation) {
		List<StorageHistory> storages = null;
		if(null != support.storageCode){
			storages = new ArrayList<>();
			StorageHistory sh = getStorageHistory(support.storageCode, storages.size(),validation.getUser());
			storages.add(sh);

		}
		return storages;

	}

	private static List<StorageHistory> updateStorages(ContainerSupport support, ContextValidation validation) {
		if(null != support.storageCode){
			if(support.storages == null){
				support.storages= new ArrayList<>();
			}
			StorageHistory sh = getStorageHistory(support.storageCode, support.storages.size(),validation.getUser());
			support.storages.add(sh);			
		}	
		return support.storages;
	}

	private static StorageHistory getStorageHistory(String storageCode, Integer index, String user) {
		StorageHistory sh = new StorageHistory();
		sh.code = storageCode;
		sh.date = new Date();
		sh.user = user;
		sh.index = index;
		return sh;
	}

	public static void updateStateOfInputContainers_(ExpWorkflowsHelper expWorkflowsHelper, Experiment exp,
			State nextState, ContextValidation ctxVal) {
		expWorkflowsHelper.updateStateOfInputContainers(ctxVal, exp, nextState);
	}

	private String getSupportCategoryCode(List<Container> containers, ContextValidation validation) {
		Set<String> categoryCodes = containers.stream().map(c -> c.support.categoryCode).collect(Collectors.toSet());
		if (categoryCodes.size() == 1) {
			return categoryCodes.iterator().next();
		} else {
			validation.addError("categoryCode","different for several containers");
			return null;
		}
	}

	private String getSupportStorageCode(List<Container> containers, ContextValidation validation) {
		Set<String> storageCodes = containers.stream().map(c -> {
										if (StringUtils.isNotBlank(c.support.storageCode)) {
											return c.support.storageCode;
										} else {
											return null;
										}
								   })
								   .collect(Collectors.toSet());
		if (storageCodes.size() == 1) {
			return storageCodes.iterator().next();
		} else {
			//validation.addErrors("storageCode","different for several containers");
			return null;
		}
	}

	private List<Container> createOutputContainers(Experiment exp, AtomicTransfertMethod atm, ContextValidation validation) {
		Set<String> fromTransformationTypeCodes = getFromTransformationTypeCodes(exp, atm);
		Set<String> fromTransformationCodes = getFromTransformationCodes(exp, atm);

		String fromPurificationTypeCode = getFromSatTypeCode(exp, ExperimentCategory.CODE.purification);
		String fromPurificationCode = getFromSatCode(exp, ExperimentCategory.CODE.purification);

		String fromTransfertTypeCode =  getFromSatTypeCode(exp, ExperimentCategory.CODE.transfert);
		String fromTransfertCode = getFromSatCode(exp, ExperimentCategory.CODE.transfert);

		Map<String, PropertyValue> containerProperties = getCommonPropertiesForALevel(exp, Level.CODE.Container);
		TreeOfLifeNode tree = getTreeOfLifeNode(exp, atm);

		Set<String> processTypeCodes =new HashSet<>();
		Set<String> inputProcessCodes =new HashSet<>();

		atm.inputContainerUseds.forEach(icu -> {
			processTypeCodes.addAll(icu.processTypeCodes);
			inputProcessCodes.addAll(icu.processCodes);
		});

		State state = new State("N", validation.getUser());
		TraceInformation traceInformation = new TraceInformation(validation.getUser());
		List<Container> newContainers = new ArrayList<>();
		if (atm.outputContainerUseds != null && atm.outputContainerUseds.size() != 0) {
			OutputContainerUsed ocu = atm.outputContainerUseds.get(0);

			Container c = new Container();
			c.code = ocu.code;
			c.categoryCode = ocu.categoryCode;
			c.contents = ocu.contents;
			c.support = ocu.locationOnContainerSupport;
			Map<String, PropertyValue> outputContainerProperties = getOutputPropertiesForALevel(exp, ocu, Level.CODE.Container);
			outputContainerProperties.putAll(containerProperties);
			c.properties = outputContainerProperties;
			c.concentration = getNullIfNoValue(ocu.concentration);
			c.quantity = getNullIfNoValue(ocu.quantity);
			c.volume = getNullIfNoValue(ocu.volume);
			c.size = getNullIfNoValue(ocu.size);
			c.projectCodes = getProjectsFromContents(c.contents);
			c.sampleCodes = getSamplesFromContents(c.contents);
			c.fromTransformationTypeCodes = fromTransformationTypeCodes;
			c.fromTransformationCodes = fromTransformationCodes;
			c.fromPurificationCode = fromPurificationCode;
			c.fromPurificationTypeCode = fromPurificationTypeCode;
			c.fromTransfertCode = fromTransfertCode;
			c.fromTransfertTypeCode = fromTransfertTypeCode;
			c.processTypeCodes = processTypeCodes;
			c.processCodes = inputProcessCodes;
			c.state = state;
			c.traceInformation = traceInformation;
			c.treeOfLife=tree;
			if (ocu.comment != null) {
				c.comments = Collections.singletonList(updateComment(ocu.comment, validation));
			}
			newContainers.add(c);
		}
		//if oneToMany you need to create new processes for each new additional container
		if (atm.outputContainerUseds != null && atm.outputContainerUseds.size() > 1) {
			//Set<String> allNewInputProcessCodes = new HashSet<String>();
			List<OutputContainerUsed> outputContainerUseds = atm.outputContainerUseds.subList(1, atm.outputContainerUseds.size());
			outputContainerUseds.forEach((OutputContainerUsed ocu) ->{
				Set<String> newInputProcessCodes = duplicateProcesses(inputProcessCodes);
				//				((Set<String>)validation.getObject(NEW_PROCESS_CODES)).addAll(newInputProcessCodes);
				validation.<Set<String>>getTypedObject(NEW_PROCESS_CODES).addAll(newInputProcessCodes);
				Container c = new Container();
				c.code = ocu.code;
				c.categoryCode = ocu.categoryCode;
				c.contents = ocu.contents;
				c.support = ocu.locationOnContainerSupport;
				Map<String, PropertyValue> outputContainerProperties = getOutputPropertiesForALevel(exp, ocu, Level.CODE.Container);
				outputContainerProperties.putAll(containerProperties);
				c.properties = outputContainerProperties;
				c.concentration = getNullIfNoValue(ocu.concentration);
				c.quantity = getNullIfNoValue(ocu.quantity);
				c.volume = getNullIfNoValue(ocu.volume);
				c.size = getNullIfNoValue(ocu.size);
				c.projectCodes = getProjectsFromContents(c.contents);
				c.sampleCodes = getSamplesFromContents(c.contents);
				c.fromTransformationTypeCodes = fromTransformationTypeCodes;
				c.fromTransformationCodes = fromTransformationCodes;
				c.fromPurificationCode = fromPurificationCode;
				c.fromPurificationTypeCode = fromPurificationTypeCode;
				c.fromTransfertCode = fromTransfertCode;
				c.fromTransfertTypeCode = fromTransfertTypeCode;				
				c.processTypeCodes = processTypeCodes;
				c.processCodes = newInputProcessCodes;
				c.state = state;
				c.traceInformation = traceInformation;
				c.treeOfLife=tree;
				if (ocu.comment != null) {
					c.comments = Collections.singletonList(ocu.comment);
				}
				newContainers.add(c);
			});			
		}

		return newContainers;
	}

	//	private PropertySingleValue getNullIfNoValue(PropertySingleValue psv) {
	//		if(null != psv && null == psv.value)return null;
	//		else return psv;
	//	}
	private PropertySingleValue getNullIfNoValue(PropertySingleValue psv) {
		if (psv == null)
			return null;
		if (psv.value == null)
			return null;
		return psv;
	}	

	private Comment updateComment(Comment comment, ContextValidation validation) {
		comment.createUser = validation.getUser();
		comment.creationDate = new Date();
		comment.code = CodeHelper.getInstance().generateExperimentCommentCode(comment);
		return comment;
	}

	private Set<String> getSamplesFromContents(List<Content> contents) {
		return contents.stream().map(c-> c.sampleCode).collect(Collectors.toSet());
	}

	private Set<String> getProjectsFromContents(List<Content> contents) {
		return contents.stream().map(c -> c.projectCode).collect(Collectors.toSet());
	}


	private Set<String> duplicateProcesses(Set<String> inputProcessCodes) {
		List<Process> processes = MongoDBDAO.find(InstanceConstants.PROCESS_COLL_NAME, Process.class, DBQuery.in("code", inputProcessCodes)).toList();
		Set<String> newInputProcessCodes = new HashSet<>();
		processes.forEach(p -> {
			p._id = null;
			p.code = CodeHelper.getInstance().generateProcessCode(p); //TO-DO NGL-119 => Quel est le sample d'origine ??
			MongoDBDAO.save(InstanceConstants.PROCESS_COLL_NAME, p);
			newInputProcessCodes.add(p.code);			
		});		
		return newInputProcessCodes;
	}


	private TreeOfLifeNode getTreeOfLifeNode(Experiment exp,	AtomicTransfertMethod atm) {
		TreeOfLifeNode treeNode = new TreeOfLifeNode();

		treeNode.from = new From();
		treeNode.from.experimentCode = exp.code;
		treeNode.from.experimentTypeCode = exp.typeCode;
		treeNode.from.containers = atm.inputContainerUseds.stream().map(icu -> {
			ParentContainers pc = new ParentContainers();
			pc.code = icu.code;
			pc.supportCode = icu.locationOnContainerSupport.code;
			pc.fromTransformationTypeCodes = icu.fromTransformationTypeCodes;
			pc.fromTransformationCodes = icu.fromTransformationCodes;			
			pc.processCodes = icu.processCodes;
			pc.processTypeCodes = icu.processTypeCodes;
			return pc;
		}).collect(Collectors.toList());

		treeNode.paths = new ArrayList<>();

		atm.inputContainerUseds.forEach(icu -> {
			Container c = MongoDBDAO.findOne(InstanceConstants.CONTAINER_COLL_NAME, Container.class, DBQuery.in("code", icu.code));
			if(null != c.treeOfLife && null != c.treeOfLife.paths){
				treeNode.paths.addAll(c.treeOfLife.paths.stream().map(s -> s+","+icu.code).collect(Collectors.toList()));
			}else{
				treeNode.paths.add(","+icu.code);
			}
		});

		return treeNode;
	}


	private Set<String> getPropertyDefinitionCodesByLevel(List<PropertyDefinition> propertyDefs, Level.CODE level){

		Level l = new Level(level);

		return propertyDefs.stream().filter(pd -> pd.levels.contains(l)).map(pd -> pd.code).collect(Collectors.toSet());
	}

	/*
	 * Filter key by object syntax
	 * @param propertyDefs
	 * @param level
	 * @return
	 */
	private Set<String> getPropertyDefinitionCodesByLevelFilterObject(List<PropertyDefinition> propertyDefs, Level.CODE level) {
		Level l = new Level(level);
		return propertyDefs.stream()
				.filter(pd -> pd.levels.contains(l))
				.map(pd -> pd.code)
				.collect(Collectors.toSet())
				.stream()
				.map(s -> getKeyPropertiesInstance(s))
				.collect(Collectors.toSet());
	}

	private String getKeyPropertiesInstance(String keyPropertyDefinition) {
		if (keyPropertyDefinition.contains("."))
			return keyPropertyDefinition.substring(0, keyPropertyDefinition.indexOf("."));
		else
			return keyPropertyDefinition;
	}


	private Map<String, PropertyValue> getOutputPropertiesForALevel(Experiment exp, OutputContainerUsed ocu, Level.CODE level) {
		Map<String, PropertyValue> propertiesForALevel = new HashMap<>(); // <String, PropertyValue>();
		ExperimentType expType = ExperimentType.find.get().findByCode(exp.typeCode);
		Set<String> experimentPropertyDefinitionCodes = getPropertyDefinitionCodesByLevelFilterObject(expType.propertiesDefinitions, level);
		if (ocu != null && ocu.experimentProperties != null && experimentPropertyDefinitionCodes.size() > 0) {
			propertiesForALevel.putAll(ocu.experimentProperties.entrySet().stream()
					.filter(entry -> experimentPropertyDefinitionCodes.contains(entry.getKey()) && entry.getValue() != null)
					.collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue(), (u,v) -> PropertiesMerger(u, v))));					
		}
		//extract instrument content properties
		InstrumentUsedType insType = InstrumentUsedType.find.get().findByCode(exp.instrument.typeCode);
		Set<String> instrumentPropertyDefinitionCodes = getPropertyDefinitionCodesByLevelFilterObject(insType.propertiesDefinitions, level);
		if (ocu != null && ocu.instrumentProperties != null && instrumentPropertyDefinitionCodes.size() > 0) {			
			propertiesForALevel.putAll(ocu.instrumentProperties.entrySet().stream()
					.filter(entry -> instrumentPropertyDefinitionCodes.contains(entry.getKey()) && entry.getValue() != null)
					.collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue(), (u,v) -> PropertiesMerger(u, v))));								
		}
		return propertiesForALevel;
	}

	private Map<String, PropertyValue> getInputPropertiesForALevel(Experiment exp, 
			InputContainerUsed icu, 
			Level.CODE level) {
		Map<String, PropertyValue> propertiesForALevel = new HashMap<>(); // String, PropertyValue>();

		ExperimentType expType = ExperimentType.find.get().findByCode(exp.typeCode);
		Set<String> experimentPropertyDefinitionCodes = getPropertyDefinitionCodesByLevelFilterObject(expType.propertiesDefinitions, level);

		if (icu != null && icu.experimentProperties != null && experimentPropertyDefinitionCodes.size() > 0) {
			propertiesForALevel.putAll(icu.experimentProperties.entrySet().stream()
					.filter(entry -> experimentPropertyDefinitionCodes.contains(entry.getKey()) && entry.getValue() != null)
					.collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue(), (u,v) -> PropertiesMerger(u, v))));					
		}

		//extract instrument content properties
		InstrumentUsedType insType = InstrumentUsedType.find.get().findByCode(exp.instrument.typeCode);
		Set<String> instrumentPropertyDefinitionCodes = getPropertyDefinitionCodesByLevelFilterObject(insType.propertiesDefinitions, level);

		if (icu != null && icu.instrumentProperties != null && instrumentPropertyDefinitionCodes.size() > 0) {			
			propertiesForALevel.putAll(icu.instrumentProperties.entrySet().stream()
					.filter(entry -> instrumentPropertyDefinitionCodes.contains(entry.getKey()) && entry.getValue() != null)
					.collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue(), (u,v) -> PropertiesMerger(u, v))));								
		}

		//extract process content properties for only the inputContainer of the process
		//NGL-3564 code obsolete
		Set<String> processesPropertyDefinitionCodes = getProcessesPropertyDefinitionCodes(icu, level)
				.stream()
				.collect(Collectors.toList()).stream().map(s->{
					return getKeyPropertiesInstance(s);
				}).collect(Collectors.toSet());

		if(processesPropertyDefinitionCodes.size() >0){
			propertiesForALevel.putAll(getProcessesProperties(icu)
					.stream()
					.filter(entry -> processesPropertyDefinitionCodes.contains(entry.getKey()) && entry.getValue() != null)
					.collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue(), (u,v) -> PropertiesMerger(u, v))));				
		}


		return propertiesForALevel;
	}


	/*
	 * Get all property for a level in expererimentProperties, instrumentProperties and inpoutContainerProperties
	 * NOT INCLUDE OUTPUT CONTAINER PROPERTY USED getOutputPropertiesForALevel METHOD
	 * @param exp
	 * @param atm
	 * @param level
	 * @return
	 */
	private Map<String, PropertyValue> getCommonPropertiesForALevelWithICU(Experiment exp, InputContainerUsed icu, Level.CODE level) {
		Map<String, PropertyValue> propertiesForALevel = new HashMap<>(); // String, PropertyValue>();
		ExperimentType expType = ExperimentType.find.get().findByCode(exp.typeCode);
		Set<String> experimentPropertyDefinitionCodes = getPropertyDefinitionCodesByLevel(expType.propertiesDefinitions, level);
		//extract experiment content properties
		if (exp.experimentProperties != null && experimentPropertyDefinitionCodes.size() > 0) {
			propertiesForALevel.putAll(exp.experimentProperties.entrySet()
					.stream()
					.filter(entry -> experimentPropertyDefinitionCodes.contains(entry.getKey()) && entry.getValue() != null)
					.collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue())));
		}
		//extract protocol
		Protocol protocol = MongoDBDAO.findByCode(InstanceConstants.PROTOCOL_COLL_NAME, Protocol.class, exp.protocolCode);
		// GA: Need to define protocol properties in description but in waiting we just copy all
		if (Level.CODE.Content.equals(level) && protocol != null && protocol.properties != null && protocol.properties.size() > 0){
			propertiesForALevel.putAll(protocol.properties);
		}

		if(null != icu.experimentProperties && icu.experimentProperties.size() > 0){
			propertiesForALevel.putAll(icu.experimentProperties.entrySet().stream()
					.filter(entry -> experimentPropertyDefinitionCodes.contains(entry.getKey()) && entry.getValue() != null)					
					.collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue(), (u,v) -> PropertiesMerger(u, v))));

		}

		//extract instrument content properties
		InstrumentUsedType insType = InstrumentUsedType.find.get().findByCode(exp.instrument.typeCode);
		Set<String> instrumentPropertyDefinitionCodes = getPropertyDefinitionCodesByLevel(insType.propertiesDefinitions, level);

		if (exp.instrumentProperties != null && instrumentPropertyDefinitionCodes.size() > 0){
			propertiesForALevel.putAll(exp.instrumentProperties.entrySet()
					.stream()
					.filter(entry -> instrumentPropertyDefinitionCodes.contains(entry.getKey()) && entry.getValue() != null)
					.collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue())));
		}
		if (icu.instrumentProperties != null && icu.instrumentProperties.size() > 0){
			propertiesForALevel.putAll(icu.instrumentProperties.entrySet().stream()
					.filter(entry -> instrumentPropertyDefinitionCodes.contains(entry.getKey()) && entry.getValue() != null)
					.collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue(), (u,v) -> PropertiesMerger(u, v))));

		}
		/* Do not extract property from process because the risk to have the same property on several process is very big
		 * To put process property in container used rules*/
		//		if(null != icu){
		//extract process content properties for only the inputContainer of the process
		List<String> processesPropertyDefinitionCodes = getProcessesPropertyDefinitionCodes(icu, level);					
		if(processesPropertyDefinitionCodes.size() >0){
			propertiesForALevel.putAll(getProcessesProperties(icu)
					.stream()
					.filter(entry -> processesPropertyDefinitionCodes.contains(entry.getKey()) && entry.getValue() != null)
					.collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue(), (u,v) -> PropertiesMerger(u, v))));				
			//			}
		}

		return propertiesForALevel;
	}


	/*
	 * Get all property for a level in experimentProperties, instrumentProperties and inpoutContainerProperties
	 * NOT INCLUDE OUTPUT CONTAINER PROPERTY USED getOutputPropertiesForALevel METHOD
	 * @param exp
	 * @param atm
	 * @param level
	 * @return
	 */
	private Map<String, PropertyValue> getCommonPropertiesForALevel(Experiment exp, Level.CODE level) {
		Map<String, PropertyValue> propertiesForALevel = new HashMap<>(); // <String, PropertyValue>();
		ExperimentType expType = ExperimentType.find.get().findByCode(exp.typeCode);
		Set<String> experimentPropertyDefinitionCodes = getPropertyDefinitionCodesByLevelFilterObject(expType.propertiesDefinitions, level);
		//extract experiment content properties
		if (exp.experimentProperties != null && experimentPropertyDefinitionCodes.size() > 0) {
			propertiesForALevel.putAll(exp.experimentProperties.entrySet()
					.stream()
					.filter(entry -> experimentPropertyDefinitionCodes.contains(entry.getKey()))
					.collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue())));
		}
		//extract protocol
		Protocol protocol = MongoDBDAO.findByCode(InstanceConstants.PROTOCOL_COLL_NAME, Protocol.class, exp.protocolCode);
		// GA: Need to define protocol properties in description but in waiting we just copy all
		if (Level.CODE.Content.equals(level) && protocol != null && protocol.properties != null && protocol.properties.size() > 0) {
			propertiesForALevel.putAll(protocol.properties);
		}


		//extract instrument content properties
		InstrumentUsedType insType = InstrumentUsedType.find.get().findByCode(exp.instrument.typeCode);
		Set<String> instrumentPropertyDefinitionCodes = getPropertyDefinitionCodesByLevelFilterObject(insType.propertiesDefinitions, level);

		if (exp.instrumentProperties != null && instrumentPropertyDefinitionCodes.size() > 0) {
			propertiesForALevel.putAll(exp.instrumentProperties.entrySet()
					.stream()
					.filter(entry -> instrumentPropertyDefinitionCodes.contains(entry.getKey()))
					.collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue())));
		}
		return propertiesForALevel;
	}

	private <T extends PropertyValue> T PropertiesMerger(T u, T v) {
		if (u.value.equals(v.value)) {
			return u;
		} else {
			throw new IllegalStateException(String.format("Duplicate key %s with different values", u)); 
		}
	}

	private List<String> getProcessesPropertyDefinitionCodes(InputContainerUsed icu, Level.CODE level) {		
		return icu.processTypeCodes.stream()
				.map(code -> ProcessType.find.get().findByCode(code))
				.map( p   -> p.getPropertyDefinitionByLevel(level))
				.flatMap(List::stream)
				.map(pd -> pd.code)
				.collect(Collectors.toList());		
	}

	/*
	 * Extract process property for only the first experiment of the process
	 */
	private List<Map.Entry<String,PropertyValue>> getProcessesProperties(InputContainerUsed icu) {
		List<Process> processes=MongoDBDAO.find(InstanceConstants.PROCESS_COLL_NAME, Process.class, DBQuery.in("code", icu.processCodes).is("inputContainerCode", icu.code)).toList();
		if (processes != null && processes.size() > 0) {
			return processes.stream()
					.filter(p -> p.properties != null)
					.map((Process p) -> p.properties.entrySet())
					.flatMap(Set::stream)
					.collect(Collectors.toList());
		} else {
			return new ArrayList<>(); // <Map.Entry<String, PropertyValue>>();
		}
	}

	public void updateComments(Experiment exp, ContextValidation validation) {
		if(null != exp.comments && exp.comments.size() > 0){
			exp.comments.forEach(comment -> {
				if(comment.createUser == null){
					comment.createUser = validation.getUser();
					comment.creationDate = new Date();
				}else if(comment.creationDate == null){
					comment.creationDate = new Date();
				}

				if(comment.code == null){
					comment.code = CodeHelper.getInstance().generateExperimentCommentCode(comment);	
				}
			});
		}		
	}

	public void updateStatus(Experiment exp, ContextValidation validation) {
		if (!TBoolean.UNSET.equals(exp.status.valid)) {
			exp.status.date = new Date();
			exp.status.user = validation.getUser();
		}
	}

	/**
	 * Asynchronous drools rules execution.
	 * @param validation validation context
	 * @param exp        experiment
	 */
	public void callWorkflowRules(ContextValidation validation, Experiment exp) {
		ArrayList<Object> facts = new ArrayList<>();
		facts.add(exp);
		facts.add(validation);
		for(int i=0; i<exp.atomicTransfertMethods.size(); i++) {
			AtomicTransfertMethod atomic = exp.atomicTransfertMethods.get(i);
			if(atomic.viewIndex == null)atomic.viewIndex = i+1; //used to have the position in the list
			facts.add(atomic);
		}
		rulesActor.tellMessage("workflow", facts);
	}

	/*
	 * Update only the qc result and not the container attribut
	 * used in admin case
	 * @param exp
	 * @param validation
	 */
	public void updateQCResultInInputContainers(ContextValidation validation, Experiment exp) {
		exp.atomicTransfertMethods
		.parallelStream()
		.map(atm -> atm.inputContainerUseds)
		.flatMap(List::stream)
		.forEach(icu -> {
			Container container = MongoDBDAO.findByCode(InstanceConstants.CONTAINER_COLL_NAME, Container.class,icu.code);

			QualityControlResult qcr = container.qualityControlResults.stream()
					.filter(rr -> rr.code.equals(exp.code))
					.findFirst().get();
			qcr.instrumentUsedTypeCode = exp.instrument.typeCode;
			qcr.properties = new HashMap<>();

			//BUG NGL-2343 qcr.properties keep ref to icu.experimentProperties
			//and update icu.experimentProperties like qcr.properties 
			//ERROR qcr.properties = icu.experimentProperties;

			if(MapUtils.isNotEmpty(icu.experimentProperties))
				qcr.properties.putAll(icu.experimentProperties);
			if(MapUtils.isNotEmpty(icu.instrumentProperties))
				qcr.properties.putAll(icu.instrumentProperties);

			qcr.valuation.valid = icu.valuation.valid;
			qcr.valuation.comment = icu.valuation.comment;
			MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME,Container.class, DBQuery.is("code", container.code), DBUpdate.set("qualityControlResults",container.qualityControlResults));
		});

	}


	/*
	 * update volume, concentration, quantity and size only if present
	 * @param exp
	 * @param validation
	 */
	public void updateInputContainers(Experiment exp, ContextValidation validation) {
		Map<String, List<Container>> containersBySupportCode = exp.atomicTransfertMethods
				.parallelStream()
				//				.map(atm -> atm.inputContainerUseds)
				//				.flatMap(List::stream)
				.flatMap(atm -> atm.inputContainerUseds.stream())
				.map(icu -> {
					Container c = MongoDBDAO.findByCode(InstanceConstants.CONTAINER_COLL_NAME, Container.class,icu.code);
					// GA: Here to keep the value in entry of experiment replace icu attribute values by input attribute values before update
					if(null != icu.newConcentration && null != icu.newConcentration.value)c.concentration = icu.newConcentration;
					if(null != icu.newQuantity && null != icu.newQuantity.value)c.quantity = icu.newQuantity;
					if(null != icu.newVolume && null != icu.newVolume.value)c.volume = icu.newVolume;
					if(null != icu.newSize && null != icu.newSize.value)c.size = icu.newSize;
					if (icu.valuation != null && TBoolean.TRUE.equals(icu.copyValuationToInput)) {
						c.valuation      = icu.valuation;
						c.valuation.user = validation.getUser();
						c.valuation.date = new Date();
					}
					//					c.traceInformation.modifyDate = new Date();
					//					c.traceInformation.modifyUser = validation.getUser();
					c.traceInformation.forceModificationStamp(validation.getUser());
					if (StringUtils.isNotBlank(icu.locationOnContainerSupport.storageCode))
						c.support.storageCode = icu.locationOnContainerSupport.storageCode;			
					
					if (c.qualityControlResults == null)
						c.qualityControlResults = new ArrayList<>(0);
					c.qualityControlResults.add(new QualityControlResult(exp.code, exp.typeCode, exp.instrument.typeCode, c.qualityControlResults.size(), icu.experimentProperties, icu.instrumentProperties, icu.valuation));
					Map<String, PropertyValue> newContentProperties = getCommonPropertiesForALevelWithICU(exp, icu, Level.CODE.Content);
					c.contents.forEach(content -> content.properties.putAll(newContentProperties));
					// GA: Validate Container
					return c;
				}).collect(Collectors.groupingBy(c -> c.support.code));

		// update support storage if needed
		containersBySupportCode.entrySet().forEach(entry -> {
			ContainerSupport support = MongoDBDAO.findByCode(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, ContainerSupport.class,entry.getKey());
			List<Container> containers = entry.getValue();
			String oldStorageCode = support.storageCode;
			String newStorageCode =  getSupportStorageCode(containers, validation);

			if (StringUtils.isNotBlank(newStorageCode) && !newStorageCode.equals(oldStorageCode)) {
				support.storageCode = newStorageCode;
				support.storages    = updateStorages(support, validation);
				//				support.traceInformation.modifyDate = new Date();
				//				support.traceInformation.modifyUser = validation.getUser();
				support.traceInformation.forceModificationStamp(validation.getUser());
				MongoDBDAO.save(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, support);				
			}
			// update containers
			containers.parallelStream().forEach(container -> MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME,container));
		});
	}

	public void updateWithNewSampleCodesIfNeeded(Experiment exp) {

		ExperimentType experimentType=ExperimentType.find.get().findByCode(exp.typeCode);
		if (experimentType.newSample) {	
			exp.atomicTransfertMethods
			.stream()
			.map(atm -> atm.outputContainerUseds)
			.flatMap(List::stream)				
			.forEach(ocu -> {					
				updateContents(ocu);
			});				
		}
	}

	/*
	 * Create new sample code for the output containers in case we want to create another sample
	 * @param exp
	 * @param validation
	 */
	public void createNewSampleCodesIfNeeded(Experiment exp, ContextValidation validation){

		ExperimentType experimentType=ExperimentType.find.get().findByCode(exp.typeCode);
		if(experimentType.newSample){	
			Set<String> newProjectCodes = new TreeSet<>();
			Set<String> newSampleCodes = new TreeSet<>();

			exp.atomicTransfertMethods
			.stream()
			.map(atm -> atm.outputContainerUseds)
			.flatMap(List::stream)
			.sorted(new Comparator<OutputContainerUsed>() {
				@Override
				public int compare(OutputContainerUsed ocu1,	OutputContainerUsed ocu2) {						
					Content content1 = ocu1.contents.get(0); //in theory only one content;
					Content content2 = ocu2.contents.get(0); //in theory only one content;						
					// GA: Add other field to compare in case of same sample. This other field need to be used in exp.js
					int result = content1.sampleCode.compareTo(content2.sampleCode);
					if(result == 0){
						result = ocu1.code.compareTo(ocu2.code);
					}						
					return result;
				}

			})
			.forEach(ocu -> {
				Map<String,PropertyValue> experimentProperties = ocu.experimentProperties;

				if (experimentProperties.containsKey("sampleTypeCode") 
						&& experimentProperties.containsKey("projectCode")
						&& !experimentProperties.containsKey("sampleCode")) {
					String nextProjectCode = (String)experimentProperties.get("projectCode").value;

					String newSampleCode=CodeHelper.getInstance().generateSampleCode(nextProjectCode, true);
					// GA: Add control to check if not already exist.
					ocu.experimentProperties.put("sampleCode", new PropertySingleValue(newSampleCode));

					newProjectCodes.add(nextProjectCode);
					newSampleCodes.add(newSampleCode);

				}
				updateContents(ocu);
			});

			if(newProjectCodes.size() > 0){
				exp.projectCodes.addAll(newProjectCodes);
			}

			if(newSampleCodes.size() > 0){
				exp.sampleCodes.addAll(newSampleCodes);
			}

			MongoDBDAO.update(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, DBQuery.is("code", exp.code), 
					DBUpdate.set("atomicTransfertMethods", exp.atomicTransfertMethods).set("projectCodes", exp.projectCodes).set("sampleCodes",exp.sampleCodes));			
		}
	}

	public void updateNewSamplesIfNeeded(ContextValidation validation, Experiment exp){
		ExperimentType experimentType=ExperimentType.find.get().findByCode(exp.typeCode);
		if(experimentType.newSample){
			exp.atomicTransfertMethods
			.stream()
			.forEach(atm -> updateNewSamples(exp, atm, validation));			
		}
	}

	private void updateNewSamples(Experiment exp, AtomicTransfertMethod  atm, ContextValidation validation) {
		if(atm.inputContainerUseds.size() == 1 && atm.inputContainerUseds.get(0).contents.size() == 1){
			InputContainerUsed icu = atm.inputContainerUseds.get(0);
			String inputContentSampleCode = atm.inputContainerUseds.get(0).contents.get(0).sampleCode;
			String inputContentProjectCode = atm.inputContainerUseds.get(0).contents.get(0).projectCode;
			Sample sampleIn=MongoDBDAO.findByCode(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, inputContentSampleCode);


			atm.outputContainerUseds
			.stream()
			.forEach(ocu -> updateNewSamplePropertiesFromSampleIn(exp, ocu, icu, sampleIn, inputContentProjectCode, validation))
			;

		}else{
			throw new RuntimeException("To create a new sample we need to have only one InputContainerUsed with only one Content");
		}		
	}

	private void updateNewSamplePropertiesFromSampleIn(Experiment exp, 
			OutputContainerUsed ocu,
			InputContainerUsed icu, 
			Sample sampleIn, 
			String inputContentProjectCode, 
			ContextValidation validation) {
		if (ocu.experimentProperties.containsKey("sampleTypeCode") 
				&& ocu.experimentProperties.containsKey("projectCode")
				&& ocu.experimentProperties.containsKey("sampleCode")) {

			String sampleCode = ocu.experimentProperties.get("sampleCode").value.toString();
			if (MongoDBDAO.checkObjectExistByCode(InstanceConstants.SAMPLE_COLL_NAME, Sample.class,sampleCode)) {
				Map<String, PropertyValue> newSampleProperties = new HashMap<>(sampleIn.properties);
				newSampleProperties.putAll(getCommonPropertiesForALevel(exp, Level.CODE.Sample));
				newSampleProperties.putAll(getInputPropertiesForALevel(exp, icu, Level.CODE.Sample));
				newSampleProperties.putAll(getOutputPropertiesForALevel(exp, ocu, Level.CODE.Sample));
				MongoDBDAO.update(InstanceConstants.SAMPLE_COLL_NAME,Sample.class, DBQuery.is("code", sampleCode), DBUpdate.set("properties", newSampleProperties));				
			}		
		}		
	}

	public void createNewSamplesIfNeeded(Experiment exp, ContextValidation validation){
		ExperimentType experimentType=ExperimentType.find.get().findByCode(exp.typeCode);
		if (experimentType.newSample) {
			validation.putObject(NEW_SAMPLE_CODES, new HashSet<String>());

			exp.atomicTransfertMethods
			.stream()
			.forEach(atm -> createNewSamples(exp, atm, validation));			
		}
	}

	private void createNewSamples(Experiment exp, AtomicTransfertMethod  atm, ContextValidation validation) {
		if (atm.inputContainerUseds.size() == 1 && atm.inputContainerUseds.get(0).contents.size() == 1) {
			InputContainerUsed icu = atm.inputContainerUseds.get(0);
			String inputContentSampleCode = atm.inputContainerUseds.get(0).contents.get(0).sampleCode;
			String inputContentProjectCode = atm.inputContainerUseds.get(0).contents.get(0).projectCode;
			Sample sampleIn=MongoDBDAO.findByCode(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, inputContentSampleCode);
			// FDS: et si aucun trouvé ????
			logger.debug("sample parent= "+ inputContentSampleCode);
			atm.outputContainerUseds
			.stream()
			.forEach(ocu -> createNewSamplesFromSampleIn(exp, ocu, icu, sampleIn, inputContentProjectCode, validation));

		} else {
			throw new RuntimeException("To create a new sample we need to have only one InputContainerUsed with only one Content");
		}		
	}

	private void updateContents(OutputContainerUsed ocu) {
		if(ocu.experimentProperties.containsKey("sampleTypeCode") 
				&& ocu.experimentProperties.containsKey("projectCode")
				&& ocu.experimentProperties.containsKey("sampleCode")){

			String sampleTypeCode=ocu.experimentProperties.get("sampleTypeCode").value.toString();
			String projectCode=ocu.experimentProperties.get("projectCode").value.toString();
			String sampleCode=ocu.experimentProperties.get("sampleCode").value.toString();			
			String sampleCategoryCode = SampleType.find.get().findByCode(sampleTypeCode).category.code;

			ocu.contents
			.forEach(c -> {
				//Add the fromSampleTypeCode in propertiesproperties to keep the link with parent type
				PropertySingleValue fromSampleTypeCode = new PropertySingleValue(c.sampleTypeCode);
				c.properties.put("fromSampleTypeCode", fromSampleTypeCode);
				PropertySingleValue fromSampleCode = new PropertySingleValue(c.sampleCode);
				c.properties.put("fromSampleCode", fromSampleCode);
				PropertySingleValue fromProjectCode = new PropertySingleValue(c.projectCode);
				c.properties.put("fromProjectCode", fromProjectCode);

				c.projectCode = projectCode;
				c.sampleCode = sampleCode;
				c.sampleTypeCode = sampleTypeCode;
				c.sampleCategoryCode = sampleCategoryCode;

			});
		}

	}


	private void createNewSamplesFromSampleIn(Experiment exp, 
			OutputContainerUsed ocu,
			InputContainerUsed icu, 
			Sample sampleIn, 
			String inputContentProjectCode, 
			ContextValidation validation) {
		if (ocu.experimentProperties.containsKey("sampleTypeCode") 
				&& ocu.experimentProperties.containsKey("projectCode")
				&& ocu.experimentProperties.containsKey("sampleCode")) {

			String sampleTypeCode = ocu.experimentProperties.get("sampleTypeCode").value.toString();
			String projectCode    = ocu.experimentProperties.get("projectCode")   .value.toString();
			String sampleCode     = ocu.experimentProperties.get("sampleCode")    .value.toString();

			Sample newSample = new Sample();
			newSample.code         = sampleCode;
			newSample.name         = sampleCode;

			newSample.typeCode     = sampleTypeCode;
			newSample.categoryCode = SampleType.find.get().findByCode(sampleTypeCode).category.code;			
			newSample.projectCodes = new HashSet<>();
			newSample.projectCodes.add(projectCode);

			newSample.taxonCode    = sampleIn.taxonCode;

			Map<String, PropertyValue> newSampleProperties = new HashMap<>(sampleIn.properties);
			newSampleProperties.putAll(getCommonPropertiesForALevel(exp, Level.CODE.Sample));
			newSampleProperties.putAll(getInputPropertiesForALevel(exp, icu, Level.CODE.Sample));
			newSampleProperties.putAll(getOutputPropertiesForALevel(exp, ocu, Level.CODE.Sample));
			newSample.properties=newSampleProperties;

			newSample.importTypeCode=sampleIn.importTypeCode;
			newSample.ncbiLineage=sampleIn.ncbiLineage;
			newSample.ncbiScientificName=sampleIn.ncbiScientificName;
			newSample.referenceCollab=sampleIn.referenceCollab;
			newSample.life = getSampleLife(exp, sampleIn, icu, inputContentProjectCode);

			newSample.traceInformation=new TraceInformation(validation.getUser());

			if (!MongoDBDAO.checkObjectExistByCode(InstanceConstants.SAMPLE_COLL_NAME, Sample.class,sampleCode)) {

				SampleHelper.executeSampleCreationRules(newSample);
				ContextValidation sampleValidation = ContextValidation.createCreationContext(validation.getUser());
				newSample.validate(sampleValidation);
				if (!sampleValidation.hasErrors()) {
					validation.<Set<String>>getTypedObject(NEW_SAMPLE_CODES).add(newSample.code);
					MongoDBDAO.save(InstanceConstants.SAMPLE_COLL_NAME,newSample);
				} else {
					validation.addErrors(sampleValidation.getErrors());
				}
			} else {
				//if exist only update properties
				//FDS 04/09/2019 lors de la creation on peut passer par une étape de mise a jour des propriétés
				logger.debug("updating properties of sample "+ newSample.code );
				MongoDBDAO.update(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, 
						DBQuery.is("code", newSample.code), DBUpdate.set("properties", newSample.properties));
			}
		}
	}

	private SampleLife getSampleLife(Experiment exp, Sample sampleIn, InputContainerUsed icu, String projectCode) {
		SampleLife lifeSample = new SampleLife();

		lifeSample.from = new models.laboratory.sample.instance.tree.From();
		lifeSample.from.experimentCode     = exp.code;
		lifeSample.from.experimentTypeCode = exp.typeCode;

		lifeSample.from.containerCode      = icu.code;
		lifeSample.from.supportCode        = icu.locationOnContainerSupport.code;
		lifeSample.from.processCodes       = icu.processCodes;
		lifeSample.from.processTypeCodes   = icu.processTypeCodes;

		lifeSample.from.projectCode        = projectCode;
		lifeSample.from.sampleCode         = sampleIn.code;
		lifeSample.from.sampleTypeCode     = sampleIn.typeCode;

		if (sampleIn.life != null && sampleIn.life.path != null) {
			lifeSample.path = sampleIn.life.path + "," + sampleIn.code;
		} else {
			lifeSample.path = "," + sampleIn.code;
		}

		return lifeSample;
	}

	/*
	 * Delete created sample and reset the last sampleCode on project
	 * @param contextValidation
	 * @param exp
	 */
	public void deleteNewSampleAndRollbackProject(ContextValidation contextValidation, Experiment exp) {
		ExperimentType experimentType=ExperimentType.find.get().findByCode(exp.typeCode);
		if(experimentType.newSample){
			Set<String> updateProjectCodes = exp.atomicTransfertMethods
					.stream()
					.flatMap(atm -> atm.outputContainerUseds.stream())
					.filter(ocu -> ocu.experimentProperties.containsKey("projectCode"))
					.map(ocu -> ocu.experimentProperties.get("projectCode").value.toString())
					.collect(Collectors.toSet());

			Set<String> deleteSampleCodes = exp.atomicTransfertMethods
					.stream()
					.flatMap(atm -> atm.outputContainerUseds.stream())
					.filter(ocu -> ocu.experimentProperties.containsKey("sampleCode"))
					.map(ocu -> ocu.experimentProperties.get("sampleCode").value.toString())
					.collect(Collectors.toSet());

			if (deleteSampleCodes != null && deleteSampleCodes.size() > 0) {
				MongoDBDAO.delete(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, DBQuery.in("code", deleteSampleCodes));
			}

			if (deleteSampleCodes != null && deleteSampleCodes.size() > 0) {
				deleteSampleCodes.forEach(deleteSampleCode -> 
				MongoDBDAO.update(InstanceConstants.PROCESS_COLL_NAME, Process.class, 
						DBQuery.in("sampleCodes", deleteSampleCode), 
						DBUpdate.pull("sampleCodes", deleteSampleCode)));				
			}

			updateProjectCodes.parallelStream()
			.forEach(projectCode -> CodeHelper.getInstance().updateProjectSampleCodeWithLastSampleCode(projectCode));
		}
	}

	public void removeOutputContainerCode(Experiment exp, ContextValidation errorValidation) {
		exp.atomicTransfertMethods.stream().forEach(atm -> atm.removeOutputContainerCode());		
	}

	public void updateContentPropertiesWithExperimentContentProperties(ContextValidation validation, Experiment exp, Experiment oldExp) {
		//		if(null == oldExp || null == exp)throw new IllegalArgumentException("missing parameters");
		if (exp    == null) throw new IllegalArgumentException("null exp");
		if (oldExp == null) throw new IllegalArgumentException("null oldExp");
		long t1 = System.currentTimeMillis();
		// 1 extract content property code

		ExperimentType expType = ExperimentType.find.get().findByCode(exp.typeCode);
		final Set<String> contentPropertyCodes = getPropertyDefinitionCodesByLevelFilterObject(expType.propertiesDefinitions, Level.CODE.Content);

		InstrumentUsedType insType = InstrumentUsedType.find.get().findByCode(exp.instrument.typeCode);
		contentPropertyCodes.addAll(getPropertyDefinitionCodesByLevelFilterObject(insType.propertiesDefinitions, Level.CODE.Content));
		//extract protocol

		Protocol protocol = MongoDBDAO.findByCode(InstanceConstants.PROTOCOL_COLL_NAME, Protocol.class, exp.protocolCode);
		// GA: Need to define protocol properties in description but in waiting we just copy all
		if (protocol != null && protocol.properties != null && protocol.properties.size() > 0) {
			contentPropertyCodes.addAll(protocol.properties.keySet());
		}
		// 2 update only if content property exist
		if (contentPropertyCodes.size() > 0) {

			Map<String, Content> oldExpContents = flatMapContentsToMap(oldExp, exp.categoryCode, contentPropertyCodes);

			//exp.atomicTransfertMethods.parallelStream().forEach(atm -> {
			exp.atomicTransfertMethods.forEach(atm -> {
				if (ExperimentCategory.CODE.qualitycontrol.toString().equals(exp.categoryCode)) {
					atm.inputContainerUseds
					.stream()
					.forEach(icu -> updateContainerContentPropertiesInCascading(validation, icu, contentPropertyCodes, oldExpContents));
				} else if (atm.outputContainerUseds != null) {
					atm.outputContainerUseds
					.stream()
					.forEach(ocu -> updateContainerContentPropertiesInCascading(validation, ocu, contentPropertyCodes, oldExpContents));					
				}
			});			
		}
		long t2 = System.currentTimeMillis();
		logger.debug("Time to progate experiment content properties : "+(t2-t1)+" ms");
	}

	public void updateSamplePropertiesWithExperimentSampleProperties(ContextValidation validation, Experiment exp, Experiment oldExp) {
		//		if (null == oldExp || null == exp)
		//			throw new IllegalArgumentException("missing parameters");
		if (exp    == null) throw new IllegalArgumentException("null exp");
		if (oldExp == null) throw new IllegalArgumentException("null oldExp");
		long t1 = System.currentTimeMillis();
		// 1 extract content property code

		ExperimentType expType = ExperimentType.find.get().findByCode(exp.typeCode);
		final Set<String> samplePropertyCodes = getPropertyDefinitionCodesByLevelFilterObject(expType.propertiesDefinitions, Level.CODE.Sample);

		InstrumentUsedType insType = InstrumentUsedType.find.get().findByCode(exp.instrument.typeCode);
		samplePropertyCodes.addAll(getPropertyDefinitionCodesByLevelFilterObject(insType.propertiesDefinitions, Level.CODE.Sample));
		// extract protocol
		// 2 update only if content property exist
		if (samplePropertyCodes.size() > 0) {

			Map<String, Content> oldExpContents = flatMapContentsToMap(oldExp, exp.categoryCode, samplePropertyCodes);

			exp.atomicTransfertMethods.forEach(atm -> {
				if (ExperimentCategory.CODE.qualitycontrol.toString().equals(exp.categoryCode)) {
					atm.inputContainerUseds
					.stream()
					.forEach(icu -> updateContainerContentPropertiesInCascading(validation, icu, samplePropertyCodes, oldExpContents));
				} else if (atm.outputContainerUseds != null) {
					atm.outputContainerUseds
					.stream()
					.forEach(ocu -> updateContainerContentPropertiesInCascading(validation, ocu, samplePropertyCodes, oldExpContents));					
				}
			});			
		}
		long t2 = System.currentTimeMillis();
		logger.debug("Time to progate experiment content properties : {} ms", t2-t1);
	}

	// The flatMapContentsToMap_ should be functionally equivalent but avoids the collect
	// done in this method.
	//	private Map<String, Content> flatMapContentsToMap(Experiment oldExp, String expCategoryCode, Set<String> contentPropertyCodes) {
	//		Map<String, Content> m = oldExp.atomicTransfertMethods
	//			.stream()
	//			.map(atm -> {
	//				Map<String, Content> acuMapping = new HashMap<>(0);
	//				if (ExperimentCategory.CODE.qualitycontrol.toString().equals(expCategoryCode)) {
	//					acuMapping = atm.inputContainerUseds
	//						.stream()
	////						.map(icu -> icu.contents.stream().map(content -> Pair.of(getKey(icu, content, contentPropertyCodes), content)).collect(Collectors.toList()))
	////						.flatMap(List::stream)
	//						.flatMap(icu -> icu.contents.stream().map(content -> Pair.of(getKey(icu, content, contentPropertyCodes), content)))
	//						.collect(Collectors.toMap(pair -> pair.getLeft(), pair -> pair.getRight()));
	//				} else if (atm.outputContainerUseds != null) {
	//					acuMapping = atm.outputContainerUseds
	//							.stream()
	////							.map(icu -> icu.contents.stream().map(content -> Pair.of(getKey(icu, content, contentPropertyCodes), content)).collect(Collectors.toList()))
	////							.flatMap(List::stream)
	//							.flatMap(icu -> icu.contents.stream().map(content -> Pair.of(getKey(icu, content, contentPropertyCodes), content)))
	//							.collect(Collectors.toMap(pair -> pair.getLeft(), pair -> pair.getRight()));					
	//				}
	//				return acuMapping.entrySet();				
	//			})
	//			.flatMap(Set::stream)
	//			.collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue()));					
	//		return m;
	//	}

	protected Map<String, Content> flatMapContentsToMap(Experiment oldExp, String expCategoryCode, Set<String> contentPropertyCodes) {
		return oldExp.atomicTransfertMethods
				.stream()
				.flatMap(atm -> {
					if (ExperimentCategory.CODE.qualitycontrol.toString().equals(expCategoryCode)) {
						return atm.inputContainerUseds
								.stream()
								.flatMap(icu -> icu.contents.stream().map(content -> Pair.of(getKey(icu, content, contentPropertyCodes), content)));
					} else if (atm.outputContainerUseds != null) {
						return atm.outputContainerUseds
								.stream()
								.flatMap(icu -> icu.contents.stream().map(content -> Pair.of(getKey(icu, content, contentPropertyCodes), content)));					
					} else {
						return Stream.empty();
					}
				})
				.collect(Collectors.toMap(pair -> pair.getLeft(), pair -> pair.getRight()));					
	}

	private String getKey(AbstractContainerUsed acu,Content content, Set<String> contentPropertyCodes) {
		if (content.properties.containsKey(InstanceConstants.TAG_PROPERTY_NAME) && !contentPropertyCodes.contains(InstanceConstants.TAG_PROPERTY_NAME)) {
			return acu.code
					+ "_" + content.projectCode
					+ "_" + content.sampleCode
					+ "_" + content.properties.get(InstanceConstants.TAG_PROPERTY_NAME).value.toString();
		} else {
			return acu.code + "_" + content.projectCode + "_" + content.sampleCode;
		}
	}

	private void updateContainerContentPropertiesInCascading(ContextValidation validation, AbstractContainerUsed acu, Set<String> contentPropertyCodes, Map<String, Content> oldExpContents) {
		Logger.debug("Update content container "+acu.code);
		List<Container> containerMustBeUpdated = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class,  
				DBQuery.or(DBQuery.is("code", acu.code), DBQuery.regex("treeOfLife.paths", Pattern.compile(","+acu.code+"$|,"+acu.code+","))))
				.toList();
		Set<String> containerCodes = containerMustBeUpdated.stream().map(c -> c.code).collect(Collectors.toSet());

		acu.contents.forEach(ocuContent -> {
			Logger.debug("Content "+ocuContent.sampleCode);
			Content oldContent = oldExpContents.get(getKey(acu, ocuContent, contentPropertyCodes));
			Map<String, Pair<PropertyValue, PropertyValue>> updatedProperties = InstanceHelpers.getUpdatedPropertiesForSomePropertyCodes(contentPropertyCodes, oldContent.properties, ocuContent.properties);
			Set<String> deletedPropertyCodes = InstanceHelpers.getDeletedPropertiesForSomePropertyCodes(contentPropertyCodes, oldContent.properties, ocuContent.properties);

			if (updatedProperties.size() > 0 || deletedPropertyCodes.size() > 0) {
				List<Sample> allSamples = MongoDBDAO.find(InstanceConstants.SAMPLE_COLL_NAME, Sample.class,  
						DBQuery.or(DBQuery.is("code", ocuContent.sampleCode), DBQuery.regex("life.path", Pattern.compile(","+ocuContent.sampleCode+"$|,"+ocuContent.sampleCode+","))))
						.toList();

				Set<String> projectCodes = allSamples.stream().map(s -> s.projectCodes).flatMap(Set::stream).collect(Collectors.toSet());
				Set<String> sampleCodes = allSamples.stream().map(s -> s.code).collect(Collectors.toSet());
				Set<String> tags = getTagAssignFromContainerLife(containerCodes, ocuContent, projectCodes, sampleCodes, updatedProperties, oldContent);
				Logger.debug("Tags "+tags);
				logger.debug(getKey(acu, ocuContent, contentPropertyCodes)+" updatedProperties "+updatedProperties);
				logger.debug(getKey(acu, ocuContent, contentPropertyCodes)+" deletedPropertyCodes "+deletedPropertyCodes);

				InstanceHelpers._updateContentProperties(projectCodes, sampleCodes, containerCodes, tags, updatedProperties,
						deletedPropertyCodes, validation);
			}

		});			
	}

	/**
	 * Get tag to filter cascade update content properties
	 * @param containerCodes
	 * @param ocuContent
	 * @param projectCodes
	 * @param sampleCodes
	 * @param updatedProperties
	 * @return
	 */
	private Set<String> getTagAssignFromContainerLife(Set<String> containerCodes,
			Content ocuContent, 
			Set<String> projectCodes,  
			Set<String> sampleCodes, 
			Map<String, Pair<PropertyValue, PropertyValue>> updatedProperties,
			Content oldContent) {
		//New Algo with secondaryTag 2733
		Set<String> tags = new TreeSet<>();
		//1. current content = tag I + tag II => STOP search
		if(ocuContent.properties.containsKey(InstanceConstants.TAG_PROPERTY_NAME) &&
				ocuContent.properties.containsKey(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME)) {
			String tag = ocuContent.properties.get(InstanceConstants.TAG_PROPERTY_NAME).value.toString();
			String secondaryTag=ocuContent.properties.get(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME).value.toString();
			//Check if content value updated
			if(updatedProperties.containsKey(InstanceConstants.TAG_PROPERTY_NAME) && oldContent.properties.containsKey(InstanceConstants.TAG_PROPERTY_NAME))
				tag=oldContent.properties.get(InstanceConstants.TAG_PROPERTY_NAME).value.toString();
			else if(updatedProperties.containsKey(InstanceConstants.TAG_PROPERTY_NAME) && !oldContent.properties.containsKey(InstanceConstants.TAG_PROPERTY_NAME))
				tag=null;
			if(updatedProperties.containsKey(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME) && oldContent.properties.containsKey(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME))
				secondaryTag=oldContent.properties.get(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME).value.toString();
			else if(updatedProperties.containsKey(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME) && !oldContent.properties.containsKey(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME))
				secondaryTag=null;
			if(tag!=null && secondaryTag!=null)
				tags.add(secondaryTag+"_"+tag);
			else if(tag!=null && secondaryTag==null)
				tags.add("_"+tag);
			else if(tag==null && secondaryTag!=null)
				tags.add(secondaryTag+"_");
			//2. current content = tag I => STOP search
		}else if(ocuContent.properties.containsKey(InstanceConstants.TAG_PROPERTY_NAME) && 
				!ocuContent.properties.containsKey(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME)) {
			String tag = ocuContent.properties.get(InstanceConstants.TAG_PROPERTY_NAME).value.toString();
			if(updatedProperties.containsKey(InstanceConstants.TAG_PROPERTY_NAME) && oldContent.properties.containsKey(InstanceConstants.TAG_PROPERTY_NAME))
				tag=oldContent.properties.get(InstanceConstants.TAG_PROPERTY_NAME).value.toString();
			else if(updatedProperties.containsKey(InstanceConstants.TAG_PROPERTY_NAME) && !oldContent.properties.containsKey(InstanceConstants.TAG_PROPERTY_NAME))
				tag=null;
			if(tag!=null)
				tags.add("_"+tag);
		}else {
			//search in container treeOfLife 
			//3. Search container  one content only tagI 
			DBQuery.Query query = DBQuery.in("code",containerCodes)
					.size("contents", 1)  //only one content is very important because we targeting the lib container and not a pool after lib prep.
					.elemMatch("contents", DBQuery.in("sampleCode", sampleCodes)
							.in("projectCode",  projectCodes)
							.exists("properties.tag").notExists("properties.secondaryTag"));
			List<Container> containersWithTag = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class,query).toList();
			if(containersWithTag.size() > 0){
				tags.addAll(containersWithTag.stream().map(c->"_"+c.contents.get(0).properties.get(InstanceConstants.TAG_PROPERTY_NAME).value.toString()).collect(Collectors.toSet()));
				/*for(Container containerWithTag : containersWithTag) {
					tags.add("_"+containerWithTag.contents.get(0).properties.get(InstanceConstants.TAG_PROPERTY_NAME).value.toString());
				}*/
			}
			//4.a Recherche tag II
			//4.a.1 Content courant tagII
			//TODO secondaryTag devient une liste
			Set<String> secondaryTags = new HashSet<String>();
			boolean emptyValueSecondaryTag=false;
			if(ocuContent.properties.containsKey(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME) &&
					!ocuContent.properties.containsKey(InstanceConstants.TAG_PROPERTY_NAME)) {
				String secondaryTag=ocuContent.properties.get(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME).value.toString();
				if(updatedProperties.containsKey(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME) && oldContent.properties.containsKey(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME))
					secondaryTag=oldContent.properties.get(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME).value.toString();
				else if(updatedProperties.containsKey(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME) && !oldContent.properties.containsKey(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME)) {
					secondaryTag=null;
					emptyValueSecondaryTag=true;
				}
				if(secondaryTag!=null) {
					tags.add(secondaryTag+"_");
					secondaryTags.add(secondaryTag);
				}
			}else {
				//4.a.2 Container enfant un seul content tagII
				query = DBQuery.in("code",containerCodes)
						.size("contents", 1)  //only one content is very important because we targeting the lib container and not a pool after lib prep.
						.elemMatch("contents", DBQuery.in("sampleCode", sampleCodes)
								.in("projectCode",  projectCodes)
								.notExists("properties.tag").exists("properties.secondaryTag"));
				List<Container> containersWithSecondTag = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class,query).toList();
				if(containersWithSecondTag.size()>0) {
					secondaryTags.addAll(containersWithSecondTag.stream().map(c->c.contents.get(0).properties.get(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME).value.toString()).collect(Collectors.toSet()));
					tags.addAll(secondaryTags.stream().map(s->s+"_").collect(Collectors.toSet()));;
				}
			}
			//4.b Recherche container enfant plusieurs content avec tagI identique et tagII du 4.a pour avoir combinaison tagII_tagI
			for(String secondaryTag : secondaryTags) {
				query = DBQuery.in("code",containerCodes)
						.elemMatch("contents", DBQuery.in("sampleCode", sampleCodes)
								.in("projectCode",  projectCodes)
								.is("properties.secondaryTag.value", secondaryTag)
								.exists("properties.tag"));
				List<Container> containersWithDoubleTag = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class,query).toList();
				if(containersWithDoubleTag.size() > 0){
					//Check same all tagI 
					for(Container container : containersWithDoubleTag) {
						if(checkSameTagInContainer(container)) {
							for(Content content : container.contents){
								if(sampleCodes.contains(content.sampleCode) && projectCodes.contains(content.projectCode) && content.properties.get(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME).value.toString().equals(secondaryTag)){
									tags.add(secondaryTag+"_"+content.properties.get(InstanceConstants.TAG_PROPERTY_NAME).value.toString());
									break;
								}
							}
							
						}
					}

				}
			}
			//NGL-3466 
			//4.b bis si le tagII de l'étape 4.a.1 est vide car ancienne valeur vide 
			//alors recherche container enfant avec plusieurs contenus qui contient tagI identique sur tous les contenus 
			//avec un tagII vide sur tous les contenus filtrés par code container, code sample.
			
			if(emptyValueSecondaryTag) {
				query = DBQuery.in("code",containerCodes)
						.elemMatch("contents", DBQuery.in("sampleCode", sampleCodes)
								.in("projectCode",  projectCodes)
								.exists("properties.tag"));
				List<Container> containersWithDoubleTag = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class,query).toList();
				if(containersWithDoubleTag.size() > 0){
					//Check same all tagI 
					for(Container container : containersWithDoubleTag) {
						if(checkSameTagInContainer(container)) {
							for(Content content : container.contents){
								if(sampleCodes.contains(content.sampleCode) && projectCodes.contains(content.projectCode)){
									tags.add("_"+content.properties.get(InstanceConstants.TAG_PROPERTY_NAME).value.toString());
									break;
								}
							}
							
						}
					}

				}
			}
			
		}
		if(tags.size()>0)
			return tags;
		else
			return null;
	}


	private boolean checkSameTagInContainer(Container container)
	{
		Set<String> tags = container.contents.stream()
				.filter(content->content.properties.containsKey(InstanceConstants.TAG_PROPERTY_NAME))
				.map(content->content.properties.get(InstanceConstants.TAG_PROPERTY_NAME).value.toString())
				.collect(Collectors.toSet());

		if(tags.size()==1)
			return true;
		else
			return false;
	}

}


