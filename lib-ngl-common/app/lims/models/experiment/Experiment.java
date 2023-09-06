package lims.models.experiment;

import java.util.Date;

import lims.models.instrument.Instrument;

import com.fasterxml.jackson.annotation.JsonInclude;
//import com.fasterxml.jackson.databind.annotation.JsonSerialize;
//import com.fasterxml.jackson.databind.annotation.JsonSerialize.Inclusion;

import play.data.validation.Constraints.Required;

// @JsonSerialize(include = Inclusion.NON_NULL)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Experiment {
	
	@Required
	public String containerSupportCode;
	
	public Date date;
	
	public Instrument instrument;

	public Integer nbCycles;
	
}
