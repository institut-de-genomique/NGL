package sra.scripts.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class Tools {

	/**
	 * Fait la difference entre 2 collections.
	 * @param plus collection de depart
	 * @param min  collection à soustraire
	 * @return     ensemble (set) correspondant à collection plus - collection min
	 * @param <A> type des elements des ensembles
	 */
	public static <A> Set<A> subtract(Collection<A> plus, Collection<A> min) {
		Set<A> diff = new HashSet<>(plus);
		diff.removeAll(min);
		return diff;
	}

	public static void writeInFile(String pahtOutputFile, String message) throws IOException {
		try (BufferedWriter output_buffer = new BufferedWriter(new java.io.FileWriter(new File("pahtOutputFile")))) {
			output_buffer.write(message);
		} 
	}
	
}
