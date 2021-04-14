package controllers.migration;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import models.laboratory.common.instance.Comment;
import models.laboratory.container.description.ContainerSupportCategory;
import models.laboratory.experiment.instance.OutputContainerUsed;
import validation.ContextValidation;
import validation.IValidation;

@JsonTypeInfo(use=Id.NAME, include=As.PROPERTY, property="class", defaultImpl= OneToOneContainer.class)
@JsonSubTypes({
	@JsonSubTypes.Type(value =  ManyToOneContainer.class, name = "ManyToOne"),
	@JsonSubTypes.Type(value =  OneToManyContainer.class, name = "OneToMany"),
	@JsonSubTypes.Type(value =  OneToOneContainer.class, name = "OneToOne"),
	@JsonSubTypes.Type(value =  OneToVoidContainer.class, name = "OneToVoid")
})
public abstract class AtomicTransfertMethodOld implements IValidation {

	public List<InputContainerUsedOld> inputContainerUseds;
	public List<OutputContainerUsed> outputContainerUseds;
	public String line;
	public String column;
	public Comment comment;
	
//	public AtomicTransfertMethodOld() {
//		super();
//	}
	
	public abstract void updateOutputCodeIfNeeded(ContainerSupportCategory outputCsc, String supportCode);

	@Override
	public void validate(ContextValidation contextValidation) {
		
	}
	
}
