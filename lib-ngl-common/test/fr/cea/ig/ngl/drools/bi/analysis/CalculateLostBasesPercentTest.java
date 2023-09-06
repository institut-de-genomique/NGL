package fr.cea.ig.ngl.drools.bi.analysis;

import org.junit.*;
import org.junit.runner.RunWith;
import org.kie.api.runtime.KieSession;
import org.mongojack.DBQuery;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.mongodb.BasicDBObject;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.junit.drools.JUnitTest;
import fr.cea.ig.junit.drools.annotations.DroolsFiles;
import fr.cea.ig.junit.drools.annotations.DroolsSession;
import fr.cea.ig.ngl.dao.api.factory.AnalysisFactory;
import fr.cea.ig.ngl.dao.api.factory.ReadsetFactory;
import models.laboratory.run.instance.Analysis;
import models.laboratory.run.instance.ReadSet;
import play.modules.mongojack.MongoDBPlugin;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@DroolsFiles(
	value = {
		"lib-ngl-common/conf/rules/ngl-bi/common/import.drl",
		"lib-ngl-common/conf/rules/ngl-bi/cns/import.drl",
		"lib-ngl-common/conf/rules/common/import.drl",
		"lib-ngl-common/conf/rules/common/common.drl",
		"lib-ngl-common/conf/rules/ngl-bi/cns/analysis/statBPA_1.drl"
	}
)
@PrepareForTest({ MongoDBPlugin.class, MongoDBDAO.class })
public class CalculateLostBasesPercentTest extends JUnitTest {

	@DroolsSession
	private KieSession session;

	/**
	 * Si on veut faire des traitements avant l'exécution de la suite de tests. 
	 * Par exemple : initialiser un mock, initialiser une méthode statique, ...
	 */
	@Before
	@SuppressWarnings("unchecked")
	public void setUp() {
		// Mock des classes statiques.
		PowerMockito.mockStatic(MongoDBPlugin.class);
		PowerMockito.mockStatic(MongoDBDAO.class);
		
		// Initialisation de la base Drools.
		Analysis analysis = AnalysisFactory.getRandomAnalysis();

		session.insert(analysis);
		
		// Mock de la recherche de readsets.
		ReadSet readset = mock(ReadSet.class);
		readset = ReadsetFactory.fillReadset(readset);
		
		when(MongoDBDAO.findOne(any(String.class), any(Class.class), any(DBQuery.Query.class), any(BasicDBObject.class))).thenReturn(readset);
	}
	
	@Test
	public void testNumberRules() {		
		int rulesFired = session.fireAllRules();
        
        assertEquals("Not all the rules were fired", 2, rulesFired);
	}
}