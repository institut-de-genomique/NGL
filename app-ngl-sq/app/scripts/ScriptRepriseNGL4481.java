package scripts;

import java.io.*;

import java.util.*;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;

import org.mongojack.DBQuery;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.chunked.ScriptWithArgs;

import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.container.instance.Container;
import models.laboratory.experiment.instance.Experiment;
import models.utils.InstanceConstants;

import play.Logger;
import validation.ContextValidation;

public class ScriptRepriseNGL4481 extends ScriptWithArgs<Object> {
	
	private ContextValidation cv;

	@Override
	public void execute(Object obj) throws Exception {
		Logger.error("Début reprise NGL-4481");

		initCtxExcel();

		repriseExpBroyage();

		repriseExpADNHPM();

		repriseExpSamplePrep();

		createExcelFileRecap();

		Logger.error("Fin reprise NGL-4481");
	}

	private void initCtxExcel() {
		this.cv = ContextValidation.createUpdateContext("ngl-support");

		this.cv.putObject("Recap", new ArrayList<String>());
		this.cv.putObject("RecapErreur", new ArrayList<String>());

		((List<String>) this.cv.getObject("Recap")).add("Type expérience, Code expérience, Code container, Valeur quantité, Conversion");
		((List<String>) this.cv.getObject("RecapErreur")).add("Type expérience, Code expérience, Code container, Valeur 'biologicalMaterialInputQty', Erreur");
	}

	private void repriseExpBroyage() {
		Logger.error("Récupération des expériences de broyage");

		List<Experiment> grindingExpList = MongoDBDAO.find(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, DBQuery.is("typeCode", "grinding")).toList();

		for (int i = 0; i < grindingExpList.size(); i++) {
			for (int j = 0; j < grindingExpList.get(i).atomicTransfertMethods.size(); j++) {
				for (int k = 0; k < grindingExpList.get(i).atomicTransfertMethods.get(j).inputContainerUseds.size(); k++) {
					if (grindingExpList.get(i).atomicTransfertMethods.get(j).inputContainerUseds.get(k).experimentProperties != null && grindingExpList.get(i).atomicTransfertMethods.get(j).inputContainerUseds.get(k).experimentProperties.get("biologicalMaterialInputQty") != null) {
						String propValue = String.valueOf(grindingExpList.get(i).atomicTransfertMethods.get(j).inputContainerUseds.get(k).experimentProperties.get("biologicalMaterialInputQty").value);
						// Je supprime les espaces, des fois c'est '1 mg' ou '1mg', on uniformise.
						propValue = propValue.replaceAll(" ", "");
						
						if (propValue.endsWith("mg")) {
							String value = propValue.split("mg")[0];

							try {
								Double valueNumber = Double.valueOf(value);
								Double valueNg = valueNumber * 1000000;

								grindingExpList.get(i).atomicTransfertMethods.get(j).inputContainerUseds.get(k).experimentProperties.get("biologicalMaterialInputQty").value = valueNg;
								((PropertySingleValue) grindingExpList.get(i).atomicTransfertMethods.get(j).inputContainerUseds.get(k).experimentProperties.get("biologicalMaterialInputQty")).unit = "ng";
						
								((List<String>) cv.getObject("Recap")).add("Grinding, " + grindingExpList.get(i).code + ", " + grindingExpList.get(i).atomicTransfertMethods.get(j).inputContainerUseds.get(k).code + ", " + propValue + ", mg -> ng");
							} catch (NumberFormatException e) {
								((List<String>) cv.getObject("RecapErreur")).add("Grinding, " + grindingExpList.get(i).code + ", " + grindingExpList.get(i).atomicTransfertMethods.get(j).inputContainerUseds.get(k).code + ", " + propValue + ", valeur impossible à parser");
							}
						} else if (propValue.endsWith("g")) {
							String value = propValue.split("g")[0];
							Double valueNumber = Double.valueOf(value);
							Double valueG = valueNumber * 1000000000;

							grindingExpList.get(i).atomicTransfertMethods.get(j).inputContainerUseds.get(k).experimentProperties.get("biologicalMaterialInputQty").value = valueG;
							((PropertySingleValue) grindingExpList.get(i).atomicTransfertMethods.get(j).inputContainerUseds.get(k).experimentProperties.get("biologicalMaterialInputQty")).unit = "ng";
						
							((List<String>) cv.getObject("Recap")).add("Grinding, " + grindingExpList.get(i).code + ", " + grindingExpList.get(i).atomicTransfertMethods.get(j).inputContainerUseds.get(k).code + ", " + propValue + ", g -> ng");
						} else {
							((List<String>) cv.getObject("RecapErreur")).add("Grinding, " + grindingExpList.get(i).code + ", " + grindingExpList.get(i).atomicTransfertMethods.get(j).inputContainerUseds.get(k).code + ", " + propValue + ", unité non trouvée");
						}
					}
				}
			}

			// MongoDBDAO.update(InstanceConstants.EXPERIMENT_COLL_NAME, grindingExpList.get(i));
		}

		Logger.error("Fin de la récupération des expériences de broyage");
	}

	private void repriseExpADNHPM() {
		Logger.error("Récupération des expériences d'extraction adn hpm");

		List<Experiment> extractionAdnHPMExpList = MongoDBDAO.find(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, DBQuery.is("typeCode", "hmw-dna-extraction")).toList();

		for (int i = 0; i < extractionAdnHPMExpList.size(); i++) {
			for (int j = 0; j < extractionAdnHPMExpList.get(i).atomicTransfertMethods.size(); j++) {
				for (int k = 0; k < extractionAdnHPMExpList.get(i).atomicTransfertMethods.get(j).inputContainerUseds.size(); k++) {
					Container container = MongoDBDAO.findByCode(InstanceConstants.CONTAINER_COLL_NAME, Container.class, extractionAdnHPMExpList.get(i).atomicTransfertMethods.get(j).inputContainerUseds.get(k).code);

					if (container.properties.get("biologicalMaterialQuantity") != null) {
						String propValue = String.valueOf(container.properties.get("biologicalMaterialQuantity").value);
						// Je supprime les espaces, des fois c'est '1 mg' ou '1mg', on uniformise.
						propValue = propValue.replaceAll(" ", "");

						if (propValue.endsWith("mg")) {
							String value = propValue.split("mg")[0];

							try {
								Double valueNumber = Double.valueOf(value);
								Double valueEnNG = valueNumber * 1000000;

								container.properties.get("biologicalMaterialQuantity").value = valueEnNG;
								((PropertySingleValue) container.properties.get("biologicalMaterialQuantity")).unit = "ng";

								((List<String>) cv.getObject("Recap")).add("ExtractADNHPM, " + extractionAdnHPMExpList.get(i).code + ", " + extractionAdnHPMExpList.get(i).atomicTransfertMethods.get(j).inputContainerUseds.get(k).code + ", " + propValue + ", " + "mg -> ng");

								// MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME, container);
							} catch (NumberFormatException e) {
								((List<String>) cv.getObject("RecapErreur")).add("ExtractADNHPM, " + extractionAdnHPMExpList.get(i).code + ", " + extractionAdnHPMExpList.get(i).atomicTransfertMethods.get(j).inputContainerUseds.get(k).code + ", " + propValue + ", valeur impossible à parser");
							}
						} else if (propValue.endsWith("g")) {
							String value = propValue.split("g")[0];

							try {
								Double valueNumber = Double.valueOf(value);
								Double valueEnNG = valueNumber * 1000000000;

								container.properties.get("biologicalMaterialQuantity").value = valueEnNG;
								((PropertySingleValue) container.properties.get("biologicalMaterialQuantity")).unit = "ng";

								((List<String>) cv.getObject("Recap")).add("ExtractADNHPM, " + extractionAdnHPMExpList.get(i).code + ", " + extractionAdnHPMExpList.get(i).atomicTransfertMethods.get(j).inputContainerUseds.get(k).code + ", " + propValue + ", " + "g -> ng");

								// MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME, container);
							} catch (NumberFormatException e) {
								((List<String>) cv.getObject("RecapErreur")).add("ExtractADNHPM, " + extractionAdnHPMExpList.get(i).code + ", " + extractionAdnHPMExpList.get(i).atomicTransfertMethods.get(j).inputContainerUseds.get(k).code + ", " + propValue + ", valeur impossible à parser");
							}
						} else {
							((List<String>) cv.getObject("RecapErreur")).add("ExtractADNHPM, " + extractionAdnHPMExpList.get(i).code + ", " + extractionAdnHPMExpList.get(i).atomicTransfertMethods.get(j).inputContainerUseds.get(k).code + ", " + propValue + ", unité non trouvée");
						}
					} else {
						((List<String>) cv.getObject("RecapErreur")).add("ExtractADNHPM, " + extractionAdnHPMExpList.get(i).code + ", " + extractionAdnHPMExpList.get(i).atomicTransfertMethods.get(j).inputContainerUseds.get(k).code + ", null, " + "propriété non trouvée");
					}
				}
			}
		}

		Logger.error("Fin de la récupération des expériences d'extraction adn hpm");
	}

	private void repriseExpSamplePrep() {
		Logger.error("Récupération des expériences de sample prep");

		List<Experiment> extractionSamplePrepExpList = MongoDBDAO.find(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, DBQuery.is("typeCode", "sample-prep-exp")).toList();

		for (int i = 0; i < extractionSamplePrepExpList.size(); i++) {
			for (int j = 0; j < extractionSamplePrepExpList.get(i).atomicTransfertMethods.size(); j++) {
				for (int k = 0; k < extractionSamplePrepExpList.get(i).atomicTransfertMethods.get(j).inputContainerUseds.size(); k++) {
					Container container = MongoDBDAO.findByCode(InstanceConstants.CONTAINER_COLL_NAME, Container.class, extractionSamplePrepExpList.get(i).atomicTransfertMethods.get(j).inputContainerUseds.get(k).code);

					if (container.properties.get("biologicalMaterialQuantity") != null) {
						String propValue = String.valueOf(container.properties.get("biologicalMaterialQuantity").value);
						// Je supprime les espaces, des fois c'est '1 mg' ou '1mg', on uniformise.
						propValue = propValue.replaceAll(" ", "");

						if (propValue.endsWith("mg")) {
							String value = propValue.split("mg")[0];

							try {
								Double valueNumber = Double.valueOf(value);
								Double valueEnNG = valueNumber * 1000000;

								container.properties.get("biologicalMaterialQuantity").value = valueEnNG;
								((PropertySingleValue) container.properties.get("biologicalMaterialQuantity")).unit = "ng";

								((List<String>) cv.getObject("Recap")).add("SamplePrep, " + extractionSamplePrepExpList.get(i).code + ", " + extractionSamplePrepExpList.get(i).atomicTransfertMethods.get(j).inputContainerUseds.get(k).code + ", " + propValue + ", mg -> ng");

								// MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME, container);
							} catch (NumberFormatException e) {
								((List<String>) cv.getObject("RecapErreur")).add("SamplePrep, " + extractionSamplePrepExpList.get(i).code + ", " + extractionSamplePrepExpList.get(i).atomicTransfertMethods.get(j).inputContainerUseds.get(k).code + ", " + propValue + ", valeur impossible à parser");
							}
						} else {
							((List<String>) cv.getObject("RecapErreur")).add("SamplePrep, " + extractionSamplePrepExpList.get(i).code + ", " + extractionSamplePrepExpList.get(i).atomicTransfertMethods.get(j).inputContainerUseds.get(k).code + ", " + propValue + ", unité non trouvée");
						}
					} else {
						((List<String>) cv.getObject("RecapErreur")).add("SamplePrep, " + extractionSamplePrepExpList.get(i).code + ", " + extractionSamplePrepExpList.get(i).atomicTransfertMethods.get(j).inputContainerUseds.get(k).code + ", null, " + "propriété non trouvée");
					}
				}
			}
		}

		Logger.error("Fin de la récupération dex expériences de sample prep");
	}

	private void createExcelFileRecap() {
		Workbook wb = new HSSFWorkbook();
		CreationHelper createHelper = wb.getCreationHelper();

		for (String key : this.cv.getContextObjects().keySet()) {
			Sheet sheet = wb.createSheet(key);
			List<String> recaps = (List<String>) cv.getObject(key);
			int nbLine = 0;

			for (String recap : recaps) {
				Row row = sheet.createRow(nbLine);
				String[] tabRecap = recap.split(",");

				for (int i=0;i<tabRecap.length;i++) {
					row.createCell(i).setCellValue(createHelper.createRichTextString(tabRecap[i]));
				}

				nbLine++;
			}
		}

		try (OutputStream fileOut = new FileOutputStream("Récap-4481-" + new Date().getTime() + ".xls")) {
			wb.write(fileOut);
		} catch(Exception e) {
			Logger.debug(e.getMessage());
		}
	}
}