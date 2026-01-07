package services.instance;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.NGLConfig;
import play.inject.Injector;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;
import services.instance.balancesheet.UpdateDailyBalanceSheet;
import services.instance.balancesheet.UpdateMonthlyBalanceSheet;
import services.instance.container.TubeImportCNS;
import services.instance.container.UpdateTaraPropertiesCNS;
import services.instance.project.ProjectImportCNS;
import services.instance.sample.UpdateFlagForReportingData;
import services.instance.sample.UpdateReportingData;
import services.instance.sample.UpdateSampleNCBITaxonCNS;
import services.instance.sample.UpdateSamplePropertiesCNS;
import services.taxonomy.TaxonomyServices;
import services.instance.sample.UpdateSampleAtlaSea;

public class ImportDataCNS {
	
	private static final play.Logger.ALogger logger      = play.Logger.of(ImportDataCNS.class);
	private final NGLApplication app;
	private final NGLConfig      config;	
	
	@Inject
	public ImportDataCNS(NGLApplication app, NGLConfig config) {
		this.app = app;
		this.config = config;
	}	
	
	public void run() {
		new ProjectImportCNS(app)
			.startScheduling(ImportDataUtil.getDurationInMinute(0),Duration.create(1,TimeUnit.HOURS)); 
		
		new TubeImportCNS(app)
			.startScheduling(ImportDataUtil.getDurationInMinute(10),Duration.create(1,TimeUnit.HOURS)); 

		new UpdateSampleNCBITaxonCNS(app, new TaxonomyServices(app))
			.startScheduling(ImportDataUtil.getDurationInMinute(15),Duration.create(6,TimeUnit.HOURS));
		
		new UpdateSamplePropertiesCNS(app)
			.startScheduling(ImportDataUtil.getDurationInMinute(45),Duration.create(6,TimeUnit.HOURS)); 
		
		new UpdateFlagForReportingData(app, config)
			.startScheduling(ImportDataUtil.getDurationInHourAndMinute(20, 00),Duration.create(1,TimeUnit.DAYS));
			
		new UpdateReportingData(app, config)
			.startScheduling(ImportDataUtil.getDurationInHourAndMinute(20, 30),Duration.create(1,TimeUnit.DAYS));
		
		//Ne pas désactiver car permet de créer les proprietés des matériels Tara crées dans DBLims sans prop. 
		//Permet également de récuperer les CAB qui sont chargés manuellement (script LoadCAB) dans la base Tara
		new UpdateTaraPropertiesCNS(app)
			.startScheduling(ImportDataUtil.getDurationInHourAndMinute(4, 0),Duration.create(1,TimeUnit.DAYS)); 
		
		new UpdateDailyBalanceSheet(app)
			.startScheduling(ImportDataUtil.getDurationInHourAndMinute(23, 0), Duration.create(1, TimeUnit.DAYS));
		
		new UpdateMonthlyBalanceSheet(app, config)
			.startScheduling(ImportDataUtil.getDurationInHourAndMinute(24, 0), Duration.create(1, TimeUnit.DAYS));

		new UpdateSampleAtlaSea(app)
			.startScheduling(ImportDataUtil.getDurationInHourAndMinute(21, 30), Duration.create(1, TimeUnit.DAYS));  
 
	}
	
} 