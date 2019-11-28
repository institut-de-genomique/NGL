package ngl.data;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.sql.DataSource;

import fr.cea.ig.ngl.tmp.INGLDataDB;
import nglapps.IApplicationData;
import play.db.DBApi;

/**
 * Repopulate databases using conventions for the database numbering.
 * This track the population of databases by IApplicationData implementations.
 * Conventional naming uses ngl_data_N for the databases.
 *  
 * @author vrd
 *
 */
@Singleton
public class NGLDataDBMulti implements INGLDataDB {
	
	private static final play.Logger.ALogger logger = play.Logger.of(NGLDataDBMulti.class);
	
	// This is expected to survive between test runs (tests in the same VM).
	private static Map<String,String>   dataToDB = new HashMap<>();
	private static Map<String,Class<?>> populations = new HashMap<>();
	
	// We allow an external source to set the "signature of the db data".
	private final DataSource dataSource; 
	private final String dataSourceName;
	
	@Inject
	public NGLDataDBMulti(DBApi dbApi, IApplicationData d) {
		String dataKey = d.getClass().getName();
		String dbName = dataToDB.get(dataKey);
		if (dbName == null) {
			dbName         = "ngl_data_" + dataToDB.size();
			dataSource     = dbApi.getDatabase(dbName).getDataSource();
			dataSourceName = dbName;
			dataToDB.put(dataKey, dbName);
			NGLDataDBBuilder.rebuildDB(dataSource, d);
			logger.debug("initialized db {} : {}", dataKey, dbName);
		} else {
			dataSource     = dbApi.getDatabase(dbName).getDataSource();
			dataSourceName = dbName;
			logger.debug("reused db {} : {}", dataKey, dbName);
		}
	}
	
	@Override
	public DataSource nglDataSource() {
		return dataSource;
	}

	@Override
	public boolean isPopulatedWith(Class<?> dataService) {
		return populations.get(dataSourceName) == dataService; 
	}

	@Override
	public void setPopulatedWith(Class<?> dataService) {
		populations.put(dataSourceName, dataService);
	}
	
}
