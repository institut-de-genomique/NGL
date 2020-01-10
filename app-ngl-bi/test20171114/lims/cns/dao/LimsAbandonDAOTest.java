package lims.cns.dao;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import lims.models.runs.EtatTacheHD;
import lims.models.runs.TacheHD;

import org.junit.Test;

import play.Logger;
import play.api.modules.spring.Spring;
import utils.AbstractTestsCNG;

public class LimsAbandonDAOTest extends AbstractTestsCNG {

	@Test
	public void getTacheHD() {
		if (play.Play.application().configuration().getString("institute").equals("CNS")) {
			LimsAbandonDAO  dao = Spring.getBeanOfType(LimsAbandonDAO.class);
			assertNotNull(dao);
			List<TacheHD> taches = dao.listTacheHD("20626");
			assertTrue(taches.size() == 0);
		}
	}

	@Test
	public void getEtatTacheHD() {
		if (play.Play.application().configuration().getString("institute").equals("CNS")) {
			LimsAbandonDAO  dao = Spring.getBeanOfType(LimsAbandonDAO.class);
			assertNotNull(dao);
			List<EtatTacheHD> etaches = dao.listEtatTacheHD();
			Logger.debug("Nb Etat tache = "+etaches.size());
			assertTrue(etaches.size() > 0);
		}
		
	}
}
