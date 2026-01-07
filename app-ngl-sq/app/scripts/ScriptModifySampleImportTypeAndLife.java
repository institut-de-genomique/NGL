package scripts;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptNoArgs;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.ContainerSupport;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.processes.instance.Process;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.sample.instance.Sample;
import models.utils.InstanceConstants;
import play.Logger;
import validation.ContextValidation;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.regex.Pattern;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.mongojack.DBQuery;

public class ScriptModifySampleImportTypeAndLife extends ScriptNoArgs {

	private ContextValidation validation = ContextValidation.createUpdateContext("ngl-support");
	
	@Override
	public void execute() throws Exception {
		Logger.error("Début reprise 4323");

		List<String> sampleCodesList = new ArrayList<>();
		sampleCodesList.add("DMM_TREC112491004");
		sampleCodesList.add("DMM_TREC112491005");

		HashMap<String, String> map = new HashMap<String, String>();
		map.put("DMM_TREC112491004", "TREC112553566");
		map.put("DMM_TREC112491005", "TREC112553567");

		initValidationObject();

		for (int i = 0; i < sampleCodesList.size(); i++) {
			Sample sample = MongoDBDAO.findByCode(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, sampleCodesList.get(i));
		
			if (sample != null) {
				Logger.error("Old sample code : " + sample.code + ", new sample code : " + map.get(sampleCodesList.get(i)));
				
				String oldSampleCode = sample.code;

				sample.traceInformation.modifyDate = new Date();
				sample.traceInformation.modifyUser = "ngl-support";

				sample.code = map.get(sampleCodesList.get(i));
				sample.name = sample.code;

				sample.importTypeCode = "reception-trec";

				sample.properties.put("trecStation", new PropertySingleValue("non renseigné"));
				sample.properties.put("trecProtocol", new PropertySingleValue("non renseigné"));
				sample.properties.put("trecBarcode", new PropertySingleValue(sample.code));

				ContextValidation ctxValidation = ContextValidation.createUndefinedContext("ngl-support");
				// Je peux pas mettre en context create / update sinon la validation du code plante.

				sample.validate(ctxValidation);
				
				if (ctxValidation.hasErrors()) {
					((List<String>) validation.getObject("RecapSample")).add("Sample (" + sampleCodesList.get(i) + ") has errors : " + ctxValidation.getErrors());
				} else {
					((List<String>) validation.getObject("RecapSample")).add(sampleCodesList.get(i));

					MongoDBDAO.update(InstanceConstants.SAMPLE_COLL_NAME, sample);

					handleChildSampleUpdate(oldSampleCode, sample);

					handleContainerUpdate(oldSampleCode, sample);

					handleContainerSupportUpdate(oldSampleCode, sample);

					handleProcessUpdate(oldSampleCode, sample);

					handleExperimentUpdate(oldSampleCode, sample);

					handleReadsetUpdate(oldSampleCode, sample); 
				}	
			} else {
				Logger.error("Sample is null (" + sampleCodesList.get(i) + ").");
			}
		}

		createExcelFileRecap(validation);

		Logger.error("Fin reprise 4323");
	}

	private void createExcelFileRecap(ContextValidation cv) {
		Workbook wb = new HSSFWorkbook();
		CreationHelper createHelper = wb.getCreationHelper();

		for (String key : cv.getContextObjects().keySet()) {
			Sheet sheet = wb.createSheet(key);
			List<String> recaps = (List<String>) cv.getObject(key);
			int nbLine = 0;

			for (String recap : recaps) {
				Row row = sheet.createRow(nbLine);
				String[] tabRecap = recap.split(",");

				for (int i = 0; i < tabRecap.length; i++) {
					row.createCell(i).setCellValue(createHelper.createRichTextString(tabRecap[i]));
				}

				nbLine++;
			}
		}

		try (OutputStream fileOut = new FileOutputStream("ScriptReprise4323.xls")) {
			wb.write(fileOut);
		} catch(Exception e){
			Logger.debug(e.getMessage());
		}
	}

	private void initValidationObject() {
		validation.putObject("RecapSample", new ArrayList<String>());
		validation.putObject("RecapChildSample", new ArrayList<String>());
		validation.putObject("RecapContainer", new ArrayList<String>());
		validation.putObject("RecapSupport", new ArrayList<String>());
		validation.putObject("RecapProcess", new ArrayList<String>());
		validation.putObject("RecapExperiment", new ArrayList<String>());
		validation.putObject("RecapReadset", new ArrayList<String>());

		((List<String>) validation.getObject("RecapSample")).add("Code sample");
		((List<String>) validation.getObject("RecapChildSample")).add("Code child sample");
		((List<String>) validation.getObject("RecapContainer")).add("Code sample");
		((List<String>) validation.getObject("RecapSupport")).add("Code support");
		((List<String>) validation.getObject("RecapProcess")).add("Code process");
		((List<String>) validation.getObject("RecapExperiment")).add("Code experiment");
		((List<String>) validation.getObject("RecapReadset")).add("Code readset");
	}
	
	private void handleReadsetUpdate(String oldSampleCode, Sample sample) {
		List<ReadSet> readSetsList = MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.is("sampleOnContainer.properties.fromSampleCode.value", oldSampleCode)).toList();
	
		for (int i = 0; i < readSetsList.size(); i++) {
			((List<String>) validation.getObject("RecapReadset")).add(readSetsList.get(i).code);

			readSetsList.get(i).traceInformation.modifyDate = new Date();
			readSetsList.get(i).traceInformation.modifyUser = "ngl-support";

			if (readSetsList.get(i).sampleOnContainer.properties.get("fromSampleCode") != null) {
				readSetsList.get(i).sampleOnContainer.properties.get("fromSampleCode").value = sample.code;	
			} 

			// Pas de màj de propriété, la cascade va faire le boulot.

			ContextValidation contextValidation = ContextValidation.createUpdateContext("ngl-support");
			readSetsList.get(i).validate(contextValidation);

			if (contextValidation.hasErrors()) {
				((List<String>) validation.getObject("RecapReadset")).add("Readset (" + readSetsList.get(i).code + ") has errors : " + contextValidation.getErrors());
			} else {
				MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, readSetsList.get(i));
			}
		}
	}

	private void handleExperimentUpdate(String oldSampleCode, Sample sample) {
		List<Experiment> expList = MongoDBDAO.find(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, DBQuery.in("sampleCodes", oldSampleCode)).toList();
	
		for (int i = 0; i < expList.size(); i++) {
			((List<String>) validation.getObject("RecapExperiment")).add(expList.get(i).code);

			expList.get(i).traceInformation.modifyDate = new Date();
			expList.get(i).traceInformation.modifyUser = "ngl-support";

			expList.get(i).sampleCodes.remove(oldSampleCode);
			expList.get(i).sampleCodes.add(sample.code);

			for (int j = 0; j < expList.get(i).atomicTransfertMethods.size(); j++) {
				for (int k = 0; k < expList.get(i).atomicTransfertMethods.get(j).inputContainerUseds.size(); k++) {
					for (int l = 0; l < expList.get(i).atomicTransfertMethods.get(j).inputContainerUseds.get(k).contents.size(); l++) {
						if (expList.get(i).atomicTransfertMethods.get(j).inputContainerUseds.get(k).contents.get(l).sampleCode.equals(oldSampleCode)) {
							expList.get(i).atomicTransfertMethods.get(j).inputContainerUseds.get(k).contents.get(l).sampleCode = sample.code;
						}

						if (expList.get(i).atomicTransfertMethods.get(j).inputContainerUseds.get(k).sampleCodes.contains(oldSampleCode)) {
							expList.get(i).atomicTransfertMethods.get(j).inputContainerUseds.get(k).sampleCodes.remove(oldSampleCode);
							expList.get(i).atomicTransfertMethods.get(j).inputContainerUseds.get(k).sampleCodes.add(sample.code);
						}
					}
				}

				for (int k = 0; k < expList.get(i).atomicTransfertMethods.get(j).outputContainerUseds.size(); k++) {
					for (int l = 0; l < expList.get(i).atomicTransfertMethods.get(j).outputContainerUseds.get(k).contents.size(); l++) {						
						if (expList.get(i).atomicTransfertMethods.get(j).outputContainerUseds.get(k).contents.get(l).sampleCode.equals(oldSampleCode)) {
							expList.get(i).atomicTransfertMethods.get(j).outputContainerUseds.get(k).contents.get(l).sampleCode = sample.code;
						}
					}
				}
			}

			ContextValidation contextValidation = ContextValidation.createUpdateContext("ngl-support");
			expList.get(i).validate(contextValidation, null);

			// if (contextValidation.hasErrors()) {
			//	Logger.error("Experiment has errors : " + contextValidation.getErrors());
			// } else {
			MongoDBDAO.update(InstanceConstants.EXPERIMENT_COLL_NAME, expList.get(i));
			// }
		}

		// 2ème requête : atm.outputContainerUseds.contents.properties.fromSampleCode

		expList = MongoDBDAO.find(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, DBQuery.in("atomicTransfertMethods.outputContainerUseds.contents.properties.fromSampleCode.value", oldSampleCode)).toList();
	
		for (int i = 0; i < expList.size(); i++) {
			((List<String>) validation.getObject("RecapExperiment")).add(expList.get(i).code);

			for (int j = 0; j < expList.get(i).atomicTransfertMethods.size(); j++) {
				for (int k = 0; k < expList.get(i).atomicTransfertMethods.get(j).outputContainerUseds.size(); k++) {
					for (int l = 0; l < expList.get(i).atomicTransfertMethods.get(j).outputContainerUseds.get(k).contents.size(); l++) {
						if (expList.get(i).atomicTransfertMethods.get(j).outputContainerUseds.get(k).contents.get(l).properties.get("fromSampleCode") != null && expList.get(i).atomicTransfertMethods.get(j).outputContainerUseds.get(k).contents.get(l).properties.get("fromSampleCode").value.equals(oldSampleCode)) {
							expList.get(i).atomicTransfertMethods.get(j).outputContainerUseds.get(k).contents.get(l).properties.get("fromSampleCode").value = sample.code;
						}
					}
				}
			}

			ContextValidation contextValidation = ContextValidation.createUpdateContext("ngl-support");
			expList.get(i).validate(contextValidation, null);

			// if (contextValidation.hasErrors()) {
			//	Logger.error("Experiment has errors : " + contextValidation.getErrors());
			// } else {
			MongoDBDAO.update(InstanceConstants.EXPERIMENT_COLL_NAME, expList.get(i));
			// }
		}

		// 3ème requête : atm.inputContainerUseds.contents.properties.fromSampleCode

		expList = MongoDBDAO.find(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, DBQuery.in("atomicTransfertMethods.inputContainerUseds.contents.properties.fromSampleCode.value", oldSampleCode)).toList();
	
		for (int i = 0; i < expList.size(); i++) {
			((List<String>) validation.getObject("RecapExperiment")).add(expList.get(i).code);

			for (int j = 0; j < expList.get(i).atomicTransfertMethods.size(); j++) {
				for (int k = 0; k < expList.get(i).atomicTransfertMethods.get(j).inputContainerUseds.size(); k++) {
					for (int l = 0; l < expList.get(i).atomicTransfertMethods.get(j).inputContainerUseds.get(k).contents.size(); l++) {
						if (expList.get(i).atomicTransfertMethods.get(j).inputContainerUseds.get(k).contents.get(l).properties.get("fromSampleCode") != null && expList.get(i).atomicTransfertMethods.get(j).inputContainerUseds.get(k).contents.get(l).properties.get("fromSampleCode").value.equals(oldSampleCode)) {
							expList.get(i).atomicTransfertMethods.get(j).inputContainerUseds.get(k).contents.get(l).properties.get("fromSampleCode").value = sample.code;
						}
					}
				}
			}

			ContextValidation contextValidation = ContextValidation.createUpdateContext("ngl-support");
			expList.get(i).validate(contextValidation, null);

			// if (contextValidation.hasErrors()) {
			//	Logger.error("Experiment has errors : " + contextValidation.getErrors());
			//} else {
			MongoDBDAO.update(InstanceConstants.EXPERIMENT_COLL_NAME, expList.get(i));
			// }
		}
	}

	private void handleProcessUpdate(String oldSampleCode, Sample sample) {
		List<Process> processList = MongoDBDAO.find(InstanceConstants.PROCESS_COLL_NAME, Process.class, DBQuery.in("sampleCodes", oldSampleCode)).toList();
	
		for (int i = 0; i < processList.size(); i++) {
			((List<String>) validation.getObject("RecapProcess")).add(processList.get(i).code);

			processList.get(i).traceInformation.modifyDate = new Date();
			processList.get(i).traceInformation.modifyUser = "ngl-support";

			processList.get(i).sampleCodes.remove(oldSampleCode);
			processList.get(i).sampleCodes.add(sample.code);

			if (processList.get(i).sampleOnInputContainer.sampleCode.equals(oldSampleCode)) {
				processList.get(i).sampleOnInputContainer.sampleCode = sample.code;
			}

			if (processList.get(i).sampleOnInputContainer.properties != null && processList.get(i).sampleOnInputContainer.properties.get("fromSampleCode") != null && processList.get(i).sampleOnInputContainer.properties.get("fromSampleCode").value.equals(oldSampleCode)) {
				processList.get(i).sampleOnInputContainer.properties.get("fromSampleCode").value = sample.code;
			}

			// On touche pas aux propriétés, c'est la cascade qui va intervenir.

			ContextValidation contextValidation = ContextValidation.createUpdateContext("ngl-support");
			processList.get(i).validateUpdate(contextValidation);

			if (contextValidation.hasErrors()) {
				((List<String>) validation.getObject("RecapProcess")).add("Process (" + processList.get(i).code + ") has errors : " + contextValidation.getErrors());
			} else {
				MongoDBDAO.update(InstanceConstants.PROCESS_COLL_NAME, processList.get(i));
			}
		}
	}

	private void handleContainerSupportUpdate(String oldSampleCode, Sample sample) {
		List<ContainerSupport> containersSupportList = MongoDBDAO.find(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, ContainerSupport.class, DBQuery.in("sampleCodes", oldSampleCode)).toList();
	
		for (int i = 0; i < containersSupportList.size(); i++) {
			((List<String>) validation.getObject("RecapSupport")).add(containersSupportList.get(i).code);

			containersSupportList.get(i).sampleCodes.remove(oldSampleCode);
			containersSupportList.get(i).sampleCodes.add(sample.code);

			containersSupportList.get(i).traceInformation.modifyDate = new Date();
			containersSupportList.get(i).traceInformation.modifyUser = "ngl-support";

			ContextValidation contextValidation = ContextValidation.createUpdateContext("ngl-support");
			containersSupportList.get(i).validate(contextValidation);

			if (contextValidation.hasErrors()) {
				((List<String>) validation.getObject("RecapSupport")).add("Container support (" + containersSupportList.get(i).code + ") has errors : " + contextValidation.getErrors());
			} else {
				MongoDBDAO.update(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, containersSupportList.get(i));
			}
		}
	}

	private void handleContainerUpdate(String oldSampleCode, Sample sample) {
		List<Container> containersList = null;

		// 1ere requête : chercher par code erroné (sans rien d'autre).

		containersList = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class, DBQuery.in("sampleCodes", oldSampleCode)).toList();

		for (int i = 0; i < containersList.size(); i++) {
			((List<String>) validation.getObject("RecapContainer")).add(containersList.get(i).code);

			containersList.get(i).sampleCodes.remove(oldSampleCode);
			containersList.get(i).sampleCodes.add(sample.code);

			containersList.get(i).traceInformation.modifyDate = new Date();
			containersList.get(i).traceInformation.modifyUser = "ngl-support";

			for (int j = 0; j < containersList.get(i).contents.size(); j++) {
				if (containersList.get(i).contents.get(j).sampleCode.equals(oldSampleCode)) {
					containersList.get(i).contents.get(j).sampleCode = sample.code;
				}
			}

			ContextValidation contextValidation = ContextValidation.createUpdateContext("ngl-support");
			containersList.get(i).validate(contextValidation, null, null);
			
			if (contextValidation.hasErrors()) {
				((List<String>) validation.getObject("RecapContainer")).add("Container (" + containersList.get(i).code + ") has errors : " + contextValidation.getErrors());
			} else {
				MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME, containersList.get(i));
			}
		}

		// 2eme requête : chercher par code corrigé, from none partout (fromTransfo / fromTransfert / fromPurif none).
		// Mauvais importType => trec.

		containersList = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class, DBQuery.exists("importTypeCode").in("sampleCodes", sample.code)).toList();
	
		for (int i = 0; i < containersList.size(); i++) {
			if (containersList.get(i).sampleCodes.contains(sample.code)) {
				((List<String>) validation.getObject("RecapContainer")).add(containersList.get(i).code);

				containersList.get(i).importTypeCode = "reception-trec";

				Logger.error("!!! ON MET A JOUR L'IMPORT TYPE CODE !!!");

				containersList.get(i).traceInformation.modifyDate = new Date();
				containersList.get(i).traceInformation.modifyUser = "ngl-support";

				for (int j = 0; j < containersList.get(i).contents.size(); j++) {
					if (containersList.get(i).contents.get(j).sampleCode.equals(sample.code)) {
						containersList.get(i).contents.get(j).properties.put("trecStation", new PropertySingleValue("non renseigné"));
						containersList.get(i).contents.get(j).properties.put("trecProtocol", new PropertySingleValue("non renseigné"));
						containersList.get(i).contents.get(j).properties.put("trecBarcode", new PropertySingleValue(sample.code)); 
					}
				}
			}
			
			ContextValidation contextValidation = ContextValidation.createUpdateContext("ngl-support");
			containersList.get(i).validate(contextValidation, null, null);
			
			if (contextValidation.hasErrors()) {
				((List<String>) validation.getObject("RecapContainer")).add("Container (" + containersList.get(i).code + ") has errors : " + contextValidation.getErrors());
			} else {
				MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME, containersList.get(i));
			}
		}
	
		// 3ème requête : chercher les containers qui ont un contents.properties.fromSampleCode.value avec l'ancien code sample erroné.

		containersList = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class, DBQuery.is("contents.properties.fromSampleCode.value", oldSampleCode)).toList();
	
		for (int i = 0; i < containersList.size(); i++) {
			for (int j = 0; j < containersList.get(i).contents.size(); j++) {
				if (containersList.get(i).contents.get(j).properties.get("fromSampleCode") != null && containersList.get(i).contents.get(j).properties.get("fromSampleCode").value.equals(oldSampleCode)) {
					((List<String>) validation.getObject("RecapContainer")).add(containersList.get(i).code);
					
					containersList.get(i).traceInformation.modifyDate = new Date();
					containersList.get(i).traceInformation.modifyUser = "ngl-support"; 

					containersList.get(i).contents.get(j).properties.get("fromSampleCode").value = sample.code;
				}
			}

			ContextValidation contextValidation = ContextValidation.createUpdateContext("ngl-support");
			containersList.get(i).validate(contextValidation, null, null);
			
			if (contextValidation.hasErrors()) {
				((List<String>) validation.getObject("RecapContainer")).add("Container (" + containersList.get(i).code + ") has errors : " + contextValidation.getErrors());
			} else {
				MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME, containersList.get(i));
			}
		}
	}

	private void handleChildSampleUpdate(String oldSampleCode, Sample sample) {
		List<Sample> childsList = MongoDBDAO.find(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, DBQuery.regex("life.path", Pattern.compile(oldSampleCode))).toList();

		for (int j = 0; j < childsList.size(); j++) {
			((List<String>) validation.getObject("RecapChildSample")).add(childsList.get(j).code);

			childsList.get(j).life.path = childsList.get(j).life.path.replace(oldSampleCode, sample.code);

			if (childsList.get(j).life.from.sampleCode.equals(oldSampleCode)) {
				childsList.get(j).life.from.sampleCode = sample.code;
			}

			childsList.get(j).traceInformation.modifyDate = new Date();
			childsList.get(j).traceInformation.modifyUser = "ngl-support";

			ContextValidation contextValidation = ContextValidation.createUpdateContext("ngl-support");
			childsList.get(j).validate(contextValidation);

			if (contextValidation.hasErrors()) {
				((List<String>) validation.getObject("RecapChildSample")).add("Child sample (" + childsList.get(j).code + ") has errors : " + contextValidation.getErrors());
			} else {
				MongoDBDAO.update(InstanceConstants.SAMPLE_COLL_NAME, childsList.get(j));
			}
		}
	}
}