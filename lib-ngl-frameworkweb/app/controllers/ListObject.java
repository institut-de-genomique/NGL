package controllers;

public class ListObject {
	
	public String code;
	public String name;
	
	public ListObject() {
//		this.name = "";
//		this.code = "";
		this("","");
	}
	
	public ListObject(String code, String label) {
		this.code = code;
		this.name = label;
	}
	
}
