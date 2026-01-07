package scripts;

import org.mongojack.DBQuery;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptNoArgs;
import fr.cea.ig.mongo.DBQueryBuilder;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.container.instance.Container;
import models.laboratory.sample.instance.Sample;
import models.utils.InstanceConstants;

import play.Logger;

public class ScriptReprise4697 extends ScriptNoArgs {

	@Override
	public void execute() throws Exception {
		Logger.error("Start ScriptReprise4697");

		cas1SurSampleDKB();
		cas1SurSampleDKLDKMDKNDKQDKP();
		// cas2SurContainerDKB();
		// cas2SurContainerDKL();

		Logger.error("End ScriptReprise4697");
	}

	private void cas1SurSampleDKB() throws IOException {
		Logger.error("cas1SurSampleDKB start");

		List<Sample> sampleList = MongoDBDAO.find(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, 
			 DBQuery.in("projectCodes", "DKB"))
		.toList();

		File csvFile = new File("cas1SurSample-list-sample.csv");
		FileWriter fileWriter = new FileWriter(csvFile);

		String fileContent = "Code container;Prop name;Prop value\r";
		HashSet<String> propNamesUnique = new HashSet<>();

		for (int i = 0; i < sampleList.size(); i++) {
			if (sampleList.get(i).properties != null) {
				for (Map.Entry<String, PropertyValue> entry : sampleList.get(i).properties.entrySet()) {
					String propKey = entry.getKey();
					PropertyValue propValue = entry.getValue();

					if (propValue.value.equals("NOT_AVAILABLE")) {
						fileContent += sampleList.get(i).code + ";" + propKey + ";" + "NOT_COLLECTED" + "\r";
						
						propNamesUnique.add(propKey);

						sampleList.get(i).traceInformation.modifyDate = new Date();
						sampleList.get(i).traceInformation.modifyUser = "ngl-support";

						sampleList.get(i).properties.get(propKey).value = "NOT_COLLECTED";

						MongoDBDAO.update(InstanceConstants.SAMPLE_COLL_NAME, sampleList.get(i));
					}
				}
			}
		}

		fileWriter.write(fileContent.toString());
		fileWriter.close();

		Logger.error(propNamesUnique.toString());

		Logger.error("cas1SurSampleDKB end");
	}

	private void cas1SurSampleDKLDKMDKNDKQDKP() throws IOException {
		Logger.error("cas1SurSampleDKLDKMDKNDKQDKP start");

		List<Sample> sampleList = MongoDBDAO.find(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, 
			DBQuery.in("projectCodes", "DKD", "DKE", "DKF", "DKC", "DKL", "DKM", "DKN", "DKP", "DKQ"))
		.toList();

		File csvFile = new File("cas1SurSampleDKL-DKM-DKN-DKP-DKQ-list-sample.csv");
		FileWriter fileWriter = new FileWriter(csvFile);

		String fileContent = "Code container;Prop name;Prop value\r";
		HashSet<String> propNamesUnique = new HashSet<>();

		for (int i = 0; i < sampleList.size(); i++) {
			if (sampleList.get(i).properties != null) {
				for (Map.Entry<String, PropertyValue> entry : sampleList.get(i).properties.entrySet()) {
					String propKey = entry.getKey();
					PropertyValue propValue = entry.getValue();

					if (propValue.value.equals("NOT_AVAILABLE")) {
						fileContent += sampleList.get(i).code + ";" + propKey + ";" + "NOT_COLLECTED" + "\r";
						
						propNamesUnique.add(propKey);

						sampleList.get(i).traceInformation.modifyDate = new Date();
						sampleList.get(i).traceInformation.modifyUser = "ngl-support";

						sampleList.get(i).properties.get(propKey).value = "NOT_COLLECTED";

						MongoDBDAO.update(InstanceConstants.SAMPLE_COLL_NAME, sampleList.get(i));
					}
				}
			}
		}

		fileWriter.write(fileContent.toString());
		fileWriter.close();

		Logger.error(propNamesUnique.toString());

		Logger.error("cas1SurSampleDKLDKMDKNDKQDKP end");
	}

	private void cas2SurContainerDKB() throws IOException {
		Logger.error("cas2SurContainerDKB start");

		File csvFile = new File("cas2SurContainer-dkb-list-container.csv");
		FileWriter fileWriter = new FileWriter(csvFile);

		List<Container> contList = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class, 
			 DBQuery.in("projectCodes", "DKB")
			 		.and(DBQuery.notExists("fromPurificationTypeCode"),
			 			DBQuery.notExists("fromTransfertTypeCode"),
			 			DBQuery.or(DBQuery.size("fromTransformationTypeCodes", 0),
			 					   DBQuery.notExists("fromTransformationTypeCodes"),
			 					   DBQuery.regex("fromTransformationTypeCodes", Pattern.compile("^ext-to-.+$")))
					)
			 )
		.toList();

		String fileContent = "Code container;Prop name;Prop value\r";
		HashSet<String> propNamesUnique = new HashSet<>();

		for (int i = 0; i < contList.size(); i++) {
			for (int j = 0; j < contList.get(i).contents.size(); j++) {
				for (Map.Entry<String, PropertyValue> entry : contList.get(i).contents.get(j).properties.entrySet()) {
					String propKey = entry.getKey();
					PropertyValue propValue = entry.getValue();

					if (propValue.value.equals("NOT_AVAILABLE")) {
						fileContent += contList.get(i).code + ";" + propKey + ";" + "NOT_COLLECTED" + "\r";

						propNamesUnique.add(propKey);

						contList.get(i).contents.get(j).properties.get(propKey).value = "NOT_COLLECTED";

						contList.get(i).traceInformation.modifyDate = new Date();
						contList.get(i).traceInformation.modifyUser = "ngl-support";

						MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME, contList.get(i));
					}
				}
			}
		}

		fileWriter.write(fileContent.toString());
		fileWriter.close();

		Logger.error(propNamesUnique.toString());

		Logger.error("cas2SurContainerDKB end");
	}

	private void cas2SurContainerDKL() throws IOException {
		Logger.error("cas2SurContainerDKL start");

		File csvFile = new File("cas2SurContainer-dkl-list-container.csv");
		FileWriter fileWriter = new FileWriter(csvFile);

		List<Container> contList = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class, 
			 DBQuery.in("projectCodes", "DKL")
			 		.and(DBQuery.notExists("fromPurificationTypeCode"),
			 			DBQuery.notExists("fromTransfertTypeCode"),
			 			DBQuery.or(DBQuery.size("fromTransformationTypeCodes", 0),
			 					   DBQuery.notExists("fromTransformationTypeCodes"),
			 					   DBQuery.regex("fromTransformationTypeCodes", Pattern.compile("^ext-to-.+$")))
					)
			 )
		.toList();

		String fileContent = "Code container;Prop name;Prop value\r";
		HashSet<String> propNamesUnique = new HashSet<>();

		for (int i = 0; i < contList.size(); i++) {
			for (int j = 0; j < contList.get(i).contents.size(); j++) {
				for (Map.Entry<String, PropertyValue> entry : contList.get(i).contents.get(j).properties.entrySet()) {
					String propKey = entry.getKey();
					PropertyValue propValue = entry.getValue();

					if (propValue.value.equals("NOT_AVAILABLE")) {
						fileContent += contList.get(i).code + ";" + propKey + ";" + "NOT_COLLECTED" + "\r";

						propNamesUnique.add(propKey);

						contList.get(i).contents.get(j).properties.get(propKey).value = "NOT_COLLECTED";

						contList.get(i).traceInformation.modifyDate = new Date();
						contList.get(i).traceInformation.modifyUser = "ngl-support";

						MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME, contList.get(i));
					}
				}
			}
		}

		fileWriter.write(fileContent.toString());
		fileWriter.close();

		Logger.error(propNamesUnique.toString());

		Logger.error("cas2SurContainerDKL end");
	}
}