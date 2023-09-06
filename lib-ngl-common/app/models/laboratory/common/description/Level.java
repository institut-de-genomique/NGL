package models.laboratory.common.description;

import static fr.cea.ig.lfw.utils.Equality.objectEquals;
import static fr.cea.ig.lfw.utils.Equality.typedEquals;
import static fr.cea.ig.lfw.utils.Hashing.hash;

import java.util.function.Supplier;

import fr.cea.ig.ngl.utils.SpringSupplier;
import models.laboratory.common.description.dao.LevelDAO;
import models.utils.Model;

/**
 * Defines the object type for which a property definition is valid.
 * This is possibly a duplicate of {@link models.laboratory.common.description.ObjectType} depending on the
 * object types that define the properties attribute.
 *  
 * @author vrd
 *
 */
public class Level extends Model {
	
	/**
	 * Standard access point to DAO.
	 */
	public static final Supplier<LevelDAO> find = new SpringSupplier<>(LevelDAO.class); 
	
	// not used ContentIn, ContentOut, ContainerSupportIn, ContainerSupportOut
	public enum CODE {
		
		/**
		 * Container.
		 */
		Container,
		
		/**
		 * Container input used.
		 */
		ContainerIn,
		
		/**
		 * Container output used.
		 */
		ContainerOut,
		
		Content, 
		ContainerSupport, 
		Experiment, 
		Instrument, 
		Project, 
		UmbrellaProject,
		Process, 
		Run, 
		Sample, 
		Lane, 
		ReadSet, 
		File,
		Read1, 
		Read2, 
		Single, 
		Pairs, 
		Default, 
		Analysis
	}
			
	public String name;
	
	// Serialization constructor
	public Level() {}
	
	public Level(Level.CODE code) {
		this.code = code.name();
		this.name = code.name();	
	}
	
	@Override
	public int hashCode() {
		return hash(super.hashCode(),name);
	}

	@Override
	public boolean equals(Object obj) {
		return typedEquals(Level.class, this, obj,
				           (a,b) -> super.equals(obj) && objectEquals(a.name,b.name));
	}
	
}
