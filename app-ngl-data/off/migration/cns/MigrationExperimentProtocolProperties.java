package controllers.migration.cns;

import static services.instance.InstanceFactory.newPSV;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mongojack.DBQuery;


import fr.cea.ig.MongoDBDAO;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.experiment.instance.OneToOneContainer;
import models.laboratory.protocol.instance.Protocol;
import models.utils.InstanceConstants;
import play.Logger;
import play.mvc.Result;

public class MigrationExperimentProtocolProperties extends MigrationExperimentProperties{

	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmm");

	private static Map<String, String> protocolsMap = new HashMap<String, String>();
	/**
	 * Propagation d'une propriété d'experiment au niveau content (containers et readSets)
	 * @param experimentTypeCode
	 * @param keyProperty
	 * @return
	 */
	public static Result migration(String experimentTypeCode, String keyProperty){

		//backupContainerCollection();
		//backupReadSetCollection();
		Logger.debug("  "+experimentTypeCode+" keyProperty "+keyProperty);
		//Get list experiment
		List<Experiment> experiments = getListExperiments(DBQuery.is("typeCode", experimentTypeCode).exists("protocolCode"));
		Logger.debug("size "+experiments.size());

		//Get list experiment with no experiment properties
		for(Experiment exp : experiments){
			//checkATMExperiment(exp);
			//checkInputExperimentProperties(exp, keyProperty);
			//checkOneContentForATM(exp);
		}

		//Get all inputQuantity to change to libraryInputQuantity
		for(Experiment exp : experiments){
		//	Logger.debug("Code experiment "+exp.code+" "+OneToOneContainer.class.getName());

			//Get protocol Name
			PropertyValue propValue = new PropertySingleValue(null);

			if (exp.protocolCode.equals("chromium-10x")){	
				Logger.debug("proto "+ exp.protocolCode);
				propValue.value="Chromium 10x";
			}else if (exp.protocolCode.equals("bq_low cost_ptr_148_3")){	
				Logger.debug("proto "+ exp.protocolCode);
				propValue.value="Bq low cost";
			}else if (exp.protocolCode.equals("bq_neb_next_ultra_ii_ptr_151_1")){	
				Logger.debug("proto "+ exp.protocolCode);
				propValue.value="Bq NEB Next Ultra II";
			}else if (exp.protocolCode.equals("bq_neb_reagent_ptr_143_4")){	
				Logger.debug("proto "+ exp.protocolCode);
				propValue.value="Bq NEB Reagent";
			}else if (exp.protocolCode.equals("bq_pcr_free")){	
				Logger.debug("proto "+ exp.protocolCode);
				propValue.value="Bq PCR free";
			}else if (exp.protocolCode.equals("bq_super_low_cost_ptr_150_1")){	
				Logger.debug("proto "+ exp.protocolCode);
				propValue.value="Bq Super low cost";
			}else {
				Logger.debug("Code proto "+exp.protocolCode);
				Logger.debug("OUPSSS!!!");
			}							
			Logger.debug("Nb ATM "+exp.atomicTransfertMethods.size());
			Logger.debug("propValue "+ propValue.toString());

			exp.atomicTransfertMethods.stream().filter(atm->atm.getClass().getName().equals(OneToOneContainer.class.getName())).forEach(atm->{
				//	Logger.debug("ATM "+atm.getClass()); OneToOne
				atm.outputContainerUseds.stream().forEach(output->{
					//Get property
					Logger.debug("Update property for container "+output.code);

					String sampleCode = output.contents.iterator().next().sampleCode;
					String tag = (String) output.contents.iterator().next().properties.get("tag").getValue();
					Logger.debug("Sample code "+sampleCode+" Tag "+tag);

					updateOutputContainerTreeOfLife(output, sampleCode, tag, keyProperty, propValue, false);

				});

			});
			//Update experiment in database
			MongoDBDAO.update(InstanceConstants.EXPERIMENT_COLL_NAME, exp);

		}
		Logger.debug("Fin update experimentType : "+experimentTypeCode+" prop "+ "keyProperty "+keyProperty);
		return ok();
	}


	private static String getProtocolName(String protocolCode)
	{
		if(protocolsMap.containsKey(protocolCode))
			return protocolsMap.get(protocolCode);
		else{
			Protocol protocol = MongoDBDAO.findByCode(InstanceConstants.PROTOCOL_COLL_NAME, Protocol.class, protocolCode);
			protocolsMap.put(protocolCode, protocol.name);
			return protocol.name;
		}
					
	}
}
