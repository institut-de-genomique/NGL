package models.laboratory.common.description;

import java.util.function.Supplier;

import fr.cea.ig.ngl.utils.SpringSupplier;
import models.laboratory.common.description.dao.MeasureUnitDAO;
import models.utils.Model;

public class MeasureUnit extends Model {

	public static Supplier<MeasureUnitDAO> find = new SpringSupplier<>(MeasureUnitDAO.class);

	public String value;   	
	public Boolean defaultUnit = Boolean.FALSE;
	// multiple par rapport à une référence ex L et µL 10-6
	public MeasureCategory category;
	
}
