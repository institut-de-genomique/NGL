package models.laboratory.reception.instance;

import static validation.utils.ValidationConstants.ERROR_NOTEXISTS_MSG;
import static validation.utils.ValidationConstants.ERROR_REQUIRED_MSG;
import static validation.utils.ValidationConstants.ERROR_SEVERAL_RESULT_MSG;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import org.mongojack.DBQuery;

import fr.cea.ig.MongoDBDAO;
import models.laboratory.parameter.index.Index;
import models.laboratory.reception.instance.ReceptionConfiguration.Action;
import models.utils.InstanceConstants;
import validation.ContextValidation;

public class TagExcelLightFieldConfiguration extends AbstractFieldConfiguration {
	
	public String headerValue;
	public Integer cellPosition;
	public Boolean tagCategory = Boolean.FALSE;

	public TagExcelLightFieldConfiguration() {
		super(AbstractFieldConfiguration.tagExcelLightType);
	}

	@Override
	public void populateField(Field field, Object dbObject, Map<Integer, String> rowMap,
			ContextValidation contextValidation, Action action) throws Exception {
		if (rowMap.containsKey(cellPosition)) {
			String value = rowMap.get(cellPosition);
			
			Index index = getIndex(contextValidation, value);
			if (index != null && !tagCategory.booleanValue()) {
				populateField(field, dbObject, index.code);
			} else if (index != null && tagCategory.booleanValue()) {
				populateField(field, dbObject, index.categoryCode);
			}			
		} else if (required) {
			contextValidation.addError(headerValue, ERROR_REQUIRED_MSG);
		}

	}
	
	private Index getIndex(ContextValidation contextValidation, String value) {
		DBQuery.Query q = DBQuery.in("typeCode", "index-illumina-sequencing","index-nanopore-sequencing","index-pacbio-sequencing", "index-mgi-sequencing").is("name", value);
		List<Index> indexes = MongoDBDAO.find(InstanceConstants.PARAMETER_COLL_NAME, Index.class, q).toList();
		switch (indexes.size()) {
		case 0  : contextValidation.addError(headerValue, ERROR_NOTEXISTS_MSG, value); return null;
		case 1  : return indexes.get(0);
		default : contextValidation.addError(headerValue, ERROR_SEVERAL_RESULT_MSG, value); return null;
		}
	}

	@Override
	public void updateFromHeader(ContextValidation vc, Map<Integer, String> header) {
		if (header.containsKey(cellPosition)) {
			headerValue = header.get(cellPosition);
		} else {
			vc.addError("Headers","not found header for cell position " + cellPosition);
		}
	}

}
