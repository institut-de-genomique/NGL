package fr.cea.ig.ngl.dao.api.factory.run;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;

import fr.cea.ig.ngl.dao.api.factory.StateFactory;
import fr.cea.ig.ngl.utils.TestUtils;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.run.instance.InstrumentUsed;
import models.laboratory.run.instance.Run;
import models.laboratory.run.instance.Treatment;
import play.Logger;

/**
 * Factory pour l'entité "Run" de type "Nanopore".
 * 
 * @author jcharpen - Jordi Charpentier - jcharpen@genoscope.cns.fr
 */
public final class RunNanoporeFactory {
	
	/**
	 * Méthode permettant de générer un objet "Run" aléatoire de type Nanopore.
	 * 
	 * @return Un objet "Run" aléatoire de type Nanopore.
	 */
	public static Run getRandomRunNanopore() {
		Run run = new Run();
		run.state = StateFactory.getRandomState();
		run.containerSupportCode= "FAA54955_A";
   		
		InstrumentUsed instUsed = new InstrumentUsed();
		instUsed.typeCode = "minION";
		instUsed.code = "MN02670";
		
   		run.instrumentUsed = instUsed;
    	
   		try {
   			run.code = (TestUtils.SDF2.format(TestUtils.SDF.parse("21/12/2012")))+"_"+run.instrumentUsed.code+"_"+run.containerSupportCode;
   		} catch (ParseException e) {
   			Logger.debug("Erreur de date. On met une date par défaut.");
   			run.code = new Date()+"_"+run.instrumentUsed.code+"_"+run.containerSupportCode;
   		}
   		
		Treatment treatment=new Treatment();
		treatment.code="minknowBasecalling";
		treatment.typeCode="minknow-basecalling";
		treatment.categoryCode="sequencing";
		treatment.results = new HashMap<>();
		treatment.results.put("default",new HashMap<String, PropertyValue>(0));
		
		run.treatments = new HashMap<>();
		run.treatments.put("minknowBasecalling",treatment);
		run.properties = new HashMap<>();
		run.properties.put("flowcellChemistry",new PropertySingleValue("R7.3"));
		
		run.typeCode="RMINION";	
		run.categoryCode="nanopore";
		
		return run;
	}
}
