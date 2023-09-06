package models.laboratory.common.instance;

import static models.laboratory.common.instance.TBoolean.*;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.springframework.util.CollectionUtils;

import validation.ContextValidation;
import validation.IValidation;
import validation.common.instance.CommonValidationHelper;
import validation.utils.ValidationHelper;

public class Valuation implements IValidation {
	
	/**
	 * Validity.
	 */
	public TBoolean valid = UNSET;
	
	/**
	 * Creation date.
	 */
    public Date date;
    
    /**
     * Creation user. 
     */
    public String user;
    
    /**
     * Extra info.
     */
    public Set<String> resolutionCodes;
    
    /**
     * Name of the rules to validate containing instance.
     */
    public String criteriaCode;
    
    /**
     * Comment.
     */
    public String comment;
    
    /**
	 * History of previous and current valuations. 
	 */
    public Set<TransientValuation> history;
    
    public Valuation() {
    }
    
    // -------------------------------------------------------------------
    // Bunch of overloaded constructor to lighten caller syntax.
    
    /**
     * Construct a valuation with the given user, validity and the current date.
     * @param user  user
     * @param valid validity
     */
    public Valuation(String user, TBoolean valid) {
    	this(user, valid, new Date());
    }
    
    /**
     * Construct a valuation with the given user, validity and date.
     * @param user  user
     * @param valid validity
     * @param date  date
     */
    public Valuation(String user, TBoolean valid, Date date) {
    	this.user  = user;
    	this.valid = valid;
    	this.date  = date;
    }
    
    /**
     * Construct a valuation with the given user, validity ({@link TBoolean#valueOf(boolean)}) and the current date.
     * @param user  user
     * @param valid validity
     */
    public Valuation(String user, boolean valid) {
    	this(user, TBoolean.valueOf(valid));
    }
    
    /**
     * Construct a valuation with the given user, validity ({@link TBoolean#valueOf(boolean)}) and date.
     * @param user  user
     * @param valid validity
     * @param date  date
     */
    public Valuation(String user, boolean valid, Date date) {
    	this(user, TBoolean.valueOf(valid), date);
    }
    
    /**
     * Validate this valuation (context parameter {@link CommonValidationHelper#FIELD_TYPE_CODE}).
     */
	@Override
	@Deprecated
	public void validate(ContextValidation contextValidation) {
		ValidationHelper.validateNotEmpty             (contextValidation, valid, "valid");
		if (this.isnt(UNSET)) {
			ValidationHelper.validateNotEmpty         (contextValidation, date, "date");
			ValidationHelper.validateNotEmpty         (contextValidation, user, "user");
		}
		CommonValidationHelper.validateResolutionCodes(contextValidation, resolutionCodes);
		// GA: resolution si different de zero
		CommonValidationHelper.validateCriteriaCode   (contextValidation, criteriaCode); 
	}
	
	public void validate(ContextValidation contextValidation, String typeCode) {
		ValidationHelper.validateNotEmpty             (contextValidation, valid, "valid");
		if (this.isnt(UNSET)) {
			ValidationHelper.validateNotEmpty         (contextValidation, date, "date");
			ValidationHelper.validateNotEmpty         (contextValidation, user, "user");
		}
		CommonValidationHelper.validateResolutionCodes(contextValidation, typeCode, resolutionCodes);
		// GA: resolution si different de zero
		CommonValidationHelper.validateCriteriaCode   (contextValidation, typeCode, criteriaCode); 
	}
	
	/**
	 * Change user and date for this valuation.
	 * @param user
	 * @param date
	 * @return this
	 */
	private Valuation update(String user, Date date) {
		this.user = user;
		this.date = date;
		return this;
	}
	
	private boolean hasExistingHistory() {
		return !CollectionUtils.isEmpty(history);
	}
	
	private void copyHistoryFrom(Valuation valuation) {
		this.history = new HashSet<>(valuation.history);
	}
	
	private void initHistoryWith(Valuation valuation) {
		this.history = new HashSet<>();
		int firstHistoryIndex = 0;
		TransientValuation transientValuation = new TransientValuation(valuation, firstHistoryIndex);
		this.history.add(transientValuation);
	}
	
	private void addCurrentValuationToHistory() {
		int nextHistoryIndex = history.size();
		TransientValuation currentValuation = new TransientValuation(this, nextHistoryIndex);
		history.add(currentValuation);
	}
	
	/**
	 * copy history from previous valuation to this one, and add the current valuation.
	 * If previous valuation history is null, it will be initiated with :
	 * <li>current valuation</li>
	 * <li>next valuation</li>
	 * @param previousValuation
	 * @return
	 */
	private Valuation createHistoryFrom(Valuation previousValuation) {
		if(previousValuation.hasExistingHistory()) {
			this.copyHistoryFrom(previousValuation);
		} else {
			this.initHistoryWith(previousValuation);
		}
		this.addCurrentValuationToHistory();
		return this;
	}
	
	/**
	 * Copy <b>resolutionCodes</b>, <b>criteriaCode</b> and <b>comment</b> from the given valuation to this one.
	 * @param valuation
	 */
	private void copyAnnotationsFrom(Valuation valuation) {
		this.resolutionCodes = valuation.resolutionCodes;
		this.criteriaCode = valuation.criteriaCode;
		this.comment = valuation.comment;
	}
	
	/**
	 * Create a new Valuation from this one, with a copy of current history + the new valuation.</br>
	 * If current history is null, it will be initiated with :
	 * <li>current valuation</li>
	 * <li>next valuation</li>
	 * @param user
	 * @param valid
	 * @param date
	 * @return a new historized valuation with specified values
	 */
	private Valuation buildNextValuation(String user, TBoolean valid, Date date) {
		Valuation nextValuation = new Valuation(user, valid, date);
		nextValuation.copyAnnotationsFrom(this);
		return nextValuation.createHistoryFrom(this);
	}
	
	/**
	 * Create history for current valuation from a previous valuation.</br>
	 * If validity changed, return this Valuation with user and date actualized, 
	 * and the history copied and actualized from the previous valuation,
	 * else return this valuation with no modifications at all.</br>
	 * </br>
	 * Correct way to use:</br>
	 * {@code
	 * Valuation nextValuation = nextValuation.createHistoryFrom(previousValuation, <String>);
	 * }</br>
	 * </br>
	 * To create next valuation from the previous one, prefer to use :
	 * <li>previousValuation.{@link #withValues(String, TBoolean, Date)}</li>
	 * <li>previousValuation.{@link #withValuesFrom(Valuation)}</li>
	 * <li>previousValuation.{@link #asValidated(String, Date)}</li>
	 * <li>previousValuation.{@link #asInvalidated(String, Date)}</li>
	 * @param previousValuation the previous valuation in history
	 * @param user
	 * @return Valuation
	 */
	public Valuation createHistoryFrom(Valuation previousValuation, String user) {
		return this.isEquivalentOf(previousValuation) ? this : this.update(user, new Date()).createHistoryFrom(previousValuation);
	}
	
	/**
	 * Get a valuation from this one, with given values.</br>
	 * If validity changed, return a new Valuation with history copied and updated from this valuation,
	 * else return this valuation with modified values.</br>
	 * </br>
	 * Correct way to use:</br>
	 * {@code
	 * Valuation nextValuation = previousValuation.withValues(<String>, <TBoolean>, <Date>);
	 * }
	 * @param user
	 * @param valid
	 * @param date
	 * @return Valuation
	 */
	public Valuation withValues(String user, TBoolean valid, Date date) {
		return this.is(valid) ? this.update(user, date) : buildNextValuation(user, valid, date);
	}
	
	/**
	 * Get a valuation from this one, with values copied from supplied valuation.</br>
	 * If validity changed, return a new Valuation with history copied and updated from this valuation,
	 * else return this valuation with modified values.</br>
	 * </br>
	 * Correct way to use:</br>
	 * {@code
	 * Valuation nextValuation = previousValuation.withValuesFrom(otherValuation);
	 * }
	 * @param valuation
	 * @return Valuation
	 */
	public Valuation withValuesFrom(Valuation valuation) {
		return this.isEquivalentOf(valuation) ? this.update(valuation.user, valuation.date) : buildNextValuation(valuation.user, valuation.valid, valuation.date);
	}
	
	/**
	 * Get a valid valuation from this one, with given values.</br>
	 * If validity changed, return a new Valuation with history copied and updated from this valuation,
	 * else return this valuation with modified values.</br>
	 * </br>
	 * Correct way to use:</br>
	 * {@code
	 * Valuation validatedValuation = previousValuation.asValidated(<String>, <Date>);
	 * }
	 * @param user
	 * @param date
	 * @return Valuation
	 * @see #asValidated(String)
	 * @see #asInvalidated(String, Date)
	 */
	public Valuation asValidated(String user, Date date) {
		return this.is(TRUE) ? this.update(user, date) : buildNextValuation(user, TRUE, date);
	}
	
	/**
	 * Get a valid valuation from this one, with given values.</br>
	 * If validity changed, return a new Valuation with history copied and updated from this valuation,
	 * else return this valuation with modified values.</br>
	 * The date is updated with {@code new Date()}.</br>
	 * </br>
	 * Correct way to use:</br>
	 * {@code
	 * Valuation validatedValuation = previousValuation.asValidated(<String>);
	 * }
	 * @param user
	 * @return valuation
	 */
	public Valuation asValidated(String user) {
		return asValidated(user, new Date());
	}
	
	/**
	 * Get an invalid valuation from this one, with given values.</br>
	 * If validity changed, return a new Valuation with history copied and updated from this valuation,
	 * else return this valuation with modified values.</br>
	 * </br>
	 * Correct way to use:</br>
	 * {@code
	 * Valuation invalidatedValuation = previousValuation.asInvalidated(<String>, <Date>);
	 * }
	 * @param user
	 * @param date
	 * @return Valuation
	 * @see #asInvalidated(String)
	 * @see #asValidated(String, Date)
	 */
	public Valuation asInvalidated(String user, Date date) {
		return this.is(FALSE) ? this.update(user, date) : buildNextValuation(user, FALSE, date);
	}
	
	/**
	 * Get an invalid valuation from this one, with given values.</br>
	 * If validity changed, return a new Valuation with history copied and updated from this valuation,
	 * else return this valuation with modified values.</br>
	 * The date is updated with {@code new Date()}.</br>
	 * </br>
	 * Correct way to use:</br>
	 * {@code
	 * Valuation invalidatedValuation = previousValuation.asInvalidated(<String>);
	 * }
	 * @param user
	 * @return Valuation
	 */
	public Valuation asInvalidated(String user) {
		return asInvalidated(user, new Date());
	}
	
	/**
	 * null-safe function to add a resolutionCode to this valuation.
	 * @param resolutionCode
	 */
	public void addResolution(String resolutionCode) {
		if(this.resolutionCodes == null)
			this.resolutionCodes = new HashSet<>(1);
		this.resolutionCodes.add(resolutionCode);
	}
	
	/**
	 * shortcut to test <b>"{@code valuationA.valid.equals(valid)}"</b>.
	 * @param valid
	 * @return true if same validity
	 */
	public boolean is(TBoolean valid) {
		return this.valid == valid;
	}
	
	/**
	 * shortcut to test <b>"{@code !valuationA.valid.equals(valid)}"</b>.
	 * @param valid
	 * @return true if different validity
	 */
	public boolean isnt(TBoolean valid) {
		return !this.is(valid);
	}
	
	/**
	 * shortcut to test <b>"{@code valuationA.valid.equals(valuationB.valid)}"</b>.
	 * @param valuation
	 * @return true if the given valuation has same validity
	 */
	public boolean isEquivalentOf(Valuation valuation) {
		return this.is(valuation.valid);
	}
	
	/**
	 * shortcut to test <b>"{@code !valuationA.valid.equals(valuationB.valid)}"</b>.
	 * @param valuation
	 * @return true if the given valuation hasn't same validity
	 */
	public boolean isntEquivalentOf(Valuation valuation) {
		return !this.isEquivalentOf(valuation);
	}

}