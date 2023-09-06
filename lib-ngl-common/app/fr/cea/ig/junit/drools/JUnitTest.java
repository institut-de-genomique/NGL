package fr.cea.ig.junit.drools;

import java.io.IOException;

import org.drools.compiler.compiler.DroolsParserException;

/**
 * Classe regroupant les éléments communs liés aux tests unitaires.
 * 
 * @author jcharpen - Jordi CHARPENTIER - jcharpen@genoscope.cns.fr
 */
public class JUnitTest {
	
    /**
     * Constructeur de la classe.
     * Permet d'initialiser également la base Drools pour les tests grâce aux différentes annotations Drools fournies.
     * 
     * @param <T> La classe de test qu'on veut exécuter.
     */
    public <T> JUnitTest() {
        try {
            new DroolsInjector().initDrools(this);
        } catch (DroolsParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}