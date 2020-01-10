package fr.cea.ig.play.migration;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import fr.cea.ig.play.IGConfig;

/**
 * Provides named accessors on top of the config.
 * 
 * @author vrd
 *
 */
// @Deprecated
@Singleton
public class NGLConfig {

	/**
	 * Undefined string when a string value is not found at a given path. 
	 */
	public static final String UNDEFINED_STRING = "#UNDEFINED#";
	
	/**
	 * NGL configuration path in configuration.
	 */
	public static final String NGL_ENV_PATH = "ngl.env";
	
	/**
	 * Valid values for the NGL_ENV_PATH value.
	 */
	public static final String[] NGL_ENV_VALUES = { "DEV", "UAT", "PROD" };
	
	/**
	 * NGL institute path in configuration.
	 */
	public static final String NGL_INSTITUTE_PATH = "institute"; 
	
	public static final String NGL_RULES_PATH = "rules.key";
	
	public static final String NGL_BARCODE_PRINTING_KEY = "ngl.printing.cb";
	
	public static final String NGL_APPLICATION_VERSION_KEY = "application.version";
	
	public static final String NGL_APPLICATION_NAME_KEY = "application.name";

	/**
	 * Configuration to use.
	 */
	private final IGConfig config;

	
	@Inject
	public NGLConfig(IGConfig config) {
		this.config = config;
	}

	/**
	 * NGL environment, typically DEV or PROD, see NGL_ENV_VALUES. 
	 * @return NGL environment, should be the application attribute 
	 */
	// TODO: use application attribute 
	// @Deprecated
	public String nglEnv() {
		return config.getCheckedString(NGL_ENV_PATH,NGL_ENV_VALUES);
	}
	
	public boolean isNGLEnvProd() {
		return "PROD".equals(nglEnv());
	}
	
	public boolean isNGLEnvDev() {
		return "DEV".equals(nglEnv());
	}
	
	public String getInstitute() {
		return config.getString(NGL_INSTITUTE_PATH);
	}
	
	public String getRulesKey() {
		return config.getString(NGL_RULES_PATH);
	}
		
	/**
	 * Is the NGL bar code printing enabled ?
	 * The configuration path is {@link #NGL_BARCODE_PRINTING_KEY}. 
	 * @return false if not configured, configured value otherwise
	 */
	public boolean isBarCodePrintingEnabled() {
		return config.getBoolean(NGL_BARCODE_PRINTING_KEY, false);
	}
	
	public String getString(String path) {
		// return config.getString(path, path + ":notDefinedInConf");
		return config.getString(path);
	}
	
	public Boolean getBoolean(String path, boolean defValue) {
		return config.getBoolean(path, defValue);
	}
	
	/**
	 * Application version string if defined in the configuration file at {@link #NGL_APPLICATION_VERSION_KEY}. 
	 * @return empty string if not defined in the configuration, the configured value otherwise
	 */
	public String getApplicationVersion() {
		return config.getString(NGL_APPLICATION_VERSION_KEY,"");
	}
	
	public String getApplicationName() {
		return config.getString(NGL_APPLICATION_NAME_KEY,"");
	}
	
	public List<String> getStringList(String path) {
		return config.getStringList(path);
	}
	
	public String getSQUrl() {
		return config.getString("sq.url");
	}
	
	public String getBIUrl() {
		return config.getString("bi.url");
	}
	
	public String getProjectUrl() {
		return config.getString("project.url");
	}
	
}
