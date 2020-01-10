package models.laboratory.reagent.description;

import play.data.validation.Constraints.Required;
import validation.IValidation;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import fr.cea.ig.DBObject;

// This has to do with null deserialization. Jackson 2.8 seems to derserialize 
// null using the defaultImpl definition (which in a way makes some sense as it has
// no type information). 
@JsonTypeInfo(use = Id.NAME, include = As.PROPERTY, property = "category", defaultImpl = models.laboratory.reagent.description.ReagentCatalog.class)
// @JsonTypeInfo(use = Id.NAME, include = As.PROPERTY, property = "category")
// @JsonTypeInfo(use = Id.NAME, include = As.PROPERTY, property = "category", defaultImpl = Void.class)
@JsonSubTypes({
	@JsonSubTypes.Type(value =  models.laboratory.reagent.description.ReagentCatalog.class, name = "Reagent"),
	@JsonSubTypes.Type(value =  models.laboratory.reagent.description.BoxCatalog.class,     name = "Box"),
	@JsonSubTypes.Type(value =  models.laboratory.reagent.description.KitCatalog.class,     name = "Kit")
})
public abstract class AbstractCatalog extends DBObject implements IValidation {

	@Required
	public String name;
	public String catalogRefCode;
	public boolean active = true;
	
//	public AbstractCatalog() {
//	super();
//}

}