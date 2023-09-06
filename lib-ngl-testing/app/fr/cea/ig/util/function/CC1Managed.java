package fr.cea.ig.util.function;

/**
 * Life cycle managed as a CC1 (see {@link CCActions#managed(java.util.function.Supplier)}). 
 * Method naming follows old JUnit scheme (setUp,tearDown).
 * 
 * @author vrd
 *
 */
public interface CC1Managed {
	
	/**
	 * Called before consumption (initialization). 
	 * @throws Exception exception
	 */
	void setUp() throws Exception;
	
	/**
	 * Called after consumption (cleanup).
	 * @throws Exception exception
	 */
	void tearDown() throws Exception;
}

