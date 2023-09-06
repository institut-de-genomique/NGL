package models.utils;

public class ListObjectNumber {

	public String name;
	public Number code;
	
	public ListObjectNumber() {
		this(0, "");
	}
	
	public ListObjectNumber(Number code, String label) {
		this.name = label;
		this.code = code;
	}
	
}
