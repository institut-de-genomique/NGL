package models.utils;

import models.utils.code.Code;
import play.api.modules.spring.Spring;

// Singleton
public class CodeHelper {

	// INJECT: should be accessed through Guice instead of Spring 
	public static Code getInstance() {			
		return Spring.getBeanOfType(Code.class);
	}
	
}
