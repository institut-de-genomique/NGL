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

//http://localhost:9000/scripts/run/scripts.UpdateRefCollabCNS?jira=SUPSQ-5243
//en indiquant dans le body,un fichier Excel avec header de 2 colonnes : colonne 1  avec codeSample et colonne 2 avec NouvelRefCollab
//Attention a ne pas mettre de valeurs dans la premiere ligne reservée aux intitulées des colonnes.
	public class UpdateRefCollabCNS extends ScriptWithArgsAndExcelBody <UpdateRefCollabCNS.Args> {

	public static class Args {
		public String jira;
	}

	/**
	 * Script permettant de modifier les refCollab d'un ensemble d'echantillon CNS
	 * Ce script va appeler la validation du sample qui vérifiera les règles de nomenclature de la refCollab qui est pour rappel
	 * Regex=^[\\.A-Za-z0-9_-]+$ max caractères=25
	 * Prend en entrée un fichier excel au format suivant 
	 * CodeSample NouvelRefCollab
	 */
	
	@Override
	public void execute(Args args, XSSFWorkbook workbook) throws Exception {
		ContextValidation ctx = ContextValidation.createUpdateContext("ngl-support");
		//NGL-4111
		if ( ! args.jira.matches("^(SUPSQ|SUPSQCNG|NGL)-\\d+$") ) {
			throw new RuntimeException("argument jira " +  args.jira + " qui n'a pas la forme attendue SUPSQ-XXX ou SUPSQCNG-XXX ou NGL-XXX");
		}
		
		workbook.getSheetAt(0).rowIterator().forEachRemaining(row -> {
			if(row.getRowNum() == 0) return; // skip header
			if(row!=null && row.getCell(0)!=null && row.getCell(1)!=null){
				String sampleCode = row.getCell(0).getStringCellValue();
				String newRefCollab = row.getCell(1).getStringCellValue();
				Logger.debug("Code sample "+sampleCode+" ref collab "+newRefCollab);
				//Get sampleCode
				Sample sample = MongoDBDAO.findByCode(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, sampleCode);
				if(sample!=null){
					//Update refCollab
					String oldRefCollab = sample.referenceCollab;
					sample.referenceCollab=newRefCollab;
					sample.traceInformation.modifyUser="ngl-support";
					sample.traceInformation.modifyDate=new Date();
					//NGL-4111
					String stComment = args.jira;
					if (StringUtils.isNotBlank(oldRefCollab)) {
						stComment = args.jira + " old refCollab : "  + oldRefCollab;
					}
					if (sample.technicalComments == null) {
						 sample.technicalComments =  new ArrayList<Comment>();
					} 
					sample.technicalComments.add(new Comment(stComment, "ngl-support", true));
					
					sample.validate(ctx);
					if(!ctx.hasErrors()){
						MongoDBDAO.update(InstanceConstants.SAMPLE_COLL_NAME, sample);
					}else{
						Logger.error("Error sample update refCollab "+sampleCode+" "+ctx.getErrors());
					}
				}else{
					Logger.error("Sample code not found "+sampleCode);
				}
			}
		});
	}


}
