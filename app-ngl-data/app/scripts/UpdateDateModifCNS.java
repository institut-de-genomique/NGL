package scripts;

import java.util.ArrayList;
import java.util.Date;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptWithArgsAndExcelBody;
import models.laboratory.common.instance.Comment;
import models.laboratory.sample.instance.Sample;
import models.utils.InstanceConstants;
import play.Logger;
import validation.ContextValidation;

/**
 * Script permettant d'actualiser la date de mise a jour d'un ensemble d'echantillon CNS
 * Ce script est utile dans le cadre de support sur des échantillons pour lesquels on souhaite lancer
 * la cascade nocturne de mise a jour de prop de Sample
 * 
 * Prend en entrée un fichier excel au format suivant 
 * CodeSample 
 * Attention la 1ere ligne du fichier n'est pas lue!
 * 
 * L'appel du script se fait avec le param xlsx
 * Ex:
 * http://localhost:9000/scripts/run/scripts.UpdateDateModifCNS?jira=SUPSQ-4444
 */

public class UpdateDateModifCNS extends ScriptWithArgsAndExcelBody<UpdateDateModifCNS.Args>{

	// structure de controle et stockage des arguments attendus dans l'url. Declarer les champs public.
	public static class Args {
		public String jira;
	}

	@Override
	public void execute(Args args, XSSFWorkbook workbook) throws Exception {
		println("Load File for update Sample modifyDate "+args.jira);
		Logger.info("Load File for update Sample "+args.jira);
		//NGL-4111
		if ( ! args.jira.matches("^(SUPSQ|SUPSQCNG|NGL)-\\d+$") ) {
			throw new RuntimeException("argument jira " +  args.jira + " qui n'a pas la forme attendue SUPSQ-XXX ou SUPSQCNG-XXX ou NGL-XXX");
		}
		
		try {
			//l'onglet (sheet) DOIT s'appeler "index"
			XSSFSheet sheet=workbook.getSheetAt(0);
			if ( null == sheet ) {	
				throw new Exception("sheet not found.");
			}
			workbook.getSheetAt(0).rowIterator().forEachRemaining(row -> {
				if(row.getRowNum() == 0) return; // skip header
				if(row!=null && row.getCell(0)!=null ){
					String sampleCode = row.getCell(0).getStringCellValue();

					//	Logger.debug("Code sample "+sampleCode);
					//Get sampleCode
					Sample sample = MongoDBDAO.findByCode(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, sampleCode);
					if(sample!=null){
						//Create contexte validation 
						ContextValidation ctx = ContextValidation.createUpdateContext("ngl-support");
						//Update date de modif
						sample.traceInformation.modifyUser="ngl-support";
						sample.traceInformation.modifyDate=new Date();
						if (sample.technicalComments == null) {
							sample.technicalComments =  new ArrayList<Comment>();
						} 
						sample.technicalComments.add(new Comment(args.jira, "ngl-support", true));
						sample.validate(ctx);
						if(!ctx.hasErrors()){
							MongoDBDAO.update(InstanceConstants.SAMPLE_COLL_NAME, sample);
						}else{
							Logger.error("Error sample update  "+sampleCode+" "+ctx.getErrors());
						}
					}else{
						Logger.error("Sample code not found "+sampleCode);
					}
				}
			});
		} catch (Exception e) {
			println(e.getMessage());
		}
		println("End of update ");
		Logger.info("End of update "+args.jira);

	}

}
