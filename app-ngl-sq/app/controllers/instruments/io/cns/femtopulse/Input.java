package controllers.instruments.io.cns.femtopulse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import au.com.bytecode.opencsv.CSVReader;
import controllers.instruments.io.utils.AbstractInput;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.property.PropertyFileValue;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.experiment.instance.InputContainerUsed;
import play.Logger;
import validation.ContextValidation;

public class Input extends AbstractInput {

    @Override
    public Experiment importFile(Experiment experiment, PropertyFileValue pfv, ContextValidation contextValidation)
            throws Exception {
        try(CSVReader reader = getReaderFrom(pfv)) {
            Map<String, String[]> records = readRecordsFrom(reader);
            updateValuesFor(experiment, records);
        }
        return experiment;
    }

    private CSVReader getReaderFrom(PropertyFileValue pfv) {
        InputStream is = new ByteArrayInputStream(pfv.byteValue());
        InputStreamReader isr = new InputStreamReader(is);
        return new CSVReader(isr, ';');
    }
    
    private Map<String, String[]> readRecordsFrom(CSVReader reader) throws IOException {
        Map<String, String[]> records = new HashMap<>();
        String[] nextLine;
        reader.readNext(); //remove header
        while((nextLine = reader.readNext()) != null) {
            String position = getPositionFrom(nextLine);
            if(records.containsKey(position)) throw new IllegalStateException("Attention doublon de positions FemtoPulse : " + position);
            records.put(position, nextLine);
        }
        return records;
    }
    
    private String getPositionFrom(String[] array) {
        String pos = array[0].trim();
        return pos.matches("[A-H]{1}0[1-9]{1}") ? pos.replace("0", "") : pos;
    }

    private void updateValuesFor(Experiment experiment, Map<String, String[]> records) {
        experiment.atomicTransfertMethods.forEach(atm -> {
            atm.inputContainerUseds
            .forEach((InputContainerUsed icu)-> {
                Map<String, PropertyValue> properties = icu.instrumentProperties;
                String icuPos = properties.get("femtopulseLine").value.toString() + properties.get("femtopulseColumn").value.toString();
                if(records.containsKey(icuPos)) {
                    updateInputContainerUsedValues(icu, records.get(icuPos));
                }
            });        
        });
    }
    
    private void updateInputContainerUsedValues(InputContainerUsed icu, String[] record) {
        String sizeRecord = record[6];
        Long sizeValue =  Math.round(Double.valueOf(sizeRecord));
        PropertySingleValue measuredSize = getOrCreatePSV(icu, "measuredSize");
        measuredSize.value = sizeValue;
        measuredSize.unit="pb";
    }

}
