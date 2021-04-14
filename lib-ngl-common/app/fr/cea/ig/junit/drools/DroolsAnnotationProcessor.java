package fr.cea.ig.junit.drools;

import java.lang.reflect.Field;

import fr.cea.ig.junit.drools.annotations.DroolsFiles;
import fr.cea.ig.junit.drools.annotations.DroolsSession;

/**
 * Classe permettant de gérer les annotations Drools créées ici.
 * 
 * @author jcharpen - Jordi CHARPENTIER - jcharpen@genoscope.cns.fr
 */
public class DroolsAnnotationProcessor {
	
	/**
	 * La classe de test lancée.
	 */
    private Object testClass;

    /**
     * Constructeur de la classe.
     * 
     * @param testClass La classe de test qu'on lance.
     */
    public DroolsAnnotationProcessor(Object testClass) {
        this.testClass = testClass;
    }

    /**
     * Méthode permettant de récupérer pour une classe donnée l'annotation 'DroolsFiles' 
     * qui va contenir la liste des fichiers Drools qu'on veut tester.
     * 
     * @return Un objet 'DroolsFiles' contenant la liste des fichiers Drools qu'on veut tester.
     */
    public DroolsFiles getDroolsFiles() {
        DroolsFiles droolsFiles = testClass.getClass().getAnnotation(DroolsFiles.class);

        if (droolsFiles == null) {
            throw new IllegalStateException("Il manque l'annotation 'DroolFiles'");
        }

        return droolsFiles;
    }

    /**
     * Méthode permettant d'injecter la session Drools dans la classe de test.
     * 
     * @param droolsSession La session Drools à injecter.
     */
    public void setDroolsSession(fr.cea.ig.junit.drools.DroolsSession droolsSession) {
        for (Field field : testClass.getClass().getDeclaredFields()) {
            field.setAccessible(true);

            if (field.isAnnotationPresent(DroolsSession.class)) {
                Object value = getValueToSet(droolsSession, field);

                try {
                    field.set(testClass, value);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /**
     * Méthode permettant de savoir sur quel champ on doit injecter la session Drools.
     * 
     * @param droolsSession La session Drools à injecter.
     * @param field Le champ sur lequel on va injecter la session.
     * 
     * @return Le champ sur lequel on doit injecter la session Drools.
     */
    private Object getValueToSet(fr.cea.ig.junit.drools.DroolsSession droolsSession, Field field) {
        Object toSet = null;
        
        if (field.getType().equals(fr.cea.ig.junit.drools.DroolsSession.class)) {
            toSet = droolsSession;
        } else {
            toSet = droolsSession.getStatefulSession();
        }
        
        return toSet;
    }
}