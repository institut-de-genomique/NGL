package builder.data;

import models.laboratory.common.instance.TBoolean;
import models.laboratory.common.instance.Valuation;

public class LaboratoryValuationBuilder {

	Valuation valuation = new Valuation();
	
	public LaboratoryValuationBuilder withValid(TBoolean valid)
	{
		valuation.valid=valid;
		return this;
	}
	
	public Valuation build()
	{
		return valuation;
	}
}
