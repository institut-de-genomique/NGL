package fr.cea.ig.ngl.tmp;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.sql.DataSource;

import play.db.Database;
import play.db.NamedDatabase;

/**
 * This is probably bogus as this does not use the spring data source. This should
 * roughly behave like the old system but without the transaction support.
 * 
 * @author vrd
 *
 */
@Singleton
public class NGLDataDBDirect implements INGLDataDB {
	
	private final DataSource dataSource;
	private Class<?> population;
	
	@Inject
	public NGLDataDBDirect(@NamedDatabase("default") Database db) {
		dataSource = db.getDataSource();
		population = null;
	}
	
	@Override
	public DataSource nglDataSource() {
		return dataSource;
	}

	@Override
	public boolean isPopulatedWith(Class<?> dataService) {
		return dataService == population;
	}

	@Override
	public void setPopulatedWith(Class<?> dataService) {
		population = dataService;
	}
	
}
