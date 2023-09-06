package fr.cea.ig.junit.drools.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Interface permettant de définir le comportement de l'annotation 'DroolsFiles'.
 * 
 * @author jcharpen - Jordi CHARPENTIER - jcharpen@genoscope.cns.fr
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface DroolsFiles {

    /**
     * Liste des fichiers Drools à utiliser pendant les tests de la classe.
     */
    String[] value();
}