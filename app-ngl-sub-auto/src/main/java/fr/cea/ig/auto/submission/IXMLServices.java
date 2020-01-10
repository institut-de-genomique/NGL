package fr.cea.ig.auto.submission;

import java.io.File;
import java.io.IOException;

import fr.genoscope.lis.devsi.birds.api.exception.BirdsException;
import fr.genoscope.lis.devsi.birds.api.exception.FatalException;
import fr.genoscope.lis.devsi.birds.api.exception.JSONDeviceException;

public interface IXMLServices {

	void writeStudyXml(File outputFile, String code) throws IOException, JSONDeviceException, FatalException;

	void writeSampleXml(File outputFile, String codes) throws IOException, JSONDeviceException, FatalException;

	void createXMLRelease(File outputFile, String submissionCode, String studyCode) throws BirdsException, IOException;

	void writeExperimentXml(File outputFile, String experimentCodes) throws IOException, JSONDeviceException, FatalException;

	void writeRunXml(File outputFile, String codes) throws IOException, JSONDeviceException, FatalException;

	void writeSubmissionXml(File outputFile, String code, String studyCode, String sampleCodes, String experimentCodes)
			throws IOException;

	
}
