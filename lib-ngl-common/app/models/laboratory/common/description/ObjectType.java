package models.laboratory.common.description;

import java.util.function.Supplier;

import fr.cea.ig.ngl.utils.SpringSupplier;
import models.laboratory.common.description.dao.ObjectTypeDAO;
import models.utils.Model;

/**
 * Type definition
 * 
 * @author ejacoby
 *
 */
public class ObjectType extends Model {
	
	public static final Supplier<ObjectTypeDAO> find = new SpringSupplier<>(ObjectTypeDAO.class); 

	public enum CODE {
		Project, 
		Process, 
		Sample, 
		Container, 
		Instrument, 
		Reagent,
		Experiment, 
		Import, 
		Run, 
		Treatment, 
		ReadSet, 
		Analysis, 
		SRASubmission, 
		SRAConfiguration, 
		SRAStudy, 
		SRASample, 
		SRAExperiment,
		ReagentReception
	} 

	// Set true if type has additional attributes compared to commonInfoType
	public Boolean generic;
		
	// Serialization constructor
	public ObjectType() {}
	
	public ObjectType(CODE code, boolean generic) {
		super(code.name());
		this.generic = generic;
	}
	
	public ObjectType(CODE code) {
		this(code, false);
	}
	
}

