package models.laboratory.run.description;

import java.util.function.Supplier;

import fr.cea.ig.ngl.utils.SpringSupplier;
import models.laboratory.common.description.CommonInfoType;
import models.laboratory.run.description.dao.AnalysisTypeDAO;
import ngl.refactoring.MiniDAO;

public class AnalysisType extends CommonInfoType {

	public static final Supplier<AnalysisTypeDAO>       find     = new SpringSupplier<>(AnalysisTypeDAO.class);
	
	public static final Supplier<MiniDAO<AnalysisType>> miniFind = MiniDAO.createSupplier(find);
}
