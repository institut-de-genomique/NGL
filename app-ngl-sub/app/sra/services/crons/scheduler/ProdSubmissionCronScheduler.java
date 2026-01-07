package sra.services.crons.scheduler;

import javax.inject.Inject;

import services.sra.NGLSUBConfig;
import sra.services.crons.AlerteEmailPublicStudy;
import sra.services.crons.SubmissionAtlaseaProjectsToEbi;
import sra.services.scheduler.AbstractSubmissionCronScheduler;
import sra.services.scheduler.SchedulerService;
import sra.services.scheduler.Tools;

public class ProdSubmissionCronScheduler extends AbstractSubmissionCronScheduler {
	@Inject
	public ProdSubmissionCronScheduler(SchedulerService scheduler) {
		super(scheduler);	
	}

	@Override
	/**
	 * Methode pour scheduler l'ensemble des crons de NGL-SUB, en particulier en PROD. Ne pas mettre d'appels en commentaires. 
	 * Utiliser TestSubmissionCronScheduler pour tester de nouveaux crons. 
	 * 
	 **/
	public void startAllScheduling() {
		logger.info("dans ProdSubmissionCronScheduler.startAllScheduling()");
		startScheduling(AlerteEmailPublicStudy.class, Tools.at(6, 00), Tools.days(1));
		startScheduling(SubmissionAtlaseaProjectsToEbi.class, Tools.at(6, 30), Tools.days(1));
	}

}
