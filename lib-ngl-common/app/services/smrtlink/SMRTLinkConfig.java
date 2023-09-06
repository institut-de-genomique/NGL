package services.smrtlink;

import javax.inject.Inject;

import com.typesafe.config.Config;

public class SMRTLinkConfig {
	
	private static final String USERNAME_CONFIG_PATH = "smrtlink.username";
	private static final String PASSWORD_CONFIG_PATH = "smrtlink.password";
	private static final String SMRT_LINK_URL_CONFIG_PATH = "smrtlink.url";
	private static final String KEYCLOACK_URL_CONFIG_PATH = "smrtlink.keycloack";
	
	private static final String SMRTLINK_PREFIX = "/SMRTLink/1.0.0";
	
	private final String smrtLinkUsername;
	private final String smrtLinkPassword;
	private final String statusEndpoint;
	private final String runDesignEndpoint;
	private final String importRunDesignEndpoint;
	private final String torkenEndpoint;
	
	
	@Inject
	public SMRTLinkConfig(Config config) {
		this.smrtLinkUsername = config.getString(USERNAME_CONFIG_PATH);
		this.smrtLinkPassword = config.getString(PASSWORD_CONFIG_PATH);
		String smrtLinkUrl = config.getString(SMRT_LINK_URL_CONFIG_PATH);
		this.statusEndpoint = smrtLinkUrl + SMRTLinkConfig.SMRTLINK_PREFIX + "/status";
		this.runDesignEndpoint = smrtLinkUrl + SMRTLinkConfig.SMRTLINK_PREFIX + "/smrt-link/run-design";
		this.importRunDesignEndpoint = smrtLinkUrl + SMRTLinkConfig.SMRTLINK_PREFIX + "/smrt-link/import-run-design";
		String keycloackkUrl = config.getString(KEYCLOACK_URL_CONFIG_PATH);
		this.torkenEndpoint = keycloackkUrl + "/token";
	}
	
	public String getUsername() {
		return this.smrtLinkUsername;
	}
	
	public String getPassword() {
		return this.smrtLinkPassword;
	}
	
	public String tokenEndpoint() {
		return this.torkenEndpoint;
	}
	
	public String statusEndpoint() {
		return this.statusEndpoint;
	}
	
	public String runDesignEndpoint(String uuid) {
		return this.runDesignEndpoint + "/" + uuid;
	}
	
	public String importRunDesignEndpoint() {
		return this.importRunDesignEndpoint;
	}

}
