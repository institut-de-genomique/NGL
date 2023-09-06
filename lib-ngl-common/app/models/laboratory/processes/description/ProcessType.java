package models.laboratory.processes.description;

import java.util.List;
import java.util.function.Supplier;

import fr.cea.ig.ngl.utils.SpringSupplier;
import models.laboratory.common.description.CommonInfoType;
import models.laboratory.experiment.description.ExperimentType;
import models.laboratory.processes.description.dao.ProcessTypeDAO;
import ngl.refactoring.MiniDAO;

public class ProcessType extends CommonInfoType {

	public static final Supplier<ProcessTypeDAO>       find     = new SpringSupplier<>(ProcessTypeDAO.class);
	public static final Supplier<MiniDAO<ProcessType>> miniFind = MiniDAO.createSupplier(find);
	
	public ProcessCategory             category;
	public List<ProcessExperimentType> experimentTypes;
	public ExperimentType              voidExperimentType;
	public ExperimentType              firstExperimentType;
	public ExperimentType              lastExperimentType;
	
}
