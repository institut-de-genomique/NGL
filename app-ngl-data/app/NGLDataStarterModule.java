import play.api.Configuration;

import java.util.List;

import javax.inject.Inject;

import fr.cea.ig.play.migration.NGLContext;
import play.api.Environment;
import play.api.inject.Binding;

public class NGLDataStarterModule extends NGLCommonStarterModule {
	
	/**
	 * Constructor.
	 * @param environment   environment
	 * @param configuration configuration 
	 */
	public NGLDataStarterModule(Environment environment, Configuration configuration) {
		super(environment,configuration);
		logger.debug("created module " + this);
		logger.info("starting NGL-DATA");	
		enableDrools();
	}

	/**
	 * Add the drools component initialization.
	 */
	@Override
	public List<Binding<?>> nglCustomBindings(Environment environment, Configuration configuration) {
		List<Binding<?>> l = super.nglCustomBindings(environment,configuration);
		//l.add(bind(rules.services.Rules6Component.class             ).toSelf().eagerly());
		return l;
	}

	/**
	 * Add GlobalStarterComponent last.
	 */
	@Override
	public List<Binding<?>> nglBindings(Environment environment, Configuration configuration) {
		List<Binding<?>> l = super.nglBindings(environment, configuration);
		l.add(bind(GlobalStarterComponent.class                     ).toSelf().eagerly());
		
		return l;
	}
	
}

//Start the old global instance using module level binding.
class GlobalStarterComponent {
	
	@Inject
	public GlobalStarterComponent(play.Application app, NGLContext ctx) {
		new Global(ctx).onStart(app);
	}
	
}

/*
public class NGLDataStarterModule extends play.api.inject.Module {
	
	private static final play.Logger.ALogger logger = play.Logger.of(NGLDataStarterModule.class);
		
	public NGLDataStarterModule(Environment environment, Configuration configuration) {
	}

	@Override
	public Seq<Binding<?>> bindings(Environment environment, Configuration configuration) {
		logger.debug("bindings are requested for module " + this);		
		// -- Recreating the play.conf boot order
		return seq(
				bind(fr.cea.ig.play.IGGlobals.class                   ).toSelf().eagerly(),
				bind(controllers.resources.AssetPlugin.class          ).toSelf().eagerly(),
				bind(fr.cea.ig.authentication.IAuthenticator.class)
				  .to(fr.cea.ig.authentication.authenticators.ConfiguredAuthenticator.class).eagerly(),
				bind(fr.cea.ig.authorization.IAuthorizator.class)
				  .to(fr.cea.ig.authorization.authorizators.ConfiguredAuthorizator.class).eagerly(),

				//bind(play.modules.jongo.MongoDBPlugin.class           ).toSelf().eagerly(),
				// was started in the mongodbplugin playplugins. 
				bind(play.modules.mongojack.MongoDBPlugin.class       ).toSelf().eagerly(),
				bind(rules.services.Rules6Component.class             ).toSelf().eagerly(),
				//bind(NGLStarter.class                                 ).toSelf().eagerly() // asEagerSingleton ?
				// Force JsMessages init
				//bind(controllers.main.tpl.Main.class                  ).toSelf().eagerly(),
				// The plugins conf stated that it's started last. It should be started after the
				// application is created because of global application instance access but it's not
				// possible anymore. We should be able to use spring as the play injector but the
				// eager initialization of the component-scan part of the configuration fails
				// miserably. We should add @Lazy to @Component.
				bind(play.api.modules.spring.SpringPlugin.class       ).toSelf().eagerly(),
				bind(GlobalStarterComponent.class                     ).toSelf().eagerly()				
			);
	}

	
}
*/


