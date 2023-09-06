//package sra.scripts.off;
//
//
//import javax.inject.Inject;
//
//import fr.cea.ig.lfw.controllers.scripts.buffered.Script;
//import fr.cea.ig.ngl.NGLApplication;
//import fr.cea.ig.ngl.dao.api.sra.SampleAPI;
//import models.laboratory.common.instance.State;
//import models.sra.submit.sra.instance.Sample;
//import play.libs.Json;
//import services.SraEbiAPI;
//import services.XmlToSra;
//import services.ncbi.NCBITaxon;
//import services.ncbi.TaxonomyServices;
//import validation.ContextValidation;
//
///*
// * Script à lancer pour importer dans NGL-SUB un sample soumis à l'EBI hors 
// * procedure NGL-SUB. (cas d'une soumission de 454 par exemple)
// * {@code http://localhost:9000/sra/scripts/run/sra.scripts.ImportEbiSamples?AC=sampleAC}
// * <br>
// * Si parametre absent dans url => declenchement d'une erreur.
// * 
// * @author sgas
// *
// */
//public class ImportEbiSamples extends Script<ImportEbiSamples.Args> {
//	private static final play.Logger.ALogger logger = play.Logger.of(ImportEbiSamples.class);
//	
//	private final SampleAPI        sampleAPI;
//	private final SraEbiAPI           ebiAPI;
//	private final NGLApplication   app;
//	private final TaxonomyServices taxonomyServices;
//	
//	@Inject
//	public ImportEbiSamples(SampleAPI          sampleAPI,
//			SraEbiAPI             ebiAPI,
//			TaxonomyServices   taxonomyServices,
//			NGLApplication     app) {
//
//		this.sampleAPI        = sampleAPI;
//		this.ebiAPI           = ebiAPI;
//		this.app              = app;
//		this.taxonomyServices = taxonomyServices;
//
//	}
//
//	public static class Args {
//		public String AC; // Numeros d'accession du sample à recuperer à l'EBI.
//	}
//
//	@Override
//	public void execute(Args args) throws Exception {
//		printfln ("Argument AC = '%s'", args.AC);
//		String adminComment = "Reprise historique";
//		String user = "william";
//
//		String xmlSamples = ebiAPI.ebiXml(args.AC, "samples");
//
//		XmlToSra repriseHistorique = new XmlToSra();
//		Iterable<Sample> listSamples = repriseHistorique.forSamples(xmlSamples, null);
//		// Verifier la validité des samples
//		ContextValidation contextValidation = ContextValidation.createCreationContext(user);
//
//		for (Sample sample: listSamples) {
//			sample.state = new State("F-SUB", user);
//			sample.traceInformation.setTraceInformation(user);
//			sample.adminComment = adminComment;	
//			//NCBITaxon ncbiTaxon = taxonomyServices.getNCBITaxon("" + sample.taxonId).toCompletableFuture().get();
//			NCBITaxon ncbiTaxon = taxonomyServices._getNCBITaxon("" + sample.taxonId);
//			sample.scientificName = ncbiTaxon.getScientificName();			
//			sample.validate(contextValidation);
//			println("displayErrors pour validationSample:" + sample.code);
//		}
//		if (contextValidation.hasErrors()) {
//			contextValidation.displayErrors(logger);
//			println(Json.prettyPrint(app.errorsAsJson(contextValidation.getErrors())));
//		} else {		
//			// Sauver tous les samples s'ils n'existent pas dans la base et si sample Genoscope
//			for (Sample sample : listSamples) {
//				sample.adminComment = adminComment;
//				// Cas normalement deja detecté par validate avec un context de creation:
//				if (sampleAPI.dao_checkObjectExist("code", sample.code)) {
//					throw new RuntimeException("Tentativee de sauvegarde dans la base d'un sample deja present dans NGL-SUB. sampleCode="+ sample.code + " et sampleAC="+sample.accession);
//				}
//				if (sampleAPI.dao_checkObjectExist("accession", sample.accession)) {
//					throw new RuntimeException("Tentativee de sauvegarde dans la base d'un sample deja present dans NGL-SUB. sampleCode="+ sample.code + " et sampleAC="+sample.accession);
//				}
//				printfln("sauvegarde dans la base du sample avec AC = %s et code =%s", sample.accession ,sample.code); 
//				sampleAPI.dao_saveObject(sample);
//			}
//		}	
//	}
//}
//
