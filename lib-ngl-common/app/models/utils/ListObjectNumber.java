package models.utils;

// TODO: looks like a ListObjectValue<Number>
public class ListObjectNumber {

	public String name;
	public Number code;
	
	public ListObjectNumber() {
		this(0, "");
//		this.name = "";
//		this.code = 0;
	}
	
	public ListObjectNumber(Number code, String label) {
		this.name = label;
		this.code = code;
	}
	
}
