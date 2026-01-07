package sra.services.scheduler;


import javax.inject.Inject;

import akka.actor.ActorSystem;
import play.inject.Injector;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;
import services.sra.NGLSUBConfig;
import sra.services.crons.AlerteEmailPublicStudy;
import sra.services.crons.SubmissionAtlaseaProjectsToEbi;
import sra.services.crons.scheduler.ProdSubmissionCronScheduler;
import sra.services.crons.scheduler.TestSubmissionCronScheduler;


import java.util.concurrent.TimeUnit;


public class SubmissionCronScheduler {
	private static final play.Logger.ALogger logger = play.Logger.of(SubmissionCronScheduler.class);
	// utilisation de l'injecteur de Play 
	//private final Injector injector;
	//private final ActorSystem actorSystem;
	//private final NGLSUBConfig configuration;
	//private final SchedulerService scheduler;

	@Inject
	public SubmissionCronScheduler(SchedulerService scheduler, NGLSUBConfig configuration, Injector injector) {	
		
		if ( configuration.isSTARTCRONS_PROD()) {
			logger.info("XXXXXXXXXXXXXXXXXXXXX            NGLSUB-CRONS-PROD STARTED");
			//ProdSubmissionCronScheduler prodSubmissionCronScheduler = new ProdSubmissionCronScheduler(this.scheduler);
			//Class <?> c = Class.forName(s);
			AbstractSubmissionCronScheduler cron = injector.instanceOf(ProdSubmissionCronScheduler.class);
			cron.startAllScheduling();
		} else if ( configuration.isSTARTCRONS_TEST() && ! configuration.isPROD() ) {
			logger.info("configuration.isProd = {}", configuration.isPROD());
			logger.info("isSTARTCRONS_TEST = {}", configuration.isSTARTCRONS_TEST());
			logger.info("XXXXXXXXXXXXXXXXXXXXX            NGLSUB-CRONS-TEST STARTED");
			AbstractSubmissionCronScheduler cron = injector.instanceOf(TestSubmissionCronScheduler.class);
			cron.startAllScheduling();
		} else if ( configuration.isSTARTCRONS_TEST() && configuration.isPROD() ) {
			logger.info("configuration.isProd = {}", configuration.isPROD());
			logger.info("isSTARTCRONS_TEST = {}", configuration.isSTARTCRONS_TEST());
			logger.info("XXXXXXXXXXXXXXXXXXXXX            NGLSUB-CRONS NOT STARTED (NGL-SUB.STARTCRONS=\"TEST\" et NGL-SUB.PROD=\"true\" incompatibles ");
		}else {
			logger.info("XXXXXXXXXXXXXXXXXXXXX            NGLSUB-CRONS NOT STARTED");
		}
	}

}
