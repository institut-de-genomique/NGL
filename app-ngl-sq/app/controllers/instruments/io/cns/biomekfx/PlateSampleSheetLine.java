package controllers.instruments.io.cns.biomekfx;

public class PlateSampleSheetLine implements Comparable<PlateSampleSheetLine> {

	public String inputContainerCode;
	public String outputContainerCode;
	
	public String sourceADN;
	public Integer swellADN;
	
	public Integer dwell;
	
	public Double inputVolume;
	public Double bufferVolume;
	
	@Override
	public int compareTo(PlateSampleSheetLine o) {
		return dwell.compareTo(o.dwell);			
	}
		
}
