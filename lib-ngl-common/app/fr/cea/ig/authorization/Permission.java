package fr.cea.ig.authorization;

public enum Permission {
	
	Read   ("reading"),
	Write  ("writing"),
	ChefProjet  ("chef-projet"),
	Admin  ("admin");
	
	private final String alias;
	
	Permission(String alias) {
		this.alias = alias;
	}
	
	public String getAlias() {
		return alias;
	}
	
	public static Permission forName(String perm) {
		for (Permission p : Permission.values())
			if (p.alias.equals(perm))
				return p;
		return null;
	}
	
}
