package fr.cea.ig.junit.drools;

import org.kie.api.runtime.KieSession;

/**
 * Implémentation de la session Drools "simplifiée".
 * Cette classe hérite de l'interface "DroolsSession".
 * 
 * @author jcharpen - Jordi CHARPENTIER - jcharpen@genoscope.cns.fr
 */
public class DroolsSessionImpl implements DroolsSession {

    /**
     * Session Drools qu'on va créér.
     */
    private KieSession statefulSession;

    /**
     * Constructeur de la classe.
     * 
     * @param statefulSession La session qu'on veut créer.
     */
    public DroolsSessionImpl(KieSession statefulSession) {
        this.statefulSession = statefulSession;
    }

    @Override
    public void fireAllRules() {
        this.statefulSession.fireAllRules();
    }

    @Override
    public void insert(Object object) {
        this.statefulSession.insert(object);
    }

    @Override
    public KieSession getStatefulSession() {
        return statefulSession;
    }
}
