package controllers.instruments.io.cns.tecanevo100andbioradqpcrsystem;

import static services.io.ExcelHelper.getNumericValue;
import static services.io.ExcelHelper.getStringValue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import controllers.instruments.io.utils.AbstractInput;
import models.laboratory.common.instance.property.PropertyFileValue;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.experiment.instance.InputContainerUsed;
import validation.ContextValidation;
import validation.utils.ValidationHelper;

public class Input extends AbstractInput {
    
    private static final int TABLE_FIRST_ROW_NUMBER = 31;
    private static final int ROW_STEP = 4;
    
    private Sheet getSheetFrom(PropertyFileValue pfv) throws InvalidFormatException, IOException {
        InputStream is = new ByteArrayInputStream(pfv.byteValue());
        Workbook wb = WorkbookFactory.create(is);
        return wb.getSheetAt(0);
    }
    
    private boolean isValidRowData(int rowNum, ContextValidation contextValidation, String sampleBarcode, Double concentration1, Double concentration2) {
        return ValidationHelper.validateNotEmpty(contextValidation, sampleBarcode, "nom échantillon : ligne = " + rowNum)
                && ValidationHelper.validateNotEmpty(contextValidation, concentration1, "Moy. concentration (nM) : ligne = " + rowNum)
                && ValidationHelper.validateNotEmpty(contextValidation, concentration2, "Moy. concentration (ng/µl) : ligne = " + rowNum);
    }
    
    private void readRow(Row row, Map<String,Data> results, ContextValidation contextValidation) {
        String test = getStringValue(row.getCell(0));
        if (StringUtils.isBlank(test)) return;
        int rowNum = row.getRowNum();
        String sampleBarcode = getStringValue(row.getCell(1));
        Double concentration1 = getNumericValue(row.getCell(10));
        Double concentration2 = getNumericValue(row.getCell(12));

        if (!isValidRowData(rowNum, contextValidation, sampleBarcode, concentration1, concentration2)) return;
        
        String cleanedSampleBarCode = sampleBarcode.replaceAll("_\\d$","");
        if (!results.containsKey(cleanedSampleBarCode)) {
            results.put(cleanedSampleBarCode, new Data(concentration1, concentration2));
        } else {
            contextValidation.addError("Erreurs fichier", "Résultats en double pour "+cleanedSampleBarCode+" : ligne = "+rowNum);
        }
    }
    
    private Map<String,Data> readAllRows(PropertyFileValue pfv, ContextValidation contextValidation) throws InvalidFormatException, IOException {
        Map<String,Data> results = new HashMap<>();
        Sheet sheet = getSheetFrom(pfv);
        int lastRowNum = sheet.getLastRowNum();
        for(int i = TABLE_FIRST_ROW_NUMBER; i <= lastRowNum; i = i + ROW_STEP) {
            Row row = sheet.getRow(i);
            readRow(row, results, contextValidation);
        }
        return results;
    }
    
    private Stream<InputContainerUsed> icuStreamFrom(Experiment experiment) {
        return experiment.atomicTransfertMethods
                .stream()
                .map(atm -> atm.inputContainerUseds.get(0));
    }
    
    private void validateFileContainsICU(InputContainerUsed icu, Set<String> codesInFile, ContextValidation contextValidation) {
        if(!codesInFile.contains(icu.code)){
            contextValidation.addError("Erreurs fichier", "io.error.resultat.notexist","La Moy. concentration pour "+icu.code);
        }
    }
    
    private void updateValues(InputContainerUsed icu, Data icuData) {
        PropertySingleValue concentration1 = getOrCreatePSV(icu, "concentration1");
        concentration1.value = icuData.concentration1;
        concentration1.unit = "nM";
        
        PropertySingleValue concentration2 = getOrCreatePSV(icu, "concentration2");
        concentration2.value = icuData.concentration2;
        concentration2.unit = "ng/µl";
    }

	@Override
	public Experiment importFile(Experiment experiment,PropertyFileValue pfv, ContextValidation contextValidation) throws Exception {	
	    Map<String,Data> results = readAllRows(pfv, contextValidation);
	    if (!contextValidation.hasErrors()) icuStreamFrom(experiment).forEach((InputContainerUsed icu) -> validateFileContainsICU(icu, results.keySet(), contextValidation));
        if (!contextValidation.hasErrors()) icuStreamFrom(experiment).forEach((InputContainerUsed icu) -> updateValues(icu, results.get(icu.code)));
		return experiment;
	}

}
