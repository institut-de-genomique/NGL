package models.laboratory.run.description;

import java.util.function.Supplier;

import fr.cea.ig.ngl.utils.SpringSupplier;
import models.laboratory.common.description.AbstractCategory;
import models.laboratory.run.description.dao.TreatmentCategoryDAO;
import ngl.refactoring.MiniDAO;

public class TreatmentCategory extends AbstractCategory {
	
	public static enum CODE {
		ngsrg, 
		global, 
		sequencing, 
		quality, 
		ba,
		bionanoAccess,
		// NGL-3967
		ccsReport
	}

	public static final Supplier<TreatmentCategoryDAO>       find     = new SpringSupplier<>(TreatmentCategoryDAO.class);
	public static final Supplier<MiniDAO<TreatmentCategory>> miniFind = MiniDAO.createSupplier(find);

	// Serialization constructor
	public TreatmentCategory() {}
	
	public TreatmentCategory(String code, String name) {
		super(code,name);
	}
	
	public TreatmentCategory(CODE code, String name) {
		this(code.name(),name);
	}
	
}
