package containers;


import java.util.ArrayList;
import java.util.List;

import controllers.containers.api.ContainerBatchElement;
import controllers.containers.api.ContainersSearchForm;
import controllers.containers.api.ContainersUpdateForm;
import models.laboratory.container.instance.Container;


public class ContainerTestHelper {	

	public static Container getFakeContainer() {
		Container cnt = new Container();
		return cnt;
	}
	
	public static Container getFakeContainer(String categoryCode){
		Container cnt = new Container();
		cnt.categoryCode = categoryCode;
		return cnt;
	}
	
	public static Container getFakeContainerWithCode(String code) {
		Container cnt = new Container();
		cnt.code = code;
		return cnt;
	}
	
	public static List<ContainerBatchElement> getFakeListContainerBatchElements(Container...c){		
		List<ContainerBatchElement> lc = new ArrayList<ContainerBatchElement>();		
		for(int i=0; i<c.length;i++){
			ContainerBatchElement cbe = new ContainerBatchElement();
			cbe.index = i;
			cbe.data = c[i];			
			lc.add(cbe);			
		}		
		return lc;		
	}
	
	public static ContainersSearchForm getFakeContainersSearchForm(){
		ContainersSearchForm csf = new ContainersSearchForm();		
		return csf;
	}
	
	public static ContainersUpdateForm getFakeContainersUpdateForm(){
		ContainersUpdateForm cuf = new ContainersUpdateForm();		
		return cuf;
	}

	

}
