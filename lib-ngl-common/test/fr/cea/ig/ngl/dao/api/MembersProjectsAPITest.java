package fr.cea.ig.ngl.dao.api;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.Test;
import org.mockito.Mockito;

import fr.cea.ig.ngl.dao.api.factory.MembersFactory;
import fr.cea.ig.ngl.dao.projects.*;
import fr.cea.ig.ngl.utils.TestUtils;
import models.laboratory.project.instance.Members;
import play.Logger;

/**
 * Test de l'API de l'entité MEMBERSPROJECTS.
 * 
 * @author jcharpen - Jordi Charpentier - jcharpen@genoscope.cns.fr
 */
public class MembersProjectsAPITest {

    private MembersProjectsDAO membersProjectsDao = Mockito.mock(MembersProjectsDAO.class);;
    
    private UserMembersProjectsDAO userDao = Mockito.mock(UserMembersProjectsDAO.class);
    
    private GroupMembersProjectsDAO groupDao = Mockito.mock(GroupMembersProjectsDAO.class);

    private MembersProjectsAPI membersProjectAPI = new MembersProjectsAPI(membersProjectsDao, userDao, groupDao);

    @Test
    public void testCreateValid() {
        String randomCode = UUID.randomUUID().toString();

		Members member = MembersFactory.getRandomMembers();
		member.codeProjet = randomCode;

        when(membersProjectsDao.saveObject(member)).thenReturn(member);

		try {
			Members membCreateAPI = membersProjectAPI.create(member, TestUtils.CURRENT_USER);

			assertTrue("Different code on testCreateValid()", membCreateAPI.codeProjet.equals(member.codeProjet));
			assertNotNull(membCreateAPI);
		} catch (APIException e) {
			Logger.error("Exception occured during testCreateValid()");
			fail(e.getMessage());
        }
    }

    // @Test
    public void testCreateInvalid() {
        String randomCode = UUID.randomUUID().toString();

		Members member = MembersFactory.getRandomMembersInvalid();
		member.codeProjet = randomCode;

        when(membersProjectsDao.saveObject(member)).thenReturn(member); 
        
        boolean exceptFired = false;
        Members membCreateAPI = null;

		try {
			membCreateAPI = membersProjectAPI.create(member, TestUtils.CURRENT_USER);
		} catch (APIException e) {
			exceptFired = true;
        }

        if (!exceptFired) {
            fail("Invalid member, should have been refused.");
        }

        assertNull(membCreateAPI);
    }

    @Test
    public void testUpdateValid() {
        Members members = mock(Members.class);
		
		when(membersProjectsDao.updateObject(members)).thenReturn(members);
		
		Members membersUpdateAPI = null;
		
		try {
			membersUpdateAPI = membersProjectAPI.update(members, TestUtils.CURRENT_USER);
		} catch (APIException e) {
			Logger.error("Exception occured during testUpdateValid()");
			fail(e.getMessage());
		}
		
		assertNotNull(membersUpdateAPI);
    }

    @Test
    public void testUpdateInvalid() {
        Members members = MembersFactory.getRandomMembersInvalid();
		
        when(membersProjectsDao.updateObject(members)).thenReturn(members);
		
        Members membersUpdateAPI = null;
        boolean exceptFired = false;
		
		try {
			membersUpdateAPI = membersProjectAPI.update(members, TestUtils.CURRENT_USER);
		} catch (APIException e) {
			exceptFired = true;
        }
        
        if (!exceptFired) {
            fail("Invalid member, should have been refused.");
        }
		
		assertNull(membersUpdateAPI);
    }
}
