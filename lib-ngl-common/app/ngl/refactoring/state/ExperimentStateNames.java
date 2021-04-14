package ngl.refactoring.state;

/**
 * Experiment states pseudo enumeration.
 *  
 * @author vrd
 *
 */
public class ExperimentStateNames {
	
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
	 * All available states for experiment.
	 */
	public static final String[] values = { 
		F  = StateNames.F,
		IP = StateNames.IP,
		N  = StateNames.N,
	};
	
}