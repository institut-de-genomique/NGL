package controllers.experiments.api;


import java.util.List;

import org.apache.commons.lang3.ObjectUtils;

import controllers.ListForm;

public class ExperimentTypeNodesSearchForm extends ListForm{
	public String code;
	public List<String> codes;
	
	public String experimentCode;
	public List<String> experimentCodes;
	
	public ExperimentTypeNodesSearchParams getParams() {
		ExperimentTypeNodesSearchParams experimentTypeNodeSearchParams = new ExperimentTypeNodesSearchParams();
		experimentTypeNodeSearchParams.experimentCode = experimentCode;
		experimentTypeNodeSearchParams.experimentCodes = experimentCodes;
		return experimentTypeNodeSearchParams;
	}
	
	public static class ExperimentTypeNodesSearchParams {
		
		public String experimentCode;
		public List<String> experimentCodes;
		
		public boolean isAtLeastOneParam(){
			return ObjectUtils.anyNotNull(experimentCode, experimentCodes);
		}
		
	}
}
