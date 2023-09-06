package models.laboratory.parameter.printer;

import java.util.List;


public abstract class EPL2Printer extends Printer {

	public boolean inverseList = false ;
	public String defaultSpeed;
	public String defaultDensity;
	public String defaultBarcodePositionId;	
	public List<BarcodePosition> barcodePositions ;
	
		
}
