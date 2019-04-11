package models.laboratory.common.description;

import java.util.function.Supplier;

import fr.cea.ig.ngl.utils.SpringSupplier;
import models.laboratory.common.description.dao.InstituteDAO;
import models.utils.Model;

/**
 * Value of institute (only 2 possible values : CNG {@literal &} CNS)
 * 
 * @author dnoisett
 *
 */
public class Institute extends Model {

	public static final Supplier<InstituteDAO> find = new SpringSupplier<>(InstituteDAO.class); 
	
	public String name;
	
	// Serialization constructor
	public Institute() {}
	
	public Institute(String code, String name) {
		super(code);
		this.name = name;
	}
	
}
