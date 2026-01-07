package services.projects.members;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.naming.*;
import javax.naming.directory.*;
import javax.naming.ldap.*;

import fr.cea.ig.ngl.NGLConfig;
import fr.cea.ig.ngl.dao.IServicesDAO;
import fr.cea.ig.util.function.F1;
import play.Logger;

/**
 * Classe abstraite permettant de définir le comportement que vont avoir les services permettant de manipuler l'AD.
 * Il en existe deux à date (AD IBFJ et historique).
 * Les classes héritant de cette classe vont pouvoir définir des méthodes propres aux AD (comment créer un groupe, par exemple)
 * et cette classe permet de définir les comportements communs (comment générer un nom de groupe à partir d'un code projet, par exemple).
 * 
 * @see ActiveDirectoryServicesIbfj Pour le service permettant de manipuler l'AD IBFJ.
 * @see ActiveDirectoryServicesOriginal Pour le service permettant de manipuler l'AD historique.
 */
public abstract class ActiveDirectoryServices implements IServicesDAO {

	protected final NGLConfig config;

	protected final String CONF_SECURITY_AUTHENTICATION = "simple";

	protected final String CONF_INITIAL_CONTEXT_FACTORY = "com.sun.jndi.ldap.LdapCtxFactory";

	protected final String CONF_JAVA_NAMING_LDAP_ATTRIBUTES_BINARY = "objectSID";

	// Il est possible de changer cette valeur. On a mis 1 000 car c'est ce que l'AD IBFJ a comme valeur, mais ça peut être décorrélé comme valeur.
	public final int PAGINATION_SIZE = 1000;
	
	@Inject
	public ActiveDirectoryServices(NGLConfig config) throws NamingException {
		this.config = config;
	}

	/**
	 * Méthode permettant de récupérer une instance du LdapContext.
	 * Au premier appel, l'objet sera initialisé, sinon on garde la même instance à chaque fois.
	 * 
	 * @return Une instance d'un objet InitialLdapContext pointant sur l'AD.
	 * 
	 * @throws NamingException Si une erreur dans l'initialisation arrive. 
	 * Attention cette exception peut englober plusieurs erreurs.
	 * Voir https://docs.oracle.com/javase%2Ftutorial%2F/jndi/ldap/exceptions.html pour plus d'infos.
	 */
	public abstract InitialLdapContext getActiveDirectoryContext() throws NamingException;
	
	/**
	 * Méthode permettant de créer un groupe dans l'AD.
	 * 
	 * @param groupName Le nom du groupe à créer (déjà sous la forme g_NAME ou g_NAME_admin).
	 * @param unixGroup Si <b>true</b>, génère des attributs UNIX différents sur le groupe à créer.
	 * 
	 * @throws NamingException Si une erreur pendant la création arrive.
	 * Attention cette exception peut englober plusieurs erreurs.
	 * Voir https://docs.oracle.com/javase%2Ftutorial%2F/jndi/ldap/exceptions.html pour plus d'infos.
	 */
	public abstract void createGroup(String groupName, boolean unixGroup) throws NamingException;

	/**
	 * Méthode permettant de 
	 * 
	 * @param gidNumber Le GID à chercher dans l'AD.
	 * 
	 * @return <b>true</b> si le GID cherché est disponible. 
	 * <b>false</b> sinon.
	 * 
	 * @throws NamingException Si une erreur pendant la création arrive.
	 * Attention cette exception peut englober plusieurs erreurs.
	 * Voir https://docs.oracle.com/javase%2Ftutorial%2F/jndi/ldap/exceptions.html pour plus d'infos.
	 */
	public boolean isGIDAvailable(int gidNumber) throws NamingException {
		List<String> results = searchValues("(&(objectClass=group)(gidNumber=" + gidNumber + "))", "gidNumber");

		// Si results est vide, on n'a pas de résultat à notre recherche, donc le GID n'est pas utilisé.
		return results.isEmpty();
	}

	/**
	 * Méthode permettant de générer un nom de groupe UNIX ('g_PROJECT_NAME') à partir d'un code projet NGL.
	 * 
	 * @param codeProjet Le code projet en entrée.
	 * @return Une chaîne de caractères au format 'g_PROJECT_NAME'.
	 */
	public String generateGroupName(String codeProjet) {
		String prefixe = "p_";

		return prefixe + codeProjet;
	}

	/**
	 * Méthode permettant de générer un nom de groupe UNIX admin ('g_PROJECT_NAME_admin') à partir d'un code projet NGL.
	 * 
	 * @param codeProjet Le code projet en entrée.
	 * @return Une chaîne de caractères au format 'g_PROJECT_NAME_admin'.
	 */
	public String generateAdminGroupName(String codeProjet)	{
		String prefixe = "p_";

		return prefixe + codeProjet+"_admin";
	}

	/**
	 * Méthode permettant d'ajouter un utilisateur à un groupe dans l'AD.
	 * 
	 * @param userAccountName L'utilisateur à ajouter.
	 * @param groupAccountName Le groupe où on veut ajouter l'utilisateur.
	 * 
	 * @throws NamingException Si une erreur pendant l'ajout arrive.
	 * Attention cette exception peut englober plusieurs erreurs.
	 * Voir https://docs.oracle.com/javase%2Ftutorial%2F/jndi/ldap/exceptions.html pour plus d'infos.
	 */
	public void addUserToGroup(String userAccountName, String groupAccountName) throws NamingException {
		if (!isUserInGroup(userAccountName, groupAccountName)) {
			String DNUser  = getUserDN(userAccountName);
			String DNGroup = getGroupDN(groupAccountName);

			addAttribute("member", DNUser, DNGroup);
		}
	}

	/**
	 * Méthode permettant de vérifier si un utilisateur est dans un groupe (ou non).
	 * 
	 * @param userAccountName Le login de l'utilisateur à vérifier.
	 * @param groupAccountName Le groupe à vérifier.
	 * 
	 * @return <b>true</b> si l'utilisateur est dans le groupe donné.
	 * <b>false</b> sinon.
	 * 
	 * @throws NamingException Si une erreur pendant l'ajout arrive.
	 * Attention cette exception peut englober plusieurs erreurs.
	 * Voir https://docs.oracle.com/javase%2Ftutorial%2F/jndi/ldap/exceptions.html pour plus d'infos.
	 */
	public boolean isUserInGroup(String userAccountName, String groupAccountName) throws NamingException {
		return getMembers(groupAccountName).contains(userAccountName);
	}

	/**
	 * Méthode permettant de chercher un utilisateur par un accountname.
	 * 
	 * @param accountName Le login de l'utilisateur cherché.
	 * 
	 * @return Le résultat de la recherche dans un objet "SearchResult".
	 * 
	 * @throws NamingException Si une erreur pendant la recherche arrive.
	 * Attention cette exception peut englober plusieurs erreurs.
	 * Voir https://docs.oracle.com/javase%2Ftutorial%2F/jndi/ldap/exceptions.html pour plus d'infos.
	 */
	public SearchResult findUserAccountByAccountName(String accountName) throws NamingException {
		return search("(&(objectClass=user)(sAMAccountName=" + accountName + "))");
	}

	/**
	 * Méthode permettant de chercher un groupe dans l'AD par son accountName.
	 * 
	 * @param accountName Le nom du groupe à chercher.
	 * 
	 * @return Un résultat sous la fome d'un objet "SearchResult".
	 * 
	 * @throws NamingException Si une erreur pendant la recherche arrive.
	 * Attention cette exception peut englober plusieurs erreurs.
	 * Voir https://docs.oracle.com/javase%2Ftutorial%2F/jndi/ldap/exceptions.html pour plus d'infos.
	 */
	public SearchResult findGroupAccountByAccountName(String accountName) throws NamingException {
		return search("(&(objectClass=group)(sAMAccountName=" + accountName + "))");
	}

	/**
	 * Méthode permettant de récupérer les membres d'un groupe dans l'AD.
	 * 
	 * @param groupName Le groupe où on veut récupérer les membres.
	 * 
	 * @return Une liste de chaîne de caractères représentant les membres (login) dans le groupe cherché.
	 * 
	 * @throws NamingException Si une erreur pendant la recherche arrive.
	 * Attention cette exception peut englober plusieurs erreurs.
	 * Voir https://docs.oracle.com/javase%2Ftutorial%2F/jndi/ldap/exceptions.html pour plus d'infos.
	 */
	public List<String> getMembers(String groupName) throws NamingException {
		String DNGroup = getGroupDN(groupName);
		return searchValues("(&(objectClass=user)(MemberOf=" + DNGroup + "))", "sAMAccountName");
	}

	/**
	 * getGIDFromGroupName()
	 * 
	 * @param groupName
	 * 
	 * @return
	 * 
	 * @throws NamingException Si une erreur pendant la recherche arrive.
	 * Attention cette exception peut englober plusieurs erreurs.
	 * Voir https://docs.oracle.com/javase%2Ftutorial%2F/jndi/ldap/exceptions.html pour plus d'infos.
	 */
	public List<String> getGIDFromGroupName(String groupName) throws NamingException {
		return searchValues("(&(objectClass=group)(sAMAccountName=" + groupName + "))", "gidNumber");
	}
	
	/**
	 * Méthode permettant de récupérer les membres d'un groupe dans l'AD.
	 * La spécificité de cette méthode par rapport à celle du dessus c'est qu'on récupère les membres du groupe qu'avec les attributs donnés en paramètres.
	 * 
	 * @param groupName Le groupe où on veut récupérer les membres.
	 * @param attributeNames Les attributs des users qu'on veut spécifiquement récupérer.
	 * 
	 * @return Une liste de chaîne de caractères représentant les membres (login) dans le groupe cherché ainsi qu'un ensemble d'attributs cherchés.
	 * Le tout représenté par une Map Java.
	 * 
	 * @throws NamingException Si une erreur pendant la recherche arrive.
	 * Attention cette exception peut englober plusieurs erreurs.
	 * Voir https://docs.oracle.com/javase%2Ftutorial%2F/jndi/ldap/exceptions.html pour plus d'infos.
	 */
	public List<Map<String, String>> getMembers(String groupName, List<String> attributeNames) throws NamingException {
		String DNGroup = getGroupDN(groupName);
		return searchValues("(&(objectClass=user)(MemberOf=" + DNGroup + "))", attributeNames);
	}

	/**
	 * Méthode permettant de récupérer tous les utilisateurs qui sont par défaut admin dans l'institut.
	 * 
	 * @param attributeNames La liste des attributs qu'on veut récupérer par utilisateur.
	 * Par exemple à date on veut le "samAccountName" et le "displayName".
	 * 
	 * @return Une Map de logins utilisateurs avec les attributs recherchés.
	 * 
	 * @throws NamingException Si une erreur pendant la recherche arrive.
	 * Attention cette exception peut englober plusieurs erreurs.
	 * Voir https://docs.oracle.com/javase%2Ftutorial%2F/jndi/ldap/exceptions.html pour plus d'infos.
	 */
	public abstract List<Map<String, String>> getMembersAllProjectAdmin(List<String> attributeNames) throws NamingException;

	/**
	 * Méthode permettant de récupérer les membres d'une OU ("Organization Unit").
	 * 
	 * @param ouName L'OU que l'on cherche.
	 * @param attributeNames La liste des attributs qu'on veut récupérer pour les membres.
	 * 
	 * @return Une liste représentant les membres de l'OU cherchée, avec pour chacun les attributs cherchés.
	 * 
	 * @throws NamingException Si une erreur pendant la recherche arrive.
	 * Attention cette exception peut englober plusieurs erreurs.
	 * Voir https://docs.oracle.com/javase%2Ftutorial%2F/jndi/ldap/exceptions.html pour plus d'infos.
	 */
	public List<Map<String, String>> getMembersByOrganizationUnit(String ouName, List<String> attributeNames) throws NamingException {
		return searchValues("(objectClass=user)",attributeNames, ouName);
	}
	
	/**
	 * Méthode permettant de chercher les utilisateurs membre d'un groupe.
	 * Utile par exemple pour afficher dans NGL quels utilisateurs ont un droits d'accès au projet : on liste le contenu du groupe g_NAME.
	 * 
	 * @param groupName Le groupe cherché.
	 * 
	 * @return Une liste de login utilisateurs représentant les membres du groupe cherché.
	 * 
	 * @throws NamingException Si une erreur pendant la recherche arrive.
	 * Attention cette exception peut englober plusieurs erreurs.
	 * Voir https://docs.oracle.com/javase%2Ftutorial%2F/jndi/ldap/exceptions.html pour plus d'infos.
	 */
	public List<String> getGroupMembers(String groupName) throws NamingException {
		String DNGroup = getGroupDN(groupName);
		return searchValues("(&(objectClass=group)(MemberOf=" + DNGroup + "))", "sAMAccountName");
	}
	
	/**
	 * Méthode permettant de chercher tous les groupes contenus dans une Organization Unit.
	 * 
	 * @param ouName Le nom de l'Organization Unit où on cherche. 
	 * 
	 * @return Une liste de chaîne de caractères représentant les groupes contenus dans l'OU.
	 * 
	 * @throws NamingException Si une erreur pendant la recherche arrive.
	 * Attention cette exception peut englober plusieurs erreurs.
	 * Voir https://docs.oracle.com/javase%2Ftutorial%2F/jndi/ldap/exceptions.html pour plus d'infos.
	 */
	public List<String> getGroups(String ouName) throws NamingException {
		return searchValues("(objectClass=group)", "sAMAccountName", ouName);
	}

	/**
	 * Méthode permettant de récupérer tous les groupes auquel appartient un utilisateur.
	 * 
	 * @param login Le login du user interrogié.
	 * 
	 * @return Une liste de chaînes de caractères comprenant l'ensemble des groupes auquel l'utilisateur appartient.
	 * 
	 * @throws NamingException Si une erreur pendant la recherche arrive.
	 * Attention cette exception peut englober plusieurs erreurs.
	 * Voir https://docs.oracle.com/javase%2Ftutorial%2F/jndi/ldap/exceptions.html pour plus d'infos.
	 */
	public List<String> getUserGroupMembers(String login) throws NamingException {
		return convertCNFromDN(searchValues("(&(objectClass=user)(sAMAccountName=" + login + "))", "MemberOf"));
	}
	
	/**
	 * Méthode permettant de supprimer un groupe dans l'AD.
	 * 
	 * @param name Le nom du groupe à supprimer.
	 * 
	 * @throws NamingException Si une erreur pendant la suppression arrive.
	 * Attention cette exception peut englober plusieurs erreurs.
	 * Voir https://docs.oracle.com/javase%2Ftutorial%2F/jndi/ldap/exceptions.html pour plus d'infos.
	 */
	public void deleteGroup(String name) throws NamingException {
		doWithContext((InitialLdapContext context) -> {
			context.destroySubcontext(createGroupDN(name));
			return null;
		});
	}

	/**
	 * Méthode permettant d'enlever un utilisateur d'un groupe.
	 * 
	 * @param userName Le nom de l'utilisateur à enlever.
	 * @param groupName Le nom du groupe à mettre à jour.
	 */
	public void removeGroupFromUser(String userName, String groupName) {
		try {
			String DNUser  = getUserDN(userName);
			String DNGroup = getGroupDN(groupName);

			removeAttribute("member", DNUser, DNGroup);
		} catch (NamingException e) {
			Logger.error(userName + " was not added in group '" + groupName + "'. Cause : " + e.getMessage());
		}
	}

	protected List<String> getGIDListFromThreshold(Integer minThreshold, Integer maxThreshold) {
		List<String> res = new ArrayList<String>();

		for (int i = minThreshold; i <= maxThreshold; i++) {
			res.add(String.valueOf(i));
		}

		return res;
	}

	/**
	 * Méthode permettant de récupérer un GID pour un nouveau groupe à créer.
	 * Ce GID est cherché entre 2 seuils (min / max).
	 * Il est uniquement associé aux groupes créés avec l'option unixGroup à <b>true</b>.
	 * 
	 * @param minThreshold Le seuil min de recherche pour le GID.
	 * @param maxThreshold Le seuil max de recherche pour le GID.
	 * 
	 * @return Le GID trouvé par la méthode (entre minThreshold et maxThreshold) et disponible.
	 * 
	 * @throws NamingException Si une erreur pendant la recherche d'un nouveau GID arrive.
	 * Attention cette exception peut englober plusieurs erreurs.
	 * Voir https://docs.oracle.com/javase%2Ftutorial%2F/jndi/ldap/exceptions.html pour plus d'infos.
	 */
	public int getGIDAvalaible(Integer minThreshold, Integer maxThreshold) throws NamingException {
		List<String> gidInAD = searchValues("(&(objectClass=group)(gidNumber>=" + minThreshold + ")(gidNumber<=" + maxThreshold + "))", "gidNumber");

		for (int i = minThreshold; i <= maxThreshold; i++) {
			if (!gidInAD.contains(String.valueOf(i))) {
				return i;
			}
		}
		
		throw new NamingException("No GID available.");
	}

	public abstract List<String> getGIDAvalaibleList(Integer minThreshold, Integer maxThreshold) throws NamingException;

	public <A> A doWithContext(F1<InitialLdapContext, A> action) throws NamingException {
		InitialLdapContext dirContext = getActiveDirectoryContext();

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
	 * Méthode permettant de chercher dans l'AD selon des critères de recherche.
	 * Contrairement aux autres méthodes de recheche présentes dans ce fichier, cette méthode cherche en utilisant la pagination AD (lot de 1 000 résultats).
	 * 
	 * @param searchFilter Les différents critères de recherche.
	 * @param attributeName Le nom des attributs que l'on cherche (souvent un DN ou un CN). 
	 * 
	 * @return Les résultats de la recherche (sous la forme d'une liste de chaîne de caractères).
	 * 
	 * @throws NamingException Si une erreur pendant la recherche arrive.
	 * Attention cette exception peut englober plusieurs erreurs.
	 * Voir https://docs.oracle.com/javase%2Ftutorial%2F/jndi/ldap/exceptions.html pour plus d'infos.
	 */
	public List<String> searchValues(String searchFilter, String attributeName) throws NamingException {
		List<String> resultValues = new ArrayList<>();
		List<SearchResult> results = getResults(searchFilter, attributeName, true);

		for (int i = 0; i < results.size(); i++) {
			SearchResult result = results.get(i);
			String resultStr = (String) result.getAttributes().get(attributeName).get();
			resultValues.add(resultStr);
		}

		return resultValues;
	}
	
	/**
	 * Méthode permettant de chercher dans l'AD selon des critères de recherche.
	 * 
	 * @param searchFilter Les différents critères de recherche.
	 * @param attributeNames Le nom des attributs que l'on cherche (souvent un DN ou un CN). 
	 * 
	 * @return Les résultats de la recherche (sous la forme d'une liste comprenant les résultats avec uniquement les attributs recherchés).
	 * 
	 * @throws NamingException Si une erreur pendant la recherche arrive.
	 * Attention cette exception peut englober plusieurs erreurs.
	 * Voir https://docs.oracle.com/javase%2Ftutorial%2F/jndi/ldap/exceptions.html pour plus d'infos.
	 */
	public List<Map<String, String>> searchValues(String searchFilter, List<String> attributeNames) throws NamingException {
		List<Map<String,String>> resultValues = new ArrayList<>();
		NamingEnumeration<SearchResult> results = getResults(searchFilter);

		while(results.hasMoreElements()){
			SearchResult result = results.nextElement();
			Map<String, String> mapValues = new HashMap<>();

			for (String attributeName:attributeNames){
				if (result.getAttributes().get(attributeName) != null) {
					mapValues.put(attributeName, (String)result.getAttributes().get(attributeName).get());
				} else {
					mapValues.put(attributeName, null);
				}
			}

			resultValues.add(mapValues);
		}

		return resultValues;
	}
	
	/**
	 * Méthode permettant de chercher dans l'AD selon des critères de recherche.
	 * 
	 * @param ou L'OU où on va chercher.
	 * @param searchFilter Les différents critères de recherche.
	 * @param attributeName L'attribut à retourner pour le résultat de recherche.
	 * 
	 * @return Les résultats de la recherche (sous la forme d'une liste de chaîne de caractères). 
	 * 
	 * @throws NamingException Si une erreur pendant la recherche arrive.
	 * Attention cette exception peut englober plusieurs erreurs.
	 * Voir https://docs.oracle.com/javase%2Ftutorial%2F/jndi/ldap/exceptions.html pour plus d'infos.
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
	 * Méthode permettant de chercher dans l'AD selon des critères de recherche.
	 * 
	 * @param searchFilter Les différents critères de recherche.
	 * @param attributeNames La liste des attributs qu'on veut retourner pour les résultats de la recherche.
	 * @param ou L'OU où on va chercher.
	 * 
	 * @return Les résultats de la recherche (sous la forme d'une liste comprenant les résultats avec uniquement les attributs recherchés).
	 * 
	 * @throws NamingException Si une erreur pendant la recherche arrive.
	 * Attention cette exception peut englober plusieurs erreurs.
	 * Voir https://docs.oracle.com/javase%2Ftutorial%2F/jndi/ldap/exceptions.html pour plus d'infos.
	 */
	public List<Map<String, String>> searchValues(String searchFilter, List<String> attributeNames, String ou) throws NamingException {
		List<Map<String, String>> resultValues = new ArrayList<>();
		NamingEnumeration<SearchResult> results = getResults(searchFilter,ou);

		while (results.hasMoreElements()) {
			SearchResult result = results.nextElement();
			Map<String, String> mapValues = new HashMap<>();

			for (String attributeName : attributeNames) {
				if (result.getAttributes().get(attributeName) != null) {
					mapValues.put(attributeName, (String)result.getAttributes().get(attributeName).get());
				} else {
					mapValues.put(attributeName, null);
				}
			}

			resultValues.add(mapValues);
		}

		return resultValues;
	}

	/**
	 * Méthode permettant de chercher dans l'AD un résultat selon un critère de recherche.
	 * Cette méthode est censée retourner un seul résultat, une exception est retournée si plus d'un résultat est retourné.
	 * 
	 * @param searchFilter Le critère de recherche.
	 * 
	 * @return Le résultat de la recherche sous la forme d'un objet "SearchResult".
	 * 
	 * @throws NamingException Si une erreur pendant la recherche arrive.
	 * Attention cette exception peut englober plusieurs erreurs.
	 * Voir https://docs.oracle.com/javase%2Ftutorial%2F/jndi/ldap/exceptions.html pour plus d'infos.
	 */
	private SearchResult search(String searchFilter) throws NamingException	{
		NamingEnumeration<SearchResult> results = getResults(searchFilter);
		SearchResult searchResult = null;

		if (results.hasMoreElements()) {
			searchResult = results.nextElement();

			// Vérifie qu'il n'y a qu'un seul match avec "accountName".
			if (results.hasMoreElements()) {
				throw new NamingException("Matched multiple results for search filter : " + searchFilter);
			}
		}

		return searchResult;
	}
	
	/**
	 * Méthode permettant de récupérer les résultats d'une recherche dans l'AD selon des critères de recherche.
	 * 
	 * @param searchFilter Les différents critères de recherche.
	 * 
	 * @return Une liste de résultats de la recherche sous la forme d'une énumération d'objet "SearchResult".
	 * 
	 * @throws NamingException Si une erreur pendant la recherche arrive.
	 * Attention cette exception peut englober plusieurs erreurs.
	 * Voir https://docs.oracle.com/javase%2Ftutorial%2F/jndi/ldap/exceptions.html pour plus d'infos.
	 */
	public abstract NamingEnumeration<SearchResult> getResults(String searchFilter) throws NamingException;
	
	/**
	 * Méthode permettant de récupérer les résultats d'une recherche dans l'AD selon des critères de recherche.
	 * Contrairement à d'autres méthodes, celle-ci va chercher les résultats dans une "OU" spécifique.
	 * 
	 * @param searchFilter Les différents critères de recherche.
	 * @param ou L'OU où on va chercher les résultats.
	 * 
	 * @return Une liste de résultats de la recherche sous la forme d'une énumération d'objet "SearchResult".
	 * 
	 * @throws NamingException Si une erreur pendant la recherche arrive.
	 * Attention cette exception peut englober plusieurs erreurs.
	 * Voir https://docs.oracle.com/javase%2Ftutorial%2F/jndi/ldap/exceptions.html pour plus d'infos.
	 */
	public abstract NamingEnumeration<SearchResult> getResults(String searchFilter, String ou) throws NamingException;

	/**
	 * Méthode permettant de récupérer les résultats d'une recherche dans l'AD selon des critères de recherche.
	 * 
	 * La subtilité de cette méthode est qu'elle prend un cookie en entrée, car elle gère la pagination.
	 * Le cookie permettant ici de 'savoir' où la méthode en est dans le retour des résultats. 
	 * 
	 * @param searchFilter Les différents critères de recherche.
	 * @param attributeName
	 * @param withCookie
	 * 
	 * @return Une liste de résultats de la recherche sous la forme d'une énumération d'objet "SearchResult".
	 * 
	 * @throws NamingException Si une erreur pendant la recherche arrive.
	 * Attention cette exception peut englober plusieurs erreurs.
	 * Voir https://docs.oracle.com/javase%2Ftutorial%2F/jndi/ldap/exceptions.html pour plus d'infos.
	 */
	public abstract List<SearchResult> getResults(String searchFilter, String attributeName, boolean withCookie) throws NamingException;

	/**
	 * Méthode permettant de récupérer le Distinguished Name (DN) d'un groupe.
	 * 
	 * @param name Le groupe où on veut récupérer le DN.
	 * 
	 * @return La valeur du DN du groupe spécifié.
	 * 
	 * @throws NamingException Si une erreur pendant la recherche arrive.
	 * Attention cette exception peut englober plusieurs erreurs.
	 * Voir https://docs.oracle.com/javase%2Ftutorial%2F/jndi/ldap/exceptions.html pour plus d'infos.
	 */
	private String getGroupDN(String name) throws NamingException {
		SearchResult resultGroup = findGroupAccountByAccountName(name);
		return getDN(resultGroup, name);
	}

	/**
	 * Méthode permettant de récupérer le "Distinguished Name" (DN) d'un login.
	 * 
	 * @param name Le login où on veut travailler.
	 * 
	 * @return Le "Distinguished Name" du login cherché.
	 * 
	 * @throws NamingException Si une erreur pendant la recherche arrive.
	 * Attention cette exception peut englober plusieurs erreurs.
	 * Voir https://docs.oracle.com/javase%2Ftutorial%2F/jndi/ldap/exceptions.html pour plus d'infos.
	 */
	private String getUserDN(String name) throws NamingException {
		SearchResult resultUser = findUserAccountByAccountName(name);
		return getDN(resultUser, name);
	}	

	/**
	 * Méthode permettant de récupérer le Distinguished Name (DN) d'un résultat de recherche.
	 * 
	 * @param searchResult Le résultat de recherche où on cherche le DN.
	 * 
	 * @return La valeur du DN du résultat de recherche (objet "SearchResult").
	 * 
	 * @throws NamingException Si une erreur pendant la recherche arrive.
	 * Attention cette exception peut englober plusieurs erreurs.
	 * Voir https://docs.oracle.com/javase%2Ftutorial%2F/jndi/ldap/exceptions.html pour plus d'infos.
	 */
	private String getDN(SearchResult searchResult, String name) throws NamingException {
		if (searchResult != null) {
			return (String) searchResult.getAttributes().get("DistinguishedName").get();
		}
		
		throw new NamingException("Group : " + name + " not found.");
	}
	
	/**
	 * Méthode permettant de récupérer le "Common Name" (CN) à partir d'un DN.
	 * 
	 * @param DN Le "Distinguished Name" à transformer.
	 * 
	 * @return Le CN récupéré.
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
	 * Méthode permettant de 'transformer' des "Common Name" (CN) en "Distinguished Name" (DN).
	 * 
	 * @param DN La liste des DN à transformer.
	 * 
	 * @return Une liste de chaîne de caractères représentant les CN en entrée, transformés.
	 */
	private List<String> convertCNFromDN(List<String> DN) {
		return DN.stream().map(dn->getCNFromDN(dn)).collect(Collectors.toList());
	}

	/**
	 * Méthode permettant de 'transformer' un nom de groupe en "Distinguished Name".
	 * 
	 * @param name Le nom du groupe à transformer.
	 * 
	 * @return Le groupe sous la forme d'un DN ("Distinguished Name").
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
	 * Méthode permettant d'ajouter un attribut d'un objet dans l'AD.
	 * L'ajout dans l'AD n'existe pas, on ne fait qu'appeler la méthode qui va permettre d'ajouter un attribut dans l'AD. 
	 * Cette méthode permet juste d'avoir du code plus 'clair' par rapport à ce qui est réellement fait.
	 * 
	 * @param attributeName Le nom de l'attribut à ajouter.
	 * @param attributeValue La valeur de l'attribut à ajouter.
	 * @param DNName Le "Distinguished Name" où on va ajouter un attribut.
	 * 
	 * @throws NamingException Si une erreur pendant la recherche / modification arrive.
	 * Attention cette exception peut englober plusieurs erreurs.
	 * Voir https://docs.oracle.com/javase%2Ftutorial%2F/jndi/ldap/exceptions.html pour plus d'infos.
	 */
	private void addAttribute(String attributeName, String attributeValue, String DNName) throws NamingException {
		modifyAttribute(DirContext.ADD_ATTRIBUTE, attributeName, attributeValue, DNName);
	}

	/**
	 * Méthode permettant d'enlever un attribut d'un objet dans l'AD.
	 * La suppression dans l'AD n'existe pas, on ne fait qu'appeler la méthode qui va permettre de modifier un attribut dans l'AD. 
	 * Cette méthode permet juste d'avoir du code plus 'clair' par rapport à ce qui est réellement fait.
	 * 
	 * @param attributeName Le nom de l'attribut à supprimer.
	 * @param attributeValue La valeur de l'attribut à supprimer.
	 * @param DNName Le "Distinguished Name" à modifier.
	 * 
	 * @throws NamingException Si une erreur pendant la recherche / modification arrive.
	 * Attention cette exception peut englober plusieurs erreurs.
	 * Voir https://docs.oracle.com/javase%2Ftutorial%2F/jndi/ldap/exceptions.html pour plus d'infos.
	 */
	private void removeAttribute(String attributeName, String attributeValue, String DNName) throws NamingException {
		modifyAttribute(DirContext.REMOVE_ATTRIBUTE, attributeName, attributeValue, DNName);
	}

	/**
	 * Méthode permettant de modifier / supprimer un attribut dans l'AD.
	 * 
	 * @param action L'action que l'on veut (ADD, REMOVE).
	 * @param attributeName Le nom de l'attribut à modifier.
	 * @param attributeValue La valeur de l'attribut à modifier.
	 * @param DNName Le "Distinguished Name" à modifier.
	 * 
	 * @throws NamingException Si une erreur pendant la recherche / modification arrive.
	 * Attention cette exception peut englober plusieurs erreurs.
	 * Voir https://docs.oracle.com/javase%2Ftutorial%2F/jndi/ldap/exceptions.html pour plus d'infos.
	 */
	private void modifyAttribute(int action, String attributeName, String attributeValue, String DNName) throws NamingException {
		ModificationItem[] mods = { new ModificationItem(action, new BasicAttribute(attributeName, attributeValue)) };

		doWithContext((InitialLdapContext context) -> {
			context.modifyAttributes(DNName, mods);
			return null;
		});
	}
}