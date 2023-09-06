package fr.cea.ig.junit.drools;

import org.drools.compiler.builder.impl.KnowledgeBuilderImpl;
import org.drools.compiler.compiler.DroolsError;
import org.drools.compiler.compiler.DroolsParserException;
import org.drools.compiler.compiler.PackageBuilderErrors;
import org.kie.api.runtime.KieSession;
import org.kie.internal.KnowledgeBase;
import org.kie.internal.KnowledgeBaseFactory;
import org.kie.internal.definition.KnowledgePackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collection;

/**
 * Classe permettant d'injecter la session Drools à partir des fichiers Drools passé dans l'annotation.
 * 
 * @author jcharpen - Jordi CHARPENTIER - jcharpen@genoscope.cns.fr
 */
public class DroolsInjector {
	
	/**
	 * Logger de la classe.
	 */
    private static final Logger LOG = LoggerFactory.getLogger(DroolsInjector.class);

    /**
     * Méthode permettant d'initialiser Drools et de créer la base de connaissance associée.
     * 
     * @param testClass Le nom de la classe de test qu'on lance.
     * 
     * @throws IOException 
     * @throws DroolsParserException 
     */
    public void initDrools(Object testClass) throws DroolsParserException, IOException {
        if (testClass == null) {
            throw new IllegalArgumentException("La classe est nulle : l'annotation n'a pas due être ajoutée sur une classe.");
        }

        LOG.info("Initializing Drools objects for test class: {}", testClass.getClass());

        DroolsAnnotationProcessor annotationProcessor = new DroolsAnnotationProcessor(testClass);
        DroolsSession droolsSession = initKnowledgeBase(Arrays.asList(annotationProcessor.getDroolsFiles().value()));

        annotationProcessor.setDroolsSession(droolsSession);
    }

    /**
     * Méthode permettant d'initialiser la base de connaissances Drools.
     * 
     * @param fileNames La liste des fichiers Drools qu'on veut utiliser pour nos tests.
     * @return Un objet 'DroolsSession' qui représente la base de connaissances Drools qu'on veut créer.
     * 
     * @throws IOException 
     * @throws DroolsParserException 
     */
    public static DroolsSession initKnowledgeBase(Iterable<String> fileNames) throws DroolsParserException, IOException {
    	KnowledgeBuilderImpl builder = new KnowledgeBuilderImpl();

        LOG.info("Initialisation de la base de connaissances avec des fichiers DRL localisés dans : {}", fileNames);
        
        for (String fileName : fileNames) {
        	File f = new File(fileName);
        	InputStream stream = new FileInputStream(f);
        	InputStreamReader isr = new InputStreamReader(stream);
        	
            builder.addPackageFromDrl(isr);
        }
        
        PackageBuilderErrors errors = builder.getErrors();

        // On vérifie s'il y a des erreurs de compilation dans les fichiers DRL.
        if (errors.getErrors().length > 0) {
            LOG.error("Erreur lors du chargement des fichiers DRL.");

            for (DroolsError error : errors.getErrors()) {
                LOG.error("Erreur : {}", error.getMessage());
            }

            throw new IllegalStateException("Il y a une (ou plusieurs) erreur(s) dans les fichiers DRL.");
        }

        Collection<KnowledgePackage> pkgs = builder.getKnowledgePackages();

        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        kbase.addKnowledgePackages(pkgs);
        
        KieSession session = kbase.newKieSession();

        return new DroolsSessionImpl(session);
    }
}