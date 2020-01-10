package validation;

// Default converter from IValidation to ICRUDValidatable
public interface ICRUDValidation<T> extends IValidation,ICRUDValidatable<T> {
	
	@Override
	default void validateInvariants(ContextValidation ctx) {
	}
	
	@Override
	default void validateCreation(ContextValidation ctx) {
		ctx.setCreationMode();
		validate(ctx);
	}

	@Override
	default void validateUpdate(ContextValidation ctx, T past) {
		ctx.setUpdateMode();
		validate(ctx);		
	}
	
	@Override
	default void validateDelete(ContextValidation ctx) {
		ctx.setDeleteMode();
		validate(ctx);				
	}

}
