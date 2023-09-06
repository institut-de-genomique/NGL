package ngl.refactoring.state;

/**
 * Analysis state names pseudo enumeration.
 * 
 * @author vrd
 *
 */
public class  AnalysisStateNames {

	/**
	 * Bioinformatics analysis done ({@link States#F_BA}). 
	 */
	public static final String F_BA = StateNames.F_BA;
	
	/**
	 * Evaluation done ({@link States#F_V}).
	 */
	public static final String F_V = StateNames.F_V;
	
	/**
	 * Bioinformatics analysis running ({@link States#IP_BA}).
	 */
	public static final String IP_BA = StateNames.IP_BA;
	
	/**
	 * Waiting for valuation ({@link States#IW_V}).
	 */		
	public static final String IW_V = StateNames.IW_V;
	
	/**
	 * New ({@link States#N}).
	 */
	public static final String N = StateNames.N;
	
	/**
	 * All available state names for analysis.
	 */
	public static final String[] values = {	F_BA, F_V, IP_BA, IW_V , N };
	
}
