package utils;

import views.components.datatable.DatatableBatchResponseElement;

public class DatatableBatchResponseElementForTest<T> extends DatatableBatchResponseElement {
	
	
	public DatatableBatchResponseElementForTest(){
		super(null,null);
	}
	
	
	public DatatableBatchResponseElementForTest(Integer status, T data, Integer index) {
		super(status, data, index);
		
	}
	
	public DatatableBatchResponseElementForTest(Integer status, Integer index) {
		super(status, index);		
	}
	

}
