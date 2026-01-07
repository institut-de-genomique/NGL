package controllers.reagents.io.receptions.mappers.impl;

import controllers.reagents.io.receptions.mappers.CommonReceptionMapper;
import validation.ContextValidation;

public class PacBioReceptionMapper extends CommonReceptionMapper {
	
	static final String PACBIO_TYPE = "pacbio-reagent";
	static final String IMPORT_TYPE_FILE = "pacbio-reagents-reception";

	public PacBioReceptionMapper(ContextValidation contextValidation) {
		super(contextValidation);
	}

	@Override
	public String getTypeCode() {
		return PACBIO_TYPE;
	}

	@Override
	public String getImportTypeCode() {
		return IMPORT_TYPE_FILE;
	}

}
