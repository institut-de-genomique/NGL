package ngl.refactoring.state;

/**
 * Run states pseudo enumeration.
 * 
 * @author vrd
 *
 */
public final class RunStateNames {
	
	/**
	 * Finished (done) ({@link States#F}).
	 */
	public static final String F;
	
	/**
	 * Read generation done ({@link States#F_RG}).
	 */
	public static final String F_RG;
	
	/**
	 * Sequencing done ({@link States#F_S}).
	 */
	public static final String F_S;
	
	/**
	 * Valuation done ({@link States#F_V}).
	 */
	public static final String F_V;
	
	/**
	 * Sequencing error ({@link States#FE_S}). 
	 */
	public static final String FE_S;
	
	/**
	 * Read generation running ({@link States#IP_RG}).
	 */
	public static final String IP_RG;
	
	/**
	 * Sequencing running ({@link States#IP_S}).
	 */
	public static final String IP_S;
	
	/**
	 * Valuation process running ({@link States#IP_V}).
	 */
	public static final String IP_V;
	
	/**
	 * Waiting for read generation ({@link States#IW_RG}).
	 */
	public static final String IW_RG;
	
	/**
	 * Waiting for valuation ({@link States#IW_V}).
	 */		
	public static final String IW_V;
	
	/**
	 * New ({@link States#N}).
	 */
	public static final String N;
	
	/**
	 * All available states for run.
	 */
	public static final String[] values = { 
		F     = StateNames.F,
		F_RG  = StateNames.F_RG,
		F_S   = StateNames.F_S,
		F_V   = StateNames.F_V,
		FE_S  = StateNames.FE_S,
		IP_RG = StateNames.IP_RG,
		IP_S  = StateNames.IP_S,
		IP_V  = StateNames.IP_V,
		IW_RG = StateNames.IW_RG,
		IW_V  = StateNames.IW_V,
		N     = StateNames.N,
	};
	
}
