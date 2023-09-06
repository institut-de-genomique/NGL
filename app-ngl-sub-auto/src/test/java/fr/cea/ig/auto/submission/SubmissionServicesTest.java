package fr.cea.ig.auto.submission;

import java.util.Set;

import fr.cea.ig.auto.submission.test.referential.RawDataDevice;
import fr.genoscope.lis.devsi.birds.api.entity.ResourceProperties;
import fr.genoscope.lis.devsi.birds.api.exception.BirdsException;
import fr.genoscope.lis.devsi.birds.api.exception.FatalException;
import fr.genoscope.lis.devsi.birds.api.rule.ResourcePropertiesSet;

public class SubmissionServicesTest implements ISubmissionServices{

	public Set<ResourceProperties> getRawDataResources(String submissionCode) throws BirdsException, FatalException 
	{
		RawDataDevice rawDataDevice = new RawDataDevice();
		ResourcePropertiesSet rps = rawDataDevice.getPropertiesSet();
		
		return rps.getResourcePropertiesSet();
	}
}
