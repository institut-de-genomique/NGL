package fr.cea.ig.ngl.dao.api;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.mongojack.DBQuery;

import com.mongodb.BasicDBObject;

import fr.cea.ig.ngl.dao.analyses.AnalysesDAO;
import models.laboratory.run.description.AnalysisType;
import models.laboratory.run.instance.Analysis;
import models.laboratory.run.instance.ReadSet;

// The simple way is to have the DAO exposed with the methods 
// prefixed by dao so there is no ambiguity when converting the
// existing code.
public class AnalysisAPI {
	
	private AnalysesDAO dao;
	
	@Inject
	public AnalysisAPI(AnalysesDAO dao) {
		this.dao = dao;
	}
	
	public Analysis getObject(String code, BasicDBObject keys) {
    	return dao.findByCode(code, keys);
    }
	
	public Analysis getObject(String code) {
    	return dao.findByCode(code);
    }
	
	public Analysis getObject(DBQuery.Query query) {
    	return dao.findOne(query);
    }
	
	public boolean isObjectExist(String code) {
		return dao.checkObjectExistByCode(code);
	}
	
	public boolean isObjectExist(DBQuery.Query query) {
		return dao.checkObjectExist(query);
	}
	
	public Analysis dao_saveObject(Analysis o) {
		return dao.save(o);
	}
	
//	protected void updateObject(T o) {
//		MongoDBDAO.update(collectionName, o);
//	}
//	
//	protected void updateObject(Query query, Builder builder){
//		MongoDBDAO.update(collectionName, type, query, builder);
//	}
//	
//	protected void deleteObject(String code){
//		MongoDBDAO.deleteByCode(collectionName,  type, code);
//	}

}

// Validation is the enforcement of constraints that are defined 
// by types. We suppose that the constraints could be defined as such.
// Some constraints could be seen as not legitimate as they rely
// on the operation and the state, not simply on the state (global or not).
// The code and id constraints are probably application level constraints
// as the only constraint in the store is that the id and the code are unique.
// Reading the validation code is quite painful as the method names are off,
// e.g. ValidationHelper.required (roughly returns true if the object is not null).
// A test like if (ValidationHelper.required(contextValidation, state, "state")) will
// fail not by returning a meaningful value but by adding an error to the validation
// context. 
class AnalysisConstraints {
	static class Constraint {
	}
	List<Constraint> constraints;
	AnalysisConstraints() {
		constraints = new ArrayList<>();
		// AnalysisValidationHelper.validateAnalysisType(this.typeCode, this.properties, contextValidation);
		// This is foreign key enforcement. In the current form this will remove
		// the other constraints check (if (...) { otherConstraints }).
		foreignKey("typeCode", AnalysisType.find, "code"); 
		
		//contextValidation.addKeyToRootKeyName("properties");
		//ValidationHelper.validateProperties(contextValidation, properties, analysisType.getPropertyDefinitionByLevel(Level.CODE.Analysis), true);
		//contextValidation.removeKeyFromRootKeyName("properties");
		// We assume that this kind of code checks if a set of joined properties
		// are properly joined. If the foreign key is OK, we check the properties
		// otherwise there is nothing to check. In the context of an analysis object,
		// we check that a set of properties from AnalysisType have been copied to the
		// property map. The following definition does not use the property map but
		// this would be the same (names are in fact JSON path). Given an object
		// of type T, we define and embedded join as the value of a referenced
		// object (reference by code that could be id) field into a path in the
		// referencing object. The original code checks if there is a value of the
		// correct type. 
		embeddedJoin(AnalysisType.class, "code", Analysis.class, "typeCode"); // embed analysisType.code as Analysis.typeCode 
		embeddedJoin(AnalysisType.class, "description", Analysis.class, "properties{'analysisType.description'}");
		
		// Check the state. It's an embedded object so the object is fully constructed
		// and can be checked using the standard validation.
		notNull(Analysis.class,"state");
		// It is a non contextual validation, this is a managed object validation.
		// could be declared as managedChild(...).
		// embeddedObject(Analysis.class,"state");
		managedObject(Analysis.class,"state");
		notNull(Analysis.class,"valuation");
		managedObject(Analysis.class,"valuation");
		notNull(Analysis.class,"traceInformation"); 
		// This is a simple coherence test. A simplification would be that
		// the trace information is actually created with the modification
		// date upon creation so the assertion is simply that not field is null.
		// Pretty obviously, the modification date cannot be earlier than the
		// creation date but as this thing is managed by the system, it
		// should not declared any constraint as the system handling should
		// simply be fail proof.
		managedObject(Analysis.class,"traceInformation"); 
		// Plural foreign key
		foreignKeys("masterReadSetCodes", ReadSet.class, "code");
		foreignKeys("readSetCodes",       ReadSet.class, "code");
		// Custom constraint, need some heavy DSL to build the constraint.
		// The else thing is pretty harsh to write.
		// Condition applies to readSets form masterReadSetCodes.
		// readset.code == "IW-BA"
		// || readSet.code == "IP-BA" && ! exists(analysis.readSetCode.contains(readSet.code) && analysis.state.code == "IP-BA")
		// and (eq ("state.code","N"));
		// Seems that there are integrity constraints that are relations between 
		// objects so they do not belong to either.
	}
	
	// Foreign key definition assumes that the code is the foreign key.
	// This would basically be foreignKey(String name, Class<T> c) that means that
	// the named field references by code and object of class T.
	void foreignKey(String k, Object o, String fk) {}
	void foreignKeys(String k, Object o, String fk) {}
	// This describes the copy of some declared fields from the analysis type
	// to the Analysis. The set of copied fields is simply a list of pairs
	// from a source path to a 
	void embeddedJoin(Object a, Object b, Object c, Object d) {}
	void notNull(Object a, Object b) {}
	void embeddedObject(Object a, Object b) {}
	void managedObject(Object a, Object b) {}
}






