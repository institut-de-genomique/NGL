package services.smrtlink;

import java.util.ArrayList;
import java.util.List;

public class RunDesign {
	
	private static final String COLUMNS_HEADERS = String.join(", ", "Run Name", "Sample Name");
	
	public List<SampleSheet> sampleSheets = new ArrayList<>();

	public String asCSV() {
		StringBuilder csvBuilder = new StringBuilder(COLUMNS_HEADERS).append("\n");
		return sampleSheets.stream()
		.map(SampleSheet::toLine)
		.reduce(csvBuilder, StringBuilder::append)
		.toString();
	}

}
