package controllers.instruments.io.cng.janus;

public class SampleSheetPoolLine implements Comparable<SampleSheetPoolLine>{

	public String inputSupportCode; //normallement uniqt pour DEBUG
	public String inputSupportSource;
	public Integer inputSupportContainerPosition;
	public String inputSupportContainerVolume;
	
	public String outputSupportDest = "Poolplate";/// HARDCODED; correction SUPSQCNG-435 => Poolplate
	public Integer outputSupportPosition;
	
	@Override
	public int compareTo(SampleSheetPoolLine o) {
		
		if(this.inputSupportCode.compareTo(o.inputSupportCode) == 0){
			return this.inputSupportContainerPosition.compareTo(o.inputSupportContainerPosition);
		}else{
			return this.inputSupportCode.compareTo(o.inputSupportCode);
		}		
	}
}
