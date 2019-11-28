package models.laboratory.run.description;

import static fr.cea.ig.lfw.utils.Equality.objectEquals;
import static fr.cea.ig.lfw.utils.Equality.typedEquals;
import static fr.cea.ig.lfw.utils.Hashing.hash;

import java.util.function.Supplier;

import fr.cea.ig.ngl.utils.SpringSupplier;
import models.laboratory.run.description.dao.TreatmentContextDAO;
import models.utils.Model;

public class TreatmentContext extends Model {
	
	public static final Supplier<TreatmentContextDAO> find = new SpringSupplier<>(TreatmentContextDAO.class);
	
	public String name;
	
	public TreatmentContext() {
	}
	
	public TreatmentContext(String name) {
		this.code = name;
		this.name = name;
	}
	
	protected TreatmentContext(Long id, String code, String name) {
		this.id   = id;
		this.code = code;
		this.name = name;
	}
	
	@Override
	public int hashCode() {
		return hash(super.hashCode(),name);
	}

	@Override
	public boolean equals(Object obj) {
		return typedEquals(TreatmentContext.class, this, obj,
				           (a,b) -> super.equals(obj) && objectEquals(a.name,b.name));
	}

}
