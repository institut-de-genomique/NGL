package models.laboratory.sample.instance.tree;

import java.util.Set;

public class From {

	public String projectCode;
	public String sampleCode;
	public String sampleTypeCode;
	
	public String experimentCode;
	public String experimentTypeCode;
	
	public String containerCode;
	public String supportCode;
	
	@Deprecated
	public Set<String> fromTransformationTypeCodes;
	@Deprecated
	public Set<String> fromTransformationCodes;
	
	public Set<String> processTypeCodes;
	public Set<String> processCodes;

}
