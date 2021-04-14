
import play.api.Configuration;
import play.api.Environment;

/**
 * NGL BI application start module.
 * 
 * @author vrd
 *
 */
public class NGLBIStarterModule extends NGLCommonStarterModule {
	
	/**
	 * Constructor.
	 * @param environment   environment
	 * @param configuration configuration 
	 */
	public NGLBIStarterModule(Environment environment, Configuration configuration) {
		super(environment,configuration);
		logger.debug("created module " + this);
		logger.info("starting NGL-BI");
		enableDrools();
	}

}
