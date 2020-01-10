package rules.services;

import java.util.Arrays;
import java.util.List;

/**
 * Messages can be any kind of object but have to be immutable. 
 * Akka can’t enforce immutability (yet) so this has to be by convention.
 * 
 * @author ejacoby
 *
 */
public class RulesMessage {

	private final String       keyRules;
	private final String       nameRule;
	private final List<Object> facts;
	
	public RulesMessage(String keyRules, String nameRule, List<Object> facts) {
		this.keyRules = keyRules;
		this.nameRule = nameRule;
		this.facts    = facts;
	}
	
	public RulesMessage(String keyRules, String nameRule, Object... facts) {
		this(keyRules, nameRule, Arrays.asList(facts));
	}

	public String getKeyRules() {
		return keyRules;
	}

	public String getNameRule() {
		return nameRule;
	}
	
	public List<Object> getFacts() {
		return facts;
	}

}
