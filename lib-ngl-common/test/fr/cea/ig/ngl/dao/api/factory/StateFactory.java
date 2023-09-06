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
		ts.code = "N";
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
		hist.add(ts);

		state.historical = hist;

		return state;
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

		TransientState ts = new TransientState();
		ts.code = "F";
		ts.date = new Date();
		ts.user = TestUtils.CURRENT_USER;

		Set<TransientState> hist = new HashSet<>();
		hist.add(ts);

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

		TransientState ts = new TransientState();
		ts.code = "IP";
		ts.date = new Date();
		ts.user = TestUtils.CURRENT_USER;

		Set<TransientState> hist = new HashSet<>();
		hist.add(ts);

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

		TransientState ts = new TransientState();
		ts.code = "N";
		ts.date = new Date();
		ts.user = TestUtils.CURRENT_USER;

		Set<TransientState> hist = new HashSet<>();
		hist.add(ts);

		state.historical = hist;

		return state;
	}
}
