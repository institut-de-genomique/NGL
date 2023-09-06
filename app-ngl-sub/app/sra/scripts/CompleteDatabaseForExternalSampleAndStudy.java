package sra.scripts;


//import java.util.Iterator;
import javax.inject.Inject;
import fr.cea.ig.lfw.controllers.scripts.buffered.Script;
import fr.cea.ig.ngl.dao.api.sra.ExperimentAPI;
import services.DatabaseConsistencyTools;

/*
 * Script à utiliser pour completer la base pour les ExternalStudy et ExternalSample et experiments si soumission 
 * réalisée avec SAM et PRJ pas disponibles sur le browser de l'EBI au moment de la soumission.
 * {@code http://localhost:9000/sra/scripts/run/sra.scripts.CompleteDatabaseForExternalSampleAndStudy?submissionCode=monCodeSubmission}
 * <br>
 * Si parametre absent dans url => declenchement d'une erreur.
 *  
 * @author sgas
 *
 */
public class CompleteDatabaseForExternalSampleAndStudy extends  Script<CompleteDatabaseForExternalSampleAndStudy.MyParam> {

	private static final play.Logger.ALogger logger = play.Logger.of(CompleteDatabaseForExternalSampleAndStudy.class);
    private final DatabaseConsistencyTools databaseConsistencyTools;

	@Inject
	public CompleteDatabaseForExternalSampleAndStudy(ExperimentAPI      experimentAPI,
				DatabaseConsistencyTools databaseConsistencyTools
				) {

	    this.databaseConsistencyTools = databaseConsistencyTools;

	}

	
	// ma structure de controle et stockage des arguments de l'url
	public static class MyParam {
		public String submissionCode;
	}


	@Override
	public void execute(MyParam args) throws Exception {
		//String submissionCode = "GSC_CSY_CUE_71BD26JKT";
		String submissionCode = args.submissionCode;
		databaseConsistencyTools.completeDatabaseForExternalSampleAndStudy(submissionCode);
	}
	

}