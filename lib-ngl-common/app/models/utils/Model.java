package models.utils;

import static fr.cea.ig.lfw.utils.Equality.objectEquals;
import static fr.cea.ig.lfw.utils.Equality.typedEquals;
import static fr.cea.ig.lfw.utils.Hashing.hash;

import com.fasterxml.jackson.annotation.JsonIgnore;

import fr.cea.ig.DBObject;

/**
 * Base class for the SQL mapped objects (equivalent to {@link DBObject} but for the SQL mapping).
 * <p>
 * SQL mapped objects define:
 * <ul>
 *   <li>an identifier that is auto generated in the SQL RDBMS and acts like a MongoDB identifier (like {@link DBObject#_id})</li>
 *   <li>a user friendly code that is unique in collection but not necessarily unique across distinct collections (like {@link DBObject#code})</li>
 * </ul>
 * Note: as the code is unique in tables, the identifier is superfluous.
 */
public abstract class Model {

	/**
	 * Auto generated primary key. 
	 */
	public Long   id;	
	
	/**
	 * User frie
	 */
	public String code;
	
	// Serialization constructor
	public Model() {}
	
	public Model(String code) {
		this.code = code;
	}
		
	@Override
	public int hashCode() {
		return hash(1,code);
	}	

	@JsonIgnore
	@Override
	public boolean equals(Object obj) {
		return typedEquals(Model.class, this, obj,
				           (a,b) -> objectEquals(a.code, b.code));
	}

}
