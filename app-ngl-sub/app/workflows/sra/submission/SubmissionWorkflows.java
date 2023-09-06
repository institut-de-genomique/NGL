package workflows.sra.submission;

import static ngl.refactoring.state.SRASubmissionStateNames.SUBR_N;
import static ngl.refactoring.state.SRASubmissionStateNames.SUBR_SMD_F;
import static ngl.refactoring.state.SRASubmissionStateNames.SUBR_SMD_FE;
import static ngl.refactoring.state.SRASubmissionStateNames.SUBR_SMD_IP;
import static ngl.refactoring.state.SRASubmissionStateNames.SUBR_SMD_IW;
import static ngl.refactoring.state.SRASubmissionStateNames.SUBR_FE;

import static ngl.refactoring.state.SRASubmissionStateNames.SUB_F;
import static ngl.refactoring.state.SRASubmissionStateNames.SUB_N;
import static ngl.refactoring.state.SRASubmissionStateNames.SUB_SMD_F;
import static ngl.refactoring.state.SRASubmissionStateNames.SUB_FE;
import static ngl.refactoring.state.SRASubmissionStateNames.SUB_SMD_IP;
import static ngl.refactoring.state.SRASubmissionStateNames.SUB_SMD_IW;
import static ngl.refactoring.state.SRASubmissionStateNames.SUB_SRD_F;
import static ngl.refactoring.state.SRASubmissionStateNames.SUB_SRD_FE;
import static ngl.refactoring.state.SRASubmissionStateNames.SUB_SMD_FE;


import static ngl.refactoring.state.SRASubmissionStateNames.SUB_SRD_IP;
import static ngl.refactoring.state.SRASubmissionStateNames.SUB_SRD_IW;
import static ngl.refactoring.state.SRASubmissionStateNames.SUB_V;

import static ngl.refactoring.state.SRASubmissionStateNames.SUBU_N;
import static ngl.refactoring.state.SRASubmissionStateNames.SUBU_SMD_IW;
import static ngl.refactoring.state.SRASubmissionStateNames.SUBU_SMD_IP;
import static ngl.refactoring.state.SRASubmissionStateNames.SUBU_SMD_F;
import static ngl.refactoring.state.SRASubmissionStateNames.SUBU_SMD_FE;
import static ngl.refactoring.state.SRASubmissionStateNames.SUBU_FE;


import java.util.Date;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;

import fr.cea.ig.authentication.Authentication;
import fr.cea.ig.lfw.utils.DoubleKeyMap;
import models.laboratory.common.description.ObjectType;
import models.laboratory.common.instance.State;
import models.sra.submit.sra.instance.Submission;
import models.sra.submit.util.SraException;
import models.utils.InstanceConstants;
import sra.api.submission.SubmissionNewAPI;
import sra.api.submission.SubmissionNewAPITools;
import validation.ContextValidation;
//import static ngl.refactoring.state.SRASubmissionStateNames.SUBR_N;

//import workflows.sra.submission.transition.N_to_V_SUB;

//@Service
@Singleton
public class SubmissionWorkflows extends StateMachine<Submission> {


	// Si references circulaires on peut utiliser un provider car import qui sera chargé uniquement 
    // au moment du get, d'ou pas de circularité.
	// mais mieux d'enlever circularité si possible.
	private final SubmissionNewAPI      submissionNewAPI;
	private final SubmissionNewAPITools submissionNewAPITools;
	private DoubleKeyMap<String, String, Transition<Submission>> trs;
	private static final play.Logger.ALogger logger = play.Logger.of(SubmissionWorkflows.class);

	
	public abstract class BasicTransition implements Transition<Submission> {
		@Override
		public void execute(ContextValidation contextValidation, Submission submission, State nextState) {}
		@Override
		public void success(ContextValidation contextValidation, Submission submission, State nextState) {}
		@Override
		public void error(ContextValidation contextValidation, Submission object, State nextState) {}
	}
	
	// Transition avec methode execute qui contient methode qui mettra l'objet dans etat final
	// Transition qui fait l'update grace a sa methode execute
	public class TransitionUpdatingState extends BasicTransition{
		@Override
		public boolean isAutonomous() {// autonome pour le changement de state => dans methodes appelees dans execute
			return true;
		}			
	}
	// Transition qui delegue le changement de state au workflow, ce n'est pas la methode execute de la transition qui assure le changement de state.
	// Transition qui delegue l'update du state au workflow
	public class TransitionDelegatingStateUpdate extends BasicTransition{
		@Override
		public boolean isAutonomous() { // n'est pas autonome pour le changement de state => necessite workflow
			return false;
		}			
	}
	
//	public class BasicTransition implements Transition<Submission> {
//		@Override
//		public void execute(ContextValidation contextValidation, Submission submission, State nextState) {}
//		@Override
//		public void success(ContextValidation contextValidation, Submission submission, State nextState) {}
//		@Override
//		public void error(ContextValidation contextValidation, Submission object, State nextState) {}
//		@Override
//		// toutes les tran
//		public boolean isAutonomous() {
//			return true;
//		}		
//	}

	@Inject
	public SubmissionWorkflows(SubmissionNewAPI          submissionNewAPI,
							   SubmissionNewAPITools     submissionNewAPITools
//							   Provider<FileAcServices>  fileAcServices,
							   ) {
//		this.fileAcServices            = fileAcServices;
		this.submissionNewAPI          = submissionNewAPI;
		this.submissionNewAPITools     = submissionNewAPITools;
		initSubmissionWorkflows();
	}

	class N_V_SUB_Transition extends TransitionDelegatingStateUpdate {
		@Override public void success(ContextValidation contextValidation, Submission submission, State nextState) {
//			submissionWorkflowsHelper.updateSubmissionChildObject(submission, contextValidation);
			try {
				submissionNewAPITools.updateSubmissionChildObject(submission);
			} catch (SraException e) {
				throw new RuntimeException(e);
			}
		}	
	}
	class N_V_SUB_Transition_ implements Transition<Submission> {
		@Override public void success(ContextValidation contextValidation, Submission submission, State nextState) {
			submissionNewAPITools.updateSubmissionChildObject(submission);
		}

		@Override
		public void execute(ContextValidation contextValidation, Submission object, State nextState) {
		}

		@Override
		public void error(ContextValidation contextValidation, Submission object, State nextState) {
		}

		@Override
		public boolean isAutonomous() {
			return false;
		}	
	}

	private State getState (String stateCode ) {
		State state = new State();
		state.date = new Date();
		String user = Authentication.getUser();
		if(StringUtils.isBlank(user)) {
			throw new RuntimeException("workflows.sra.submission::getState: Pas de user authentifié avec Authentication.getUser()");
		}
		state.user = user;
		state.code = stateCode;
		return state;
	}
	
	void initSubmissionWorkflows() {
		trs = new DoubleKeyMap<>();
		BasicTransition NOTHING = new TransitionUpdatingState();
	
//		trs.put(SUB_N      , SUB_N      , NOTHING);// Autoriser transistion de A vers A qui ne fait rien.
		trs.put(SUB_V      , SUB_V      , NOTHING);	
		
		
		// Autoriser les transitions metiers pour les soumissions primaires de données :
		//------------------------------------------------------------------------------
		
		// implementation d'une sous-classe de BasicTransition anonyme et interne 
		// classe interne qui existera au sein de l'instance environnementale SubmissionWorkflows
		
		// Interface validation soumission de ngl : 

		// NonAutonomousTransition => C'est la stateMachine qui met le state à la valeur finale indiquée
		// par la transition fait la validation et sauve dans base
		
		 //AutonomousTransition => c'est le code exec qui met le state et historique, qui fait la validation
		 // et sauve dans database
		
		// Transition appelée depuis interface creation soumission puis setState
		trs.put(SUB_N     , SUB_V   , new TransitionDelegatingStateUpdate() {		
			@Override public void success(ContextValidation contextValidation, Submission submission, State nextState) {
				//logger.debug("transition de SUB-N vers SUB-V reussie");
				submissionNewAPITools.updateSubmissionChildObject(submission);			
			}
		});

		// Interface activation de ngl :		
		trs.put(SUB_V , SUB_SRD_IW  , new TransitionUpdatingState() {
			@Override public void execute(ContextValidation contextValidation, Submission submission, State nextState) {
				submissionNewAPI.startPrimarySubmission(contextValidation, submission);
			}			
		});
		
		// Interface activation de ngl si pas de données brutes 		
		trs.put(SUB_V , SUB_SMD_IW  , new TransitionUpdatingState() {
			@Override public void execute(ContextValidation contextValidation, Submission submission, State nextState) {
				submissionNewAPI.startPrimarySubmission(contextValidation, submission);
				submissionNewAPI.writeMetaData(submission, contextValidation);
			}	
		});
		
//		// Cas d'une creation d'un project umbrella		
//		trs.put(SUB_V , SUB_SMD_IW  , new TransitionUpdatingState() {
//			@Override public void execute(ContextValidation contextValidation, Submission submission, State nextState) {
//				// creer repertoire de soumission et mettre à jour soumission pour directorySubmission
//				submissionNewAPI.startPrimarySubmission(contextValidation, submission);
//			}
//			@Override public void success(ContextValidation contextValidation, Submission submission, State nextState) {
//				// Ecrire metadonnées :
//				submissionNewAPI.writeMetaData(submission, contextValidation);				
//			}
//		});	
		
		// Transition utilisée par birds :
		trs.put(SUB_SRD_IW , SUB_SRD_IP  , new TransitionDelegatingStateUpdate() {		
			@Override public void success(ContextValidation contextValidation, Submission submission, State nextState) {
				submissionNewAPITools.updateSubmissionChildObject(submission);
			}
		});	
		
		// Transition utilisée par birds une fois données brutes envoyées:
		trs.put(SUB_SRD_IP , SUB_SRD_F  , new TransitionDelegatingStateUpdate() {		
			@Override public void success(ContextValidation contextValidation, Submission submission, State nextState) {
				logger.debug("transition SRD-IP vers SRD-F pour la soumission " + submission.code );
				submissionNewAPITools.updateSubmissionChildObject(submission);			
				// declencher sans aucune action, le passage dans transition suivante en installant le state à SUB_SMD_IW
				// attention ne marcherait pas dans execute, à placer dans success.
				State prochainState = getState(SUB_SMD_IW);
				prochainState.user = nextState.user;
				
				logger.debug("PROCHAIN STATE date " + prochainState.date);
				setState(contextValidation, submission, getState(SUB_SMD_IW));
				logger.debug("transition de SRD-F vers SMD-IW pour la soumission " + submission.code);
			}
		});	
		
		// Transition utilisée par birds si problemes envoie données brutes:
		trs.put(SUB_SRD_IP , SUB_SRD_FE  , new TransitionDelegatingStateUpdate() {		
			@Override public void success(ContextValidation contextValidation, Submission submission, State nextState) {
				submissionNewAPITools.updateSubmissionChildObject(submission);			
			}
		});		
		// Transition appelée par la transition precedente		
		trs.put(SUB_SRD_F, SUB_SMD_IW  , new TransitionUpdatingState() {	
			@Override public void execute(ContextValidation contextValidation, Submission submission, State nextState) {
//				// Ecrire metadonnées avec md5 nouvellement calcules si CCRT mais deja dans base:
				logger.debug("XXXXXXXXXXXXXXXXXXXXX     Dans execute de la transition SUB-SRD-F vers SUB-SMD-IW");
				submissionNewAPI.writeMetaData(submission, contextValidation);	
			}
		});
		
		// Transition utilisée par birds :
		trs.put(SUB_SMD_IW, SUB_SMD_IP  , new TransitionDelegatingStateUpdate() {
			@Override public void success(ContextValidation contextValidation, Submission submission, State nextState) {
				submissionNewAPITools.updateSubmissionChildObject(submission);
			}
		});	
	
		// Transition utilisée par birds :
		trs.put(SUB_SMD_IP, SUB_SMD_F  , new TransitionDelegatingStateUpdate() {
			@Override public void success(ContextValidation contextValidation, Submission submission, State nextState) {
				submissionNewAPITools.updateSubmissionChildObject(submission);
				// declencher sans aucune action, le passage dans transition suivante en installant le state à SUB_F
				setState(contextValidation, submission, getState(SUB_F));
			}
		});	
		// Transition utilisée par birds si erreur dans envoie metadonnées:
		trs.put(SUB_SMD_IP, SUB_SMD_FE  , new TransitionDelegatingStateUpdate() {
			@Override public void success(ContextValidation contextValidation, Submission submission, State nextState) {
				submissionNewAPITools.updateSubmissionChildObject(submission);
			}
		});	
		// Transition utilisée par submissionNewAPI.loadEbiResponseAC(ctxVal, sub.code) :
		trs.put(SUB_SMD_F, SUB_F   , new TransitionUpdatingState() {	
			@Override public void execute(ContextValidation contextValidation, Submission submission, State nextState) {
				logger.debug("Dans transition SUB-SMD-F vers SUB-F, avant appel submissionNewAPI.loadEbiResponseAC");
				submissionNewAPI.loadEbiResponseAC(contextValidation, submission);
				logger.debug("Dans transition SUB-SMD-F vers SUB-F, apres appel submissionNewAPI.loadEbiResponseAC");
			}
		});	
		
		// Transition utilisée par submissionNewAPI.loadEbiResponseAC(ctxVal, sub.code) :
		trs.put(SUB_SMD_F, SUB_FE , new TransitionUpdatingState() {	
			@Override public void execute(ContextValidation contextValidation, Submission submission, State nextState) {
				submissionNewAPI.loadEbiResponseAC(contextValidation, submission);
			}
		});			
		// Autoriser les transitions reprise sur erreur :
		trs.put(SUB_SRD_FE, SUB_SRD_IP,new TransitionDelegatingStateUpdate() {	
			@Override public void success(ContextValidation contextValidation, Submission submission, State nextState) {
				submissionNewAPITools.updateSubmissionChildObject(submission);
			}
		});			
		// Autoriser les transitions reprise sur erreur :
		trs.put(SUB_SMD_FE, SUB_SMD_IP,new TransitionDelegatingStateUpdate() {	
			@Override public void success(ContextValidation contextValidation, Submission submission, State nextState) {
				submissionNewAPITools.updateSubmissionChildObject(submission);
			}
		});					
		// Autoriser les transitions reprise sur erreur :(pas utile,sorti de birds, pas de process à relancer)
		trs.put(SUB_FE, SUB_SMD_IP,new TransitionDelegatingStateUpdate() {	
			@Override public void success(ContextValidation contextValidation, Submission submission, State nextState) {
				submissionNewAPITools.updateSubmissionChildObject(submission);
			}
		});			

		// Autoriser les transitions reprise sur erreur :
		trs.put(SUB_FE, SUB_SMD_F,new TransitionDelegatingStateUpdate() {	
			@Override public void success(ContextValidation contextValidation, Submission submission, State nextState) {
				//logger.debug("Je passe bien dans la transition SU-FE vers SUB_SMD_F");
				submissionNewAPITools.updateSubmissionChildObject(submission);
				//logger.debug("objets enfants mis à SUB_SMD-F et appel setState pour SUB-F");
				setState(contextValidation, submission, getState(SUB_F)); 
				// inutile de refaire updateSubmissionChildObject car on est reparti avec le setState 
				// sur une transition updatingState
			}
		});		

		// Autoriser les transitions metier pour les soumissions de type release d'un study :
		// ----------------------------------------------------------------------------------	
		// Transition appelée appelée au niveau de details.js (bouton releaseStudy) 
		// apres controllers.sra.submissions.api. createFromStudyRelease(studyCode);
		// au niveau de controllers.sra.submissions.api.updateState(SUBR_SMD_IW)
		trs.put(SUBR_N   , SUBR_SMD_IW, new TransitionUpdatingState() {	
			@Override public void execute(ContextValidation contextValidation, Submission submission, State nextState) {
				submissionNewAPI.writeAndActivateSubmissionRelease(contextValidation, submission);
			}
		});	
		// Transition utilisée par birds :
		trs.put(SUBR_SMD_IW   , SUBR_SMD_IP, new TransitionDelegatingStateUpdate() {	
			@Override public void success(ContextValidation contextValidation, Submission submission, State nextState) {
				submissionNewAPITools.updateSubmissionChildObject(submission);
			}
		});
		// Transition utilisée par birds :
		trs.put(SUBR_SMD_IP   , SUBR_SMD_F, new TransitionDelegatingStateUpdate() {	
			@Override public void success(ContextValidation contextValidation, Submission submission, State nextState) {
				submissionNewAPITools.updateSubmissionChildObject(submission);
				// declencher sans aucune action, le passage dans transition suivante en installant le state à SUB_F
				setState(contextValidation, submission, getState(SUB_F));
			}
		});	
		// Transition utilisée par birds :
		trs.put(SUBR_SMD_IP   , SUBR_SMD_FE, new TransitionDelegatingStateUpdate() {	
			@Override public void success(ContextValidation contextValidation, Submission submission, State nextState) {
				submissionNewAPITools.updateSubmissionChildObject(submission);
			}
		});	
		// Transition utilisée par submissionNewAPI.loadRespEbiForRelease(ctxVal, sub.code) appelée par la 
		// transition precedente
		trs.put(SUBR_SMD_F   , SUB_F, new TransitionUpdatingState() {	
			@Override public void execute(ContextValidation contextValidation, Submission submission, State nextState) {
				submissionNewAPI.loadRespEbiForRelease(contextValidation, submission);
			}
		});	
		// Transition utilisée par submissionNewAPI.loadRespEbiForRelease(ctxVal, sub.code) appelée par la 
		// transition precedente
		trs.put(SUBR_SMD_F   , SUBR_FE, new TransitionUpdatingState() {	
			@Override public void execute(ContextValidation contextValidation, Submission submission, State nextState) {
				submissionNewAPI.loadRespEbiForRelease(contextValidation, submission);
			}
		});	
		// Autoriser les transitions reprise sur erreur :
		trs.put(SUBR_SMD_FE   , SUBR_SMD_IP, new TransitionDelegatingStateUpdate() {	
			@Override public void success(ContextValidation contextValidation, Submission submission, State nextState) {
				submissionNewAPITools.updateSubmissionChildObject(submission);
			}
		});	
		trs.put(SUBR_FE   , SUBR_SMD_IP, new TransitionDelegatingStateUpdate() {	
			@Override public void success(ContextValidation contextValidation, Submission submission, State nextState) {
				submissionNewAPITools.updateSubmissionChildObject(submission);
			}
		});	
		trs.put(SUBR_FE   , SUBR_SMD_F, new TransitionDelegatingStateUpdate() {	
			@Override public void success(ContextValidation contextValidation, Submission submission, State nextState) {
				submissionNewAPITools.updateSubmissionChildObject(submission);
				setState(contextValidation, submission, getState(SUB_F));
			}
		});	
		
		// Autoriser les transitions metier pour les soumissions de type update :
		// ----------------------------------------------------------------------	
		// Transition utilisé par interface update de study, sample ou experiment(bouton submit)
		trs.put(SUBU_N   , SUBU_SMD_IW, new TransitionUpdatingState() {	
			@Override public void execute(ContextValidation contextValidation, Submission submission, State nextState) {
				submissionNewAPI.writeAndActivateSubmissionUpdate(contextValidation, submission);
			}
		});	
		// transition utilisé par birds
		trs.put(SUBU_SMD_IW   , SUBU_SMD_IP, new TransitionDelegatingStateUpdate() {	
			@Override public void success(ContextValidation contextValidation, Submission submission, State nextState) {
				submissionNewAPITools.updateSubmissionChildObject(submission);
			}
		});
		// transition utilisé par birds
		trs.put(SUBU_SMD_IP   , SUBU_SMD_F, new TransitionDelegatingStateUpdate() {	
			@Override public void success(ContextValidation contextValidation, Submission submission, State nextState) {
				submissionNewAPITools.updateSubmissionChildObject(submission);
				// declencher sans aucune action, le passage dans transition suivante en installant le state à SUB_F
				setState(contextValidation, submission, getState(SUB_F));
			}
		});	
		// Transition utilisée par submissionNewAPI.loadRespEbiForUpdate(ctxVal, sub.code) appelée par la 
		// transition precedente
		trs.put(SUBU_SMD_F   , SUB_F, new TransitionUpdatingState() {	
			@Override public void execute(ContextValidation contextValidation, Submission submission, State nextState) {
				submissionNewAPI.loadRespEbiForUpdate(contextValidation, submission);
			}
		});	
		// Transition utilisée par birds
		trs.put(SUBU_SMD_IP   , SUBU_SMD_FE, new TransitionDelegatingStateUpdate() {	
			@Override public void success(ContextValidation contextValidation, Submission submission, State nextState) {
				submissionNewAPITools.updateSubmissionChildObject(submission);
			}
		});	
		// Transition utilisée par submissionNewAPI.loadRespEbiForUpdate(ctxVal, sub.code) appelée par la 
		// transition precedente
		trs.put(SUBU_SMD_F   , SUBU_FE, new TransitionUpdatingState() {	
			@Override public void execute(ContextValidation contextValidation, Submission submission, State nextState) {
				submissionNewAPI.loadRespEbiForUpdate(contextValidation, submission);
			}
		});	
		
		// Autoriser les transitions reprise sur erreur :
		trs.put(SUBU_FE   , SUBU_SMD_IP, new TransitionDelegatingStateUpdate() {	
			@Override public void success(ContextValidation contextValidation, Submission submission, State nextState) {
				submissionNewAPITools.updateSubmissionChildObject(submission);
			}
		});	
		trs.put(SUBU_FE   , SUBU_SMD_F, new TransitionDelegatingStateUpdate() {	
			@Override public void success(ContextValidation contextValidation, Submission submission, State nextState) {
				submissionNewAPITools.updateSubmissionChildObject(submission);
				setState(contextValidation, submission, getState(SUB_F)); 
				// avec le setState(SUB_F) on passe dans la transition SUBU_SMD_F vers SUB_F en executant loadRespEbiForUpdate
			}
		});	
		// Autoriser les transitions reprise sur erreur :
		trs.put(SUBU_SMD_FE   , SUBU_SMD_IP, new TransitionDelegatingStateUpdate() {	
			@Override public void success(ContextValidation contextValidation, Submission submission, State nextState) {
				submissionNewAPITools.updateSubmissionChildObject(submission);
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
	
	@Deprecated
	public void activateSubmissionRelease(ContextValidation contextValidation, Submission submission) {
//		setState(contextValidation, submission, new State(SubmissionWorkflows.IW_SUB_R, contextValidation.getUser()));
		setState(contextValidation, submission, new State(SUBR_SMD_IW, contextValidation.getUser()));
	}
	
}
