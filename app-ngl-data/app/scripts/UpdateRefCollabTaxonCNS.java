package scripts;

import java.util.ArrayList;
import java.util.Date;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptWithExcelBody;
import models.laboratory.common.instance.Comment;
import models.laboratory.sample.instance.Sample;
import models.utils.InstanceConstants;
import play.Logger;
import validation.ContextValidation;

public class UpdateRefCollabTaxonCNS extends ScriptWithExcelBody{

	/**
	 * Script permettant de modifier les refCollab et taxon d'un ensemble d'echantillon CNS
	 * Ce script va appeler la validation du sample qui vérifiera les règles de nomenclature de la refCollab qui est pour rappel
	 * Regex=^[\\.A-Za-z0-9_-]+$ max caractères=25
	 * Prend en entrée un fichier excel au format suivant 
	 * CodeSample NouvelRefCollab taxonId
	 */
	@Override
	public void execute(XSSFWorkbook workbook) throws Exception {
		workbook.getSheetAt(0).rowIterator().forEachRemaining(row -> {
			if(row.getRowNum() == 0) return; // skip header
			//if(row!=null && row.getCell(0)!=null && row.getCell(1)!=null && row.getCell(2)!=null){
			if(row!=null && row.getCell(0)!=null && row.getCell(1)!=null && row.getCell(2)!=null && row.getCell(3)!=null && row.getCell(4)!=null && row.getCell(5)!=null){

				String sampleCode = row.getCell(0).getStringCellValue();
				String refCollab = row.getCell(1).getStringCellValue();
				String newRefCollab = row.getCell(3).getStringCellValue();

				//String com= "SUPSQ-????";
				String com = row.getCell(5).getStringCellValue();

				Logger.debug("Code sample "+sampleCode);

				Integer taxon=  (int)row.getCell(2).getNumericCellValue();				
				Integer newTaxon=  (int)row.getCell(4).getNumericCellValue();
				//Get sampleCode
				Sample sample = MongoDBDAO.findByCode(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, sampleCode);
				if(sample!=null){
					Logger.debug("Code sample "+sampleCode+" ref collab old : file : new "+sample.referenceCollab+":"+refCollab+":"+newRefCollab+ " taxon old : file : new "+ sample.taxonCode+":"+taxon+":"+newTaxon+":"+com);

					//Create contexte validation 
					ContextValidation ctx = ContextValidation.createUpdateContext("ngl-support");
					//Update refCollab
					sample.referenceCollab=newRefCollab;
					
					if (sample.taxonCode.equals(newTaxon.toString())) {
						sample.taxonCode=newTaxon.toString();
						sample.ncbiLineage=null;
						sample.ncbiScientificName=null;
					}
					sample.traceInformation.modifyUser="ngl-support";
					sample.traceInformation.modifyDate=new Date();

					if (sample.comments!= null && ! sample.comments.isEmpty() ){

						if (sample.comments.get(0).comment != null) {
							//Si ne change pas le comment on laisse tel quel (cas de maj multiples)
							if (! sample.comments.get(0).comment.contains(com))
								sample.comments.get(0).comment = com+" "+sample.comments.get(0).comment;
						}else {
							sample.comments.get(0).comment = com;
						}
						sample.comments.get(0).createUser= "ngl-support";
						sample.comments.get(0).creationDate= new Date();
					}else{
						sample.comments.add(new Comment(com, "ngl-support"));
					} 

					sample.validate(ctx);
					if(!ctx.hasErrors()){
						MongoDBDAO.update(InstanceConstants.SAMPLE_COLL_NAME, sample);
					}else{
						Logger.error("Error sample update refCollab and taxon "+sampleCode+" "+ctx.getErrors());
					}
				}else{
					Logger.error("Sample code not found "+sampleCode);
				}
			}else {
				Logger.error("Error in: "+ row.getCell(0).getStringCellValue());
			}
		});
	}

}
