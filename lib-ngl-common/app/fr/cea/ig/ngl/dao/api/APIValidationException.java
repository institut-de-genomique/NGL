package fr.cea.ig.ngl.dao.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import play.data.validation.ValidationError;

public class APIValidationException extends APIException {
	
	/**
	 * Eclipse requested.
	 */
	private static final long serialVersionUID = 1L;
	
	private final Map<String, List<ValidationError>> errors;

	public APIValidationException(String message) {
//		super(message);
//		this.errors = null;
		this(message, new HashMap<>());
	}
	
	public APIValidationException(String message, Throwable t) {
//		super(message, t);
//		this.errors = null;
		this(message, t, new HashMap<>());
	}
	
	public APIValidationException(String message, Map<String, List<ValidationError>> errors) {
		super(message);
		this.errors = errors;
	}
	
	public APIValidationException(String message, Throwable t, Map<String, List<ValidationError>> errors) {
		super(message, t);
		this.errors = errors;
	}

	public final Map<String, List<ValidationError>> getErrors() {
		return errors;
	}

	@Override
	public String getMessage() {
		StringBuilder sb = new StringBuilder();
		sb.append(super.getMessage());
		if (errors.size() > 0) {
			sb.append(" (");
			char comma = 0;
			for (Map.Entry<String,List<ValidationError>> me : errors.entrySet()) 
				for (ValidationError ve : me.getValue())
					for (String s : ve.messages()) {
						if (comma != 0)
							sb.append(comma);
						else
							comma = ',';
						sb.append(me.getKey())
						  .append(':')
						  .append(s);
					}
			sb.append(')');
		}
		return sb.toString();
	}
	
}
