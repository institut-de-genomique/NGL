package ngl.refactoring.state;

/**
 * Pseudo enumeration of reagents states.
 * 
 * @author vrd
 *
 */
public class ReagentStateNames {
	
	/**
	 * Finished (done) ({@link States#F}).
	 */
	public static final String F;
	
	/**
	 * In process (running) ({@link States#IP}).
	 */
	public static final String IP;
	
	/**
	 * New ({@link States#N}).
	 */
	public static final String N;
	
	/**
	 * All available states for reagents. 
	 */
	public static final String[] values = { 
		F     = StateNames.F,
		IP    = StateNames.IP,
		N     = StateNames.N,
	};						
	
}
