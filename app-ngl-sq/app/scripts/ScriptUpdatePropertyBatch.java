package scripts;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptWithExcelBody;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.sample.instance.Sample;
import models.utils.InstanceConstants;
import java.util.*;
import play.Logger;
import validation.ContextValidation;

public class ScriptUpdatePropertyBatch extends ScriptWithExcelBody {
	
	@Override
	public void execute(XSSFWorkbook workbook) throws Exception {
		Logger.error("Start ScriptUpdatePropertyBatch");

		// Parcours des lignes du premier feuille et du fichier Excel.
		workbook.getSheetAt(0).rowIterator().forEachRemaining(row -> {
			// On ignore la première ligne : c'est le libellé des colonnes.
			if (row.getRowNum() == 0) {
				return;
			}
			
			if (row != null && row.getCell(0) != null && row.getCell(1) != null) { // Si la ligne et les cellules de la ligne ne sont pas vides.
				String sampleCode = row.getCell(0).getStringCellValue();
				String batch = row.getCell(1).getStringCellValue();

				if (sampleCode != null) {
					Sample sample = MongoDBDAO.findByCode(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, sampleCode);
				
					if (sample != null) {
						if (sample.life == null) {
							if ("reception-trec".equals(sample.importTypeCode) ||
								"reception-tara-microbiome".equals(sample.importTypeCode) || 
								"update-tara-microbiome".equals(sample.importTypeCode) ||
								"biological-sample-reception".equals(sample.importTypeCode)) {

								Logger.error("Mise à jour du sample : '" + sample.code + "'.");
								ContextValidation contextValidation = ContextValidation.createUpdateContext("ngl-support");

								sample.traceInformation.modifyUser = "ngl-support";
								sample.traceInformation.modifyDate = new Date();

								if (sample.properties == null) {
									sample.properties = new HashMap();
								}						

								sample.properties.put("batch", new PropertySingleValue(batch));

								Logger.error("Ajout de la propriété 'batch' avec comme valeur : " + batch + ".");

								sample.validate(contextValidation); 

								if (!contextValidation.hasErrors()) {
									MongoDBDAO.update(InstanceConstants.SAMPLE_COLL_NAME, sample);
								} else {
									Logger.error(sample.code + " a des erreurs de validation : " + contextValidation.getErrors());
								}
							} else {
								Logger.error("Le sample '" + sampleCode + "' n'est pas avec un import autorisé.");
							} 
						} else {
							Logger.error("Le sample '" + sample.code + "' n'est pas le sample parent.");
						}
					} else {
						Logger.error("Sample non trouvé (" + sampleCode + ").");
					}
				} else {
					Logger.error("Code sample null.");
				}
			}
		});

		Logger.error("End ScriptUpdatePropertyBatch");
	}
}