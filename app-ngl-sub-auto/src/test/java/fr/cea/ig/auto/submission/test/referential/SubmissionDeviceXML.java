package fr.cea.ig.auto.submission.test.referential;

import java.util.HashSet;
import java.util.Set;

import fr.genoscope.lis.devsi.birds.api.device.AbstractHardCodedReferentialDevice;
import fr.genoscope.lis.devsi.birds.api.entity.ResourceProperties;
import fr.genoscope.lis.devsi.birds.api.entity.ResourceType;
import fr.genoscope.lis.devsi.birds.api.exception.BirdsException;
import fr.genoscope.lis.devsi.birds.api.exception.FatalException;
import fr.genoscope.lis.devsi.birds.api.rule.ResourcePropertiesSet;
import fr.genoscope.lis.devsi.birds.api.rule.ResourcePropertiesSetFact;
import fr.genoscope.lis.devsi.birds.impl.factory.CoreResourcesServicesFactory;

public class SubmissionDeviceXML extends AbstractHardCodedReferentialDevice{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public ResourcePropertiesSet getPropertiesSet() throws BirdsException,
			FatalException {
		ResourceProperties rp1 = new ResourceProperties();
		rp1.setProperty("code","codeSub1");
		rp1.setProperty("submissionDate","23_09_2014");
		rp1.setProperty("submissionDirectory","/env/cns/submit_traces/SRA/SNTS_output_xml/autoFtpTest/testU/dataTest");
		
		//Add property for test create xml
		rp1.setProperty("submissionXML","/env/cns/submit_traces/SRA/SNTS_output_xml/autoFtpTest/testU/dataTest/submission.xml");
		rp1.setProperty("studyXML","/env/cns/submit_traces/SRA/SNTS_output_xml/autoFtpTest/testU/dataTest/study.xml");
		rp1.setProperty("sampleXML","/env/cns/submit_traces/SRA/SNTS_output_xml/autoFtpTest/testU/dataTest/sample.xml");
		rp1.setProperty("experimentXML","/env/cns/submit_traces/SRA/SNTS_output_xml/autoFtpTest/testU/dataTest/experiment.xml");
		rp1.setProperty("runXML","/env/cns/submit_traces/SRA/SNTS_output_xml/autoFtpTest/testU/dataTest/run.xml");
		Set<ResourceProperties> propertiesSet = new HashSet<ResourceProperties>();
		propertiesSet.add(rp1);
		ResourceType resourceType = CoreResourcesServicesFactory.getInstance().getResourceType("submissionXML");
		ResourcePropertiesSet resourcePropertiesSet = new ResourcePropertiesSetFact(resourceType);
		resourcePropertiesSet.setResourcePropertiesSet(propertiesSet);
		return resourcePropertiesSet;
	}

}
