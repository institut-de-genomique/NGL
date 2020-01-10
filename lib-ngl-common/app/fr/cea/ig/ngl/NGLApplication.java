package fr.cea.ig.ngl;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.inject.Provider;

import fr.cea.ig.authorization.IAuthorizator;
import fr.cea.ig.lfw.LFWApplication;
import fr.cea.ig.ngl.dao.api.APIs;
import play.i18n.Lang;
import play.i18n.Messages;
import play.libs.ws.WSClient;

// this class : fr.cea.ig.ngl.NGLApplication

@Singleton
public class NGLApplication extends LFWApplication {
	
	private final Provider<APIs> apis;
	private final NGLConfig      config;
	private final IAuthorizator  authorizator;
	
	@Inject
	public NGLApplication(LFWApplication lfwa ,
						  NGLConfig      config,
			              Provider<APIs> apis,
			              IAuthorizator  authorizator) {
		super(lfwa);
		this.apis         = apis;
		this.config       = config;
		this.authorizator = authorizator;
	}
	
	public APIs apis()           { return apis.get(); }
	public NGLConfig nglConfig() { return config;     }
	public IAuthorizator authorizator() { return authorizator; } 

	// parce que pas trouvé le wsClient:
	public WSClient ws() {
		return injector().instanceOf(WSClient.class);
	}	

	// --------------------------------------------------------------------------------------------------------------
	// --- old NGLContext method
	
	// Should use the currentLang method.
	public Messages messages() {
		// logger.debug("messages");
		List<Lang> langs = new ArrayList<>();
		Lang lang = currentLang();
		if (lang != null)
			langs.add(lang);
		return messagesApi().preferred(langs);
		// return Messages;
	}
	
	public String message(String key) {
		return messages().at(key);
		// return play.api.i18n.Messages.get(key);
		// return "Messages(" + key + ")";
	}

	/**
	 * Pretty poor shortcut that is used in application name displays in the
	 * menu bar. This appends the application configuration if the application is not
	 * in production mode. 
	 * @param key configuration path
	 * @return    requested path value with the NGL environment name if not in production mode
	 */
	public String messageEnv(String key) {
		if (config.isNGLEnvProd()) 
			return message(key);
		return message(key) + "-" + config.nglEnv();
	}

}
