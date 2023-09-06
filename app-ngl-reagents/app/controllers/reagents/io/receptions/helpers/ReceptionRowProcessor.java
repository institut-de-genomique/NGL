package controllers.reagents.io.receptions.helpers;

import static controllers.reagents.io.receptions.helpers.ReceptionsFileHelpers.*;

import java.util.List;

import org.apache.poi.ss.usermodel.Row;

import controllers.reagents.io.receptions.mappers.AbstractReceptionMapper;
import controllers.reagents.io.receptions.mappers.row.RequiredPropertiesRowMapper.RequiredProperties;
import fr.cea.ig.ngl.dao.api.APIException;
import models.laboratory.reagent.instance.AbstractReception;
import validation.ContextValidation;
import validation.reagent.instance.ReceptionValidationHelper;

public class ReceptionRowProcessor {
	
	private final ReceptionBuilder receptionBuilder;
	
	private final ReceptionFiller receptionFiller;
	
	private final AbstractReceptionMapper receptionMapper;
	
	private final ContextValidation contextValidation;
	
	private final String currentUser;
	
	public ReceptionRowProcessor(ReceptionBuilder receptionBuilder, ReceptionFiller receptionFiller, AbstractReceptionMapper receptionMapper, ContextValidation contextValidation, String currentUser) {
		this.receptionBuilder = receptionBuilder;
		this.receptionFiller = receptionFiller;
		this.receptionMapper = receptionMapper;
		this.contextValidation = contextValidation;
		this.currentUser = currentUser;
	}
	
	private void findMissingRequiredProperties(RequiredProperties requiredProperties) {
		ReceptionValidationHelper.validateMendatoryProperty(contextValidation, requiredProperties.getProviderCode(), "providerCode");
		ReceptionValidationHelper.validateMendatoryProperty(contextValidation, requiredProperties.getKitCatalogName(), "kitCatalogName");
		ReceptionValidationHelper.validateMendatoryProperty(contextValidation, requiredProperties.getCatalogRefCode(), "catalogRefCode");
	}
	
	private boolean areValids(Row row, RequiredProperties requiredProperties) throws APIException {
		if(isValid(requiredProperties)) {
			return true;
		}
		if(!isEmpty(row)) {
			findMissingRequiredProperties(requiredProperties);
		} return false;
	}
	
	private AbstractReception performRowMapping(Row row, RequiredProperties requiredProperties) {
		AbstractReception reception = receptionBuilder.createEmptyReception(requiredProperties);
		receptionMapper.setCorrectTypes(reception);
		receptionMapper.mapRow(row, reception, currentUser);
		receptionFiller.fillReceptionInfos(reception, currentUser);
		return reception;
	}
	
	private AbstractReception handleRowMapping(Row row, RequiredProperties requiredProperties) throws APIException {
		try {
			return performRowMapping(row, requiredProperties);
		} catch(Exception e) {
			throw new APIException("Reagent-reception, unexpected mapping error: " + e.getMessage(), e);
		}
	}
	
	private void processRow(Row row, List<AbstractReception> receptions) throws APIException {
		final RequiredProperties requiredProperties = receptionMapper.getRequiredProperties(row);
		if(areValids(row, requiredProperties)) {
			final AbstractReception reception = handleRowMapping(row, requiredProperties);
			reception.validate(contextValidation);
			receptions.add(reception);
		}
	}
	
	public void handleRowProcessing(Row row, List<AbstractReception> receptions) throws APIException {
		final int rowIndex = getIndex(row);
		contextValidation.addKeyToRootKeyName("Ligne " + rowIndex);
		processRow(row, receptions);
		contextValidation.removeKeyFromRootKeyName("Ligne " + rowIndex);
	}

}
