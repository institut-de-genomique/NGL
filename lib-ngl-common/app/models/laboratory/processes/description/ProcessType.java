package models.laboratory.processes.description;

import java.util.List;
import java.util.function.Supplier;

import fr.cea.ig.ngl.utils.SpringSupplier;
import models.laboratory.common.description.CommonInfoType;
import models.laboratory.experiment.description.ExperimentType;
import models.laboratory.processes.description.dao.ProcessTypeDAO;
import ngl.refactoring.MiniDAO;

public class ProcessType extends CommonInfoType {

//	@SuppressWarnings("hiding")
//	public static final ProcessTypeFinder find = new ProcessTypeFinder(); 
//	@SuppressWarnings("hiding")
//	public static final ProcessTypeDAO find = Spring.getBeanOfType(ProcessTypeDAO.class); 
	@SuppressWarnings("hiding")
	public static final Supplier<ProcessTypeDAO>       find     = new SpringSupplier<>(ProcessTypeDAO.class);
	public static final Supplier<MiniDAO<ProcessType>> miniFind = MiniDAO.createSupplier(find);
	
	public ProcessCategory             category;
	public List<ProcessExperimentType> experimentTypes;
	public ExperimentType              voidExperimentType;
	public ExperimentType              firstExperimentType;
	public ExperimentType              lastExperimentType;
	
//	public ProcessType() {
//		super(ProcessTypeDAO.class.getName());
//	}
	
//	public List<PropertyDefinition> getPropertiesDefinitionDefaultLevel() {
//		return getPropertyDefinitionByLevel(Level.CODE.Process);
//	}
	
//	public static class ProcessTypeFinder extends Finder<ProcessType,ProcessTypeDAO> {
//
////		public ProcessTypeFinder() {
////			super(ProcessTypeDAO.class.getName());			
////		}
//		public ProcessTypeFinder() { super(ProcessTypeDAO.class);	}
//		
//		public List<ProcessType> findByProcessCategoryCodes(String...processCategoryCode) throws DAOException {
////			return ((ProcessTypeDAO)getInstance()).findByProcessCategoryCodes(processCategoryCode);
//			return getInstance().findByProcessCategoryCodes(processCategoryCode);
//		}
//
//		public List<ProcessType> findByExperimentTypeCode(String experimentTypeCode) throws DAOException {
////			return ((ProcessTypeDAO)getInstance()).findByExperimentCode(experimentTypeCode);
//			return getInstance().findByExperimentCode(experimentTypeCode);
//		}
//		
//		public List<ProcessType> findAllLight() throws DAOException {
////			return ((ProcessTypeDAO)getInstance()).findAllLight();
//			return getInstance().findAllLight();
//		}
//		
//	}
	
}
