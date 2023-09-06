package models.laboratory.reagent.instance;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import fr.cea.ig.DBObject;
import models.laboratory.common.instance.Comment;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.reagent.description.AbstractCatalog;
import models.laboratory.reagent.description.KitCatalog;
import models.utils.InstanceConstants;
import validation.ContextValidation;
import validation.IValidation;
import validation.common.instance.CommonValidationHelper;
import validation.reagent.instance.ReceptionValidationHelper;
import validation.utils.ValidationHelper;

@JsonTypeInfo(use = Id.NAME, include = As.PROPERTY, property = "category", defaultImpl = models.laboratory.reagent.instance.ReagentReception.class)
@JsonSubTypes({
	@JsonSubTypes.Type(value = models.laboratory.reagent.instance.ReagentReception.class, name = "Reagent"),
	@JsonSubTypes.Type(value = models.laboratory.reagent.instance.BoxReception.class, name = "Box"),
})
public abstract class AbstractReception extends DBObject implements IValidation {

	/** Numéro de lot. */
	public String batchNumber;

	/** Date à la réception du produit. */
	public Date receptionDate;

	/** Code Ref catalogue. */
	public String catalogRefCode;
	
	/** Nom du Catalogue Kit associé **/
	public String kitCatalogName;

	/** Code du fournisseur */
	public String providerCode;

	/** Id chez le fournisseur. */
	public String fromProviderId;

	/** date de début d'utilisation */
	public Date startUseDate;

	/** date de fin d'utilisation */
	public Date endUseDate;

	/** date de péremption */
	public Date expirationDate;
	
	/** Label de travail */
	public String workLabel;

	/** types de réactif/boite */
	public String typeCode;
	
	/* type d'import (null si création manuelle) */
	public String importTypeCode;

	/** Historique */
	public TraceInformation traceInformation;

	/** Commentaries */
	public List<Comment> comments;

	/** Etat */
	public State state;

	/** Résolution */
	public String resolution;

	/* Catalogues pour les consultations. */
	@JsonInclude(NON_NULL)
	public AbstractCatalog catalog;
	@JsonInclude(NON_NULL)
	public KitCatalog catalogKit;

	public void addNewComment(String newComment, String user) {
		if (this.comments == null) {
			this.comments = Arrays.asList(new Comment(newComment, user, false));
		} else {
			this.comments.add(new Comment(newComment, user, false));
		}
	}

	@Override
	public void validate(ContextValidation contextValidation) {
		CommonValidationHelper.validateIdPrimary(contextValidation, this);
		CommonValidationHelper.validateCodePrimary(contextValidation, this,
				InstanceConstants.REAGENT_RECEPTION_COLL_NAME);
		CommonValidationHelper.validateTraceInformationRequired(contextValidation, this.traceInformation);
		
		String contextState = contextValidation.getTypedObject(ReceptionValidationHelper.FIELD_STATE_RECEPTION_CONTEXT);
		
		if(ReceptionValidationHelper.STATE_CONTEXT_IMPORT_FILE_ILLUMINA.equals(contextState)) {
			ReceptionValidationHelper.validateMendatoryProperty(contextValidation, this.receptionDate, "receptionDate");
			ReceptionValidationHelper.validateMendatoryProperty(contextValidation, this.providerCode, "providerCode");
			ReceptionValidationHelper.validateMendatoryProperty(contextValidation, this.kitCatalogName, "kitCatalogName");
			ReceptionValidationHelper.validateMendatoryProperty(contextValidation, this.catalogRefCode, "catalogRefCode");
			ReceptionValidationHelper.validateMendatoryProperty(contextValidation, this.batchNumber, "batchNumber");
		}

		ValidationHelper.validateNotEmpty(contextValidation, this.typeCode, "typeCode");

		ReceptionValidationHelper.validateEmptyCatalogOnUpdate(contextValidation, this.catalog, "catalog");
		ReceptionValidationHelper.validateEmptyCatalogOnUpdate(contextValidation, this.catalogKit, "catalogKit");
	}

}
