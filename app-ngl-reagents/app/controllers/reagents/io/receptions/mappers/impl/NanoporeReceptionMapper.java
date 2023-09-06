package controllers.reagents.io.receptions.mappers.impl;

import controllers.reagents.io.receptions.mappers.CommonReceptionMapper;
import validation.ContextValidation;

public class NanoporeReceptionMapper extends CommonReceptionMapper {
	
	static final String NANOPORE_TYPE = "nanopore-reagent";
	static final String IMPORT_TYPE_FILE = "nanopore-reagents-reception";

	protected NanoporeReceptionMapper(ContextValidation contextValidation) {
		super(contextValidation);
	}

	@Override
	public String getTypeCode() {
		return NANOPORE_TYPE;
	}

	@Override
	public String getImportTypeCode() {
		return IMPORT_TYPE_FILE;
	}

}
