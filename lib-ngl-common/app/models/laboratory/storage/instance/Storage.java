package models.laboratory.storage.instance;

import java.util.List;
import java.util.function.Supplier;

import org.mongojack.MongoCollection;

import com.fasterxml.jackson.annotation.JsonIgnore;

import fr.cea.ig.DBObject;
import fr.cea.ig.ngl.dao.StoragesDAO;
import fr.cea.ig.ngl.utils.GuiceSupplier;
import validation.ContextValidation;
import validation.IValidation;

/**
 * 
 * Instances stock represents all positions in box, shelf, storageDevic, room, floor and building 
 * Code name is like  
 * 
 * Stock are referenced in Container
 *  
 * @author mhaquell
 *
 */
@MongoCollection(name="Stock")
public class Storage extends DBObject implements IValidation {
	
	public static final Supplier<StoragesDAO> find = new GuiceSupplier<>(StoragesDAO.class);
	
	// Place
	public String buildingCode;
//	Not necessarry
	// public String floorCode;
	public String roomCode;
	
	// Conteneur
	public String storageDeviceCode; 
	public String shelf;
	
	// Box
	public String boxCode;
	public String x;
	public String y;
	
	// History stocks/support
	public List<StorageHistory> stockUsed;

	@JsonIgnore
	@Override
	public void validate(ContextValidation contextValidation) {
	}

}
