package ngl.refactoring;

//import java.lang.annotation.Documented;
//import java.lang.annotation.ElementType;
//import java.lang.annotation.Retention;
//import java.lang.annotation.RetentionPolicy;
//import java.lang.annotation.Target;

import validation.ContextValidation;

/**
 * Express as much as possible constraints using annotations.
 *  
 * @author vrd
 *
 */
public class NGLConstraints {
	
//	/**
//	 * The field or method is a primary field in the persisted collection (like
//	 * a SQL unique).
//	 * 
//	 * @author vrd
//	 *
//	 */
//	@Retention(RetentionPolicy.RUNTIME)
//	@Target({ElementType.FIELD,ElementType.METHOD})
//	@Documented
//	public @interface Primary {
//	}
//	
//	/**
//	 * The annotated field or getter references an instance of the argument
//	 * class.
//	 * 
//	 * @author vrd
//	 *
//	 */
//	@Retention(RetentionPolicy.RUNTIME)
//	@Target({ElementType.FIELD,ElementType.METHOD})
//	@Documented
//	public @interface Foreign {
//		Class<?> value();
//	}
//	
//	/**
//	 * The annotated field or getter value cannot be null, if neither Required
//	 * or Optional is specified, the field is supposed to be optional.
//	 * This cannot be used with {@link Optional}.
//	 * 
//	 * @author vrd
//	 *
//	 */
//	@Retention(RetentionPolicy.RUNTIME)
//	@Target({ElementType.FIELD,ElementType.METHOD})
//	@Documented	
//	public @interface Optional {
//	}
//	
//	/**
//	 * The annotated field or getter value can be null. This cannot be used with
//	 * {@link Required} and the default constraint behavior is to forbid null values.
//	 * 
//	 * @author vrd
//	 *
//	 */
//	@Retention(RetentionPolicy.RUNTIME)
//	@Target({ElementType.FIELD,ElementType.METHOD})
//	@Documented
//	public @interface Required {
//	}
//	
//	public @interface Property {		
//	}
	
}

interface IConstraint<T> {
	void validate(ContextValidation ct, T t);
}

class DeclaredConstraints {
	
	public static void constraint(Class<?> c) {
		
	}
	
}
