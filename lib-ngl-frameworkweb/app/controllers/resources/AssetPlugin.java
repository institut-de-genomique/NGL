package controllers.resources;

// import play.Application;
import com.typesafe.config.Config;
import play.Logger;
// import play.Play;
// import play.Plugin;
import javax.inject.Inject;
import javax.inject.Singleton;
// import fr.cea.ig.authentication.AuthenticatePlugin;

@Singleton
public class AssetPlugin { // extends play.Plugin {

	/**
	 * Logger.
	 */
	private static final Logger.ALogger logger = Logger.of(AssetPlugin.class);

	/**
	 * Application.conf key for assets server.
	 */ 
	private static final String ASSET_URL = "asset.url";

	/**
	 * Global definition of the assets server url.
	 */
	private static String url; 
	
	@Inject
	// public AssetPlugin(Application app)	{
	public AssetPlugin(Config config) {
		// rl = app.configuration().getString(ASSET_URL);
		if (config.hasPath(ASSET_URL)) {
			url = config.getString(ASSET_URL);
			logger.info("using asset url '{}'",url);
		} else {
			url = "notConfigured";
			logger.error("no asset url at {} in configuration",ASSET_URL);
		}
	}

	public static String getServer() {
		if (url == null) {
			logger.warn("accessing url that has not been initialized");
			return "assetPathNotSet";
		}
		logger.debug("getServer() : " + url); 
		return url;
	}

}
