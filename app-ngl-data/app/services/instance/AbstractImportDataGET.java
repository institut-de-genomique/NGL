package services.instance;

import models.LimsGETDAO;
//import models.TaraDAO;
import play.api.modules.spring.Spring;
import scala.concurrent.duration.FiniteDuration;

public abstract class AbstractImportDataGET extends AbstractImportData {

	public AbstractImportDataGET(String name, FiniteDuration durationFromStart,
			FiniteDuration durationFromNextIteration) {
		super(name, durationFromStart, durationFromNextIteration);
	}

	protected static LimsGETDAO  limsServices = Spring.getBeanOfType(LimsGETDAO.class);

}