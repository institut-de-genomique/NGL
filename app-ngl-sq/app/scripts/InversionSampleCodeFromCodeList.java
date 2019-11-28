package scripts;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptWithExcelBody;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.ContainerSupport;
import models.laboratory.container.instance.Content;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.processes.instance.Process;
import models.laboratory.run.instance.Analysis;
import models.laboratory.run.instance.File;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.run.instance.Run;
import models.laboratory.run.instance.SampleOnContainer;
import models.laboratory.sample.instance.Sample;
import models.utils.InstanceConstants;
import play.Logger;
import validation.ContextValidation;

/**
 * Adaptation script inversion filtre pour ticket erreur BSI (NGL-2507)
 * Echange codeSample/codeProjet dans tous les champs liés à un sampleCode ou projectCode : sampleCode, projectCode, fromSampleCode, fromProjectCode, life.path, readSetCode, pathReadSet, filesName ReadSet
 * Cas spécifique support pas de recherche sur les champs : taPcrBlank1SampleCode, tagPcrBlank2SampleCode, extractionBlankSampleCode (à rajouter si le cas se présente)
 * Recherche de ces champs dans les codes indiqués dans le fichier pour chaque collections : sample, container, containerSupport, experiment, process, analysis, readSet, run 
 * NB : Ce script fait aussi l'inversion des propriétés sample
 * La descendance est donné dans le fichier pas de recherche de descendance
 * Entrée fichier excel : un onglet par collection nomenclature = Container / Experiment / Process / Run / ReadSet
 * TODO Sample Analysis 
 * Entête fichier mapping : objectCode sampleCode newSampleCode   
 * @author ejacoby
 *
 */

public class InversionSampleCodeFromCodeList extends ScriptWithExcelBody{

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
		//Add Header Recap
		((List<String>)cv.getObject("RecapSample")).add("Code sample,collection,propriete collection MAJ,code collection,valeur initiale,valeur finale");
		((List<String>)cv.getObject("RecapContainer")).add("Code sample,collection,propriete collection MAJ,code collection,valeur initiale,valeur finale");
		((List<String>)cv.getObject("RecapContainerSupport")).add("Code sample,collection,propriete collection MAJ,code collection,valeur initiale,valeur finale");
		((List<String>)cv.getObject("RecapExperiment")).add("Code sample,collection,propriete collection MAJ,code collection,valeur initiale,valeur finale");
		((List<String>)cv.getObject("RecapProcess")).add("Code sample,collection,propriete collection MAJ,code collection,valeur initiale,valeur finale");
		((List<String>)cv.getObject("RecapRun")).add("Code sample,collection,propriete collection MAJ,code collection,valeur initiale,valeur finale");
		((List<String>)cv.getObject("RecapReadSet")).add("Code sample,collection,propriete collection MAJ,code collection,valeur initiale,valeur finale");
		((List<String>)cv.getObject("RecapAnalysis")).add("Code sample,collection,propriete collection MAJ,code collection,valeur initiale,valeur finale");

		for(int i=0; i<workbook.getNumberOfSheets();i++){
			XSSFSheet sheet = workbook.getSheetAt(i);
			sheet.rowIterator().forEachRemaining(row -> {
				if(row.getRowNum() == 0) return; // skip header
				if(row!=null && row.getCell(0)!=null && row.getCell(1)!=null){

					String code = row.getCell(0).getStringCellValue();
					String oldSampleCode = row.getCell(1).getStringCellValue();
					String newSampleCode = row.getCell(2).getStringCellValue();
					if(!code.equals("")){
						Logger.debug("code "+code+"replace "+oldSampleCode+" by "+newSampleCode);
						//TODO update sample
						//updateSample(sampleCode, projectCode, newSampleCode, newProjectCode, cv);

						if(sheet.getSheetName().equals("Container")){
							//update container
							updateContainer(code, oldSampleCode, newSampleCode, cv);
						}else if(sheet.getSheetName().equals("Experiment")){
							//update experiment
							updateExperiment(code, oldSampleCode, newSampleCode, cv);
						}else if(sheet.getSheetName().equals("Process")){
							// update process
							updateProcess(code, oldSampleCode, newSampleCode, cv);
						}else if(sheet.getSheetName().equals("Run")){
							//update run
							updateRun(code, oldSampleCode, newSampleCode, cv);
						}else if(sheet.getSheetName().equals("ReadSet")){
							//Search update readSet
							updateReadSet(code, oldSampleCode, newSampleCode, cv);
						}
						//TODO Search update analysis
						//updateAnalysis(sampleCode, newSampleCode, newProjectCode,cv);
					}

				}
			});
		}
		//TODO updateListeProjectCodes(listNewSampleCodes, cv);
		//Get error
		if(cv.hasErrors()){
			Logger.debug(cv.getErrors().toString());
		}
		//Create excel file to recap in execution directory
		createExcelFileRecap(cv);
		Logger.debug("End InversionSampleCode");
	}

	//TODO
	/*private void updateSample(String sampleCode, String projectCode, String newSampleCode, String newProjectCode, ContextValidation cv)
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
	}*/

	@SuppressWarnings("unchecked")
	private void updateContainer(String codeContainer, String sampleCode, String newSampleCode,  ContextValidation cv)
	{
		Logger.debug("Update Container"+new Date()+" "+codeContainer);
		//Get old and new Sample
		Sample oldSample = MongoDBDAO.findByCode(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, sampleCode);
		Sample newSample = MongoDBDAO.findByCode(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, newSampleCode);
		Container container = MongoDBDAO.findByCode(InstanceConstants.CONTAINER_COLL_NAME,Container.class, codeContainer);
		Set<String> newSampleCodes = updateSampleCodes(container.sampleCodes, sampleCode, newSampleCode);
		Logger.debug(sampleCode+","+InstanceConstants.CONTAINER_COLL_NAME+",sampleCodes,"+container.code);
		((List<String>)cv.getObject("RecapContainer")).add(sampleCode+","+InstanceConstants.CONTAINER_COLL_NAME+",sampleCodes,"+container.code+","+container.sampleCodes.toString().replaceAll(",", "#")+","+newSampleCodes.toString().replaceAll(",", "#"));

		for(Content content : container.contents){
			if(content.sampleCode.equals(sampleCode)){
				Logger.debug(sampleCode+","+InstanceConstants.CONTAINER_COLL_NAME+",contents.sampleCode,"+container.code);
				((List<String>)cv.getObject("RecapContainer")).add(sampleCode+","+InstanceConstants.CONTAINER_COLL_NAME+",contents.sampleCode,"+container.code+","+content.sampleCode+","+newSampleCode);
				content.sampleCode=newSampleCode;
				replaceSamplePropertiesToContent(newSample, oldSample, content);
			}
		}
		MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME, Container.class,
				DBQuery.is("code", container.code),
				DBUpdate.set("sampleCodes", newSampleCodes).set("contents", container.contents));

		updateContainerSupport(container.support.code, sampleCode, newSampleCode, cv);


	}

	@SuppressWarnings("unchecked")
	private void updateContainerSupport(String codeContainerSupport, String sampleCode, String newSampleCode, ContextValidation cv)
	{
		Logger.debug("Update Container Support"+new Date());
		ContainerSupport cs = MongoDBDAO.findByCode(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, ContainerSupport.class, codeContainerSupport);
		Set<String> newSampleCodes = updateSampleCodes(cs.sampleCodes, sampleCode, newSampleCode);
		Logger.debug(sampleCode+","+InstanceConstants.CONTAINER_SUPPORT_COLL_NAME+",sampleCodes,"+cs.code);
		((List<String>)cv.getObject("RecapContainerSupport")).add(sampleCode+","+InstanceConstants.CONTAINER_SUPPORT_COLL_NAME+",sampleCodes,"+cs.code+","+cs.sampleCodes.toString().replaceAll(",", "#")+","+newSampleCodes.toString().replaceAll(",", "#"));

		MongoDBDAO.update(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, ContainerSupport.class,
				DBQuery.is("code", cs.code),
				DBUpdate.set("sampleCodes", newSampleCodes));
	}

	@SuppressWarnings("unchecked")
	private void updateExperiment(String codeExperiment, String sampleCode, String newSampleCode, ContextValidation cv)
	{
		Logger.debug("Update Experiment"+new Date());
		//Get old and new Sample
		Sample oldSample = MongoDBDAO.findByCode(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, sampleCode);
		Sample newSample = MongoDBDAO.findByCode(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, newSampleCode);

		Experiment experiment = MongoDBDAO.findByCode(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, codeExperiment);
		Set<String> newSampleCodes = updateSampleCodes(experiment.sampleCodes, sampleCode, newSampleCode);
		Logger.debug(sampleCode+","+InstanceConstants.EXPERIMENT_COLL_NAME+",sampleCodes,"+experiment.code);
		((List<String>)cv.getObject("RecapExperiment")).add(sampleCode+","+InstanceConstants.EXPERIMENT_COLL_NAME+",sampleCodes,"+experiment.code+","+experiment.sampleCodes.toString().replaceAll(",", "#")+","+newSampleCodes.toString().replaceAll(",", "#"));

		experiment.atomicTransfertMethods.forEach(atm->{
			atm.inputContainerUseds.forEach(icu->{
				if(icu.sampleCodes.contains(sampleCode)){
					Set<String> newSampleCodesicu = updateSampleCodes(icu.sampleCodes, sampleCode, newSampleCode);
					Logger.debug(sampleCode+","+InstanceConstants.EXPERIMENT_COLL_NAME+",atomicTransfertMethods.inputContainerUseds.sampleCodes,"+experiment.code);
					((List<String>)cv.getObject("RecapExperiment")).add(sampleCode+","+InstanceConstants.EXPERIMENT_COLL_NAME+",atomicTransfertMethods.inputContainerUseds.sampleCodes,"+experiment.code+","+icu.sampleCodes.toString().replaceAll(",", "#")+","+newSampleCodesicu.toString().replaceAll(",", "#"));
					icu.sampleCodes=newSampleCodesicu;
				}
				icu.contents.stream().forEach(content->{
					if(content.sampleCode.equals(sampleCode)){
						Logger.debug(sampleCode+","+InstanceConstants.EXPERIMENT_COLL_NAME+",atomicTransfertMethods.inputContainerUseds.contents.sampleCode,"+experiment.code);
						((List<String>)cv.getObject("RecapExperiment")).add(sampleCode+","+InstanceConstants.EXPERIMENT_COLL_NAME+",atomicTransfertMethods.inputContainerUseds.contents.sampleCode,"+experiment.code+","+content.sampleCode+","+newSampleCode);
						content.sampleCode=newSampleCode;
						replaceSamplePropertiesToContent(newSample, oldSample, content);
					}
				});
			});
			if(atm.outputContainerUseds!=null){
				atm.outputContainerUseds.forEach(ocu->{
					if(ocu.experimentProperties!=null && ocu.experimentProperties.get("sampleCode")!=null && ocu.experimentProperties.get("sampleCode").getValue().equals(sampleCode)){
						PropertyValue propertyValue = ocu.experimentProperties.get("sampleCode");
						Logger.debug(sampleCode+","+InstanceConstants.EXPERIMENT_COLL_NAME+",atomicTransfertMethods.outputContainerUseds.experimentProperties.sampleCode,"+experiment.code);
						((List<String>)cv.getObject("RecapExperiment")).add(sampleCode+","+InstanceConstants.EXPERIMENT_COLL_NAME+",atomicTransfertMethods.outputContainerUseds.experimentProperties.sampleCode,"+experiment.code+","+propertyValue.getValue()+","+newSampleCode);
						ocu.experimentProperties.get("sampleCode").assignValue(newSampleCode);
					}
					ocu.contents.stream().forEach(content->{
						if(content.sampleCode.equals(sampleCode)){
							Logger.debug(sampleCode+","+InstanceConstants.EXPERIMENT_COLL_NAME+",atomicTransfertMethods.outputContainerUseds.contents.sampleCode,"+experiment.code);
							((List<String>)cv.getObject("RecapExperiment")).add(sampleCode+","+InstanceConstants.EXPERIMENT_COLL_NAME+",atomicTransfertMethods.outputContainerUseds.contents.sampleCode,"+experiment.code+","+content.sampleCode+","+newSampleCode);
							content.sampleCode=newSampleCode;
							replaceSamplePropertiesToContent(newSample, oldSample, content);
						}
					});
				});
			}
		});



		MongoDBDAO.update(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class,
				DBQuery.is("code", experiment.code),
				DBUpdate.set("sampleCodes", newSampleCodes).set("atomicTransfertMethods", experiment.atomicTransfertMethods));

	}


	@SuppressWarnings("unchecked")
	private void updateProcess(String codeProcess, String sampleCode, String newSampleCode, ContextValidation cv)
	{
		Logger.debug("Update Process"+new Date());

		Sample oldSample = MongoDBDAO.findByCode(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, sampleCode);
		Sample newSample = MongoDBDAO.findByCode(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, newSampleCode);

		Process p = MongoDBDAO.findByCode(InstanceConstants.PROCESS_COLL_NAME, Process.class, codeProcess);
		Logger.debug(sampleCode+","+InstanceConstants.PROCESS_COLL_NAME+",sampleCodes,"+p.code);
		Set<String> newSampleCodes = updateSampleCodes(p.sampleCodes, sampleCode, newSampleCode);
		((List<String>)cv.getObject("RecapProcess")).add(sampleCode+","+InstanceConstants.PROCESS_COLL_NAME+",sampleCodes,"+p.code+","+p.sampleCodes.toString().replaceAll(",", "#")+","+newSampleCodes.toString().replaceAll(",", "#"));

		MongoDBDAO.update(InstanceConstants.PROCESS_COLL_NAME, Process.class,
				DBQuery.is("code", p.code),
				DBUpdate.set("sampleCodes", newSampleCodes));

		if(p.sampleOnInputContainer.sampleCode.equals(sampleCode)){
			Logger.debug(sampleCode+","+InstanceConstants.PROCESS_COLL_NAME+",sampleOnInputContainer.sampleCode,"+p.code);
			((List<String>)cv.getObject("RecapProcess")).add(sampleCode+","+InstanceConstants.PROCESS_COLL_NAME+",sampleOnInputContainer.sampleCode,"+p.code+","+p.sampleOnInputContainer.sampleCode+","+newSampleCode);
			p.sampleOnInputContainer.sampleCode=newSampleCode;
			replaceSamplePropertiesToSampleOnContainer(newSample, oldSample, p.sampleOnInputContainer);
			MongoDBDAO.update(InstanceConstants.PROCESS_COLL_NAME, Process.class,
					DBQuery.is("code", p.code),
					DBUpdate.set("sampleOnInputContainer", p.sampleOnInputContainer));
		}

	}

	//TODO
	/*private void updateAnalysis(String sampleCode,  String newSampleCode, String newProjectCode, ContextValidation cv)
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

	}*/

	@SuppressWarnings("unchecked")
	private void updateRun(String codeRun, String sampleCode, String newSampleCode, ContextValidation cv)
	{
		Logger.debug("Update Run"+new Date());
		Run run = MongoDBDAO.findByCode(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, codeRun);
		Set<String> newSampleCodes = updateSampleCodes(run.sampleCodes, sampleCode, newSampleCode);
		Logger.debug(sampleCode+","+InstanceConstants.RUN_ILLUMINA_COLL_NAME+",sampleCodes,"+run.code);
		((List<String>)cv.getObject("RecapRun")).add(sampleCode+","+InstanceConstants.RUN_ILLUMINA_COLL_NAME+",sampleCodes,"+run.code+","+run.sampleCodes.toString().replaceAll(",", "#")+","+newSampleCodes.toString().replaceAll(",", "#"));

		MongoDBDAO.update(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class,
				DBQuery.is("code", run.code),
				DBUpdate.set("sampleCodes", newSampleCodes));


	}

	private boolean containRS(List<String> listRS, String sampleCode)
	{
		for(String rsCode : listRS){
			if(rsCode.contains(sampleCode))
				return true;
		}
		return false;
	}

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
	private void updateReadSet(String codeReadSet, String sampleCode, String newSampleCode, ContextValidation cv)
	{
		Logger.debug("Update ReadSet"+new Date()+" "+codeReadSet);
		ReadSet rs = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, codeReadSet);
		String newReadSetCode = rs.code.replace(sampleCode, newSampleCode);
		String newPath = rs.path.replace("projet_"+rs.projectCode+"/"+rs.sampleCode.replace(rs.projectCode+"_", "")+"/","projet_"+rs.projectCode+"/"+newSampleCode.replace(rs.projectCode+"_", "")+"/");
		Logger.debug(sampleCode+","+InstanceConstants.READSET_ILLUMINA_COLL_NAME+",code,"+rs.code);
		((List<String>)cv.getObject("RecapReadSet")).add(sampleCode+","+InstanceConstants.READSET_ILLUMINA_COLL_NAME+",code,"+rs.code+","+rs.code+","+newReadSetCode);
		((List<String>)cv.getObject("RecapReadSet")).add(sampleCode+","+InstanceConstants.READSET_ILLUMINA_COLL_NAME+",sampleCode,"+rs.code+","+rs.sampleCode+","+newSampleCode);
		((List<String>)cv.getObject("RecapReadSet")).add(sampleCode+","+InstanceConstants.READSET_ILLUMINA_COLL_NAME+",path,"+rs.code+","+rs.path+","+newPath);
		for(File file : rs.files){
			String newfullName = file.fullname.replace(sampleCode, newSampleCode);
			String newLabel = ((String)file.properties.get("label").getValue()).replace(rs.code, newReadSetCode);
			((List<String>)cv.getObject("RecapReadSet")).add(sampleCode+","+InstanceConstants.READSET_ILLUMINA_COLL_NAME+",files.fullname,"+rs.code+","+file.fullname+","+newfullName);
			((List<String>)cv.getObject("RecapReadSet")).add(sampleCode+","+InstanceConstants.READSET_ILLUMINA_COLL_NAME+",files.label,"+rs.code+","+file.properties.get("label").getValue()+","+newLabel);
			file.fullname=newfullName;
			file.properties.put("label", new PropertySingleValue(newLabel));
		}

		MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class,
				DBQuery.is("code", rs.code),
				DBUpdate.set("code", newReadSetCode).set("sampleCode", newSampleCode).set("path", newPath).set("files", rs.files));

		Sample oldSample = MongoDBDAO.findByCode(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, sampleCode);
		Sample newSample = MongoDBDAO.findByCode(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, newSampleCode);

		if(rs.sampleOnContainer.sampleCode.equals(sampleCode)){
			Logger.debug(sampleCode+","+InstanceConstants.READSET_ILLUMINA_COLL_NAME+",sampleOnContainer.sampleCode,"+newReadSetCode);
			((List<String>)cv.getObject("RecapReadSet")).add(sampleCode+","+InstanceConstants.READSET_ILLUMINA_COLL_NAME+",sampleOnContainer.sampleCode,"+rs.code+","+rs.sampleOnContainer.sampleCode+","+newSampleCode);

			rs.sampleOnContainer.sampleCode=newSampleCode;
			replaceSamplePropertiesToSampleOnContainer(newSample, oldSample, rs.sampleOnContainer);
			MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class,
					DBQuery.is("code", newReadSetCode),
					DBUpdate.set("sampleOnContainer", rs.sampleOnContainer));
		}
		
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

		

		Logger.debug("Update ReadSet End"+new Date());
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

	private void replaceSamplePropertiesToContent(Sample sample, Sample oldSample, Content content)
	{
		content.sampleTypeCode=sample.typeCode;
		content.sampleCategoryCode=sample.categoryCode;
		content.referenceCollab=sample.referenceCollab;
		content.taxonCode=sample.taxonCode;
		content.ncbiScientificName=sample.ncbiScientificName;
		//Remplacer ou ajouter uniquement des propriétés de niveau sample
		for(String key : sample.properties.keySet()){
			content.properties.put(key, sample.properties.get(key));
		}
		//Remove properties sample de l'ancien sample
		for(String key : oldSample.properties.keySet()){
			if(!sample.properties.containsKey(key)){
				content.properties.remove(key);
			}
		}
	}

	private void replaceSamplePropertiesToSampleOnContainer(Sample sample, Sample oldSample, SampleOnContainer soic)
	{
		soic.sampleTypeCode=sample.typeCode;
		soic.sampleCategoryCode=sample.categoryCode;
		soic.referenceCollab=sample.referenceCollab;
		soic.taxonCode=sample.taxonCode;
		soic.ncbiScientificName=sample.ncbiScientificName;
		//Remplacer ou ajouter uniquement des propriétés de niveau sample
		for(String key : sample.properties.keySet()){
			soic.properties.put(key, sample.properties.get(key));
		}
		//Remove properties sample de l'ancien sample
		for(String key : oldSample.properties.keySet()){
			if(!sample.properties.containsKey(key)){
				soic.properties.remove(key);
			}
		}

	}



}
