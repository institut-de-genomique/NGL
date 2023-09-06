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
	 * Abandoned ({@link States#FE})
	 */
	public static final String FE        = "FE";
	
	/**
	 * Declared ({@link States#IW_U})
	 */
	public static final String IW_U        = "IW-U";
	
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
	 * Objet lié a aucune soumission({@link States#NONE}).
	 */
	public static final String NONE = "NONE";

	/**
	 * Nouvelle soumission de type creation({@link States#SUB_N}).
	 */
	public static final String SUB_N = "SUB-N";
	
	/**
	 * Soumission de type création validée utilisateur ({@link States#SUB_V}).
	 */
	public static final String SUB_V = "SUB-V";
	
	/**
	 * Soumission de type création en attente pour étape "Send Raw Data" ({@link States#SUB_SRD_IW}).
	 */
	public static final String SUB_SRD_IW = "SUB-SRD-IW";
	
	/**
	 * Soumission de type création en cours pour "Send Raw Data" ({@link States#SUB_SRD_IP}).
	 */
	public static final String SUB_SRD_IP = "SUB-SRD-IP";
	
	/**
	 * Soumission de type création finie pour l'étape "Send Raw Data"({@link States#SUB_SRD_F}).
	 */
	public static final String SUB_SRD_F = "SUB-SRD-F";
	
	/**
	 * Soumission de type création en echec pour l'étape "Send Raw Data"({@link States#SUB_SRD_FE}).
	 */
	public static final String SUB_SRD_FE = "SUB-SRD-FE";
	
	/**
	 * Soumission de type création en attente pour étape "Send Meta Data"({@link States#SUB_SMD_IW}).
	 */
	public static final String SUB_SMD_IW = "SUB-SMD-IW";
	
	/**
	 * Soumission de type création en cours pour "Send Meta Data"({@link States#SUB_SMD_IP}).
	 */
	public static final String SUB_SMD_IP = "SUB-SMD-IP";	
	
	/**
	 * Soumission de type création finie pour l'étape "Send Meta Data" ({@link States#SUB_SMD_F}).
	 */
	public static final String SUB_SMD_F = "SUB-SMD-F";
	
	/**
	 * Soumission de type création en echec pour l'étape "Send Meta Data"({@link States#SUB_SMD_FE}).
	 */
	public static final String SUB_SMD_FE = "SUB-SMD-FE";
	
	/**
	 * Soumission finie ({@link States#SUB_F}).
	 */
	public static final String SUB_F = "SUB-F";
	
	/**
	 * Soumission en echec ({@link States#SUB_FE}).
	 */
	public static final String SUB_FE = "SUB-FE";	
	
	/**
	 * Nouvelle soumission de type update({@link States#SUB_N}).
	 */
	public static final String SUBU_N = "SUBU-N";
	
	/**
	 * Soumission de type update validée utilisateur ({@link States#SUBU_V}).
	 */
	public static final String SUBU_V = "SUBU-V";
	
	/**
	 * Soumission de type update en attente pour étape "Send Meta Data" ({@link States#SUBU_SMD_IW}).
	 */
	public static final String  SUBU_SMD_IW = "SUBU-SMD-IW";
	
	/**
	 * Soumission de type update en cours pour "Send Meta Data" ({@link States#SUBU_SMD_IP}).
	 */
	public static final String SUBU_SMD_IP = "SUBU-SMD-IP";
	
	/**
	 * Soumission de type update finie pour l'étape "Send Meta Data"({@link States#SUBU_SMD_F}).
	 */
	public static final String SUBU_SMD_F = "SUBU-SMD-F";
	
	/**
	 * Soumission de type update en echec pour l'étape "Send Meta Data"({@link States#SUBU_SMD_FE}).
	 */
	public static final String SUBU_SMD_FE = "SUBU-SMD-FE";
	
	/**
	 * Soumission de type update en echec({@link States#SUBU_FE}).
	 */
	public static final String SUBU_FE = "SUBU-FE";
	
	/**
	 * Nouvelle soumission de type release({@link States#SUBR_N}).
	 */
	public static final String SUBR_N = "SUBR-N";

	/**
	 * Soumission de type release en attente pour étape "Send Meta Data" ({@link States#SUBR_SMD_IW}).
	 */
	public static final String SUBR_SMD_IW  = "SUBR-SMD-IW";
	
	/**
	 * Soumission de type release en cours pour "Send Meta Data" ({@link States#SUBR_SMD_IP}).
	 */
	public static final String SUBR_SMD_IP = "SUBR-SMD-IP";
	
	/**
	 * Soumission de type release finie pour l'étape "Send Meta Data"({@link States#SUBR_SMD_F}).
	 */
	public static final String SUBR_SMD_F = "SUBR-SMD-F";
	
	/**
	 * Soumission de type release en echec pour l'étape "Send Meta Data"({@link States#SUBR_SMD_FE}).
	 */
	public static final String SUBR_SMD_FE = "SUBR-SMD-FE";
	
	/**
	 * Soumission de type release en echec ({@link States#SUBR_FE}).
	 */
	public static final String SUBR_FE = "SUBR-FE";
	

}
