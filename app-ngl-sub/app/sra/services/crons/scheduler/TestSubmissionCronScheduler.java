package sra.services.crons.scheduler;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import scala.concurrent.duration.Duration;
import services.sra.NGLSUBConfig;
import sra.services.scheduler.AbstractSubmissionCronScheduler;
import sra.services.scheduler.SchedulerService;
import sra.services.scheduler.Tools;
import sra.services.crons.tests.ForTestCron_A;
import sra.services.crons.tests.ForTestCron_B;
import sra.services.crons.tests.TestCronLog;
import sra.services.crons.AlerteEmailPublicStudy;
import sra.services.crons.SubmissionAtlaseaProjectsToEbi;
import sra.services.crons.SubmissionAtlaseaProjectsToEbi;

public class TestSubmissionCronScheduler extends AbstractSubmissionCronScheduler {
	@Inject
	public TestSubmissionCronScheduler(SchedulerService scheduler) {
		super(scheduler);	
	}

	@Override	
	/**
	 * Methode pour scheduler l'ensemble des crons de tests de NGL-SUB. Ne devrait pas etre utilisée sur le serveur de PROD 
	 * 
	 **/
	public void startAllScheduling() {
		logger.info("dans TestSubmissionCronScheduler.startAllScheduling()");
		//		startScheduling(AlerteEmailPublicStudy.class, Tools.at(6, 30), Tools.days(1));
		//		startScheduling(SubmissionAtlaseaProjectsToEbi.class, Tools.at(6, 40), Tools.days(1));
		//startScheduling(ForTestCron_A.class, Duration.create(4, TimeUnit.MINUTES), Duration.create(1, TimeUnit.HOURS));
		//startScheduling(TestCronLog.class,   Duration.create(1, TimeUnit.SECONDS), Duration.create(1, TimeUnit.SECONDS));
		//startScheduling(TestCronLog.class,   Tools.at(21, 30), Tools.days(1));

		//		startScheduling(AlerteEmailPublicStudy.class, Duration.create(2, TimeUnit.MINUTES), Duration.create(1, TimeUnit.HOURS));
		//startScheduling(ForTestCron_A.class, Duration.create(4, TimeUnit.MINUTES), Duration.create(1, TimeUnit.HOURS));
		//startScheduling(SubmissionAtlaseaProjectsToEbi.class, Duration.create(20, TimeUnit.SECONDS), Duration.create(24, TimeUnit.HOURS));
		//startScheduling(ForTestCron_B.class, Duration.create(10, TimeUnit.MINUTES), Duration.create(1, TimeUnit.HOURS));
		startScheduling(AlerteEmailPublicStudy.class, Tools.at(6, 00), Tools.days(1));
		startScheduling(AlerteEmailPublicStudy.class, Tools.at(6, 00), Tools.days(1));

	}
}