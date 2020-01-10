package controllers.migration.cns;

import java.util.*;
import java.util.stream.Collectors;

import org.mongojack.DBQuery;


import models.laboratory.common.description.Level;
import models.laboratory.common.description.PropertyDefinition;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.sample.description.ImportType;
import models.laboratory.sample.instance.Sample;
import models.utils.InstanceConstants;
import play.Logger;
import play.Logger.ALogger;
import play.mvc.Result;

import validation.ContextValidation;
import controllers.CommonController;

import fr.cea.ig.MongoDBDAO;

public class MigrationSampleTaraStation extends CommonController {
	protected static ALogger logger = Logger.of(MigrationSampleTaraStation.class.getName());
	public static Result migration() {
	
		updateSample();
		
		
		return ok("Migration Sample TaraStation finish");
	}

	private static void updateSample() {
		
		//Logger.debug("Sample "+samples.size());
		
		
		
		ImportType importType = ImportType.find.findByCode("reception-tara-pacific");
		
		PropertyDefinition taraStationPD = importType.propertiesDefinitions.stream().filter(pd -> "taraStation".equals(pd.code)).findFirst().get(); //PropertyDefinition.find.findByCode("taraStation");
		Map<String, String> taraStationMapping = taraStationPD.possibleValues.stream().collect(Collectors.toMap(value -> value.code, value -> value.name));
		
		MongoDBDAO.find(InstanceConstants.SAMPLE_COLL_NAME, Sample.class,DBQuery.exists("properties.taraProtocol")).getCursor()
				.forEach(sample -> {
		
				//Logger.debug("Treat Sample "+sample.code);
				PropertyValue taraStationCodePV = sample.properties.get("taraStation");
				
				if(null == taraStationCodePV){
					Logger.error("not found taraStation for  sample "+sample.code);
				}else{
					String taraStationName = taraStationMapping.get(taraStationCodePV.value.toString());				
					
					if(taraStationName != null && taraStationName.length() == 9){
						PropertySingleValue island = new PropertySingleValue(taraStationName.substring(0, 3));
						PropertySingleValue site = new PropertySingleValue(taraStationName.substring(3, 6));
						PropertySingleValue colony = new PropertySingleValue(taraStationName.substring(6, 9));
						sample.properties.put("taraIsland", island);
						sample.properties.put("taraSite", site);
						sample.properties.put("taraColony", colony);					
					}else if(taraStationName != null && taraStationName.length() == 6 && !taraStationName.startsWith("OA")){
						PropertySingleValue island = new PropertySingleValue(taraStationName.substring(0, 3));
						PropertySingleValue site = new PropertySingleValue(taraStationName.substring(3, 6));
						sample.properties.put("taraIsland", island);
						sample.properties.put("taraSite", site);
									
					}else if(taraStationName != null && taraStationName.length() == 6 && taraStationName.startsWith("OA")){
						PropertySingleValue island = new PropertySingleValue(taraStationName);
						sample.properties.put("taraIsland", island);					
					}else if(taraStationName == null){
						Logger.error("Not found taraStationName for"+taraStationCodePV.value.toString());
					}
					sample.traceInformation.setTraceInformation("ngl-data");
					ContextValidation contextValidation = new ContextValidation("ngl-data");
					sample.validate(contextValidation);
					
					if(!contextValidation.hasErrors()){
						MongoDBDAO.save(InstanceConstants.SAMPLE_COLL_NAME, sample);
					}else{
						Logger.error("Error for "+sample.code);
						contextValidation.displayErrors(logger);
					}
					
				}
			
		});
		Logger.debug("Migration Sample TaraStation finish");
	}

	
	
}


