package controllers.instruments.io.cns.gilsonpipetmax;

public class HelpSampleSheetLine implements Comparable<HelpSampleSheetLine> {
	
	public String inputContainerCode;
	public String sampleNameList;
	
	public String sourcePos;
	
	@Override
	public int compareTo(HelpSampleSheetLine o) {
		return this.inputContainerCode.compareTo(o.inputContainerCode);			
	}
	
	
}
