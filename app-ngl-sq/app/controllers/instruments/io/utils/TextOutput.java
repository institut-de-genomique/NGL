package controllers.instruments.io.utils;

// Test implementation for output file implementation.
// Script like output generation. 
public abstract class TextOutput {

	private String cr;
	private StringBuilder content;
	
	public TextOutput() {
		content  = new StringBuilder();
		windowsCrLf(); // default to windows cr lf 
	}
	
	public void windowsCrLf() { cr = "\r\n"; }
	public void unixCrLf()    { cr = "\n";   }
		
	public TextOutput print(String... ss) {
		for (String s : ss)
			content.append(s);
		return this;
	}
	
	public TextOutput println(String... ss) {
		return print(ss).print(cr);
	}
	
	public TextOutput printf(String s, Object... args) {
		return print(String.format(s, args));
	}
	
	public TextOutput printfln(String s, Object... args) {
		return printf(s,args).println();
	}
	
	public String getContent() {
		return content.toString(); 
	}
	
}
