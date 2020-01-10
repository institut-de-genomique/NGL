package SraValidation;

import java.io.IOException;

import models.sra.submit.util.SraException;
import models.sra.submit.util.VariableSRA;

import org.junit.Assert;
import org.junit.Test;

import play.Logger;
import utils.AbstractTestsSRA;
import validation.ContextValidation;
import validation.sra.SraValidationHelper;

public class SraValidationHelperTest extends AbstractTestsSRA {
	@Test
	public void validationSraValidationHelperRequiredAndConstraintSuccess() throws IOException, SraException {
		ContextValidation contextValidation = new ContextValidation(userTest);
		String librarySelection = "random";
		SraValidationHelper.requiredAndConstraint(contextValidation, librarySelection, VariableSRA.mapLibrarySelection(), "librarySelection");
		System.out.println("\n - displayErrors pour validationSraValidationHelperRequiredAndConstraintSuccess :");
		contextValidation.displayErrors(Logger.of("SRA"));
		Assert.assertTrue(contextValidation.errors.size()==0); // si aucune erreur
	}
	
	@Test
	public void validationSraValidationHelperRequiredAndConstraintEchec() throws IOException, SraException {
		ContextValidation contextValidation = new ContextValidation(userTest);
		String librarySelection = "farfelue";
		SraValidationHelper.requiredAndConstraint(contextValidation, librarySelection, VariableSRA.mapLibrarySelection(), "librarySelection");
		System.out.println("\n - displayErrors pour validationSraValidationHelperRequiredAndConstraintEchec :");
		contextValidation.displayErrors(Logger.of("SRA"));
		Assert.assertTrue(contextValidation.errors.size()==1); // si une erreur
	}
	
	@Test
	public void validationSraValidationHelperRequiredAndConstraintNull() throws IOException, SraException {
		ContextValidation contextValidation = new ContextValidation(userTest);
		String librarySelection = null;
		SraValidationHelper.requiredAndConstraint(contextValidation, librarySelection, VariableSRA.mapLibrarySelection(), "librarySelection");
		System.out.println("\n - displayErrors pour validationSraValidationHelperRequiredAndConstraintNull :");
		contextValidation.displayErrors(Logger.of("SRA"));
		Assert.assertTrue(contextValidation.errors.size()==1); // si une erreur
	}
	
	@Test
	public void validationSraValidationHelperRequiredAndConstraintVoidString() throws IOException, SraException {
		ContextValidation contextValidation = new ContextValidation(userTest);
		String librarySelection = "";
		SraValidationHelper.requiredAndConstraint(contextValidation, librarySelection, VariableSRA.mapLibrarySelection(), "librarySelection");
		System.out.println("\n - displayErrors pour validationSraValidationHelperRequiredAndConstraintVoidString :");
		contextValidation.displayErrors(Logger.of("SRA"));		Assert.assertTrue(contextValidation.errors.size()==1); // si une erreur
	}	
}
