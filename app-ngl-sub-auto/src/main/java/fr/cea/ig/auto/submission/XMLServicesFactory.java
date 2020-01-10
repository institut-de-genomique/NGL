package fr.cea.ig.auto.submission;


import fr.genoscope.lis.devsi.birds.api.exception.FatalException;
import fr.genoscope.lis.devsi.birds.impl.properties.ProjectProperties;

public class XMLServicesFactory {

	static IXMLServices xmlServices;

	public static IXMLServices getInstance() throws FatalException {
		if (xmlServices == null){
			if(ProjectProperties.isInTestContext())
				//TODO 
				System.out.println("Test");
				//submissionServices = new SubmissionServicesTest();
			else
				xmlServices = new XMLServices();
		}
		return xmlServices;
	}
}
