package models.laboratory.project.description;

import java.util.function.Supplier;

import fr.cea.ig.ngl.utils.SpringSupplier;
import models.laboratory.common.description.CommonInfoType;
import models.laboratory.project.description.dao.UmbrellaProjectTypeDAO;
import ngl.refactoring.MiniDAO;

public class UmbrellaProjectType extends CommonInfoType {

	public static final Supplier<UmbrellaProjectTypeDAO>       find     = new SpringSupplier<>(UmbrellaProjectTypeDAO.class);
	public static final Supplier<MiniDAO<UmbrellaProjectType>> miniFind = MiniDAO.createSupplier(find); 

	public UmbrellaProjectCategory category;
	
}
