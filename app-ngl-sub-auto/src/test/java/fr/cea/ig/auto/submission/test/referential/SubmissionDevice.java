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

public class SubmissionDevice extends AbstractHardCodedReferentialDevice{

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
		rp1.setProperty("submissionDirectory","/env/cns/home/ejacoby/testU/app-NGL-sub-auto/dataTest");
		
		Set<ResourceProperties> propertiesSet = new HashSet<ResourceProperties>();
		propertiesSet.add(rp1);
		ResourceType resourceType = CoreResourcesServicesFactory.getInstance().getResourceType("submission");
		ResourcePropertiesSet resourcePropertiesSet = new ResourcePropertiesSetFact(resourceType);
		resourcePropertiesSet.setResourcePropertiesSet(propertiesSet);
		return resourcePropertiesSet;
	}

}
