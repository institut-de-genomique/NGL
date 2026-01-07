package fr.cea.ig.ngl.dao.api.factory;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import fr.cea.ig.ngl.utils.TestUtils;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TransientState;

/**
 * Factory pour l'entité "State".
 * 
 * @author jcharpen - Jordi Charpentier - jcharpen@genoscope.cns.fr
 */
public final class StateFactory {
	
	/**
	 * Méthode permettant de générer un objet "State" aléatoire.
	 * 
	 * @return Un objet "State" aléatoire.
	 */
	public static State getRandomState() {
		State state = new State();

		state.date = new Date();
		state.code = "IW-V";
		state.user = TestUtils.CURRENT_USER;

		TransientState ts = new TransientState();
		ts.code = "IW-V";
		ts.date = new Date();
		ts.user = TestUtils.CURRENT_USER;

		Set<TransientState> hist = new HashSet<>();
		hist.add(ts);

		state.historical = hist;

		return state;
	}

	/**
	 * Méthode permettant de générer un objet "State" aléatoire.
	 * Ici on fait manquer un utilisateur dans le modèle pour ne plus passer la validation.
	 * 
	 * @return Un objet "State" aléatoire.
	 */
	public static State getRandomInvalidState() {
		State state = new State();
		
		state.date = new Date();

		TransientState ts = new TransientState();
		ts.code = "N";
		ts.date = new Date();
		ts.user = TestUtils.CURRENT_USER;

		Set<TransientState> hist = new HashSet<>();
		hist.add(getTransientState("N"));

		state.historical = hist;

		return state;
	}

	/**
	 * getTransientState()
	 * 
	 * @param state
	 * 
	 * @return
	 */
	private static TransientState getTransientState(String state) {
		TransientState ts = new TransientState();
		ts.code = state;
		ts.date = new Date();
		ts.user = TestUtils.CURRENT_USER;

		return ts;		
	}

	/**
	 * Méthode permettant de générer un objet state à l'état "F" (terminé).
	 * 
	 * @return Un objet "State" à l'état "F" (terminé).
	 */
	public static State getFinishState() {
		State state = new State();
		state.code = "F";
		state.date = new Date();

		Set<TransientState> hist = new HashSet<>();
		hist.add(getTransientState("F"));

		state.historical = hist;

		return state;
	}

	/**
	 * Méthode permettant de générer un objet state à l'état "IP" (en cours).
	 * 
	 * @return Un objet "State" à l'état "IP" (en cours).
	 */
	public static State getInProgressState() {
		State state = new State();
		state.code = "IP";
		state.date = new Date();

		Set<TransientState> hist = new HashSet<>();
		hist.add(getTransientState("IP"));

		state.historical = hist;

		return state;
	}

	/**
	 * Méthode permettant de générer un objet state à l'état "IU" (in use).
	 * 
	 * @return Un objet "State" à l'état "IU" (in use).
	 */
	public static State getInUseState() {
		State state = new State();
		state.code = "IU";
		state.date = new Date();

		Set<TransientState> hist = new HashSet<>();
		hist.add(getTransientState("IU"));

		state.historical = hist;

		return state;
	}

	/**
	 * Méthode permettant de générer un objet state à l'état "N" (nouveau).
	 * 
	 * @return Un objet "State" à l'état "N" (nouveau).
	 */
	public static State getNewState() {
		State state = new State();
		state.code = "N";
		state.date = new Date();

		Set<TransientState> hist = new HashSet<>();
		hist.add(getTransientState("N"));

		state.historical = hist;

		return state;
	}

	/**
	 * Méthode permettant de générer un objet state à l'état "IW-C" (in waiting container).
	 * 
	 * @return Un objet "State" à l'état "IW-C" (in waiting container).
	 */
	public static State getInWaitingContainerState() {
		State state = new State();
		state.code = "IW-C";
		state.date = new Date();

		Set<TransientState> hist = new HashSet<>();
		hist.add(getTransientState("IW-C"));

		state.historical = hist;

		return state;
	}

	/**
	 * Méthode permettant de générer un objet state à l'état "IW-P" (in waiting process).
	 * 
	 * @return Un objet "State" à l'état "IW-P" (in waiting process).
	 */
	public static State getInWaitingProcessState() {
		State state = new State();
		state.code = "IW-P";
		state.date = new Date();

		Set<TransientState> hist = new HashSet<>();
		hist.add(getTransientState("IW-P"));

		state.historical = hist;

		return state;
	}

	/**
	 * Méthode permettant de générer un objet state à l'état "IS" (in stock).
	 * 
	 * @return Un objet "State" à l'état "IW-P" (in stock).
	 */
	public static State getInStockState() {
		State state = new State();
		state.code = "IS";
		state.date = new Date();

		Set<TransientState> hist = new HashSet<>();
		hist.add(getTransientState("IS"));

		state.historical = hist;

		return state;
	}

	/**
	 * Méthode permettant de générer un objet state à l'état "UA" (unavailable).
	 * 
	 * @return Un objet "State" à l'état "UA" (unavailable).
	 */
	public static State getUnavailableState() {
		State state = new State();
		state.code = "UA";
		state.date = new Date();

		Set<TransientState> hist = new HashSet<>();
		hist.add(getTransientState("UA"));

		state.historical = hist;

		return state;
	}

	/**
	 * Méthode permettant de générer un objet state à l'état "A" (available).
	 * 
	 * @return Un objet "State" à l'état "A" (available).
	 */
	public static State getAvailableState() {
		State state = new State();
		state.code = "A";
		state.date = new Date();

		Set<TransientState> hist = new HashSet<>();
		hist.add(getTransientState("A"));

		state.historical = hist;

		return state;
	}

	/**
	 * Méthode permettant de générer un objet state à l'état "IW-P" (in waiting experiment).
	 * 
	 * @return Un objet "State" à l'état "IW-P" (in waiting experiment).
	 */
	public static State getInWaitingExperimentState() {
		State state = new State();
		state.code = "IW-E";
		state.date = new Date();

		Set<TransientState> hist = new HashSet<>();
		hist.add(getTransientState("IW-E"));

		state.historical = hist;

		return state;
	}

	/**
	 * Méthode permettant de générer un objet state à l'état "IW-D" (in waiting dispatch).
	 * 
	 * @return Un objet "State" à l'état "IW-D" (in waiting dispatch).
	 */
	public static State getInWaitingDispatchState() {
		State state = new State();
		state.code = "IW-D";
		state.date = new Date();

		Set<TransientState> hist = new HashSet<>();
		hist.add(getTransientState("IW-D"));

		state.historical = hist;

		return state;
	}
}
