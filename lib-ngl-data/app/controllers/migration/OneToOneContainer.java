package controllers.migration;

import models.laboratory.container.description.ContainerSupportCategory;
import validation.ContextValidation;

public class OneToOneContainer extends AtomicTransfertMethodOld {

//	public OneToOneContainer(){
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
