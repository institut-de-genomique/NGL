package fr.cea.ig.lfw;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.typesafe.config.Config;

/**
 * Wrapper around play configuration.
 * 
 * @author vrd
 *
 */
@Singleton
public class LFWConfig {
	
	/**
	 * Rules key in the configuration.
	 */
	public static final String NGL_RULES_PATH = "rules.key";

	/**
	 * Underlying configuration.
	 */
	private final Config config;
	
	/**
	 * Constructor.
	 * @param config play configuration
	 */
	@Inject
	public LFWConfig(Config config) {
		this.config = config;	
	}
	
	/**
	 * Underlying configuration instance.
	 * @return underlying configuration instance
	 */
	public Config config() { 
		return config;
	}
	
	/**
	 * Get boolean value at path in config, return the provided
	 * default value if no value is found at given path.
	 *  
	 * @param path         path to look for boolean value at
	 * @param defaultValue value to return if the path is not found in config
	 * @return             either the config value of the given defaultValue
	 */
	public boolean getBoolean(String path, boolean defaultValue) {
		if (!config.hasPath(path))
			return defaultValue;
		return config.getBoolean(path);
	}
	
	/**
	 * Returns a string constrained by a string set provided as an array.
	 * @param path   path of value to get
	 * @param values valid values
	 * @return       string from configuration
	 */
	public String getCheckedString(String path, String[] values) {
		if (!config.hasPath(path))
			throw new RuntimeException(path + " has no value in configuration");
		String value = config.getString(path);
		for (String s : values)
			if (s.equals(value))
				return value;
		throw new RuntimeException("value " + value + " at " + path + " not in allowed values:" + String.join(",", values));
	}
	
	/**
	 * Hard failing get string (throws a runtime exception if path has no value
	 * in the configuration.
	 * @param path value path in configuration
	 * @return     string value from configuration
	 */
	public String getString(String path) {
		if (!config.hasPath(path))
			throw new RuntimeException(path + " has no value in configuration");
		return config.getString(path);
	}
		
	/**
	 * Get string from configuration or default value if it does not exist.
	 * @param path         path of value in configuration
	 * @param defaultValue default value
	 * @return             configuration value or default value if no configuration value is defined
	 */
	public String getString(String path, String defaultValue) {
		if (!config.hasPath(path))
			return defaultValue;
		return config.getString(path);
	}
	
	/**
	 * List of strings from the configuration.
	 * @param path path of value in configuration
	 * @return     value in configuration
	 */
	public List<String> getStringList(String path) {
		try {
			return config.getStringList(path);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Rules key from configuration.
	 * @return rules key
	 */
	public String getRulesKey() {
		return config.getString(NGL_RULES_PATH);
	}

}
