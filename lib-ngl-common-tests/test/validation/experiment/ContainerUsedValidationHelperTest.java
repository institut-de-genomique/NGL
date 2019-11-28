package validation.experiment;

import java.util.function.Consumer;

import org.junit.Assert;
import org.junit.Test;

import validation.ContextValidation;
import validation.experiment.instance.ContainerUsedValidationHelper;

public class ContainerUsedValidationHelperTest {

	private static void testContextValidationConsumer(Consumer<ContextValidation> consumer, Consumer<Boolean> assertion) {
		ContextValidation ctx = ContextValidation.createUndefinedContext(null);
		consumer.accept(ctx);
		assertion.accept(ctx.hasErrors());
	}

	private static void testPercentage(double value, Consumer<Boolean> assertion) {
		testContextValidationConsumer(ctx -> ContainerUsedValidationHelper.validatePercentage(ctx, value), assertion);
	}
	
	@Test
	public void testPercentage_n10() {
		testPercentage(-10d, Assert::assertTrue);
	}
	
	@Test
	public void testPercentage_110() {
		testPercentage(110d, Assert::assertTrue);
	}
	
	@Test
	public void testPercentage_1() {
		testPercentage(1d, Assert::assertFalse);
	}
	
	@Test
	public void testPercentage_99() {
		testPercentage(99d, Assert::assertFalse);
	}
	
}
