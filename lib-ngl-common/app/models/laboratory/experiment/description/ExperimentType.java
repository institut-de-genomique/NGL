package models.laboratory.experiment.description;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.fasterxml.jackson.annotation.JsonIgnore;

import fr.cea.ig.ngl.utils.SpringSupplier;
import models.laboratory.common.description.CommonInfoType;
import models.laboratory.experiment.description.dao.ExperimentTypeDAO;
import models.laboratory.experiment.instance.AtomicTransfertMethod;
import models.laboratory.instrument.description.InstrumentUsedType;
import models.laboratory.sample.description.SampleType;
import ngl.refactoring.MiniDAO;

/**
 * Parent class categories not represented by a table in the database
 * Database relationship for experiment with instrumentUsedType and protocol are represented in CommonInfoType table
 * 
 * @author ejacoby
 *
 */
public class ExperimentType extends CommonInfoType {
 
	@JsonIgnore
	public static final Supplier<ExperimentTypeDAO>       find     = new SpringSupplier<>(ExperimentTypeDAO.class);
	public static final Supplier<MiniDAO<ExperimentType>> miniFind = MiniDAO.createSupplier(find);
	
	public ExperimentCategory       category;
	
	// Relationship accessible by the parent table in the database
	/**
	 * List of instrument types that can be used to execute this type
	 * of experiment. 
	 */
	public List<InstrumentUsedType> instrumentUsedTypes = new ArrayList<>();
	
	// public List<Protocol> protocols = new ArrayList<Protocol>();
	
	/**
	 * Reference to the type of mapping for this experiment type ({@link AtomicTransfertMethod}).
	 */
	public String                   atomicTransfertMethod;
	
	public String                   shortCode;
	
	public Boolean                  newSample = Boolean.FALSE;
	
	/**
	 * List of sample types that can be used for this type of experiment.
	 */
	public List<SampleType>         sampleTypes = new ArrayList<>();
	
}
