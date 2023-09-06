package models.laboratory.sample.description;

import java.util.function.Supplier;

import fr.cea.ig.ngl.utils.SpringSupplier;
import models.laboratory.common.description.CommonInfoType;
import models.laboratory.sample.description.dao.SampleTypeDAO;
import ngl.refactoring.MiniDAO;

/**
 * Defines the type of a sample.
 * <p>
 * It is an attribute value and, if the attribute is mutable, a sample type cannot
 * be replaced by creating a sample subclass. The direct mapping would be to have
 * sample type subclasses that would define the extraneous data (the sample type instance
 * would hold the extra data). 
 * 
 * @author vrd
 *
 */
public class SampleType extends CommonInfoType {

	public static final Supplier<SampleTypeDAO>       find     = new SpringSupplier<>(SampleTypeDAO.class);
	public static final Supplier<MiniDAO<SampleType>> miniFind = MiniDAO.createSupplier(find);
	
	public SampleCategory category;

}
