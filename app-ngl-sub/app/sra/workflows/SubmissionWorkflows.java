package sra.workflows;

import static ngl.refactoring.state.SRASubmissionStateNames.SUBR_N;
import static ngl.refactoring.state.SRASubmissionStateNames.SUBR_SMD_F;
import static ngl.refactoring.state.SRASubmissionStateNames.SUBR_SMD_FE;
import static ngl.refactoring.state.SRASubmissionStateNames.SUBR_SMD_IP;
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
import models.utils.InstanceConstants;
import services.sra.SraException;
import sra.api.SubmissionNewAPI;
import sra.api.SubmissionNewAPITools;
import validation.ContextValidation;
//import static ngl.refactoring.state.SRASubmissionStateNames.SUBR_N;

//import workflows.sra.submission.transition.N_to_V_SUB;

//@Service
@Singleton
public class SubmissionWorkflows extends StateMachine<Submission> {

	
	
	// methode à utiliser en cas d'erreur lors du processus de release
	public void debugRelease(ContextValidation ctxVal, Submission submission) throws SraException {	
		logger.debug("entree dans debugRelease, avec submission=" + submission.code + " et state=" + submission.state.code);
		String user = Authentication.getUser();
		// voir si on ne doit pas remplacer Authentication.getUser() par ctxVal.getUser() car pb avec cron dans ngl_data en isoprod pour l'authentification.
		// Envoyer Metadonnées à l'EBI et mettre en base soumission avec etat SUBR-SMD-F :
		////submissionNewAPI.ebiCurlForRelease(submission, contextValidation, user);
		submission.state.code = SUBR_SMD_IP;
		logger.debug("dans debugRelease avant appel de setState(SUBR_SMD_F), avec submission=" + submission.code + " et state=" + submission.state.code);
		this.setState(ctxVal, submission, new State(SUBR_SMD_F, user));
		if(ctxVal.hasErrors()) {
			this.setState(ctxVal, submission, new State(SUBR_SMD_FE, user));
		}
		SraException.assertNoError(ctxVal);
		logger.debug("!!!! debugRelease :: Envoie des metadonnees a l'EBI ok pour la submission " + submission.code + " avec state="  + submission.state.code);
		
		// Charger Rapport de soumission de l'EBI et mettre en base soumission avec etat SUB-F ou SUBR-FE :
		////submissionNewAPI.loadRespEbiForRelease(contextValidation, submission);
		this.setState(ctxVal, submission, new State(SUB_F, user));
		if(ctxVal.hasErrors()) {
			this.setState(ctxVal, submission, new State(SUBR_FE, user));
		}
		SraException.assertNoError(ctxVal);
		logger.debug("!!!! debugRelease :: Chargement du rapport de l'EBI ok pour la submission " + submission.code + " avec state="  + submission.state.code);
	}
	
	/**
	 * Positionne l'etat à SUB-F ou SUB-FE
	 * @param ctxVal         contexte de validation
	 * @param submission     soumission
	 * @throws SraException  erreur
	 */
	// methode à utiliser en cas d'erreur lors du processus d'update
	public void debugUpdate(ContextValidation ctxVal, Submission submission) throws SraException {	
		logger.debug("entree dans debugUpdate, avec submission=" + submission.code + " et state=" + submission.state.code);
		String user = Authentication.getUser();
		// voir si on ne doit pas remplacer Authentication.getUser() par ctxVal.getUser() car pb avec cron dans ngl_data en isoprod pour l'authentification.

		// Envoyer Metadonnées à l'EBI et mettre en base soumission avec etat SUBU-SMD-F :
		////submissionNewAPI.ebiCurlForRelease(submission, contextValidation, user);
		submission.state.code = SUBU_SMD_IP;
		logger.debug("dans debugUpdate avant appel de setState(SUBU_SMD_F), avec submission=" + submission.code + " et state=" + submission.state.code);
		this.setState(ctxVal, submission, new State(SUBU_SMD_F, user));
		if(ctxVal.hasErrors()) {
			this.setState(ctxVal, submission, new State(SUBU_SMD_FE, user));
		}
		SraException.assertNoError(ctxVal);
		logger.debug("!!!! debugUpdate :: Envoie des metadonnees a l'EBI ok pour la submission " + submission.code + " avec state="  + submission.state.code);
		
		// Charger Rapport de soumission de l'EBI et mettre en base soumission avec etat SUB-F ou SUBR-FE :
		////submissionNewAPI.loadRespEbiForRelease(contextValidation, submission);
		this.setState(ctxVal, submission, new State(SUB_F, user));
		if(ctxVal.hasErrors()) {
			this.setState(ctxVal, submission, new State(SUBU_FE, user));
		}
		SraException.assertNoError(ctxVal);
		logger.debug("!!!! debugUpdate :: Chargement du rapport de l'EBI ok pour la submission " + submission.code + " avec state="  + submission.state.code);
	}
	
	
	
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
	
	/** 
	* Transition avec methode execute qui contient methode qui mettra l'objet dans etat final
	*  transition qui fait l'update grace a la methode execute que l'utilisateur doit implementer.
	*/
	public class TransitionManualState extends BasicTransition{
		@Override
		public boolean isAutonomous() {// autonome pour le changement de state => dans methodes appelees dans execute
			return true;
		}			
	}
	/**
	 * TransitionDelegatingStateUpdateToStateMachine
	* Gestion de l'etat non gere par l'execute mais par le {@link StateMachine}.
	* Transition qui delegue le changement de state au workflow, ce n'est pas la methode execute definit par l'utilisateur de la transition qui assure le changement de state.
	* Transition qui delegue l'update du state au workflow
	*/
	public class TransitionAutomaticState extends BasicTransition {
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

	class N_V_SUB_Transition extends TransitionAutomaticState {
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
		// voir si on ne doit pas remplacer Authentication.getUser() par ctxVal.getUser() car pb avec cron dans ngl_data en isoprod pour l'authentification.
		if(StringUtils.isBlank(user)) {
			throw new RuntimeException("workflows.sra.submission::getState: Pas de user authentifié avec Authentication.getUser()");
		}
		state.user = user;
		state.code = stateCode;
		return state;
	}
	
	void initSubmissionWorkflows() {
		trs = new DoubleKeyMap<>();
		BasicTransition NOTHING = new TransitionManualState();
	
//		trs.put(SUB_N           , SUB_N       , NOTHING);// Autoriser transistion de A vers A qui ne fait rien.
		trs.put(SUB_V           , SUB_V       , NOTHING);	
		trs.put(SUBR_SMD_IP     , SUBR_SMD_IP , NOTHING);	
		
		
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
		// La soumission passe de SUB-N à SUB-V, et ensuite, propagation de l'etat SUB-V aux enfants.
		trs.put(SUB_N     , SUB_V   , new TransitionAutomaticState() {		
			@Override public void success(ContextValidation contextValidation, Submission submission, State nextState) {
				//logger.debug("transition de SUB-N vers SUB-V reussie");
				submissionNewAPITools.updateSubmissionChildObject(submission);			
			}
		});

		// Interface activation de ngl :		
		trs.put(SUB_V , SUB_SRD_IW  , new TransitionManualState() {
			@Override public void execute(ContextValidation contextValidation, Submission submission, State nextState) {
				submissionNewAPI.startPrimarySubmission(contextValidation, submission);
			}			
		});
		
		// Interface activation de ngl si pas de données brutes 		
		trs.put(SUB_V , SUB_SMD_IW  , new TransitionManualState() {
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
		trs.put(SUB_SRD_IW , SUB_SRD_IP  , new TransitionAutomaticState() {		
			@Override public void success(ContextValidation contextValidation, Submission submission, State nextState) {
				submissionNewAPITools.updateSubmissionChildObject(submission);
			}
		});	
		
		// Transition utilisée par birds une fois données brutes envoyées:
		trs.put(SUB_SRD_IP , SUB_SRD_F  , new TransitionAutomaticState() {		
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
		trs.put(SUB_SRD_IP , SUB_SRD_FE  , new TransitionAutomaticState() {		
			@Override public void success(ContextValidation contextValidation, Submission submission, State nextState) {
				submissionNewAPITools.updateSubmissionChildObject(submission);			
			}
		});		
		// Transition appelée par la transition precedente		
		trs.put(SUB_SRD_F, SUB_SMD_IW  , new TransitionManualState() {	
			@Override public void execute(ContextValidation contextValidation, Submission submission, State nextState) {
//				// Ecrire metadonnées avec md5 nouvellement calcules si CCRT mais deja dans base:
				//logger.debug("XXXXXXXXXXXXXXXXXXXXX     Dans execute de la transition SUB-SRD-F vers SUB-SMD-IW");
				submissionNewAPI.writeMetaData(submission, contextValidation);	
			}
		});
		
		// Transition utilisée par birds :
		trs.put(SUB_SMD_IW, SUB_SMD_IP  , new TransitionAutomaticState() {
			@Override public void success(ContextValidation contextValidation, Submission submission, State nextState) {
				submissionNewAPITools.updateSubmissionChildObject(submission);
			}
		});	
	
		// Transition utilisée par birds :
		trs.put(SUB_SMD_IP, SUB_SMD_F  , new TransitionAutomaticState() {
			@Override public void success(ContextValidation contextValidation, Submission submission, State nextState) {
				submissionNewAPITools.updateSubmissionChildObject(submission);
				// declencher sans aucune action, le passage dans transition suivante en installant le state à SUB_F
				setState(contextValidation, submission, getState(SUB_F));
			}
		});	
		// Transition utilisée par birds si erreur dans envoie metadonnées:
		trs.put(SUB_SMD_IP, SUB_SMD_FE  , new TransitionAutomaticState() {
			@Override public void success(ContextValidation contextValidation, Submission submission, State nextState) {
				submissionNewAPITools.updateSubmissionChildObject(submission);
			}
		});	
		// Transition utilisée par submissionNewAPI.loadRespEbiForCreation(ctxVal, sub.code) :
		trs.put(SUB_SMD_F, SUB_F   , new TransitionManualState() {	
			@Override public void execute(ContextValidation contextValidation, Submission submission, State nextState) {
				//logger.debug("Dans transition SUB-SMD-F vers SUB-F, avant appel submissionNewAPI.loadRespEbiForCreation");
				submissionNewAPI.loadRespEbiForCreation(contextValidation, submission);
				//logger.debug("Dans transition SUB-SMD-F vers SUB-F, apres appel submissionNewAPI.loadRespEbiForCreation");
			}
		});	
		
		// Transition utilisée par submissionNewAPI.loadRespEbiForCreation(ctxVal, sub.code) :
		trs.put(SUB_SMD_F, SUB_FE , new TransitionManualState() {	
			@Override public void execute(ContextValidation contextValidation, Submission submission, State nextState) {
				submissionNewAPI.loadRespEbiForCreation(contextValidation, submission);
			}
		});			
		// Autoriser les transitions reprise sur erreur :
		trs.put(SUB_SRD_FE, SUB_SRD_IP,new TransitionAutomaticState() {	
			@Override public void success(ContextValidation contextValidation, Submission submission, State nextState) {
				submissionNewAPITools.updateSubmissionChildObject(submission);
			}
		});			
		// Autoriser les transitions reprise sur erreur :
		trs.put(SUB_SMD_FE, SUB_SMD_IP,new TransitionAutomaticState() {	
			@Override public void success(ContextValidation contextValidation, Submission submission, State nextState) {
				submissionNewAPITools.updateSubmissionChildObject(submission);
			}
		});					
		// Autoriser les transitions reprise sur erreur :(pas utile,sorti de birds, pas de process à relancer)
		trs.put(SUB_FE, SUB_SMD_IP,new TransitionAutomaticState() {	
			@Override public void success(ContextValidation contextValidation, Submission submission, State nextState) {
				submissionNewAPITools.updateSubmissionChildObject(submission);
			}
		});			

		// Autoriser les transitions reprise sur erreur :
		trs.put(SUB_FE, SUB_SMD_F,new TransitionAutomaticState() {	
			@Override public void success(ContextValidation contextValidation, Submission submission, State nextState) {
				//logger.debug("Je passe bien dans la transition SU-FE vers SUB_SMD_F");
				submissionNewAPITools.updateSubmissionChildObject(submission);
				//logger.debug("objets enfants mis à SUB_SMD-F et appel setState pour SUB-F");
				setState(contextValidation, submission, getState(SUB_F)); 
				// inutile de refaire updateSubmissionChildObject car on est reparti avec le setState 
				// sur une transition updatingState
			}
		});		

		
		// ------------------------------------------------------------------------------------------------------------------------------------------	
		// Autoriser les transitions metier pour les soumissions de type release d'un study :
		// Transitions appelées par details.js (bouton releaseStudy) puis controllers.sra.submissions.api.createFromStudyRelease(studyCode);
		// puis sra.api.SubmissionWorkflowAPI.processSubmissionFromStudyRelease() ou sra.api.SubmissionWorkflowAPI.processSubmissionFromSampleRelease()
		// ------------------------------------------------------------------------------------------------------------------------------------------	
		
		trs.put(SUBR_N   , SUBR_SMD_IP, new TransitionManualState() {	
			@Override public void execute(ContextValidation contextValidation, Submission submission, State nextState) {
				logger.debug("dans transition SUBR_N vers SUBR_SMD_IP appel de submissionNewAPI.writeAndActivateSubmissionRelease");
				submissionNewAPI.writeAndActivateSubmissionRelease(contextValidation, submission);
			}
		});	
		
//		// Transition utilisée par birds supprimée avec ticket NGL-5030 :
//		trs.put(SUBR_SMD_IW   , SUBR_SMD_IP, new TransitionDelegatingStateUpdate() {	
//			@Override public void success(ContextValidation contextValidation, Submission submission, State nextState) {
//				submissionNewAPITools.updateSubmissionChildObject(submission);
//			}
//		});
		
		// soit on met ebiCurl avec  TransitionAutomaticState, soit on met ebiCurlForRelease avec TransitionManualState
		trs.put(SUBR_SMD_IP   , SUBR_SMD_F, new TransitionAutomaticState() {	
			@Override public void execute(ContextValidation contextValidation, Submission submission, State nextState) {
				logger.debug("ZZZZ   entree dans transition SUBR_SMD_IP vers SUBR_SMD_F, submission=" + submission.code + ", stateCode="+submission.state.code);
				// Ne pas verifier etat d'entree dans ebiCurl
				submissionNewAPI.ebiCurl(submission, contextValidation);
			}
			@Override public void success(ContextValidation contextValidation, Submission submission, State nextState) {
				submissionNewAPITools.updateSubmissionChildObject(submission);
				logger.debug("ZZZZ   sortie de transition SUBR_SMD_IP vers SUBR_SMD_F, dans success, submission=" + submission.code + ", stateCode="+submission.state.code);
			}
		});	

		trs.put(SUBR_SMD_IP   , SUBR_SMD_FE, new TransitionAutomaticState() {	
			@Override public void success(ContextValidation contextValidation, Submission submission, State nextState) {
				submissionNewAPITools.updateSubmissionChildObject(submission);
				logger.debug("ZZZZ   sortie de transition SUBR_SMD_IP vers SUBR_SMD_FE, submission=" + submission.code+ ", stateCode="+submission.state.code);
			}
		});	

		trs.put(SUBR_SMD_F   , SUB_F, new TransitionManualState() {	
			@Override public void execute(ContextValidation contextValidation, Submission submission, State nextState) {
				submissionNewAPI.loadRespEbiForRelease(contextValidation, submission);
			}
			@Override public void success(ContextValidation contextValidation, Submission submission, State nextState) {
				submissionNewAPITools.updateSubmissionChildObject(submission);
				logger.debug("ZZZZ   sortie success de transition SUBR_SMD_F vers SUBR_F,  submission=" + submission.code + ", stateCode="+submission.state.code);
			}
			// inutile car gere dans loadRespEbiForRelease qui met à SUBR-FE et propage aux enfants.
//			@Override public void error(ContextValidation contextValidation, Submission submission, State nextState) {
//				submission.state.code = SUBR_FE;
//				submissionNewAPITools.updateSubmissionChildObject(submission);
//				logger.debug("ZZZZ   sortie echec de transition SUBR_SMD_F vers SUBR_F,  submission=" + submission.code + ", stateCode="+submission.state.code);
//			}
		});	

		trs.put(SUBR_SMD_F   , SUBR_FE, new TransitionAutomaticState() {	
			@Override public void success(ContextValidation contextValidation, Submission submission, State nextState) {
				submissionNewAPITools.updateSubmissionChildObject(submission);
				logger.debug("ZZZZ   sortie de transition SUBR_SMD_F vers SUBR_FE,  submission=" + submission.code + ", stateCode="+submission.state.code);

			}
		});	
		
		// Autoriser les transitions reprise sur erreur :
		trs.put(SUBR_SMD_FE   , SUB_F, new TransitionManualState() {	
			@Override public void execute(ContextValidation contextValidation, Submission submission, State nextState) {
				logger.debug("ZZZZ  EEE   entree de transition SUBR_SMD_FE vers SUB_F, submission=" + submission.code + ", stateCode="+submission.state.code);
				logger.debug("ZZZZ  EEE   dans transition SUBR_SMD_FE vers SUB_F, appel de processSubmissionForReleaseDebug()");
				debugRelease(contextValidation, submission);
			}
			@Override public void success(ContextValidation contextValidation, Submission submission, State nextState) {
				submissionNewAPITools.updateSubmissionChildObject(submission);
				logger.debug("ZZZZ  EEE   sortie de transition SUBR_SMD_FE vers SUB_F, submission=" + submission.code + ", stateCode="+submission.state.code);

			}
		});	
		
		trs.put(SUBR_FE   , SUB_F, new TransitionManualState() {	
			// la transition verifie si etat initial de la soumission est bien SUBR_FE et met la soumission à SUBR_SMD_IP et si ok, alors process success.
			@Override public void execute(ContextValidation contextValidation, Submission submission, State nextState) {
				logger.debug("ZZZZ  EEE   entree de transition SUBR_FE vers SUB_F, submission=" + submission.code + ", stateCode="+submission.state.code);
				logger.debug("ZZZZ  EEE   dans transition SUBR_FE vers SUB_F, appel de processSubmissionForReleaseDebug()");
				debugRelease(contextValidation, submission);
			}
			@Override public void success(ContextValidation contextValidation, Submission submission, State nextState) {
				submissionNewAPITools.updateSubmissionChildObject(submission);
				logger.debug("ZZZZ  EEE   sortie de transition SUBR_FE vers SUB_F, submission=" + submission.code + ", stateCode="+submission.state.code);
			}
		});	
		
		trs.put(SUBR_FE   , SUBR_FE, new TransitionAutomaticState() {	
			// la transition verifie si etat initial de la soumission est bien SUBR_FE et met la soumission à SUBR_SMD_IP et si ok, alors process success.
			@Override public void success(ContextValidation contextValidation, Submission submission, State nextState) {
				submissionNewAPITools.updateSubmissionChildObject(submission);
				logger.debug("ZZZZ  EEE   sortie de transition SUBR_FE vers SUBR_FE, submission=" + submission.code + ", stateCode="+submission.state.code);
			}
		});	
		
		// appelé lorsqu'on lance 2 fois transition  SUBR_FE  vers SUB_F ( support pour debuguer une soumission ) car la deuxième fois, l'EBI refuse de mettre en public un study deja public 
		trs.put(SUBR_SMD_FE   , SUBR_SMD_FE, new TransitionAutomaticState() {	
			@Override public void success(ContextValidation contextValidation, Submission submission, State nextState) {
				submissionNewAPITools.updateSubmissionChildObject(submission);
				logger.debug("ZZZZ  EEE   sortie de transition SUBR_SMD_FE vers SUBR_SMD_FE, submission=" + submission.code + ", stateCode="+submission.state.code);
			}
		});	

		
		// -----------------------------------------------------------------------------------------------------------------------------------------------------------	
		// Autoriser les transitions metier pour les soumissions de type release d'un study :
		// Transitions appelées par intefaces update de umbrella, study, sample ou experiment et bouton submit puis controllers.sra.submissions.api.createFromUpdate();
		// puis sra.api.SubmissionWorkflowAPI.processSubmissionFromUpdate() 
		// -----------------------------------------------------------------------------------------------------------------------------------------------------------	

		// transition modifiée pour ticket NGL-5042 :
		trs.put(SUBU_N   , SUBU_SMD_IP, new TransitionManualState() {	
			@Override public void execute(ContextValidation contextValidation, Submission submission, State nextState) {
				submissionNewAPI.writeAndActivateSubmissionUpdate(contextValidation, submission);
			}
		});	
		
//		// transition utilisee par birds supprimée pour ticket NGL-5042 :
//		trs.put(SUBU_SMD_IW   , SUBU_SMD_IP, new TransitionDelegatingStateUpdate() {	
//			@Override public void success(ContextValidation contextValidation, Submission submission, State nextState) {
//				submissionNewAPITools.updateSubmissionChildObject(submission);
//			}
//		});
//		// transition utilisee par birds supprimée pour ticket NGL-5042 :
//		trs.put(SUBU_SMD_IP   , SUBU_SMD_F, new TransitionDelegatingStateUpdate() {	
//			@Override public void success(ContextValidation contextValidation, Submission submission, State nextState) {
//				submissionNewAPITools.updateSubmissionChildObject(submission);
//				// declencher sans aucune action, le passage dans transition suivante en installant le state à SUB_F
//				setState(contextValidation, submission, getState(SUB_F));
//			}
//		});	

		trs.put(SUBU_SMD_IP   , SUBU_SMD_F, new TransitionAutomaticState() {	
			@Override public void execute(ContextValidation contextValidation, Submission submission, State nextState) {
				logger.debug("dans transition SUBU_SMD_IP vers SUBU_SMD_F appel de submissionNewAPI.ebiCurl");
				submissionNewAPI.ebiCurl(submission, contextValidation);
			}
			@Override public void success(ContextValidation contextValidation, Submission submission, State nextState) {				
				submissionNewAPITools.updateSubmissionChildObject(submission);
			}
		});	
		
		trs.put(SUBU_SMD_F   , SUB_F, new TransitionManualState() {	
			@Override public void execute(ContextValidation contextValidation, Submission submission, State nextState) {
				submissionNewAPI.loadRespEbiForUpdate(contextValidation, submission);
			}
			@Override public void success(ContextValidation contextValidation, Submission submission, State nextState) {
				submissionNewAPITools.updateSubmissionChildObject(submission);
			}
		});	

		trs.put(SUBU_SMD_IP   , SUBU_SMD_FE, new TransitionAutomaticState() {	
			@Override public void success(ContextValidation contextValidation, Submission submission, State nextState) {
				submissionNewAPITools.updateSubmissionChildObject(submission);
			}
		});	

		trs.put(SUBU_SMD_F   , SUBU_FE, new TransitionManualState() {	
			@Override public void execute(ContextValidation contextValidation, Submission submission, State nextState) {
				submissionNewAPI.loadRespEbiForUpdate(contextValidation, submission);
			}
		});	
		
		// Autoriser les transitions reprise sur erreur :
		trs.put(SUBU_SMD_FE   , SUB_F, new TransitionManualState() {	
			@Override public void execute(ContextValidation contextValidation, Submission submission, State nextState) {
				logger.debug("ZZZZ  EEE   entree de transition SUBU_SMD_FE vers SUB_F, submission=" + submission.code + ", stateCode="+submission.state.code);
				logger.debug("ZZZZ  EEE   dans transition SUBU_SMD_FE vers SUB_F, appel de debugUpdate()");
				debugUpdate(contextValidation, submission);
			}
			@Override public void success(ContextValidation contextValidation, Submission submission, State nextState) {
				submissionNewAPITools.updateSubmissionChildObject(submission);
				logger.debug("ZZZZ  EEE   sortie de transition SUBU_SMD_FE vers SUB_F, submission=" + submission.code + ", stateCode="+submission.state.code);
			}
		});	
		
		trs.put(SUBU_FE   , SUB_F, new TransitionManualState() {	
			// la transition verifie si etat initial de la soumission est bien SUBU_FE et met la soumission à SUBR_SMD_IP et si ok, alors process success.
			@Override public void execute(ContextValidation contextValidation, Submission submission, State nextState) {
				logger.debug("ZZZZ  EEE   entree de transition SUBU_FE vers SUB_F, submission=" + submission.code + ", stateCode="+submission.state.code);
				logger.debug("ZZZZ  EEE   dans transition SUBU_FE vers SUB_F, appel de debugUpdate()");
				debugUpdate(contextValidation, submission);
			}
			@Override public void success(ContextValidation contextValidation, Submission submission, State nextState) {
				submissionNewAPITools.updateSubmissionChildObject(submission);
				logger.debug("ZZZZ  EEE   sortie de transition SUBU_FE vers SUB_F, submission=" + submission.code + ", stateCode="+submission.state.code);
			}
		});	
		
		trs.put(SUBU_FE   , SUBU_FE, new TransitionAutomaticState() {	
			// la transition verifie si etat initial de la soumission est bien SUBR_FE et met la soumission à SUBR_SMD_IP et si ok, alors process success.
			@Override public void success(ContextValidation contextValidation, Submission submission, State nextState) {
				submissionNewAPITools.updateSubmissionChildObject(submission);
				logger.debug("ZZZZ  EEE   sortie de transition SUBU_FE vers SUBU_FE, submission=" + submission.code + ", stateCode="+submission.state.code);
			}
		});	
		
		// appelé lorsqu'on lance 2 fois transition  SUBU_FE  vers SUB_F ( support pour debuguer une soumission ) si la deuxième fois, l'EBI refuse toujours notre soumission 
		trs.put(SUBU_SMD_FE   , SUBU_SMD_FE, new TransitionAutomaticState() {	
			@Override public void success(ContextValidation contextValidation, Submission submission, State nextState) {
				submissionNewAPITools.updateSubmissionChildObject(submission);
				logger.debug("ZZZZ  EEE   sortie de transition SUBU_SMD_FE vers SUBU_SMD_FE, submission=" + submission.code + ", stateCode="+submission.state.code);
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

	
}
