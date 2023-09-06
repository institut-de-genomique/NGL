package scripts;

import java.util.ArrayList;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptWithArgsAndExcelBody;
import models.laboratory.common.instance.Comment;
import models.laboratory.sample.instance.Sample;
import models.utils.InstanceConstants;
import play.Logger;
import validation.ContextValidation;

public class UpdateRefCollabTaxonCNS extends ScriptWithArgsAndExcelBody <UpdateRefCollabTaxonCNS.Args> {

	public static class Args {
		public String jira;
	}

	/**
	 * Script permettant de modifier les refCollab et taxon d'un ensemble d'echantillon CNS
	 * Ce script va appeler la validation du sample qui vérifiera les règles de nomenclature de la refCollab qui est pour rappel
	 * Regex=^[\\.A-Za-z0-9_-]+$ max caractères=25
	 * Prend en entrée un fichier excel au format suivant 
	 * CodeSample refCollab taxon NewRefCollab taxonId Commentaire
	 * * ATTENTION la 1ere ligne du fichier n'est pas lue!!! Il s'agit de l'entête!!!!
	 * 
	 * ATTENTION, dans le cas où on ne modifierai que le taxon, il ne faut pas mettre a jour la date de modif
	 * c'est la récupération des infos au ncbi qui entraine la modif de la date
	 * ex de lancement : 
	 * http://localhost:9000/scripts/run/scripts.UpdateRefCollabTaxonCNS?jira=SUPSQ-4444
	 */
	@Override
	public void execute(Args args, XSSFWorkbook workbook) throws Exception {
		//NGL-4111
		if ( ! args.jira.matches("^(SUPSQ|SUPSQCNG|NGL)-\\d+$") ) {
			throw new RuntimeException("argument jira " +  args.jira + " qui n'a pas la forme attendue SUPSQ-XXX ou SUPSQCNG-XXX ou NGL-XXX");
		}
		
		workbook.getSheetAt(0).rowIterator().forEachRemaining(row -> {
			if(row.getRowNum() == 0) return; // skip header
			//if(row!=null && row.getCell(0)!=null && row.getCell(1)!=null && row.getCell(2)!=null){
			if(row!=null && row.getCell(0)!=null && row.getCell(1)!=null && row.getCell(2)!=null && row.getCell(3)!=null && row.getCell(4)!=null && row.getCell(5)!=null){

				String sampleCode = row.getCell(0).getStringCellValue();
				String refCollab = row.getCell(1).getStringCellValue();
				String newRefCollab = row.getCell(3).getStringCellValue();

				Logger.debug("Code sample "+sampleCode);

				Integer taxon=  (int)row.getCell(2).getNumericCellValue();				
				Integer newTaxon=  (int)row.getCell(4).getNumericCellValue();
				//Get sampleCode
				Sample sample = MongoDBDAO.findByCode(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, sampleCode);
				if(sample!=null){
					Logger.debug("Code sample "+sampleCode+" ref collab old : file : new "+sample.referenceCollab+":"+refCollab+":"+newRefCollab+ " taxon old : file : new "+ sample.taxonCode+":"+taxon+":"+newTaxon+":"+args.jira);

					//Create contexte validation 
					ContextValidation ctx = ContextValidation.createUpdateContext("ngl-support");
	
					//NGL-4111
					String stComment = args.jira;
					boolean modifRefCollab = false;
					if (StringUtils.isNotBlank(sample.referenceCollab)) {
						stComment = args.jira + " old refCollab : "  + sample.referenceCollab;
						modifRefCollab = true;
					}
					//Update refCollab
					sample.referenceCollab = newRefCollab;
					boolean modifTaxonCode = false;
					if ((newTaxon != 0) &&  StringUtils.isNotBlank(sample.taxonCode) && ! (sample.taxonCode.equals(newTaxon.toString()))) {
						// NGL-4111
						modifTaxonCode = true;
						if(modifRefCollab) {
							stComment = stComment + " et old taxon : " + sample.taxonCode; 
						} else {
							stComment = args.jira + " old taxon : "  + sample.taxonCode;
						}
						sample.taxonCode = newTaxon.toString();
						sample.ncbiLineage = null;
						sample.ncbiScientificName = null;
					}else {
						/* La mise a jour de la date de modif ne doit etre effectuée que dans le cas où on ne 
						 *remet pas a null les infos de taxon
						 * sinon on risque de lancer la cascade de maj trop tôt
						 * On met a jour la date seulement si on ne modifie que le taxon
						 */
						sample.traceInformation.modifyUser="ngl-support";
						sample.traceInformation.modifyDate=new Date();
					}
					
					//NGL-4111
					if (modifRefCollab || modifTaxonCode) {
						if (sample.technicalComments == null) {
							sample.technicalComments =  new ArrayList<Comment>();
						}
						sample.technicalComments.add(new Comment(stComment, "ngl-support", true));
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
			Logger.info("Fin update Sample");
			println("End of update ");
		});
	}

}
