package models.laboratory.common.instance;

import java.util.Date;

import models.utils.CodeHelper;

import validation.ContextValidation;
import validation.IValidation;
import validation.utils.ValidationHelper;

/**
 * Comment are embedded data in collection like Container, Experiment.... 
 * 
 *
 */
public class Comment implements IValidation {

	public String code;
	public String comment;
	public String createUser;
	public Date   creationDate;
	
	public Comment() {
	}
	
	public Comment(String comment, String user) {
		this(comment, user, true);		
	}
	
	public Comment(String comment, String user, boolean withCode) {
		this.createUser   = user;
		this.comment      = comment;
		this.creationDate = new Date();
		if (withCode) 
			this.code = CodeHelper.getInstance().generateExperimentCommentCode(this);		
	}
	
	@Override
	public void validate(ContextValidation contextValidation) {
		ValidationHelper.validateNotEmpty(contextValidation, code,         "code"); // GA: check if not exist on the same object
		ValidationHelper.validateNotEmpty(contextValidation, comment,      "comment");
		ValidationHelper.validateNotEmpty(contextValidation, createUser,   "createUser");
		ValidationHelper.validateNotEmpty(contextValidation, creationDate, "creationDate");
	}
	
}
