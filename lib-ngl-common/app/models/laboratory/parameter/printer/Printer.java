package models.laboratory.parameter.printer;

import models.laboratory.parameter.Parameter;


public abstract class Printer extends Parameter{	
	
	protected Printer() {
		super("BBP11");

	}

	public String model ;
	
	public String location;

	public String ipAdress;

	public Integer port;	
	
}