package fr.cea.ig.ngl.dao.api.factory;

import java.util.UUID;

public class CatalogFactory {
	
	private CatalogFactory() {}
	
	public static String getKitCatalogCode() {
		return UUID.randomUUID().toString();
	}

}
