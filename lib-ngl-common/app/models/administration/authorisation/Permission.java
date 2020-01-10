package models.administration.authorisation;

import java.util.function.Supplier;

import com.fasterxml.jackson.annotation.JsonIgnore;

import fr.cea.ig.ngl.utils.SpringSupplier;
import models.administration.authorisation.description.dao.PermissionDAO;
import models.utils.Model;

/**
 * 
 * @author michieli
 *
 */
public class Permission extends Model {

	@JsonIgnore
	public static final Supplier<PermissionDAO> find = new SpringSupplier<>(PermissionDAO.class);
	
	public String label;

}
