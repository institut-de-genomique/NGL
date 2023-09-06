package controllers.reagents.io.receptions.mappers;

import models.laboratory.reagent.instance.AbstractReception;

public interface ReceptionTypeMapper {
	
	public String getTypeCode();
	
	public String getImportTypeCode();
	
	public default void setCorrectTypes(AbstractReception reception) {
		reception.typeCode = getTypeCode();
		reception.importTypeCode = getImportTypeCode();
	}

}
