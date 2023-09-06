package fr.cea.ig.auto.submission;


import fr.genoscope.lis.devsi.birds.api.exception.FatalException;
import fr.genoscope.lis.devsi.birds.impl.properties.ProjectProperties;

public class SubmissionServicesFactory {

	static ISubmissionServices submissionServices;

	public static ISubmissionServices getInstance() throws FatalException {
		if (submissionServices == null){
			if(ProjectProperties.isInTestContext())
				//TODO 
				System.out.println("Test");
				//submissionServices = new SubmissionServicesTest();
			else
				submissionServices = new SubmissionServices();
		}
		return submissionServices;
	}
}
