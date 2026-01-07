package services.projects.members;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import javax.inject.*;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;
import javax.naming.ldap.*;

import fr.cea.ig.ngl.NGLConfig;

/**
 * Service pour appeler l'AD historique (CNS / CNRGH).
 * 
 * @see ActiveDirectoryServicesIbfj Qui est le service permettant d'appeler l'autre AD.
 */
@Singleton
public class ActiveDirectoryServicesOriginal extends ActiveDirectoryServices {

	@Inject
	public ActiveDirectoryServicesOriginal(NGLConfig config) throws NamingException {
		super(config);
	}

	@Override
	public InitialLdapContext getActiveDirectoryContext() throws NamingException {
		Hashtable<String, Object> env = new Hashtable<>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, CONF_INITIAL_CONTEXT_FACTORY);
		env.put(Context.PROVIDER_URL,            config.getActiveDirectoryServer());
		env.put(Context.SECURITY_AUTHENTICATION, CONF_SECURITY_AUTHENTICATION);
		env.put(Context.SECURITY_PRINCIPAL,      config.getActiveDirectoryUserName());
		env.put(Context.SECURITY_CREDENTIALS,    config.getActiveDirectoryPassword());
		env.put(Context.REFERRAL,          "follow");

		env.put("java.naming.ldap.attributes.binary", CONF_JAVA_NAMING_LDAP_ATTRIBUTES_BINARY);

		InitialLdapContext ctx = new InitialLdapContext(env, null);

		try {
			ctx.setRequestControls(new Control[] { new PagedResultsControl(PAGINATION_SIZE, Control.CRITICAL) });
		} catch (IOException e) {
			throw new NamingException("Erreur lors de la configuration de la pagination dans la recherche. Cause : " + e.getMessage());
		}
					
		return ctx;
	}

	public void createGroup(String groupName, boolean unixGroup, int gidChoosen) throws NamingException {
		Attributes attributes = new BasicAttributes();
		attributes.put("objectClass", "group");
		attributes.put("samAccountName",groupName);
		attributes.put("cn",groupName);
		attributes.put("groupType",config.getActiveDirectoryGroupType());

		if (unixGroup) {
			attributes.put("gidNumber",String.valueOf(gidChoosen));
			attributes.put("description",String.valueOf(gidChoosen));
		}

		attributes.put("msSFU30NisDomain",config.getActiveDirectoryMsfuNisDomain());

		String group = "CN="+groupName+","+config.getActiveDirectoryOrganisationUnit()+","+config.getActiveDirectoryDomainComponent();
		
		getActiveDirectoryContext().createSubcontext(group, attributes).close();
	}
	
	@Override
	public void createGroup(String groupName, boolean unixGroup) throws NamingException	{
		// Create attributes group
		Attributes attributes = new BasicAttributes();
		attributes.put("objectClass", "group");
		attributes.put("samAccountName",groupName);
		attributes.put("cn",groupName);

		// Universal group
		attributes.put("groupType",config.getActiveDirectoryGroupType());

		if (unixGroup) {
			int gidNumber = getGIDAvalaible(config.getActiveDirectoryGIDNumberMin(), config.getActiveDirectoryGIDNumberMax());
			attributes.put("gidNumber", String.valueOf(gidNumber));
			attributes.put("description", String.valueOf(gidNumber));
		}

		attributes.put("msSFU30NisDomain",config.getActiveDirectoryMsfuNisDomain());

		String group = "CN="+groupName+","+config.getActiveDirectoryOrganisationUnit()+","+config.getActiveDirectoryDomainComponent();

		getActiveDirectoryContext().createSubcontext(group, attributes).close();
	}

	@Override
	public NamingEnumeration<SearchResult> getResults(String searchFilter) throws NamingException {
		// Determine le scope de la recherche
		SearchControls searchControls = new SearchControls();
		// OBJECT_SCOPE   : recherche l'objet selon texte
		// ONELEVEL_SCOPE : recherhe sur un seul niveau du context
		// SUBTREE_SCOPE  : recherhe sur la totalité de l'arbre de l'objet
		searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);

		return doWithContext((InitialLdapContext context) -> context.search(config.getActiveDirectoryDomainComponent(), searchFilter, searchControls));
	}

	@Override
	public NamingEnumeration<SearchResult> getResults(String searchFilter, String ou) throws NamingException {
		// Determine le scope de la recherche
		SearchControls searchControls = new SearchControls();
		// OBJECT_SCOPE   : recherche l'objet selon texte
		// ONELEVEL_SCOPE : recherhe sur un seul niveau du context
		// SUBTREE_SCOPE  : recherhe sur la totalité de l'arbre de l'objet
		searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);

		return doWithContext((InitialLdapContext context) -> context.search(ou + "," + config.getActiveDirectoryDomainComponent(), searchFilter, searchControls));
	}

	@Override
	public List<SearchResult> getResults(String searchFilter, String attributeName, boolean withCookie) throws NamingException {
		List<SearchResult> resResults = new ArrayList<SearchResult>();

		// Determine le scope de la recherche
		SearchControls searchControls = new SearchControls();
		// OBJECT_SCOPE   : recherche l'objet selon texte
		// ONELEVEL_SCOPE : recherhe sur un seul niveau du context
		// SUBTREE_SCOPE  : recherhe sur la totalité de l'arbre de l'objet
		searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);

		return doWithContext((InitialLdapContext context) -> {
			NamingEnumeration<SearchResult> namingRes = context.search(config.getActiveDirectoryDomainComponent(), searchFilter, searchControls);

			while(namingRes.hasMore()) {
				resResults.add(namingRes.next());
			}

			Control[] controls = context.getResponseControls();
			byte[] cookie = null;

			if (controls != null) {
				for (int i = 0; i < controls.length; i++) {
					if (controls[i] instanceof PagedResultsResponseControl) {
						PagedResultsResponseControl prrc = (PagedResultsResponseControl) controls[i];
						cookie = prrc.getCookie();
					} 
				}

				while (cookie != null) {
					try {
						context.setRequestControls(new Control[]{ new PagedResultsControl(PAGINATION_SIZE, cookie, Control.CRITICAL) });
					} catch (IOException e) {
						e.printStackTrace();
							
						throw new NamingException("Erreur lors de la configuration de la pagination dans la recherche.");
					}

					namingRes = context.search(config.getActiveDirectoryDomainComponent(), searchFilter, searchControls);

					while(namingRes.hasMore()) {
						resResults.add(namingRes.next());
					}
		
					controls = context.getResponseControls();
					cookie = null;
					
					if (controls != null) {
						for (int i = 0; i < controls.length; i++) {
							if (controls[i] instanceof PagedResultsResponseControl) {
								PagedResultsResponseControl prrc = (PagedResultsResponseControl) controls[i];
		
								cookie = prrc.getCookie();
							} 
						}
					} 
				}	
			} 

			return resResults;
		});
	}

	@Override
	public List<Map<String, String>> getMembersAllProjectAdmin(List<String> attributeNames) throws NamingException {
		String DNGroup = "CN=" + config.getString("ad.default.group.admin") + "," + config.getActiveDirectoryOrganisationUnit() + "," + config.getActiveDirectoryIbfjDomainComponent();
		return searchValues("(&(objectClass=user)(MemberOf=" + DNGroup + "))", attributeNames);
	}

	@Override
	public List<String> getGIDAvalaibleList(Integer minThreshold, Integer maxThreshold) throws NamingException {
		final List<String> gidInAD = searchValues("(&(objectClass=group)(gidNumber>=" + minThreshold + ")(gidNumber<=" + maxThreshold + "))", "gidNumber");

		List<String> gidListFromTreshold = getGIDListFromThreshold(minThreshold, maxThreshold);

		List<String> differences = gidListFromTreshold.stream()
            .filter(element -> !gidInAD.contains(element))
            .collect(Collectors.toList());

		if (differences.isEmpty()) {
			throw new NamingException("No GID available.");
		}
		
		return differences;
	}
}