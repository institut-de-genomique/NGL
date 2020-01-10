package ngl.refactoring.state;

/**
 * SRA sample states pseudo enumeration.
 * 
 * @author vrd
 *
 */
public class SRASampleStateNames {

	/**
	 * Submission done ({@link States#F_SUB}).
	 */
	public static final String F_SUB;
	
	/**
	 * Submission error ({@link States#FE_SUB}).
	 */
	public static final String FE_SUB;
	
	/**
	 * Submission running ({@link States#IP_SUB}). 
	 */
	public static final String IP_SUB;
	
	/**
	 * Waiting for submission ({@link States#IW_SUB}).
	 */
	public static final String IW_SUB;
	
	/**
	 * User validated submission ({@link States#V_SUB}). 
	 */
	public static final String V_SUB;
	
	/**
	 * All available states for SRA sample.
	 */
	public static final String[] values = { 
		F_SUB  = StateNames.F_SUB,
		FE_SUB = StateNames.FE_SUB,
		IP_SUB = StateNames.IP_SUB,
		IW_SUB = StateNames.IW_SUB,
		V_SUB  = StateNames.V_SUB,
	};
	
}
