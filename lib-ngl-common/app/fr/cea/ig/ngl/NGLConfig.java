package fr.cea.ig.ngl;

import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import fr.cea.ig.lfw.LFWConfig;

/**
 * NGL configuration typed access to NGL configuration definitions.
 * 
 * @author vrd
 *
 */
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

	public static final String NGL_BARCODE_PRINTING_PATH = "ngl.printing.cb";

	public static final String NGL_APPLICATION_VERSION_PATH = "application.version";

	public static final String NGL_APPLICATION_NAME_PATH = "application.name";

	public static final String NGL_BI_URL_PATH = "bi.url"; 

	public static final String NGL_PROJECT_URL_PATH = "project.url";

	public static final String NGL_SQ_URL_PATH = "sq.url";

	public static final String NGL_IS_MINIFIED = "minify.enabled";

	// AD Configuration
	public static final String AD_SERVER               = "ad.server";
	public static final String AD_USER_NAME            = "ad.user.name";
	public static final String AD_PASSWORD             = "ad.user.password";
	public static final String AD_DOMAIN_COMPONENT     = "ad.dc";
	public static final String AD_ORGANIZATION_UNIT    = "ad.ouproj";
	public static final String AD_MSFU_NIS_DOMAIN      = "ad.msfu.nis.domain";
	public static final String AD_GID_NUMBER_MIN       = "ad.gid.number.min";
	public static final String AD_GID_NUMBER_MAX       = "ad.gid.number.max";
	public static final String AD_DEFAULT_GROUP_ACCESS = "ad.default.group.access";
	public static final String AD_DEFAULT_GROUP_ADMIN  = "ad.default.group.admin";
	public static final String AD_OU_GROUPS_LABO_NAME  = "ad.ou.groups.labo.name";
	public static final String AD_GROUP_REGEX	        = "ad.groups.name.regex";
	public static final String AD_GROUP_TYPE	        = "ad.groups.type";

	public static final String AD_IBFJ_SERVER               = "ad.ibfj.server";
	public static final String AD_IBFJ_USER_NAME            = "ad.ibfj.user.name";
	public static final String AD_IBFJ_PASSWORD             = "ad.ibfj.user.password";
	public static final String AD_IBFJ_DOMAIN_COMPONENT     = "ad.ibfj.dc";
	public static final String AD_IBFJ_ORGANIZATION_UNIT    = "ad.ibfj.ouproj";
	public static final String AD_IBFJ_MSFU_NIS_DOMAIN      = "ad.ibfj.msfu.nis.domain";
	public static final String AD_IBFJ_GID_NUMBER_MIN       = "ad.ibfj.gid.number.min";
	public static final String AD_IBFJ_GID_NUMBER_MAX       = "ad.ibfj.gid.number.max";
	public static final String AD_IBFJ_GROUP_TYPE	        = "ad.ibfj.groups.type";
	public static final String AD_IBFJ_OU_GROUPS_LABO_NAME  = "ad.ibfj.ou.groups.labo.name";
	public static final String AD_IBFJ_DEFAULT_GROUP_ACCESS = "ad.ibfj.default.group.access";
	public static final String AD_IBFJ_DEFAULT_GROUP_ADMIN  = "ad.ibfj.default.group.admin"; 

	public static final String AD_IBFJ_ENABLED = "ad.ibfj.enabled";
	public static final String AD_ORIGINAL_ENABLED = "ad.original.enabled";


	/**
	 * Configuration to use.
	 */
	private final LFWConfig config;

	/**
	 * DI constructor.
	 * @param config config to use
	 */
	@Inject
	public NGLConfig(LFWConfig config) {
		this.config = config;
	}

	/**
	 * NGL environment, typically DEV or PROD, see NGL_ENV_VALUES. 
	 * @return NGL environment, should be the application attribute 
	 */
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

	public boolean isMinified() {
		return config.getBoolean(NGL_IS_MINIFIED, false);
	}

	public String getInstitute() {
		return config.getString(NGL_INSTITUTE_PATH);
	}

	public boolean isCNSInstitute() {
		return "CNS".equals(getInstitute());
	}

	public boolean isCNGInstitute() {
		return "CNG".equals(getInstitute());
	}

	public String getRulesKey() {
		return config.getString(NGL_RULES_PATH);
	}

	/**
	 * Is the NGL bar code printing enabled ?
	 * The configuration path is {@link #NGL_BARCODE_PRINTING_PATH}. 
	 * @return false if not configured, configured value otherwise
	 */
	public boolean isBarCodePrintingEnabled() {
		return config.getBoolean(NGL_BARCODE_PRINTING_PATH, false);
	}

	public String getString(String path) {
		// return config.getString(path, path + ":notDefinedInConf");
		return config.getString(path);
	}

	public Boolean getBoolean(String path, boolean defValue) {
		return config.getBoolean(path, defValue);
	}

	/**
	 * Application version string if defined in the configuration file at {@link #NGL_APPLICATION_VERSION_PATH}. 
	 * @return empty string if not defined in the configuration, the configured value otherwise
	 */
	public String getApplicationVersion() {
		return config.getString(NGL_APPLICATION_VERSION_PATH,"");
	}

	/**
	 * Application name string if defined in the configuration file at {@link #NGL_APPLICATION_NAME_PATH}.
	 * @return empty string if not defined in the configuration, the configured value otherwise
	 */
	public String getApplicationName() {
		return config.getString(NGL_APPLICATION_NAME_PATH,"");
	}

	public List<String> getStringList(String path) {
		return config.getStringList(path);
	}

	public String getSQUrl() {
		return config.getString(NGL_SQ_URL_PATH);
	}

	public String getBIUrl() {
		return config.getString(NGL_BI_URL_PATH);
	}

	public String getProjectUrl() {
		return config.getString(NGL_PROJECT_URL_PATH);
	}

	
	// AD configuration
	public String getActiveDirectoryServer() {
		return config.getString(AD_SERVER);
	}
	
	public String getActiveDirectoryUserName() {
		return config.getString(AD_USER_NAME);
	}
	
	public String getActiveDirectoryPassword() {
		return config.getString(AD_PASSWORD);
	}
	
	public String getActiveDirectoryDomainComponent() {
		return config.getString(AD_DOMAIN_COMPONENT);
	}
	
	public String getActiveDirectoryOrganisationUnit() {
		return config.getString(AD_ORGANIZATION_UNIT);
	}

	public String getActiveDirectoryMsfuNisDomain() {
		return config.getString(AD_MSFU_NIS_DOMAIN);
	}

	public Integer getActiveDirectoryGIDNumberMin() {
		return Integer.parseInt(config.getString(AD_GID_NUMBER_MIN));
	}

	public Integer getActiveDirectoryGIDNumberMax() {
		return Integer.parseInt(config.getString(AD_GID_NUMBER_MAX));
	}

	public String getActiveDirectoryDefaultGroupAccess() {
		return config.getString(AD_DEFAULT_GROUP_ACCESS);
	}

	public String getActiveDirectoryDefaultGroupAdmin() {
		return config.getString(AD_DEFAULT_GROUP_ADMIN);
	}

	public String getActiveDirectoryOrganizationUnitGroupsLaboName() {
		return config.getString(AD_OU_GROUPS_LABO_NAME);
	}
	
	public String getActiveDirectoryGroupsNameRegex(){
		return config.getString(AD_GROUP_REGEX);
	}
	
	public String getActiveDirectoryGroupType() {
		return config.getString(AD_GROUP_TYPE);
	}

	// AD IBFJ

	public String getActiveDirectoryIbfjPassword() {
		return config.getString(AD_IBFJ_PASSWORD);
	}

	public String getActiveDirectoryIbfjOrganisationUnit() {
		return config.getString(AD_IBFJ_ORGANIZATION_UNIT);
	}

	public String getActiveDirectoryIbfjServer() {
		return config.getString(AD_IBFJ_SERVER);
	}

	public String getActiveDirectoryIbfjUserName() {
		return config.getString(AD_IBFJ_USER_NAME);
	}

	public String getActiveDirectoryIbfjDomainComponent() {
		return config.getString(AD_IBFJ_DOMAIN_COMPONENT);
	}

	public String getActiveDirectoryIbfjGroupType() {
		return config.getString(AD_IBFJ_GROUP_TYPE);
	}

	public Integer getActiveDirectoryIbfjGIDNumberMin() {
		return Integer.parseInt(config.getString(AD_IBFJ_GID_NUMBER_MIN));
	}

	public Integer getActiveDirectoryIbfjGIDNumberMax() {
		return Integer.parseInt(config.getString(AD_IBFJ_GID_NUMBER_MAX));
	}

	public String getActiveDirectoryIbfjMsfuNisDomain() {
		return config.getString(AD_IBFJ_MSFU_NIS_DOMAIN);
	}

	public String getActiveDirectoryIbfjOrganizationUnitGroupsLaboName() {
		return config.getString(AD_IBFJ_OU_GROUPS_LABO_NAME);
	}

	public String getActiveDirectoryIbfjDefaultGroupAccess() {
		return config.getString(AD_IBFJ_DEFAULT_GROUP_ACCESS);
	}

	public String getActiveDirectoryIbfjDefaultGroupAdmin() {
		return config.getString(AD_IBFJ_DEFAULT_GROUP_ADMIN);
	}

	public boolean isADIBFJEnabled() {
		return config.getBoolean(AD_IBFJ_ENABLED, false);
	}

	public boolean isADOriginalEnabled() {
		return config.getBoolean(AD_ORIGINAL_ENABLED, false);
	}



}
