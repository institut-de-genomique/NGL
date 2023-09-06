package lims.models.instrument;

import com.fasterxml.jackson.annotation.JsonInclude;
//import com.fasterxml.jackson.databind.annotation.JsonSerialize;
//import com.fasterxml.jackson.databind.annotation.JsonSerialize.Inclusion;

//@JsonSerialize(include = Inclusion.NON_NULL)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Instrument {
	
	public String code;
	public String categoryCode;
	public Boolean active;
	public String path;

}
