package fr.cea.ig.play;

import com.typesafe.config.Config;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Config facade defining accessors with defaults.
 * 
 * @author vrd
 *
 */
// @Deprecated
@Singleton
public class IGConfig {
	
	/**
	 * Underlying configuration.
	 */
	private final Config config;
	
	/**
	 * Constructor.
	 * @param config configuration to use
	 */
	@Inject
	public IGConfig(Config config) {
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
	
	public String getCheckedString(String path, String[] values) {
		if (!config.hasPath(path))
			throw new RuntimeException(path + " has no value in configuration");
		String value = config.getString(path);
		for (String s : values)
			if (s.equals(value))
				return value;
		throw new RuntimeException("value " + value + " at " + path + " not in allowed values:" + String.join(",", values));
	}
	
	public String getString(String path) {
		if (!config.hasPath(path))
			throw new RuntimeException(path + " has no value in configuration");
		return config.getString(path);
	}
	
	// Silently fails, not good
	/*
	public String getString(String[] paths, String defaultValue) {
		String result = null;
		for (String path : paths) {
			if (config.hasPath(path)) {
				if (result == null) {
					result = config.getString(path);
				} else {
					
				}
			}
		}
	}
	*/
	
	public String getString(String path, String defaultValue) {
		if (!config.hasPath(path))
			return defaultValue;
		return config.getString(path);
	}
	
	public List<String> getStringList(String path) {
		try {
			return config.getStringList(path);
		} catch (Exception e) {
			return null;
		}
	}

}
