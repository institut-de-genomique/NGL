package ngl.data.data;

import org.junit.Test;

import fr.cea.ig.play.IGGlobals;
import models.Constants;
import ngl.data.Global;
import nglapps.DataService;
import validation.ContextValidation;

/**
 * Resolutions database population test.
 * 
 * @author vrd
 *
 */
public class ResolutionsTest {

	/**
	 * Tests that the CNS data service executes without errors.
	 * @throws Exception test failed
	 */
	@Test
	public void testSave() throws Exception {
		Global.afCNS.run(app -> {
			DataService dataService = IGGlobals.instanceOf(DataService.class);
//			ContextValidation ctx = new ContextValidation(Constants.NGL_DATA_USER);
//			ctx.setCreationMode();
			ContextValidation ctx = ContextValidation.createCreationContext(Constants.NGL_DATA_USER);
			dataService.saveResolutionData(ctx);
			if (ctx.getErrors().size() > 0)
				throw new RuntimeException("error occured");
		});
	}
	
}
