package controllers.instruments.io.cns.gilsonpipetmax;

public class GilsonSampleSheetLine implements Comparable<GilsonSampleSheetLine> {
	public String inputSupportCode;
	public String outputSupportCode;
	
	public String inputSupportSource; //Position du support en entrée (ordre a prendre en compte pour def sourcePos
	public String outputSupportSource;//Position du support en sortie (ordre a prendre en compte pour def destPos
	
	public String inputContainerCode;
	public String outputContainerCode;
	public String sampleName;
	
	public Double dnaVol; //Affiché dans la FDR
	public Double bufferVol; //Affiché dans la FDR
	
	public String inputContainerColumn;
	public String inputContainerLine;
	public Integer sourcePos; //Affiché dans la FDR
	
	public String outputContainerColumn;
	public String outputContainerLine;	
	public Integer destPos; //Affiché dans la FDR
	
	public String protocolName; //Affiché dans la FDR
	
	@Override
	public int compareTo(GilsonSampleSheetLine o) {
		return this.destPos.compareTo(o.destPos);			
	}
	
	
}
