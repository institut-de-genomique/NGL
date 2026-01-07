package fr.cea.ig.ngl.dao.api.factory;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import fr.cea.ig.ngl.utils.TestUtils;

/**
 * Factory pour l'entité "ExperimentType".
 * 
 * @author aprotat - Alexandre PROTAT - aprotat@genoscope.cns.fr
 */
public class ExperimentTypeFactory {
	
	private ExperimentTypeFactory() {}
	
	/**
	 * Méthode permettant de générer un code d'experiment type.
	 * 
	 * @return Une chaîne de caractères représentant un code d'experiment type.
	 */
	public static String getExperimentTypeCode() {
		return UUID.randomUUID().toString();
	}
	
	/**
	 * Méthode permettant de générer une liste de codes experiment types.
	 * 
	 * @return Une liste de codes experiment types.
	 */
	public static List<String> getExperimentTypeCodes() {
		return IntStream
			.range(0, TestUtils.LIST_SIZE)
			.mapToObj(x -> getExperimentTypeCode())
			.collect(Collectors.toList());
	}

}
