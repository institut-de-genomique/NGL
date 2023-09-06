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
	 * Abandoned ({@link States#FE})
	 */
	public static final String FE;
	
	/**
	 * Declared ({@link States#IW_U})
	 */
	public static final String IW_U;

	/**
	 * All available states for read set.
	 */
	public static final String[] values = { 
			IW_U = StateNames.IW_U, 
			IP = StateNames.IP, 
			F = StateNames.F, 
			FE = StateNames.FE
	};

}
