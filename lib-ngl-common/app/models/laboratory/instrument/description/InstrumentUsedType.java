package models.laboratory.instrument.description;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import models.laboratory.common.description.CommonInfoType;
import models.laboratory.common.description.Level;
import models.laboratory.common.description.PropertyDefinition;
import models.laboratory.container.description.ContainerSupportCategory;
import models.laboratory.instrument.description.dao.InstrumentUsedTypeDAO;
import models.utils.dao.DAOException;

/**
 * Entity type used to declare properties that will be indicated with the use of the instrument
 * 
 * @author ejacoby
 *
 */
public class InstrumentUsedType extends CommonInfoType {
	
	@SuppressWarnings("hiding")
	public static final InstrumentUsedTypeFinder find = new InstrumentUsedTypeFinder(); 
	
	public List<Instrument> instruments;
	public InstrumentCategory category;
	public List<ContainerSupportCategory> inContainerSupportCategories;
	public List<ContainerSupportCategory> outContainerSupportCategories;
	
	public InstrumentUsedType() {
		super(InstrumentUsedTypeDAO.class.getName());
	}
	
	@JsonIgnore
	public List<PropertyDefinition> getPropertiesDefinitionDefaultLevel() {
		return getPropertyDefinitionByLevel(Level.CODE.Instrument);
	}
	
	public static class InstrumentUsedTypeFinder extends Finder<InstrumentUsedType,InstrumentUsedTypeDAO> {

//		public InstrumentUsedTypeFinder() {
//			super(InstrumentUsedTypeDAO.class.getName());			
//		}
		public InstrumentUsedTypeFinder() { super(InstrumentUsedTypeDAO.class);	}
		
		public List<InstrumentUsedType> findByExperimentTypeCode(String instrumentUsedTypeCode) throws DAOException {
//			return ((InstrumentUsedTypeDAO)getInstance()).findByExperimentTypeCode(instrumentUsedTypeCode);
			return getInstance().findByExperimentTypeCode(instrumentUsedTypeCode);
		}
		
		public void cleanCache() throws DAOException{
//			 ((InstrumentUsedTypeDAO)getInstance()).cleanCache();
			 getInstance().cleanCache();
		}
		
	}
	
}
