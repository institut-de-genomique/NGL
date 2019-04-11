package services.instance;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import fr.cea.ig.ngl.NGLApplication;
import scala.concurrent.duration.Duration;
import services.instance.container.BanqueAmpliImportCNS;
import services.instance.container.SizingImportCNS;
import services.instance.container.TubeImportCNS;
import services.instance.container.UpdateTaraPropertiesCNS;
import services.instance.parameter.IndexImportCNS;
import services.instance.project.ProjectImportCNS;
import services.instance.sample.UpdateReportingData;
import services.instance.sample.UpdateSampleCNS;
import services.instance.sample.UpdateSampleNCBITaxonCNS;
import services.instance.sample.UpdateSamplePropertiesCNS;
import services.ncbi.TaxonomyServices;

public class ImportDataCNS {

	private final NGLApplication app;
	
	@Inject
	public ImportDataCNS(NGLApplication app) {
		this.app = app;
	}

	public void run() {
//		// Import Projects tous les jours à 16h00
//		new ProjectImportCNS(ImportDataUtil.getDurationForNextHour(0),Duration.create(1,TimeUnit.HOURS), ctx);
//		new TubeImportCNS(ImportDataUtil.getDurationForNextHour(10),Duration.create(1,TimeUnit.HOURS), ctx);
//		new UpdateSampleCNS(ImportDataUtil.getDurationForNextHour(20),Duration.create(1,TimeUnit.HOURS), ctx);
//		
//		new BanqueAmpliImportCNS(Duration.create(5,TimeUnit.SECONDS),Duration.create(5,TimeUnit.MINUTES), ctx);
//		new SizingImportCNS(Duration.create(10,TimeUnit.SECONDS),Duration.create(5,TimeUnit.MINUTES), ctx);
//		
//		
//		//Update/Create Container
//		//	new PrepaflowcellImportCNS(Duration.create(2,TimeUnit.MINUTES),Duration.create(15,TimeUnit.MINUTES));
//	    //new RunImportCNS(Duration.create(5,TimeUnit.MINUTES),Duration.create(60,TimeUnit.MINUTES));
//	    //new UpdateReadSetCNS(Duration.create(6,TimeUnit.MINUTES),Duration.create(60,TimeUnit.MINUTES));
//	    //Update State and Tara Properties
//	    
//	    
//		
//		//new SolutionStockImportCNS(Duration.create(30,TimeUnit.SECONDS),Duration.create(10,TimeUnit.MINUTES));
//		//new UpdateSolutionStockCNS(Duration.create(20,TimeUnit.SECONDS),Duration.create(5,TimeUnit.MINUTES));
//		
//		//Update NCBI scientificName and lineage for Sample
//		
//			
//		
//		/*
//		new UpdateSizingCNS(Duration.create(1,TimeUnit.MINUTES),Duration.create(10,TimeUnit.MINUTES));
//		new UpdateAmpliCNS(Duration.create(1,TimeUnit.MINUTES),Duration.create(10,TimeUnit.MINUTES));
//		*/
//		new UpdateSampleNCBITaxonCNS(ImportDataUtil.getDurationForNextHour(15),Duration.create(6,TimeUnit.HOURS), ctx, new TaxonomyServices(ctx));
//		new UpdateSamplePropertiesCNS(ImportDataUtil.getDurationForNextHour(45),Duration.create(6,TimeUnit.HOURS), ctx);
//		
//		//new RunExtImportCNS(ImportDataUtil.getDurationInMillinsBefore(12, 30),Duration.create(12,TimeUnit.HOURS), ctx);
//		
//		new UpdateReportingData(ImportDataUtil.getDurationInMillinsBefore(20, 0),Duration.create(1,TimeUnit.DAYS), ctx);
//		new UpdateTaraPropertiesCNS(ImportDataUtil.getDurationInMillinsBefore(4, 0),Duration.create(1,TimeUnit.DAYS), ctx);
//	    new IndexImportCNS(ImportDataUtil.getDurationInMillinsBefore(5, 0),Duration.create(1,TimeUnit.DAYS), ctx);

		// Import Projects tous les jours à 16h00
		new ProjectImportCNS(app)
			.startScheduling(ImportDataUtil.getDurationForNextHour(0),Duration.create(1,TimeUnit.HOURS));
		new TubeImportCNS(app)
			.startScheduling(ImportDataUtil.getDurationForNextHour(10),Duration.create(1,TimeUnit.HOURS));
		new UpdateSampleCNS(app)
			.startScheduling(ImportDataUtil.getDurationForNextHour(20),Duration.create(1,TimeUnit.HOURS));
		
		new BanqueAmpliImportCNS(app)
			.startScheduling(Duration.create(5,TimeUnit.SECONDS),Duration.create(5,TimeUnit.MINUTES));
		new SizingImportCNS(app)
			.startScheduling(Duration.create(10,TimeUnit.SECONDS),Duration.create(5,TimeUnit.MINUTES));
		
		
		//Update/Create Container
		//	new PrepaflowcellImportCNS(Duration.create(2,TimeUnit.MINUTES),Duration.create(15,TimeUnit.MINUTES));
	    //new RunImportCNS(Duration.create(5,TimeUnit.MINUTES),Duration.create(60,TimeUnit.MINUTES));
	    //new UpdateReadSetCNS(Duration.create(6,TimeUnit.MINUTES),Duration.create(60,TimeUnit.MINUTES));
	    //Update State and Tara Properties
	    
	    
		
		//new SolutionStockImportCNS(Duration.create(30,TimeUnit.SECONDS),Duration.create(10,TimeUnit.MINUTES));
		//new UpdateSolutionStockCNS(Duration.create(20,TimeUnit.SECONDS),Duration.create(5,TimeUnit.MINUTES));
		
		//Update NCBI scientificName and lineage for Sample
		
			
		
		/*
		new UpdateSizingCNS(Duration.create(1,TimeUnit.MINUTES),Duration.create(10,TimeUnit.MINUTES));
		new UpdateAmpliCNS(Duration.create(1,TimeUnit.MINUTES),Duration.create(10,TimeUnit.MINUTES));
		*/
		new UpdateSampleNCBITaxonCNS(app, new TaxonomyServices(app))
			.startScheduling(ImportDataUtil.getDurationForNextHour(15),Duration.create(6,TimeUnit.HOURS));
		new UpdateSamplePropertiesCNS(app)
			.startScheduling(ImportDataUtil.getDurationForNextHour(45),Duration.create(6,TimeUnit.HOURS));
		
		//new RunExtImportCNS(ImportDataUtil.getDurationInMillinsBefore(12, 30),Duration.create(12,TimeUnit.HOURS), ctx);
		
		new UpdateReportingData(app)
			.startScheduling(ImportDataUtil.getDurationInMillinsBefore(20, 0),Duration.create(1,TimeUnit.DAYS));
		new UpdateTaraPropertiesCNS(app)
			.startScheduling(ImportDataUtil.getDurationInMillinsBefore(4, 0),Duration.create(1,TimeUnit.DAYS));
	    new IndexImportCNS(app)
	    	.startScheduling(ImportDataUtil.getDurationInMillinsBefore(5, 0),Duration.create(1,TimeUnit.DAYS));

	}

//	public static List<CronTabEntry> cronEntries() {
//		return Arrays.asList(
////				// Import Projects tous les jours à 16h00
////				new ProjectImportCNS(ctx)
////				.startScheduling(ImportDataUtil.getDurationForNextHour(0),Duration.create(1,TimeUnit.HOURS));
//				new CronTabEntry(new CronSchedule().everyDay().hours(16).minutes(00),
//						         new Job("import CNS projects", IMPORT, ProjectImportCNS.class)),
////				new TubeImportCNS(ctx)
////				.startScheduling(ImportDataUtil.getDurationForNextHour(10),Duration.create(1,TimeUnit.HOURS));
//				new CronTabEntry(new CronSchedule().everyHour().minutes(10),
//						         new Job("import CNS tubes", IMPORT, TubeImportCNS.class)),
////				new UpdateSampleCNS(ctx)
////				.startScheduling(ImportDataUtil.getDurationForNextHour(20),Duration.create(1,TimeUnit.HOURS));
//				new CronTabEntry(new CronSchedule().everyHour().minutes(20),
//					             new Job("update CNS samples", IMPORT, UpdateSampleCNS.class)),
////				new BanqueAmpliImportCNS(ctx)
////				.startScheduling(Duration.create(5,TimeUnit.SECONDS),Duration.create(5,TimeUnit.MINUTES));
//				new CronTabEntry(new CronSchedule().everyHour().minutes(0,5,10,15,20,25,30,35,40,45,50,55),
//						         new Job("import CNS ampli banks", IMPORT, BanqueAmpliImportCNS.class)),			
////				new SizingImportCNS(ctx)
////				.startScheduling(Duration.create(10,TimeUnit.SECONDS),Duration.create(5,TimeUnit.MINUTES));
//				new CronTabEntry(new CronSchedule().everyHour().minutes(0,5,10,15,20,25,30,35,40,45,50,55),
//						         new Job("import CNS sizing", IMPORT, SizingImportCNS.class)),
//
//		//Update/Create Container
//		//	new PrepaflowcellImportCNS(Duration.create(2,TimeUnit.MINUTES),Duration.create(15,TimeUnit.MINUTES));
//		//new RunImportCNS(Duration.create(5,TimeUnit.MINUTES),Duration.create(60,TimeUnit.MINUTES));
//		//new UpdateReadSetCNS(Duration.create(6,TimeUnit.MINUTES),Duration.create(60,TimeUnit.MINUTES));
//		//Update State and Tara Properties
//
//
//
//		//new SolutionStockImportCNS(Duration.create(30,TimeUnit.SECONDS),Duration.create(10,TimeUnit.MINUTES));
//		//new UpdateSolutionStockCNS(Duration.create(20,TimeUnit.SECONDS),Duration.create(5,TimeUnit.MINUTES));
//
//		//Update NCBI scientificName and lineage for Sample
//
//
//
//		/*
//	new UpdateSizingCNS(Duration.create(1,TimeUnit.MINUTES),Duration.create(10,TimeUnit.MINUTES));
//	new UpdateAmpliCNS(Duration.create(1,TimeUnit.MINUTES),Duration.create(10,TimeUnit.MINUTES));
//		 */
////				new UpdateSampleNCBITaxonCNS(ctx, new TaxonomyServices(ctx))
////				.startScheduling(ImportDataUtil.getDurationForNextHour(15),Duration.create(6,TimeUnit.HOURS));
//				new CronTabEntry(new CronSchedule().everyDay().hours(0,6,12,18).minutes(15),
//						         new Job("update samples taxons", IMPORT, UpdateSampleNCBITaxonCNS.class)), 
////				new UpdateSamplePropertiesCNS(ctx)
////				.startScheduling(ImportDataUtil.getDurationForNextHour(45),Duration.create(6,TimeUnit.HOURS));
//				new CronTabEntry(new CronSchedule().everyDay().hours(0,6,12,18).minutes(45),
//						         new Job("update samples properties", IMPORT, UpdateSamplePropertiesCNS.class)),
//		//new RunExtImportCNS(ImportDataUtil.getDurationInMillinsBefore(12, 30),Duration.create(12,TimeUnit.HOURS), ctx);
////				new UpdateReportingData(ctx)
////				.startScheduling(ImportDataUtil.getDurationInMillinsBefore(20, 0),Duration.create(1,TimeUnit.DAYS));
//				new CronTabEntry(new CronSchedule().everyDay().hours(4).minutes(30),
//						         new Job("update reporting data", IMPORT, UpdateReportingData.class)),
////				new UpdateTaraPropertiesCNS(ctx)
////				.startScheduling(ImportDataUtil.getDurationInMillinsBefore(4, 0),Duration.create(1,TimeUnit.DAYS));
//				new CronTabEntry(new CronSchedule().everyDay().hours(4).minutes(40),
//						         new Job("update tara properties CNS", IMPORT, UpdateTaraPropertiesCNS.class)),
////				new IndexImportCNS(ctx)
////				.startScheduling(ImportDataUtil.getDurationInMillinsBefore(5, 0),Duration.create(1,TimeUnit.DAYS));
//				new CronTabEntry(new CronSchedule().everyDay().hours(4).minutes(50),
//						         new Job("import CNS index", IMPORT, IndexImportCNS.class)),
////				new ReportingCNS(ctx)
//				
//				// ---------------- 
//				// Reporting cron
////				.startScheduling(Duration.create(ImportDataUtil.nextExecutionInSeconds(8, 0),TimeUnit.SECONDS),
////						         Duration.create(1,TimeUnit.DAYS));
//				new CronTabEntry(new CronSchedule().everyDay().hours(8).minutes(0),
//						         new Job("reporting CNS", REPORT, ReportingCNS.class))
//				);
//	}
	
}
