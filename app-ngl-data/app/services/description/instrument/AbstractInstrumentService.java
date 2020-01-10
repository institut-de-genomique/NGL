package services.description.instrument;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import models.laboratory.common.description.Institute;
import models.laboratory.container.description.ContainerSupportCategory;
import models.laboratory.instrument.description.Instrument;
import models.laboratory.instrument.description.InstrumentCategory;
import models.laboratory.instrument.description.InstrumentUsedType;
import models.utils.dao.DAOException;
import models.utils.dao.DAOHelpers;
//import play.Logger;
import play.data.validation.ValidationError;

public abstract class AbstractInstrumentService {
	
	private static final play.Logger.ALogger logger = play.Logger.of(AbstractInstrumentService.class);
	
	public void main(Map<String, List<ValidationError>> errors) throws DAOException{
		
		logger.debug("Begin remove Instrument Used Type");
		DAOHelpers.removeAll(InstrumentUsedType.class, InstrumentUsedType.find);
		
		logger.debug("Begin remove Instrument Category !!!");
		DAOHelpers.removeAll(InstrumentCategory.class, InstrumentCategory.find);
		
		logger.debug("Begin save categories");
		saveInstrumentCategories(errors);
		
		logger.debug("Begin save Instrument Used Type");
		saveInstrumentUsedTypes(errors);	
		
		logger.debug("End Instrument service");
	}

	public abstract void saveInstrumentUsedTypes(Map<String, List<ValidationError>> errors) throws DAOException;
		
	public abstract void saveInstrumentCategories(Map<String, List<ValidationError>> errors) throws DAOException;

	
	protected static Instrument createInstrument(String code, String name, String shortName, Boolean active, String path, List<Institute> institutes) {
		Instrument i = new Instrument();
		i.code = code;
		i.name = name;
		i.active=active;
		i.path=path;
		i.institutes=institutes;
		i.shortName = shortName;
		return i;
	}

	protected static List<Instrument> getInstruments(Instrument...instruments) {
		List<Instrument> linstruments = new ArrayList<>(); 
		for (Instrument instrument : instruments) {
			linstruments.add(instrument); 
		}
		return linstruments; 
	}

	
	protected static List<ContainerSupportCategory> getContainerSupportCategories(String[] codes) throws DAOException{		
		return DAOHelpers.getModelByCodes(ContainerSupportCategory.class,ContainerSupportCategory.find, codes);
	}
}
