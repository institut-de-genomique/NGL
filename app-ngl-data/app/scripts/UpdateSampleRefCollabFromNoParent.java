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
import scripts.UpdateSampleRefCollabFromNoParent.Args;
import services.instance.sample.UpdateSamplePropertiesCNS;
import validation.ContextValidation;


/**
 * 
 *  Script permettant d'exécuter la mise à jour de la propriété refCollab en cascade a partir d'un échantillon non parent
 * Ticket support SUPSQ-3859 : Demande Caro pour soumission CEB issu de CEA (multi projet) projet témoin négatif
 * 
 * Format fichier : CodeSample NewRefCollab
 * Extension xlsx
 * 
 * @author ejacoby
 *
 */
//public class UpdateSampleRefCollabFromNoParent extends ScriptWithExcelBody{
public class UpdateSampleRefCollabFromNoParent extends ScriptWithArgsAndBody<UpdateSampleRefCollabFromNoParent.Args> {
	// structure de controle et stockage des arguments attendus dans l'url. Declarer les champs public.
	public static class Args {
		public String jira;
	}

	private final NGLApplication app;

	@Inject
	public UpdateSampleRefCollabFromNoParent(NGLApplication app) {
		super();
		this.app=app;
	}

	
	/*	public void execute(XSSFWorkbook workbook) throws Exception {

	ContextValidation contextError = ContextValidation.createUndefinedContext(Constants.NGL_DATA_USER);
		UpdateSamplePropertiesCNS update = new UpdateSamplePropertiesCNS(app);
		contextError.addKeyToRootKeyName("import");
		workbook.getSheetAt(0).rowIterator().forEachRemaining(row -> {
			if(row.getRowNum() == 0) return; // skip header
			if(row!=null && row.getCell(0)!=null && row.getCell(1)!=null){
				String sampleCode = row.getCell(0).getStringCellValue();
				String newRefCollab = row.getCell(1).getStringCellValue();
				Sample sample = MongoDBDAO.findByCode(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, sampleCode);
				sample.comments.add(new Comment("SUPSQ-4003 old ref collab "+sample.referenceCollab, "ngl-support"));
				sample.referenceCollab=newRefCollab;
				sample.traceInformation.modifyUser="ngl-support";
				sample.traceInformation.modifyDate=new Date();

				//update sample in database
				MongoDBDAO.update(InstanceConstants.SAMPLE_COLL_NAME, sample);
				update.updateOneSample(sample, contextError);
			}
		});
		if(contextError.hasErrors()){
			Logger.debug("Error "+contextError.getErrors());
		}else{
			Logger.debug("Modif refCollab ok");
		}
	}

}*/
	//Version utilisée pour ticket 	SUPSQ-4003
	@Override
	public void execute(Args args, RequestBody body) throws Exception {

		try {
			// le fichier est récuperé dans le POST MultipartFormData dans 'xlsx'
			File fxlsx = (File) body.asMultipartFormData().getFile("xlsx").getFile();
			FileInputStream fis = new FileInputStream(fxlsx);

			XSSFWorkbook workbook = new XSSFWorkbook (fis);// workbook du fichier
			println("Load File for update RefCollab "+args.jira);
			Logger.info("Load File for update RefCollab "+args.jira);
			
			//l'onglet (sheet) DOIT s'appeler "index"
			try {
				XSSFSheet sheet=workbook.getSheetAt(0);
				if ( null != sheet ) {	
					ContextValidation contextError = ContextValidation.createUndefinedContext(Constants.NGL_DATA_USER);
					UpdateSamplePropertiesCNS update = new UpdateSamplePropertiesCNS(app);
					contextError.addKeyToRootKeyName("import");

					workbook.getSheetAt(0).rowIterator().forEachRemaining(row -> {
						if(row.getRowNum() == 0) return; // skip header
						if(row!=null && row.getCell(0)!=null && row.getCell(1)!=null){
							String sampleCode = row.getCell(0).getStringCellValue();
							String newRefCollab = row.getCell(1).getStringCellValue();
							Sample sample = MongoDBDAO.findByCode(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, sampleCode);
							if (sample !=null){
								println("update "+sample.code+"  old ref collab "+sample.referenceCollab+ " new ref collab "+newRefCollab);
								Logger.info("update "+sample.code+"  old ref collab "+sample.referenceCollab+ " new ref collab "+newRefCollab);
								if (! sample.referenceCollab.equals(newRefCollab)){
									//Seul 1 commentaire est affiché dans la vue du coup on va se condtenter d'incrémenter le commentaire existant
									//sample.comments.add(new Comment(args.jira+" old ref collab "+sample.referenceCollab, "ngl-support"));

									if (sample.comments!= null && sample.comments.get(0).comment != null){
										sample.comments.get(0).comment = args.jira+" old ref collab "+sample.referenceCollab+" "+sample.comments.get(0).comment;
										sample.comments.get(0).createUser= "ngl-support";
										sample.comments.get(0).creationDate= new Date();
									}else{
										sample.comments.add(new Comment(args.jira+" old ref collab "+sample.referenceCollab, "ngl-support"));
									}

									sample.referenceCollab=newRefCollab;
									sample.traceInformation.modifyUser="ngl-support";
									sample.traceInformation.modifyDate=new Date();

									//Update a décommenter Pour lancer la mise a jour
									//update sample in database
									MongoDBDAO.update(InstanceConstants.SAMPLE_COLL_NAME, sample);
									update.updateOneSample(sample, contextError);
								}else{
									println("Ref collab is not modified for "+sample.code+"!");
								}
							}else{
								println("Sample "+ sampleCode+" not found!");
							}

						}else if (row!=null && row.getCell(0)!=null && row.getCell(1)==null){
							println("Sample "+ row.getCell(0).getStringCellValue()+" ref collab not found in file!");
							Logger.info("Sample "+ row.getCell(0).getStringCellValue()+" ref collab not found in file!");
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
