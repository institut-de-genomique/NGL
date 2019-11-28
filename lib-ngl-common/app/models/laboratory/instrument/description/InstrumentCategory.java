package models.laboratory.instrument.description;

import java.util.function.Supplier;

import fr.cea.ig.ngl.utils.SpringSupplier;
import models.laboratory.common.description.AbstractCategory;
import models.laboratory.instrument.description.dao.InstrumentCategoryDAO;
import ngl.refactoring.MiniDAO;

//public class InstrumentCategory extends AbstractCategory<InstrumentCategory> {
public class InstrumentCategory extends AbstractCategory {
	
//	public static final InstrumentCategoryFinder find = new InstrumentCategoryFinder(); 
//	public static final InstrumentCategoryDAO find = Spring.getBeanOfType(InstrumentCategoryDAO.class); 
	public static final Supplier<InstrumentCategoryDAO> find = new SpringSupplier<>(InstrumentCategoryDAO.class);
	public static final Supplier<MiniDAO<InstrumentCategory>> miniFind = MiniDAO.createSupplier(find); 
	
	// Serialization constructor
	public InstrumentCategory() {}
	
	public InstrumentCategory(String code, String name) {
		super(code,name);
	}
	
//	public InstrumentCategory() {
//		super(InstrumentCategoryDAO.class.getName());
//	}
//	
//	@Override
//	protected Class<? extends AbstractDAO<InstrumentCategory>> daoClass() {
//		return InstrumentCategoryDAO.class;
//	}
	
//	public static class InstrumentCategoryFinder extends Finder<InstrumentCategory,InstrumentCategoryDAO> {
//
////		public InstrumentCategoryFinder() {
////			super(InstrumentCategoryDAO.class.getName());			
////		}
//		public InstrumentCategoryFinder() { super(InstrumentCategoryDAO.class); }
//		
//		public List<InstrumentCategory> findByInstrumentUsedTypeCode(String instrumentTypeCode) throws DAOException {
////			return ((InstrumentCategoryDAO)getInstance()).findByInstrumentUsedTypeCode(instrumentTypeCode);
//			return getInstance().findByInstrumentUsedTypeCode(instrumentTypeCode);
//		}
//		
//	}

}
