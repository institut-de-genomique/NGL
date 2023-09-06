package services.instance;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.NGLConfig;
import scala.concurrent.duration.Duration;
import services.instance.balancesheet.UpdateDailyBalanceSheet;
import services.instance.balancesheet.UpdateQuarterBalanceSheet;
import services.instance.container.ContainerImportCNG;
import services.instance.project.ProjectImportCNG;
import services.instance.sample.UpdateFlagForReportingData;
import services.instance.sample.UpdateReportingData;
import services.instance.sample.UpdateSampleNCBITaxonCNG;
import services.instance.sample.UpdateSamplePropertiesCNS;
import services.taxonomy.TaxonomyServices;

/**
 * Import samples and container from CNG's LIMS to NGL.
 *  
 * @author dnoisett
 * 
 */
public class ImportDataCNG {
	
	private final NGLApplication app;

	private final NGLConfig config;
	
	@Inject
	public ImportDataCNG(NGLApplication app, NGLConfig config) {
		this.app = app;
		this.config = config;
	}

	public void run() {
		// 1er parametre=delai avant 1er declenchement, 2eme parametre=delai pour repetition
		// decaler les demarragesr pour eviter que les logs s'entrecroisent !!!

		//vérifier s'il y a des projets a importer 1 fois par heure
		new ProjectImportCNG(app)
			.startScheduling(ImportDataUtil.getDurationInMinute(0),Duration.create(60,TimeUnit.MINUTES)); 
		
		//vérifier s'il y a des index a importer 1 fois par jour
		//Desactive le 18/07/2019 NGL-2538
		//new IndexImportCNG(app)
		//	.startScheduling(ImportDataUtil.getDurationInMillinsBefore(5, 0),Duration.create(1,TimeUnit.DAYS));
		
		//FDS: pas fonctionnel ?? ni nécessaire ??
		//new ExperimentImportCNG(Duration.create(4,TimeUnit.SECONDS),Duration.create(60,TimeUnit.MINUTES));	
		
		//vérifier s'il y a des containers a importer toutes les 10 minutes
		new ContainerImportCNG(app)
			.startScheduling(ImportDataUtil.getDurationInMinute(30),Duration.create(10,TimeUnit.MINUTES)); 
		
		//Mise a jour des info du NCBI pour les samples qui n'en ont pas
		new UpdateSampleNCBITaxonCNG(app, new TaxonomyServices(app))
			.startScheduling(ImportDataUtil.getDurationInMinute(30),Duration.create(6,TimeUnit.HOURS)); 
		 
		//11/04/2017 ajouter la propagation des modifications apportées aux samples...( le nom ..CNS est trompeur=> code commun)
		new UpdateSamplePropertiesCNS(app)
			.startScheduling(ImportDataUtil.getDurationInMinute(45),Duration.create(6,TimeUnit.HOURS)); 
		
		new UpdateFlagForReportingData(app, config)
			.startScheduling(ImportDataUtil.getDurationInHourAndMinute(20, 0),Duration.create(1,TimeUnit.DAYS));

		new UpdateReportingData(app, config)
			.startScheduling(ImportDataUtil.getDurationInHourAndMinute(20, 30),Duration.create(1,TimeUnit.DAYS));

		new UpdateDailyBalanceSheet(app)
			.startScheduling(ImportDataUtil.getDurationInHourAndMinute(20, 0), Duration.create(1, TimeUnit.DAYS));
	
		new UpdateQuarterBalanceSheet(app)
			.startScheduling(ImportDataUtil.getDurationInHourAndMinute(24, 0), Duration.create(91, TimeUnit.DAYS)); 
	}
	
}
