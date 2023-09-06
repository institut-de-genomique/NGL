package fr.cea.ig.ngl.drools.bi.run;

import org.junit.*;
import org.junit.runner.RunWith;
import org.kie.api.runtime.ClassObjectFilter;
import org.kie.api.runtime.KieSession;
import org.mongojack.DBQuery;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.MongoDBResult;
import fr.cea.ig.junit.drools.JUnitTest;
import fr.cea.ig.junit.drools.annotations.DroolsFiles;
import fr.cea.ig.junit.drools.annotations.DroolsSession;
import fr.cea.ig.ngl.dao.api.factory.ContainerFactory;
import fr.cea.ig.ngl.dao.api.factory.run.RunNanoporeFactory;
import fr.cea.ig.ngl.utils.TestUtils;
import models.laboratory.container.instance.Container;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.run.instance.Run;
import models.utils.InstanceHelpers;
import ngl.refactoring.state.ContainerStateNames;
import play.modules.mongojack.MongoDBPlugin;
import validation.ContextValidation;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

@RunWith(PowerMockRunner.class)
@DroolsFiles(
	value = {
		"lib-ngl-common/conf/rules/ngl-bi/common/import.drl",
		"lib-ngl-common/conf/rules/ngl-bi/cns/import.drl",
		"lib-ngl-common/conf/rules/ngl-bi/cns/run/rulesRuns_F_S_1.drl"
	}
)
@PrepareForTest({ InstanceHelpers.class, MongoDBPlugin.class, MongoDBDAO.class })
public class CreateReadsetNanoporeTest extends JUnitTest {
	
	@DroolsSession
	private KieSession session;

	/**
	 * Si on veut faire des traitements avant l'exécution de la suite de tests. 
	 * Par exemple : initialiser un mock, initialiser une méthode statique, ...
	 */
	@Before
	@SuppressWarnings({"unchecked"})
	public void setUp() {		
		// Mock des classes statiques.
		PowerMockito.mockStatic(InstanceHelpers.class);
		PowerMockito.mockStatic(MongoDBPlugin.class);
		PowerMockito.mockStatic(MongoDBDAO.class);
		
		// Initialisation de la base Drools.
		Run run = RunNanoporeFactory.getRandomRunNanopore();
		Container container = ContainerFactory.getRandomContainer(ContainerStateNames.IW_P);
		ContextValidation ctx = ContextValidation.createCreationContext(TestUtils.CURRENT_USER);

		session.insert(run);
		session.insert(container);
		session.insert(ctx);
		
		// Mock pour les tests.
		MongoDBResult<ReadSet> dbRes = mock(MongoDBResult.class);
		List<ReadSet> rsList = new ArrayList<>();	
						
		when(MongoDBDAO.find(any(String.class), any(Class.class), any(DBQuery.Query.class))).thenReturn(dbRes);
		when(dbRes.toList()).thenReturn(rsList);
	}
	
	@Test
	public void testNumberRules() {		
		int rulesFired = session.fireAllRules();
        
        assertEquals("Not all the rules were fired", 2, rulesFired);
	}
	
	@Test
	public void testReadsetGenerated() {		
		session.fireAllRules();
		
		// Récupération du readset généré.
		
		Collection<?> validations = session.getObjects(new ClassObjectFilter(ReadSet.class));
        Iterator<?> it = validations.iterator();
        
        while (it.hasNext()) {
        	ReadSet readsetInRule = (ReadSet) it.next();
        	
        	assertEquals("Code is not as expected", "AAA_ONT_1_FAA54955_A", readsetInRule.code);
        	assertEquals("Location is not as expected", "CNS", readsetInRule.location);
        }
        
        // Récupération du contexte de validation.
        
        validations = session.getObjects(new ClassObjectFilter(ContextValidation.class));
        it = validations.iterator();
        
        while (it.hasNext()) {
        	ContextValidation ctxInRule = (ContextValidation) it.next();
        	
        	assertEquals("Context has errors", 0, ctxInRule.getErrors().size());
        }     
	}
}