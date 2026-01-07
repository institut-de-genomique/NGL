import java.util.List;
import play.api.Configuration;
import play.api.Environment;
import play.api.inject.Binding;
import sra.services.crons.scheduler.ProdSubmissionCronScheduler;
import sra.services.scheduler.AbstractSubmissionCronScheduler;
import sra.services.scheduler.SchedulerService;
import sra.services.scheduler.SubmissionCronScheduler;
import sra.services.scheduler.Tools;

/**
 * NGL sub application start module.
 *  
 * @author vrd
 *
 */
public class NGLSubStarterModule extends NGLCommonStarterModule {
	
	/**
	 * Constructor.
	 * @param environment   environment
	 * @param configuration configuration 
	 */
	public NGLSubStarterModule(Environment environment, Configuration configuration) {
		super(environment, configuration);
		//enableDrools();  // normalement regles druls du service6 quand meme démarrées meme si ens druls n'est pas activé
		logger.debug("created module " + this);
		logger.info("starting NGL-Sub");
	}
	
	
	
	/**
	 * Adds SubmissionCronScheduler start through eager bindings.
	 * <ul>
	 *   <li>{@link SubmissionCronScheduler}</li>
	 * </ul>
	 */
	@Override
	public List<Binding<?>> nglCustomBindings(Environment environment, Configuration configuration) {
		List<Binding<?>> l = super.nglCustomBindings(environment,configuration);
		// demande de creer au demarrage de play une instance de SubmissionCronScheduller et dans le constructeur
		// de SubmissionCronScheduller, on démarre l'ensemble des crons. 
		l.add(bind(SubmissionCronScheduler.class).toSelf().eagerly()); 
		return l;
	}


}

