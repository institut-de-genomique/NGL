package services.smrtlink;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Base64;

public final class SMRTLinkKeyCloackHelper {
	
	private SMRTLinkKeyCloackHelper() {}
	
	private static final String UTF_8 = "UTF-8";
	private static final String USERNAME = "username";
	private static final String PASSWORD = "password";
	private static final String GRANT_TYPE = "grant_type";
	private static final String SCOPE = "scope";
	private static final String SCOPE_VALUE = "welcome run-design run-qc openid analysis sample-setup data-management userinfo";
	
	private static String encodedKeyValue(String key, String value) throws SMRTLinkAuthorizationException {
		try {
			return URLEncoder.encode(key, UTF_8) + "=" + URLEncoder.encode(value, UTF_8);
		} catch (UnsupportedEncodingException e) {
			throw new SMRTLinkAuthorizationException("Error while encoding keycloack body for key '" + String.valueOf(key) + "'", e);
		}
	}
	
	public static String getAuthenticationBody(String username, String password) throws SMRTLinkAuthorizationException {
		return String.join("&", 
				encodedKeyValue(GRANT_TYPE, PASSWORD),
				encodedKeyValue(USERNAME, username),
				encodedKeyValue(PASSWORD, password),
				encodedKeyValue(SCOPE, SCOPE_VALUE)
				);
	}
	
	public static String getAuthenticationHeader(String username, String password) {
		String auth = username + ":" + password;
		String EncodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
		return "Basic " + EncodedAuth;
	}

}
