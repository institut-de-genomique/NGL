package fr.cea.ig.ngl.dao.api.factory.run;

import java.text.ParseException;

import fr.cea.ig.ngl.dao.api.factory.StateFactory;
import fr.cea.ig.ngl.utils.TestUtils;
import models.laboratory.run.instance.InstrumentUsed;
import models.laboratory.run.instance.Run;

/**
 * Factory pour l'entité "Run" de type "Bionano".
 * 
 * @author jcharpen - Jordi Charpentier - jcharpen@genoscope.cns.fr
 */
public final class RunBionanoFactory {
	
	/**
	 * Méthode permettant de générer un objet "Run" aléatoire de type Bionano.
	 * 
	 * @return Un objet "Run" aléatoire de type Bionano.
	 */
	public static Run getRandomRunBionano() throws ParseException {
		Run run = new Run();
		run.state = StateFactory.getRandomState();
		run.containerSupportCode= "XPJY-2U6L-PQLG-RNWU";
   		
		InstrumentUsed instUsed = new InstrumentUsed();
		instUsed.typeCode = "minION";
		instUsed.code = "MN02670";
		
   		run.instrumentUsed = instUsed;
    	
    	run.code = (TestUtils.SDF2.format(TestUtils.SDF.parse("21/12/2019")))+"_"+run.instrumentUsed.code+"_"+run.containerSupportCode;
		
		run.typeCode="RSAPHYR";	
		run.categoryCode="bionano";
		
		return run;
	}
}
