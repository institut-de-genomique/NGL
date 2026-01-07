package sra.services.lock;

import org.bson.types.ObjectId;

/**
 * Verrou mongo geré avec un 
 * <a href="https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html"> try-with-ressources</a>.
 * Le verrou sera libéré après la sortie du try.
 * <p>
 * Le verrou est maintenu activement grace à un Thread qui rafraichit le verrou 
 * toutes les {@link LockAPI#LOCK_REFRESH} millisecondes
 */
public class ActiveLock implements AutoCloseable {

	private final LockAPI api;
	private final ObjectId lockId;
	private final Thread refresher;
	
	ActiveLock(LockAPI api, ObjectId lockId) {
		this.api = api;
		this.lockId = lockId;
		this.refresher = new Thread ( () -> {
			while(true) {
				api.refreshLock(lockId);
				try {
					Thread.sleep(LockAPI.LOCK_REFRESH);
				} catch (InterruptedException e) {
					return;
				}
			}
		} );
	}
	
	@Override
	public void close() throws Exception {
		api.getMylocks().remove(this);
		refresher.interrupt();
		api.releaseLock(lockId);
	}

	public void startRefresher() {
		refresher.start();
	}

}
