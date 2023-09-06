package services.instance;

import fr.cea.ig.ngl.NGLApplication;
import models.LimsCNSDAO;
import models.TaraDAO;
import play.api.modules.spring.Spring;

public abstract class AbstractImportDataCNS extends AbstractImportData {

	protected static LimsCNSDAO  limsServices;
	protected static TaraDAO     taraServices;

	protected AbstractImportDataCNS(String name, NGLApplication app) {
		super(name, app);
		limsServices = Spring.getBeanOfType(LimsCNSDAO.class);
		taraServices = Spring.getBeanOfType(TaraDAO.class);
	}

}
