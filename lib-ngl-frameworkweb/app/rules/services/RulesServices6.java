package rules.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.kie.api.KieBase;
import org.kie.api.KieBaseConfiguration;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

import play.Application;

public class RulesServices6 {
	
	/**
	 * Logger.
	 */
	private static final play.Logger.ALogger logger = play.Logger.of(RulesServices6.class);

	/**
	 * Cache of instances indexed by knowledge base names.
	 */
	private static final Map<String,RulesServices6> instances = new HashMap<>();
	
	/**
	 * Singleton ref.
	 */
	private static class SingletonHolder {		
		// private final static RulesServices6 instance = new RulesServices6();
		// @Inject
		private static RulesServices6 instance;
	}

	private KieBase kbase;
	
	private String kbasename;
	// private static final String kbasename = Play.application().configuration().getString("rules.kbasename");
	
	// @Inject
	private RulesServices6(Application app) {
//		kbasename = app.configuration().getString("rules.kbasename");
		kbasename = app.config().getString("rules.kbasename");
		buildKnowledgeBase(app);
	}
	
 
	public static void initSingleton(Application app) {
		logger.debug("initializing singleton");
//		String kbasename = app.configuration().getString("rules.kbasename");
		String kbasename = app.config().getString("rules.kbasename");
		logger.info("using knowledge base name {}", kbasename);
		SingletonHolder.instance = instances.get(kbasename);
		if (SingletonHolder.instance == null) {
			SingletonHolder.instance = new RulesServices6(app);
			logger.debug("create instance for {} : {}", kbasename, SingletonHolder.instance);
			instances.put(kbasename, SingletonHolder.instance);
		} else {
			logger.debug("reusing instance for {} : {}", kbasename, SingletonHolder.instance);
		}
		logger.debug("singleton initialized");
	}
	
	/* Point d'accès pour l'instance unique du singleton */
	public static RulesServices6 getInstance() {
		if (SingletonHolder.instance == null)
			throw new RuntimeException("RulesServices6 not intiailized, call initSingleton");
		return SingletonHolder.instance;
	}
	
	private void buildKnowledgeBase(Application app) {
		if (kbase == null && StringUtils.isNotBlank(kbasename)) {
			logger.info("Load Drools Rules for KBaseName = "+ kbasename);
			KieServices kieServices = KieServices.Factory.get();
			KieContainer kContainer = kieServices.newKieClasspathContainer(/*play.Play.application()*/app.classloader());
		    KieBaseConfiguration kbaseConf = kieServices.newKieBaseConfiguration();
		    kbase = kContainer.newKieBase(kbasename, kbaseConf); 		    
		} else if (StringUtils.isBlank(kbasename)) {
			logger.warn("Load Drools Rules : rules.kbasename is empty");
		}
	}

	private KieBase getKieBase() {
		if (kbase == null)
			throw new RuntimeException("KieBase instance should have been created at RulesServices6 creation");
		return kbase;
	}
	
	/**
	 * Synchronous firing of the rules matching the rule name and the annotation name. 
	 * @param keyRules           key rules
	 * @param ruleAnnotationName rule name
	 * @param factsToInsert      facts
	 */
	public void callRules(String keyRules, String ruleAnnotationName, List<Object> factsToInsert) {
		KieSession kSession = getKieBase().newKieSession();
		for (Object fact : factsToInsert) 
			kSession.insert(fact);
		kSession.fireAllRules(RulesAgendaFilter6.getInstance(keyRules, ruleAnnotationName));
		kSession.dispose();		
	}
	
	/**
	 * Synchronous firing of the rules matching the rule name and the annotation name
	 * and return the drools facts. 
	 * @param keyRules           key rules
	 * @param ruleAnnotationName rule name
	 * @param factsToInsert      facts
	 * @return                   drools returned facts
	 */	
	public List<Object> callRulesWithGettingFacts(String keyRules, String ruleAnnotationName, List<Object> factsToInsert) {
		KieSession kSession = getKieBase().newKieSession();
		for (Object fact : factsToInsert)
			kSession.insert(fact);
		kSession.fireAllRules(RulesAgendaFilter6.getInstance(keyRules, ruleAnnotationName));
		List<Object> factsAfterRules = new ArrayList<>(kSession.getObjects());
		kSession.dispose();
		return factsAfterRules;
	}
	
}
