package scripts;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.mongodb.BasicDBObject;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;

import controllers.admin.supports.api.NGLObject;
import controllers.admin.supports.api.NGLObjectsSearchForm;
import controllers.admin.supports.api.objects.AbstractUpdate;
import controllers.admin.supports.api.objects.ContainerUpdate;
import controllers.admin.supports.api.objects.ExperimentUpdate;
import controllers.admin.supports.api.objects.ProcessUpdate;
import controllers.admin.supports.api.objects.ReadSetUpdate;
import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptWithExcelBody;
import models.laboratory.sample.instance.Sample;
import models.utils.InstanceConstants;
import play.Logger;
import validation.ContextValidation;

/**
 * Script permettant de lancer en mode batch l'ajout d'une propriété content 
 * Attention : ce script mettra à jour tous les objets de la requête contrairement à l'interface qui permet de selectionner les objets à mettre à jour
 * Pour le moment aucune règle de selection, tout est mis à jour à partir d'une liste de sampleCodes
 * Attention Tous les samples enfant sont aussi mis à jour
 * Fichier excel (une ligne pour le header)
 * CodeProjet / Code sample / New code Property / New value
 * 
 * @author ejacoby
 *
 */
public class AddBatchContentProperties extends ScriptWithExcelBody{

	final String user = "ngl-admin";

	public static class Args {
	}

	private Map<String, AbstractUpdate<?>>   mappingCollectionUpdates;

	
	@Override
	public void execute(XSSFWorkbook workbook) throws Exception {
		getLogger().debug("Start update content properties");

		
		//Init mappCollection update 
		mappingCollectionUpdates = new HashMap<>();
		mappingCollectionUpdates.put("ngl_sq.Container", new ContainerUpdate());
		mappingCollectionUpdates.put("ngl_sq.Process", new ProcessUpdate());
		mappingCollectionUpdates.put("ngl_sq.Experiment", new ExperimentUpdate());
		//ReadSets controller argument null (required not null for delete action)
		mappingCollectionUpdates.put("ngl_bi.ReadSetIllumina", new ReadSetUpdate(null));
		List<String> collectionNames = Arrays.asList("ngl_sq.Container","ngl_sq.Process","ngl_sq.Experiment","ngl_bi.ReadSetIllumina");
		ContextValidation cv = ContextValidation.createUpdateContext(user);
		cv.putObject("Recap", new ArrayList<String>());
		//File xlsx = (File) body.asMultipartFormData().getFile("excel").getFile();
		//FileInputStream fis = new FileInputStream(xlsx);
		//getLogger().debug("File excel "+fis);
		// Finds the workbook instance for XLSX file
		//XSSFWorkbook workbook = new XSSFWorkbook (fis);
		workbook.getSheetAt(0).rowIterator().forEachRemaining(row -> {
			if(row.getRowNum() == 0) return; // skip header
			if(row!=null){
				//Search by oldValue
				NGLObjectsSearchForm nglSearchForm = new NGLObjectsSearchForm();
				String newProperty	       = row.getCell(2).getStringCellValue();
				String newValue	       = row.getCell(3).getStringCellValue();
				nglSearchForm.projectCode=row.getCell(0).getStringCellValue();
				nglSearchForm.sampleCode=row.getCell(1).getStringCellValue();
				nglSearchForm.contentPropertyNameUpdated=newProperty;
				//nglSearchForm.contentProperties.put(property, Arrays.asList(oldValue));
				nglSearchForm.collectionNames=collectionNames;
				getLogger().debug("get list NGL Object for sample "+nglSearchForm.sampleCode+" update "+nglSearchForm.contentPropertyNameUpdated+" with "+newValue);
				List<Sample> childSamples = MongoDBDAO.find(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, DBQuery.regex("life.path", Pattern.compile(","+nglSearchForm.sampleCode))).toList();
				for(String collectionName : collectionNames){
					//Update Sample parent
					nglSearchForm.projectCode=row.getCell(0).getStringCellValue();
					nglSearchForm.sampleCode=row.getCell(1).getStringCellValue();
					Logger.debug("Update sample "+nglSearchForm.sampleCode);
					updateSample(nglSearchForm, collectionName, newValue, cv);
					//Update child sample
					for(Sample sampleChild:childSamples){
						nglSearchForm.sampleCode=sampleChild.code;
						//Get project code if mulitple project error return
						if(sampleChild.projectCodes.size()==1){
							nglSearchForm.projectCode=sampleChild.projectCodes.iterator().next();
							Logger.debug("Update child sample "+sampleChild.code);
							updateSample(nglSearchForm, collectionName, newValue, cv);
						}else{
							cv.addError("projectCodes", "Muliple project for "+sampleChild.code);
						}
					}
				}
			}


		});
		//Get error
		if(cv.hasErrors()){
			Logger.debug(cv.getErrors().toString());
		}
		//Create excel file to recap in execution directory
		createExcelFileRecap(cv);
	}


	private BasicDBObject getKeys() {
		BasicDBObject keys = new BasicDBObject();
		keys.put("code",1);
		keys.put("typeCode",1);
		return keys;
	}

	@SuppressWarnings("unchecked")
	private void updateSample(NGLObjectsSearchForm nglSearchForm, String collectionName, String newValue, ContextValidation cv)
	{
		Query q =  mappingCollectionUpdates.get(collectionName).getQuery(nglSearchForm);
		if(q!=null){
			Logger.debug("Get NGL Object from "+collectionName);
			List<NGLObject> r = MongoDBDAO.find(collectionName, NGLObject.class, q, getKeys()).toList();
			Logger.debug("Size of list objects "+r.size());
			r.forEach(o -> {
				ContextValidation oCv = ContextValidation.createUpdateContext(user);
				getLogger().debug("treat"+o.code);
				if(o.code!=null){
					o.collectionName = collectionName;
					o.contentPropertyNameUpdated = nglSearchForm.contentPropertyNameUpdated;
					//o.currentValue = nglSearchForm.contentProperties.get(nglSearchForm.contentPropertyNameUpdated).get(0);
					o.action=NGLObject.Action.add.toString();
					o.newValue=newValue;
					o.projectCode = nglSearchForm.projectCode;
					o.sampleCode = nglSearchForm.sampleCode;	
					//o.nbOccurrences = mappingCollectionUpdates.get(collectionName).getNbOccurrence(o);
					mappingCollectionUpdates.get(collectionName).update(o, oCv);
					if(oCv.hasErrors()){
						cv.addErrors(oCv.getErrors());
						((List<String>)cv.getObject("Recap")).add(nglSearchForm.sampleCode+","+collectionName+","+o.code+",ERROR,"+o.nbOccurrences);
					}
					else
						((List<String>)cv.getObject("Recap")).add(nglSearchForm.sampleCode+","+collectionName+","+o.code+","+newValue+","+o.nbOccurrences);
				}
			});
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
		List<String> recaps = (List<String>) cv.getObject("Recap");
		Workbook wb = new HSSFWorkbook();
		CreationHelper createHelper = wb.getCreationHelper();
		Sheet sheet = wb.createSheet("Recap");
		int nbLine=0;
		for(String recap : recaps){
			Logger.debug(recap);
			Row row = sheet.createRow(nbLine);
			String[] tabRecap = recap.split(",");
			for(int i=0;i<tabRecap.length;i++){
				row.createCell(i).setCellValue(
						createHelper.createRichTextString(tabRecap[i]));
			}
			nbLine++;
		}
		// Write the output to a file
		try (OutputStream fileOut = new FileOutputStream("Test.xls")) {
			wb.write(fileOut);
		}catch(Exception e){
			Logger.debug(e.getMessage());
		}
	}

	

}
