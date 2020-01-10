package containerSupports;

import controllers.containers.api.ContainerSupportsSearchForm;
import models.laboratory.container.instance.ContainerSupport;

public class ContainerSupportTestHelper {

	public static ContainerSupport getFakeContainerSupport(){
		ContainerSupport cs = new ContainerSupport();
		return cs;
	}
	
	public static ContainerSupport getFakeContainerSupportWithCode(String code){
		ContainerSupport cs = new ContainerSupport();
		cs.code = code;
		return cs;
	}
	
	public static ContainerSupportsSearchForm getFakeContainerSupportsSearchForm(){
		ContainerSupportsSearchForm ssf = new ContainerSupportsSearchForm();
		return ssf;
		
	}
	
	
}
