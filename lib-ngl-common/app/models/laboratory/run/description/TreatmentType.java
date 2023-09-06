package models.laboratory.run.description;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import fr.cea.ig.ngl.utils.SpringSupplier;
import models.laboratory.common.description.CommonInfoType;
import models.laboratory.run.description.dao.TreatmentTypeDAO;
import ngl.refactoring.MiniDAO;

public class TreatmentType extends CommonInfoType {

	public static final Supplier<TreatmentTypeDAO>       find     = new SpringSupplier<>(TreatmentTypeDAO.class);
	public static final Supplier<MiniDAO<TreatmentType>> miniFind = MiniDAO.createSupplier(find); 

	public TreatmentCategory          category;
	public String                     names;	
	public List<TreatmentTypeContext> contexts = new ArrayList<>();
	public String                     displayOrders;
	
}
