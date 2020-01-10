package models.utils;

/**
 * @author mhaquell
 *
 * @param <T>  object Type  
 */
public interface IFetch<T> {
	
	public T getObject() throws Exception;

}
