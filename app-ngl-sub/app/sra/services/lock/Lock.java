package sra.services.lock;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.bson.types.ObjectId;

// Mongojack definition of the lock.
/**
 * Definition d'un objet verrou stoqué dans la collection {@link LockAPI#LockCollectionName}.
 * <p>
 * Le verrou definit 2 champs :
 * <dl>
 *   <dt>{@link #lastRefreshDate}</dt>
 *   <dd>date de dernier rafraichissement du verrou</dd>
  <dt>{@link #serverDate}</dt>
 *   <dd>date coté serveur Mongo mise à jour lors de la vérification de la validité du verrou</dd>
 * </dl>
 * 
 * @author sgas
 *
 */
class Lock extends RawMongoObject {
	
	/**
	 * Date de dernier rafraichissement du verrou.
	 */
	public Date lastRefreshDate;
	
	/**
	 * Date coté serveur Mongo mise à jour lors de la vérification de la validité du verrou.
	 */
	public Date serverDate;
	
	/**
	 * Constructeur par defaut requis pour la deserialisation.
	 */
	public Lock() {}
	
	/**
	 * Constructeur utilisé par le code.
	 *
	 * @param id identifiant Mongo.
	 */
	public Lock(ObjectId id) {
		super(id);
	}
	
}