package controllers.migration;

import models.laboratory.container.description.ContainerSupportCategory;
import validation.ContextValidation;

public class OneToManyContainer extends AtomicTransfertMethodOld {

	public int outputNumber;
	
//	public OneToManyContainer(){
//		super();
//	}
	
	@Override
	public void updateOutputCodeIfNeeded(ContainerSupportCategory outputCsc, String supportCode) {
	}
	
	@Override
	public void validate(ContextValidation contextValidation) {
		super.validate(contextValidation);
	}
	
}
