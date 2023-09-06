package services.instance;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.NGLConfig;
import scala.concurrent.duration.Duration;
import services.instance.balancesheet.UpdateDailyBalanceSheet;
import services.instance.balancesheet.UpdateQuarterBalanceSheet;
import services.instance.container.TubeImportCNS;
import services.instance.container.UpdateTaraPropertiesCNS;
import services.instance.project.ProjectImportCNS;
import services.instance.sample.UpdateFlagForReportingData;
import services.instance.sample.UpdateReportingData;
import services.instance.sample.UpdateSampleNCBITaxonCNS;
import services.instance.sample.UpdateSamplePropertiesCNS;
import services.taxonomy.TaxonomyServices;

public class ImportDataCNS {

	private final NGLApplication app;

	private final NGLConfig config;
	
	@Inject
	public ImportDataCNS(NGLApplication app, NGLConfig config) {
		this.app = app;
		this.config = config;
	}

	public void run() {

		// Import Projects tous les jours à 16h00
		new ProjectImportCNS(app)
			.startScheduling(ImportDataUtil.getDurationInMinute(0),Duration.create(1,TimeUnit.HOURS));
		
		new TubeImportCNS(app)
			.startScheduling(ImportDataUtil.getDurationInMinute(10),Duration.create(1,TimeUnit.HOURS)); 
		
		//new UpdateSampleCNS(app)
		//	.startScheduling(ImportDataUtil.getDurationForNextHour(20),Duration.create(1,TimeUnit.HOURS));
		
		//Desactive le 18/07/2019 NGL-2538
		//new BanqueAmpliImportCNS(app)
		//	.startScheduling(Duration.create(5,TimeUnit.SECONDS),Duration.create(5,TimeUnit.MINUTES));
		
		//Desactive le 18/07/2019 NGL-2538
		//new SizingImportCNS(app)
		//	.startScheduling(Duration.create(10,TimeUnit.SECONDS),Duration.create(5,TimeUnit.MINUTES));
		
		
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
			.startScheduling(ImportDataUtil.getDurationInMinute(15),Duration.create(6,TimeUnit.HOURS));
		new UpdateSamplePropertiesCNS(app)
			.startScheduling(ImportDataUtil.getDurationInMinute(45),Duration.create(6,TimeUnit.HOURS)); 
		
		//new RunExtImportCNS(ImportDataUtil.getDurationInMillinsBefore(12, 30),Duration.create(12,TimeUnit.HOURS), ctx);
		
		new UpdateFlagForReportingData(app, config)
			.startScheduling(ImportDataUtil.getDurationInHourAndMinute(20, 0),Duration.create(1,TimeUnit.DAYS));
			
		new UpdateReportingData(app, config)
			.startScheduling(ImportDataUtil.getDurationInHourAndMinute(20, 30),Duration.create(1,TimeUnit.DAYS));
		
		//Ne pas désactiver car permet de créer les proprietés des matériels Tara crées dans DBLims sans prop. 
		//Permet également de récuperer les CAB qui sont chargés manuellement (script LoadCAB) dans la base Tara
		new UpdateTaraPropertiesCNS(app)
			.startScheduling(ImportDataUtil.getDurationInHourAndMinute(4, 0),Duration.create(1,TimeUnit.DAYS)); 
		
		//Desactive le 18/07/2019 NGL-2538
	    //new IndexImportCNS(app)
	    //	.startScheduling(ImportDataUtil.getDurationInMillinsBefore(5, 0),Duration.create(1,TimeUnit.DAYS));
		
		new UpdateDailyBalanceSheet(app)
			.startScheduling(ImportDataUtil.getDurationInHourAndMinute(23, 0), Duration.create(1, TimeUnit.DAYS));
		
		new UpdateQuarterBalanceSheet(app)
			.startScheduling(ImportDataUtil.getDurationInHourAndMinute(24, 0), Duration.create(91, TimeUnit.DAYS)); 

	}
	
}
