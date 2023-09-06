package services.projects.members;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.naming.Context;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import fr.cea.ig.ngl.NGLConfig;
import fr.cea.ig.ngl.dao.IServicesDAO;
import fr.cea.ig.util.function.F1;

/**
 * Services to query and modify Active Directory.
 * 
 * @author ejacoby
 *
 */
@Singleton
public class ActiveDirectoryServices implements IServicesDAO {

	private final NGLConfig config;

	private final String CONF_SECURITY_AUTHENTICATION = "simple";
	private final String CONF_INITIAL_CONTEXT_FACTORY = "com.sun.jndi.ldap.LdapCtxFactory";
	private final String CONF_JAVA_NAMING_LDAP_ATTRIBUTES_BINARY = "objectSID";

	@Inject
	public ActiveDirectoryServices(NGLConfig config) throws NamingException {
		this.config = config;
	}

	/**
	 * Get new context to interact with AD.
	 * @return                 active directory context
	 * @throws NamingException active directory context initialization failure
	 */
	public DirContext getActiveDirectoryContext() throws NamingException {
		// Definition dans variable environement parametre connexion
		Hashtable<String, Object> env = new Hashtable<>();
		// Initial Context LDAP Connexion
		env.put(Context.INITIAL_CONTEXT_FACTORY, CONF_INITIAL_CONTEXT_FACTORY);
		env.put(Context.PROVIDER_URL,            config.getActiveDirectoryServer());
		// Authentification simple avec userName et password en clair (existe d'autres solutions mais celle-ci est plus simple)
		env.put(Context.SECURITY_AUTHENTICATION, CONF_SECURITY_AUTHENTICATION);
		env.put(Context.SECURITY_PRINCIPAL,      config.getActiveDirectoryUserName());
		env.put(Context.SECURITY_CREDENTIALS,    config.getActiveDirectoryPassword());

		// S'assurer que les objectSID soir retournés 
		// EJACOBY: voir les autres attributs possible
		env.put("java.naming.ldap.attributes.binary", CONF_JAVA_NAMING_LDAP_ATTRIBUTES_BINARY);
		// Debug erreur
		// EJACOBY: voir comment les mettre dans les logs
		// env.put("com.sun.jndi.ldap.trace.ber",System.err);
		return new InitialDirContext(env);
	}
	
	/**
	 * Create user group in AD.
	 * @param groupName        name of group to create
	 * @param unixGroup        if true generate group with property to associate UNIX directory
	 * @throws NamingException operation error
	 */
	public void createGroup(String groupName, boolean unixGroup) throws NamingException	{
		// Create attributes group
		// EJACOBY: definir les attributs des groupes
		// EJACOBY: deux types de groupes les groupes unix (definir gid number) et les groupes de droits
		Attributes attributes = new BasicAttributes();
		attributes.put("objectClass", "group");
		attributes.put("samAccountName",groupName);
		attributes.put("cn",groupName);
		//Universal group
		attributes.put("groupType",config.getActiveDirectoryGroupType());

		if (unixGroup) {
			String gidNumber = getGIDAvalaible(config.getActiveDirectoryGIDNumberMin(), config.getActiveDirectoryGIDNumberMax());
			attributes.put("gidNumber",gidNumber);
			attributes.put("description",gidNumber);
		}

		attributes.put("msSFU30NisDomain",config.getActiveDirectoryMsfuNisDomain());

		String group = "CN="+groupName+","+config.getActiveDirectoryOrganisationUnit()+","+config.getActiveDirectoryDomainComponent();

		DirContext dirContext = getActiveDirectoryContext();
		dirContext.createSubcontext(group, attributes);
		dirContext.close();
	}

	/**
	 * Group name for a project in AD. 
	 * @param codeProjet project code to create group name for
	 * @return           group name for the given project
	 */
	public String generateGroupName(String codeProjet) {
		return "g_"+codeProjet;
	}

	/**
	 * Administrator group name for a project in AD.
	 * @param codeProjet project code  
	 * @return           project administrator group name       
	 */
	public String generateAdminGroupName(String codeProjet)	{
		return "g_"+codeProjet+"_admin";
	}

	/**
	 * Add users to user group in AD.
	 * add to members of group
	 * add group to memberOf of users
	 * @param userAccountNames list user name to be added
	 * @param groupAccountName name of user group to add
	 * @throws NamingException AD operation error
	 */
	public void addListUsersToGroup(List<String> userAccountNames, String groupAccountName) throws NamingException {
		for (String userAccountName : userAccountNames) 
			addUserToGroup(userAccountName, groupAccountName);
	}

	/**
	 * Add one user to user group in AD.
	 * add to members of group
	 * add group to memberOf of user
	 * @param userAccountName  user name to be added
	 * @param groupAccountName name of user group to add
	 * @throws NamingException operation error
	 */
	public void addUserToGroup(String userAccountName, String groupAccountName) throws NamingException {
		if (!isUserInGroup(userAccountName, groupAccountName)) {
			String DNUser  = getUserDN(userAccountName);
			String DNGroup = getGroupDN(groupAccountName);
			addAttribute("member", DNUser, DNGroup);
		}
	}

	/**
	 * Check if user is in a group user.
	 * @param userAccountName  user name to check
	 * @param groupAccountName group name to check
	 * @return                 true if in user group
	 * @throws NamingException AD operation error
	 */
	public boolean isUserInGroup(String userAccountName, String groupAccountName) throws NamingException {
		return getMembers(groupAccountName).contains(userAccountName);
	}
	
	/**
	 * Add groupName member to parentGroupName.
	 * @param parentGroupName  group name to add member group (memberOf=groupName)
	 * @param groupName        group name to add parent group to members list (members=parentGroupName)
	 * @throws NamingException AD operation error
	 */
	public void addUsersGroupToGroup(String parentGroupName, String groupName) throws NamingException {
		if (!isGroupUserInGroup(parentGroupName, groupName)) {
			String DNParentGroup = getGroupDN(parentGroupName);
			String DNGroup       = getGroupDN(groupName);
			addAttribute("member", DNParentGroup, DNGroup);
		}
	}

	/**
	 * Check if a userGroup is member of another user group.
	 * @param groupUserAccountName       user group name to check member of groupUserParentAccountName
	 * @param groupUserParentAccountName parent user group name
	 * @return                           true if groupUserAccountName is member of groupUserParentAccountName
	 * @throws NamingException           AD operation error
	 */
	public boolean isGroupUserInGroup(String groupUserAccountName, String groupUserParentAccountName) throws NamingException {
		return getGroupMembers(groupUserParentAccountName).contains(groupUserAccountName);
	}

	/**
	 * Find user in AD by accountName (usually login).
	 * @param accountName      usually login
	 * @return                 AD result in SearchResult object
	 * @throws NamingException AD operation error
	 */
	public SearchResult findUserAccountByAccountName(String accountName) throws NamingException {
		return search("(&(objectClass=user)(sAMAccountName=" + accountName + "))");
	}

	/**
	 * Find user group in AD by accountName.
	 * @param accountName      user group name
	 * @return                 AD result in SearchResult object
	 * @throws NamingException AD operation error
	 */
	public SearchResult findGroupAccountByAccountName(String accountName) throws NamingException {
		return search("(&(objectClass=group)(sAMAccountName=" + accountName + "))");
	}

	/**
	 * Get list of members user name (login) from user group.
	 * @param groupName        user group name 
	 * @return                 list of user name (usually login)
	 * @throws NamingException AD operation error
	 */
	public List<String> getMembers(String groupName) throws NamingException {
		String DNGroup = getGroupDN(groupName);
		return searchValues("(&(objectClass=user)(MemberOf=" + DNGroup + "))", "sAMAccountName");
	}
	
	public List<Map<String, String>> getMembers(String groupName, List<String> attributeNames) throws NamingException {
		String DNGroup = getGroupDN(groupName);
		return searchValues("(&(objectClass=user)(MemberOf=" + DNGroup + "))", attributeNames);
	}

	public List<Map<String, String>> getMembersAllProjectAdmin(List<String> attributeNames) throws NamingException {
		String DNGroup = "CN=" + config.getString("ad.default.group.admin") + ",OU=Rights,OU=UserGroups,OU=UsersAccounts,OU=CNRGH,DC=ad-cng,DC=cng,DC=fr";
		return searchValues("(&(objectClass=user)(MemberOf=" + DNGroup + "))", attributeNames);
	}
	
	/**
	 * Get list of members user name (login) from organization unit.
	 * @param ouName           organization unit name
	 * @return                 list of user name (usually login)
	 * @throws NamingException AD operation error
	 */
	public List<String> getMembersByOrganizationUnit(String ouName) throws NamingException {
		return searchValues("(objectClass=user)", "sAMAccountName", ouName);
	}

	/**
	 * Get list of attributes members from organization unit.
	 * @param ouName           organization unit name
	 * @param attributeNames   list of attributes names to get back
	 * @return                 list of attributes with key is the attribute name and value the attribute value
	 * @throws NamingException AD operation error
	 */
	public List<Map<String, String>> getMembersByOrganizationUnit(String ouName, List<String> attributeNames) throws NamingException {
		return searchValues("(objectClass=user)",attributeNames, ouName);
	}
	
	/**
	 * Get user group member of user group.
	 * @param groupName        user group name 
	 * @return                 list of user group name
	 * @throws NamingException AD operation error
	 */
	public List<String> getGroupMembers(String groupName) throws NamingException {
		String DNGroup = getGroupDN(groupName);
		return searchValues("(&(objectClass=group)(MemberOf=" + DNGroup + "))", "sAMAccountName");
	}
	
	/**
	 * Get all group from OU name.
	 * @param ouName           Organization Unit name 
	 * @return                 list of group name
	 * @throws NamingException AD operation error
	 */
	public List<String> getGroups(String ouName) throws NamingException {
		return searchValues("(objectClass=group)", "sAMAccountName", ouName);
	}
	
	
	/**
	 * Get all OU inside another OU.
	 * @param ouName           Organization Unit name 
	 * @return                 list of group name
	 * @throws NamingException AD operation error
	 */
	public List<String> getOrganizationUnits(String ouName) throws NamingException {
		return searchValues("(objectClass=organizationalUnit)", "Name", ouName);
	}

	/**
	 * Get all user in AD.
	 * @return                 list of user name
	 * @throws NamingException AD operation error
	 */
	public List<String> getAllMembers() throws NamingException {
		return searchValues( "(&(objectClass=user))","sAMAccountName");
	}

	/**
	 * Get users not in user group.
	 * @param groupName        user group name
	 * @return                 list of user name
	 * @throws NamingException AD operation error
	 */
	public List<String> getMembersNotInGroup(String groupName) throws NamingException {
		String DNGroup = getGroupDN(groupName);
		return searchValues("(&(objectClass=user)(!(MemberOf=" + DNGroup + ")))", "sAMAccountName");
	}

	/**
	 * Get user group member of user.
	 * @param login            user name
	 * @return                 list of user group name
	 * @throws NamingException AD operation error
	 */
	public List<String> getUserGroupMembers(String login) throws NamingException {
		return convertCNFromDN(searchValues("(&(objectClass=user)(sAMAccountName=" + login + "))", "MemberOf"));
	}
	
	/**
	 * Delete user group in AD.
	 * @param name             name of group to delete
	 * @throws NamingException AD operation error
	 */
	public void deleteGroup(String name) throws NamingException {
		try {
			DirContext dirContext = getActiveDirectoryContext();
			dirContext.destroySubcontext(createGroupDN(name));
			dirContext.close();
		} catch (NameNotFoundException e) {
			// EJACOBY: properly handle errors
			e.printStackTrace();
		}
	}

	/**
	 * Delete user group from member of user (do not delete the user group).
	 * @param userName  user name 
	 * @param groupName user group name to delete from list of member of user
	 */
	public void removeGroupFromUser(String userName, String groupName) {
		try {
			String DNUser  = getUserDN(userName);
			String DNGroup = getGroupDN(groupName);
			removeAttribute("member", DNUser, DNGroup);
		} catch (NamingException e) {
			// EJACOBY: properly handle errors
			e.printStackTrace(); 
		} catch (Exception e) {
			// EJACOBY: properly handle errors
		}
	}

	/**
	 * Get new GID number available in AD according to threshold in configuration.
	 * GID number is associated to UNIX user group.
	 * @param minThreshold     GID number must be greater than minThreshold
	 * @param maxThreshold     GID number must be less than maxThreshold
	 * @return                 GID
	 * @throws NamingException GID generation error
	 */
	public String getGIDAvalaible(Integer minThreshold, Integer maxThreshold) throws NamingException {
		List<String> gidInAD = searchValues("(&(objectClass=group)(gidNumber>=" + minThreshold + ")(gidNumber<=" + maxThreshold + "))", "gidNumber");
		for (int i=minThreshold; i<maxThreshold; i++) {
			if (!gidInAD.contains("" + i))
				return "" + i;
		}
		// EJACOBY: créer exception specifique
		throw new NamingException("No GID avalaible"); 
	}

	/**
	 * Searching specific values in AD.
	 * @param searchFilter     expression to filter values in AD
	 * @param attributeName    getting attribute name from search result 
	 * @return                 list of values attributes 
	 * @throws NamingException AD operation error
	 */
	public List<String> searchValues(String searchFilter, String attributeName) throws NamingException {
		List<String> resultValues = new ArrayList<>();
		NamingEnumeration<SearchResult> results = getResults(searchFilter);
		while (results.hasMoreElements()) {
			SearchResult result = results.nextElement();
			resultValues.add((String)result.getAttributes().get(attributeName).get());
		}
		return resultValues;
	}
	
	public List<Map<String, String>> searchValues(String searchFilter, List<String> attributeNames) throws NamingException {
		List<Map<String,String>> resultValues = new ArrayList<>();

		NamingEnumeration<SearchResult> results = getResults(searchFilter);
		while(results.hasMoreElements()){
			SearchResult result = results.nextElement();
			Map<String, String> mapValues = new HashMap<>();
			for(String attributeName:attributeNames){
				if(result.getAttributes().get(attributeName)!=null)
					mapValues.put(attributeName, (String)result.getAttributes().get(attributeName).get());
				else
					mapValues.put(attributeName, null);
			}
			resultValues.add(mapValues);
		}
		return resultValues;
	}
	
	/**
	 * Searching specific values in AD.
	 * @param ou searchBase    organization unit
	 * @param searchFilter     expression to filter values in AD
	 * @param attributeName    getting attribute name from search result 
	 * @return                 list of values attributes 
	 * @throws NamingException AD error
	 */
	public List<String> searchValues(String searchFilter, String attributeName, String ou) throws NamingException {
		List<String> resultValues = new ArrayList<>();
		NamingEnumeration<SearchResult> results = getResults(searchFilter,ou);
		while (results.hasMoreElements()) {
			SearchResult result = results.nextElement();
			resultValues.add((String)result.getAttributes().get(attributeName).get());
		}
		return resultValues;
	}
	
	/**
	 * Searching list of values by name in AD.
	 * @param searchFilter     expression to filter values in AD
	 * @param attributeNames   getting attribute names from search result
	 * @param ou               searchBase organization unit
	 * @return                 list of key values attributes
	 * @throws NamingException AD error
	 */
	public List<Map<String, String>> searchValues(String searchFilter, List<String> attributeNames, String ou) throws NamingException {
		List<Map<String, String>> resultValues = new ArrayList<>();
		NamingEnumeration<SearchResult> results = getResults(searchFilter,ou);
		while (results.hasMoreElements()) {
			SearchResult result = results.nextElement();
			Map<String, String> mapValues = new HashMap<>();
			for (String attributeName : attributeNames) {
				if (result.getAttributes().get(attributeName) != null)
					mapValues.put(attributeName, (String)result.getAttributes().get(attributeName).get());
				else
					mapValues.put(attributeName, null);
			}
			resultValues.add(mapValues);
		}
		return resultValues;
	}

	/**
	 * Search values in AD.
	 * @param searchFilter     expression to filter values in AD
	 * @return                 result of searching in SearchResult object
	 * @throws NamingException AD error
	 */
	private SearchResult search(String searchFilter) throws NamingException	{
		NamingEnumeration<SearchResult> results = getResults(searchFilter);
		SearchResult searchResult = null;
		if (results.hasMoreElements()) {
			searchResult = results.nextElement();
			// Vérifie qu'il n'y a qu'un seul match avec accountName
			if (results.hasMoreElements()) {
				// EJACOBY: properly handle multiple values (probably throw an exception)
				System.err.println("Matched multiple results");
			}
		}
		return searchResult;
	}

	// EJACOBY: validate method, this is a replacement for not closed context when exceptions occur
	/**
	 * Execute an action using a temporary {@link DirContext}.
	 * @param action           action to execute
	 * @return                 action result
	 * @throws NamingException execution error
	 */
	private <A> A doWithContext(F1<DirContext,A> action) throws NamingException {
		DirContext dirContext = getActiveDirectoryContext();
		try {
			return action.apply(dirContext);
		} catch (NamingException | RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			dirContext.close();
		}
	}
	
	/**
	 * Executes a search in the AD.
	 * @param searchFilter     searchFilter expression to filter values in AD
	 * @return                 enumeration of results
	 * @throws NamingException AD error
	 */
	private NamingEnumeration<SearchResult> getResults(String searchFilter) throws NamingException {
		// Determine le scope de la recherche
		SearchControls searchControls = new SearchControls();
		// OBJECT_SCOPE   : recherche l'objet selon texte
		// ONELEVEL_SCOPE : recherhe sur un seul niveau du context
		// SUBTREE_SCOPE  : recherhe sur la totalité de l'arbre de l'objet
		searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);

		//DirContext dirContext = getActiveDirectoryContext();
		//NamingEnumeration<SearchResult> results = dirContext.search(config.getActiveDirectoryDomainComponent(), searchFilter, searchControls);
		//dirContext.close();
		//return results;
		// EJACOBY: validate call
		return doWithContext(dirContext -> dirContext.search(config.getActiveDirectoryDomainComponent(), searchFilter, searchControls));
	}
	
	/**
	 * Execute a search in AD.
	 * @param searchFilter     searchFilter expression to filter values in AD
	 * @param ou               searchBase organization unit
	 * @return                 enumeration of results
	 * @throws NamingException AD error
	 */
	private NamingEnumeration<SearchResult> getResults(String searchFilter, String ou) throws NamingException {
		// Determine le scope de la recherche
		SearchControls searchControls = new SearchControls();
		// OBJECT_SCOPE   : recherche l'objet selon texte
		// ONELEVEL_SCOPE : recherhe sur un seul niveau du context
		// SUBTREE_SCOPE  : recherhe sur la totalité de l'arbre de l'objet
		searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);

//		DirContext dirContext = getActiveDirectoryContext();
//		NamingEnumeration<SearchResult> results = dirContext.search(ou+","+config.getActiveDirectoryDomainComponent(), searchFilter, searchControls);
//		dirContext.close();
//		return results;
		return doWithContext(dirContext -> dirContext.search(ou + "," + config.getActiveDirectoryDomainComponent(), searchFilter, searchControls));
	}

	/**
	 * Get Distinguished Name (DN) of a user group. 
	 * @param name             user group name
	 * @return                 value of group DN
	 * @throws NamingException AD error
	 */
	private String getGroupDN(String name) throws NamingException {
		SearchResult resultGroup = findGroupAccountByAccountName(name);
		return getDN(resultGroup);
	}

	/**
	 * Get Distinguished Name (DN) of user name.
	 * @param name             user name
	 * @return                 value of user DN 
	 * @throws NamingException AD error
	 */
	private String getUserDN(String name) throws NamingException {
		SearchResult resultUser = findUserAccountByAccountName(name);
		return getDN(resultUser);
	}	

	/**
	 * Get DN attribute from AD Result.
	 * @param searchResult     AD result
	 * @return                 value of DN
	 * @throws NamingException AD error
	 */
	private String getDN(SearchResult searchResult) throws NamingException {
		return (String) searchResult.getAttributes().get("DistinguishedName").get();
	}
	
	/**
	 * Get Common Name (CN) in DN expression.
	 * @param DN Distinguished Name in AD
	 * @return   common name
	 */
	private String getCNFromDN(String DN) {
		Pattern pattern = Pattern.compile("CN=(.*?),");
		Matcher matcher = pattern.matcher(DN);
		if (matcher.find()) {
			return matcher.group(1);
		} else {
			return DN;
		}
	}
	
	/**
	 * Convert a list of DN to CN.
	 * @param  DN distinguished name collection to convert
	 * @return    list of converted names
	 */
	private List<String> convertCNFromDN(List<String> DN) {
		return DN.stream().map(dn->getCNFromDN(dn)).collect(Collectors.toList());
	}

	/**
	 * Create DN syntax from user group name.
	 * @param name user group name
	 * @return     user group distinguished name
	 */
	private String createGroupDN(String name) {
		return new StringBuffer()
				.append("CN=")
				.append(name)
				.append(",")
				.append(config.getActiveDirectoryOrganisationUnit())
				.append(",")
				.append(config.getActiveDirectoryDomainComponent())
				.toString();
	}

	/**
	 * Add attribute in AD object.
	 * @param attributeName    attribute name to add
	 * @param attributeValue   attribute value to add
	 * @param DNName DN name   object to add attribute
	 * @throws NamingException AD error
	 */
	private void addAttribute(String attributeName, String attributeValue, String DNName) throws NamingException {
		modifyAttribute(DirContext.ADD_ATTRIBUTE, attributeName, attributeValue, DNName);
	}

	/**
	 * Remove attribute in AD object.
	 * @param attributeName    attribute name to remove
	 * @param attributeValue   attribute value to remove
	 * @param DNName           DN name to remove attribute
	 * @throws NamingException AD error
	 */
	private void removeAttribute(String attributeName, String attributeValue, String DNName) throws NamingException {
		modifyAttribute(DirContext.REMOVE_ATTRIBUTE, attributeName, attributeValue, DNName);
	}

	/**
	 * Modify attribute in AD object.
	 * @param action           modify action (ADD, REMOVE)
	 * @param attributeName    attribute name to modify
	 * @param attributeValue   attribute value to modify
	 * @param DNName           DN name to modify attribute
	 * @throws NamingException AD error
	 */
	private void modifyAttribute(int action, String attributeName, String attributeValue, String DNName) throws NamingException {
//		ModificationItem[] mods = new ModificationItem[1];
//		Attribute mod = new BasicAttribute(attributeName,attributeValue);
//		mods[0] = new ModificationItem(action, mod);
//		DirContext dirContext = getActiveDirectoryContext();
//		dirContext.modifyAttributes(DNName, mods);
//		dirContext.close();
		// EJACOBY: validate rewrite
		ModificationItem[] mods = { new ModificationItem(action, new BasicAttribute(attributeName,attributeValue)) };
		doWithContext(dirContext -> {dirContext.modifyAttributes(DNName, mods); return null; });
	}

}
