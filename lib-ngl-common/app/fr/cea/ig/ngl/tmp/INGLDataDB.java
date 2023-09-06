package fr.cea.ig.ngl.tmp;

import javax.sql.DataSource;

public interface INGLDataDB {
	
	DataSource nglDataSource();
	// Should be defined using IApplicationData but we cannot move 
	// IApplicationData in common.
	// boolean isPopulatedWith(Class<? extends IApplicationData> dataService);
	boolean isPopulatedWith(Class<?> dataService);
	void setPopulatedWith(Class<?> dataService);
}