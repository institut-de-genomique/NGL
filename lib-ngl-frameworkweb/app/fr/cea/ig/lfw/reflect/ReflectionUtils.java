package fr.cea.ig.lfw.reflect;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import fr.cea.ig.lfw.utils.HashMapBuilder;
import fr.cea.ig.lfw.utils.Iterables;
import fr.cea.ig.util.function.F1;
import fr.cea.ig.util.function.F2;

/**
 * Reflection based utilities.
 * 
 * @author vrd
 *
 */
public class ReflectionUtils {

//	private static final play.Logger.ALogger logger = play.Logger.of(ReflectionUtils.class);
	
	/**
	 * Extracts the first type argument from generic class given a 
	 * subclass of the generic class. 
	 * @param <D>          generic parameter defining class type
	 * @param <T>          subclass to start search from
	 * @param genericClass generic class to extract type parameter from
	 * @param someClass    subclass of the generic class that binds the generic type
	 * @return             type argument of the generic class
	 */
	@SuppressWarnings("unchecked")
	public static <D,T> Class<T> getDefiningClassTypeArgument(Class<D> genericClass, Class<? extends D> someClass) {
		return (Class<T>)net.jodah.typetools.TypeResolver.resolveRawArguments(genericClass, someClass)[0];
	}

	/**
	 * Assert not null value.
	 * @param field field
	 * @param value value to test
	 */
	private static void assertNotNull(Field field, Object value) {
		if (value == null)
			throw new RuntimeException("not argument value for " + field.getName());
	}
	
//	private static void assertNotBlank(Field field, String value) {
//		if (StringUtils.isBlank(value))
//			throw new RuntimeException("not argument value for " + field.getName());
//	}
	
	/**
	 * Assert that no values element is blank. 
	 * @param field  field
	 * @param values values to test
	 */
	private static void assertNotBlank(Field field, String[] values) {
		for (int i=0; i<values.length; i++) 
			if (StringUtils.isBlank(values[i]))
				throw new RuntimeException("not argument value for " + field.getName() + " at position " + i);
	}
	
	/**
	 * Assert that there is exactly one value in the values array.
	 * @param field  field
	 * @param values values
	 */
	private static void assertOne(Field field, String[] values) {
		if (values.length != 1)
			throw new RuntimeException("more than one value for afield " + field.getName() + " of type " + field.getType());
	}
	
	/**
	 * Single value converter (required value).
	 * @param <T> converted value type
	 * @param sc  string to value converter
	 * @return    converter instance
	 */
	private static <T> F2<Field, String[],T> singleValueConverter(F1<String, T> sc) {
		return (field, values) -> {
			 assertNotNull (field, values);
			 assertNotBlank(field, values);
			 assertOne     (field, values);
			 return sc.apply(values[0]);
		 };
	}
	
	/**
	 * Array value converter (required values).
	 * @param <T> converted value type
	 * @param c   value class
	 * @param sc  string to value converter
	 * @return    converter instance
	 */
	private static <T> F2<Field, String[], Object> arrayValueConverter(Class<T> c, F1<String, T> sc) {
		 return (field, values) -> {
			 assertNotNull (field, values);
			 assertNotBlank(field, values);
			 @SuppressWarnings("unchecked")
			T[] tab = (T[]) Array.newInstance(c, values.length);
			 for (int i=0; i<tab.length; i++) 
				 tab[i] = sc.apply(values[i]);
			 return tab;
		 };
	}
	
	/**
	 * Converters for supported types.
	 */
	private static final HashMap<Class<?>, F2<Field, String[], Object>> converters =
			new HashMapBuilder<Class<?>, F2<Field, String[], Object>>()
			.put(String .class, singleValueConverter(s -> s))
			.put(short  .class, singleValueConverter(s -> Short.parseShort(s)))
			.put(int    .class, singleValueConverter(s -> Integer.parseInt(s)))
			.put(long   .class, singleValueConverter(s -> Long.parseLong(s)))
			.put(float  .class, singleValueConverter(s -> Float.parseFloat(s)))
			.put(double .class, singleValueConverter(s -> Double.parseDouble(s)))
			.put(Short  .class, singleValueConverter(s -> Short.parseShort(s)))
			.put(Integer.class, singleValueConverter(s -> Integer.parseInt(s)))
			.put(Long   .class, singleValueConverter(s -> Long.parseLong(s)))
			.put(Float  .class, singleValueConverter(s -> Float.parseFloat(s)))
			.put(Double .class, singleValueConverter(s -> Double.parseDouble(s)))
			.put(String[].class,
				 (field, values) -> {
					 assertNotNull (field, values);
					 assertNotBlank(field, values); 
					 return values;
				 })
			.put(short[].class,
					 (field, values) -> {
						 assertNotNull (field, values);
						 assertNotBlank(field, values);
						 short[] tab = new short[values.length];				
						 for (int i=0; i<tab.length; i++) 
							 tab[i] = Short.parseShort(values[i]);
						 return tab;
					 })
			.put(int[].class,
				 (field, values) -> {
					 assertNotNull (field, values);
					 assertNotBlank(field, values);
					 int[] tab = new int[values.length];				
					 for (int i=0; i<tab.length; i++) 
						 tab[i] = Integer.parseInt(values[i]);
					 return tab;
				 })
			.put(long[].class,
					 (field, values) -> {
						 assertNotNull (field, values);
						 assertNotBlank(field, values);
						 long[] tab = new long[values.length];				
						 for (int i=0; i<tab.length; i++) 
							 tab[i] = Long.parseLong(values[i]);
						 return tab;
					 })
			.put(float[].class,
					 (field, values) -> {
						 assertNotNull (field, values);
						 assertNotBlank(field, values);
						 float[] tab = new float[values.length];				
						 for (int i=0; i<tab.length; i++) 
							 tab[i] = Float.parseFloat(values[i]);
						 return tab;
					 })
			.put(double[].class,
					 (field, values) -> {
						 assertNotNull (field, values);
						 assertNotBlank(field, values);
						 double[] tab = new double[values.length];				
						 for (int i=0; i<tab.length; i++) 
							 tab[i] = Double.parseDouble(values[i]);
						 return tab;
					 })
			.put(Short[]  .class, arrayValueConverter(Short  .class, s -> Short.parseShort(s)))
			.put(Integer[].class, arrayValueConverter(Integer.class, s -> Integer.parseInt(s)))
			.put(Long[]   .class, arrayValueConverter(Long   .class, s -> Long.parseLong(s)))
			.put(Float[]  .class, arrayValueConverter(Float  .class, s -> Float.parseFloat(s)))
			.put(Double[] .class, arrayValueConverter(Double .class, s -> Double.parseDouble(s)))
			.put(Optional.class,
				 (field, values) -> {
					 if (values == null)
						 return Optional.empty();
					 Type vType = ((ParameterizedType)field.getGenericType()).getActualTypeArguments()[0];
					 return Optional.of(getValue(values, field, Class.forName(vType.getTypeName())));
				 })
			.put(boolean.class, 
				 (field, values) -> {
					 assertNotNull(field, values);
					 if (values.length == 0)
						 return true;
					 assertOne(field, values);
					 switch (values[0].toLowerCase()) {
					 case "true"  : return true;
					 case "yes"   : return true;
					 case "false" : return false;
					 case "no"    : return false;
					 default      : throw new RuntimeException("unknown boolean value '" + values[0] + "'");
					 }
				 })
			.asMap();
	
	/**
	 * Converts a string array to a typed value.
	 * @param args       data to convert
	 * @param f          field
	 * @param c          type to convert to
	 * @return           converted value
	 * @throws Exception conversion error
	 */
	public static Object getValue(String[] args, Field f, Class<?> c) throws Exception {
		F2<Field, String[], Object> converter = converters.get(c);
		if (converter == null)
			throw new RuntimeException("type '" + c.getName() + "' is not handled");
		return converter.apply(f, args);		
	}
	
	/**
	 * Creates and populates data of a given class with a map of values to
	 * be interpreted as values of instance fields.
	 * @param <T>        type of instance to populate
	 * @param c          class of instance to populate
	 * @param args       arguments (typically a HTTP request)
	 * @return           created and populated instance of input class
	 * @throws Exception error
	 */
	public static <T> T readInstance(Class<T> c, Map<String, String[]> args) throws Exception {
		assertStructFieldDefinitions(c);
		T t = c.newInstance();
		Iterable<Field> fields             = Iterables.zenThem(c.getFields());
		Set<String>     expectedFieldNames = Iterables.map(fields, f -> f.getName()).toSet();
		Set<String>     argNames           = args.keySet();
		List<String>    unexpectedArgs     = Iterables.filter(argNames, x -> !expectedFieldNames.contains(x)).toList();
		if (unexpectedArgs.size() > 0)
			throw new RuntimeException("unexpected arguments: " + Iterables.intercalate(unexpectedArgs, ", ").asString());
		for (Field f : fields)
			f.set(t, getValue(args.get(f.getName()), f, f.getType()));
		return t;
	}

	/**
	 * Assert that the field definitions in a class hierarchy are all
	 * public instance fields.
	 * @param c class to check fields of
	 */
	public static void assertStructFieldDefinitions(Class<?> c) {
		if (c == Object.class)
			return;
		Field[] fields = c.getDeclaredFields();
		for (Field field : fields)
			if (Modifier.isStatic(field.getModifiers()) || ! Modifier.isPublic(field.getModifiers()))
				throw new RuntimeException("not a public instance field " + c.getName() + "." + field.getName());
		assertStructFieldDefinitions(c.getSuperclass());
	}
	
}
