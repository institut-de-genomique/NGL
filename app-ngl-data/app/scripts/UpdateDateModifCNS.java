package scripts;

import java.io.File;
import java.io.FileInputStream;
import java.util.Date;

import javax.inject.Inject;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptWithExcelBody;
import fr.cea.ig.lfw.controllers.scripts.chunked.ScriptWithArgsAndBody;
import fr.cea.ig.ngl.NGLApplication;
import models.laboratory.common.instance.Comment;
import models.laboratory.sample.instance.Sample;
import models.utils.InstanceConstants;
import play.Logger;
import play.mvc.Http.RequestBody;
import validation.ContextValidation;

public class UpdateDateModifCNS extends ScriptWithArgsAndBody<UpdateDateModifCNS.Args>{

	/**
	 * Script permettant d'actualiser la date de mise a jour d'un ensemble d'echantillon CNS
	 * Ce script est utile dans le cadre de support sur des échantillons pour lesquels on souhaite lancer
	 * la cascade nocturne de mise a jour de prop de Sample
	 * 
	 * Ce script va appeler la validation du sample qui vérifiera les règles de nomenclature de la taxonId qui est pour rappel
	 * Regex=^[\\.A-Za-z0-9_-]+$ max caractères=25
	 * Prend en entrée un fichier excel au format suivant 
	 * CodeSample 
	 * Attention la 1ere ligne du fichier n'est pas lue!
	 * 
	 * L'appel du script se fait avec le param xlsx
	 * Ex:
	 * http://localhost:9000/scripts/run/scripts.UpdateDateModifCNS?jira=SUPSQ-4444
	 */

	// structure de controle et stockage des arguments attendus dans l'url. Declarer les champs public.
	public static class Args {
		public String jira;
	}

	private final NGLApplication app;

	@Inject
	public UpdateDateModifCNS(NGLApplication app) {
		super();
		this.app=app;
	}

	@Override
	public void execute(Args args, RequestBody body) throws Exception {

		String newComment= "- 08/04/21 : l'import type \"Update Tara Pacific\" est remis en \"Réception tara pacific\" (cf. NGL-3317) pour résoudre "+args.jira+"\r\n"+
				"- échantillon mis à jour via le fichier update tara pacific";


		try {
			// le fichier est récuperé dans le POST MultipartFormData dans 'xlsx'
			File fxlsx = (File) body.asMultipartFormData().getFile("xlsx").getFile();
			FileInputStream fis = new FileInputStream(fxlsx);

			XSSFWorkbook workbook = new XSSFWorkbook (fis);// workbook du fichier
			println("Load File for update Sample modifyDate "+args.jira);
			Logger.info("Load File for update Sample "+args.jira);

			//l'onglet (sheet) DOIT s'appeler "index"
			try {
				XSSFSheet sheet=workbook.getSheetAt(0);
				if ( null != sheet ) {	

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

								if (sample.comments!= null && ! sample.comments.isEmpty() && sample.comments.get(0).comment != null){
									//commentaire deja mis a jour
									
									//Cas tres particulier où le numéro du ticket concerné a déja été ajouté au com									
									if (sample.comments.get(0).comment.equals(args.jira) || sample.comments.get(0).comment.equals(" "+args.jira) ) {
										sample.comments.get(0).comment=newComment; 

									}else
										if (sample.comments.get(0).comment.contains(args.jira)) {
											sample.comments.get(0).comment= sample.comments.get(0).comment.replaceFirst("SUPSQ-4444",newComment+"\r\n-"); 
										}else {
											sample.comments.get(0).comment=newComment+"\r\n- "+sample.comments.get(0).comment; 
										}

									if (sample.importTypeCode.equals("update-tara-pacific")) {
										sample.importTypeCode="reception-tara-pacific";
									}else {
										Logger.debug("importType diff de update");
									}
									sample.comments.get(0).createUser= "ngl-support";
									sample.comments.get(0).creationDate= new Date();
								}else{
									sample.comments.add(new Comment(newComment, "ngl-support")); 
								}
								Logger.debug("Code sample "+sampleCode+" importTypeCode: "+sample.importTypeCode+" com: "+sample.comments.get(0).comment);

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
				}
			} catch (Exception e) {
				println(e.getMessage());
			}
		} catch (Exception e) {
			// pas un  fichier Excel...
			println(e.getMessage());
		}
		println("End of update ");
		Logger.info("End of update "+args.jira);

	}

	/**
	@Override
	public void execute(Args args, RequestBody body) throws Exception {
		try {
			// le fichier est récuperé dans le POST MultipartFormData dans 'xlsx'
			File fxlsx = (File) body.asMultipartFormData().getFile("xlsx").getFile();
			FileInputStream fis = new FileInputStream(fxlsx);

			XSSFWorkbook workbook = new XSSFWorkbook (fis);// workbook du fichier
			println("Load File for update Sample modifyDate "+args.jira);
			Logger.info("Load File for update Sample "+args.jira);

			//l'onglet (sheet) DOIT s'appeler "index"
			try {
				XSSFSheet sheet=workbook.getSheetAt(0);
				if ( null != sheet ) {	

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

								if (sample.comments!= null && ! sample.comments.isEmpty() && sample.comments.get(0).comment != null){
									if (!sample.comments.get(0).comment.contains(args.jira))
										sample.comments.get(0).comment = args.jira+" "+sample.comments.get(0).comment;
									sample.comments.get(0).createUser= "ngl-support";
									sample.comments.get(0).creationDate= new Date();
								}else{
									sample.comments.add(new Comment(args.jira, "ngl-support")); 
								}
								Logger.debug("Code sample "+sampleCode+" com: "+sample.comments.get(0).comment);

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
				}
			} catch (Exception e) {
				println(e.getMessage());
			}
		} catch (Exception e) {
			// pas un  fichier Excel...
			println(e.getMessage());
		}
		println("End of update ");
		Logger.info("End of update "+args.jira);

	}
	 **/
}
