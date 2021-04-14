package ngl.refactoring.state;

/**
 * SRA sample states pseudo enumeration.
 * 
 * @author vrd
 *
 */
public class SRASampleStateNames {
	/**
	 * Nouvelle soumission de type creation({@link States#SUB_N}).
	 */
	public static final String SUB_N;
	/**
	 * Soumission de type création validée utilisateur ({@link States#SUB_V}).
	 */
	public static final String SUB_V;
	/**
	 * Soumission de type création en attente pour étape "Send Raw Data" ({@link States#SUB_SRD_IW}).
	 */
	public static final String SUB_SRD_IW;
	/**
	 * Soumission de type création en cours pour "Send Raw Data" ({@link States#SUB_SRD_IP}).
	 */
	public static final String SUB_SRD_IP;
	/**
	 * Soumission de type création finie pour l'étape "Send Raw Data"({@link States#SUB_SRD_F}).
	 */
	public static final String SUB_SRD_F;
	/**
	 * Soumission de type création en echec pour l'étape "Send Raw Data"({@link States#SUB_SRD_FE}).
	 */
	public static final String SUB_SRD_FE;
	/**
	 * Soumission de type création en attente pour étape "Send Meta Data"({@link States#SUB_SMD_IW}).
	 */
	public static final String SUB_SMD_IW;
	/**
	 * Soumission de type création en cours pour "Send Meta Data"({@link States#SUB_SMD_IP}).
	 */
	public static final String SUB_SMD_IP;	
	/**
	 * Soumission de type création finie pour l'étape "Send Meta Data" ({@link States#SUB_SMD_F}).
	 */
	public static final String SUB_SMD_F;
	/**
	 * Soumission de type création en echec pour l'étape "Send Meta Data"({@link States#SUB_SMD_FE}).
	 */
	public static final String SUB_SMD_FE;
	/**
	 * Soumission finie ({@link States#SUB_F}).
	 */
	public static final String SUB_F;
	
	/**
	 * Nouvelle soumission de type update({@link States#SUB_N}).
	 */
	public static final String SUBU_N;
	/**
	 * Soumission de type update validée utilisateur ({@link States#SUBU_V}).
	 */
	public static final String SUBU_V;
	/**
	 * Soumission de type update en attente pour étape "Send Meta Data" ({@link States#SUBU_SMD_IW}).
	 */
	public static final String SUBU_SMD_IW;
	/**
	 * Soumission de type update en cours pour "Send Meta Data" ({@link States#SUBU_SMD_IP}).
	 */
	public static final String SUBU_SMD_IP;
	/**
	 * Soumission de type update finie pour l'étape "Send Meta Data"({@link States#SUBU_SMD_F}).
	 */
	public static final String SUBU_SMD_F;
	/**
	 * Soumission de type update en echec pour l'étape "Send Meta Data"({@link States#SUBU_SMD_FE}).
	 */
	public static final String SUBU_SMD_FE;


	/**
	 * All available states for SRASample. 
	 */
	public static final String[] values = { 
		SUB_N       = StateNames.SUB_N,
		SUB_V       = StateNames.SUB_V,
		SUB_SRD_IW  = StateNames.SUB_SRD_IW,
		SUB_SRD_IP  = StateNames.SUB_SRD_IP,
		SUB_SRD_F   = StateNames.SUB_SRD_F,
		SUB_SRD_FE  = StateNames.SUB_SRD_FE,
		SUB_SMD_IW  = StateNames.SUB_SMD_IW,
		SUB_SMD_IP  = StateNames.SUB_SMD_IP,
		SUB_SMD_F   = StateNames.SUB_SMD_F,
		SUB_SMD_FE  = StateNames.SUB_SMD_FE,
		SUB_F       = StateNames.SUB_F,
		SUBU_N       = StateNames.SUBU_N,
		SUBU_V       = StateNames.SUBU_V,
		SUBU_SMD_IW  = StateNames.SUBU_SMD_IW,
		SUBU_SMD_IP  = StateNames.SUBU_SMD_IP,
		SUBU_SMD_F   = StateNames.SUBU_SMD_F,
		SUBU_SMD_FE  = StateNames.SUBU_SMD_FE,
		
	};
	
}
