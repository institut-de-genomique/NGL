package scripts;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.mongojack.DBQuery;
import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptWithArgsAndExcelBody;
import models.laboratory.common.instance.Comment;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.Content;
import models.laboratory.experiment.instance.AtomicTransfertMethod;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.experiment.instance.InputContainerUsed;
import models.laboratory.experiment.instance.OutputContainerUsed;
import models.laboratory.processes.instance.Process;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.sample.instance.Sample;
import models.utils.InstanceConstants;
import play.Logger;
import validation.ContextValidation;

/**
 * Script de mise a jour du sampleTypeCode et sampleCategoryCode
 * Recherche de ces champs pour chaque collections : sample, container, experiment, process, readSet 
 * La recherche est faite pour toutes les ressources associées au sampleCode précisé, y compris les descendants 
 * (notion de fromSample dans le ressources)
 * Les nouvelles valeurs sont données dans un fichier excel
 * Entrée fichier excel   sampleCode oldSampleTypeCode newSampleTypeCode oldSampleCategoryCode newSampleCategoryCode
 * Sortie un fichier excel dans le repertoire d'execution, listant l'ensemble des objets modifiés, un onglet par ressource corrigée.
 * 
 * ex de lancement :
 * http://localhost:9000/scripts/run/scripts.ScriptUpdateSampleTypeFromList?jira=SUPSQ-4444   et fichier excel dans body
 * @author gsamson
 *
 */

public class ScriptUpdateSampleTypeFromList extends ScriptWithArgsAndExcelBody <ScriptUpdateSampleTypeFromList.Args> {

	final String user = "ngl-support";


	public static class Args {
		public String jira;
	}


	@SuppressWarnings("unchecked")
	@Override
	public void execute(Args args, XSSFWorkbook workbook) throws Exception {
		//NGL-4111
		if ( ! args.jira.matches("^(SUPSQ|SUPSQCNG|NGL)-\\d+$") ) {
			throw new RuntimeException("argument jira " +  args.jira + " qui n'a pas la forme attendue SUPSQ-XXX ou SUPSQCNG-XXX ou NGL-XXX");
		}
		
		ContextValidation cv = ContextValidation.createUpdateContext(user);
		cv.putObject("RecapSample", new ArrayList<String>());
		cv.putObject("RecapContainer", new ArrayList<String>());
		cv.putObject("RecapExperiment", new ArrayList<String>());
		cv.putObject("RecapProcess", new ArrayList<String>());
		cv.putObject("RecapReadSet", new ArrayList<String>());
		//Add Header Recap
		((List<String>)cv.getObject("RecapSample")).add("Code sample,collection,propriete collection MAJ,valeur initiale,valeur finale,propriete collection MAJ,valeur initiale,valeur finale");
		((List<String>)cv.getObject("RecapContainer")).add("Code container,collection,propriete collection MAJ,valeur initiale,valeur finale,propriete collection MAJ,valeur initiale,valeur finale");
		((List<String>)cv.getObject("RecapExperiment")).add("Code experiment,collection,propriete collection MAJ,valeur initiale,valeur finale,propriete collection MAJ,valeur initiale,valeur finale");
		((List<String>)cv.getObject("RecapProcess")).add("Code process,collection,propriete collection MAJ,valeur initiale,valeur finale,propriete collection MAJ,valeur initiale,valeur finale");
		((List<String>)cv.getObject("RecapReadSet")).add("Code readset,collection,propriete collection MAJ,valeur initiale,valeur finale,propriete collection MAJ,valeur initiale,valeur finale");

		for(int i=0; i<workbook.getNumberOfSheets();i++){
			XSSFSheet sheet = workbook.getSheetAt(i);
			sheet.rowIterator().forEachRemaining(row -> {
				if(row.getRowNum() == 0) return; // skip header
				if(row!=null && row.getCell(0)!=null && row.getCell(1)!=null){

					String sampleCode = row.getCell(0).getStringCellValue();
					String oldSampleTypeCode = row.getCell(1).getStringCellValue();
					String newSampleTypeCode = row.getCell(2).getStringCellValue();
					String oldSampleCategoryCode = row.getCell(3).getStringCellValue();
					String newSampleCategoryCode = row.getCell(4).getStringCellValue();
					//on ne gère pas les modif d'importType car c'est lui qui gère les propriétés de Sample!!!
					//	String oldImportTypeCode= row.getCell(5).getStringCellValue();
					//	String newImportTypeCode= row.getCell(6).getStringCellValue();
					Logger.debug("sampleCode "+sampleCode+" replace sampleTypeCode "+oldSampleTypeCode+" by "+newSampleTypeCode+" and sampleCategoryCode "+oldSampleCategoryCode
							+" by "+newSampleCategoryCode);

					if(!sampleCode.equals("")){
						//update Sample
						updateSample(args.jira, sampleCode, oldSampleTypeCode, newSampleTypeCode,oldSampleCategoryCode,newSampleCategoryCode, cv);
						//update containers
						updateContainer(sampleCode, oldSampleTypeCode,newSampleTypeCode,oldSampleCategoryCode,newSampleCategoryCode, cv);
						// update experiment
						updateExperiment(sampleCode, oldSampleTypeCode,newSampleTypeCode,oldSampleCategoryCode,newSampleCategoryCode, cv);
						// update process
						updateProcess(sampleCode, oldSampleTypeCode,newSampleTypeCode,oldSampleCategoryCode,newSampleCategoryCode, cv);
						//Search update readSet
						updateReadSet(sampleCode, oldSampleTypeCode,newSampleTypeCode,oldSampleCategoryCode,newSampleCategoryCode, cv);
					}
				}
			});
		}
		//Get error
		if(cv.hasErrors()){
			Logger.debug(cv.getErrors().toString());
		}
		//Create excel file to recap in execution directory
		createExcelFileRecap(cv);
		Logger.info("End Update SampleType and SampleTypeCatgory "+new Date());
		println("End of update ");
	}
	@SuppressWarnings("unchecked")
	private void updateSample(String jira, String sampleCode, String oldSampleTypeCode, String newSampleTypeCode, String oldSampleCategoryCode, String newSampleCategoryCode, ContextValidation cv)
	{
		Logger.debug("Update Sample "+new Date());
		Sample sample = MongoDBDAO.findByCode(InstanceConstants.SAMPLE_COLL_NAME, Sample.class,sampleCode);
		//NGL-4111
		String stComment = jira;
		if (StringUtils.isNotBlank(oldSampleTypeCode)) {
			stComment = jira + " old sampleTypeCode : "  + oldSampleTypeCode;
		}
		if(sample!=null){
			Logger.debug(sampleCode+","+InstanceConstants.SAMPLE_COLL_NAME+",code,"+sample.code+" "+new Date());
			((List<String>)cv.getObject("RecapSample")).add(sampleCode+","+InstanceConstants.SAMPLE_COLL_NAME+",sampleTypeCode,"+oldSampleTypeCode+","+newSampleTypeCode+",sampleCategoryCode,"+oldSampleCategoryCode+","+newSampleCategoryCode+",traceInfo");
			
//			MongoDBDAO.update(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, 
//					DBQuery.is("code", sampleCode),  
//					DBUpdate.set("typeCode",newSampleTypeCode).set("categoryCode", newSampleCategoryCode)
//					.set("traceInformation.modifyUser",user).set("traceInformation.modifyDate",new Date()));
			sample.traceInformation.modifyUser = user;
			sample.traceInformation.modifyDate = new Date();
			sample.typeCode = newSampleTypeCode;
			sample.categoryCode = newSampleCategoryCode;
			if (sample.technicalComments == null) {
				sample.technicalComments =  new ArrayList<Comment>();
			} 
			sample.technicalComments.add(new Comment(stComment, user, true));
			MongoDBDAO.update(InstanceConstants.SAMPLE_COLL_NAME, sample);
		}

		Logger.debug("Update Sample life "+new Date());
		//Recupération des enfants du Sample
		List<Sample> samples = MongoDBDAO.find(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, DBQuery.is("life.from.sampleCode", sampleCode)).toList();
		for(Sample child : samples){
			Logger.debug(child.code+","+InstanceConstants.SAMPLE_COLL_NAME+",life.from.sampleCode,"+child.life.from.sampleCode);
			((List<String>)cv.getObject("RecapSample")).add(child.code+","+InstanceConstants.SAMPLE_COLL_NAME+",life.from.sampleTypeCode,"+oldSampleTypeCode+","+newSampleTypeCode);
			child.life.from.sampleTypeCode = newSampleTypeCode;
			MongoDBDAO.update(InstanceConstants.SAMPLE_COLL_NAME, child);				
		}
		//On ne corrige pas processes car il sera mis a jour avec la cascade nocturne
		Logger.debug("Fin update Sample");
	}

	@SuppressWarnings("unchecked")
	private void updateContainer(String sampleCode,String oldSampleTypeCode, String newSampleTypeCode , String oldSampleCategoryCode, String newSampleCategoryCode,  ContextValidation cv)
	{
		Logger.debug("Update Container "+new Date());
		//Get containers from sample
		List<Container> containers = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class, DBQuery.all("sampleCodes", sampleCode)).toList();

		for(Container container : containers){
			Logger.debug("Update Container "+container.code+" "+new Date());
			container.traceInformation.modifyDate= new Date();
			container.traceInformation.modifyUser= user;
			for(Content content : container.contents){
				if(content.sampleCode.equals(sampleCode)){
					//Logger.debug("Update content "+content.sampleCode+" of container "+container.code+" "+new Date());
					((List<String>)cv.getObject("RecapContainer")).add("contents of "+container.code+","+InstanceConstants.CONTAINER_COLL_NAME+",sampleTypeCode,"+oldSampleTypeCode+","+newSampleTypeCode+",sampleCategoryCode,"+oldSampleCategoryCode+","+newSampleCategoryCode+",traceInfo");
					content.sampleTypeCode=newSampleTypeCode;
					content.sampleCategoryCode=newSampleCategoryCode;			 
				}
			}
			MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME, container);
		}
		//update des containers correspondants aux sample enfants
		containers = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class, DBQuery.all("contents.properties.fromSampleCode.value", sampleCode)).toList();
		for(Container child : containers){
			for(Content content : child.contents){
				if(content.properties.containsKey("fromSampleCode") && content.properties.get("fromSampleCode").value.equals(sampleCode)){
					Logger.debug("Update Container "+child.code+" "+new Date());
					
					((List<String>)cv.getObject("RecapContainer")).add("container enfant "+child.code+","+InstanceConstants.CONTAINER_COLL_NAME+",contents.properties.fromSampleTypeCode,"+oldSampleTypeCode+","+newSampleTypeCode);			
					content.properties.put("fromSampleTypeCode", new PropertySingleValue (newSampleTypeCode));
				}
			}
			MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME,child);
		}

		Logger.debug("Fin updateContainer");
	}
	@SuppressWarnings("unchecked")
	private void updateExperiment(String sampleCode,String oldSampleTypeCode, String newSampleTypeCode , String oldSampleCategoryCode, String newSampleCategoryCode,  ContextValidation cv)
	{
		Logger.debug("Update experiment "+new Date());
		//Gestion des atm en entrée
		List<Experiment> experiments = MongoDBDAO.find(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, DBQuery.is("atomicTransfertMethods.inputContainerUseds.contents.sampleCode", sampleCode)).toList();
		for(Experiment exp : experiments){
			Logger.debug("Update experiment inputContainerUseds.contents "+exp.code);
			for(AtomicTransfertMethod atm : exp.atomicTransfertMethods){
				if (atm.inputContainerUseds !=null) {
					for(InputContainerUsed input : atm.inputContainerUseds){
						for(Content inputContent : input.contents){						
							if(inputContent.sampleCode.equals(sampleCode)){
								((List<String>)cv.getObject("RecapExperiment")).add("contents "+inputContent.sampleCode+" of "+exp.code+","+InstanceConstants.EXPERIMENT_COLL_NAME+",sampleTypeCode,"+oldSampleTypeCode+","+newSampleTypeCode+",sampleCategoryCode,"+oldSampleCategoryCode+","+newSampleCategoryCode);
								inputContent.sampleTypeCode=newSampleTypeCode;
								inputContent.sampleCategoryCode=newSampleCategoryCode;			
							}
						}
					}
				}
			}
			MongoDBDAO.update(InstanceConstants.EXPERIMENT_COLL_NAME, exp);
		}
		//Gestion atm en sortie
		experiments = MongoDBDAO.find(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, DBQuery.is("atomicTransfertMethods.outputContainerUseds.contents.sampleCode", sampleCode)).toList();
		for(Experiment exp : experiments){
			Logger.debug("Update experiment outputContainerUseds.contents "+exp.code);

			for(AtomicTransfertMethod atm : exp.atomicTransfertMethods){
				for(OutputContainerUsed output : atm.outputContainerUseds){
					for(Content outputContent : output.contents){
						if(outputContent.sampleCode.equals(sampleCode)){
							((List<String>)cv.getObject("RecapExperiment")).add("contents "+outputContent.sampleCode+" of "+exp.code+","+InstanceConstants.EXPERIMENT_COLL_NAME+",sampleTypeCode,"+oldSampleTypeCode+","+newSampleTypeCode+",sampleCategoryCode,"+oldSampleCategoryCode+","+newSampleCategoryCode);
							outputContent.sampleTypeCode=newSampleTypeCode;
							outputContent.sampleCategoryCode=newSampleCategoryCode;	
						}
					}
				}
			}
			MongoDBDAO.update(InstanceConstants.EXPERIMENT_COLL_NAME, exp);
		}
		
		// Gestion cette fois des atm pour lesquels on a un lien sur le sample dans le properties (fromSampleCode)
		experiments = MongoDBDAO.find(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, DBQuery.all("atomicTransfertMethods.inputContainerUseds.contents.properties.fromSampleCode.value", sampleCode)).toList();
		for(Experiment exp : experiments){
			Logger.debug("Update experiment inputContainerUseds.contents.properties "+exp.code);
			exp.traceInformation.modifyDate= new Date();
			exp.traceInformation.modifyUser= user;			
			for(AtomicTransfertMethod atm : exp.atomicTransfertMethods){
				if (atm.inputContainerUseds !=null) {
					for(InputContainerUsed input : atm.inputContainerUseds){
						for(Content inputContent : input.contents){													
							if (inputContent.properties.containsKey("fromSampleCode") && inputContent.properties.get("fromSampleCode").value.equals(sampleCode)) {
								((List<String>)cv.getObject("RecapExperiment")).add("properties de content enfant "+inputContent.sampleCode+" of "+exp.code+","+InstanceConstants.EXPERIMENT_COLL_NAME+",content.properties.fromSampleTypeCode,"+oldSampleTypeCode+","+newSampleTypeCode);
								inputContent.properties.put("fromSampleTypeCode", new PropertySingleValue(newSampleTypeCode));
							}
						}
					}
				}
			}
			MongoDBDAO.update(InstanceConstants.EXPERIMENT_COLL_NAME, exp);
		}
		experiments = MongoDBDAO.find(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, DBQuery.all("atomicTransfertMethods.outputContainerUseds.contents.properties.fromSampleCode.value", sampleCode)).toList();
		for(Experiment exp : experiments){
			Logger.debug("Update experiment inputContainerUseds.contents.properties "+exp.code);
			exp.traceInformation.modifyDate= new Date();
			exp.traceInformation.modifyUser= user;			
			for(AtomicTransfertMethod atm : exp.atomicTransfertMethods){
				if (atm.inputContainerUseds !=null) {
					for(OutputContainerUsed output : atm.outputContainerUseds){
						for(Content outputContent : output.contents){													
							if (outputContent.properties.containsKey("fromSampleCode") && outputContent.properties.get("fromSampleCode").value.equals(sampleCode)) {
								((List<String>)cv.getObject("RecapExperiment")).add("properties de content enfant "+outputContent.sampleCode+" of "+exp.code+","+InstanceConstants.EXPERIMENT_COLL_NAME+",content.properties.fromSampleTypeCode,"+oldSampleTypeCode+","+newSampleTypeCode);
								outputContent.properties.put("fromSampleTypeCode", new PropertySingleValue(newSampleTypeCode));
							}
						}
					}
				}
			}
			MongoDBDAO.update(InstanceConstants.EXPERIMENT_COLL_NAME, exp);
		}

		Logger.debug("Fin updateExperiment");
	}

	@SuppressWarnings("unchecked")
	private void updateProcess(String sampleCode,String oldSampleTypeCode, String newSampleTypeCode , String oldSampleCategoryCode, String newSampleCategoryCode,  ContextValidation cv)
	{
		Logger.debug("Update process "+new Date());
		List<Process> processes = MongoDBDAO.find(InstanceConstants.PROCESS_COLL_NAME, Process.class, DBQuery.all("sampleCodes", sampleCode)).toList();

		for(Process process : processes){
			Logger.debug("Update process "+process.code+" "+new Date());

			if (process.sampleOnInputContainer.sampleCode.contentEquals(sampleCode)) { //Pas d'enfant
				((List<String>)cv.getObject("RecapProcess")).add(process.code+","+InstanceConstants.PROCESS_COLL_NAME+",sampleOnInputContainer.sampleTypeCode,"+oldSampleTypeCode+","+newSampleTypeCode+",sampleOnInputContainer.sampleCategoryCode,"+oldSampleCategoryCode+","+newSampleCategoryCode+",lastUpdateDate");
				process.sampleOnInputContainer.lastUpdateDate = new Date();
				process.sampleOnInputContainer.sampleTypeCode= newSampleTypeCode;
				process.sampleOnInputContainer.sampleCategoryCode = newSampleCategoryCode;
			}else // dans le cas où on a des samples qui donnent d'autres samples dans le process
				//Update des process enfant (1ere generation)
				if (process.sampleOnInputContainer.properties.containsKey("fromSampleCode") && process.sampleOnInputContainer.properties.get("fromSampleCode").value.equals(sampleCode)) {
					((List<String>)cv.getObject("RecapProcess")).add(process.code+","+InstanceConstants.PROCESS_COLL_NAME+",sampleOnInputContainer.properties.fromSampleTypeCode,"+oldSampleTypeCode+","+newSampleTypeCode+",lastUpdateDate");
					process.sampleOnInputContainer.lastUpdateDate = new Date();
					process.sampleOnInputContainer.properties.get("fromSampleTypeCode").value = newSampleTypeCode;
				}
			MongoDBDAO.update(InstanceConstants.PROCESS_COLL_NAME,process);
		}
		Logger.debug("Fin updateProcess");
	}



	@SuppressWarnings("unchecked")
	private void updateReadSet(String sampleCode,String oldSampleTypeCode, String newSampleTypeCode , String oldSampleCategoryCode, String newSampleCategoryCode,  ContextValidation cv)
	{
		Logger.debug("Update ReadSet");
		List<ReadSet> readsets = MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.is("sampleOnContainer.sampleCode",sampleCode)).toList();
		for(ReadSet readset : readsets){
			Logger.debug("Update ReadSet "+readset.code+" "+new Date()) ;
			((List<String>)cv.getObject("RecapReadSet")).add(readset.code+","+InstanceConstants.READSET_ILLUMINA_COLL_NAME+",sampleOnContainer.sampleTypeCode,"+oldSampleTypeCode+","+newSampleTypeCode+",sampleOnContainer.sampleCategoryCode,"+oldSampleCategoryCode+","+newSampleCategoryCode+", traceinfo");

			readset.sampleOnContainer.sampleTypeCode = newSampleTypeCode;
			readset.sampleOnContainer.sampleCategoryCode= newSampleCategoryCode;
			readset.traceInformation.modifyUser= user;
			readset.traceInformation.modifyDate= new Date();

			MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, readset);	
		}
//Gestion des readsets correspondants à des samples enfant du sample considéré
		readsets = MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.is("sampleOnContainer.properties.fromSampleCode.value",sampleCode)).toList();
		for(ReadSet child : readsets){
			Logger.debug("Update ReadSet "+child.code+" "+new Date()) ;
			((List<String>)cv.getObject("RecapReadSet")).add(child.code+","+InstanceConstants.READSET_ILLUMINA_COLL_NAME+",sampleOnContainer.properties.fromSampleTypeCode,"+oldSampleTypeCode+","+newSampleTypeCode);

			child.sampleOnContainer.properties.get("fromSampleTypeCode").value= newSampleTypeCode;
			MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME,child);
		}		
		Logger.debug("Fin updateReadset");
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
				//	Logger.debug("recap: "+recap);
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
		try (OutputStream fileOut = new FileOutputStream("scriptUpdateSampleTypeFromList_out.xls")) {
			wb.write(fileOut);
			fileOut.flush();
		}catch(Exception e){
			Logger.debug(e.getMessage());
		}

	}
}
