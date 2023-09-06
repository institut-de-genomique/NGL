package scripts;

import java.util.ArrayList;
import java.util.Date;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptWithArgsAndExcelBody;
import fr.cea.ig.ngl.NGLApplication;
import models.Constants;
import models.laboratory.common.instance.Comment;
import models.laboratory.sample.instance.Sample;
import models.utils.InstanceConstants;
import play.Logger;
import services.instance.sample.UpdateSamplePropertiesCNS;
import validation.ContextValidation;


/**
 * 
 *  Script permettant d'exécuter la mise à jour de la propriété refCollab en cascade a partir d'un échantillon non parent
 * Ticket support SUPSQ-3859 : Demande Caro pour soumission CEB issu de CEA (multi projet) projet témoin négatif
 * ATTENTION CE SCRIPT NE DEVRAIT PLUS ETRE UTILISE DEPUIS QUE L ON A CREE LA REFCOLLABSUB AU NIVEAU DES READSETS
 * 
 * Format fichier : CodeSample NewRefCollab
 * Extension xlsx
 * Ex de lancement :
 * http://localhost:9000/scripts/run/scripts.UpdateSampleRefCollabFromNoParent?jira=SUPSQ-5243
 * en indiquant dans le body,un fichier Excel avec header de 2 colonnes : colonne 1  avec CodeSample et colonne 2 avec NewRefCollab
 * Attention a ne pas mettre de valeurs dans la premiere ligne reservée aux intitulées des colonnes.
 * @author ejacoby
 *
 */

@Deprecated
public class UpdateSampleRefCollabFromNoParent extends ScriptWithArgsAndExcelBody<UpdateSampleRefCollabFromNoParent.Args> {
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


	//Version utilisée pour ticket 	SUPSQ-4003
	@Override
	//public void execute(Args args, RequestBody body) throws Exception {
	public void execute(Args args, XSSFWorkbook workbook) throws Exception {
		//NGL-4111
		if ( ! args.jira.matches("^(SUPSQ|SUPSQCNG|NGL)-\\d+$") ) {
			throw new RuntimeException("argument jira " +  args.jira + " qui n'a pas la forme attendue SUPSQ-XXX ou SUPSQCNG-XXX ou NGL-XXX");
		}
		

		//l'onglet (sheet) DOIT s'appeler "index"
		try {
			XSSFSheet sheet=workbook.getSheetAt(0);
			if ( null == sheet ) {
				throw new Exception("sheet not found.");
			}
			ContextValidation contextError = ContextValidation.createUndefinedContext(Constants.NGL_DATA_USER);
			UpdateSamplePropertiesCNS update = new UpdateSamplePropertiesCNS(app);
			contextError.addKeyToRootKeyName("import");

			workbook.getSheetAt(0).rowIterator().forEachRemaining(row -> {
				if(row.getRowNum() == 0) {
					return; // skip header
				}
				if(row!=null && row.getCell(0)!=null && row.getCell(1)!=null) {
					String sampleCode = row.getCell(0).getStringCellValue();
					String newRefCollab = row.getCell(1).getStringCellValue();
					Sample sample = MongoDBDAO.findByCode(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, sampleCode);
					if (sample !=null) {
						println("update "+sample.code+"  old ref collab "+sample.referenceCollab+ " new ref collab "+newRefCollab);
						Logger.info("update "+sample.code+"  old ref collab "+sample.referenceCollab+ " new ref collab "+newRefCollab);
						if (! sample.referenceCollab.equals(newRefCollab)) {
							//NGL-4111
							String stComment = args.jira;
							if (StringUtils.isNotBlank(sample.referenceCollab)) {
								stComment = args.jira + " old refCollab : "  + sample.referenceCollab;
							}
							if (sample.technicalComments == null) {
								sample.technicalComments =  new ArrayList<Comment>();
							} 
							sample.technicalComments.add(new Comment(stComment, "ngl-support", true));
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

				}else if (row != null && row.getCell(0) != null && row.getCell(1) == null) {
					println("Sample "+ row.getCell(0).getStringCellValue()+" ref collab not found in file!");
					Logger.info("Sample "+ row.getCell(0).getStringCellValue()+" ref collab not found in file!");
				}
			});
			println("end of file!");
			Logger.info("end of file!");

		} catch (Exception e) {
			println(e.getMessage());
		}
	}
}	
