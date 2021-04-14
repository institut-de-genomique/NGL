package scripts;

import java.util.Date;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptWithExcelBody;
import models.laboratory.common.instance.Comment;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.sample.instance.Sample;
import models.utils.InstanceConstants;
import play.Logger;
import validation.ContextValidation;

public class UpdateTaraSamplePropertiesCNS extends ScriptWithExcelBody{

	/**
	 * Script permettant de modifier les prop Tara d'un ensemble d'echantillon CNS
	 * Ce script va appeler la validation du sample qui vérifiera les règles de nomenclature de la refCollab qui est pour rappel
	 * Regex=^[\\.A-Za-z0-9_-]+$ max caractères=25
	 * Prend en entrée un fichier excel au format suivant 
	 * CodeSample NewPropValue OldFractionSize
	 * !!!!Attention a modifier la prop a prendre en compte
	 */
	@Override
	public void execute(XSSFWorkbook workbook) throws Exception {
		workbook.getSheetAt(0).rowIterator().forEachRemaining(row -> {
			if(row.getRowNum() == 0) return; // skip header
			if(row!=null && row.getCell(0)!=null && row.getCell(1)!=null){
				String sampleCode = row.getCell(0).getStringCellValue();
				String newtaraDepthCode = row.getCell(1).getStringCellValue();
				String oldDepthInFile = row.getCell(2).getStringCellValue();
				PropertyValue oldDepthCodeProp=new PropertySingleValue("not defined");
			
				//Get sampleCode
				Sample sample = MongoDBDAO.findByCode(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, sampleCode);
				if(sample!=null){
					//Create contexte validation 
					ContextValidation ctx = ContextValidation.createUpdateContext("ngl-support");
					
					//Update Property
					if (sample.properties.get("taraDepthCode") != null)
					oldDepthCodeProp = sample.properties.get("taraDepthCode");
					
					// Pour valider le fichier
					if (! oldDepthCodeProp.getValue().equals(oldDepthInFile))
					Logger.error("ATTENTION Code sample "+sampleCode+" old taraDepthCode "+oldDepthCodeProp.getValue()+" old taraDepthCode in File "+oldDepthInFile);
					
					if (sample.properties.get("taraDepthCode") == null)
						sample.properties.put("taraDepthCode", new PropertySingleValue(newtaraDepthCode));
					
					sample.properties.get("taraDepthCode").assignValue(newtaraDepthCode);
					
					sample.traceInformation.modifyUser="ngl-support";
					sample.traceInformation.modifyDate=new Date();
					//ATTENTION si existe deja un commentaire on ne pourra pas visualiser ce nouveau commentaire!!
					//sample.comments.add(new Comment("SUPSQ-3850","ngl-support"));
					if (sample.comments!= null && sample.comments.get(0).comment != null){
						sample.comments.get(0).comment = "SUPSQ-3850 "+sample.comments.get(0).comment;
						sample.comments.get(0).createUser= "ngl-support";
						sample.comments.get(0).creationDate= new Date();
					}else{
						sample.comments.add(new Comment("SUPSQ-3850", "ngl-support"));
					}
					
					Logger.debug("Code sample "+sampleCode+" taraDepthCode "+newtaraDepthCode);
					sample.validate(ctx);
					if(!ctx.hasErrors()){
						MongoDBDAO.update(InstanceConstants.SAMPLE_COLL_NAME, sample);
					}else{
						Logger.error("Error sample property "+sampleCode+" "+ctx.getErrors());
					}
				}else{
					//Logger.error("Sample code not found "+sampleCode);
				}
			}
		});
	}

}
