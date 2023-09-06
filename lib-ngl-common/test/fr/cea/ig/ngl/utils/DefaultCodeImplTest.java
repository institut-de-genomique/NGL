package fr.cea.ig.ngl.utils;

import static org.junit.Assert.*;

import org.junit.*;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

import fr.cea.ig.ngl.dao.api.factory.ProjectFactory;
import models.laboratory.project.instance.Project;
import models.utils.code.DefaultCodeImpl;

/**
 * Test de la classe 'DefaultCodeImpl'.
 * 
 * @author jcharpen - Jordi Charpentier - jcharpen@genoscope.cns.fr
 */
@RunWith(PowerMockRunner.class)
public class DefaultCodeImplTest {
	
	/**
	 * Si on veut faire des traitements avant l'exécution de la suite de tests. 
	 * Par exemple : initialiser un mock, initialiser une méthode statique, ...
	 */
	@Before
	public void setUp() {
		
	}
	
	@Test
	public void testNextSampleCodePassageAVersB() {
		String lastSampleCode = "BUQ_AAAA";
		String code = "BUQ";
		Project project = ProjectFactory.getRandomProject(true, code, lastSampleCode, 4);
		
		String nextSampleCode = DefaultCodeImpl.nextSampleCode(project);
		
		assertNotNull(nextSampleCode);
		assertEquals("Next sample code is not as expected.", "BUQ_AAAB", nextSampleCode);
	}

	@Test
	public void testNextSampleCodePassageAZVersBA() {
		String lastSampleCode = "BUQ_AAAZ";
		String code = "BUQ";
		Project project = ProjectFactory.getRandomProject(true, code, lastSampleCode, 4);
		
		String nextSampleCode = DefaultCodeImpl.nextSampleCode(project);
		
		assertNotNull(nextSampleCode);
		assertEquals("Next sample code is not as expected.", "BUQ_AABA", nextSampleCode);
	}

	@Test
	public void testNextSampleCodePassageZZZVersAAAA() {
		String lastSampleCode = "BUQ_ZZZ";
		String code = "BUQ";
		Project project = ProjectFactory.getRandomProject(true, code, lastSampleCode, 3);
		
		String nextSampleCode = DefaultCodeImpl.nextSampleCode(project);
		
		assertNotNull(nextSampleCode);
		assertEquals("Next sample code is not as expected.", "BUQ_AAAA", nextSampleCode);
	}
}