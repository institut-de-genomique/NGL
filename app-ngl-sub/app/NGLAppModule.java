
import play.api.Configuration;
import play.api.Environment;

/**
 * Common name across NGL applications for the start module.
 *  
 * @author vrd
 *
 */
public class NGLAppModule extends NGLSubStarterModule {
	
	/**
	 * Constructor.
	 * @param environment   environment
	 * @param configuration configuration
	 */
	public NGLAppModule(Environment environment, Configuration configuration) {
		super(environment,configuration);
	}

}
