package scripts;

import java.util.Date;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptWithExcelBody;
import models.laboratory.common.instance.Comment;
import models.laboratory.sample.instance.Sample;
import models.utils.InstanceConstants;
import play.Logger;
import validation.ContextValidation;

public class UpdateTaxonIdCNS extends ScriptWithExcelBody{

	/**
	 * Script permettant de modifier les taxonId d'un ensemble d'echantillon CNS
	 * Ce script va appeler la validation du sample qui vérifiera les règles de nomenclature de la taxonId qui est pour rappel
	 * Regex=^[\\.A-Za-z0-9_-]+$ max caractères=25
	 * Prend en entrée un fichier excel au format suivant 
	 * CodeSample NewTaxonId
	 */
	@Override
	public void execute(XSSFWorkbook workbook) throws Exception {
		workbook.getSheetAt(0).rowIterator().forEachRemaining(row -> {
			if(row.getRowNum() == 0) return; // skip header
			if(row!=null && row.getCell(0)!=null && row.getCell(1)!=null){
				String sampleCode = row.getCell(0).getStringCellValue();
				String newTaxonId = row.getCell(1).getStringCellValue();
				String ticket= "SUPSQ-4031";
				Logger.debug("Code sample "+sampleCode+" taxonId "+newTaxonId);
				//Get sampleCode
				Sample sample = MongoDBDAO.findByCode(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, sampleCode);
				if(sample!=null){
					//Create contexte validation 
					ContextValidation ctx = ContextValidation.createUpdateContext("ngl-support");
					//Update taxonId
					sample.taxonCode=newTaxonId;
					sample.ncbiScientificName=null;
					sample.ncbiLineage=null;
					sample.traceInformation.modifyUser="ngl-support";
					sample.traceInformation.modifyDate=new Date();

					if (sample.comments!= null && ! sample.comments.isEmpty() && sample.comments.get(0).comment != null){
						if (!sample.comments.get(0).comment.contains(ticket))
							sample.comments.get(0).comment = ticket+" "+sample.comments.get(0).comment;
						sample.comments.get(0).createUser= "ngl-support";
						sample.comments.get(0).creationDate= new Date();
					}else{
						sample.comments.add(new Comment(ticket, "ngl-support")); 
					}

					sample.validate(ctx);
					if(!ctx.hasErrors()){
						//MongoDBDAO.update(InstanceConstants.SAMPLE_COLL_NAME, sample);
					}else{
						Logger.error("Error sample update taxonId "+sampleCode+" "+ctx.getErrors());
					}
				}else{
					Logger.error("Sample code not found "+sampleCode);
				}
			}
		});
	}

}
