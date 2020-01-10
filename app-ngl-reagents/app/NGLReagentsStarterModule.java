
import play.api.Configuration;
import play.api.Environment;

/**
 * NGL SQ application start module.
 *  
 * @author vrd
 *
 */
public class NGLReagentsStarterModule extends NGLCommonStarterModule {
	
	/**
	 * Constructor.
	 * @param environment   environment
	 * @param configuration configuration 
	 */
	public NGLReagentsStarterModule(Environment environment, Configuration configuration) {
		super(environment,configuration);
		logger.debug("created module " + this);
		logger.info("starting NGL-Reagents");
	}

}

/*
import play.Logger;
import play.Application;
import play.api.Configuration;
import play.api.Environment;
import play.api.inject.Binding;
//import rules.services.RulesServices6;
import scala.collection.Seq;
import play.inject.ApplicationLifecycle;
import play.libs.F;

import javax.inject.Inject;
import javax.inject.Singleton;

public class NGLReagentsStarterModule extends play.api.inject.Module {
	
	private static final Logger.ALogger logger = Logger.of(NGLReagentsStarterModule.class);

	public NGLReagentsStarterModule(Environment environment, Configuration configuration) {
		logger.debug("created module " + this);
		logger.info("starting NGL-Reagents");
	}

	// 0:fr.cea.ig.authentication.AuthenticatePlugin
	// 1:controllers.resources.AssetPlugin
	@Override
	public Seq<Binding<?>> bindings(Environment environment, Configuration configuration) {
		logger.debug("bindings are requested for module " + this);
		return seq(
				bind(fr.cea.ig.play.IGGlobals.class                   ).toSelf().eagerly(),
				bind(controllers.resources.AssetPlugin.class          ).toSelf().eagerly(),
				bind(fr.cea.ig.authentication.IAuthenticator.class)
				  .to(fr.cea.ig.authentication.authenticators.ConfiguredAuthenticator.class).eagerly(),
				bind(fr.cea.ig.authorization.IAuthorizator.class)
				  .to(fr.cea.ig.authorization.authorizators.ConfiguredAuthorizator.class).eagerly(),

				// bind(play.modules.jongo.MongoDBPlugin.class           ).toSelf().eagerly(),
				bind(play.modules.mongojack.MongoDBPlugin.class       ).toSelf().eagerly(),
				// Force JsMessages init
				bind(controllers.main.tpl.Main.class                  ).toSelf().eagerly(),
				bind(play.api.modules.spring.SpringPlugin.class       ).toSelf().eagerly()
				);
	}
	
}
*/
