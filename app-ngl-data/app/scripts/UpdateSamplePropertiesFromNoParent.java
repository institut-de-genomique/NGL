package scripts;

import java.util.Date;
import javax.inject.Inject;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptWithExcelBody;
import fr.cea.ig.ngl.NGLApplication;
import models.Constants;
import models.laboratory.sample.instance.Sample;
import models.utils.InstanceConstants;
import play.Logger;
import services.instance.sample.UpdateSamplePropertiesCNS;
import validation.ContextValidation;


/**
 * 
 *  Script permettant d'exécuter la cascade de mise a jour de sample a partir de sample non parent
 * Ticket NGL-3504
 * 
 * Format fichier : CodeSample 
 * Extension xlsx
 * l'onglet (sheet) DOIT s'appeler "index"
 * l'execution du script se fait avec en paramètre -xlsx: nom du fichier
 * ATTENTION la 1ere ligne du fichier n'est pas lue!!! Il s'agit de l'entête!!!!		
 * 
 * @author gsamson
 *
 */

public class UpdateSamplePropertiesFromNoParent extends ScriptWithExcelBody{

	private final NGLApplication app;


	@Inject
	public UpdateSamplePropertiesFromNoParent(NGLApplication app) {
		super();
		this.app=app;
	}

	public void execute(XSSFWorkbook workbook) throws Exception {
		ContextValidation contextError = ContextValidation.createUndefinedContext(Constants.NGL_DATA_USER);
		UpdateSamplePropertiesCNS update = new UpdateSamplePropertiesCNS(app);

		contextError.addKeyToRootKeyName("import");

		workbook.getSheetAt(0).rowIterator().forEachRemaining(row -> {
			if(row.getRowNum() == 0) return; // skip header
			if(row!=null && row.getCell(0)!=null ){
				String sampleCode = row.getCell(0).getStringCellValue();				

				Sample sample = MongoDBDAO.findByCode(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, sampleCode);

				if (sample != null ) {
					sample.traceInformation.modifyUser="ngl-support";
					sample.traceInformation.modifyDate=new Date();
					Logger.debug("sample: "+sample.code);	
					//update sample in database
					MongoDBDAO.update(InstanceConstants.SAMPLE_COLL_NAME, sample);
					update.updateOneSample(sample, contextError);
				}else {
					Logger.debug("sample: "+sampleCode+" not find");
				}
			}
		});
		if(contextError.hasErrors()){
			Logger.debug("Error "+contextError.getErrors());
		}else{
			Logger.info("Fin cascade mise a jour de samples");
			println("End of update ");
		}
	}
}	
