
import play.api.Configuration;
import play.api.Environment;

/**
 * NGL SQ application start module.
 *  
 * @author vrd
 *
 */
public class NGLSQStarterModule extends NGLCommonStarterModule {
	
	/**
	 * Constructor.
	 * @param environment   environment
	 * @param configuration configuration 
	 */
	public NGLSQStarterModule(Environment environment, Configuration configuration) {
		super(environment,configuration);
		logger.debug("created module " + this);
		logger.info("starting NGL-SQ");
		enableDrools();
	}
	
}
