package workflows.sra.submission;

import javax.inject.Singleton;

// import org.apache.commons.lang3.StringUtils;

import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;
// import org.springframework.stereotype.Service;

import fr.cea.ig.MongoDBDAO;
// import fr.cea.ig.play.migration.NGLContext;
import models.laboratory.common.description.ObjectType;
import models.laboratory.common.instance.State;
// import models.laboratory.container.instance.ContainerSupport;
// import models.laboratory.run.instance.ReadSet;
// import models.sra.submit.common.instance.Sample;
// import models.sra.submit.common.instance.Study;
// import models.sra.submit.common.instance.Submission;
import models.sra.submit.sra.instance.Configuration;
// import models.sra.submit.sra.instance.Experiment;
// import models.sra.submit.util.VariableSRA;
import models.utils.InstanceConstants;
import validation.ContextValidation;
import validation.common.instance.CommonValidationHelper;
// import validation.sra.SraValidationHelper;
import workflows.Workflows;

// @Service
@Singleton
public class ConfigurationWorkflows extends Workflows<Configuration> {

//	public ConfigurationWorkflows(NGLContext ctx) {
//		super(ctx);
//	}


//	private static final play.Logger.ALogger logger = play.Logger.of(ConfigurationWorkflows.class);

	//public static ConfigurationWorkflows instance= new ConfigurationWorkflows();


	@Override
	public void applyPreStateRules(ContextValidation validation, Configuration object, State nextState) {
	}

	@Override
	public void applyPreValidateCurrentStateRules(ContextValidation validation, Configuration object) {
	}
	
	@Override
	public void applyPostValidateCurrentStateRules(ContextValidation validation, Configuration object) {
	}

	@Override
	public void applySuccessPostStateRules(ContextValidation validation, Configuration object) {	
	}

	@Override
	public void applyErrorPostStateRules(ContextValidation validation, Configuration object, State nextState) {
	}

	@Override
	public void setState(ContextValidation contextValidation, Configuration object, State nextState) {
		contextValidation.setUpdateMode();

		// Gerer historique de la modification
		object.traceInformation = updateTraceInformation(object.traceInformation, nextState); 			

		// verifier que le state à installer est valide avant de mettre à jour base de données : 
		// verification qui ne passe pas par VariableSRA [SraValidationHelper.requiredAndConstraint(contextValidation, nextState.code , VariableSRA.mapStatus, "state.code")]		
        // mais par CommonValidationHelper.validateState(ObjectType.CODE.SRASubmission, nextState, contextValidation); 
		// pour uniformiser avec reste du code ngl
		
		CommonValidationHelper.validateState(ObjectType.CODE.SRAConfiguration, nextState, contextValidation); 		
		if (!contextValidation.hasErrors()) {
			// Gerer l'historique des states :
			object.state = updateHistoricalNextState(object.state, nextState);	
			// sauver le state dans la base avec traceInformation
			MongoDBDAO.update(InstanceConstants.SRA_CONFIGURATION_COLL_NAME,  Configuration.class, 
				DBQuery.is("code", object.code),
				DBUpdate.set("state", object.state).set("traceInformation", object.traceInformation));
			
		}		
	}

	@Override
	public void nextState(ContextValidation contextValidation, Configuration object) {
	}
	
}
