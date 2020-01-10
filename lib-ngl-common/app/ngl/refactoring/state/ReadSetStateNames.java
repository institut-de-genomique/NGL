package ngl.refactoring.state;

/**
 * Pseudo enumeration of read set states.
 * 
 * @author vrd
 *
 */
public class ReadSetStateNames {
	
	/**
	 * Available ({@link States#A}).
	 */
	public static final String A;
	
	/**
	 * Unavailable ({@link States#UA}).
	 */
	public static final String UA;
	
	/**
	 * Bioinformatics analysis done ({@link States#F_BA}). 
	 */
	public static final String F_BA;
	
	/**
	 * Quality control done ({@link States#F_QC}).
	 */
	public static final String F_QC;

	/**
	 * Read generation done ({@link States#F_RG}).
	 */
	public static final String F_RG;
	
	/**
	 * Transfer done ({@link States#F_TF}). 
	 */
	public static final String F_TF; 

	/**
	 * Bioinformatics analysis done ({@link States#F_VBA}).
	 */
	public static final String F_VBA;
	
	/**
	 * Quality control done ({@link States#F_VQC}).
	 */
	public static final String F_VQC;
	
	/**
	 * Transfer error ({@link States#FE_TF}). 
	 */
	public static final String FE_TF;
	
	/**
	 * Bioinformatics analysis running ({@link States#IP_BA}).
	 */
	public static final String IP_BA;

	/**
	 * Quality control process running (In Quality Control Process) ({@link States#IP_QC}).
	 */
	public static final String IP_QC;

	/**
	 * Read generation running ({@link States#IP_RG}).
	 */
	public static final String IP_RG;
	
	/**
	 * Transfer running ({@link States#IP_TF}).
	 */
	public static final String IP_TF;
	
	/**
	 * Quality control running ({@link States#IP_VQC}).
	 */
	public static final String IP_VQC;
	
	/**
	 * Waiting for bioinformatics analysis ({@link States#IW_BA}).
	 */
	public static final String IW_BA;
	
	/**
	 * Waiting for transfer ({@link States#IW_TF}). 
	 */
	public static final String IW_TF;
	
	/**
	 * Waiting for quality control ({@link States#IW_QC}).
	 */
	public static final String IW_QC;
	
	/**
	 * Waiting for bioinformatics analysis ({@link States#IW_VBA}). 
	 */
	public static final String IW_VBA;
	
	/**
	 * Waiting for quality control execution ({@link States#IW_VQC}).
	 */
	public static final String IW_VQC;
	
	/**
	 * New ({@link States#N}).
	 */
	public static final String N;
	
	/**
	 * All available states for read set.
	 */
	public static final String[] values = {
		A      = StateNames.A,
		UA     = StateNames.UA,
		F_BA   = StateNames.F_BA,
		F_QC   = StateNames.F_QC,
		F_RG   = StateNames.F_RG,
		F_TF   = StateNames.F_TF,
		F_VBA  = StateNames.F_VBA,
		F_VQC  = StateNames.F_VQC,
		FE_TF  = StateNames.FE_TF,
		IP_BA  = StateNames.IP_BA,
		IP_QC  = StateNames.IP_QC,
		IP_RG  = StateNames.IP_RG,
		IP_TF  = StateNames.IP_TF,
		IP_VQC = StateNames.IP_VQC,
		IW_BA  = StateNames.IW_BA,
		IW_QC  = StateNames.IW_QC,
		IW_TF  = StateNames.IW_TF,
		IW_VBA = StateNames.IW_VBA,
		IW_VQC = StateNames.IW_VQC,
		N      = StateNames.N,
	};
	
}
