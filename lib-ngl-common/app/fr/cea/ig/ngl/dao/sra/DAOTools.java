package fr.cea.ig.ngl.dao.sra;

import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import fr.cea.ig.DBObject;
import fr.cea.ig.ngl.dao.GenericMongoDAO;
import models.laboratory.common.instance.ITracingAccess;
import models.laboratory.common.instance.State;
import models.sra.submit.sra.instance.IStateReference;

public class DAOTools {

	/**
	 * Met à jour l'objet (implementant les interfaces) pour la trace information, 
	 * l'etat et son historique.
	 * @param t          objet à mettre à jour  pour les champs state et traceInformation
	 * @param nextState  objet {@link State} à installer dans le champs state
	 */
	static <T extends IStateReference & ITracingAccess> void updateStateAndTrace(T t, State nextState) {
		t.setTraceUpdateStamp(nextState.user);
		t.setStateHistorique(nextState.code, nextState.user);		
	}

	/**
	 * Met à jour l'objet (implementant les interfaces) pour la trace information, 
	 * l'etat et son historique.
	 * @param t          objet à mettre à jour  pour les champs state et traceInformation
	 * @param nextState  objet {@link State} à installer dans le champs state
	 */
	static <T extends DBObject & IStateReference & ITracingAccess> void saveStateAndTrace(GenericMongoDAO<T> d, T t) {
		d.update(DBQuery.is("code", t.code), 
				 DBUpdate.set("state", t.getState())
						 .set("traceInformation", t.getTraceInformation()));
	}

}
