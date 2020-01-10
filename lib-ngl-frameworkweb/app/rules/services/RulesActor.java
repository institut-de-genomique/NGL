package rules.services;

import akka.actor.AbstractActor;

public class RulesActor extends AbstractActor {

	@Override
	public Receive createReceive() {
		return receiveBuilder()
			   .match(RulesMessage.class, m -> {
					RulesServices rulesServices = new RulesServices();
					rulesServices.callRules(m.getKeyRules(), m.getNameRule(), m.getFacts());
				})
			   .build();
	}

}

//import akka.actor.UntypedActor;
//
//public class RulesActor extends UntypedActor {
//
//	@Override
//	public void onReceive(Object message) throws Exception {
//		
//		// Receive RulesMessage with facts to call rules
//		RulesMessage ruleMessage = (RulesMessage)message;
//		
//		RulesServices rulesServices = new RulesServices();
//		rulesServices.callRules(ruleMessage.getKeyRules(),ruleMessage.getNameRule(), ruleMessage.getFacts());
//		
//	}
//
//}
