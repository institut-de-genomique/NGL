package fr.cea.ig.ngl.utils;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.function.Supplier;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.mongojack.DBCursor;

import fr.cea.ig.DBObject;
import fr.cea.ig.MongoDBResult;

/**
 * 
 * @author aprotat
 *
 */
public final class MockUtils {
	
	private MockUtils() {}
	
	/**
	 * Usage:</br>
	 * <li>
	 * when(
	 * 		MongoDBDAO.find(any(), any(), any())
	 * )</br>
	 * .thenAnswer(
	 * 		MockUtils.answerDBResult(SomeFactory::someDBObjectList)
	 * );
	 * </li>
	 * @param <T> a DBObject type
	 * @param supplier
	 * @return {@link org.mockito.stubbing.Answer}
	 */
	@SuppressWarnings("unchecked")
	public static <T extends DBObject> Answer<MongoDBResult<T>> answerDBResult(Supplier<Iterable<T>> supplier) {
		return (InvocationOnMock invocation) -> {
			Iterable<T> iterable = supplier.get();
			Iterator<T> iterator = iterable.iterator();
			
			DBCursor<T> dbCursor = mock(DBCursor.class);
			when(dbCursor.iterator()).thenReturn(iterator);
			
			MongoDBResult<T> dbResult = mock(MongoDBResult.class);
			when(dbResult.getCursor()).thenReturn(dbCursor);
			
			return dbResult;
		};
	}
	
	/**
	 * Usage:</br>
	 * <li>
	 * when(
	 * 		MongoDBDAO.find(any(), any(), any())
	 * )</br>
	 * .thenAnswer(
	 * 		MockUtils.answerDBResult(someDBObjectCollection)
	 * );
	 * </li>
	 * @param <T> a DBObject type
	 * @param iterable
	 * @return {@link org.mockito.stubbing.Answer}
	 */
	public static <T extends DBObject> Answer<MongoDBResult<T>> answerDBResult(Iterable<T> iterable) {
		return answerDBResult(() -> iterable);
	}

	/**
	 * Usage:</br>
	 * <li>
	 * when(
	 * 		MongoDBDAO.find(any(), any(), any())
	 * )</br>
	 * .thenAnswer(
	 * 		MockUtils.answerDBResult(someDBObjectIterator)
	 * );
	 * </li>
	 * @param <T> a DBObject type
	 * @param iterator
	 * @return {@link org.mockito.stubbing.Answer}
	 */
	public static <T extends DBObject> Answer<MongoDBResult<T>> answerDBResult(Iterator<T> iterator) {
		return answerDBResult(() -> iterator);
	}
	
	/**
	 * Usage:</br>
	 * <li>
	 * when(
	 * 		MongoDBDAO.find(any(), any(), any())
	 * )</br>
	 * .thenAnswer(
	 * 		MockUtils.answerDBResult(obj1, obj2, obj3, etc...)
	 * );
	 * </li>
	 * @param <T> a DBObject type
	 * @param a an array of typed T objects
	 * @return {@link org.mockito.stubbing.Answer}
	 */
	@SuppressWarnings("unchecked")
	public static <T extends DBObject> Answer<MongoDBResult<T>> answerDBResult(T... a) {
		return answerDBResult(Arrays.asList(a));
	}
	
	/**
	 * Usage:</br>
	 * <li>
	 * when(
	 * 		MongoDBDAO.find(any(), any(), any())
	 * )</br>
	 * .thenAnswer(
	 * 		MockUtils.answerUniqueDBResult(someDBObject)
	 * );
	 * </li>
	 * @param <T> a DBObject type
	 * @param t an object of type T
	 * @return {@link org.mockito.stubbing.Answer}
	 */
	public static <T extends DBObject> Answer<MongoDBResult<T>> answerUniqueDBResult(T t) {
		return answerDBResult(Collections.singleton(t));
	}
	
	/**
	 * Usage:</br>
	 * <li>
	 * when(
	 * 		MongoDBDAO.find(any(), any(), any())
	 * )</br>
	 * .thenAnswer(
	 * 		MockUtils.answerEmptyDBResult()
	 * );
	 * </li>
	 * @param <T> a DBObject type
	 * @return {@link org.mockito.stubbing.Answer}
	 */
	public static <T extends DBObject> Answer<MongoDBResult<T>> answerEmptyDBResult() {
		return answerDBResult(Collections.emptyIterator());
	}
	
	/**
	 * Usage:</br>
	 * <li>
	 * when(
	 * 		MongoDBDAO.find(any(), any(), any())
	 * )</br>
	 * .thenAnswer(
	 * 		MockUtils.answerSuppliedDBResult(SomeFactory::someDBObject)
	 * );
	 * </li>
	 * @param <T> a DBObject type
	 * @param supplier
	 * @return {@link org.mockito.stubbing.Answer}
	 */
	public static <T extends DBObject> Answer<MongoDBResult<T>> answerSuppliedDBResult(Supplier<T> supplier) {
		return answerDBResult(() -> {
			T t = supplier.get();
			return Collections.singleton(t);
		});
	}

}
