package sra.scripts.off;


import java.util.List;
import javax.inject.Inject;

import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import fr.cea.ig.lfw.controllers.scripts.buffered.Script;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.dao.api.sra.ExperimentAPI;
//import models.sra.submit.common.instance.Sample;
import models.sra.submit.sra.instance.Run;
import play.libs.Json;
import services.SraEbiAPI;
import services.XmlToSra;
import validation.ContextValidation;

/*
 * Script à lancer pour importer dans NGL-SUB un run soumis à l'EBI hors 
 * procedure NGL-SUB. (cas d'une soumission de 454 par exemple)
 * {@code http://localhost:9000/sra/scripts/run/sra.scripts.ImportEbiRuns?AC=runAC}
 * <br>
 * Si parametre absent dans url => declenchement d'une erreur.
 * 
 * @author sgas
 *
 */
public class ImportEbiRuns extends Script<ImportEbiRuns.Args> {
	private static final play.Logger.ALogger logger = play.Logger.of(ImportEbiRuns.class);
	private final ExperimentAPI  experimentAPI;
	private final SraEbiAPI         ebiAPI;
	private final NGLApplication app;
	@Inject
	public ImportEbiRuns(ExperimentAPI      experimentAPI,
					  			SraEbiAPI             ebiAPI,
					  			NGLApplication     app) {
		this.experimentAPI = experimentAPI;
		this.ebiAPI        = ebiAPI;
		this.app           = app;
	}
	
	public static class Args {
		public String AC; // Numeros d'accession du sample à recuperer à l'EBI.
	}

	@Override
	public void execute(Args args) throws Exception {
		printfln ("Argument AC = '%s'", args.AC);
		String adminComment = "Reprise historique";
		String user = "william";

		String xmlRuns = ebiAPI.ebiXml(args.AC, "runs");
		
		XmlToSra repriseHistorique = new XmlToSra();
		List<Run> listRuns = repriseHistorique.forRuns(xmlRuns);

		// Verifier la validité des experiments
		ContextValidation contextValidation = ContextValidation.createCreationContext(user); // on veut updater l'experiment sans run pour son run
		for (Run run: listRuns) {
			run.validate(contextValidation); // On teste la validation des runs en mode creation.
		}
		
		if (contextValidation.hasErrors()) {
			contextValidation.displayErrors(logger);
			println(Json.prettyPrint(app.errorsAsJson(contextValidation.getErrors())));
		} else {
			// Sauver tous les runs s'ils n'existent pas dans la base
			for (Run run : listRuns) {
				run.adminComment = adminComment;
				if (experimentAPI.dao_checkObjectExist("run.code", run.code)) {
					throw new RuntimeException("Tentativee de sauvegarde dans la base d'un run deja present dans NGL-SUB. runCode="+ run.code + " et runAC="+run.accession);
				}
				if (experimentAPI.dao_checkObjectExist("run.accession", run.accession)) {
					throw new RuntimeException("Tentativee de sauvegarde dans la base d'un run deja present dans NGL-SUB. runCode="+ run.code + " et runAC="+run.accession);
				}
				if (! experimentAPI.dao_checkObjectExist("code", run.expCode)) {
					throw new RuntimeException("Tentativee de sauvegarde dans la base du run " + run.code + " sans que l'experiment " + run.expCode + " soit dans la base");
				}
				printfln("sauvegarde dans la base de l'experiment exp.code=%s avec run.AC = %s et run.code =%s", run.expCode, run.accession , run.code);
				experimentAPI.dao_update(DBQuery.is("code", run.expCode), DBUpdate.set("run", run));
			}
		}	
	}
}
	
