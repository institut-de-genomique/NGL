package controllers.migration;

import models.laboratory.container.description.ContainerSupportCategory;
import validation.ContextValidation;

public class OneToVoidContainer extends AtomicTransfertMethodOld {

	@Override
	public void validate(ContextValidation contextValidation) {
		super.validate(contextValidation);
	}

	@Override
	public void updateOutputCodeIfNeeded(ContainerSupportCategory outputCsc, String supportCode) {
		// TODO Auto-generated method stub		
	}

}
