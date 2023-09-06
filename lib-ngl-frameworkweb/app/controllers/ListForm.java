package controllers;

import views.components.datatable.DatatableForm;

public class ListForm extends DatatableForm {
	
	// This is a list form (type) that is not a list form by default ? 
	public Boolean list      = Boolean.FALSE;
	public Boolean count     = Boolean.FALSE;
//	public boolean reporting = Boolean.FALSE;
//	public boolean aggregate = Boolean.FALSE;
	public boolean reporting = false;
	public boolean aggregate = false;
	
	public Integer limit = 5000; // limit the number of elements in the result

	public String reportingQuery;

}

//// Trying to understand the ListForm type.
//class LFU {
//	// We assume that some flags are related to the query and others to the
//	// result type.
//	// We split the query and the result type parameters.
//	class ReturnType { }
//	class RTDatatable extends ReturnType { }
//	class RTList      extends ReturnType { }
//	class RTCount     extends ReturnType { }
//	// query / restriction
//	class QueryType {}
//	class QTDatatable extends QueryType {}
//	// class 
//	// Projection or count
//	class ProjectionType {}
//}
