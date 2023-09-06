package fr.cea.ig.junit.drools;

import org.kie.api.runtime.KieSession;

/**
 * Interface permettant de définir le comportement d'une session Drools.
 * 
 * @author jcharpen - Jordi CHARPENTIER - jcharpen@genoscope.cns.fr
 */
public interface DroolsSession {

    /**
     * Méthode permettant de déclencher toutes les règles de la session Drools.
     */
    void fireAllRules();

    /**
     * Méthode permettant d'insérer un objet (un fait) dans la session Drools.
     * On l'utilise par exemple pour ajouter une expérience, un sample, un container, ...
     * 
     * @param object L'objet à insérer dans la session Drools.
     */
    void insert(Object object);

    /**
     * Méthode permettant de récupérer la session Drools qu'on créé.
     * 
     * @return Un objet KieSession qui représente la session Drools qu'on créé.
     */
    KieSession getStatefulSession();
}
