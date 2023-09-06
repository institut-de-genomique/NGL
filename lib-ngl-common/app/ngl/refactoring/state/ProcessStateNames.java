package ngl.refactoring.state;

/**
 * Process states pseudo enumeration.
 * 
 * @author vrd
 *
 */
public class ProcessStateNames {

	/**
	 * Finished (done) ({@link States#F}).
	 */
	public static final String F;

	/**
	 * In process (running) ({@link States#IP}).
	 */
	public static final String IP;
	
	/**
	 * Waiting for container ({@link States#IW_C}).
	 */
	public static final String IW_C;
	
	/**
	 * New ({@link States#N}).
	 */
	public static final String N;
	
	/**
	 * All available states for process.
	 */
	public static final String[] values = { 
		F    = StateNames.F,
		IP   = StateNames.IP,
		IW_C = StateNames.IW_C,
		N    = StateNames.N,
	};
	
}
