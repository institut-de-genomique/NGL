package controllers.instruments.io.common.novaseq;

import java.util.function.Supplier;

/**
 * Interface to allow 2 implementations of String suppliers and selection
 * through application binding.
 * 
 * @author vrd
 *
 */
public interface OutputImplementationSwitch {
	
	/**
	 * Select one of the 2 implementations.
	 * @param oldImplementation old implementation
	 * @param newImplementation new implementation
	 * @return                  selected implementation execution result
	 */
	String get(Supplier<String> oldImplementation,
			   Supplier<String> newImplementation);
	
	/**
	 * Select old implementations.
	 * 
	 * @author vrd
	 *
	 */
	public static class Old implements OutputImplementationSwitch {

		@Override
		public String get(Supplier<String> oldImplementation, Supplier<String> newImplementation) {
			return oldImplementation.get();
		}
		
	}

	/**
	 * Select new implementations.
	 * 
	 * @author vrd
	 *
	 */
	public static class New implements OutputImplementationSwitch {

		@Override
		public String get(Supplier<String> oldImplementation, Supplier<String> newImplementation) {
			return newImplementation.get();
		}
		
	}

}
