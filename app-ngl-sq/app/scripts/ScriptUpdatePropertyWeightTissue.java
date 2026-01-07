package scripts;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.mongojack.DBQuery;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptWithExcelBody;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.container.instance.Container;
import models.laboratory.experiment.instance.Experiment;
import models.utils.InstanceConstants;
import java.util.*;
import play.Logger;

public class ScriptUpdatePropertyWeightTissue extends ScriptWithExcelBody {
	
	@Override
	public void execute(XSSFWorkbook workbook) throws Exception {
		Logger.error("Start ScriptUpdatePropertyContainerNotContent");

		// Parcours des lignes du premier feuille et du fichier Excel.
		workbook.getSheetAt(0).rowIterator().forEachRemaining(row -> {
			// On ignore la première ligne : c'est le libellé des colonnes.
			if (row.getRowNum() == 0) {
				return;
			}
			
			if (row != null && row.getCell(0) != null && row.getCell(1) != null) { // Si la ligne et les cellules de la ligne ne sont pas vides.
				String containerCode = row.getCell(0).getStringCellValue();
				Double poids = row.getCell(1).getNumericCellValue();

				if (containerCode != null) {
					Container container = MongoDBDAO.findByCode(InstanceConstants.CONTAINER_COLL_NAME, Container.class, containerCode);
				
					if (container != null) {
						Logger.error("Mise à jour du container : '" + container.code + "'.");

						container.traceInformation.modifyUser = "ngl-support";
						container.traceInformation.modifyDate = new Date();			

						updateQuantity(container, containerCode, poids);
						updateProperty(container, poids);
						updateBroyageAssocies(containerCode, poids);

						Logger.error("Ajout de la propriété 'weightTissue' avec comme valeur : " + poids.longValue() + ".");

						//MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME, container);
					} else {
						Logger.error("Container non trouvé.");
					}
				} else {
					Logger.error("Code container null.");
				}
			}
		});

		Logger.error("End ScriptUpdatePropertyContainerNotContent");
	}

	private void updateQuantity(Container container, String containerCode, Double poids) {
		if (container.quantity == null || container.quantity.value == null) {
			Logger.error("Pour le container '" + containerCode + "' ajout d'une valeur pour la quantité.");
			container.quantity = new PropertySingleValue(poids.longValue(), "mg");
		} else {
			Logger.error("Pour le container '" + containerCode + "' il existe déjà une valeur pour la quantité, elle vaut : " + container.quantity.value + ".");
		}
	}

	private void updateProperty(Container container, Double poids) {
		if (container.properties == null) {
			container.properties = new HashMap();
		}			

		container.properties.put("weightTissue", new PropertySingleValue(poids.longValue(), "mg"));
	}

	private void updateBroyageAssocies(String containerCode, Double poids) {
		List<Experiment> expList = MongoDBDAO.find(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, DBQuery.is("typeCode", "grinding").in("inputContainerCodes", containerCode)).toList();
	
		expList.forEach(exp -> {
			exp.atomicTransfertMethods.forEach(atm -> {
				atm.inputContainerUseds.forEach(icu -> {
					if (icu.quantity != null) {
						icu.quantity.value = poids;
						Logger.error("Mise à jour de la quantité  avec comme valeur : " + poids.longValue() + ".");
					}
				});
			});

			// MongoDBDAO.update(InstanceConstants.EXPERIMENT_COLL_NAME, exp);
		});
	}
}