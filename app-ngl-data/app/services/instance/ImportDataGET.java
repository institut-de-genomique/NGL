package services.instance;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.concurrent.TimeUnit;
import models.Constants;
import scala.concurrent.duration.Duration;
import services.instance.container.PoolImportGET;
//import services.instance.container.BanqueAmpliImportGET;
//import services.instance.container.PrepaflowcellImportGET;
//import services.instance.container.SizingImportGET;
//import services.instance.container.SolutionStockImportGET;
import services.instance.container.TubeImportGET;
//import services.instance.container.UpdateSolutionStockGET;
//import services.instance.container.UpdateTaraPropertiesGET;
import services.instance.user.UserImportGET;
import services.instance.parameter.IndexImportGET;
import services.instance.project.ProjectImportGET;
import services.instance.resolution.ResolutionServiceGET;
import services.instance.container.SampleImportGET;
import services.instance.container.puitsPlaqueImportGET;
//import services.instance.run.RunExtImportGET;
//import services.instance.run.RunImportGET;
//import services.instance.run.UpdateReadSetGET;
//import services.instance.sample.UpdateSampleGET;
//import services.instance.resolution.ResolutionService;
import play.data.validation.ValidationError;
import play.Logger;
import validation.ContextValidation;

public class ImportDataGET{

	public ImportDataGET(){
		Logger.debug("ImportDataGET");
/*
 * 		ResolutionService
 * 		Créé dans la collection mongo (ngl_common.ResolutionConfiguration) les résolutions à indiquer à la fin d'expérience 
 * 		n'ont pas besoin d'être importés régulièrement
 */
//  	new ResolutionServiceGET();
//		Map<String,List<ValidationError>> errors = new HashMap<String, List<ValidationError>>();	
//		ContextValidation ctx = new ContextValidation(Constants.NGL_DATA_USER);
//		ctx.setCreationMode();
//		try {
//			ResolutionServiceGET.main(ctx); 
//			if (ctx.errors.size() > 0) {
//				Logger.error(ctx.errors.size() + " erreurs : " + errors);
//			} 
//		} catch (Exception e) {
//			Logger.error(e.getMessage(), e);
//		}

		new ProjectImportGET(Duration.create(1,TimeUnit.SECONDS),Duration.create(60,TimeUnit.MINUTES)); 
		//Import Index
		new IndexImportGET(Duration.create(1,TimeUnit.SECONDS),Duration.create(24,TimeUnit.HOURS)); 
 
		//Update/Create Container
		new TubeImportGET(Duration.create(3,TimeUnit.SECONDS),Duration.create(60,TimeUnit.MINUTES)); 
		//new SampleImportGET(Duration.create(10,TimeUnit.SECONDS),Duration.create(60,TimeUnit.MINUTES));
		new puitsPlaqueImportGET(Duration.create(7,TimeUnit.SECONDS),Duration.create(60,TimeUnit.MINUTES));
		new PoolImportGET(Duration.create(10,TimeUnit.SECONDS),Duration.create(60,TimeUnit.MINUTES));
		new UserImportGET(Duration.create(50,TimeUnit.SECONDS),Duration.create(24,TimeUnit.MINUTES));
	}

}
