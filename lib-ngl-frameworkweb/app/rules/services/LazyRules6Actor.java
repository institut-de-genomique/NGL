package rules.services;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import fr.cea.ig.lfw.LFWConfig;
import fr.cea.ig.lfw.utils.LazyLambdaSupplier;

@Singleton
class LazyRulesKey extends LazyLambdaSupplier<String> {
	
	@Inject
	public LazyRulesKey(LFWConfig config) {
		super(() -> config.getRulesKey());
	}
	
}

/**
 * Asynchronous (actor system) based drools rules execution.
 * 
 * @author vrd
 *
 */
@Singleton
public class LazyRules6Actor extends LazyLambdaSupplier<ActorRef> implements IDrools6Actor {
	
	private LazyRulesKey rulesKey;
	
	@Inject
	public LazyRules6Actor(ActorSystem actorSystem, LazyRulesKey rulesKey) {
		super(() -> actorSystem.actorOf(Props.create(RulesActor6.class)));
		this.rulesKey = rulesKey;
	}
	
	@Override
	public void tellMessage(String rulesCode, List<Object> objects) {
		get().tell(new RulesMessage(rulesKey.get(), rulesCode, objects), null);
	}
	
//	@Override
//	public void tellMessage(String rulesCode, Object... objects) {
//		get().tell(new RulesMessage(rulesKey.get(), rulesCode, objects), null);
//	}
	
}



