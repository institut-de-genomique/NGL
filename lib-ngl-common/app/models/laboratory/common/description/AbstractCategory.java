package models.laboratory.common.description;

import static fr.cea.ig.lfw.utils.Hashing.hash;
import static fr.cea.ig.lfw.utils.Equality.objectEquals;
import static fr.cea.ig.lfw.utils.Equality.typedEquals;

import models.utils.Model;

/**
 * Categories have a code ({@link Model}) and a name.
 * <p>
 * Parent class categories not represented by a table in the database.
 * 
 * @author ejacoby
 *
 */
public abstract class AbstractCategory extends Model {

	public String name;

	// Serialization constructor
	public AbstractCategory() {}
	
	public AbstractCategory(String code, String name) {
		super(code);
		this.name = name;
	}

	@Override
	public int hashCode() {
		return hash(super.hashCode(),name);
	}

	@Override
	public boolean equals(Object obj) {
		return typedEquals(AbstractCategory.class, this, obj, 
				           (a,b) -> super.equals(obj) && objectEquals(a.name,b.name));
	}

}
