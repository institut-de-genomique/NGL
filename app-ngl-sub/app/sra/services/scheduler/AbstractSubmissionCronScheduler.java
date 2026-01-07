package sra.services.scheduler;


import javax.inject.Inject;

import akka.actor.ActorSystem;
import play.inject.Injector;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;
import services.sra.NGLSUBConfig;
import sra.services.crons.AlerteEmailPublicStudy;
import sra.services.crons.SubmissionAtlaseaProjectsToEbi;
import sra.services.crons.tests.ForTestCron_A;
import sra.services.crons.tests.ForTestCron_B;
import sra.services.crons.tests.TestCronLog;
import java.util.concurrent.TimeUnit;


public abstract class AbstractSubmissionCronScheduler {
	protected static final play.Logger.ALogger logger = play.Logger.of(AbstractSubmissionCronScheduler.class);
	//private final NGLSUBConfig configuration;
	private final SchedulerService scheduler;

	@Inject
	//public AbstractSubmissionCronScheduler(SchedulerService scheduler, NGLSUBConfig configuration) {
	public AbstractSubmissionCronScheduler(SchedulerService scheduler) {
		//this.configuration = configuration;
		this.scheduler = scheduler;

	}
	
	// methode à definir dans les sous-classes
	public abstract void startAllScheduling();

	public <T extends Runnable> void startScheduling(Class<T> c, FiniteDuration durationFromStart,  FiniteDuration durationFromNextIteration) {
		scheduler.startScheduling(c, durationFromStart, durationFromNextIteration);
	}

	
}


