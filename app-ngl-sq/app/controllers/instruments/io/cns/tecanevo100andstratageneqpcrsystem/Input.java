package controllers.instruments.io.cns.tecanevo100andstratageneqpcrsystem;

import static services.io.ExcelHelper.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import controllers.instruments.io.utils.AbstractInput;
import models.laboratory.common.instance.property.PropertyFileValue;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.experiment.instance.Experiment;
import validation.ContextValidation;
import validation.utils.ValidationHelper;

public class Input extends AbstractInput {

	@Override
	public Experiment importFile(Experiment experiment,PropertyFileValue pfv, ContextValidation contextValidation) throws Exception {		
//		InputStream is = new ByteArrayInputStream(pfv.value);
		InputStream is = new ByteArrayInputStream(pfv.byteValue());
		
		Workbook wb = WorkbookFactory.create(is);
		Sheet sheet = wb.getSheetAt(0);
		Map<String,Data> results = new HashMap<>(0);
		for (int i = 31; i <= sheet.getLastRowNum(); i=i+4) {
			String test = getStringValue(sheet.getRow(i).getCell(0));
			if (StringUtils.isNotBlank(test)) {
				String sampleBarcode = getStringValue(sheet.getRow(i).getCell(1));
				Double concentration1 = getNumericValue(sheet.getRow(i).getCell(10));
				Double concentration2 = getNumericValue(sheet.getRow(i).getCell(12));

				if (ValidationHelper.validateNotEmpty(contextValidation, sampleBarcode, "nom échantillon : ligne = "+i)
						&& ValidationHelper.validateNotEmpty(contextValidation, concentration1, "Moy. concentration (nM) : ligne = "+i)
						&& ValidationHelper.validateNotEmpty(contextValidation, concentration2, "Moy. concentration (ng/µl) : ligne = "+i)){
					String key = sampleBarcode.replaceAll("_\\d$","");
					if (!results.containsKey(key)) {
						results.put(key, new Data(concentration1, concentration2));
					} else {
						contextValidation.addError("Erreurs fichier", "Résultats en double pour "+key+" : ligne = "+i);
					}
				}
			}
		}
		//validation
		if (!contextValidation.hasErrors()) {
			experiment.atomicTransfertMethods
				.stream()
				.map(atm -> atm.inputContainerUseds.get(0))
				.forEach(icu -> {
					if(!results.containsKey(icu.code)){
						contextValidation.addError("Erreurs fichier", "io.error.resultat.notexist","La Moy. concentration pour "+icu.code);
					}
				});
		}
		//update.
		if(!contextValidation.hasErrors()){
			experiment.atomicTransfertMethods
				.stream()
				.map(atm -> atm.inputContainerUseds.get(0))
				.forEach(icu -> {
					PropertySingleValue concentration1 = getOrCreatePSV(icu, "concentration1");
					concentration1.value = results.get(icu.code).concentration1;
					concentration1.unit = "nM";
					
					PropertySingleValue concentration2 = getOrCreatePSV(icu, "concentration2");
					concentration2.value = results.get(icu.code).concentration2;
					concentration2.unit = "ng/µl";
				});
		}
		
		return experiment;
	}

	
	
	

}
