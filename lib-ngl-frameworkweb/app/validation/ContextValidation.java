package validation;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;

import play.Logger.ALogger;
import play.data.Form;
import play.data.validation.ValidationError;
import validation.ChildValidationContext.KeyingChildValidationContext;
import validation.ChildValidationContext.MappingChildValidationContext;
import validation.ChildValidationContext.ModingChildValidationContext;


// RENAME: change name to ValidationContext (proper English construction)
// The context mode should probably be required at the constructor level to avoid
// the NOT_DEFINED mode. The mode should probably not be modified for a given context
// so it should be final.

/**
 * Validation context interface.
 * <p>
 * Creation is done through factory methods:
 * <ul>
 *   <li>{@link #createCreationContext(String)}</li>
 *   <li>{@link #createCreationContext(String, Form)}</li>
 *   <li>{@link #createDeleteContext(String)}</li>
 *   <li>{@link #createDeleteContext(String, Form)}</li>
 *   <li>{@link #createUndefinedContext(String)}</li>
 *   <li>{@link #createUndefinedContext(String, Form)}</li>
 *   <li>{@link #createUpdateContext(String)}</li>
 *   <li>{@link #createUpdateContext(String, Form)}</li>
 * </ul>
 * <p>
 * Context objects (map of strings and objects) are used to passed somewhat hidden
 * parameters (which should be avoided).
 * <p>
 * Error key path is built incrementally using {@link #appendPath(String)} and does not
 * affect the original context but return a new context.
 * 
 * @author vrd
 *
 */
public interface ContextValidation {
	
	public enum Mode {
		CREATION, 
		UPDATE, 
		DELETE, 
		NOT_DEFINED
	}

	/**
	 * User running the validation.
	 * @return user running the validation. 
	 */
	String getUser();

	// ------------------------------------------------------------------------
	// Context objects support (to be removed)
	
	/**
	 * Get object for a key in context.
	 * @param key key
	 * @return    value
	 */
//	@Deprecated
	Object getObject(String key);

	/**
	 * Put an object for a key in the context.
	 * @param key   key
	 * @param value value
	 */
	void putObject(String key, Object value);

	/**
	 * Remove key (and associated object) from context.
	 * @param key key to remove
	 */
	void removeObject(String key);

	/**
	 * Is the key defined in the context ?
	 * @param key key to test existence of
	 * @return    true if the key is defined in the context, false otherwise
	 */
	boolean containsKey(String key);

	/**
	 * Get a typed object from this context, this does nothing else than
	 * moving the cast problem in a single place.
	 * @param key object name
	 * @param <T> expected object type
	 * @return    typed object
	 */
//	@Deprecated
	<T> T getTypedObject(String key);

	/**
	 * Map of context objects.
	 * @return map of context objects
	 */
	Map<String, Object> getContextObjects();

	/**
	 * Set the context objects.
	 * @param contextObjects new context objects
	 */
	void setContextObjects(Map<String, Object> contextObjects);

	/**
	 * Add an error message (ill named, no plural).
	 * @param property  property key
	 * @param message   message
	 * @param arguments message parameters
	 * @deprecated use addError(String,String,Object...)
	 */
	@Deprecated
	default void addErrors(String property, String message, Object... arguments) { addError(property, message, arguments); }
	
	/**
	 * Add an error message to this context.
	 * @param property  property key
	 * @param message   message
	 * @param arguments message parameters
	 */
	void addError(String property, String message, Object... arguments);

	void addErrors(Map<String, List<ValidationError>> errors);

	Map<String, List<ValidationError>> getErrors();

	boolean hasErrors();

	String getRootKeyName();

//	@Deprecated
	void setRootKeyName(String rootKeyName);
//	@Deprecated
	void addKeyToRootKeyName(String key);
//	@Deprecated
	void removeKeyFromRootKeyName(String key);

	void setCreationMode();

	void setUpdateMode();

//	void setDeleteMode();

	void setMode(Mode mode);

	boolean isUpdateMode();

	boolean isCreationMode();

	boolean isDeleteMode();

	boolean isNotDefined();

	Mode getMode();

	void displayErrors(ALogger logger);
	
	void displayErrors(ALogger logger, String loggerLevel);
	
	int errorCount();
	
	// ---------------------
	// -- Utilities
	
//	/**
//	 * Build a new context with a key appended to the current key.
//	 * @param key key to append
//	 * @return    context with new root key
//	 */
//	default ValidationContext appendKey(String key) {
//		return new KeyingChildValidationContext(this,ChildValidationContext.composeKey(getRootKeyName(), key));
//	}
//	/**
//	 * Build a new context using a given key as root key.
//	 * @param key root key to use
//	 * @return    new context with given root key
//	 */
//	default ValidationContext withKey(String key) {
//		return new KeyingChildValidationContext(this, key);
//	}
//	default ValidationContext withFreshRootKey() {
//		return withKey("");
//	}
//	default ValidationContext withObject(String key, Object value) {
//		return new MappingChildValidationContext(this, key, value);
//	}
//	default ValidationContext withMode(Mode mode) {
//		return new ModingChildValidationContext(this,mode);
//	}
//	default ValidationContext withCreationMode() { return withMode(Mode.CREATION); }
//	default ValidationContext withUpdateMode() { return withMode(Mode.UPDATE); }
//	default void execute(Consumer<ValidationContext> c) {
//		c.accept(this);
//	}
		
	/**
	 * Build a new context with a key appended to the current key.
	 * @param key key to append
	 * @return    context with new path
	 */
	default ContextValidation appendPath(String key) {
		return new KeyingChildValidationContext(this, ChildValidationContext.composeKey(getRootKeyName(), key));
	}
	
	/**
	 * Build a new context using a given key as root key.
	 * @param key root key to use
	 * @return    new context with given root key
	 */
	default ContextValidation withKey(String key) {
		return new KeyingChildValidationContext(this, key);
	}
	
	/**
	 * Build a new context using an empty key as root key.
	 * @return    new context with given root key
	 */	
	default ContextValidation withFreshRootKey() {
		return withKey("");
	}
	
	/**
	 * Build a new context with the given named value.  
	 * @param key   name
	 * @param value value
	 * @return      new context with the given mapping
	 */
	default ContextValidation withObject(String key, Object value) {
		return new MappingChildValidationContext(this, key, value);
	}
	
	/**
	 * Build a new context with the given mode.
	 * @param mode mode
	 * @return     context with the given mode
	 */
	default ContextValidation withMode(Mode mode) {
		return new ModingChildValidationContext(this,mode);
	}
	
	default ContextValidation withCreationMode() { return withMode(Mode.CREATION); }
	default ContextValidation withUpdateMode() { return withMode(Mode.UPDATE); }
	
	/**
	 * Executes a consumer by providing this context.
	 * @param c consumer
	 */
	default void execute(Consumer<ContextValidation> c) {
		c.accept(this);
	}
	
	// Constructors replacements
	
	/**
	 * Create a creation mode validation context.
	 * @param user user 
	 * @return     validation context in creation mode
	 */
	public static ContextValidation createCreationContext(String user) {
		return new ValidationContext(Mode.CREATION, user, new TreeMap<>(), new TreeMap<>());
	}
	
	/**
	 * Create a creation mode validation context.
	 * @param user user
	 * @param form error map
	 * @return     validation context in creation mode
	 */
	@SuppressWarnings("deprecation")
	public static ContextValidation createCreationContext(String user, Form<?> form) {
		return new ValidationContext(Mode.CREATION, user, new TreeMap<>(form.errors()), new TreeMap<>());
	}
	
	public static ContextValidation createDeleteContext(String user) {
		return new ValidationContext(Mode.DELETE, user, new TreeMap<>(), new TreeMap<>());
	}
	@SuppressWarnings("deprecation")
	public static ContextValidation createDeleteContext(String user, Form<?> form) {
		return new ValidationContext(Mode.DELETE, user, new TreeMap<>(form.errors()), new TreeMap<>());
	}
	public static ContextValidation createUndefinedContext(String user) {
		return new ValidationContext(Mode.NOT_DEFINED, user, new TreeMap<>(), new TreeMap<>());
	}
	@SuppressWarnings("deprecation")
	public static ContextValidation createUndefinedContext(String user, Form<?> form) {
		return new ValidationContext(Mode.NOT_DEFINED, user, new TreeMap<>(form.errors()), new TreeMap<>());
	}
	public static ContextValidation createUpdateContext(String user) {
		return new ValidationContext(Mode.UPDATE, user, new TreeMap<>(), new TreeMap<>());
	}
	@SuppressWarnings("deprecation")
	public static ContextValidation createUpdateContext(String user, Form<?> form) {
		return new ValidationContext(Mode.UPDATE, user, new TreeMap<>(form.errors()), new TreeMap<>());
	}

	/**
	 * The context mode is not the expected one.
	 * 
	 * @author vrd
	 *
	 */
	public static class IllegalContextModeException extends RuntimeException {
		private static final long serialVersionUID = 1L;
		public final Mode expected, actual;
		public IllegalContextModeException(Mode expected, Mode actual) {
			super("expected mode " + expected + ", actual was " + actual);
			this.expected = expected;
			this.actual   = actual;
		}
	}

	/**
	 * Assert that a validation context is in the specified mode, otherwise
	 * throw an {@link IllegalContextModeException}. 
	 * @param cv   validation context
	 * @param mode expected mode
	 */
	public static void assertMode(ContextValidation cv, Mode mode) {
		if (!cv.getMode().equals(mode))
			throw new IllegalContextModeException(mode, cv.getMode());
	}
	
	void addErrorKey(String key, String property, String message, Object... arguments);
	
}

// ---------------------------------------------------------------
// -- Draft implementation to reformat.

/**
 * Validation context that delegates all the calls to a parent context.
 * Subclasses that override part of the validation context API can do so
 * by deriving this class instead of implementing the validation context 
 * interface.  
 *  
 * @author vrd
 *
 */
abstract class ChildValidationContext implements ContextValidation {
	
	/**
	 * Parent validation context.
	 */
	protected final ContextValidation parent;
	
	/**
	 * Construct a delegating context for the given parent.
	 * @param parent parent to delegate calls to
	 */
	protected ChildValidationContext(ContextValidation parent) {
		this.parent = parent;
	}
	
	
	@Override public String getUser()               { return parent.getUser(); }
	@Override public Object getObject(String key)   { return parent.getObject(key); }
	@Override public void putObject(String key, Object value) { parent.putObject(key, value); }
	@Override public void removeObject(String key) { parent.removeObject(key); }
	@Override public boolean containsKey(String key) { return parent.containsKey(key); }
	@Override public <T> T getTypedObject(String key) { return parent.getTypedObject(key); }
	@Override public Map<String, Object> getContextObjects() { return parent.getContextObjects(); }
	@Override public void setContextObjects(Map<String, Object> contextObjects) { parent.setContextObjects(contextObjects);	}
	@Override public void addError(String property, String message, Object... arguments) { parent.addError(property, message, arguments); }
	@Override public void addErrors(Map<String, List<ValidationError>> errors) { parent.addErrors(errors); }
	@Override public Map<String, List<ValidationError>> getErrors() { return parent.getErrors(); }
	@Override public boolean hasErrors() { return parent.hasErrors(); }
	@Override public int errorCount() { return parent.errorCount(); }
	@Override public String getRootKeyName() { return parent.getRootKeyName(); }
	@Override public void setRootKeyName(String rootKeyName) { parent.setRootKeyName(rootKeyName); }
	@Override public void addKeyToRootKeyName(String key) { parent.addKeyToRootKeyName(key); }
	@Override public void removeKeyFromRootKeyName(String key) { parent.removeKeyFromRootKeyName(key); }
	@Override public void setCreationMode() { parent.setCreationMode(); }
	@Override public void setUpdateMode() { parent.setUpdateMode(); }
	@Override public void setMode(Mode mode) { parent.setMode(mode); }
	@Override public boolean isUpdateMode() { return parent.isUpdateMode(); }
	@Override public boolean isCreationMode() { return parent.isCreationMode(); }
	@Override public boolean isDeleteMode() { return parent.isDeleteMode(); }
	@Override public boolean isNotDefined() { return parent.isNotDefined(); }
	@Override public Mode getMode() { return parent.getMode(); }
	@Override public void displayErrors(ALogger logger) { parent.displayErrors(logger); }
	@Override public void displayErrors(ALogger logger, String loggerLevel) { parent.displayErrors(logger, loggerLevel); }

	@Override public void addErrorKey(String key, String property, String message, Object... arguments) { parent.addErrorKey(key, property, message, arguments); }
	public static String composeKey(String root, String key) { return StringUtils.isBlank(root) ? key : root + "." + key; }

	/**
	 * Override the key value from parent context.
	 * 
	 * @author vrd
	 *
	 */
	public static class KeyingChildValidationContext extends ChildValidationContext {
		
		/**
		 * Key value.
		 */
		private final String key;
		
		/**
		 * Construct a context that overrides the key access.
		 * @param parent parent to hide key of
		 * @param key    key value
		 */
		public KeyingChildValidationContext(ContextValidation parent, String key) { 
			super(parent);
			this.key = key;
		}
		
		@Override public String getRootKeyName()                   { return key; }
		// Key override is not compatible with the 
		@Override public void setRootKeyName(String rootKeyName)   { throw new UnsupportedOperationException(); }
		@Override public void addKeyToRootKeyName(String key)      { throw new UnsupportedOperationException(); }
		@Override public void removeKeyFromRootKeyName(String key) { throw new UnsupportedOperationException(); }
		
		@Override public void addError(String property, String message, Object... arguments) { 
			parent.addErrorKey(key, property, message, arguments); 
		}
	}
	
	/**
	 * Child context that should not be used, provided to allow a kind of 
	 * backward compatibility.
	 *  
	 * @author vrd
	 *
	 */
	static class MappingChildValidationContext extends ChildValidationContext {
		
		/**
		 * Key to override.
		 */
		private final String key;
		
		/**
		 * Value.
		 */
		private final Object value;
		
		public MappingChildValidationContext(ContextValidation parent, String key, Object value) { 
			super(parent);
			this.key = key;
			this.value = value;
		}
//		@Override public Object getObject(String key)   {
//			if (this.key.equals(key)) return value;
//			return parent.getObject(key);
//		}
		@Override public Object getObject(String key)             { return this.key.equals(key) ? value : parent.getObject(key); }
		@Override public void putObject(String key, Object value) { throw new UnsupportedOperationException("use withObject method"); }
		@Override public void removeObject(String key)            { throw new UnsupportedOperationException("use withObject method"); }
		@Override public boolean containsKey(String key)          { return this.key.equals(key) || parent.containsKey(key); }
//		@SuppressWarnings("unchecked")
//		@Override public <T> T getTypedObject(String key) { 
//			if (this.key.equals(key)) return (T)value;
//			return parent.getTypedObject(key); 
//		}
		@SuppressWarnings("unchecked")
		@Override public <T> T getTypedObject(String key)         { return this.key.equals(key) ? (T)value : parent.getTypedObject(key); }
		@Override public Map<String, Object> getContextObjects()  { throw new UnsupportedOperationException(); }
		@Override public void setContextObjects(Map<String, Object> contextObjects) { throw new UnsupportedOperationException();}
	}
	
	/**
	 * Mode overriding child context.
	 * 
	 * @author vrd
	 *
	 */
	public static class ModingChildValidationContext extends ChildValidationContext {
		
		/**
		 * Mode.
		 */
		private final Mode mode;
		
		/**
		 * Construct a new context using the provided mode.
		 * @param parent parent context
		 * @param mode   mode
		 */
		public ModingChildValidationContext(ContextValidation parent, Mode mode) { 
			super(parent);
			this.mode = mode;
		}
		
		@Override public void setCreationMode()   { throw new UnsupportedOperationException("use withMode method"); }
		@Override public void setUpdateMode()     { throw new UnsupportedOperationException("use withMode method"); }
		@Override public void setMode(Mode mode)  { throw new UnsupportedOperationException("use withMode method"); }
		
		@Override public boolean isUpdateMode()   { return Mode.UPDATE     .equals(mode); }
		@Override public boolean isCreationMode() { return Mode.CREATION   .equals(mode); }
		@Override public boolean isDeleteMode()   { return Mode.DELETE     .equals(mode); }
		@Override public boolean isNotDefined()   { return Mode.NOT_DEFINED.equals(mode); }
		@Override public Mode getMode()           { return mode; }
		
	}
	
	static void example(ModingChildValidationContext ctx) {
		ctx.withMode(Mode.UPDATE)
		   .withObject("key", 1)
		   .appendPath("someKey");
	}
	
}

