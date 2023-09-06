package services.smrtlink;

public class SampleSheet {
	
	public String runName;
	
	public String sampleName;
	
	public final StringBuilder toLine() {
		return new StringBuilder()
				.append(this.runName)
				.append(", ")
				.append(this.sampleName)
				.append("\n");
	}

}
