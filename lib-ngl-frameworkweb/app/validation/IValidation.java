package validation;

// RENAME: rename to IValidatable ('that can be validated')

// This is a legitimate interface for objects that can be validated
// by themselves otherwise method parameters should be added and thus
// this interface should not be used (e.g: a run that contains
// a file should be provided as an argument to the file validation
// method).

// The validation methods should be split using the CRUD naming scheme
//   validateInvariants
//   validateUpdate
//   validateCreation
//   validateDeletion
// This scheme exists in ICRUDValidation ICRUDValidatable that are kept sources (java.off).

public interface IValidation {
	
	public void validate(ContextValidation contextValidation);

}
