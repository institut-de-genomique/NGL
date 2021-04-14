package models.laboratory.processes.description;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import fr.cea.ig.ngl.utils.SpringSupplier;
import models.laboratory.experiment.description.ExperimentType;
import models.laboratory.processes.description.dao.ExperimentTypeNodeDAO;
import models.utils.Model;

public class ExperimentTypeNode extends Model {

	public static final Supplier<ExperimentTypeNodeDAO> find = new SpringSupplier<>(ExperimentTypeNodeDAO.class);

	// Possibility to do purification
	public boolean doPurification          = false; // Boolean.FALSE;
	public boolean mandatoryPurification   = false; // Boolean.FALSE;
	
	// Possibility to do quality control
	public boolean doQualityControl        = false; // Boolean.FALSE;
	public boolean mandatoryQualityControl = false; // Boolean.FALSE;

	// Possibility to do transfert
	public boolean doTransfert             = false; // Boolean.FALSE;
	public boolean mandatoryTransfert      = false; // Boolean.FALSE;

	public ExperimentType           experimentType;
	public List<ExperimentTypeNode> previousExperimentTypeNodes = new ArrayList<>();
	public List<ExperimentType>     previousExperimentTypes;
	public List<ExperimentType>     possibleQualityControlTypes = new ArrayList<>();
	public List<ExperimentType>     possiblePurificationTypes   = new ArrayList<>();
	public List<ExperimentType>     possibleTransferts          = new ArrayList<>();
	
}
