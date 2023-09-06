package models.laboratory.project.description;

import java.util.function.Supplier;

import fr.cea.ig.ngl.utils.SpringSupplier;
import models.laboratory.common.description.CommonInfoType;
import models.laboratory.project.description.dao.ProjectTypeDAO;
import ngl.refactoring.MiniDAO;

public class ProjectType extends CommonInfoType {

	public static final Supplier<ProjectTypeDAO>       find     = new SpringSupplier<>(ProjectTypeDAO.class);
	public static final Supplier<MiniDAO<ProjectType>> miniFind = MiniDAO.createSupplier(find); 

	public ProjectCategory category;
	
}
