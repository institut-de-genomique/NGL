package ngl.refactoring.state;

public class ReagentReceptionStateNames {

	/**
	 * Available ({@link States#IP}).
	 */
	public static final String IP;

	/**
	 * Finished (done) ({@link States#F}).
	 */
	public static final String F;

	/**
	 * All available states for read set.
	 */
	public static final String[] values = { 
			IP = StateNames.IP, 
			F = StateNames.F, 
	};

}
