package sra.scripts.off;


import javax.inject.Inject;

import fr.cea.ig.lfw.controllers.scripts.buffered.Script;
import fr.cea.ig.ngl.NGLApplication; // pour affichage du contextValidationError
import fr.cea.ig.ngl.dao.api.sra.StudyAPI;
import models.laboratory.common.instance.State;
import play.libs.Json; // pour affichage du contextValidationError
import services.SraEbiAPI;
import services.XmlToSra;
import validation.ContextValidation;
import models.sra.submit.sra.instance.Study;
import models.sra.submit.util.VariableSRA;

/*
 * Script à lancer pour importer dans NGL-SUB un ensemble de studies soumis à l'EBI hors 
 * procedure NGL-SUB. (cas d'une soumission de 454 par exemple)
 * {@code http://localhost:9000/sra/scripts/run/sra.scripts.ImportEbiStudies?AC=studyAC}
 * <br>
 * Si parametre absent dans url => declenchement d'une erreur.
 * 
 * @author sgas
 *
 */
public class ImportEbiStudies extends Script<ImportEbiStudies.Args> {
	private static final play.Logger.ALogger logger = play.Logger.of(ImportEbiStudies.class);
	private final StudyAPI       studyAPI;

	private final SraEbiAPI         ebiAPI;
	private final NGLApplication app;
	@Inject
	public ImportEbiStudies(StudyAPI           studyAPI,
					  		SraEbiAPI             ebiAPI,
					  		NGLApplication     app) {
		this.studyAPI      = studyAPI;
		this.ebiAPI        = ebiAPI;
		this.app           = app;
	}
	
	public static class Args {
		public String AC; // Numeros d'accession du study à recuperer à l'EBI.
	}

	@Override
	public void execute(Args args) throws Exception {
		printfln ("Argument AC = '%s'", args.AC);
		String user = "william";
		String adminComment = "Reprise historique";
		
		String xmlStudies = ebiAPI.ebiXml(args.AC, "studies");
		
		//List<Study> listStudies = RepriseHistorique.xmlToStudy(xmlStudies);
		XmlToSra repriseHistorique = new XmlToSra();
		Iterable<Study> listStudies = repriseHistorique.forStudies(xmlStudies, null);
		
		// Verifier la validité des studies
		ContextValidation contextValidation = ContextValidation.createCreationContext(user);
		for (Study study: listStudies) {
			study.state = new State("F-SUB", user);
			study.traceInformation.setTraceInformation(user);
			study.adminComment = adminComment;	
			study.validate(contextValidation);
			println("displayErrors pour validationStudy:" + study.code);
		}
		if (contextValidation.hasErrors()) {
			contextValidation.displayErrors(logger);
			println(Json.prettyPrint(app.errorsAsJson(contextValidation.getErrors())));
		} else {
			// Sauver tous les study s'ils n'existent pas dans la base et si study Genoscope
			for (Study study : listStudies) {
				study.adminComment = adminComment;
				if(! VariableSRA.centerName.equals(study.centerName)) {
					throw new RuntimeException("Tentative de sauvegarde d'un study collaborateur ? study.centerName=");
				}
				// Cas normalement deja detecté par validate avec un context de creation:
				if (studyAPI.dao_checkObjectExist("code", study.code)) {
					throw new RuntimeException("Tentativee de sauvegarde dans la base d'un study deja present dans NGL-SUB. studyCode="+ study.code + " et studyAC="+study.accession);
				}
				if (studyAPI.dao_checkObjectExist("accession", study.accession)) {
					throw new RuntimeException("Tentativee de sauvegarde dans la base d'un study deja present dans NGL-SUB. studyCode="+ study.code + " et studyAC="+study.accession);
				}
				printfln("sauvegarde dans la base du study avec AC = %s et code =%s et studyCenterName=%s et studyProjectName=%s", 
						study.accession, study.code, study.centerName, study.centerProjectName);
				studyAPI.dao_saveObject(study);
			}
		}	
	}
}
	
