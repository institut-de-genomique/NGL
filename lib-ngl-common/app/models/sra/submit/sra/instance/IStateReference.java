package models.sra.submit.sra.instance;

import com.fasterxml.jackson.annotation.JsonIgnore;

import models.laboratory.common.instance.ITracingAccess;
import models.laboratory.common.instance.State;

/**
 * State container (object contenant {@link State}).
 * 
 * @author sgas
 *
 */
public interface IStateReference extends ITracingAccess  {
	
	/**
	 * Get object state.
	 * @return object state
	 */
    State getState(); // inutile de mettre public abstract car on est dans interface
    
    /**
     * Set object state.
     * @param state new state
     */
    void setState(State state);

    /**
	 * Met l'objet à jour pour son etat et son historique.
	 * @param nextStateCode  code du prochain etat
	 * @param user           utilisateur faisant la mise à jour de l'etat
	 */   
    @JsonIgnore  // defensif
    default void setStateHistorique(String nextStateCode, String user) {
    	if(! this.getState().code.equals(nextStateCode)) {
    		setStateHistorique(this, nextStateCode, user);
    	}
    }
    
    /**
	 * Met à jour l'etat et son historique pour l'objet passé en argument.
	 * N'affecte pas les arguments.
	 * @param object         implementant l'interface {@link IStateReference }
	 * @param nextStateCode  code du prochain etat
	 * @param user           utilisateur faisant la mise à jour de l'etat
	 */
	public static void setStateHistorique(IStateReference object, String nextStateCode, String user) {
//		State ns = new State(nextStateCode, user);
//		ns.createHistory(sc.getState());
		State ns = createStateHistorique(object.getState(), nextStateCode, user);
		object.setState(ns);
	}	
	
	public static State createStateHistorique(State current, String nextStateCode, String user) {
		State ns = new State(nextStateCode, user);
		ns.createHistory(current);
		return ns;
	}

	/**
	 * Met à jour l'objet (implementant les interfaces) pour la trace information, 
	 * l'etat et son historique.
	 * @param nextState  objet {@link State} à installer dans le champs state
	 */
	default void updateStateAndTrace(State nextState) {
		setTraceUpdateStamp(nextState.user);
		setStateHistorique(nextState.code, nextState.user);		
	}
	
	
	
}
