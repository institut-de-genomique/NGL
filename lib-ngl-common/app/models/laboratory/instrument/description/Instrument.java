package models.laboratory.instrument.description;

import java.util.List;
import java.util.function.Supplier;

import com.fasterxml.jackson.annotation.JsonIgnore;

import fr.cea.ig.ngl.utils.SpringSupplier;
import models.laboratory.common.description.Institute;
import models.laboratory.instrument.description.dao.InstrumentDAO;
import models.utils.Model;
import ngl.refactoring.MiniDAO;

//public class Instrument extends Model<Instrument> {
public class Instrument extends Model {

//	public static final InstrumentFinder find = new InstrumentFinder();
	public static final Supplier<InstrumentDAO> find = new SpringSupplier<>(InstrumentDAO.class);// Spring.getBeanOfType(InstrumentDAO.class);
	public static final Supplier<MiniDAO<Instrument>> miniFind = MiniDAO.createSupplier(find);

	public String shortName;
	public String name;
	public Boolean active;
	public String path;
	
	@JsonIgnore
	public InstrumentUsedType instrumentUsedType;	
	
	@JsonIgnore
	public List<Institute> institutes;
	
	/* used only to send in json */
	public String typeCode;
	public String categoryCode;	
	
//	public Instrument() {
//		super(InstrumentDAO.class.getName());
//	}
//		
//	@Override
//	protected Class<? extends AbstractDAO<Instrument>> daoClass() {
//		return InstrumentDAO.class;
//	}
	
//	public static class InstrumentFinder extends Finder<Instrument,InstrumentDAO> {
//
////		public InstrumentFinder() {
////			super(InstrumentDAO.class.getName());			
////		}
//		public InstrumentFinder() { super(InstrumentDAO.class); }
//		
//		public List<Instrument> findByQueryParams(InstrumentQueryParams instrumentsQueryParams) throws DAOException {
////			return ((InstrumentDAO)getInstance()).findByQueryParams(instrumentsQueryParams);
//			return getInstance().findByQueryParams(instrumentsQueryParams);
//		}
//		
//		public List<Instrument> findByExperimentTypeQueryParams(InstrumentQueryParams instrumentsQueryParams) throws DAOException {
////			return ((InstrumentDAO)getInstance()).findByExperimentTypeQueryParams(instrumentsQueryParams);
//			return getInstance().findByExperimentTypeQueryParams(instrumentsQueryParams);
//		}
//		
//		public void cleanCache() throws DAOException {
////			 ((InstrumentDAO)getInstance()).cleanCache();
//			 getInstance().cleanCache();
//		}
//		
//	}

}
