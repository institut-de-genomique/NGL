//package models.utils;
//
//import models.utils.Model.Finder;
//import models.utils.dao.AbstractDAO;
//import models.utils.dao.DAOException;
//
//import java.util.function.Supplier;
//
//import com.fasterxml.jackson.annotation.JsonIgnore;
//
///**
// * Object used to retrieve an object define in the NGL SQL DB.
// * In SQL, code is unique, it's the data processing label
// * 
// * @author galbini
// *
// */
//public class ObjectSGBDReference<T extends Model<T>> implements IFetch<T> {
//	
//	@JsonIgnore
//	private Class<T> className;
////	@JsonIgnore
////	private Class<AbstractDAO<T>> className;
//	
//	public String code;
//	
//	public ObjectSGBDReference() {		
//	}
//	
//	public ObjectSGBDReference(Class<T> className, String code) {
//		this.className = className;
//		this.code = code;
//	}
//	
//	public ObjectSGBDReference(Class<T> className){
//		this(className,null);
//	}
//	
////	public ObjectSGBDReference(Class<AbstractDAO<T>> className, String code) {
////		this.className = className;
////		this.code = code;
////	}
//	
////	public ObjectSGBDReference(Class<AbstractDAO<T>> className) {
////		this(className,null);
////	}
//
//	@Override
//	public T getObject() throws DAOException {
//		// init Finder from class DAO associated to this class in package ./dao
////		Finder<T> find = new Finder<T>(className.getName().replaceAll("description", "description.dao") + "DAO");
//		try {
//			// Perlish hackery, PHP level hackery if i may say so
//			Class<AbstractDAO<T>> c = (Class<AbstractDAO<T>>)Class.forName(className.getName().replaceAll("description", "description.dao") + "DAO");
//			Finder<T,AbstractDAO<T>> find = new Finder<>(c);
//			return find.findByCode(code);
////			return new Finder<>(className).findByCode(code);
//		} catch (ClassNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			throw new RuntimeException(e);
//		}
//	}
//
//}
