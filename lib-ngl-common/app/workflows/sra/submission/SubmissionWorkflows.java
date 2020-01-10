package workflows.sra.submission;

import static ngl.refactoring.state.SRASubmissionStateNames.*;

//import java.util.HashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import fr.cea.ig.lfw.utils.DoubleKeyMap;
import models.laboratory.common.description.ObjectType;
import models.laboratory.common.instance.State;
import models.sra.submit.common.instance.Submission;
import models.sra.submit.util.SraException;
import models.utils.InstanceConstants;
import validation.ContextValidation;
//import workflows.sra.submission.transition.N_to_V_SUB;

//@Service
@Singleton
public class SubmissionWorkflows extends StateMachine<Submission> {
	
//	public static final String 
//		N        = "N",
//		N_R      = "N-R",
//		V_SUB    = "V-SUB",
//		IW_SUB	 = "IW-SUB",
//		IP_SUB   = "IP-SUB",
//		FE_SUB   = "FE-SUB",
//		F_SUB    = "F-SUB",
//		IW_SUB_R = "IW-SUB-R",
//		IP_SUB_R = "IP-SUB-R",
//		FE_SUB_R = "FE_SUB-R";
	
	private SubmissionWorkflowsHelper submissionWorkflowsHelper;

	private DoubleKeyMap<String, String, Transition<Submission>> trs;
	
//	public class BasicTransition implements SubmissionTransition {
	public class BasicTransition implements Transition<Submission> {
		@Override
		public void execute(ContextValidation contextValidation, Submission submission, State nextState) {}
		@Override
		public void success(ContextValidation contextValidation, Submission submission, State nextState) {}
		@Override
		public void error(ContextValidation contextValidation, Submission object, State nextState) {}		
	}

	@Inject
	public SubmissionWorkflows(SubmissionWorkflowsHelper submissionWorkflowsHelper) {
		this.submissionWorkflowsHelper = submissionWorkflowsHelper;
		initSubmissionWorkflows();
	}

	class N_V_SUB_Transition extends BasicTransition {
		@Override public void success(ContextValidation contextValidation, Submission submission, State nextState) {
			submissionWorkflowsHelper.updateSubmissionChildObject(submission, contextValidation);
		}	
	}

	void initSubmissionWorkflows() {
		trs = new DoubleKeyMap<>();
		BasicTransition NOTHING = new BasicTransition();
		
		
//		BasicTransition NOTHING_ = new BasicTransition(){
//			{} // constructeur anonyme de la classe anonyme
//		};
//		{} // petit bloc (espace de nommage)
		//--------------------------------------------------
		// workflow d'une premiere soumission des données :
		//--------------------------------------------------

		// Autoriser les transition de A vers A qui n'ont aucune action:
		// --------------------------------------------------------------
		// => permet de faire un setState et si prochain etat = etat courant 
		// alors aucune action mais pas de declenchement d'erreur.
		// trs.put(V_SUB , V_SUB  , new BasicTransition() {});	
		trs.put(V_SUB  , V_SUB   , NOTHING);// Autoriser transistion de V_SUB vers V_SUB qui ne fait rien.
		trs.put(IW_SUB , IW_SUB  , NOTHING);	
		trs.put(IP_SUB , IP_SUB  , NOTHING);	
		trs.put(F_SUB  , F_SUB   , NOTHING);	
		trs.put(FE_SUB , FE_SUB  , NOTHING);	
		
		
		// Autoriser les transitions metiers :
		//------------------------------------
		
		// implementation d'une sous-classe de BasicTransition anonyme et interne 
		// classe interne qui existera au sein de l'instance environnementale SubmissionWorkflows
		trs.put(N     , V_SUB   , new BasicTransition() {		
			@Override public void success(ContextValidation contextValidation, Submission submission, State nextState) {
				submissionWorkflowsHelper.updateSubmissionChildObject(submission, contextValidation);
			}
		});
		// On aurait pu definir une classe N_V_SUB_Transition et definir la transition ainsi :
        //      trs.put(N     , V_SUB   , new N_V_SUB_Transition()); 		
				
		trs.put(V_SUB , IW_SUB  , new BasicTransition() {
			@Override public void execute(ContextValidation contextValidation, Submission submission, State nextState) {
				submissionWorkflowsHelper.activationPrimarySubmission(contextValidation, submission);
			}			
			@Override public void success(ContextValidation contextValidation, Submission submission, State nextState) {
				submissionWorkflowsHelper.updateSubmissionChildObject(submission, contextValidation);
			}
		});
		trs.put(IW_SUB, IP_SUB  , new BasicTransition() {
			@Override public void success(ContextValidation contextValidation, Submission submission, State nextState) {
				submissionWorkflowsHelper.updateSubmissionChildObject(submission, contextValidation);
			}
		});			
		trs.put(IP_SUB, F_SUB   , new BasicTransition() {	
			@Override public void success(ContextValidation contextValidation, Submission submission, State nextState) {
				submissionWorkflowsHelper.updateSubmissionChildObject(submission, contextValidation);
				submissionWorkflowsHelper.updateSubmissionForDates(submission);

			}
		});	
		// Autoriser les transitions reprises sur erreur :
		// -----------------------------------------------
		trs.put(IP_SUB, FE_SUB  , new BasicTransition() {
			@Override public void success(ContextValidation contextValidation, Submission submission, State nextState) {
				submissionWorkflowsHelper.updateSubmissionChildObject(submission, contextValidation);
			}
		});
		trs.put(FE_SUB, IW_SUB  , new BasicTransition() {
			@Override public void success(ContextValidation contextValidation, Submission submission, State nextState) {
				submissionWorkflowsHelper.updateSubmissionChildObject(submission, contextValidation);
			}			
		});
		
		
		//-------------------------------------------------------
		// workflow d'une soumission  pour release des données :
		//-------------------------------------------------------
		// Autoriser les transitions de A vers A :
		// ----------------------------------------
		trs.put(IW_SUB_R , IW_SUB_R  , NOTHING);	
		trs.put(IP_SUB_R , IP_SUB_R  , NOTHING);	
		trs.put(FE_SUB_R , FE_SUB_R  , NOTHING);
		
		// Autoriser les transitions metier :
		// ----------------------------------				
		trs.put(N_R   , IW_SUB_R, new BasicTransition() {
			@Override public void execute(ContextValidation contextValidation, Submission submission, State nextState) {
				submissionWorkflowsHelper.createDirSubmission(submission, contextValidation);
			}
			@Override public void success(ContextValidation contextValidation, Submission submission, State nextState) {
				submissionWorkflowsHelper.updateSubmissionChildObject(submission, contextValidation);
			}
		});	
		trs.put(IW_SUB_R, IP_SUB_R, new BasicTransition() {
			@Override public void success(ContextValidation contextValidation, Submission submission, State nextState) {
				submissionWorkflowsHelper.updateSubmissionChildObject(submission, contextValidation);
			}
			@Override public void error(ContextValidation contextValidation, Submission submission, State nextState) {		
				try {
                    submissionWorkflowsHelper.rollbackSubmission(submission, contextValidation);
                } catch (SraException e) {
                    logger.error(e.getMessage(), e);
                }
			}
		});			
		trs.put(IP_SUB_R, F_SUB, new BasicTransition() {
			@Override public void success(ContextValidation contextValidation, Submission submission, State nextState) {
				submissionWorkflowsHelper.updateSubmissionRelease(submission);
				submissionWorkflowsHelper.updateSubmissionChildObject(submission, contextValidation);
			}
		});	
		trs.put(IP_SUB_R, FE_SUB_R, new BasicTransition() {
			@Override public void success(ContextValidation contextValidation, Submission submission, State nextState) {
				submissionWorkflowsHelper.updateSubmissionChildObject(submission, contextValidation);
			}
		});	
		// Autoriser les transitions reprise sur erreurs :
		// -----------------------------------------------
		trs.put(FE_SUB_R, IW_SUB_R  , new BasicTransition() {
			@Override public void success(ContextValidation contextValidation, Submission submission, State nextState) {
				submissionWorkflowsHelper.updateSubmissionChildObject(submission, contextValidation);
			}			
		});
		
	}


	@Override
	public Transition<Submission> get(String currentStateCode, String nextStateCode) {
		return trs.get(currentStateCode, nextStateCode);
	}
	
	@Override
	public ObjectType.CODE getObjectType() {
		return ObjectType.CODE.SRASubmission;
	}
	
	@Override
	public String getCollectionName() {
		return InstanceConstants.SRA_SUBMISSION_COLL_NAME;
	}
	@Override
	public Class<Submission> getElementClass() {
		return Submission.class;
	}
	
	public void activateSubmissionRelease(ContextValidation contextValidation, Submission submission) {
		//submission.setState(new State(SubmissionWorkflows.IW_SUB_R, contextValidation.getUser()));
//		setState(contextValidation, submission, new State(SubmissionWorkflows.IW_SUB_R, contextValidation.getUser()));
		setState(contextValidation, submission, new State(IW_SUB_R, contextValidation.getUser()));
	}
	
}
