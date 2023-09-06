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

public class TagExcelFieldConfiguration extends AbstractFieldConfiguration {

	public String headerValue;
	public Integer cellSequence;
	//public Integer cellCode; 04/07/2017 FDS utiliser le Name et pas le code
	public Integer cellName;	
	public Boolean tagCategory = Boolean.FALSE;
	
	public TagExcelFieldConfiguration() {
		super(AbstractFieldConfiguration.tagExcelType);		
	}

	@Override
	public void populateField(Field field, Object dbObject, Map<Integer, String> rowMap, ContextValidation contextValidation, Action action) throws Exception {
		if (rowMap.containsKey(cellSequence) && rowMap.containsKey(cellName)) {
			String sequence = rowMap.get(cellSequence);
			String name     = rowMap.get(cellName);
			// Get Index
			Index index = getIndex(contextValidation, sequence, name);
			if (index != null && !tagCategory.booleanValue()) {
				populateField(field, dbObject, index.code);
			} else if (index != null && tagCategory.booleanValue()) {
				populateField(field, dbObject, index.categoryCode);
			}
		} else if(rowMap.containsKey(cellSequence)){
			String sequence = rowMap.get(cellSequence);
			
			Index index = getIndex(contextValidation, sequence, null);
			if (index != null && !tagCategory.booleanValue()) {
				populateField(field, dbObject, index.code);
			} else if (index != null && tagCategory.booleanValue()) {
				populateField(field, dbObject, index.categoryCode);
			}
		} else if (required) {
			contextValidation.addError(headerValue, ERROR_REQUIRED_MSG);
		}
	}

	//04/07/2017 FDS utiliser le name et pas le code
//	private Index getIndex(ContextValidation contextValidation, String sequence, String name) {
//		String additionalErrInfo = "'" + sequence + "'/'" + name + "'";
//        
//		DBQuery.Query q = DBQuery.in("typeCode", "index-illumina-sequencing","index-nanopore-sequencing").is("sequence", sequence);
//		if (name != null) {
//			q.is("name", name);
//		}
//		
//		Index index = null; 
//		List<Index> indexes = MongoDBDAO.find(InstanceConstants.PARAMETER_COLL_NAME, Index.class, q).toList();
//		
//		if (indexes.size() == 1) {
//			index = indexes.get(0);
//		} else if (indexes.size() == 0) {
//			contextValidation.addError(headerValue, ERROR_NOTEXISTS_MSG, additionalErrInfo);
//		} else {
//			contextValidation.addError(headerValue, ERROR_SEVERAL_RESULT_MSG, additionalErrInfo);
//		}
//		return index;
//	}
	private Index getIndex(ContextValidation contextValidation, String sequence, String name) {
		String additionalErrInfo = "'" + sequence + "'/'" + name + "'";
		DBQuery.Query q = DBQuery.in("typeCode", "index-illumina-sequencing","index-nanopore-sequencing","index-pacbio-sequencing", "index-mgi-sequencing").is("sequence", sequence);
		if (name != null) 
			q.is("name", name);
		List<Index> indexes = MongoDBDAO.find(InstanceConstants.PARAMETER_COLL_NAME, Index.class, q).toList();
		switch (indexes.size()) {
		case 0  : contextValidation.addError(headerValue, ERROR_NOTEXISTS_MSG, additionalErrInfo); return null;
		case 1  : return indexes.get(0);
		default : contextValidation.addError(headerValue, ERROR_SEVERAL_RESULT_MSG, additionalErrInfo); return null;
		}
	}

	@Override
//	public void updateFromHeader(ContextValidation vc, Map<Integer, String> header) {
//		if (header.containsKey(cellSequence) && header.containsKey(cellName)) {
//			headerValue = header.get(cellSequence) + " / " + header.get(cellName);
//		} else if (header.containsKey(cellSequence)) {
//			headerValue = header.get(cellSequence);
//		} else {
//			vc.addError("Headers", "not found header for cell position " + cellSequence);
//		}
//	}
	public void updateFromHeader(ContextValidation vc, Map<Integer, String> header) {
		if (header.containsKey(cellSequence)) {
			if (header.containsKey(cellName)) {
				headerValue = header.get(cellSequence) + " / " + header.get(cellName);
			} else {
				headerValue = header.get(cellSequence);
			}
		} else {
			vc.addError("Headers", "not found header for cell position " + cellSequence);
		}
	}

}
