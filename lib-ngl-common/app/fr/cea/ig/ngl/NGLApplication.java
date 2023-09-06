package fr.cea.ig.ngl;

import java.io.File; // ajouté pour getCanonicalPath() !!!
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
	
	/**
	 *NGL-3363: recupérer dynamiquement le numéro de version via le nom du package qui contient la classe "NGLApplication"
	 *  ==>  https://stackoverflow.com/questions/320542/how-to-get-the-path-of-a-running-jar-file
	 */
	public String getVersion() {
		/* String path = NGLApplication.class.getProtectionDomain().getCodeSource().getLocation().getPath();
			==> resultat = path qui depend de l'installation !!!!
				en DEV chez moi  => /home/fernando/GITCLONES/ngl/lib-ngl-common/target/scala-2.12/classes/    pas de version dans ce path!!!!
				en UAT-ISOPROD   => /env/export/genoapp/uat/ngl/install/ngl-sq-2.16.17-SNAPSHOT/lib/fr.cea.ig.ngl.ngl-common-2.6.0-SNAPSHOT.jar
				en PROD          => /env/cng/apps/ngl/install/ngl-sq-1/lib/fr.cea.ig.ngl.ngl-common-2.6.0-SNAPSHOT.jar
									                      !!! ngl-sq-1 est un lien sybolique !!!
					             =>  /env/cng/apps/ngl/install/ngl-sq-2.16.16-20210604/lib/fr.cea.ig.ngl.ngl-common-2.6.0-SNAPSHOT.jar
		
			!!! utiliser getCanonicalPath pour résoudre les liens dans l'installation NGL 
			mais String path = NGLApplication.class.getProtectionDomain().getCodeSource().getLocation().getCanonicalPath();  ne marche pas!!
			https://exceptionshub.com/how-to-get-the-path-of-a-running-jar-file.html
		*/
		
		String versionNumber="???";
		
		try {
			File jarFile = new File(NGLApplication.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
			String path = jarFile.getCanonicalPath();
			
			String[] pathElements=path.split("/");
			String version= pathElements[pathElements.length-3]; //=> récupère le 3 ème élément du path en partant de la fin
			String[] versionElements=version.split("-"); //  <ngl>-<APP>-<VERSION>-<date>  ==> récupérer l'élément 2=<VERSION>
			if ( versionElements.length > 2) {versionNumber=versionElements[2];}
		} catch (Exception e) {
			return versionNumber;
		}
		
		return versionNumber;
	}
}
