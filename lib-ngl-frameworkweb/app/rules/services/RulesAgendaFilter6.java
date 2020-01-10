package rules.services;

import java.util.HashMap;
import java.util.Map;

import org.kie.api.runtime.rule.AgendaFilter;
import org.kie.api.runtime.rule.Match;

public class RulesAgendaFilter6 implements AgendaFilter {

	/**
	 * Cached instances of agenda filters.
	 */
	// Agenda filter instances are small and their creation infrequent so caching the
	// instances seems pointless.
	private static Map<String, RulesAgendaFilter6> rulesAgendaFilter = new HashMap<>();
	
	private String metadataKey;
	private String metadataValue;

	// Odd implementation where the meta data key does not influence the indexing like
	// there is only one possible meta data key.
	public static RulesAgendaFilter6 getInstance(String metadataKey, String metadataValue) {
		if (!rulesAgendaFilter.containsKey(metadataValue)) 
			rulesAgendaFilter.put(metadataValue, new RulesAgendaFilter6(metadataKey, metadataValue));
		return rulesAgendaFilter.get(metadataValue);
	}
	
	private RulesAgendaFilter6(String metadataKey, String metadataValue) {
		this.metadataKey   = metadataKey;
		this.metadataValue = metadataValue;
	}

	@Override
	public boolean accept(Match match) {
		String s = (String) match.getRule().getMetaData().get(metadataKey);
		return s!= null && s.equals(metadataValue);
	}

}
