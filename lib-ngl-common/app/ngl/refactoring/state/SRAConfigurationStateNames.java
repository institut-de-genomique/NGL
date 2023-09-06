package ngl.refactoring.state;

/**
 * Pseudo enumeration of SRA configuration states.
 * 
 * @author vrd
 *
 */
public class SRAConfigurationStateNames {
	// Pré état (état pour les configurations avant d'etre utilises dans une soumission).
	/**
	 * Joe created state (not in description database).
	 */
	public static final String NONE = "NONE";	
	
	/**
	 * New ({@link States#N}).
	 */
	public static final String N;
	
	/**
	 * All available states for SRA configuration.
	 */
	public static final String[] values = { 
		N  = StateNames.N,
	};
	
}
