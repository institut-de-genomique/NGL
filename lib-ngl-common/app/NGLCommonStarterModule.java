
import java.util.List;

import play.api.Configuration;
import play.api.Environment;

import play.api.inject.Binding;

/**
 * NGL common starter module that defines enough of an application to
 * run the NGL common test suite. Applications that inherit from NGL common
 * should extends this module and add the relevant bindings.
 * 
 * @author vrd
 *
 */
public class NGLCommonStarterModule extends NGLBaseModule {
	
	private boolean enableDrools = false;
	
	/**
	 * Constructor.
	 * @param environment   environment
	 * @param configuration configuration
	 */
	public NGLCommonStarterModule(Environment environment, Configuration configuration) {
		super(environment,configuration);
		logger.debug("created {}", this);
	}
	
	/**
	 * Adds Mongo plugin start through eager bindings.
	 * <ul>
	 *   <li>{@link play.modules.jongo.MongoDBPlugin}</li>
	 *   <li>{@link play.modules.mongojack.MongoDBPlugin}</li>
	 * </ul>
	 */
	@Override
	public List<Binding<?>> nglCustomBindings(Environment environment, Configuration configuration) {
		List<Binding<?>> l = super.nglCustomBindings(environment,configuration);
		l.add(bind(play.modules.jongo.MongoDBPlugin.class           ).toSelf().eagerly());
		l.add(bind(play.modules.mongojack.MongoDBPlugin.class       ).toSelf().eagerly());
		if(enableDrools) {
			l.add(bind(rules.services.Rules6Component.class         ).toSelf().eagerly());
		} else {
			//Default behavior: disable Drools but it could be overridden into tests
			l.add(bind(rules.services.Rules6Component.class).to(rules.services.Rules6ComponentDisable.class).eagerly());
		}
		return l;
	}

	/**
	 * Enable the drools component binding.
	 */
	public void enableDrools() {
		enableDrools = true;
	}
	
}

/*
public class NGLCommonStarterModule extends play.api.inject.Module {
	
	private static final play.Logger.ALogger logger = play.Logger.of(NGLCommonStarterModule.class);
		
	// Required constructor
	public NGLCommonStarterModule(Environment environment, Configuration configuration) {
	}

	// Application bindings
	@Override
	public Seq<Binding<?>> bindings(Environment environment, Configuration configuration) {
		return seq(
				bind(fr.cea.ig.play.IGGlobals.class                   ).toSelf().eagerly(),
				bind(controllers.resources.AssetPlugin.class          ).toSelf().eagerly(),
				// Authenticator binding
				bind(fr.cea.ig.authentication.IAuthenticator.class)
				  .to(fr.cea.ig.authentication.authenticators.ConfiguredAuthenticator.class).eagerly(),
				bind(fr.cea.ig.authorization.IAuthorizator.class)
				  .to(fr.cea.ig.authorization.authorizators.ConfiguredAuthorizator.class).eagerly(),

				// bind(play.modules.jongo.MongoDBPlugin.class           ).toSelf().eagerly(),
				// was started in the mongodbplugin playplugins. 
				bind(play.modules.mongojack.MongoDBPlugin.class       ).toSelf().eagerly(),
				// bind(rules.services.Rules6Component.class             ).toSelf().eagerly(),
				//bind(NGLStarter.class                                 ).toSelf().eagerly() // asEagerSingleton ?
				// Force JsMessages init
				// bind(controllers.main.tpl.Main.class                  ).toSelf().eagerly(),
				// The plugins conf stated that it's started last. It should be started after the
				// application is created because of global application instance access but it's not
				// possible anymore. We should be able to use spring as the play injector but the
				// eager initialization of the component-scan part of the configuration fails
				// miserably. We should add @Lazy to @Component.
				bind(play.api.modules.spring.SpringPlugin.class       ).toSelf().eagerly()
			);
	}

	
}
*/