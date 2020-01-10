package services.instance.sample;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoException;

import controllers.migration.OneToVoidContainer;
import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.MongoDBResult;
import fr.cea.ig.MongoDBResult.Sort;
import fr.cea.ig.ngl.NGLApplication;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.experiment.description.ExperimentType;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.experiment.instance.InputContainerUsed;
import models.laboratory.experiment.instance.OutputContainerUsed;
import models.laboratory.processes.instance.Process;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.sample.instance.Sample;
import models.laboratory.sample.instance.reporting.SampleExperiment;
import models.laboratory.sample.instance.reporting.SampleProcess;
import models.laboratory.sample.instance.reporting.SampleProcessesStatistics;
import models.laboratory.sample.instance.reporting.SampleReadSet;
import models.utils.InstanceConstants;
import models.utils.dao.DAOException;
import play.Logger;
import rules.services.RulesException;
import services.instance.AbstractImportData;
import validation.ContextValidation;
import workflows.process.ProcWorkflowHelper;

public class UpdateReportingData extends AbstractImportData {
	
	private final ProcWorkflowHelper procWorkflowHelper;	
	private Map<String, List<String>> transformationCodesByProcessTypeCode = new HashMap<>();
	private Map<String, Integer> nbExpPositionInProcessType = new HashMap<>(); 

	@Inject
	public UpdateReportingData(NGLApplication app) {
		super("UpdateReportingData", app);
		procWorkflowHelper = app.injector().instanceOf(ProcWorkflowHelper.class);
	}

//	@Override
//	public void runImport() throws SQLException, DAOException, MongoException, RulesException {
//		logger.debug("Start reporting synchro");
//		Integer skip = 0;
//		Date date = new Date();
//		MongoDBResult<Sample> result = MongoDBDAO.find(InstanceConstants.SAMPLE_COLL_NAME, Sample.class);
//		Integer nbResult = result.count(); 
//		while (skip < nbResult) {
//			try {
//				long t1 = System.currentTimeMillis();
//				List<Sample> cursor = MongoDBDAO.find(InstanceConstants.SAMPLE_COLL_NAME, Sample.class)
//						.sort("traceInformation.creationDate", Sort.DESC).skip(skip).limit(1000)
//						.toList();
//
//				cursor.forEach(sample -> {
//					try {
//						updateProcesses(sample);
//						logger.debug("update sample "+sample.code);
//						if (sample.processes != null && sample.processes.size() > 0) {
//							MongoDBDAO.update(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, DBQuery.is("code", sample.code), 
//									DBUpdate.set("processes", sample.processes).set("processesStatistics", sample.processesStatistics).set("processesUpdatedDate", date));
//						} else {
//							MongoDBDAO.update(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, DBQuery.is("code", sample.code), 
//									DBUpdate.unset("processes").unset("processesStatistics").set("processesUpdatedDate", date));
//						}
//					} catch(Throwable e) {
//						logger.error("Sample : "+sample.code+" - "+e,e);
//						if (e.getMessage() != null)
//							contextError.addErrors(sample.code, e.getMessage());
//						else
//							contextError.addErrors(sample.code, "null");
//					}
//				});
//				skip = skip + 1000;
//				long t2 = System.currentTimeMillis();
//				logger.debug("time " + skip + " - " + ((t2-t1)/1000));
//			} catch(Throwable e) {
//				logger.error("Error : "+e,e);
//				if (e.getMessage() != null)
//					contextError.addErrors("Error", e.getMessage());
//				else
//					contextError.addErrors("Error", "null");
//			}
//		}
//	}

	@Override
	public void runImport(ContextValidation contextError) throws SQLException, DAOException, MongoException, RulesException {
		logger.debug("Start reporting synchro");
		Integer skip = 0;
		Date date = new Date();
		MongoDBResult<Sample> result = MongoDBDAO.find(InstanceConstants.SAMPLE_COLL_NAME, Sample.class);
		Integer nbResult = result.count(); 
		while (skip < nbResult) {
			try {
				long t1 = System.currentTimeMillis();
				List<Sample> cursor = MongoDBDAO.find(InstanceConstants.SAMPLE_COLL_NAME, Sample.class)
						.sort("traceInformation.creationDate", Sort.DESC).skip(skip).limit(1000)
						.toList();

				cursor.forEach(sample -> {
					try {
						if(!sample.projectCodes.contains("CEA")){ //skip very big sample on CNS
							updateProcesses(sample);						
							logger.debug("update sample "+sample.code);
							if (sample.processes != null && sample.processes.size() > 0) {
								MongoDBDAO.update(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, DBQuery.is("code", sample.code), 
										DBUpdate.set("processes", sample.processes).set("processesStatistics", sample.processesStatistics).set("processesUpdatedDate", date));
							} else {
								MongoDBDAO.update(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, DBQuery.is("code", sample.code), 
										DBUpdate.unset("processes").unset("processesStatistics").set("processesUpdatedDate", date));
							}
						}
					} catch(Throwable e) {
						logger.error("Sample : "+sample.code+" - "+e,e);
						if (e.getMessage() != null)
							contextError.addError(sample.code, e.getMessage());
						else
							contextError.addError(sample.code, "null");
					}
				});
				skip = skip + 1000;
				long t2 = System.currentTimeMillis();
				logger.debug("time " + skip + " - " + ((t2-t1)/1000));
			} catch(Throwable e) {
				logger.error("Error : "+e,e);
				if (e.getMessage() != null)
					contextError.addError("Error", e.getMessage());
				else
					contextError.addError("Error", "null");
			}
		}
	}

	
	public void updateProcesses(Sample sample) {
		List<Process> processes = MongoDBDAO.find(InstanceConstants.PROCESS_COLL_NAME, Process.class, DBQuery.in("sampleCodes", sample.code))
				.toList();
		
		sample.processes = processes.parallelStream()
					.map(process -> convertToSampleProcess(sample, process))
					.collect(Collectors.toList());	
		SampleProcess spWithoutProcess = getReadSetBeforeNGLSQ(sample);
		if (spWithoutProcess != null) {
			sample.processes.add(spWithoutProcess);
		}
		
		computeStatistics(sample);
	}

	private void computeStatistics(Sample sample) {
		if (sample.processes != null) {
			sample.processesStatistics = new SampleProcessesStatistics();	
			sample.processesStatistics.processTypeCodes = sample.processes.stream().filter(p -> p.typeCode != null).collect(Collectors.groupingBy(p -> p.typeCode, Collectors.counting()));
			sample.processesStatistics.processCategoryCodes = sample.processes.stream().filter(p -> p.categoryCode != null).collect(Collectors.groupingBy(p -> p.categoryCode, Collectors.counting()));
			sample.processesStatistics.readSetTypeCodes = sample.processes.stream().filter(p -> p.readsets != null).map(p -> p.readsets).flatMap(List::stream).collect(Collectors.groupingBy(r -> r.typeCode, Collectors.counting()));
		}
	}

	private SampleProcess convertToSampleProcess(Sample sample, Process process) {
		SampleProcess sampleProcess = new SampleProcess();
		sampleProcess.code= process.code;
		sampleProcess.typeCode= process.typeCode;
		sampleProcess.categoryCode= process.categoryCode;
		sampleProcess.state= process.state;
		sampleProcess.state.historical=null;
		sampleProcess.traceInformation= process.traceInformation;
		sampleProcess.sampleOnInputContainer = process.sampleOnInputContainer;
		
		if (process.properties != null && process.properties.size() > 0) {
			sampleProcess.properties = process.properties;			
		}
		sampleProcess.currentExperimentTypeCode = process.currentExperimentTypeCode;
		if (process.experimentCodes != null && process.experimentCodes.size() > 0) {
			List<SampleExperiment> experiments  = updateExperiments(process);
			if (experiments != null && experiments.size() > 0) {
				sampleProcess.experiments = experiments;
			}
		}
		
		if (process.outputContainerCodes != null && process.outputContainerCodes.size() > 0) {
			List<SampleReadSet> readsets = getSampleReadSets(sample, process);
			if (readsets != null && readsets.size() > 0) {
				sampleProcess.readsets = readsets;
			}
		}
		List<String> transformationCodes = getTransformationCodesForProcessTypeCode(process);
		//extract only transformation
		
		Integer nbExp = 0;
		if (process.experimentCodes != null && process.experimentCodes.size() > 0 && transformationCodes != null && transformationCodes.size() > 0) {
			nbExp = MongoDBDAO.find(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, DBQuery.in("code", process.experimentCodes)
					.in("typeCode", transformationCodes)
					.in("state.code", Arrays.asList("IP","F"))).count();
			
			sampleProcess.progressInPercent = (new BigDecimal((nbExp.floatValue() / Integer.valueOf(nbExpPositionInProcessType.get(process.typeCode)).floatValue())*100.00)).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
			//Logger.debug("progressInPercent : "+(nbExp.floatValue() / Integer.valueOf(transformationCodes.size()).floatValue()));
		} else {
			sampleProcess.progressInPercent = null;			
		}
		return sampleProcess;
	}
			
	private List<String> getTransformationCodesForProcessTypeCode(Process process) {
		List<String> transformationCodes;
		if (transformationCodesByProcessTypeCode.containsKey(process.typeCode)) {
			transformationCodes = transformationCodesByProcessTypeCode.get(process.typeCode);
		} else {
			transformationCodes =ExperimentType.find.get().findByProcessTypeCode(process.typeCode,true).stream().map(e -> e.code).collect(Collectors.toList());
			transformationCodesByProcessTypeCode.put(process.typeCode, transformationCodes);
			
			Integer nbPos = ExperimentType.find.get().countDistinctExperimentPositionInProcessType(process.typeCode);
			nbExpPositionInProcessType.put(process.typeCode, nbPos);
		}
		
		return transformationCodes;
	}

	private List<SampleExperiment> updateExperiments(Process process) {		
		List<SampleExperiment> sampleExperiments = new ArrayList<>();
		MongoDBDAO.find(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, DBQuery.in("code", process.experimentCodes))
			.cursor.forEach(experiment -> {
				sampleExperiments.addAll(convertToSampleExperiments(process, experiment));
			});
		return sampleExperiments;
		//List<Experiment> experiments = MongoDBDAO.find(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, DBQuery.in("code", process.experimentCodes)).toList();
		//return experiments.parallelStream().map(exp -> convertToSampleExperiments(process, exp)).flatMap(List::stream).collect(Collectors.toList());		
	}
	
	//map key = expCode-processCode
	private List<SampleExperiment> convertToSampleExperiments(Process process, Experiment experiment) {
		List<SampleExperiment> sampleExperiments = new ArrayList<>();
		Set<String> containerCodes = new TreeSet<>();
		containerCodes.add(process.inputContainerCode);
		
		if (process.outputContainerCodes != null) {
			containerCodes.addAll(process.outputContainerCodes);
		}
		experiment.atomicTransfertMethods.parallelStream().forEach(atm -> {
			if (OneToVoidContainer.class.isInstance(atm)) {
				atm.inputContainerUseds.forEach(icu -> {
					if (containerCodes.contains(icu.code)) {
						SampleExperiment sampleExperiment = new SampleExperiment();
						sampleExperiment.code = experiment.code;
						sampleExperiment.typeCode= experiment.typeCode;
						sampleExperiment.categoryCode= experiment.categoryCode;
						sampleExperiment.state= experiment.state;
						sampleExperiment.state.historical=null;
						sampleExperiment.status= experiment.status;
						
						sampleExperiment.traceInformation= experiment.traceInformation;
						sampleExperiment.protocolCode = experiment.protocolCode;
						sampleExperiment.properties = computeExperimentProperties(experiment, icu, null);
						sampleExperiments.add(sampleExperiment);
					}					
				});
			} else {
				atm.inputContainerUseds.forEach(icu -> {
					if (atm.outputContainerUseds != null) {
						atm.outputContainerUseds.forEach(ocu -> {
							if (ocu.code != null && containerCodes.containsAll(Arrays.asList(icu.code, ocu.code))) {
								SampleExperiment sampleExperiment = new SampleExperiment();
								sampleExperiment.code = experiment.code;
								sampleExperiment.typeCode= experiment.typeCode;
								sampleExperiment.categoryCode= experiment.categoryCode;
								sampleExperiment.state= experiment.state;
								sampleExperiment.state.historical=null;
								sampleExperiment.status= experiment.status;
								
								sampleExperiment.traceInformation= experiment.traceInformation;
								sampleExperiment.protocolCode = experiment.protocolCode;
								sampleExperiment.properties = computeExperimentProperties(experiment, icu, ocu);
								sampleExperiments.add(sampleExperiment);
							} else if(containerCodes.contains(icu.code)) {
								SampleExperiment sampleExperiment = new SampleExperiment();
								sampleExperiment.code = experiment.code;
								sampleExperiment.typeCode= experiment.typeCode;
								sampleExperiment.categoryCode= experiment.categoryCode;
								sampleExperiment.state= experiment.state;
								sampleExperiment.state.historical=null;
								sampleExperiment.status= experiment.status;
								sampleExperiment.traceInformation= experiment.traceInformation;
								sampleExperiment.protocolCode = experiment.protocolCode;
								sampleExperiment.properties = computeExperimentProperties(experiment, icu, null);
								sampleExperiments.add(sampleExperiment);
							}
						});
					} else if(containerCodes.contains(icu.code)) {
						SampleExperiment sampleExperiment = new SampleExperiment();
						sampleExperiment.code = experiment.code;
						sampleExperiment.typeCode= experiment.typeCode;
						sampleExperiment.categoryCode= experiment.categoryCode;
						sampleExperiment.state= experiment.state;
						sampleExperiment.state.historical=null;
						sampleExperiment.status= experiment.status;
						
						sampleExperiment.traceInformation= experiment.traceInformation;
						sampleExperiment.protocolCode = experiment.protocolCode;
						sampleExperiment.properties = computeExperimentProperties(experiment, icu, null);
						sampleExperiments.add(sampleExperiment);
					}
				});	
			}
		});
		//one-to-void
		//one-to-one
		//many-to-one
		//one-to-many ???
		
		return sampleExperiments;
	}

	private Map<String, PropertyValue> computeExperimentProperties(Experiment experiment, 
			                                                          InputContainerUsed icu,
			                                                          OutputContainerUsed ocu) {
		Map<String, PropertyValue> finalProperties = new HashMap<>(); // <String, PropertyValue>();
		if (experiment.experimentProperties != null) finalProperties.putAll(filterProperties(experiment.experimentProperties));
		if (experiment.instrumentProperties != null) finalProperties.putAll(filterProperties(experiment.instrumentProperties));
		if (icu.experimentProperties        != null) finalProperties.putAll(filterProperties(icu.experimentProperties));
		if (icu.instrumentProperties        != null) finalProperties.putAll(filterProperties(icu.instrumentProperties));
		if (ocu != null) {
			if (ocu.experimentProperties != null) finalProperties.putAll(filterProperties(ocu.experimentProperties));
			if (ocu.instrumentProperties != null) finalProperties.putAll(filterProperties(ocu.instrumentProperties));
		}
		return finalProperties;
	}

	private Map<String, PropertyValue> filterProperties(Map<String, PropertyValue> properties) {
		return properties.entrySet().parallelStream()
				.filter(entry -> entry.getValue() != null && !entry.getValue()._type.equals(PropertyValue.imgType)
						&& !entry.getValue()._type.equals(PropertyValue.fileType))
				.collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue()));
	}
	
	private List<SampleReadSet> getSampleReadSets(Sample sample, Process process) {
		List<SampleReadSet> sampleReadSets = new ArrayList<>();
		Set<String> tags = procWorkflowHelper.getTagAssignFromProcessContainers(process);
		
		BasicDBObject keys = new BasicDBObject();
		keys.put("treatments", 0);
		
		MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME,ReadSet.class,	
				DBQuery.in("sampleOnContainer.containerCode", process.outputContainerCodes).in("sampleCode", process.sampleCodes).in("projectCode", process.projectCodes),
				keys)
		.cursor
		.forEach(readset -> {
			if (!readset.sampleOnContainer.properties.containsKey(InstanceConstants.TAG_PROPERTY_NAME)
					|| (null != tags && readset.sampleOnContainer.properties.containsKey(InstanceConstants.TAG_PROPERTY_NAME) 
					&&  tags.contains(readset.sampleOnContainer.properties.get(InstanceConstants.TAG_PROPERTY_NAME).value))){
				sampleReadSets.add(convertToSampleReadSet(readset));				
			}
		});
		return sampleReadSets;
	}

	private SampleReadSet convertToSampleReadSet(ReadSet readset) {
		SampleReadSet sampleReadSet = new SampleReadSet();
		sampleReadSet.code = readset.code;
		sampleReadSet.typeCode = readset.typeCode;
		sampleReadSet.state = readset.state;
		sampleReadSet.state.historical = null;
		sampleReadSet.runCode = readset.runCode;
		sampleReadSet.runTypeCode = readset.runTypeCode;
		sampleReadSet.runSequencingStartDate = readset.runSequencingStartDate;
		
		sampleReadSet.productionValuation = readset.productionValuation;   
		sampleReadSet.bioinformaticValuation = readset.bioinformaticValuation; 
		sampleReadSet.sampleOnContainer = readset.sampleOnContainer; 
		if (!readset.typeCode.equals("rsnanopore")) {
			BasicDBObject keys = new BasicDBObject();
			keys.put("code", 1);
			keys.put("treatments.ngsrg", 1);
			
			ReadSet rs = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME,ReadSet.class, readset.code, keys);
			sampleReadSet.treatments = rs.treatments;
		}
		//sampleReadSet.treatments = filterTreaments(readset.treatments);
		return sampleReadSet;
	}

//	private Map<String, Treatment> filterTreaments(Map<String, Treatment> treatments) {
//		treatments.values()
//			.parallelStream()
//			.forEach(treament ->{
//				treament.results.entrySet().forEach(entry -> {
//					entry.setValue(filterProperties(entry.getValue()));
//				});
//			});
//		return treatments;
//	}
	
	/*
	 * Extract ReadSet before the beginning of ngl-sq used.
	 * @param sample
	 * @return
	 */
	private SampleProcess getReadSetBeforeNGLSQ(Sample sample) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
			Date date = sdf.parse("2015/03/30");
		
			List<SampleReadSet> sampleReadSets = new ArrayList<>();
			
			BasicDBObject keys = new BasicDBObject();
			keys.put("treatments", 0);
			
			MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME,ReadSet.class,	
					DBQuery.lessThan("runSequencingStartDate", date).in("sampleCode", sample.code).in("projectCode", sample.projectCodes),
					keys)
			.cursor.forEach(readset -> {
				sampleReadSets.add(convertToSampleReadSet(readset));							
			});
			
			SampleProcess sampleProcess = null;
			if (sampleReadSets.size() > 0) {
				sampleProcess = new SampleProcess();
				sampleProcess.typeCode = "Old LIMS";
				sampleProcess.readsets = sampleReadSets;
			}
			
			return sampleProcess;
		} catch (ParseException e) {
			logger.error(e.getMessage(),e);
			return null;
		}
	}
	
}
