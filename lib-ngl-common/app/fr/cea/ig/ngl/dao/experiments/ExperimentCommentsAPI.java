package fr.cea.ig.ngl.dao.experiments;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import javax.inject.Inject;

import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;
import org.mongojack.DBUpdate;

import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APISemanticException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.api.GenericAPI;
import fr.cea.ig.ngl.dao.api.SubDocumentGenericAPI;
import models.laboratory.common.instance.Comment;
import models.laboratory.experiment.instance.Experiment;
import models.utils.CodeHelper;
import validation.ContextValidation;

public class ExperimentCommentsAPI extends SubDocumentGenericAPI<Comment, ExperimentsDAO, Experiment>{

	private static final String EDIT_ERROR_MESSAGE = "only user who creates the comment is allowed to modify it";
	private static final play.Logger.ALogger logger = play.Logger.of(ExperimentCommentsAPI.class);
	
	@Inject
	public ExperimentCommentsAPI(ExperimentsDAO dao) {
		super(dao);
	}

	@Override
	public Collection<Comment> getSubObjects(Experiment objectInDB) {
		return objectInDB.comments;
	}

	@Override
	public Comment getSubObject(Experiment objectInDB, String code) {
		for(Comment c : objectInDB.comments){
			if(code.equals(c.code)){
				return c;
			}
		}
		return null;
	}

	@Override
	public Iterable<Experiment> listObjects(String parentCode, Query query) {
		logger.debug("listObjects: query not used");
		return Arrays.asList(dao.findByCode(parentCode));
	}

	@Override
	public Comment save(Experiment objectInDB, Comment input, String currentUser) throws APISemanticException, APIValidationException {
		if (input.code == null) {
			input.createUser = currentUser;
			input.creationDate = new Date();
			input.code = CodeHelper.getInstance().generateExperimentCommentCode(input);									
		} else {
			throw new APISemanticException("use update method instead of save to update existing comments");
		}
//		ContextValidation ctxVal = new ContextValidation(currentUser); 
//		ctxVal.setCreationMode();
		ContextValidation ctxVal = ContextValidation.createCreationContext(currentUser); 
		ctxVal.putObject("experiment", objectInDB);		
		input.validate(ctxVal);
		if (!ctxVal.hasErrors()) {
			objectInDB.traceInformation.modificationStamp(ctxVal, currentUser);
			dao.updateObject(DBQuery.is("code", objectInDB.code), 
					DBUpdate.push("comments", input)
					.set("traceInformation", objectInDB.traceInformation));
			return input;
		} else {
			throw new APIValidationException(GenericAPI.INVALID_INPUT_ERROR_MSG, ctxVal.getErrors());
		}
	}

	@Override
	public Comment update(Experiment objectInDB, Comment input, String currentUser) throws APIException, APIValidationException {
//		ContextValidation ctxVal = new ContextValidation(currentUser);
//		ctxVal.setUpdateMode();
		if (currentUser.equals(input.createUser)) {
			ContextValidation ctxVal = ContextValidation.createUpdateContext(currentUser);
			ctxVal.putObject("experiment", objectInDB);
			input.validate(ctxVal);
			if (!ctxVal.hasErrors()) {
				objectInDB.traceInformation.modificationStamp(ctxVal, currentUser);
				dao.updateObject(DBQuery.is("code", objectInDB.code).is("comments.code", input.code), 
						DBUpdate.set("comments.$", input)
						.set("traceInformation", objectInDB.traceInformation));
				return input;
			} else {
				throw new APIValidationException(GenericAPI.INVALID_INPUT_ERROR_MSG, ctxVal.getErrors());
			}
		} else {
			throw new APIException(EDIT_ERROR_MESSAGE);
		}
	}

	@Override
	public void delete(Experiment objectInDB, String code, String currentUser) throws APIException {
		Comment deleteComment = this.getSubObject(objectInDB, code);	
		if (deleteComment == null) {
			throw new APIException("comment to delete not found: " + code);
		} else {
			if (currentUser.equals(deleteComment.createUser)) {
				objectInDB.traceInformation.modificationStamp(ContextValidation.createUndefinedContext(currentUser), currentUser);
				dao.updateObject(DBQuery.is("code", objectInDB.code), 
						DBUpdate.pull("comments", deleteComment)
						.set("traceInformation", objectInDB.traceInformation));
			} else {
				throw new APIException(EDIT_ERROR_MESSAGE);
			}	
		}
	}

}
