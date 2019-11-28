package models.laboratory.experiment.description;

import java.util.function.Supplier;

import fr.cea.ig.ngl.utils.SpringSupplier;
import models.laboratory.common.description.AbstractCategory;
import models.laboratory.experiment.description.dao.ExperimentCategoryDAO;
import ngl.refactoring.MiniDAO;
import ngl.refactoring.state.ContainerStateNames;

//public class ExperimentCategory extends AbstractCategory<ExperimentCategory>{
public class ExperimentCategory extends AbstractCategory {

	public enum CODE {
		purification, 
		qualitycontrol, 
		transfert, 
		transformation, 
		voidprocess
	}

//	public static final ExperimentCategoryFinder find = new ExperimentCategoryFinder(); 
//	public static final ExperimentCategoryDAO find = Spring.getBeanOfType(ExperimentCategoryDAO.class); 
	public static final Supplier<ExperimentCategoryDAO> find = new SpringSupplier<>(ExperimentCategoryDAO.class); 
	public static final Supplier<MiniDAO<ExperimentCategory>> miniFind = MiniDAO.createSupplier(find);

	// Serialization constructor
	public ExperimentCategory() {}
	
//	public ExperimentCategory(String code, String name) {
//		super(code,name);
//	}
	
	public ExperimentCategory(CODE code, String name) {
		super(code.name(), name);
	}
	
//	public ExperimentCategory() {
//		super(ExperimentCategoryDAO.class.getName());
//	}
//
//	@Override
//	protected Class<? extends AbstractDAO<ExperimentCategory>> daoClass() {
//		return ExperimentCategoryDAO.class;
//	}
	
//	public static class ExperimentCategoryFinder extends Finder<ExperimentCategory,ExperimentCategoryDAO> {
//
////		public ExperimentCategoryFinder() {
////			super(ExperimentCategoryDAO.class.getName());			
////		}
//		public ExperimentCategoryFinder() { super(ExperimentCategoryDAO.class);	}
//		
//		public List<ExperimentCategory> findByProcessTypeCode(String processTypeCode) throws DAOException{
////			return ((ExperimentCategoryDAO)getInstance()).findByProcessTypeCode(processTypeCode);
//			return getInstance().findByProcessTypeCode(processTypeCode);
//		}
//		
//	}

	public static String getContainerStateFromExperimentCategory(String categoryCode) {
		if (categoryCode == null)
			return null;
		switch (ExperimentCategory.CODE.valueOf(categoryCode)) {
		case transformation : return ContainerStateNames.A_TM;
		case transfert      : return ContainerStateNames.A_TF;
		case qualitycontrol : return ContainerStateNames.A_QC;
		case purification   : return ContainerStateNames.A_PF;
		default             : return null;
		}
	}

}
