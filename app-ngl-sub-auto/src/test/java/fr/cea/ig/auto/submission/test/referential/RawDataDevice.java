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

public class RawDataDevice extends AbstractHardCodedReferentialDevice{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public ResourcePropertiesSet getPropertiesSet() throws BirdsException,
			FatalException {
		ResourceProperties rp1 = new ResourceProperties();
		rp1.setProperty("path","/env/cns/submit_traces/SRA/SNTS_output_xml/autoFtpTest/testU/dataTest");
		rp1.setProperty("relatifName","file1.fastq");
		rp1.setProperty("md5",null);
		rp1.setProperty("md5File", "/env/cns/submit_traces/SRA/SNTS_output_xml/autoFtpTest/testU/md5.txt");
		rp1.setProperty("fileZiped", "/env/cns/submit_traces/SRA/SNTS_output_xml/autoFtpTest/testU/dataTest/file1.fastq.gz");
		
		ResourceProperties rp2 = new ResourceProperties();
		rp2.setProperty("path","/env/cns/submit_traces/SRA/SNTS_output_xml/autoFtpTest/testU/dataTest");
		rp2.setProperty("relatifName","file2.sff");
		rp2.setProperty("md5",null);
		rp2.setProperty("md5File", "/env/cns/submit_traces/SRA/SNTS_output_xml/autoFtpTest/testU/md5.txt");
		rp2.setProperty("fileZiped", "/env/cns/submit_traces/SRA/SNTS_output_xml/autoFtpTest/testU/dataTest/file2.sff.gz");
		
		Set<ResourceProperties> propertiesSet = new HashSet<ResourceProperties>();
		propertiesSet.add(rp1);
		propertiesSet.add(rp2);
		ResourceType resourceType = CoreResourcesServicesFactory.getInstance().getResourceType("rawData");
		ResourcePropertiesSet resourcePropertiesSet = new ResourcePropertiesSetFact(resourceType);
		resourcePropertiesSet.setResourcePropertiesSet(propertiesSet);
		return resourcePropertiesSet;
		
	}

}
