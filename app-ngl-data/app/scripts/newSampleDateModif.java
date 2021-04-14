package scripts;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptWithExcelBody;
import fr.cea.ig.lfw.controllers.scripts.chunked.ScriptWithArgsAndBody;
import fr.cea.ig.ngl.NGLApplication;
import models.Constants;
import models.laboratory.common.instance.Comment;
import models.laboratory.sample.instance.Sample;
import models.utils.InstanceConstants;
import play.Logger;
import play.mvc.Http.RequestBody;
import scripts.newSampleDateModif.Args;
import validation.ContextValidation;


/**
 * 
 *  Ce script permet de mettre a jour la date de modif d'un sample pour réactiver la cascade automatique nocturne 
 * 
 * Format fichier : CodeSample 
 * Extension xlsx
 * 
 * @author gsamson
 *
 */
//public class UpdateSampleRefCollabFromNoParent extends ScriptWithExcelBody{
public class newSampleDateModif extends ScriptWithArgsAndBody<newSampleDateModif.Args> {
	// structure de controle et stockage des arguments attendus dans l'url. Declarer les champs public.
	public static class Args {
		public String jira;
	}

	private final NGLApplication app;

	@Inject
	public newSampleDateModif(NGLApplication app) {
		super();
		this.app=app;
	}



	@Override
	public void execute(Args args, RequestBody body) throws Exception {

		try {
			// le fichier est récuperé dans le POST MultipartFormData dans 'xlsx'
			File fxlsx = (File) body.asMultipartFormData().getFile("xlsx").getFile();
			FileInputStream fis = new FileInputStream(fxlsx);

			XSSFWorkbook workbook = new XSSFWorkbook (fis);// workbook du fichier
			println("Load File for update ModifyDate "+args.jira);
			Logger.info("Load File for update DateModif "+args.jira);

			try {
				XSSFSheet sheet=workbook.getSheetAt(0);
				if ( null != sheet ) {	
					ContextValidation contextError = ContextValidation.createUndefinedContext(Constants.NGL_DATA_USER);

					workbook.getSheetAt(0).rowIterator().forEachRemaining(row -> {
						if(row.getRowNum() == 0) return; // skip header
						if(row!=null && row.getCell(0)!=null ){
							String sampleCode = row.getCell(0).getStringCellValue();
							//Get sampleCode
							Sample sample = MongoDBDAO.findByCode(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, sampleCode);
							if(sample!=null){
								Logger.debug("Code sample "+sampleCode);
								//Create contexte validation 
								ContextValidation ctx = ContextValidation.createUpdateContext("ngl-support");
								//Update refCollab
								sample.traceInformation.modifyUser="ngl-support";
								sample.traceInformation.modifyDate=new Date();
								if (sample.comments!= null && sample.comments.size() != 0 && sample.comments.get(0).comment != null){
									//Si ne change pas le comment on laisse tel quel (cas de maj multiples)
									if (! sample.comments.get(0).comment.equals("SUPSQ-4057 "+sample.comments.get(0).comment))
										sample.comments.get(0).comment = "SUPSQ-4057 "+sample.comments.get(0).comment;

									sample.comments.get(0).createUser= "ngl-support";
									sample.comments.get(0).creationDate= new Date();
								}else{
									sample.comments.add(new Comment("SUPSQ-4057", "ngl-support"));
								}
								Logger.debug("comment: "+sample.comments.get(0).comment);
							//	sample.validate(ctx);
								if(!ctx.hasErrors()){
									MongoDBDAO.update(InstanceConstants.SAMPLE_COLL_NAME, sample);
								}else{
									Logger.error("Error sample update "+ctx.getErrors());
								}
							}else{
								Logger.error("Sample code not found "+sampleCode);
							}
						}
					});
					println("end of file!");
					Logger.info("end of file!");
				} else {
					throw new Exception("sheet not found.");
				}
			} catch (Exception e) {
				println(e.getMessage());
			}
		} catch (Exception e) {
			// pas un  fichier Excel...
			println(e.getMessage());
		}
	}

}	
