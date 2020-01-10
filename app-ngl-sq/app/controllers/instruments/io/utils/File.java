package controllers.instruments.io.utils;

public class File {

	public String filename;
	public String content;

	public File(String filename, String content) {
		this.filename = filename;
		this.content = content;
	}
	public String getFileName(){
		return this.filename;
	}
	public String getBaseName(){
		return this.filename.substring(this.filename.lastIndexOf("."));	
	}
}
