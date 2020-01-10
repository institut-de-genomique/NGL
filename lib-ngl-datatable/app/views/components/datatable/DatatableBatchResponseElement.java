package views.components.datatable;

public class DatatableBatchResponseElement {
	
	public Integer status;
	public Object data;
	public Integer index;	
	
	public DatatableBatchResponseElement(Integer status, Object data, Integer index) {
		super();
		this.status = status;
		this.data = data;
		this.index = index;
	}
	
	public DatatableBatchResponseElement(Integer status, Integer index) {
		super();
		this.status = status;
		this.index = index;
	}

}
