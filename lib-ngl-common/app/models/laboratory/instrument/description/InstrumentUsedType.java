package models.laboratory.instrument.description;

import java.util.List;
import java.util.function.Supplier;

import fr.cea.ig.ngl.utils.SpringSupplier;
import models.laboratory.common.description.CommonInfoType;
import models.laboratory.container.description.ContainerSupportCategory;
import models.laboratory.instrument.description.dao.InstrumentUsedTypeDAO;
import ngl.refactoring.MiniDAO;

/**
 * Entity type used to declare properties that will be indicated with the use of the instrument.
 * 
 * @author ejacoby
 *
 */
public class InstrumentUsedType extends CommonInfoType {
	
	public static final Supplier<InstrumentUsedTypeDAO>       find     = new SpringSupplier<>(InstrumentUsedTypeDAO.class); 
	public static final Supplier<MiniDAO<InstrumentUsedType>> miniFind = MiniDAO.createSupplier(find);
	
	public List<Instrument>               instruments;
	public InstrumentCategory             category;
	public List<ContainerSupportCategory> inContainerSupportCategories;
	public List<ContainerSupportCategory> outContainerSupportCategories;
	
}
