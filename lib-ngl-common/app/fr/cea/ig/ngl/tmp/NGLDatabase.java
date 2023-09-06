package fr.cea.ig.ngl.tmp;

import javax.inject.Inject;
import javax.sql.DataSource;

/**
 * Injectable "default" ("ngl") database. 
 * Uses the INGLData binding/implementation to fetch the data source. 
 * 
 * @author vrd
 *
 */
public class NGLDatabase {
	
	private final INGLDataDB nglData;
	
	@Inject
	public NGLDatabase(INGLDataDB nglData) {
		this.nglData = nglData;
	}
		
	public DataSource getDataSource() {
		return nglData.nglDataSource();
	}
	
}
