package fr.cea.ig.ngl.dao.api.factory;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import fr.cea.ig.ngl.utils.TestUtils;

public class ExperimentTypeFactory {
	
	private ExperimentTypeFactory() {}
	
	public static String getExperimentTypeCode() {
		return UUID.randomUUID().toString();
	}
	
	public static List<String> getExperimentTypeCodes() {
		return IntStream
			.range(0, TestUtils.LIST_SIZE)
			.mapToObj(x -> getExperimentTypeCode())
			.collect(Collectors.toList());
	}

}
