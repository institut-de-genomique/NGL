package controllers.instruments.io.cns.tecanevo100;

public class SampleSheetPoolLine implements Comparable<SampleSheetPoolLine>{

	public String inputSupportContainerLine;
	public String inputSupportContainerColumn;
	public String inputSupportSource;
	public String inputSupportCode;
	public String inputSupportContainerCode;
	public Integer inputSupportContainerPosition;
	public String inputSupportContainerVolume;
	
	public String outputSupportCode;
	public String outputSupportPosition;
	public String outputSupportDestination = "Dest";
	
	@Override
	public int compareTo(SampleSheetPoolLine o) {
		
		if(this.inputSupportCode.compareTo(o.inputSupportCode) == 0){
			return this.inputSupportContainerPosition.compareTo(o.inputSupportContainerPosition);
		}else{
			return this.inputSupportCode.compareTo(o.inputSupportCode);
		}		
	}
	
	
	
}
