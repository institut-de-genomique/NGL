package fr.cea.ig.ngl.dao.api.factory.run;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;

import fr.cea.ig.ngl.dao.api.factory.SampleFactory;
import fr.cea.ig.ngl.dao.api.factory.StateFactory;
import fr.cea.ig.ngl.utils.TestUtils;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.run.instance.InstrumentUsed;
import models.laboratory.run.instance.Run;
import models.laboratory.run.instance.Treatment;
import play.Logger;

/**
 * Factory pour l'entité "Run".
 * 
 * @author jcharpen - Jordi Charpentier - jcharpen@genoscope.cns.fr
 */
public final class RunFactory {

	/**
	 * Méthode permettant de générer un code run aléatoire.
	 * 
	 * @return Une chaîne de caractères aléatoire respectant la règle d'un code run.
	 */
	public static String getRandomRunCode() {
		Random r = new Random();
		
		String randomDate = r.nextInt(99) + "0" + r.nextInt(9) + "0" + r.nextInt(9);
		String randomInstrument = "BISMUTH";
		String containerSupportCode = "637UMAAXX";

		return randomDate + "_" + randomInstrument + "_" + containerSupportCode;
	}
	
	/**
	 * Méthode permettant de générer un objet "Run" aléatoire.
	 * 
	 * @return Un objet "Run" aléatoire.
	 */
	public static Run getRandomRun() {
		Run run = new Run();
		run.state = StateFactory.getRandomState();
		run.containerSupportCode= "FAA54955_A";
		
		TraceInformation ti = new TraceInformation();
		run.traceInformation = ti;
   		
		InstrumentUsed instUsed = new InstrumentUsed();
		instUsed.typeCode = "minION";
		instUsed.code = "MN02670";
		
   		run.instrumentUsed = instUsed;
    	
   		try {
   			String code = (TestUtils.SDF2.format(TestUtils.SDF.parse("21/12/2012")))+"_";
   			code += run.instrumentUsed.code+"_";
   			code += run.containerSupportCode;
   			run.code = code;
   		} catch (ParseException e) {
   			Logger.debug("Erreur de date. On met une date par défaut.");
   			run.code = new Date() + "_" + run.instrumentUsed.code + "_" + run.containerSupportCode;
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

		run.sampleCodes = SampleFactory.getRandomSampleCodesHashset();
		
		run.typeCode="RMINION";	
		run.categoryCode="nanopore";
		
		return run;
	}
}
