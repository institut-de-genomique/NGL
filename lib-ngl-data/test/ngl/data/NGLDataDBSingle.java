package ngl.data;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.sql.DataSource;

import fr.cea.ig.ngl.tmp.INGLDataDB;
import nglapps.IApplicationData;
import play.db.Database;
import play.db.NamedDatabase;

/**
 * Repopulate as needed the data base. This is a candidate implementation 
 * for application so any application deployment will use the source definition
 * of data.
 * 
 * @author vrd
 *
 */
@Singleton
public class NGLDataDBSingle implements INGLDataDB {
	
	private static String dataKey;
	private final DataSource dataSource;
	private static Class<?> population; 
	
	@Inject
	public NGLDataDBSingle(@NamedDatabase("default") Database db, IApplicationData ad) {
		dataSource = db.getDataSource();
		String newDataKey = ad.getClass().getName();
		if (!newDataKey.equals(dataKey)) {
			dataKey = newDataKey;
			NGLDataDBBuilder.rebuildDB(dataSource, ad);
		}
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
