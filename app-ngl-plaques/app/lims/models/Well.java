package lims.models;

import models.laboratory.common.instance.TBoolean;


public class Well {
	
	public Integer code;
	
	public String name;
	
	public Integer x;
	
	public String y;
	
	public Integer typeCode;
	
	public String typeName;

	public TBoolean valid;
	
	public String typeMaterial;

	@Override
	public String toString() {
	    return "Well [code=" + code + ", name=" + name + ", x=" + x
		    + ", y=" + y + ", typeCode=" + typeCode + ", typeName="
		    + typeName + ", valid=" + valid + ", typeMaterial="
		    + typeMaterial + "]";
	}
	
	
	
	

}
