package fr.cea.ig.lfw.utils;

public class CodeAndName {
	
	public String code;
	public String name;
	
	public CodeAndName() {
		this("");
	}
	
	public CodeAndName(String codeAndName) {
		this(codeAndName,codeAndName);
	}
	
	public CodeAndName(String code, String label) {
		this.code = code;
		this.name = label;
	}
	
	
}
