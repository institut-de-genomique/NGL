package models.laboratory.common.description;

import java.util.List;
import java.util.function.Supplier;

import fr.cea.ig.ngl.utils.SpringSupplier;
import models.laboratory.common.description.dao.StateDAO;
import models.utils.Model;
import ngl.refactoring.MiniDAO;

// This link : {@link models.laboratory.common.description.State}

/**
 * Value of the possible state of type.
 * 
 * @author ejacoby
 * @author dnoisett
 * 
 */
public class State extends Model {

    public static final Supplier<StateDAO>       find     = new SpringSupplier<>(StateDAO.class);
    public static final Supplier<MiniDAO<State>> miniFind = MiniDAO.createSupplier(find);
    
    public String           name;
    public boolean          active;
    public Integer          position;
    public StateCategory    category;
    public List<ObjectType> objectTypes;
    
    public boolean          display;
    public String           functionnalGroup;
    
}
