package fr.cea.ig.ngl.dao.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.mockito.Mockito;

import models.administration.authorisation.Permission;
import models.utils.dao.DAOException;
import play.Logger;
import fr.cea.ig.ngl.dao.permissions.PermissionAPI;
import fr.cea.ig.ngl.dao.permissions.PermissionDAO;
import fr.cea.ig.ngl.utils.TestUtils;

/**
 * Test de l'API de l'entité PERMISSION.
 * 
 * @author jcharpen - Jordi Charpentier - jcharpen@genoscope.cns.fr
 */
public class PermissionAPITest {
	
	private final PermissionDAO permissionDAO = Mockito.mock(PermissionDAO.class);

	private final PermissionAPI permissionAPI = new PermissionAPI(permissionDAO);
	
	@Test
	public void testHasPermissionsValid() {
		List<Permission> permiList = new ArrayList<>();

		Permission pe1 = new Permission();
		pe1.code = "reading";
		pe1.label = "reading";
		permiList.add(pe1);

		Permission pe2 = new Permission();
		pe2.code = "writing";
		pe2.label = "writing";
		permiList.add(pe2);

		when(permissionDAO.byUserLogin(TestUtils.CURRENT_USER)).thenReturn(permiList);

		boolean hasPermission = permissionAPI.hasPermission(TestUtils.CURRENT_USER, "reading");

		assertEquals("hasPermission() is not working properly", true, hasPermission);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testHasPermissionsInvalid() {
		Logger.debug("testHasPermissionsInvalid");

		when(permissionDAO.byUserLogin(TestUtils.CURRENT_USER)).thenThrow(DAOException.class);
		boolean exceptFired = false;

		try {
			permissionAPI.hasPermission(TestUtils.CURRENT_USER, "reading");
		} catch (DAOException e) {
			exceptFired = true;
		}

		if (!exceptFired) {
			fail("Invalid permission, should have been refused.");
		}
	}
}
