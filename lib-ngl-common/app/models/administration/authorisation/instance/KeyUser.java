package models.administration.authorisation.instance;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import fr.cea.ig.authorization.Permission;

public class KeyUser {
	
	public String name;
	
	public Map<String, Set<Permission>> permissions;
	
	private boolean isRegisteredApp(String app) {
		return permissions.containsKey(app);
	}
	
	private Set<String> getAliasesSet(Set<Permission> appPermissions) {
		return appPermissions
				.stream()
				.map(Permission::getAlias)
				.collect(Collectors.toSet());
	}
	
	public Set<String> getPermissionsAliases(String app) {
		if(!isRegisteredApp(app)) {
			return Collections.emptySet();
		}
		Set<Permission> appPermissions = permissions.get(app);
		return this.getAliasesSet(appPermissions);
	}

}
