package views.components.datatable;

import java.util.List;

public class DatatableResponse<T> {
	
	public List<T> data;
	public Integer recordsNumber;	
	
	public DatatableResponse(List<T> data) {
		super();
		this.data = data;
		this.recordsNumber = data.size();
	}

	public DatatableResponse(List<T> data, Integer recordsNumber) {
		super();
		this.data = data;
		this.recordsNumber = recordsNumber;
	}

}
