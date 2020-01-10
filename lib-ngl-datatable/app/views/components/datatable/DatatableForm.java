package views.components.datatable;

import java.util.Set;
import java.util.TreeSet;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class DatatableForm implements IDatatableForm {
	
	// This is a data table form (type) that is not a data table form object by default ? 
	public Boolean datatable            = Boolean.FALSE;
	public Integer orderSense           = DatatableConfig.DEFAULT_ORDER_SENSE;
	public Integer numberRecordsPerPage = DatatableConfig.DEFAULT_NB_ELEMENT;
	public Integer pageNumber           = DatatableConfig.DEFAULT_PAGE_NUMBER;
	public String  orderBy;	
	public String  paginationMode       = "REMOTE";	
	public Set<String> excludes = new TreeSet<>();
	public Set<String> includes = new TreeSet<>();

	@JsonIgnore
	public boolean isServerPagination() {
		return "REMOTE".equalsIgnoreCase(paginationMode);
	}
	
	@Override
	@JsonIgnore
	public Set<String> excludes() {
		return excludes;
	}
	
	@Override
	@JsonIgnore
	public Set<String> includes() {
		return includes;
	}
	
}
