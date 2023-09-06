package ngl.refactoring.state;

/**
 * Container allowed states names (subset of {@link StateNames}).
 * 
 * @author vrd
 *
 */
public class  ContainerStateNames {
	
	/**
	 * Available ({@link States#A}).
	 */
	public static final String A;
	
	/**
	 * Available for purification ({@link States#A_PF}).
	 */
	public static final String A_PF;
	
	/**
	 * Available for quality control ({@link States#A_QC}).
	 */
	public static final String A_QC;
	
	/**
	 * Available for transfer ({@link States#A_TF}).
	 */
	public static final String A_TF;
	
	/**
	 * Available for transformation ({@link States#A_TM}).
	 */
	public static final String A_TM;
	
	/**
	 * Done ({@link States#F}).
	 */
	public static final String F;
	
	/**
	 * In stock ({@link States#IS}).
	 */
	public static final String IS;
	
	/**
	 * In use ({@link States#IU}).
	 */
	public static final String IU;
	
	/**
	 * Waiting for dispatch ({@link States#IW_D}).
	 */
	public static final String IW_D;
	
	/**
	 * Waiting for experiment ({@link States#IW_E}).
	 */
	public static final String IW_E;
	
	/**
	 * Waiting for process ({@link States#IW_P}).
	 */
	public static final String IW_P;
	
	/**
	 * New ({@link States#N}).
	 */
	public static final String N;
	
	/**
	 * Unavailable ({@link States#UA}).
	 */
	public static final String UA;
	
	/**
	 * All available states for containers. 
	 */
	public static final String[] values = { 
		A    = StateNames.A,
		A_PF = StateNames.A_PF,
		A_QC = StateNames.A_QC,
		A_TF = StateNames.A_TF,
		A_TM = StateNames.A_TM,
		F    = StateNames.F,
		IS   = StateNames.IS,
		IU   = StateNames.IU,
		IW_D = StateNames.IW_D,
		IW_E = StateNames.IW_E,
		IW_P = StateNames.IW_P,
		N    = StateNames.N,
		UA   = StateNames.UA,
	};
	
}
