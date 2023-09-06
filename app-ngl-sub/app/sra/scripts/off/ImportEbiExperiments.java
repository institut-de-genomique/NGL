package sra.scripts.off;


import java.util.List;
import javax.inject.Inject;

import fr.cea.ig.lfw.controllers.scripts.buffered.Script;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.dao.api.sra.ExperimentAPI;
//import models.sra.submit.common.instance.Sample;
import models.sra.submit.sra.instance.Experiment;
import play.libs.Json;
import services.SraEbiAPI;
import services.XmlToSra;
import validation.ContextValidation;
import models.laboratory.common.instance.State;

/*
 * Script à lancer pour importer dans NGL-SUB un experiment soumis à l'EBI hors 
 * procedure NGL-SUB. (cas d'une soumission de 454 par exemple)
 * {@code http://localhost:9000/sra/scripts/run/sra.scripts.ImportEbiExperiments?AC=experimentAC}
 * <br>
 * Si parametre absent dans url => declenchement d'une erreur.
 * 
 * @author sgas
 *
 */
public class ImportEbiExperiments extends Script<ImportEbiExperiments.Args> {
	private static final play.Logger.ALogger logger = play.Logger.of(ImportEbiExperiments.class);
	private final ExperimentAPI  experimentAPI;
	private final SraEbiAPI         ebiAPI;
	private final NGLApplication app;
	
	@Inject
	public ImportEbiExperiments(ExperimentAPI      experimentAPI,
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

		String xmlExperiments = ebiAPI.ebiXml(args.AC, "experiments");
		
		XmlToSra repriseHistorique = new XmlToSra();
		List<Experiment> listExperiments = repriseHistorique.forExperiments(xmlExperiments, null);
		
		// Verifier la validité des experiments
		ContextValidation contextValidation = ContextValidation.createCreationContext(user);

		for (Experiment experiment: listExperiments) {
			// update de l'experiment pour champs specifiques qui n'apparaissent pas à l'EBI :
			experiment.state = new State("F-SUB", user);
			experiment.traceInformation.setTraceInformation(user);
			experiment.adminComment = adminComment;	
			experiment.validateInvariantsNoRun(contextValidation);
			println("displayErrors pour validationExperimentSansRun:" + experiment.code);
		}
		
		if (contextValidation.hasErrors()) {
			contextValidation.displayErrors(logger);
			println(Json.prettyPrint(app.errorsAsJson(contextValidation.getErrors())));
		} else {
			// Sauver tous les experiments s'ils n'existent pas dans la base
			for (Experiment experiment : listExperiments) {
				experiment.adminComment = adminComment;
				experiment.state = new State("F-SUB", user);
				experiment.traceInformation.setTraceInformation(user);
				experiment.adminComment = adminComment;
				// Cas normalement deja detecté par validate avec un context de creation:
				if (experimentAPI.dao_checkObjectExist("code", experiment.code)) {
					throw new RuntimeException("Tentativee de sauvegarde dans la base d'un experiment deja present dans NGL-SUB. experimentCode="+ experiment.code + " et experimentAC="+experiment.accession);
				}
				if (experimentAPI.dao_checkObjectExist("accession", experiment.accession)) {
					throw new RuntimeException("Tentativee de sauvegarde dans la base d'un experiment deja present dans NGL-SUB. experimentCode="+ experiment.code + " et experimentAC="+experiment.accession);
				}
				printfln("sauvegarde dans la base de l'experiment avec AC = %s et code =%s", experiment.accession , experiment.code); 
				experimentAPI.dao_saveObject(experiment);
				
			}
		}	
	}
}
	
