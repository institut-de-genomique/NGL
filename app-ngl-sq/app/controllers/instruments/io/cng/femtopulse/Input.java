// copie de celui du CNS
package controllers.instruments.io.cng.femtopulse;

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

/*
 * Le fichier de données à importer comporte un header de 1 ligne
 * Les données sont séparées par ";"
 * chaque ligne commence en colonne 0 par la position sur plaque Femtopuse A1...H12 
 * Les positions <X>12 sont réservées au Ladder et ne sont pas importées car ne correspondent 
 * a aucun des input containers en entrée d'expérience
 * La valeur à importer est en colonne 6 "Average Size"
 */

public class Input extends AbstractInput {

    @Override
    public Experiment importFile(Experiment experiment, PropertyFileValue pfv, ContextValidation contextValidation)
            throws Exception {
        try(CSVReader reader = getReaderFrom(pfv)) {
            Map<String, String[]> records = readRecordsFrom(reader);
            Logger.info("records OK");
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
        /* FDS: les positions femtopulse ne sont pas en <X>01, <X>02... ceci est inutile... */
        return pos.matches("[A-H]{1}0[1-9]{1}") ? pos.replace("0", "") : pos;
    }

    private void updateValuesFor(Experiment experiment, Map<String, String[]> records) {
        experiment.atomicTransfertMethods.forEach(atm -> {
            atm.inputContainerUseds
            .forEach((InputContainerUsed icu)-> {
                Map<String, PropertyValue> properties = icu.instrumentProperties;
                String icuPos = properties.get("femtopulseLine").value.toString() + properties.get("femtopulseColumn").value.toString();
                Logger.info(icuPos);
                if(records.containsKey(icuPos)) {
                    updateInputContainerUsedValues(icu, records.get(icuPos));
                    Logger.info("updating ICU"+ icu + ">>"+ records.get(icuPos) );
                }
            });        
        });
    }
    
    /* La valeur à importer "Average Size" est en colonne 6  */
    private void updateInputContainerUsedValues(InputContainerUsed icu, String[] record) {
        String sizeRecord = record[6];
        Logger.info("load :"+sizeRecord);
        Long sizeValue =  Math.round(Double.valueOf(sizeRecord));
        PropertySingleValue measuredSize = getOrCreatePSV(icu, "measuredSize");
        measuredSize.value = sizeValue;
        measuredSize.unit="pb";
    }

}
