package controllers.authorisation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import play.mvc.With;

/*
 * Exemple:
 *	 {@literal @}Permission(value={"54ki2"},teams={"THETEAM"})
 * 	
 * 
 * 	@author ydeshayes
 * 	@author michieli
 */
@With(PermissionAction.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Inherited
@Documented
public @interface Permission {
	String[] value() default "";//name/value permission
//	String[] teams() default "";//the teams
//	boolean  allPermissions() default false;//need to have all the permission(true) or just one(false) 
}

