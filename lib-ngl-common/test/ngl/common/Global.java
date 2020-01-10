package ngl.common;

import fr.cea.ig.ngl.test.TestAppWithDroolsFactory;

/**
 * Test global definitions.
 * <p>
 * Factories: 
 * <ul>
 *   <li> ngl-sq {@link #afSq} </li>
 *   <li> ngl-bi {@link #afBi} </li>
 *   <li> others {@link #af}   </li>
 * </ul>
 * 
 * @author vrd
 *
 */
public class Global {

	/**
	 * Factory for applications with drools disabled.
	 */
	public static final TestAppWithDroolsFactory af = 
			new TestAppWithDroolsFactory("ngl-common.test.conf");
	
	/**
	 * Factory for applications with the SQ drools rule set.
	 */
	public static final TestAppWithDroolsFactory afSq = 
			af.bindRulesComponent()
			  .configure("rules.key",       "nglSQ")
			  .configure("rules.kbasename", "ngl-all-cns");

	/**
	 * Factory for applications with the BI drools rule set.
	 */
	public static final TestAppWithDroolsFactory afBi = 
			af.bindRulesComponent()
			  .configure("rules.key",       "nglBI")
			  .configure("rules.kbasename", "ngl-bi-cns");
	
}
