package controllers.instruments.io.cns.biomekfx;

public class SampleSheetPoolLine implements Comparable<SampleSheetPoolLine>{

	public String inputSupportCode; 
	public String inputContainerCode; 
	public String inputSupportSource;
	public Integer inputSupportContainerPosition;
	public Double inputSupportContainerVolume;

	public String outputSupportDest = "plaque a covariser";/// HARDCODED; 
	public String outputSupportCode; 
	public String outputContainerCode; 

	public Integer outputSupportPosition;
	public Double outputSupportContainerBufferVolume;

	@Override
	public int compareTo(SampleSheetPoolLine o) {
		if(this.inputSupportCode !=null && this.inputSupportContainerPosition != null){
			if(this.inputSupportCode.compareTo(o.inputSupportCode) == 0){
				return this.inputSupportContainerPosition.compareTo(o.inputSupportContainerPosition);
			}else{
				return this.inputSupportCode.compareTo(o.inputSupportCode);
			}		
		}else{
			if(this.outputSupportCode.compareTo(o.outputSupportCode) == 0){
				return this.outputSupportPosition.compareTo(o.outputSupportPosition);
			}else{
				return this.outputSupportCode.compareTo(o.outputSupportCode);
			}
		}
	}
}
