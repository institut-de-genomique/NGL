package scripts;

import java.util.ArrayList;
import java.util.Date;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptWithArgsAndExcelBody;
import models.laboratory.common.instance.Comment;
import models.laboratory.container.instance.Container;
import models.laboratory.sample.instance.Sample;
import models.utils.InstanceConstants;
import play.Logger;
import validation.ContextValidation;

public class UpdateSampleImportTypeDate extends ScriptWithArgsAndExcelBody<UpdateSampleImportTypeDate.Args>{

	/**
	 * Script permettant de corriger l'importTypeCode des sample, pour éviter les pb de cascade de mise à jour
	 * erronnée et d'actualiser la date de mise a jour d'un ensemble d'echantillon CNS
	 * Ce script est utile dans le cadre de support sur des échantillons pour lesquels on souhaite lancer
	 * la cascade nocturne de mise a jour de prop de Sample
	 * 
	 * Ce script va appeler la validation du sample qui vérifiera les règles de nomenclature de importTypeCode qui est pour rappel
	 * Regex=^[\\.A-Za-z0-9_-]+$ max caractères=25
	 * Prend en entrée un fichier excel au format suivant 
	 * CodeSample 
	 * ATTENTION
	 * - la 1ere ligne du fichier n'est pas lue!
	 * - le nom du feuillet excel doit s'appeler index
	 * L'appel du script se fait avec le param xlsx
	 * Ex de lancement:
	 * http://localhost:9000/scripts/run/scripts.UpdateSampleImportTypeDate?jira=SUPSQ-4444 et fichier excel dans body
	 */

	// structure de controle et stockage des arguments attendus dans l'url. Declarer les champs public.
	public static class Args {
		public String jira;
	}

	@Inject
	public UpdateSampleImportTypeDate() {
		super();
	}

	@Override
	public void execute(Args args, XSSFWorkbook workbook) throws Exception {
		//NGL-4111
		if ( ! args.jira.matches("^(SUPSQ|SUPSQCNG|NGL)-\\d+$") ) {
			throw new RuntimeException("argument jira " +  args.jira + " qui n'a pas la forme attendue SUPSQ-XXX ou SUPSQCNG-XXX ou NGL-XXX");
		}
		
		workbook.getSheetAt(0).rowIterator().forEachRemaining(row -> {
			// NGL-4111
			String newComment = args.jira;
			if(row.getRowNum() == 0) return; // skip header
			if(row!=null && row.getCell(0)!=null ){
				String sampleCode = row.getCell(0).getStringCellValue();
				ContextValidation ctx = ContextValidation.createUpdateContext("ngl-support");
				//	Logger.debug("Code sample "+sampleCode);
				//Get sampleCode
				Sample sample = MongoDBDAO.findByCode(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, sampleCode);
				if(sample!=null){
					//Create contexte validation 

					//Update date de modif
					sample.traceInformation.modifyUser="ngl-support";
					sample.traceInformation.modifyDate=new Date();
					//NGL-4111
					if (StringUtils.isNotBlank(sample.importTypeCode)) {
						newComment = args.jira + " old importType : "  + sample.importTypeCode;
					}
					if (sample.technicalComments == null) {
						sample.technicalComments =  new ArrayList<Comment>();
					} 
					sample.technicalComments.add(new Comment(newComment, "ngl-support", true));

					if (sample.importTypeCode.equals("update-tara-pacific")) {
						sample.importTypeCode="reception-tara-pacific";
					}else if(sample.importTypeCode.equals("update-tara-mp")){
						sample.importTypeCode="reception-tara-mp";
					}else {
						Logger.debug("importType diff de update");
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

				//Get container
				Container container = MongoDBDAO.findByCode(InstanceConstants.CONTAINER_COLL_NAME, Container.class, sampleCode);
				if(container!=null) {
					Logger.debug("Container "+container.code);
					//Update date de modif
					container.traceInformation.modifyUser="ngl-support";
					container.traceInformation.modifyDate=new Date();
					if (container.comments!= null && ! container.comments.isEmpty() && container.comments.get(0).comment != null){
						//Cas tres particulier où le numéro du ticket concerné a déja été ajouté au com									
						if (container.comments.get(0).comment.equals(args.jira) || container.comments.get(0).comment.equals(" "+args.jira) ) {
							container.comments.get(0).comment=newComment; 
						}else {
							if (container.comments.get(0).comment.contains(args.jira)) {
								container.comments.get(0).comment= container.comments.get(0).comment.replaceFirst("SUPSQ-4444",newComment+"_ "); 
							}else {
								container.comments.get(0).comment=newComment+"_ "+container.comments.get(0).comment; 
							}
						}
					}else{
						container.comments.add(new Comment(newComment, "ngl-support")); 
					}

					if (container.importTypeCode.equals("update-tara-pacific")) {
						container.comments.get(0).createUser= "ngl-support";
						container.comments.get(0).creationDate= new Date();
						container.importTypeCode="reception-tara-pacific";
					}else if(container.importTypeCode.equals("update-tara-mp")){
						container.comments.get(0).createUser= "ngl-support";
						container.comments.get(0).creationDate= new Date();
						container.importTypeCode="reception-tara-mp";
					}else {
						Logger.debug("importType diff de update");
					}
					Logger.debug("Code sample "+sampleCode+" importTypeCode: "+container.importTypeCode+" com: "+container.comments.get(0).comment);
					MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME, container);

				}
			}
		});


		println("End of update ");
		Logger.info("End of update "+args.jira);

	}

}