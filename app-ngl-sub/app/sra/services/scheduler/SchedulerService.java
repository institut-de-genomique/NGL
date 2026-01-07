package sra.services.scheduler;

import javax.inject.Inject;
import javax.inject.Singleton;

import akka.actor.ActorSystem;
import play.inject.Injector;
import scala.concurrent.duration.FiniteDuration;
import services.sra.NGLSUBConfig;


@Singleton
public class SchedulerService {
	private static final play.Logger.ALogger logger = play.Logger.of(SchedulerService.class);
	// utilisation de l'actorSystem akka transmis au demarrage de l'application
	private final ActorSystem actorSystem;
	// utilisation de l'injecteur de Play par binding au demarrage de l'application
	private final Injector injector;
	@Inject
	SchedulerService(ActorSystem actorSystem, Injector injector) {
		this.actorSystem = actorSystem;
		this.injector = injector;
	}
	
	
	// methode qui permet d'envoyer en cron (de scheduler), n'importe quelle classe runnable.
	// exemple de lancement :
	//startScheduling(CronLog.class,  Duration.create(1, TimeUnit.SECONDS) , Duration.create(1, TimeUnit.SECONDS));
	public <T extends Runnable> void startScheduling(Class<T> c, FiniteDuration durationFromStart,  FiniteDuration durationFromNextIteration) {	
		T t = this.injector.instanceOf(c); // utilisation de l'injecteur play
		logger.warn("scheduling: {}, in {}", c, durationFromStart);
		this.actorSystem.scheduler().
		schedule(durationFromStart,
                durationFromNextIteration, 
                t, 
                this.actorSystem.dispatcher());		
	}

}
