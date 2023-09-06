package scripts;

import java.util.Date;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.chunked.ScriptWithExcelBody;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.container.instance.Container;
import models.utils.InstanceConstants;
import play.Logger;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/** 
 * Le script s'appelle UpdateInfosContainers et a été remonté dans la branche isoprod sq.
 * Il prend un Excel en entrée avec 7 colonnes (sans en-tête) :

 * Code container | Concentration | Unité concentration | Volume | Unité volume | Concentration | Unité concentration
 * Des tests sont faits sur le contenu du fichier. Notamment :

 * Si les valeurs sont vides, on ne met rien à jour.
 * On trim toutes les cellules.
 * @author jcharpen - Jordi CHARPENTIER - jcharpen@genoscope.cns.fr
 */
public class UpdateInfosContainers extends ScriptWithExcelBody {
	
	private void update(XSSFWorkbook workbook) {	
		workbook.getSheetAt(0).rowIterator().forEachRemaining(row -> {	
            if (row != null && row.getCell(0) != null) {			
                String containerCode = row.getCell(0).getStringCellValue();
                containerCode = containerCode.trim();

                Container c = MongoDBDAO.findByCode(InstanceConstants.CONTAINER_COLL_NAME, Container.class, containerCode);

                if (c != null) {
                    Logger.error(containerCode);

                    double concentration = 0.0;
                    
                    try {
                        concentration = row.getCell(1).getNumericCellValue();
                    } catch (NullPointerException | IllegalStateException e) {

                    }

                    String concentrationUnit = row.getCell(2).getStringCellValue();

                    Logger.error(concentration + " " + concentrationUnit);

                    double volume = 0.0;

                    try {
                        volume = row.getCell(3).getNumericCellValue();
                    } catch (NullPointerException | IllegalStateException e) {

                    }

                    String volumeUnit = row.getCell(4).getStringCellValue();

                    Logger.error(volume + " " + volumeUnit);

                    double quantite = 0.0;

                    try {
                        quantite = row.getCell(5).getNumericCellValue();
                    } catch (NullPointerException | IllegalStateException e) {

                    }

                    String quantiteUnit = row.getCell(6).getStringCellValue();

                    Logger.error(quantite + " " + quantiteUnit);
                        
                    concentrationUnit = concentrationUnit.trim();
                    volumeUnit = volumeUnit.trim();
                    quantiteUnit = quantiteUnit.trim();
                        
                    PropertySingleValue concProperty = new PropertySingleValue(concentration, concentrationUnit);
                    PropertySingleValue volumeProperty = new PropertySingleValue(volume, volumeUnit);
                    PropertySingleValue quantiteProperty = new PropertySingleValue(quantite, quantiteUnit);

                    if (concentration != 0.0) {
                        c.concentration = concProperty;
                    }

                    if (volume != 0.0) {
                        c.volume = volumeProperty;
                    }

                    if (quantite != 0.0) {
                        c.quantity = quantiteProperty;
                    }

                    c.traceInformation.modifyUser = "ngl-support";
                    c.traceInformation.modifyDate = new Date();
                            
                    MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME, c);
                } else {
                    Logger.error("Le container '" + containerCode + "' n'existe pas.");
                }
            }
        });
		
		Logger.debug("End update containers");
	}

	@Override
	public void execute(XSSFWorkbook workbook) throws Exception {
		Logger.debug("Start update containers");
		
		update(workbook);
	}
}
