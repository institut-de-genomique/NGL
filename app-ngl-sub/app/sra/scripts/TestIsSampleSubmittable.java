package sra.scripts;

import javax.inject.Inject;

import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptNoArgs;
import services.SraEbiAPI;

/*
 * Script à utiliser pour recharger les md5 d'une soumission a partir de NGL.
 * {@code http://localhost:9000/sra/scripts/run/sra.scripts.TestIsSampleSubmittable}
 * <br>
 * Si parametre absent dans url => declenchement d'une erreur.
 *  
 * @author sgas
 *
 */
public class TestIsSampleSubmittable extends ScriptNoArgs {
	private final SraEbiAPI ebiAPI;
	private static final play.Logger.ALogger logger = play.Logger.of(TestIsSampleSubmittable.class);
	
	
	@Inject
	public TestIsSampleSubmittable(SraEbiAPI ebiAPI
				) {
		this.ebiAPI = ebiAPI;

		
	}


	@Override
	public void execute() throws Exception {
		
		Boolean infosTaxon = ebiAPI.submittable(344338);
		printfln("taxonid 344338 isSubmittable = " + infosTaxon);
		
		infosTaxon = ebiAPI.submittable(1385);
		printfln("taxonid 1385 isSubmittable = " + infosTaxon);
		
		infosTaxon = ebiAPI.submittable(999999999);
		printfln("taxonid 999999999 isSubmittable = " + infosTaxon);

	}
		
		
}