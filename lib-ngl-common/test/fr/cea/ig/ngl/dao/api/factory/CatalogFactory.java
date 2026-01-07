package fr.cea.ig.ngl.dao.api.factory;

import java.util.UUID;

/**
 * Factory pour l'entité "Catalog".
 * 
 * @author aprotat - Alexandre PROTAT - aprotat@genoscope.cns.fr
 */
public class CatalogFactory {
	
	private CatalogFactory() {}
	
	/**
	 * getKitCatalogCode()
	 * 
	 * @return
	 */
	public static String getKitCatalogCode() {
		return UUID.randomUUID().toString();
	}

}
