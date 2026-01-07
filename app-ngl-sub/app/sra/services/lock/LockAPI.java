package sra.services.lock;

import static fr.cea.ig.lfw.utils.Iterables.zen;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.mongojack.DBQuery;
import org.mongojack.DBSort;
import org.mongojack.DBUpdate;

import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import akka.Done;
import akka.actor.CoordinatedShutdown;
import fr.cea.ig.mongo.MongoConfig;
import scala.concurrent.duration.Duration;


/**
 * API de gestion de verrou.
 * <p>
 * On utilise une collection mongo ({@link #UniquenessCollectionName}) pour generer
 * des identifiants uniques pour les noms des verrous. Ces identifiants uniques permettent
 * d'utiliser une opération atomique dans mongo qui est la création d'un document avec un seul champs 
 * qui est un identifiant explicite. Si deux processus tentent de créer un document 
 * avec le meme identifiant, seul l'un des deux va réussir et obtenir le verrou. 
 * Les verrous sont crées dans la collection ({@link #LockCollectionName}) avec l'identifiant 
 * trouvé dans la collection {@link #UniquenessCollectionName}. 
 * <p>
 * On aurait pu definir les identifiants uniques dans la classe LockAPI pour implémenter 
 * la méthode {@link #getId(String)}. Si on fait cela, il faut connaitre à prioris tous 
 * les noms de verrous possibles.
 * La création d'un document dans {@link #LockCollectionName} n'assure pas l'unicite du document.
 * La solution retenue est d'utiliser une collection {@link #UniquenessCollectionName}. 
 * <p>
 * <b>L'opération atomique au niveau mongo est la création d'un document avec un seul champs correspondant
 * à un identifiant particulier (déjà defini).</b>
 * <p>
 * La collection {@link #UniquenessCollectionName} contient les objets de type {@link NamedObject}.
 * <p>
 * La collection  {@link #LockCollectionName}  contient les objets de type {@link Lock}.
 * 
 * @author sgas
 *
 */
@Singleton
public class LockAPI  {
	
	private static final play.Logger.ALogger logger = play.Logger.of(LockAPI.class);

	
	/**
	 * Nombre de millisecondes après lequel le verrou est invalide 
	 */
	public static final long LOCK_TIMEOUT = 120_000;
	
	/**
	 * Nombre de millisecondes entre 2 rafraichissements du verrou (pour le garder valide).
	 */
	public static final long LOCK_REFRESH = LOCK_TIMEOUT - 10_000;
	
	public static final String UniquenessCollectionName = "ngl_sub.uniqueObjectId";
	public static final String LockCollectionName       = "ngl_sub.lock";
	// Lock data type "definition"
	// Definition des noms des champs de la classe Lock pour utilisation dans requete Mongo (de bas niveau.)
	/**
	 * Nom du champs {@link Lock#lastRefreshDate} pour utilisation dans requete Mongo.
	 * Permet de construire une requete de type (mise à jour de la date de validité du verrou): 
	 * <p>
	 * <code> 
	 * requete = lastRefreshKey + " = $currentDate" 
	 * </code>
	 */
	private static final String lastRefreshKey = "lastRefreshDate"; // $currentDate needs the field name.
	
	/**
	 * Nom du champs {@link Lock#serverDateKey} pour utilisation dans requete Mongo.
	 * Permet de construire une requete de type (mise à jour de la date de vérification du verrou): 
	 * <p>
	 * <code> 
	 * requete = serverDateKey + " = $currentDate" 
	 * </code>
	 */
	private static final String serverDateKey = "serverDate";
	
	private RawMongoJack<NamedObject> namedObjects;
	private RawMongoJack<Lock>        locks;
	private boolean                   isAllowLocking; // autorisation de poser un lock
	private List<ActiveLock>              mylocks;        // liste des locks posés sur l'instance de NGL-SUB
	
	@Inject
	//public LockAPI(MongoConfig mc, CoordinatedShutdown cs) {
	public LockAPI(MongoConfig mc) {
		namedObjects   = new RawMongoJack<>(mc, UniquenessCollectionName, NamedObject.class);
		locks          = new RawMongoJack<>(mc, LockCollectionName,       Lock       .class);
		setAllowLocking(true);
		setMylocks(new ArrayList<>());
		
		// trouver l'ecriture d'un future scala en java
		// on indique au coordinateur de shutDown qu'on ajoute une étape d'attente que les lock soient tous terminés 
		//cs.addTask(
		//        CoordinatedShutdown.PhaseServiceUnbind(), "free-some-resource", () -> {// disableLock puis boucle pour attente liste lock videes.});
		
	}
	
	public int lockCount() {
		return getMylocks().size();
	}
	
	public List<NamedObject> getNamedObjectsByName(String name) {
		return zen(namedObjects.find(DBQuery.is("name", name)).sort(DBSort.asc("_id"))).toList();
	}
	
	public ActiveLock aquireLock(String lockKey) throws LockException {
		if( ! getAllowLocking() ) {
			throw new LockException("Impossible d'aquerir le lock car opération interdite");
		}
		ObjectId lockId = getId(lockKey);
		if(! getLock(lockId)) {
			//throw new RuntimeException("Impossible d'acquerir le lock");
			throw new LockException("Impossible d'acquerir le lock parce que deja pris");
		}
		ActiveLock myLock = new ActiveLock(this, lockId);
		getMylocks().add(myLock);
		myLock.startRefresher();		
		return myLock;
	}

	public ActiveLock aquireLock(Class<?> c) throws LockException {
		return aquireLock(c.getName());
	}

	// comment recuperer ou creer un identifiant Mongo pour une cle 
	public ObjectId getId(String key) {
		logger.debug("getting unique id for '{}'", key);
		// select key(s) from unique id table ordered by id.	
		List<NamedObject> nos = getNamedObjectsByName(key);
		if(! nos.isEmpty()) {
			ObjectId id = nos.get(0).getId();
			logger.debug("unique id for '{}' : {}", key, id );
			return id;
		}
		logger.debug("creating entry for '{}'", key);
		namedObjects.upsert(DBQuery.is("name", key), new NamedObject(key));
		nos = getNamedObjectsByName(key);
		if (nos.isEmpty()) {
			throw new RuntimeException("read after insert failed (no result)");
		}
		ObjectId id = nos.get(0).getId();
		logger.debug("unique id for '{}' : {}", key, id );
		return id;
	}
	
	
	public void garbageCollectLock(ObjectId id) {
		Lock l = locks.findOneById(id);
		
		if (l != null) {
			// on a trouve verrou et on veut savoir s'il est ou non perimé.
			// s'il est périmé, alors on peut le recuperer.
			// On veut trouver date coté Mongo. On verrifie si date de rafraichissement + date de lock_timeout est plus petite que date mongo
			// on ne sait pas comment recuperer date coté serveur. 
			// On effectue une operation(bas niveau = raw) : met à jour le serveurDateKey (champ serverDate dans un Lock)
			locks.updateById(id, new DBUpdate.Builder().addRawOperation("$currentDate", serverDateKey, true));
			l = locks.findOneById(id);
			if (l != null) {
				Date rd = l.lastRefreshDate;
				Date sd = l.serverDate;
				
				// The refresh date may be null if we are looking at the lock during
				// an acquisition phase.
				if (rd != null) {
					if(sd.getTime() > rd.getTime() + LOCK_TIMEOUT ) {
						//locks.removeById(id);	
						releaseLock(id);
					}
				} else {
					logger.warn("no refresh date set for lock {}", id);
				}
			}
		}
	}

	
	
	// implementation atomique de verouillage en base 
	public boolean getLock(ObjectId id) {
		logger.debug("trying to acquire lock for {}", id);
		if( ! getAllowLocking() ) {
			return false;
		}
		// Garbage collect lock before trying to acquire lock.
		
		garbageCollectLock(id);
		try {
			logger.debug("trying to acquire physical lock {}", id);
			locks.insert(new Lock(id));
			logger.debug("acquired lock {}", id);
			//locks.updateById(id, new DBUpdate.Builder().addRawOperation("$currentDate", lastRefreshKey, true));
			refreshLock(id);
			logger.debug("updated refresh date");
			return true;
		} catch (MongoException e) {
			logger.debug("physical lock acquisition failed {}", e.getMessage());
			return false;
		}	
	}
	
	public void releaseLock(ObjectId id) {
		locks.removeById(id);
	}
	
	// This does not check if we own the lock so this may refresh a lock
	// that has been taken by someone else. Query should include 
	// an owning check.
	public void refreshLock(ObjectId id) {
		locks.updateById(id, new DBUpdate.Builder().addRawOperation("$currentDate", lastRefreshKey, true));
	}
//
//	public boolean executeWithLock(Class<?> c, Runnable r) {
//		return executeWithLock(c.getName(), r);
//	}
//	

	public boolean getAllowLocking() {
		return isAllowLocking;
	}

	public void setAllowLocking(boolean isAllowLocking) {
		this.isAllowLocking = isAllowLocking;
	}

	public List<ActiveLock> getMylocks() {
		return mylocks;
	}

	public void setMylocks(List<ActiveLock> mylocks) {
		this.mylocks = mylocks;
	}
	
//	public boolean executeWithLock(String lockKey, Runnable r) {
//		ObjectId lockId = getId(lockKey);
//		if(getLock(lockId)) {
//			Thread refresher = new Thread ( () -> {
//				while(true) {
//					refreshLock(lockId);
//					try {
//						Thread.sleep(LOCK_REFRESH);
//					} catch (InterruptedException e) {
//						return;
//					}
//				}
//			} );
//			refresher.start();
//			try {
//				r.run();
//				return true;
//			} finally {
//				refresher.interrupt();
//				releaseLock(lockId);
//			}
//		}
//		return false;
//	}
}

