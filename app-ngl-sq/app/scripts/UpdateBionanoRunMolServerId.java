package scripts;

import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptWithExcelBody;
import models.laboratory.run.instance.Run;
import models.utils.InstanceConstants;
import play.Logger;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.property.PropertySingleValue;

/**
 * Second script développé pour le ticket NGL-2966 de reprise de passif Bionano.
 * But du script : Mettre à jour les id de lanes de run Bionano.
 * Le script prend en entrée un fichier Excel avec ce format :
 * CODE RUN | ID FLOWCELL LANE 1 | ID FLOWCELL LANE 2
 * La ligne 1 ne contient que les libellés des colonnes.
 * 
 * Règles :
 * - Le run existe bien en base.
 * - On met à jour les id des lanes d'un run uniquement si ces lanes n'avaient pas d'id à la base.
 * - Chaque colonne contenant un id peut avoir un "/" ou un nombre.
 * - Si la colonne contient un slash, on le met tel quel dans l'id de la lane.
 * 
 * @author Jordi CHARPENTIER jcharpen@genoscope.cns.fr 
 */
public class UpdateBionanoRunMolServerId extends ScriptWithExcelBody {

	@Override
	public void execute(XSSFWorkbook workbook) throws Exception {
		// Parcours des lignes du premier feuille et du fichier Excel.
		workbook.getSheetAt(0).rowIterator().forEachRemaining(row -> {
			// On ignore la première ligne : c'est le libellé des colonnes.
			if (row.getRowNum() == 0) {
				return;
			}
			
			if (row != null && row.getCell(0) != null && row.getCell(1) != null && row.getCell(2) != null) { // Si la ligne et les cellules de la ligne ne sont pas vides.
				String runCode = row.getCell(0).getStringCellValue();
				Logger.debug("Code RUN : " + runCode);

				double idMol1 = -1;
				boolean hasSlash1 = false;

				if (row.getCell(1).getCellType() == Cell.CELL_TYPE_NUMERIC) {
					idMol1 = row.getCell(1).getNumericCellValue();
				} else if (row.getCell(1).getCellType() == Cell.CELL_TYPE_STRING && row.getCell(1).getStringCellValue().trim().equals("/")) {
					Logger.debug("On a un slash dans l'id.");
					hasSlash1 = true;
				} else {
					Logger.debug("Erreur - ID 1 - Pas un slash ni un chiffre.");
				}

				Logger.debug("idMol1 : " + idMol1);			

				double idMol2 = -1;
				boolean hasSlash2 = false;

				if (row.getCell(2).getCellType() == Cell.CELL_TYPE_NUMERIC) {
					idMol2 = row.getCell(2).getNumericCellValue();
				} else if (row.getCell(2).getCellType() == Cell.CELL_TYPE_STRING && row.getCell(2).getStringCellValue().trim().equals("/")) {
					Logger.debug("On a un slash dans l'ID.");
					hasSlash2 = true;
				} else {
					Logger.debug("Erreur - ID 2 - Pas un slash ni un chiffre.");
				}

				Logger.debug("idMol2 : " + idMol2);

				Run run = MongoDBDAO.findByCode(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, runCode);

				if (run == null) { // Le run n'existe pas.
					Logger.debug("Le run n'existe pas. Rien a mettre a jour.");				
				} else { // Le run existe.
					if (run.categoryCode.equals("bionano")) {
						Logger.debug("Le run existe. On met a jour son mapCreationJobId.");

						// Parcours des lanes du run.
						for (int i = 0; i < run.lanes.size(); i++) {
							Map<String, Map<String, PropertyValue>> map = run.lanes.get(i).treatments.get("mapCreationJobId").results;
							Map<String, PropertyValue> valueFromDB = map.get("default");

							if (valueFromDB.get("mapCreationJobId") == null) { // Si la lane n'a pas d'id.
								Map<String, PropertyValue> mapMapId = new HashMap<>();

								// Si idMol1 == -1, c'est un slash.
								if ((run.lanes.get(i).number == 1) && (idMol1 != -1)) {
									Logger.debug("Mise à jour de l'id de la FC 1.");
									mapMapId.put("mapCreationJobId", new PropertySingleValue(String.valueOf(idMol1).replace(".0", ""), null));
								} else if ((run.lanes.get(i).number == 1) && (idMol1 == -1) && hasSlash1) {
									Logger.debug("Mise à jour de l'id de la FC 1 avec un slash.");
									mapMapId.put("mapCreationJobId", new PropertySingleValue("/", null));
								}

								// Si idMol2 == -1, c'est un slash.
								if ((run.lanes.get(i).number == 2) && (idMol2 != -1)) {
									Logger.debug("Mise a jour de l'id de la FC 2.");
									mapMapId.put("mapCreationJobId", new PropertySingleValue(String.valueOf(idMol2).replace(".0", ""), null));
								} else if ((run.lanes.get(i).number == 2) && (idMol2 == -1) && hasSlash2) {
									Logger.debug("Mise a jour de l'id de la FC 2 avec un slash.");
									mapMapId.put("mapCreationJobId", new PropertySingleValue("/", null));
								}

								map.put("default", mapMapId);
							} else {
								Logger.debug("Un id existe deja pour la lane " + run.lanes.get(i).number + ". Pas de mise a jour.");
							}
						}

						// Mise à jour en base.
						MongoDBDAO.update(InstanceConstants.RUN_ILLUMINA_COLL_NAME, run);
					} else { // Le run n'est pas un run bionano.
						Logger.debug("Le run n'est pas un run bionano.");
					}
				}

				Logger.debug("===");
			}
		});
	}
}
