package controllers.instruments.io.cns.labchipgx;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import au.com.bytecode.opencsv.CSVReader;
import controllers.instruments.io.utils.AbstractInput;
import models.laboratory.common.instance.property.PropertyFileValue;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.experiment.instance.InputContainerUsed;
import validation.ContextValidation;

public class Input extends AbstractInput {
	
	@Override
	public Experiment importFile(Experiment experiment,PropertyFileValue pfv, ContextValidation contextValidation) throws Exception {	
		String plateCodeInExp = experiment.inputContainerSupportCodes.iterator().next();
		Map<String, String[]> allMap = new HashMap<>();
//		InputStream is = new ByteArrayInputStream(pfv.value);
		try (InputStream is = new ByteArrayInputStream(pfv.byteValue());
			 CSVReader reader = new CSVReader(new InputStreamReader(is))) {
			List<String[]> all = reader.readAll();
			all.forEach(array -> {

				String pos = array[0];
				if (pos.matches("[A-H]{1}0[1-9]{1}")) {
					pos = pos.replace("0", "");
				}

				allMap.put(plateCodeInExp+"_"+pos, array);
			});
		}
		experiment.atomicTransfertMethods.forEach(atm -> {
			InputContainerUsed icu = atm.inputContainerUseds.get(0);
			if (allMap.containsKey(icu.code)) {
				String[] data = allMap.get(icu.code);
				PropertySingleValue measuredSize = getOrCreatePSV(icu, "measuredSize");
				measuredSize.value = Math.round(Double.valueOf(data[1]));
				measuredSize.unit="pb";
			}			
		});
				
		return experiment;
	}
	
}
