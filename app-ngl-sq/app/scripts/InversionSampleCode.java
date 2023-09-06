package scripts;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptWithExcelBody;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.ContainerSupport;
import models.laboratory.container.instance.Content;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.processes.instance.Process;
import models.laboratory.run.instance.Analysis;
import models.laboratory.run.instance.File;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.run.instance.Run;
import models.laboratory.sample.instance.Sample;
import models.utils.InstanceConstants;
import play.Logger;
import validation.ContextValidation;

/**
 * Script créer pour inversion filtre TARA (NGL-2298)
 * Echange codeSample/codeProjet dans tous les champs liés à un sampleCode ou projectCode : sampleCode, projectCode, fromSampleCode, fromProjectCode, life.path, readSetCode, pathReadSet, filesName ReadSet
 * Cas spécifique support pas de recherche sur les champs : taPcrBlank1SampleCode, tagPcrBlank2SampleCode, extractionBlankSampleCode (à rajouter si le cas se présente)
 * Recherche de ces champs dans toutes les collections : sample, container, containerSupport, experiment, process, analysis, readSet, run
 * NB : Sans inversion de propriétés de sample 
 * Entrée fichier excel : sample/project code à remplacer et nouveau sample/project code
 * Entête fichier mapping : sampleCode projectCode newSampleCode newProjectCode   
 * @author ejacoby
 *
 */
public class InversionSampleCode extends ScriptWithExcelBody{

	final String user = "ngl-admin";

	public static class Args {
	}

	@SuppressWarnings("unchecked")
	@Override
	public void execute(XSSFWorkbook workbook) throws Exception {
		ContextValidation cv = ContextValidation.createUpdateContext(user);
		cv.putObject("RecapSample", new ArrayList<String>());
		cv.putObject("RecapContainer", new ArrayList<String>());
		cv.putObject("RecapContainerSupport", new ArrayList<String>());
		cv.putObject("RecapExperiment", new ArrayList<String>());
		cv.putObject("RecapProcess", new ArrayList<String>());
		cv.putObject("RecapRun", new ArrayList<String>());
		cv.putObject("RecapReadSet", new ArrayList<String>());
		cv.putObject("RecapAnalysis", new ArrayList<String>());
		cv.putObject("RecapListProjects", new ArrayList<String>());
		//Add Header Recap
		((List<String>)cv.getObject("RecapSample")).add("Code sample,collection,propriete collection MAJ,code collection,valeur initiale,valeur finale");
		((List<String>)cv.getObject("RecapContainer")).add("Code sample,collection,propriete collection MAJ,code collection,valeur initiale,valeur finale");
		((List<String>)cv.getObject("RecapContainerSupport")).add("Code sample,collection,propriete collection MAJ,code collection,valeur initiale,valeur finale");
		((List<String>)cv.getObject("RecapExperiment")).add("Code sample,collection,propriete collection MAJ,code collection,valeur initiale,valeur finale");
		((List<String>)cv.getObject("RecapProcess")).add("Code sample,collection,propriete collection MAJ,code collection,valeur initiale,valeur finale");
		((List<String>)cv.getObject("RecapRun")).add("Code sample,collection,propriete collection MAJ,code collection,valeur initiale,valeur finale");
		((List<String>)cv.getObject("RecapReadSet")).add("Code sample,collection,propriete collection MAJ,code collection,valeur initiale,valeur finale");
		((List<String>)cv.getObject("RecapAnalysis")).add("Code sample,collection,propriete collection MAJ,code collection,valeur initiale,valeur finale");
		((List<String>)cv.getObject("RecapListProjects")).add("Code sample,collection,propriete collection MAJ,code collection,valeur initiale,valeur finale");

		List<String> listNewSampleCodes = new ArrayList<String>();

		workbook.getSheetAt(0).rowIterator().forEachRemaining(row -> {
			if(row.getRowNum() == 0) return; // skip header
			if(row!=null && row.getCell(0)!=null && row.getCell(1)!=null){

				String sampleCode = row.getCell(0).getStringCellValue();
				String projectCode = row.getCell(1).getStringCellValue();
				String newSampleCode = row.getCell(2).getStringCellValue();
				String newProjectCode = row.getCell(3).getStringCellValue();
				if(!projectCode.equals("")){
					Logger.debug("project Code"+projectCode+" sampleCode "+sampleCode);
					//Search update sample
					updateSample(sampleCode, projectCode, newSampleCode, newProjectCode, cv);
					//Search update in container
					updateContainer(sampleCode, newSampleCode, newProjectCode, cv);
					//Search update in container support
					updateContainerSupport(sampleCode, newSampleCode, newProjectCode, cv);
					//Search update experiment
					updateExperiment(sampleCode, newSampleCode, newProjectCode,cv);
					//Search update process
					updateProcess(sampleCode, newSampleCode, newProjectCode, cv);
					//Search update analysis
					updateAnalysis(sampleCode, newSampleCode, newProjectCode,cv);
					//Search update run
					updateRun(sampleCode, newSampleCode, newProjectCode, cv);
					//Search update readSet
					updateReadSet(sampleCode, newSampleCode, newProjectCode, cv);
					listNewSampleCodes.add(newSampleCode);
				}

			}
		});
		//TODO updateListeProjectCodes(listNewSampleCodes, cv);
		//Get error
		if(cv.hasErrors()){
			Logger.debug(cv.getErrors().toString());
		}
		//Create excel file to recap in execution directory
		createExcelFileRecap(cv);
		Logger.debug("End InversionSampleCode");
	}


	@SuppressWarnings("unchecked")
	private void updateSample(String sampleCode, String projectCode, String newSampleCode, String newProjectCode, ContextValidation cv)
	{
		Logger.debug("Update Sample "+new Date());
		Sample sample = MongoDBDAO.findByCode(InstanceConstants.SAMPLE_COLL_NAME, Sample.class,sampleCode);

		if(sample!=null){
			Logger.debug(sampleCode+","+InstanceConstants.SAMPLE_COLL_NAME+",code,"+sample.code);
			Set<String> newProjectCodes = new HashSet<String>();
			newProjectCodes.add(newProjectCode);
			String newName=newSampleCode.substring(newSampleCode.indexOf("_")+1);
			((List<String>)cv.getObject("RecapSample")).add(sampleCode+","+InstanceConstants.SAMPLE_COLL_NAME+",code,"+sample.code+","+sample.code+","+newSampleCode);
			((List<String>)cv.getObject("RecapSample")).add(sampleCode+","+InstanceConstants.SAMPLE_COLL_NAME+",projectCodes,"+sample.code+","+sample.projectCodes+","+newProjectCodes);
			((List<String>)cv.getObject("RecapSample")).add(sampleCode+","+InstanceConstants.SAMPLE_COLL_NAME+",name,"+sample.code+","+sample.name+","+newName);
			MongoDBDAO.update(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, 
					DBQuery.is("code", sampleCode),  
					DBUpdate.set("code",newSampleCode).set("projectCodes", newProjectCodes).set("name", newName));
		}
		Logger.debug("Update Sample life"+new Date());
		List<Sample> sampleLifes = MongoDBDAO.find(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, DBQuery.is("life.from.sampleCode", sampleCode)).toList();
		for(Sample sampleLife : sampleLifes){
			Logger.debug(sampleCode+","+InstanceConstants.SAMPLE_COLL_NAME+",life.from.sampleCode,"+sampleLife.code);
			((List<String>)cv.getObject("RecapSample")).add(sampleCode+","+InstanceConstants.SAMPLE_COLL_NAME+",life.from.sampleCode,"+sampleLife.code+","+sampleLife.life.from.sampleCode+","+newSampleCode);
			((List<String>)cv.getObject("RecapSample")).add(sampleCode+","+InstanceConstants.SAMPLE_COLL_NAME+",life.from.projectCode,"+sampleLife.code+","+sampleLife.life.from.projectCode+","+newProjectCode);

			MongoDBDAO.update(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, 
					DBQuery.is("code", sampleLife.code),  
					DBUpdate.set("life.from.sampleCode",newSampleCode).set("life.from.projectCode", newProjectCode));
		}

		Logger.debug("Update Sample path"+new Date());
		List<Sample> samplePaths = MongoDBDAO.find(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, DBQuery.regex("life.path", Pattern.compile(","+sampleCode+","))).toList();
		for(Sample samplePath : samplePaths){
			String newPath = samplePath.life.path.replace(","+sampleCode, ","+newSampleCode);
			Logger.debug(sampleCode+","+InstanceConstants.SAMPLE_COLL_NAME+",life.path,"+samplePath.code+","+samplePath.life.path+","+newPath);
			((List<String>)cv.getObject("RecapSample")).add(sampleCode+","+InstanceConstants.SAMPLE_COLL_NAME+",life.path,"+samplePath.code+","+samplePath.life.path.replaceAll(",", "#")+","+newPath.replaceAll(",", "#"));

			MongoDBDAO.update(InstanceConstants.SAMPLE_COLL_NAME, Sample.class,
					DBQuery.is("code", samplePath.code),
					DBUpdate.set("life.path", newPath));
		}
		Logger.debug("Update Sample path"+new Date());
		List<Sample> samplePaths2 = MongoDBDAO.find(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, DBQuery.regex("life.path", Pattern.compile(","+sampleCode+"$"))).toList();
		for(Sample samplePath : samplePaths2){
			String newPath = samplePath.life.path.replace(","+sampleCode, ","+newSampleCode);
			Logger.debug(sampleCode+","+InstanceConstants.SAMPLE_COLL_NAME+",life.path,"+samplePath.code+","+samplePath.life.path+","+newPath);
			((List<String>)cv.getObject("RecapSample")).add(sampleCode+","+InstanceConstants.SAMPLE_COLL_NAME+",life.path,"+samplePath.code+","+samplePath.life.path.replaceAll(",", "#")+","+newPath.replaceAll(",", "#"));

			MongoDBDAO.update(InstanceConstants.SAMPLE_COLL_NAME, Sample.class,
					DBQuery.is("code", samplePath.code),
					DBUpdate.set("life.path", newPath));
		}
	}

	@SuppressWarnings("unchecked")
	private void updateContainer(String sampleCode, String newSampleCode, String newProjectCode, ContextValidation cv)
	{
		Logger.debug("Update Container"+new Date());
		List<Container> containers = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class, DBQuery.in("sampleCodes", sampleCode)).toList();
		for(Container container : containers){
			Set<String> newSampleCodes = updateSampleCodes(container.sampleCodes, sampleCode, newSampleCode);
			Logger.debug(sampleCode+","+InstanceConstants.CONTAINER_COLL_NAME+",sampleCodes,"+container.code);
			((List<String>)cv.getObject("RecapContainer")).add(sampleCode+","+InstanceConstants.CONTAINER_COLL_NAME+",sampleCodes,"+container.code+","+container.sampleCodes.toString().replaceAll(",", "#")+","+newSampleCodes.toString().replaceAll(",", "#"));
			Set<String> newProjectCodes = updateProjecCodes(container.projectCodes, newProjectCode);
			((List<String>)cv.getObject("RecapContainer")).add(sampleCode+","+InstanceConstants.CONTAINER_COLL_NAME+",projectCodes,"+container.code+","+container.projectCodes.toString().replaceAll(",", "#")+","+newProjectCodes.toString().replaceAll(",", "#"));

			MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME, Container.class,
					DBQuery.is("code", container.code),
					DBUpdate.set("sampleCodes", newSampleCodes).set("projectCodes", newProjectCodes));
		}
		Logger.debug("Update Container content sampleCode"+new Date());
		List<Container> containersContent = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class, DBQuery.is("contents.sampleCode", sampleCode)).toList();
		for(Container containerContent : containersContent){
			for(Content content : containerContent.contents){
				if(content.sampleCode.equals(sampleCode)){
					Logger.debug(sampleCode+","+InstanceConstants.CONTAINER_COLL_NAME+",contents.sampleCode,"+containerContent.code);
					((List<String>)cv.getObject("RecapContainer")).add(sampleCode+","+InstanceConstants.CONTAINER_COLL_NAME+",contents.sampleCode,"+containerContent.code+","+content.sampleCode+","+newSampleCode);
					((List<String>)cv.getObject("RecapContainer")).add(sampleCode+","+InstanceConstants.CONTAINER_COLL_NAME+",contents.projectCode,"+containerContent.code+","+content.projectCode+","+newProjectCode);
					content.sampleCode=newSampleCode;
					content.projectCode=newProjectCode;
				}
			}
			MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME, Container.class,
					DBQuery.is("code", containerContent.code),
					DBUpdate.set("contents", containerContent.contents));
		}
		Logger.debug("Update Container content fromSampleCode"+new Date());
		List<Container> containerProperties = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class, DBQuery.is("contents.properties.fromSampleCode.value", sampleCode)).toList();
		for(Container containerProperty : containerProperties){
			for(Content content : containerProperty.contents){
				if(content.properties.get("fromSampleCode")!=null && content.properties.get("fromSampleCode").getValue().equals(sampleCode)){
					PropertyValue propertyValue = content.properties.get("fromSampleCode");
					PropertyValue propertyValueProject = content.properties.get("fromProjectCode");
					Logger.debug(sampleCode+","+InstanceConstants.CONTAINER_COLL_NAME+",contents.properties.fromSampleCode,"+containerProperty.code);
					((List<String>)cv.getObject("RecapContainer")).add(sampleCode+","+InstanceConstants.CONTAINER_COLL_NAME+",contents.properties.fromSampleCode.value,"+containerProperty.code+","+propertyValue.getValue()+","+newSampleCode);
					((List<String>)cv.getObject("RecapContainer")).add(sampleCode+","+InstanceConstants.CONTAINER_COLL_NAME+",contents.properties.fromProjectCode.value,"+containerProperty.code+","+propertyValueProject.getValue()+","+newProjectCode);
					content.properties.get("fromSampleCode").assignValue(newSampleCode);
					content.properties.get("fromProjectCode").assignValue(newProjectCode);
				}
			}
			MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME, Container.class,
					DBQuery.is("code", containerProperty.code),
					DBUpdate.set("contents", containerProperty.contents));
		}
		//Add update tagPCR
		Logger.debug("Update Container tagPcrBlank1SampleCode"+new Date());
		List<Container> containerPropertiesTagPCR1 = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class, DBQuery.is("contents.properties.tagPcrBlank1SampleCode.value", sampleCode)).toList();
		for(Container containerPropertyTagPCR1 : containerPropertiesTagPCR1){
			for(Content content : containerPropertyTagPCR1.contents){
				if(content.properties.get("tagPcrBlank1SampleCode")!=null && content.properties.get("tagPcrBlank1SampleCode").getValue().equals(sampleCode)){
					PropertyValue propertyValue = content.properties.get("tagPcrBlank1SampleCode");
					Logger.debug(sampleCode+","+InstanceConstants.CONTAINER_COLL_NAME+",contents.properties.tagPcrBlank1SampleCode,"+containerPropertyTagPCR1.code);
					((List<String>)cv.getObject("RecapContainer")).add(sampleCode+","+InstanceConstants.CONTAINER_COLL_NAME+",contents.properties.tagPcrBlank1SampleCode.value,"+containerPropertyTagPCR1.code+","+propertyValue.getValue()+","+newSampleCode);
					content.properties.get("tagPcrBlank1SampleCode").assignValue(newSampleCode);
				}
			}
			MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME, Container.class,
					DBQuery.is("code", containerPropertyTagPCR1.code),
					DBUpdate.set("contents", containerPropertyTagPCR1.contents));
		}
		Logger.debug("Update Container tagPcrBlank2SampleCode"+new Date());
		List<Container> containerPropertiesTagPCR2 = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class, DBQuery.is("contents.properties.tagPcrBlank2SampleCode.value", sampleCode)).toList();
		for(Container containerPropertyTagPCR2 : containerPropertiesTagPCR2){
			for(Content content : containerPropertyTagPCR2.contents){
				if(content.properties.get("tagPcrBlank2SampleCode")!=null && content.properties.get("tagPcrBlank2SampleCode").getValue().equals(sampleCode)){
					PropertyValue propertyValue = content.properties.get("tagPcrBlank2SampleCode");
					Logger.debug(sampleCode+","+InstanceConstants.CONTAINER_COLL_NAME+",contents.properties.tagPcrBlank2SampleCode,"+containerPropertyTagPCR2.code);
					((List<String>)cv.getObject("RecapContainer")).add(sampleCode+","+InstanceConstants.CONTAINER_COLL_NAME+",contents.properties.tagPcrBlank2SampleCode.value,"+containerPropertyTagPCR2.code+","+propertyValue.getValue()+","+newSampleCode);
					content.properties.get("tagPcrBlank2SampleCode").assignValue(newSampleCode);
				}
			}
			MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME, Container.class,
					DBQuery.is("code", containerPropertyTagPCR2.code),
					DBUpdate.set("contents", containerPropertyTagPCR2.contents));
		}
		Logger.debug("Update Container extractionBlankSampleCode"+new Date());
		List<Container> containerPropertiesBlankSample = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class, DBQuery.is("contents.properties.extractionBlankSampleCode.value", sampleCode)).toList();
		for(Container containerPropertyBlankSample : containerPropertiesBlankSample){
			for(Content content : containerPropertyBlankSample.contents){
				if(content.properties.get("extractionBlankSampleCode")!=null && content.properties.get("extractionBlankSampleCode").getValue().equals(sampleCode)){
					PropertyValue propertyValue = content.properties.get("extractionBlankSampleCode");
					Logger.debug(sampleCode+","+InstanceConstants.CONTAINER_COLL_NAME+",contents.properties.extractionBlankSampleCode,"+containerPropertyBlankSample.code);
					((List<String>)cv.getObject("RecapContainer")).add(sampleCode+","+InstanceConstants.CONTAINER_COLL_NAME+",contents.properties.extractionBlankSampleCode.value,"+containerPropertyBlankSample.code+","+propertyValue.getValue()+","+newSampleCode);
					content.properties.get("extractionBlankSampleCode").assignValue(newSampleCode);
				}
			}
			MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME, Container.class,
					DBQuery.is("code", containerPropertyBlankSample.code),
					DBUpdate.set("contents", containerPropertyBlankSample.contents));
		}

	}

	@SuppressWarnings("unchecked")
	private void updateContainerSupport(String sampleCode, String newSampleCode, String newProjectCode, ContextValidation cv)
	{
		Logger.debug("Update Container Support"+new Date());
		List<ContainerSupport> containerSupports = MongoDBDAO.find(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, ContainerSupport.class, DBQuery.in("sampleCodes", sampleCode)).toList();
		for(ContainerSupport cs : containerSupports){
			Set<String> newSampleCodes = updateSampleCodes(cs.sampleCodes, sampleCode, newSampleCode);
			Set<String> newProjectCodes = updateProjecCodes(cs.projectCodes, newProjectCode);
			Logger.debug(sampleCode+","+InstanceConstants.CONTAINER_SUPPORT_COLL_NAME+",sampleCodes,"+cs.code);
			((List<String>)cv.getObject("RecapContainerSupport")).add(sampleCode+","+InstanceConstants.CONTAINER_SUPPORT_COLL_NAME+",sampleCodes,"+cs.code+","+cs.sampleCodes.toString().replaceAll(",", "#")+","+newSampleCodes.toString().replaceAll(",", "#"));
			((List<String>)cv.getObject("RecapContainerSupport")).add(sampleCode+","+InstanceConstants.CONTAINER_SUPPORT_COLL_NAME+",projectCodes,"+cs.code+","+cs.projectCodes.toString().replaceAll(",", "#")+","+newProjectCodes.toString().replaceAll(",", "#"));

			MongoDBDAO.update(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, ContainerSupport.class,
					DBQuery.is("code", cs.code),
					DBUpdate.set("sampleCodes", newSampleCodes).set("projectCodes", newProjectCodes));
		}
	}

	@SuppressWarnings("unchecked")
	private void updateExperiment(String sampleCode, String newSampleCode, String newProjectCode, ContextValidation cv)
	{
		Logger.debug("Update Experiment"+new Date());
		List<Experiment> experiments = MongoDBDAO.find(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, DBQuery.in("sampleCodes", sampleCode)).toList();
		for(Experiment experiment : experiments){
			Set<String> newSampleCodes = updateSampleCodes(experiment.sampleCodes, sampleCode, newSampleCode);
			Set<String> newProjectCodes = updateProjecCodes(experiment.projectCodes, newProjectCode);
			Logger.debug(sampleCode+","+InstanceConstants.EXPERIMENT_COLL_NAME+",sampleCodes,"+experiment.code);
			((List<String>)cv.getObject("RecapExperiment")).add(sampleCode+","+InstanceConstants.EXPERIMENT_COLL_NAME+",sampleCodes,"+experiment.code+","+experiment.sampleCodes.toString().replaceAll(",", "#")+","+newSampleCodes.toString().replaceAll(",", "#"));
			((List<String>)cv.getObject("RecapExperiment")).add(sampleCode+","+InstanceConstants.EXPERIMENT_COLL_NAME+",projectCodes,"+experiment.code+","+experiment.projectCodes.toString().replaceAll(",", "#")+","+newProjectCodes.toString().replaceAll(",", "#"));

			MongoDBDAO.update(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class,
					DBQuery.is("code", experiment.code),
					DBUpdate.set("sampleCodes", newSampleCodes).set("projectCodes", newProjectCodes));
		}

		Logger.debug("Update Experiment content sampleCode"+new Date());
		List<Experiment> expICUContents = MongoDBDAO.find(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, DBQuery.is("atomicTransfertMethods.inputContainerUseds.contents.sampleCode", sampleCode)).toList();
		for(Experiment expICUContent : expICUContents){
			expICUContent.atomicTransfertMethods.forEach(atm->{
				atm.inputContainerUseds.forEach(icu->{
					if(icu.sampleCodes.contains(sampleCode)){
						Set<String> newSampleCodes = updateSampleCodes(icu.sampleCodes, sampleCode, newSampleCode);
						Set<String> newProjectCodes = updateProjecCodes(icu.projectCodes, newProjectCode);
						Logger.debug(sampleCode+","+InstanceConstants.EXPERIMENT_COLL_NAME+",atomicTransfertMethods.inputContainerUseds.sampleCodes,"+expICUContent.code);
						((List<String>)cv.getObject("RecapExperiment")).add(sampleCode+","+InstanceConstants.EXPERIMENT_COLL_NAME+",atomicTransfertMethods.inputContainerUseds.sampleCodes,"+expICUContent.code+","+icu.sampleCodes.toString().replaceAll(",", "#")+","+newSampleCodes.toString().replaceAll(",", "#"));
						((List<String>)cv.getObject("RecapExperiment")).add(sampleCode+","+InstanceConstants.EXPERIMENT_COLL_NAME+",atomicTransfertMethods.inputContainerUseds.projectCodes,"+expICUContent.code+","+icu.projectCodes+","+newProjectCodes);
						icu.sampleCodes=newSampleCodes;
						icu.projectCodes=newProjectCodes;
					}
					icu.contents.stream().forEach(content->{
						if(content.sampleCode.equals(sampleCode)){
							Logger.debug(sampleCode+","+InstanceConstants.EXPERIMENT_COLL_NAME+",atomicTransfertMethods.inputContainerUseds.contents.sampleCode,"+expICUContent.code);
							((List<String>)cv.getObject("RecapExperiment")).add(sampleCode+","+InstanceConstants.EXPERIMENT_COLL_NAME+",atomicTransfertMethods.inputContainerUseds.contents.sampleCode,"+expICUContent.code+","+content.sampleCode+","+newSampleCode);
							((List<String>)cv.getObject("RecapExperiment")).add(sampleCode+","+InstanceConstants.EXPERIMENT_COLL_NAME+",atomicTransfertMethods.inputContainerUseds.contents.projectCode,"+expICUContent.code+","+content.projectCode+","+newProjectCode);
							content.sampleCode=newSampleCode;
							content.projectCode=newProjectCode;
						}
					});
				});
			});
			MongoDBDAO.update(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class,
					DBQuery.is("code", expICUContent.code),
					DBUpdate.set("atomicTransfertMethods", expICUContent.atomicTransfertMethods));
		}
		Logger.debug("Update Experiment content fromSampleCode"+new Date());
		List<Experiment> expICUContentFromSampleCodes = MongoDBDAO.find(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, DBQuery.is("atomicTransfertMethods.inputContainerUseds.contents.properties.fromSampleCode.value", sampleCode)).toList();
		for(Experiment eicucfsc : expICUContentFromSampleCodes){
			eicucfsc.atomicTransfertMethods.forEach(atm->{
				atm.inputContainerUseds.forEach(icu->{
					icu.contents.stream().forEach(content->{
						if(content.properties.get("fromSampleCode")!=null && content.properties.get("fromSampleCode").getValue().equals(sampleCode)){
							PropertyValue propertyValue = content.properties.get("fromSampleCode");
							PropertyValue propertyValueProject = content.properties.get("fromProjectCode");
							Logger.debug(sampleCode+","+InstanceConstants.EXPERIMENT_COLL_NAME+",atomicTransfertMethods.inputContainerUseds.contents.properties.fromSampleCode,"+eicucfsc.code);
							((List<String>)cv.getObject("RecapExperiment")).add(sampleCode+","+InstanceConstants.EXPERIMENT_COLL_NAME+",atomicTransfertMethods.inputContainerUseds.contents.properties.fromSampleCode,"+eicucfsc.code+","+propertyValue.getValue()+","+newSampleCode);
							((List<String>)cv.getObject("RecapExperiment")).add(sampleCode+","+InstanceConstants.EXPERIMENT_COLL_NAME+",atomicTransfertMethods.inputContainerUseds.contents.properties.fromProjectCode,"+eicucfsc.code+","+propertyValueProject.getValue()+","+newProjectCode);
							content.properties.get("fromSampleCode").assignValue(newSampleCode);
							content.properties.get("fromProjectCode").assignValue(newProjectCode);
						}
					});
				});
			});

			MongoDBDAO.update(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class,
					DBQuery.is("code", eicucfsc.code),
					DBUpdate.set("atomicTransfertMethods", eicucfsc.atomicTransfertMethods));
		}

		//ADD Update tagPCRBlank
		Logger.debug("Update Experiment icu tagPcrBlank1SampleCode"+new Date());
		List<Experiment> expICUContenttagPcrBlank1SampleCode = MongoDBDAO.find(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, DBQuery.is("atomicTransfertMethods.inputContainerUseds.contents.properties.tagPcrBlank1SampleCode.value", sampleCode)).toList();
		for(Experiment eicupcr1 : expICUContenttagPcrBlank1SampleCode){
			eicupcr1.atomicTransfertMethods.forEach(atm->{
				atm.inputContainerUseds.forEach(icu->{
					icu.contents.stream().forEach(content->{
						if(content.properties.get("tagPcrBlank1SampleCode")!=null && content.properties.get("tagPcrBlank1SampleCode").getValue().equals(sampleCode)){
							PropertyValue propertyValue = content.properties.get("tagPcrBlank1SampleCode");
							Logger.debug(sampleCode+","+InstanceConstants.EXPERIMENT_COLL_NAME+",atomicTransfertMethods.inputContainerUseds.contents.properties.tagPcrBlank1SampleCode,"+eicupcr1.code);
							((List<String>)cv.getObject("RecapExperiment")).add(sampleCode+","+InstanceConstants.EXPERIMENT_COLL_NAME+",atomicTransfertMethods.inputContainerUseds.contents.properties.tagPcrBlank1SampleCode,"+eicupcr1.code+","+propertyValue.getValue()+","+newSampleCode);
							content.properties.get("tagPcrBlank1SampleCode").assignValue(newSampleCode);
						}
					});
				});
			});

			MongoDBDAO.update(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class,
					DBQuery.is("code", eicupcr1.code),
					DBUpdate.set("atomicTransfertMethods", eicupcr1.atomicTransfertMethods));
		}
		Logger.debug("Update Experiment icu tagPcrBlank2SampleCode"+new Date());
		List<Experiment> expICUContenttagPcrBlank2SampleCode = MongoDBDAO.find(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, DBQuery.is("atomicTransfertMethods.inputContainerUseds.contents.properties.tagPcrBlank2SampleCode.value", sampleCode)).toList();
		for(Experiment eicupcr2 : expICUContenttagPcrBlank2SampleCode){
			eicupcr2.atomicTransfertMethods.forEach(atm->{
				atm.inputContainerUseds.forEach(icu->{
					icu.contents.stream().forEach(content->{
						if(content.properties.get("tagPcrBlank2SampleCode")!=null && content.properties.get("tagPcrBlank2SampleCode").getValue().equals(sampleCode)){
							PropertyValue propertyValue = content.properties.get("tagPcrBlank2SampleCode");
							Logger.debug(sampleCode+","+InstanceConstants.EXPERIMENT_COLL_NAME+",atomicTransfertMethods.inputContainerUseds.contents.properties.tagPcrBlank2SampleCode,"+eicupcr2.code);
							((List<String>)cv.getObject("RecapExperiment")).add(sampleCode+","+InstanceConstants.EXPERIMENT_COLL_NAME+",atomicTransfertMethods.inputContainerUseds.contents.properties.tagPcrBlank2SampleCode,"+eicupcr2.code+","+propertyValue.getValue()+","+newSampleCode);
							content.properties.get("tagPcrBlank2SampleCode").assignValue(newSampleCode);
						}
					});
				});
			});

			MongoDBDAO.update(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class,
					DBQuery.is("code", eicupcr2.code),
					DBUpdate.set("atomicTransfertMethods", eicupcr2.atomicTransfertMethods));
		}
		Logger.debug("Update Experiment icu extractionBlankSampleCode"+new Date());
		List<Experiment> expICUContentBlankSampleCode = MongoDBDAO.find(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, DBQuery.is("atomicTransfertMethods.inputContainerUseds.contents.properties.extractionBlankSampleCode.value", sampleCode)).toList();
		for(Experiment eicublank : expICUContentBlankSampleCode){
			eicublank.atomicTransfertMethods.forEach(atm->{
				atm.inputContainerUseds.forEach(icu->{
					icu.contents.stream().forEach(content->{
						if(content.properties.get("extractionBlankSampleCode")!=null && content.properties.get("extractionBlankSampleCode").getValue().equals(sampleCode)){
							PropertyValue propertyValue = content.properties.get("extractionBlankSampleCode");
							Logger.debug(sampleCode+","+InstanceConstants.EXPERIMENT_COLL_NAME+",atomicTransfertMethods.inputContainerUseds.contents.properties.extractionBlankSampleCode,"+eicublank.code);
							((List<String>)cv.getObject("RecapExperiment")).add(sampleCode+","+InstanceConstants.EXPERIMENT_COLL_NAME+",atomicTransfertMethods.inputContainerUseds.contents.properties.extractionBlankSampleCode,"+eicublank.code+","+propertyValue.getValue()+","+newSampleCode);
							content.properties.get("extractionBlankSampleCode").assignValue(newSampleCode);
						}
					});
				});
			});

			MongoDBDAO.update(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class,
					DBQuery.is("code", eicublank.code),
					DBUpdate.set("atomicTransfertMethods", eicublank.atomicTransfertMethods));
		}

		Logger.debug("Update Experiment ocu sampleCode"+new Date());
		List<Experiment> expOCUContents = MongoDBDAO.find(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, DBQuery.is("atomicTransfertMethods.outputContainerUseds.contents.sampleCode", sampleCode)).toList();
		for(Experiment expOCUContent : expOCUContents){
			expOCUContent.atomicTransfertMethods.forEach(atm->{
				atm.outputContainerUseds.forEach(ocu->{
					if(ocu.experimentProperties!=null && ocu.experimentProperties.get("sampleCode")!=null && ocu.experimentProperties.get("sampleCode").getValue().equals(sampleCode)){
						PropertyValue propertyValue = ocu.experimentProperties.get("sampleCode");
						PropertyValue propertyValueProject = ocu.experimentProperties.get("projectCode");
						Logger.debug(sampleCode+","+InstanceConstants.EXPERIMENT_COLL_NAME+",atomicTransfertMethods.outputContainerUseds.experimentProperties.sampleCode,"+expOCUContent.code);
						((List<String>)cv.getObject("RecapExperiment")).add(sampleCode+","+InstanceConstants.EXPERIMENT_COLL_NAME+",atomicTransfertMethods.outputContainerUseds.experimentProperties.sampleCode,"+expOCUContent.code+","+propertyValue.getValue()+","+newSampleCode);
						((List<String>)cv.getObject("RecapExperiment")).add(sampleCode+","+InstanceConstants.EXPERIMENT_COLL_NAME+",atomicTransfertMethods.outputContainerUseds.experimentProperties.projectCode,"+expOCUContent.code+","+propertyValueProject.getValue()+","+newProjectCode);
						ocu.experimentProperties.get("sampleCode").assignValue(newSampleCode);
						ocu.experimentProperties.get("projectCode").assignValue(newProjectCode);
					}
					ocu.contents.stream().forEach(content->{
						if(content.sampleCode.equals(sampleCode)){
							Logger.debug(sampleCode+","+InstanceConstants.EXPERIMENT_COLL_NAME+",atomicTransfertMethods.outputContainerUseds.contents.sampleCode,"+expOCUContent.code);
							((List<String>)cv.getObject("RecapExperiment")).add(sampleCode+","+InstanceConstants.EXPERIMENT_COLL_NAME+",atomicTransfertMethods.outputContainerUseds.contents.sampleCode,"+expOCUContent.code+","+content.sampleCode+","+newSampleCode);
							((List<String>)cv.getObject("RecapExperiment")).add(sampleCode+","+InstanceConstants.EXPERIMENT_COLL_NAME+",atomicTransfertMethods.outputContainerUseds.contents.projectCode,"+expOCUContent.code+","+content.projectCode+","+newProjectCode);
							content.sampleCode=newSampleCode;
							content.projectCode=newProjectCode;
						}
					});
				});
			});

			MongoDBDAO.update(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class,
					DBQuery.is("code", expOCUContent.code),
					DBUpdate.set("atomicTransfertMethods", expOCUContent.atomicTransfertMethods));
		}
		Logger.debug("Update Experiment ocu fromSampleCode"+new Date());
		List<Experiment> expOCUContentFromSampleCodes = MongoDBDAO.find(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, DBQuery.is("atomicTransfertMethods.outputContainerUseds.contents.properties.fromSampleCode.value", sampleCode)).toList();
		for(Experiment eocucfsc : expOCUContentFromSampleCodes){
			eocucfsc.atomicTransfertMethods.forEach(atm->{
				atm.outputContainerUseds.forEach(ocu->{
					ocu.contents.stream().forEach(content->{
						if(content.properties.get("fromSampleCode")!=null && content.properties.get("fromSampleCode").getValue().equals(sampleCode)){
							PropertyValue propertyValue = content.properties.get("fromSampleCode");
							PropertyValue propertyValueProject = content.properties.get("fromProjectCode");
							Logger.debug(sampleCode+","+InstanceConstants.EXPERIMENT_COLL_NAME+",atomicTransfertMethods.outputContainerUseds.contents.properties.fromSampleCode,"+eocucfsc.code);
							((List<String>)cv.getObject("RecapExperiment")).add(sampleCode+","+InstanceConstants.EXPERIMENT_COLL_NAME+",atomicTransfertMethods.outputContainerUseds.contents.properties.fromSampleCode,"+eocucfsc.code+","+propertyValue.getValue()+","+newSampleCode);
							((List<String>)cv.getObject("RecapExperiment")).add(sampleCode+","+InstanceConstants.EXPERIMENT_COLL_NAME+",atomicTransfertMethods.outputContainerUseds.contents.properties.fromProjectCode,"+eocucfsc.code+","+propertyValueProject.getValue()+","+newProjectCode);
							content.properties.get("fromSampleCode").assignValue(newSampleCode);
							content.properties.get("fromProjectCode").assignValue(newProjectCode);
						}
					});
				});
			});

			MongoDBDAO.update(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class,
					DBQuery.is("code", eocucfsc.code),
					DBUpdate.set("atomicTransfertMethods", eocucfsc.atomicTransfertMethods));


		}


		//ADD Update tagPCRBlank
		Logger.debug("Update Experiment ocu tagPcrBlank1SampleCode"+new Date());
		List<Experiment> expOCUContenttagPcrBlank1SampleCode = MongoDBDAO.find(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, DBQuery.is("atomicTransfertMethods.outputContainerUseds.contents.properties.tagPcrBlank1SampleCode.value", sampleCode)).toList();
		for(Experiment eocupcr1 : expOCUContenttagPcrBlank1SampleCode){
			eocupcr1.atomicTransfertMethods.forEach(atm->{
				atm.outputContainerUseds.forEach(ocu->{
					if(ocu.experimentProperties!=null && ocu.experimentProperties.get("tagPcrBlank1SampleCode")!=null && ocu.experimentProperties.get("tagPcrBlank1SampleCode").getValue().equals(sampleCode)){
						PropertyValue propertyValue = ocu.experimentProperties.get("tagPcrBlank1SampleCode");
						Logger.debug(sampleCode+","+InstanceConstants.EXPERIMENT_COLL_NAME+",atomicTransfertMethods.outputContainerUseds.ocu.experimentProperties.tagPcrBlank1SampleCode,"+eocupcr1.code);
						((List<String>)cv.getObject("RecapExperiment")).add(sampleCode+","+InstanceConstants.EXPERIMENT_COLL_NAME+",atomicTransfertMethods.outputContainerUseds.ocu.experimentProperties.tagPcrBlank1SampleCode,"+eocupcr1.code+","+propertyValue.getValue()+","+newSampleCode);
						ocu.experimentProperties.get("tagPcrBlank1SampleCode").assignValue(newSampleCode);
					}
					ocu.contents.stream().forEach(content->{
						if(content.properties.get("tagPcrBlank1SampleCode")!=null && content.properties.get("tagPcrBlank1SampleCode").getValue().equals(sampleCode)){
							PropertyValue propertyValue = content.properties.get("tagPcrBlank1SampleCode");
							Logger.debug(sampleCode+","+InstanceConstants.EXPERIMENT_COLL_NAME+",atomicTransfertMethods.outputContainerUseds.contents.properties.tagPcrBlank1SampleCode,"+eocupcr1.code);
							((List<String>)cv.getObject("RecapExperiment")).add(sampleCode+","+InstanceConstants.EXPERIMENT_COLL_NAME+",atomicTransfertMethods.outputContainerUseds.contents.properties.tagPcrBlank1SampleCode,"+eocupcr1.code+","+propertyValue.getValue()+","+newSampleCode);
							content.properties.get("tagPcrBlank1SampleCode").assignValue(newSampleCode);
						}
					});
				});
			});

			MongoDBDAO.update(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class,
					DBQuery.is("code", eocupcr1.code),
					DBUpdate.set("atomicTransfertMethods", eocupcr1.atomicTransfertMethods));
		}
		Logger.debug("Update Experiment ocu tagPcrBlank2SampleCode"+new Date());
		List<Experiment> expOCUContenttagPcrBlank2SampleCode = MongoDBDAO.find(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, DBQuery.is("atomicTransfertMethods.outputContainerUseds.contents.properties.tagPcrBlank2SampleCode.value", sampleCode)).toList();
		for(Experiment eocupcr2 : expOCUContenttagPcrBlank2SampleCode){
			eocupcr2.atomicTransfertMethods.forEach(atm->{
				atm.outputContainerUseds.forEach(ocu->{
					if(ocu.experimentProperties!=null && ocu.experimentProperties.get("tagPcrBlank2SampleCode")!=null && ocu.experimentProperties.get("tagPcrBlank2SampleCode").getValue().equals(sampleCode)){
						PropertyValue propertyValue = ocu.experimentProperties.get("tagPcrBlank2SampleCode");
						Logger.debug(sampleCode+","+InstanceConstants.EXPERIMENT_COLL_NAME+",atomicTransfertMethods.outputContainerUseds.ocu.experimentProperties.tagPcrBlank2SampleCode,"+eocupcr2.code);
						((List<String>)cv.getObject("RecapExperiment")).add(sampleCode+","+InstanceConstants.EXPERIMENT_COLL_NAME+",atomicTransfertMethods.outputContainerUseds.ocu.experimentProperties.tagPcrBlank2SampleCode,"+eocupcr2.code+","+propertyValue.getValue()+","+newSampleCode);
						ocu.experimentProperties.get("tagPcrBlank2SampleCode").assignValue(newSampleCode);
					}
					ocu.contents.stream().forEach(content->{
						if(content.properties.get("tagPcrBlank2SampleCode")!=null && content.properties.get("tagPcrBlank2SampleCode").getValue().equals(sampleCode)){
							PropertyValue propertyValue = content.properties.get("tagPcrBlank2SampleCode");
							Logger.debug(sampleCode+","+InstanceConstants.EXPERIMENT_COLL_NAME+",atomicTransfertMethods.outputContainerUseds.contents.properties.tagPcrBlank2SampleCode,"+eocupcr2.code);
							((List<String>)cv.getObject("RecapExperiment")).add(sampleCode+","+InstanceConstants.EXPERIMENT_COLL_NAME+",atomicTransfertMethods.outputContainerUseds.contents.properties.tagPcrBlank2SampleCode,"+eocupcr2.code+","+propertyValue.getValue()+","+newSampleCode);
							content.properties.get("tagPcrBlank2SampleCode").assignValue(newSampleCode);
						}
					});
				});
			});

			MongoDBDAO.update(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class,
					DBQuery.is("code", eocupcr2.code),
					DBUpdate.set("atomicTransfertMethods", eocupcr2.atomicTransfertMethods));
		}
		Logger.debug("Update Experiment ocu extractionBlankSampleCode"+new Date());
		List<Experiment> expOCUContentBlankSampleCode = MongoDBDAO.find(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, DBQuery.is("atomicTransfertMethods.outputContainerUseds.contents.properties.extractionBlankSampleCode.value", sampleCode)).toList();
		for(Experiment eocublank : expOCUContentBlankSampleCode){
			eocublank.atomicTransfertMethods.forEach(atm->{
				atm.outputContainerUseds.forEach(ocu->{
					if(ocu.experimentProperties!=null && ocu.experimentProperties.get("extractionBlankSampleCode")!=null && ocu.experimentProperties.get("extractionBlankSampleCode").getValue().equals(sampleCode)){
						PropertyValue propertyValue = ocu.experimentProperties.get("extractionBlankSampleCode");
						Logger.debug(sampleCode+","+InstanceConstants.EXPERIMENT_COLL_NAME+",atomicTransfertMethods.outputContainerUseds.ocu.experimentProperties.extractionBlankSampleCode,"+eocublank.code);
						((List<String>)cv.getObject("RecapExperiment")).add(sampleCode+","+InstanceConstants.EXPERIMENT_COLL_NAME+",atomicTransfertMethods.outputContainerUseds.ocu.experimentProperties.extractionBlankSampleCode,"+eocublank.code+","+propertyValue.getValue()+","+newSampleCode);
						ocu.experimentProperties.get("extractionBlankSampleCode").assignValue(newSampleCode);
					}
					ocu.contents.stream().forEach(content->{
						if(content.properties.get("extractionBlankSampleCode")!=null && content.properties.get("extractionBlankSampleCode").getValue().equals(sampleCode)){
							PropertyValue propertyValue = content.properties.get("extractionBlankSampleCode");
							Logger.debug(sampleCode+","+InstanceConstants.EXPERIMENT_COLL_NAME+",atomicTransfertMethods.outputContainerUseds.contents.properties.extractionBlankSampleCode,"+eocublank.code);
							((List<String>)cv.getObject("RecapExperiment")).add(sampleCode+","+InstanceConstants.EXPERIMENT_COLL_NAME+",atomicTransfertMethods.outputContainerUseds.contents.properties.extractionBlankSampleCode,"+eocublank.code+","+propertyValue.getValue()+","+newSampleCode);
							content.properties.get("extractionBlankSampleCode").assignValue(newSampleCode);
						}
					});
				});
			});

			MongoDBDAO.update(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class,
					DBQuery.is("code", eocublank.code),
					DBUpdate.set("atomicTransfertMethods", eocublank.atomicTransfertMethods));
		}

	}


	@SuppressWarnings("unchecked")
	private void updateProcess(String sampleCode, String newSampleCode, String newProjectCode, ContextValidation cv)
	{
		Logger.debug("Update Process"+new Date());
		List<Process> processes = MongoDBDAO.find(InstanceConstants.PROCESS_COLL_NAME, Process.class, DBQuery.in("sampleCodes", sampleCode)).toList();
		for(Process p : processes){
			Logger.debug(sampleCode+","+InstanceConstants.PROCESS_COLL_NAME+",sampleCodes,"+p.code);
			Set<String> newSampleCodes = updateSampleCodes(p.sampleCodes, sampleCode, newSampleCode);
			Set<String> newProjectCodes = updateProjecCodes(p.projectCodes, newProjectCode);
			((List<String>)cv.getObject("RecapProcess")).add(sampleCode+","+InstanceConstants.PROCESS_COLL_NAME+",sampleCodes,"+p.code+","+p.sampleCodes.toString().replaceAll(",", "#")+","+newSampleCodes.toString().replaceAll(",", "#"));
			((List<String>)cv.getObject("RecapProcess")).add(sampleCode+","+InstanceConstants.PROCESS_COLL_NAME+",projectCodes,"+p.code+","+p.projectCodes.toString().replaceAll(",", "#")+","+newProjectCodes.toString().replaceAll(",", "#"));

			MongoDBDAO.update(InstanceConstants.PROCESS_COLL_NAME, Process.class,
					DBQuery.is("code", p.code),
					DBUpdate.set("sampleCodes", newSampleCodes).set("projectCodes", newProjectCodes));
		}
		Logger.debug("Update Process soc sampleCode"+new Date());
		List<Process> processSOC = MongoDBDAO.find(InstanceConstants.PROCESS_COLL_NAME, Process.class, DBQuery.is("sampleOnInputContainer.sampleCode", sampleCode)).toList();
		for(Process pSOC:processSOC){
			Logger.debug(sampleCode+","+InstanceConstants.PROCESS_COLL_NAME+",sampleOnInputContainer.sampleCode,"+pSOC.code);
			((List<String>)cv.getObject("RecapProcess")).add(sampleCode+","+InstanceConstants.PROCESS_COLL_NAME+",sampleOnInputContainer.sampleCode,"+pSOC.code+","+pSOC.sampleOnInputContainer.sampleCode+","+newSampleCode);
			((List<String>)cv.getObject("RecapProcess")).add(sampleCode+","+InstanceConstants.PROCESS_COLL_NAME+",sampleOnInputContainer.projectCode,"+pSOC.code+","+pSOC.sampleOnInputContainer.projectCode+","+newProjectCode);

			MongoDBDAO.update(InstanceConstants.PROCESS_COLL_NAME, Process.class,
					DBQuery.is("code", pSOC.code),
					DBUpdate.set("sampleOnInputContainer.sampleCode", newSampleCode).set("sampleOnInputContainer.projectCode", newProjectCode));
		}
		Logger.debug("Update Process soc fromSampleCode"+new Date());
		List<Process> processSOCFromSampleCode = MongoDBDAO.find(InstanceConstants.PROCESS_COLL_NAME, Process.class, DBQuery.is("sampleOnInputContainer.properties.fromSampleCode.value", sampleCode)).toList();
		for(Process pSOCFSC:processSOCFromSampleCode){
			PropertyValue propertyValue = pSOCFSC.sampleOnInputContainer.properties.get("fromSampleCode");
			PropertyValue propertyProjectValue = pSOCFSC.sampleOnInputContainer.properties.get("fromProjectCode");
			Logger.debug(sampleCode+","+InstanceConstants.PROCESS_COLL_NAME+",sampleOnInputContainer.properties.fromSampleCode.value,"+pSOCFSC.code);
			((List<String>)cv.getObject("RecapProcess")).add(sampleCode+","+InstanceConstants.PROCESS_COLL_NAME+",sampleOnInputContainer.properties.fromSampleCode.value,"+pSOCFSC.code+","+propertyValue.getValue()+","+newSampleCode);
			((List<String>)cv.getObject("RecapProcess")).add(sampleCode+","+InstanceConstants.PROCESS_COLL_NAME+",sampleOnInputContainer.properties.fromProjectCode.value,"+pSOCFSC.code+","+propertyProjectValue.getValue()+","+newProjectCode);
			pSOCFSC.properties.get("fromSampleCode").assignValue(newSampleCode);
			pSOCFSC.properties.get("fromProjectCode").assignValue(newProjectCode);

			MongoDBDAO.update(InstanceConstants.PROCESS_COLL_NAME, Process.class,
					DBQuery.is("code", pSOCFSC.code),
					DBUpdate.set("sampleOnInputContainer.properties.fromSampleCode.value", newSampleCode).set("sampleOnInputContainer.properties.fromProjectCode.value", newProjectCode));
		}
		Logger.debug("Update Process soc tagPcrBlank1SampleCode"+new Date());
		List<Process> processSOCTagPCR1 = MongoDBDAO.find(InstanceConstants.PROCESS_COLL_NAME, Process.class, DBQuery.is("sampleOnInputContainer.properties.tagPcrBlank1SampleCode.value", sampleCode)).toList();
		for(Process pTagPCR1:processSOCTagPCR1){
			Logger.debug(sampleCode+","+InstanceConstants.PROCESS_COLL_NAME+",sampleOnInputContainer.properties.tagPcrBlank1SampleCode.value,"+pTagPCR1.code);
			((List<String>)cv.getObject("RecapProcess")).add(sampleCode+","+InstanceConstants.PROCESS_COLL_NAME+",sampleOnInputContainer.properties.tagPcrBlank1SampleCode.value,"+pTagPCR1.code);
			MongoDBDAO.update(InstanceConstants.PROCESS_COLL_NAME, Process.class,
					DBQuery.is("code", pTagPCR1.code),
					DBUpdate.set("sampleOnInputContainer.properties.tagPcrBlank1SampleCode.value", newSampleCode));
		}
		Logger.debug("Update Process soc tagPcrBlank2SampleCode"+new Date());
		List<Process> processSOCTagPCR2 = MongoDBDAO.find(InstanceConstants.PROCESS_COLL_NAME, Process.class, DBQuery.is("sampleOnInputContainer.properties.tagPcrBlank2SampleCode.value", sampleCode)).toList();
		for(Process pTagPCR2:processSOCTagPCR2){
			Logger.debug(sampleCode+","+InstanceConstants.PROCESS_COLL_NAME+",sampleOnInputContainer.properties.tagPcrBlank2SampleCode.value,"+pTagPCR2.code);
			((List<String>)cv.getObject("RecapProcess")).add(sampleCode+","+InstanceConstants.PROCESS_COLL_NAME+",sampleOnInputContainer.properties.tagPcrBlank2SampleCode.value,"+pTagPCR2.code);
			MongoDBDAO.update(InstanceConstants.PROCESS_COLL_NAME, Process.class,
					DBQuery.is("code", pTagPCR2.code),
					DBUpdate.set("sampleOnInputContainer.properties.tagPcrBlank2SampleCode.value", newSampleCode));
		}
		Logger.debug("Update Process soc extractionBlankSampleCode"+new Date());
		List<Process> processSOCBlank = MongoDBDAO.find(InstanceConstants.PROCESS_COLL_NAME, Process.class, DBQuery.is("sampleOnInputContainer.properties.extractionBlankSampleCode.value", sampleCode)).toList();
		for(Process pBlank:processSOCBlank){
			Logger.debug(sampleCode+","+InstanceConstants.PROCESS_COLL_NAME+",sampleOnInputContainer.properties.extractionBlankSampleCode.value,"+pBlank.code);
			((List<String>)cv.getObject("RecapProcess")).add(sampleCode+","+InstanceConstants.PROCESS_COLL_NAME+",sampleOnInputContainer.properties.extractionBlankSampleCode.value,"+pBlank.code);
			MongoDBDAO.update(InstanceConstants.PROCESS_COLL_NAME, Process.class,
					DBQuery.is("code", pBlank.code),
					DBUpdate.set("sampleOnInputContainer.properties.extractionBlankSampleCode.value", newSampleCode));
		}

	}

	@SuppressWarnings("unchecked")
	private void updateAnalysis(String sampleCode,  String newSampleCode, String newProjectCode, ContextValidation cv)
	{
		Logger.debug("Update Analysis"+new Date());
		List<Analysis> analysis = MongoDBDAO.find(InstanceConstants.ANALYSIS_COLL_NAME, Analysis.class, DBQuery.in("sampleCodes", sampleCode)).toList();
		for(Analysis a : analysis){
			Logger.debug(sampleCode+","+InstanceConstants.ANALYSIS_COLL_NAME+",sampleCodes,"+a.code);
			Set<String> newSampleCodes = updateSampleCodes(new HashSet<String>(a.sampleCodes), sampleCode, newSampleCode);
			Set<String> newProjectCodes = updateProjecCodes(new HashSet<String>(a.projectCodes), newProjectCode);
			((List<String>)cv.getObject("RecapAnalysis")).add(sampleCode+","+InstanceConstants.ANALYSIS_COLL_NAME+",sampleCodes,"+a.code+","+a.sampleCodes.toString().replaceAll(",", "#")+","+newSampleCodes.toString().replace(",", "#"));
			((List<String>)cv.getObject("RecapAnalysis")).add(sampleCode+","+InstanceConstants.ANALYSIS_COLL_NAME+",projectCodes,"+a.code+","+a.projectCodes.toString().replaceAll(",", "#")+","+newProjectCodes.toString().replace(",", "#"));
			MongoDBDAO.update(InstanceConstants.ANALYSIS_COLL_NAME, Analysis.class,
					DBQuery.is("code", a.code),
					DBUpdate.set("sampleCodes", newSampleCodes).set("projectCodes", newProjectCodes));
		}

	}

	@SuppressWarnings("unchecked")
	private void updateRun(String sampleCode, String newSampleCode, String newProjectCode, ContextValidation cv)
	{
		Logger.debug("Update Run"+new Date());
		List<Run> runs = MongoDBDAO.find(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, DBQuery.in("sampleCodes", sampleCode)).toList();
		for(Run run : runs){
			Set<String> newSampleCodes = updateSampleCodes(run.sampleCodes, sampleCode, newSampleCode);
			Set<String> newProjectCodes = updateProjecCodes(run.projectCodes, newProjectCode);
			Logger.debug(sampleCode+","+InstanceConstants.RUN_ILLUMINA_COLL_NAME+",sampleCodes,"+run.code);
			((List<String>)cv.getObject("RecapRun")).add(sampleCode+","+InstanceConstants.RUN_ILLUMINA_COLL_NAME+",sampleCodes,"+run.code+","+run.sampleCodes.toString().replaceAll(",", "#")+","+newSampleCodes.toString().replaceAll(",", "#"));
			((List<String>)cv.getObject("RecapRun")).add(sampleCode+","+InstanceConstants.RUN_ILLUMINA_COLL_NAME+",projectCodes,"+run.code+","+run.projectCodes.toString().replaceAll(",", "#")+","+newProjectCodes.toString().replaceAll(",", "#"));

			MongoDBDAO.update(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class,
					DBQuery.is("code", run.code),
					DBUpdate.set("sampleCodes", newSampleCodes).set("projectCodes", newProjectCodes));
		}


	}

	private boolean containRS(List<String> listRS, String sampleCode)
	{
		for(String rsCode : listRS){
			if(rsCode.contains(sampleCode))
				return true;
		}
		return false;
	}

	/*private List<String> updateListReadSetCodes_(List<String> initListReadSetCodes, String oldSampleCode, String newSampleCode)
	{
		List<String> listToModif = new ArrayList<String>();
		listToModif.addAll(initListReadSetCodes);
		List<String> newListReadSetCodes = listToModif.stream().map(s->{
			if(s.startsWith(oldSampleCode))
				return s.replaceFirst(oldSampleCode, newSampleCode);
			else
				return s;
		}).collect(Collectors.toList());

		return newListReadSetCodes;
	}*/

	private List<String> updateListReadSetCodes(List<String> initListReadSetCodes, String oldRSCode, String newRSCode)
	{
		List<String> newListReadSetCodes = new ArrayList<String>();
		for(String s : initListReadSetCodes){
			if(s.equals(oldRSCode))
				newListReadSetCodes.add(newRSCode);
			else
				newListReadSetCodes.add(s);
		}
		return newListReadSetCodes;
	}


	@SuppressWarnings("unchecked")
	private void updateReadSet(String sampleCode, String newSampleCode, String newProjectCode, ContextValidation cv)
	{
		Logger.debug("Update ReadSet"+new Date());
		List<ReadSet> readSets = MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.is("sampleCode", sampleCode)).toList();
		for(ReadSet rs : readSets){
			String newReadSetCode = rs.code.replace(sampleCode, newSampleCode);
			String newPath = rs.path.replace("projet_"+rs.projectCode+"/"+rs.sampleCode.replace(rs.projectCode+"_", "")+"/","projet_"+newProjectCode+"/"+newSampleCode.replace(newProjectCode+"_", "")+"/");
			Logger.debug(sampleCode+","+InstanceConstants.READSET_ILLUMINA_COLL_NAME+",code,"+rs.code);
			((List<String>)cv.getObject("RecapReadSet")).add(sampleCode+","+InstanceConstants.READSET_ILLUMINA_COLL_NAME+",code,"+rs.code+","+rs.code+","+newReadSetCode);
			((List<String>)cv.getObject("RecapReadSet")).add(sampleCode+","+InstanceConstants.READSET_ILLUMINA_COLL_NAME+",sampleCode,"+rs.code+","+rs.sampleCode+","+newSampleCode);
			((List<String>)cv.getObject("RecapReadSet")).add(sampleCode+","+InstanceConstants.READSET_ILLUMINA_COLL_NAME+",projectCode,"+rs.code+","+rs.projectCode+","+newProjectCode);
			((List<String>)cv.getObject("RecapReadSet")).add(sampleCode+","+InstanceConstants.READSET_ILLUMINA_COLL_NAME+",path,"+rs.code+","+rs.path+","+newPath);
			for(File file : rs.files){
				String newfullName = file.fullname.replace(sampleCode, newSampleCode);
				((List<String>)cv.getObject("RecapReadSet")).add(sampleCode+","+InstanceConstants.READSET_ILLUMINA_COLL_NAME+",files.fullname,"+rs.code+","+file.fullname+","+newfullName);
				file.fullname=newfullName;
			}

			MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class,
					DBQuery.is("code", rs.code),
					DBUpdate.set("code", newReadSetCode).set("sampleCode", newSampleCode).set("projectCode", newProjectCode).set("path", newPath).set("files", rs.files));
			List<Analysis> analysisRS =  MongoDBDAO.find(InstanceConstants.ANALYSIS_COLL_NAME, Analysis.class, DBQuery.regex("readSetCodes", Pattern.compile(rs.code))).toList();
			for(Analysis ars : analysisRS){
				List<String> newReadSetCodes = updateListReadSetCodes(ars.readSetCodes, rs.code, newReadSetCode);
				((List<String>)cv.getObject("RecapAnalysis")).add(sampleCode+","+InstanceConstants.ANALYSIS_COLL_NAME+",readSetCodes,"+ars.code+","+ars.readSetCodes.toString().replaceAll(",", "#")+","+newReadSetCodes.toString().replaceAll(",", "#"));
				ars.readSetCodes=newReadSetCodes;
				MongoDBDAO.update(InstanceConstants.ANALYSIS_COLL_NAME, Analysis.class,
						DBQuery.is("code", ars.code),
						DBUpdate.set("readSetCodes", newReadSetCodes));
			}
			Logger.debug("Update Run lane readSetCodes"+new Date());
			List<Run> laneRuns = MongoDBDAO.find(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, DBQuery.regex("lanes.readSetCodes", Pattern.compile(rs.code))).toList();
			for(Run lr : laneRuns){
				Logger.debug(sampleCode+","+InstanceConstants.RUN_ILLUMINA_COLL_NAME+",lanes.readSetCodes,"+lr.code);
				lr.lanes.forEach(l->{
					if(containRS(l.readSetCodes, rs.code)){
						Logger.debug("Init readSetCodes "+l.readSetCodes);
						List<String> newListReadSetCodes=updateListReadSetCodes(l.readSetCodes, rs.code, newReadSetCode);
						((List<String>)cv.getObject("RecapRun")).add(sampleCode+","+InstanceConstants.RUN_ILLUMINA_COLL_NAME+",lanes.readSetCodes,"+lr.code+","+l.readSetCodes.toString().replaceAll(",", "#")+","+newListReadSetCodes.toString().replaceAll(",", "#")+","+l.number);
						l.readSetCodes=newListReadSetCodes;
					}
				});

				MongoDBDAO.update(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class,
						DBQuery.is("code", lr.code),
						DBUpdate.set("lanes", lr.lanes));
			}


		}
		Logger.debug("Update ReadSet soc sampleCode"+new Date());
		List<ReadSet> readSetsSCO = MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.is("sampleOnContainer.sampleCode", sampleCode)).toList();
		for(ReadSet rsSOC : readSetsSCO){
			Logger.debug(sampleCode+","+InstanceConstants.READSET_ILLUMINA_COLL_NAME+",sampleOnContainer.sampleCode,"+rsSOC.code);
			((List<String>)cv.getObject("RecapReadSet")).add(sampleCode+","+InstanceConstants.READSET_ILLUMINA_COLL_NAME+",sampleOnContainer.sampleCode,"+rsSOC.code+","+rsSOC.sampleOnContainer.sampleCode+","+newSampleCode);
			((List<String>)cv.getObject("RecapReadSet")).add(sampleCode+","+InstanceConstants.READSET_ILLUMINA_COLL_NAME+",sampleOnContainer.projectCode,"+rsSOC.code+","+rsSOC.sampleOnContainer.projectCode+","+newProjectCode);

			MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class,
					DBQuery.is("code", rsSOC.code),
					DBUpdate.set("sampleOnContainer.sampleCode", newSampleCode).set("sampleOnContainer.projectCode", newProjectCode));
		}
		Logger.debug("Update ReadSet soc fromSampleCode"+new Date());
		List<ReadSet> readSetFromSample = MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.is("sampleOnContainer.properties.fromSampleCode.value", sampleCode)).toList();
		for(ReadSet rsFS : readSetFromSample){
			PropertyValue propertyValue = rsFS.sampleOnContainer.properties.get("fromSampleCode");
			PropertyValue propertyProjectValue = rsFS.sampleOnContainer.properties.get("fromProjectCode");
			Logger.debug(sampleCode+","+InstanceConstants.READSET_ILLUMINA_COLL_NAME+",sampleOnContainer.properties.fromSampleCode.value,"+rsFS.code);
			((List<String>)cv.getObject("RecapReadSet")).add(sampleCode+","+InstanceConstants.READSET_ILLUMINA_COLL_NAME+",sampleOnContainer.properties.fromSampleCode.value,"+rsFS.code+","+propertyValue.getValue()+","+newSampleCode);
			((List<String>)cv.getObject("RecapReadSet")).add(sampleCode+","+InstanceConstants.READSET_ILLUMINA_COLL_NAME+",sampleOnContainer.properties.fromProjectCode.value,"+rsFS.code+","+propertyProjectValue.getValue()+","+newProjectCode);

			MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class,
					DBQuery.is("code", rsFS.code),
					DBUpdate.set("sampleOnContainer.properties.fromSampleCode.value", newSampleCode).set("sampleOnContainer.properties.fromProjectCode.value", newProjectCode));
		}

		//Add tag
		Logger.debug("Update ReadSet soc tagPcrBlank1SampleCode"+new Date());
		List<ReadSet> readSetTagPCR1 = MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.is("sampleOnContainer.properties.tagPcrBlank1SampleCode.value", sampleCode)).toList();
		for(ReadSet rsTagPCR1:readSetTagPCR1){
			PropertyValue propertyValue = rsTagPCR1.sampleOnContainer.properties.get("tagPcrBlank1SampleCode");
			Logger.debug(sampleCode+","+InstanceConstants.READSET_ILLUMINA_COLL_NAME+",sampleOnContainer.properties.tagPcrBlank1SampleCode.value,"+rsTagPCR1.code);
			((List<String>)cv.getObject("RecapReadSet")).add(sampleCode+","+InstanceConstants.READSET_ILLUMINA_COLL_NAME+",sampleOnContainer.properties.tagPcrBlank1SampleCode.value,"+rsTagPCR1.code+","+propertyValue.getValue()+","+newSampleCode);
			MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class,
					DBQuery.is("code", rsTagPCR1.code),
					DBUpdate.set("sampleOnContainer.properties.tagPcrBlank1SampleCode.value", newSampleCode));
		}
		Logger.debug("Update ReadSet soc tagPcrBlank2SampleCode"+new Date());
		List<ReadSet> readSetTagPCR2 = MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.is("sampleOnContainer.properties.tagPcrBlank2SampleCode.value", sampleCode)).toList();
		for(ReadSet rsTagPCR2:readSetTagPCR2){
			PropertyValue propertyValue = rsTagPCR2.sampleOnContainer.properties.get("tagPcrBlank2SampleCode");
			Logger.debug(sampleCode+","+InstanceConstants.READSET_ILLUMINA_COLL_NAME+",sampleOnContainer.properties.tagPcrBlank2SampleCode.value,"+rsTagPCR2.code);
			((List<String>)cv.getObject("RecapReadSet")).add(sampleCode+","+InstanceConstants.READSET_ILLUMINA_COLL_NAME+",sampleOnContainer.properties.tagPcrBlank2SampleCode.value,"+rsTagPCR2.code+","+propertyValue.getValue()+","+newSampleCode);
			MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class,
					DBQuery.is("code", rsTagPCR2.code),
					DBUpdate.set("sampleOnContainer.properties.tagPcrBlank2SampleCode.value", newSampleCode));
		}
		Logger.debug("Update ReadSet soc extractionBlankSampleCode"+new Date());
		List<ReadSet> readSetBlank = MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.is("sampleOnContainer.properties.extractionBlankSampleCode.value", sampleCode)).toList();
		for(ReadSet rsBlank:readSetBlank){
			PropertyValue propertyValue = rsBlank.sampleOnContainer.properties.get("extractionBlankSampleCode");
			Logger.debug(sampleCode+","+InstanceConstants.READSET_ILLUMINA_COLL_NAME+",sampleOnContainer.properties.extractionBlankSampleCode.value,"+rsBlank.code);
			((List<String>)cv.getObject("RecapReadSet")).add(sampleCode+","+InstanceConstants.READSET_ILLUMINA_COLL_NAME+",sampleOnContainer.properties.extractionBlankSampleCode.value,"+rsBlank.code+","+propertyValue.getValue()+","+newSampleCode);
			MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class,
					DBQuery.is("code", rsBlank.code),
					DBUpdate.set("sampleOnContainer.properties.extractionBlankSampleCode.value", newSampleCode));
		}
		Logger.debug("Update ReadSet End"+new Date());
	}


	private Set<String> getNewProjectCodes(Set<String> sampleCodes, Map<String, Set<String>> mapSampleProjects)
	{
		Set<String> newProjectCodes = new HashSet<String>();
		for(String sampleCode : sampleCodes){
			if(mapSampleProjects.containsKey(sampleCode))
				newProjectCodes.addAll(mapSampleProjects.get(sampleCode));
			else{
				Sample sample = MongoDBDAO.findByCode(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, sampleCode);
				newProjectCodes.addAll(sample.projectCodes);
				mapSampleProjects.put(sampleCode, sample.projectCodes);
			}
		}
		return newProjectCodes;
	}

	@SuppressWarnings("unchecked")
	private void updateListeProjectCodes(List<String> newSampleCodes, ContextValidation cv)
	{
		Map<String, Set<String>> mapSampleProjects = new HashMap<String, Set<String>>();
		for(String newSampleCode : newSampleCodes){
			Logger.debug("Update list projectCodes for "+newSampleCode);
			//Collection container propriete : projectCodes 
			List<Container> containers = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class, DBQuery.in("sampleCodes", newSampleCode)).toList();
			for(Container container : containers){
				Set<String> newProjectCodes = getNewProjectCodes(container.sampleCodes, mapSampleProjects);
				Logger.debug(newSampleCode+","+InstanceConstants.CONTAINER_COLL_NAME+",projectCodes,"+container.code+","+container.projectCodes.toString().replaceAll(",", "#")+","+newProjectCodes.toString().replaceAll(",", "#"));
				((List<String>)cv.getObject("RecapListProjects")).add(newSampleCode+","+InstanceConstants.CONTAINER_COLL_NAME+",projectCodes,"+container.code+","+container.projectCodes.toString().replaceAll(",", "#")+","+newProjectCodes.toString().replaceAll(",", "#"));
				MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME, Container.class,
						DBQuery.is("code", container.code),
						DBUpdate.set("projectCodes", newProjectCodes));
			}

			//Collection containerSupport propriete : projectCodes
			List<ContainerSupport> containerSupports = MongoDBDAO.find(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, ContainerSupport.class, DBQuery.in("sampleCodes", newSampleCode)).toList();
			for(ContainerSupport cs : containerSupports){
				Set<String> newProjectCodes = getNewProjectCodes(cs.sampleCodes, mapSampleProjects);
				Logger.debug(newSampleCode+","+InstanceConstants.CONTAINER_SUPPORT_COLL_NAME+",projectCodes,"+cs.code+","+cs.projectCodes.toString().replaceAll(",", "#")+","+newProjectCodes.toString().replaceAll(",", "#"));
				((List<String>)cv.getObject("RecapListProjects")).add(newSampleCode+","+InstanceConstants.CONTAINER_SUPPORT_COLL_NAME+",projectCodes,"+cs.code+","+cs.projectCodes.toString().replaceAll(",", "#")+","+newProjectCodes.toString().replaceAll(",", "#"));
				MongoDBDAO.update(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, ContainerSupport.class,
						DBQuery.is("code", cs.code),
						DBUpdate.set("projectCodes", newProjectCodes));
			}

			//Collection experiment propriete : projectCodes
			List<Experiment> experiments = MongoDBDAO.find(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, DBQuery.in("sampleCodes", newSampleCode)).toList();
			for(Experiment exp : experiments){
				Set<String> newProjectCodes = getNewProjectCodes(exp.sampleCodes, mapSampleProjects);
				Logger.debug(newSampleCode+","+InstanceConstants.EXPERIMENT_COLL_NAME+",projectCodes,"+exp.code+","+exp.projectCodes.toString().replaceAll(",", "#")+","+newProjectCodes.toString().replaceAll(",", "#"));
				((List<String>)cv.getObject("RecapListProjects")).add(newSampleCode+","+InstanceConstants.EXPERIMENT_COLL_NAME+",projectCodes,"+exp.code+","+exp.projectCodes.toString().replaceAll(",", "#")+","+newProjectCodes.toString().replaceAll(",", "#"));
				MongoDBDAO.update(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class,
						DBQuery.is("code", exp.code),
						DBUpdate.set("projectCodes", newProjectCodes));
			}
			List<Experiment> experimentsATM = MongoDBDAO.find(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, DBQuery.in("atomicTransfertMethods.inputContainerUseds.sampleCodes", newSampleCode)).toList();
			for(Experiment expATM : experimentsATM){
				expATM.atomicTransfertMethods.forEach(atm->{
					atm.inputContainerUseds.forEach(icu->{
						if(icu.sampleCodes.contains(newSampleCode)){
							Set<String> newProjectCodes = getNewProjectCodes(icu.sampleCodes, mapSampleProjects);
							Logger.debug(newSampleCode+","+InstanceConstants.EXPERIMENT_COLL_NAME+",atomicTransfertMethods.inputContainerUseds.sampleCodes,"+expATM.code+","+icu.projectCodes.toString().replaceAll(",", "#")+","+newProjectCodes.toString().replaceAll(",", "#"));
							((List<String>)cv.getObject("RecapListProjects")).add(newSampleCode+","+InstanceConstants.EXPERIMENT_COLL_NAME+",atomicTransfertMethods.inputContainerUseds.sampleCodes,"+expATM.code+","+icu.projectCodes.toString().replaceAll(",", "#")+","+newProjectCodes.toString().replaceAll(",", "#"));
							icu.projectCodes=newProjectCodes;
						}
					});
				});
				MongoDBDAO.update(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class,
						DBQuery.is("code", expATM.code),
						DBUpdate.set("atomicTransfertMethods", expATM.atomicTransfertMethods));
			}
			//Collection process propriete : projectCodes
			List<Process> processes = MongoDBDAO.find(InstanceConstants.PROCESS_COLL_NAME, Process.class, DBQuery.in("sampleCodes", newSampleCode)).toList();
			for(Process p : processes){
				Set<String> newProjectCodes = getNewProjectCodes(p.sampleCodes, mapSampleProjects);
				Logger.debug(newSampleCode+","+InstanceConstants.PROCESS_COLL_NAME+",projectCodes,"+p.code+","+p.projectCodes.toString().replaceAll(",", "#")+","+newProjectCodes.toString().replaceAll(",", "#"));
				((List<String>)cv.getObject("RecapListProjects")).add(newSampleCode+","+InstanceConstants.PROCESS_COLL_NAME+",projectCodes,"+p.code+","+p.projectCodes.toString().replaceAll(",", "#")+","+newProjectCodes.toString().replaceAll(",", "#"));
				MongoDBDAO.update(InstanceConstants.PROCESS_COLL_NAME, Process.class,
						DBQuery.is("code", p.code),
						DBUpdate.set("projectCodes", newProjectCodes));
			}

			//Collection Run propriete : projectCodes
			List<Run> runs = MongoDBDAO.find(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, DBQuery.in("sampleCodes", newSampleCode)).toList();
			for(Run run : runs){
				Set<String> newProjectCodes = getNewProjectCodes(run.sampleCodes, mapSampleProjects);
				Logger.debug(newSampleCode+","+InstanceConstants.RUN_ILLUMINA_COLL_NAME+",projectCodes,"+run.code+","+run.projectCodes.toString().replaceAll(",", "#")+","+newProjectCodes.toString().replaceAll(",", "#"));
				((List<String>)cv.getObject("RecapListProjects")).add(newSampleCode+","+InstanceConstants.RUN_ILLUMINA_COLL_NAME+",projectCodes,"+run.code+","+run.projectCodes.toString().replaceAll(",", "#")+","+newProjectCodes.toString().replaceAll(",", "#"));
				MongoDBDAO.update(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class,
						DBQuery.is("code", run.code),
						DBUpdate.set("projectCodes", newProjectCodes));
			}
		}



	}

	/**
	 * Create excel file in execution directory
	 * File recap : list all objects updated
	 * @param cv
	 */
	@SuppressWarnings("unchecked")
	private void createExcelFileRecap(ContextValidation cv)
	{
		Workbook wb = new HSSFWorkbook();
		CreationHelper createHelper = wb.getCreationHelper();
		for(String key : cv.getContextObjects().keySet()){
			Sheet sheet = wb.createSheet(key);
			List<String> recaps = (List<String>) cv.getObject(key);
			int nbLine=0;
			for(String recap : recaps){
				//Logger.debug(recap);
				Row row = sheet.createRow(nbLine);
				String[] tabRecap = recap.split(",");
				for(int i=0;i<tabRecap.length;i++){
					row.createCell(i).setCellValue(
							createHelper.createRichTextString(tabRecap[i]));
				}
				nbLine++;
			}
		}

		// Write the output to a file
		try (OutputStream fileOut = new FileOutputStream("Test.xls")) {
			wb.write(fileOut);
		}catch(Exception e){
			Logger.debug(e.getMessage());
		}
	}

	private Set<String> updateSampleCodes(Set<String> initialSampleCodes, String oldSampleCode, String newSampleCode)
	{
		Set<String> newSampleCodes = new HashSet<String>();
		newSampleCodes.addAll(initialSampleCodes);
		newSampleCodes.remove(oldSampleCode);
		newSampleCodes.add(newSampleCode);
		return newSampleCodes;
	}

	private Set<String> updateProjecCodes(Set<String> initialProjectCodes, String newProjectCode)
	{
		Set<String> newProjectCodes = new HashSet<String>();
		newProjectCodes.addAll(initialProjectCodes);
		if(!newProjectCodes.contains(newProjectCode))
			newProjectCodes.add(newProjectCode);
		return newProjectCodes;
	}



}
