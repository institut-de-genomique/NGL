package models.laboratory.common.description;

import java.util.function.Supplier;

import fr.cea.ig.ngl.utils.SpringSupplier;
import models.laboratory.common.description.dao.ValueDAO;
import models.utils.Model;

/**
 * Possible value of property definition
 * @author ejacoby
 *
 */
public class Value extends Model {

	public static final Supplier<ValueDAO> find = new SpringSupplier<>(ValueDAO.class); 

	public String value;  // used as code but not rename because strong impact will be remove after
	public String name;
	public Boolean defaultValue = Boolean.FALSE;

}
