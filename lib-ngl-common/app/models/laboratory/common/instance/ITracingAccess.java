package models.laboratory.common.instance;

import validation.ContextValidation;

/**
 * Trace information holder. Implementors are required to define the
 * getTraceInformation that must return a non null TraceInformation instance.
 * 
 * @author vrd
 *
 */
public interface ITracingAccess {
		
	/**
	 * Trace information, must be created if needed.
	 * @return trace information
	 */
	TraceInformation getTraceInformation();
	
	/**
	 * Set the creation stamp using the current user and the current time.
	 * @param ctx      validation context
	 * @param userName user name to stamp trace with
	 */
	default void setTraceCreationStamp(ContextValidation ctx, String userName) {
		getTraceInformation().creationStamp(ctx,userName);
	}
	
	/**
	 * Set the update stamp using the current user and the current time.
	 * @param ctx      validation context
	 * @param userName user name to stamp trace with
	 */
	default void setTraceModificationStamp(ContextValidation ctx, String userName) {
		getTraceInformation().modificationStamp(ctx,userName);
	}
	
	/**
	 * Set the update stamp using the current user and the current time.
	 * @param userName user name to stamp trace with
	 */
	default void setTraceUpdateStamp(String userName) {
		getTraceInformation().modificationStamp(userName);
	}
	
}
