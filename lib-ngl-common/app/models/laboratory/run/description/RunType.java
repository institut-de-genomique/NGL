package models.laboratory.run.description;

import java.util.function.Supplier;

import fr.cea.ig.ngl.utils.SpringSupplier;
import models.laboratory.common.description.CommonInfoType;
import models.laboratory.run.description.dao.RunTypeDAO;
import ngl.refactoring.MiniDAO;

public class RunType extends CommonInfoType {
	
	public static final Supplier<RunTypeDAO>       find     = new SpringSupplier<>(RunTypeDAO.class);
	public static final Supplier<MiniDAO<RunType>> miniFind = MiniDAO.createSupplier(find); 

	public RunCategory category;
	public Integer     nbLanes;	
}