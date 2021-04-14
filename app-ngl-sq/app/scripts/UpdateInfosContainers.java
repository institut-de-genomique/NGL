package scripts;

import java.util.Date;
import java.util.List;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.chunked.ScriptWithExcelBody;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.container.instance.Container;
import models.laboratory.project.instance.Project;
import models.utils.InstanceConstants;
import play.Logger;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.mongojack.DBQuery;

/** 
 * @author jcharpen - Jordi CHARPENTIER - jcharpen@genoscope.cns.fr
 */
public class UpdateInfosContainers extends ScriptWithExcelBody {
	
	private void update(XSSFWorkbook workbook) {	
		workbook.getSheetAt(0).rowIterator().forEachRemaining(row -> {	
            if (row != null && row.getCell(0) != null) {			
                String containerCode = row.getCell(0).getStringCellValue();
                containerCode = containerCode.trim();

                Container c = MongoDBDAO.findByCode(InstanceConstants.CONTAINER_COLL_NAME, Container.class, containerCode);

                double concentration = row.getCell(1).getNumericCellValue();
                String concentrationUnit = row.getCell(2).getStringCellValue();
                double volume = row.getCell(3).getNumericCellValue();
                String volumeUnit = row.getCell(4).getStringCellValue();
                double quantite = row.getCell(5).getNumericCellValue();
                String quantiteUnit = row.getCell(6).getStringCellValue();
                    
                concentrationUnit = concentrationUnit.trim();
                volumeUnit = volumeUnit.trim();
                quantiteUnit = quantiteUnit.trim();

                Logger.error("containerCode : " + containerCode);
                Logger.error("concentration : " + concentration);
                Logger.error("concentrationUnit : " + concentrationUnit);
                Logger.error("volume : " + volume);
                Logger.error("volumeUnit : " + volumeUnit);
                Logger.error("quantite : " + quantite);
                Logger.error("quantiteUnit : " + quantiteUnit);
                    
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
