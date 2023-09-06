package services.instance;

import fr.cea.ig.ngl.NGLApplication;
import models.LimsCNGDAO;
import play.api.modules.spring.Spring;

public abstract class AbstractImportDataCNG extends AbstractImportData {

	protected static LimsCNGDAO limsServices = Spring.getBeanOfType(LimsCNGDAO.class);
	
//	@Inject
//	public AbstractImportDataCNG(String name, FiniteDuration durationFromStart,	FiniteDuration durationFromNextIteration, NGLContext ctx) {
//		super(name, durationFromStart, durationFromNextIteration, ctx);
//	}
	
//	protected AbstractImportDataCNG(String name, NGLContext ctx) {
//		super(name, ctx);
//	}

	protected AbstractImportDataCNG(String name, NGLApplication app) {
		super(name, app);
	}

}
