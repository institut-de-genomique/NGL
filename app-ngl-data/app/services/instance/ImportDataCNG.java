package services.instance;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import fr.cea.ig.ngl.NGLApplication;
import scala.concurrent.duration.Duration;
import services.instance.container.ContainerImportCNG;
import services.instance.parameter.IndexImportCNG;
import services.instance.project.ProjectImportCNG;
import services.instance.sample.UpdateReportingData;
import services.instance.sample.UpdateSampleNCBITaxonCNG;
import services.instance.sample.UpdateSamplePropertiesCNS;
import services.ncbi.TaxonomyServices;

/**
 * Import samples and container from CNG's LIMS to NGL
 *  
 * @author dnoisett
 * 
 */
public class ImportDataCNG {
	
	private final NGLApplication app;
	
	@Inject
	public ImportDataCNG(NGLApplication app) {
		this.app = app;
	}

	public void run() {
		// 1er parametre=delai avant 1er declenchement, 2eme parametre=delai pour repetition
		// decaler les demarragesr pour eviter que les logs s'entrecroisent !!!
		
//		//vérifier s'il y a des projets a importer 1 fois par heure
//		new ProjectImportCNG(ImportDataUtil.getDurationForNextHour(0),Duration.create(60,TimeUnit.MINUTES), ctx);
//		
//		//vérifier s'il y a des index a importer 1 fois par jour
//		new IndexImportCNG(ImportDataUtil.getDurationInMillinsBefore(5, 0),Duration.create(1,TimeUnit.DAYS), ctx);
//		
//		//FDS: pas fonctionnel ?? ni nécessaire ??
//		//new ExperimentImportCNG(Duration.create(4,TimeUnit.SECONDS),Duration.create(60,TimeUnit.MINUTES));	
//		
//		//vérifier s'il y a des containers a importer toutes les 10 minutes
//		new ContainerImportCNG(ImportDataUtil.getDurationForNextHour(30),Duration.create(10,TimeUnit.MINUTES), ctx);
//		
//		//Mise a jour des info du NCBI pour les samples qui n'en ont pas
//		new UpdateSampleNCBITaxonCNG(ImportDataUtil.getDurationForNextHour(30),Duration.create(6,TimeUnit.HOURS), ctx, new TaxonomyServices(ctx));
//		 
//		//11/04/2017 ajouter la propagation des modifications apportées aux samples...
//		new UpdateSamplePropertiesCNS(ImportDataUtil.getDurationForNextHour(45),Duration.create(6,TimeUnit.HOURS), ctx);
//		
//		new UpdateReportingData(ImportDataUtil.getDurationInMillinsBefore(20, 0),Duration.create(1,TimeUnit.DAYS), ctx);
		
		//vérifier s'il y a des projets a importer 1 fois par heure
		new ProjectImportCNG(app)
			.startScheduling(ImportDataUtil.getDurationForNextHour(0),Duration.create(60,TimeUnit.MINUTES));
		
		//vérifier s'il y a des index a importer 1 fois par jour
		new IndexImportCNG(app)
			.startScheduling(ImportDataUtil.getDurationInMillinsBefore(5, 0),Duration.create(1,TimeUnit.DAYS));
		
		//FDS: pas fonctionnel ?? ni nécessaire ??
		//new ExperimentImportCNG(Duration.create(4,TimeUnit.SECONDS),Duration.create(60,TimeUnit.MINUTES));	
		
		//vérifier s'il y a des containers a importer toutes les 10 minutes
		new ContainerImportCNG(app)
			.startScheduling(ImportDataUtil.getDurationForNextHour(30),Duration.create(10,TimeUnit.MINUTES));
		
		//Mise a jour des info du NCBI pour les samples qui n'en ont pas
		new UpdateSampleNCBITaxonCNG(app, new TaxonomyServices(app))
			.startScheduling(ImportDataUtil.getDurationForNextHour(30),Duration.create(6,TimeUnit.HOURS));
		 
		//11/04/2017 ajouter la propagation des modifications apportées aux samples...( le nom ..CNS est trompeur=> code commun)
		new UpdateSamplePropertiesCNS(app)
			.startScheduling(ImportDataUtil.getDurationForNextHour(45),Duration.create(6,TimeUnit.HOURS));
		
		new UpdateReportingData(app)
			.startScheduling(ImportDataUtil.getDurationInMillinsBefore(20, 0),Duration.create(1,TimeUnit.DAYS));
	}
	
//	public static List<CronTabEntry> cronEntries() {
//		return Arrays.asList(
////				// vérifier s'il y a des projets a importer 1 fois par heure
////				new ProjectImportCNG(ctx)
////				.startScheduling(ImportDataUtil.getDurationForNextHour(0),Duration.create(60,TimeUnit.MINUTES));
//				new CronTabEntry(new CronSchedule().everyHour().minutes(0),
//				                 new Job("import CNG projects", IMPORT, ProjectImportCNG.class)),
////				//vérifier s'il y a des index a importer 1 fois par jour
////				new IndexImportCNG(ctx)
////				.startScheduling(ImportDataUtil.getDurationInMillinsBefore(5, 0),Duration.create(1,TimeUnit.DAYS));
//				new CronTabEntry(new CronSchedule().everyDay().hours(5).minutes(0),
//						         new Job("import CNG index", IMPORT, IndexImportCNG.class)),
//				//FDS: pas fonctionnel ?? ni nécessaire ??
//				//new ExperimentImportCNG(Duration.create(4,TimeUnit.SECONDS),Duration.create(60,TimeUnit.MINUTES));	
//
////				//vérifier s'il y a des containers a importer toutes les 10 minutes
////				new ContainerImportCNG(ctx)
////				.startScheduling(ImportDataUtil.getDurationForNextHour(30),Duration.create(10,TimeUnit.MINUTES));
//				new CronTabEntry(new CronSchedule().everyHour().minutes(0,10,20,30,40,50),
//						         new Job("import CNG containers", IMPORT, ContainerImportCNG.class)),
////				//Mise a jour des info du NCBI pour les samples qui n'en ont pas
////				new UpdateSampleNCBITaxonCNG(ctx, new TaxonomyServices(ctx))
////				.startScheduling(ImportDataUtil.getDurationForNextHour(30),Duration.create(6,TimeUnit.HOURS));
//				new CronTabEntry(new CronSchedule().everyDay().hours(0,6,12,18).minutes(30),
//						         new Job("update CNG sample taxons", IMPORT, UpdateSampleNCBITaxonCNG.class)),
////				//11/04/2017 ajouter la propagation des modifications apportées aux samples...
////				new UpdateSamplePropertiesCNS(ctx)
////				.startScheduling(ImportDataUtil.getDurationForNextHour(45),Duration.create(6,TimeUnit.HOURS));
//				new CronTabEntry(new CronSchedule().everyDay().hours(0,6,12,18).minutes(45),
//						         new Job("update CNG sample properties", IMPORT, UpdateSamplePropertiesCNS.class)),
////				new UpdateReportingData(ctx)
////				.startScheduling(ImportDataUtil.getDurationInMillinsBefore(20, 0),Duration.create(1,TimeUnit.DAYS));
//				new CronTabEntry(new CronSchedule().everyDay().hours(20).minutes(0),
//						         new Job("update CNG reporting data", IMPORT, UpdateReportingData.class))
//				);
//	}
	
}
