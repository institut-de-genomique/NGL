package workflows.sra.submission;

import fr.cea.ig.DBObject;
import models.laboratory.common.instance.State;
import models.sra.submit.sra.instance.IStateReference;
import validation.ContextValidation;

/**
 * Transition of a state machine ({@link workflows.Workflows}).
 * <ul>
 *   <li>{@link #execute(ContextValidation, DBObject, State)} is the transition
 *       code to be executed for the transition to a given state. Execution failures
 *       are reported using the validation context.</li>
 *   <li>{@link #success(ContextValidation, DBObject, State)} is executed if the 
 *       {@link #execute(ContextValidation, DBObject, State)} was successful (like a commit).</li>
 *   <li>{@link #error(ContextValidation, DBObject, State)} is executed if the 
 *       {@link #execute(ContextValidation, DBObject, State)} reported an error (like a roll back).
 *       This is where attempts at error correction are made.</li>
 * </ul>
 * 
 * @author sgas
 *
 * @param <T> state machine state type
 */
public interface Transition <T extends DBObject & IStateReference> {
	
	/**
	 * Try to execute the transition that leads to the argument state.
	 * @param contextValidation validation context
	 * @param object            current state machine state
	 * @param nextState         state to try to transition to
	 */
	public void execute(ContextValidation contextValidation, T object, State nextState);
	
	/**
	 * Commit the transition to the next state. 
	 * Code executé pour finaliser la transition deja effectuée 
	 * du point de vu de NGL (transition sauvée dans base).
	 * @param contextValidation validation context
	 * @param object            current state machine state
	 * @param nextState         state to transition to
	 */
	public void success(ContextValidation contextValidation, T object, State nextState);

	/**
	 * Roll back the transition.
	 * @param contextValidation validation context
	 * @param object            current state machine state
	 * @param nextState         state to transition to
	 */
	public void error(ContextValidation contextValidation, T object, State nextState);

	//SGAS	trouver bon nom et commentaire :
	public boolean isAutonomous();
	
}
