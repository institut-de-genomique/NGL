package lims.cng.dao;

import java.util.List;

import junit.framework.Assert;
import lims.cng.services.LimsRunServices;
import lims.models.instrument.Instrument;

import org.junit.Test;

import play.api.modules.spring.Spring;
import utils.AbstractTestsCNG;

public class LimsRunDAOTest extends AbstractTestsCNG {

	@Test
	 public void getInstruments() {
		if (play.Play.application().configuration().getString("institute").equals("CNG")) {
			LimsRunServices  limsRunServices = Spring.getBeanOfType(LimsRunServices.class);
			Assert.assertNotNull(limsRunServices);
			List<Instrument> instruments  = limsRunServices.getInstruments();
			Assert.assertTrue(instruments.size() > 0);
		}
	}

}
