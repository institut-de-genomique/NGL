package models.laboratory.reagent.instance;

import java.util.Date;
import java.util.List;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;

import models.laboratory.common.description.State;
import models.laboratory.common.instance.Comment;
import models.utils.InstanceConstants;
import ngl.refactoring.MiniDAO;
import validation.ContextValidation;
import validation.common.instance.CommonValidationHelper;
import validation.reagent.instance.BoxValidationHelper;
import validation.utils.ValidationHelper;


public class Box extends AbstractDeclaration {
	
	public static final Supplier<MiniDAO<Box>> find        = MiniDAO.createSupplier(InstanceConstants.REAGENT_INSTANCE_COLL_NAME, Box.class);
	public static final Supplier<MiniDAO<Box>> catalogFind = MiniDAO.createSupplier(InstanceConstants.REAGENT_CATALOG_COLL_NAME,  Box.class);
	
	public String catalogCode;
	
	public String kitCode;

	public String providerID;
	public String lotNumber;
	
	public Date startToUseDate;
	public Date stopToUseDate;
	
	public State state;
	
	public Date expirationDate;
	
	public String stockInformation;
	
	public List<Comment> comments;

	@Override
	public void validate(ContextValidation contextValidation) {
		ValidationHelper.validateNotEmpty(contextValidation, code,           "code");
		ValidationHelper.validateNotEmpty(contextValidation, catalogCode,    "catalogCode");
		ValidationHelper.validateNotEmpty(contextValidation, providerID,     "providerID");
		ValidationHelper.validateNotEmpty(contextValidation, receptionDate,  "receptionDate");
		ValidationHelper.validateNotEmpty(contextValidation, expirationDate, "expirationDate");
		ValidationHelper.validateNotEmpty(contextValidation, state,          "state");
		ValidationHelper.validateNotEmpty(contextValidation, orderCode,      "orderCode");
		
		if (!contextValidation.hasErrors()) {
			CommonValidationHelper.validateCodePrimary(contextValidation, this, InstanceConstants.REAGENT_INSTANCE_COLL_NAME);
			if (StringUtils.isNotEmpty(kitCode)) {
				if (declarationType.equals("kit")) {
					BoxValidationHelper.validateKitCodeOptional(contextValidation, kitCode);
				}
				BoxValidationHelper.validateBoxCatalogCodeOptional(contextValidation, catalogCode);
			}
		}
	}
	
}
