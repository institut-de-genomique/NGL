package views.components.datatable;

import java.util.List;
import java.util.Map;

public class DatatableColumn {
	
	public String header; //header of the column
	public String property; //property name in object
	public String id;   //id of the column
	public Boolean edit = Boolean.FALSE;//Can be edited
	public Boolean hide = Boolean.FALSE;//Can be hidden
	public Boolean order = Boolean.FALSE;//Can be ordered
	public String type = "String";//"String", "Number", "Month", "Week", "Time", "DateTime", "Range", "Color", "Mail", "Tel", "Url", "Date"
	public boolean choiceInList = Boolean.FALSE;//Constraint choice with a list
	public String listStyle = "select";//select/radio/multiselect
	
	public List<Object> possibleValues = null;//The possible value, can be null if choiceInList=false
	
	public String filter = "";
	public String render;

	public Map<Integer, String> extraHeaders = null;//Headers of the column
	
	public Boolean isDate(){
		return type.equals("Date")  || type.equals("DateTime");
	}
}
