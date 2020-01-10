package ngl.refactoring.state;

/**
 * Names of all the available states.
 * 
 * @author vrd
 *
 */
public class StateNames {
	
	/**
	 * Available ({@link States#A}).
	 */
	public static final String A        = "A";
	
	/**
	 * Unavailable ({@link States#UA}).
	 */
	public static final String UA       = "UA";
	
	/**
	 * Finished (done) ({@link States#F}).
	 */
	public static final String F        = "F";
	
	/**
	 * Waiting for quality control ({@link States#IW_QC}).
	 */
	public static final String 	IW_QC    = "IW-QC";
	
	/**
	 * Quality control process running (In Quality Control Process) ({@link States#IP_QC}).
	 */
	public static final String IP_QC    = "IP-QC";
	
	/**
	 * Quality control done ({@link States#F_QC}).
	 */
	public static final String F_QC     = "F-QC";
	
	/**
	 * Waiting for valuation ({@link States#IW_V}).
	 */		
	public static final String IW_V     = "IW-V";
	
	/**
	 * Valuation process running ({@link States#IP_V}).
	 */
	public static final String IP_V     = "IP-V";
	
	/**
	 * Valuation done ({@link States#F_V}).
	 */
	public static final String F_V      = "F-V";
	
	/**
	 * Waiting for container ({@link States#IW_C}).
	 */
	public static final String IW_C     = "IW-C";
	
	/**
	 * New ({@link States#N}).
	 */
	public static final String N        = "N";
	
	/**
	 * In process (running) ({@link States#IP}).
	 */
	public static final String IP       = "IP";
	
	/**
	 * Waiting for process ({@link States#IW_P}).
	 */
	public static final String IW_P     = "IW-P";
	
	/**
	 * Waiting for experiment ({@link States#IW_E}).
	 */
	public static final String IW_E     = "IW-E";

	/**
	 * In use ({@link States#IU}).
	 */
	public static final String IU       = "IU";
	
	/**
	 * Waiting for dispatch ({@link States#IW_D}).
	 */
	public static final String IW_D     = "IW-D";
	
	/**
	 * In stock ({@link States#IS}).
	 */
	public static final String IS       = "IS";
	
	/**
	 * Available for transformation ({@link States#A_TM}).
	 */
	public static final String A_TM     = "A-TM";
	
	/**
	 * Available for quality control ({@link States#A_QC}).
	 */
	public static final String A_QC     = "A-QC";
	
	/**
	 * Available for purification ({@link States#A_PF}).
	 */
	public static final String A_PF     = "A-PF";
	
	/**
	 * Available for transfer ({@link States#A_TF}).
	 */
	public static final String A_TF     = "A-TF";
	
	/**
	 * Sequencing running ({@link States#IP_S}).
	 */
	public static final String IP_S     = "IP-S";
	
	/**
	 * Sequencing done ({@link States#F_S}).
	 */
	public static final String F_S      = "F-S";
	
	/**
	 * Sequencing error ({@link States#FE_S}). 
	 */
	public static final String FE_S     = "FE-S";
	
	/**
	 * Waiting for read generation ({@link States#IW_RG}).
	 */
	public static final String IW_RG    = "IW-RG";
	
	/**
	 * Read generation running ({@link States#IP_RG}).
	 */
	public static final String IP_RG    = "IP-RG";
	
	/**
	 * Read generation done ({@link States#F_RG}).
	 */
	public static final String F_RG     = "F-RG";
	
	/**
	 * Waiting for quality control execution ({@link States#IW_VQC}).
	 */
	public static final String IW_VQC   = "IW-VQC";
	
	/**
	 * Quality control running ({@link States#IP_VQC}).
	 */
	public static final String IP_VQC   = "IP-VQC";
	
	/**
	 * Quality control done ({@link States#F_VQC}).
	 */
	public static final String F_VQC    = "F-VQC";
	
	/**
	 * Waiting for bioinformatics analysis ({@link States#IW_BA}).
	 */
	public static final String IW_BA    = "IW-BA";
	
	/**
	 * Bioinformatics analysis running ({@link States#IP_BA}).
	 */
	public static final String IP_BA    = "IP-BA";
	
	/**
	 * Bioinformatics analysis done ({@link States#F_BA}). 
	 */
	public static final String F_BA     = "F-BA";

	/**
	 * Waiting for transfer ({@link States#IW_TF}). 
	 */
	public static final String IW_TF    = "IW-TF";
	
	/**
	 * Transfer running ({@link States#IP_TF}).
	 */
	public static final String IP_TF    = "IP-TF";
	
	/**
	 * Transfer done ({@link States#F_TF}). 
	 */
	public static final String F_TF     = "F-TF";
	
	/**
	 * Transfer error ({@link States#FE_TF}). 
	 */
	public static final String FE_TF    = "FE-TF";
	
	/**
	 * Waiting for bioinformatics analysis ({@link States#IW_VBA}). 
	 */
	public static final String IW_VBA   = "IW-VBA";
	
	/**
	 * Bioinformatics analysis done ({@link States#F_VBA}).
	 */
	public static final String F_VBA    = "F-VBA";

	/**
	 * User validated submission ({@link States#V_SUB}). 
	 */
	public static final String V_SUB    = "V-SUB";
	
	/**
	 * Waiting for submission ({@link States#IW_SUB}).
	 */
	public static final String IW_SUB   = "IW-SUB";
	
	/**
	 * Submission running ({@link States#IP_SUB}). 
	 */
	public static final String IP_SUB   = "IP-SUB";
	
	/**
	 * Submission done ({@link States#F_SUB}).
	 */
	public static final String F_SUB    = "F-SUB";
	
	/**
	 * Submission error ({@link States#FE_SUB}).
	 */
	public static final String FE_SUB   = "FE-SUB";
	
	/**
	 * New submission release ({@link States#N_R}). 
	 */
	public static final String N_R      = "N-R";
	
	/**
	 * Submission release waiting ({@link States#IW_SUB_R}). 
	 */
	public static final String IW_SUB_R = "IW-SUB-R";
	
	/**
	 * Submission release running ({@link States#IP_SUB_R}).
	 */
	public static final String IP_SUB_R = "IP-SUB-R";
	
	/**
	 * Submission release error ({@link States#FE_SUB_R}).
	 */
	public static final String FE_SUB_R = "FE-SUB-R";

}
