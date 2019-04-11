package models.laboratory.common.description;


import java.util.function.Supplier;

import fr.cea.ig.ngl.utils.SpringSupplier;
import models.laboratory.common.description.dao.StateHierarchyDAO;
import models.utils.Model;

public class StateHierarchy extends Model {
	
    public static final Supplier<StateHierarchyDAO> find = new SpringSupplier<>(StateHierarchyDAO.class);
    
    public String  childStateCode;
    public String  childStateName;
    public String  parentStateCode;
    public String  objectTypeCode;
    public Integer position;
    public String  functionnalGroup; 

}
