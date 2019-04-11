package ngl.refactoring.state;

/**
 * Pseudo enumeration of SRA submission states.
 *  
 * @author vrd
 *
 */
public class SRASubmissionStateNames {
	
	/**
	 * New ({@link States#N}).
	 */
	public static final String N;
	
	/**
	 * New submission release ({@link States#N_R}). 
	 */
	public static final String N_R;
	
	/**
	 * Submission done ({@link States#F_SUB}).
	 */
	public static final String F_SUB;
	
	/**
	 * Submission error ({@link States#FE_SUB}).
	 */
	public static final String FE_SUB;
	
	/**
	 * Submission release error ({@link States#FE_SUB_R}).
	 */
	public static final String FE_SUB_R;
	
	/**
	 * Submission running ({@link States#IP_SUB}). 
	 */
	public static final String IP_SUB;
	
	/**
	 * Submission release running ({@link States#IP_SUB_R}).
	 */
	public static final String IP_SUB_R;
	
	/**
	 * Waiting for submission ({@link States#IW_SUB}).
	 */
	public static final String IW_SUB;
	
	/**
	 * Submission release waiting ({@link States#IW_SUB_R}). 
	 */
	public static final String IW_SUB_R;
	
	/**
	 * User validated submission ({@link States#V_SUB}). 
	 */
	public static final String V_SUB;
	
	// Pré état (état pour la soummission des read sets (avant soummision)).
	/**
	 * Joe created state (not in description database).
	 */
	public static final String NONE = "NONE";
	
	/**
	 * All available states for SRA submission.
	 */
	public static final String[] values = { 
		N        = StateNames.N,
		N_R      = StateNames.N_R,
		F_SUB    = StateNames.F_SUB,
		FE_SUB   = StateNames.FE_SUB,
		FE_SUB_R = StateNames.FE_SUB_R, 
		IP_SUB   = StateNames.IP_SUB,
		IP_SUB_R = StateNames.IP_SUB_R,
		IW_SUB   = StateNames.IW_SUB,
		IW_SUB_R = StateNames.IW_SUB_R, 
		V_SUB    = StateNames.V_SUB,
	};
	
}
