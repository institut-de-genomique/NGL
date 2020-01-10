package fr.cea.ig.ngl;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import play.data.validation.ValidationError;
import play.mvc.Result;

public interface APINGLController {

	// Mandatory methods
	public abstract Result head(String code);
	public abstract Result list();
	public abstract Result get(String code);
	public abstract Result save();
	public abstract Result update(String code) ;
	
	default Map<String, List<ValidationError>> mapErrors(List<ValidationError> formErrors) {
		Map<String, List<ValidationError>> map = new TreeMap<>(); 
		formErrors.forEach(ve -> {
			if(map.containsKey(ve.key())) {
				map.get(ve.key()).add(ve);
			} else {
				map.put(ve.key(), Arrays.asList(ve));
			}
		});
		return map;
	}
	
}
