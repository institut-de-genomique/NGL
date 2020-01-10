package rules.services;

import akka.actor.AbstractActor;

public class RulesActor6 extends AbstractActor {

	@Override
	public Receive createReceive() {
		return receiveBuilder()
			   .match(RulesMessage.class, m -> RulesServices6.getInstance().callRules(m.getKeyRules(), m.getNameRule(), m.getFacts()))
			   .build();
	}

}
