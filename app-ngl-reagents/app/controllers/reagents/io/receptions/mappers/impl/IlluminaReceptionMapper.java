package controllers.reagents.io.receptions.mappers.impl;

import controllers.reagents.io.receptions.mappers.CommonReceptionMapper;
import validation.ContextValidation;

public class IlluminaReceptionMapper extends CommonReceptionMapper {

	static final String ILLUMINA_TYPE = "illumina-depot-reagent";
	static final String IMPORT_TYPE_FILE = "illumina-depot-reagents-reception";
	
	protected IlluminaReceptionMapper(ContextValidation contextValidation) {
		super(contextValidation);
	}
	
	@Override
	public String getTypeCode() {
		return ILLUMINA_TYPE;
	}

	@Override
	public String getImportTypeCode() {
		return IMPORT_TYPE_FILE;
	}

}
