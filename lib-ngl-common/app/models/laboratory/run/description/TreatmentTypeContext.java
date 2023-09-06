package models.laboratory.run.description;

import static fr.cea.ig.lfw.utils.Equality.objectEquals;
import static fr.cea.ig.lfw.utils.Equality.typedEquals;
import static fr.cea.ig.lfw.utils.Hashing.hash;

import java.util.function.Supplier;

import fr.cea.ig.ngl.utils.SpringSupplier;
import models.laboratory.run.description.dao.TreatmentTypeContextDAO;

/**
 * Treatment context and a boolean, nothing clear.
 */
public class TreatmentTypeContext extends TreatmentContext {

	public static final Supplier<TreatmentTypeContextDAO> find = new SpringSupplier<>(TreatmentTypeContextDAO.class);

	public Boolean required = false;
	
	public TreatmentTypeContext() {
	}

	public TreatmentTypeContext(TreatmentContext tc, Boolean required) {
		super(tc.id, tc.code, tc.name);
		this.required = required;
	}
	
	@Override
	public int hashCode() {
		return hash(super.hashCode(),required);
	}

	@Override
	public boolean equals(Object obj) {
		return typedEquals(TreatmentTypeContext.class, this, obj,
				           (a,b) -> super.equals(obj) && objectEquals(a.required,b.required));
	}
}
