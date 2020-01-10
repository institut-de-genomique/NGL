package controllers.migration;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import fr.cea.ig.ngl.NGLApplication;
import play.mvc.Controller;
import play.mvc.Result;
import scala.concurrent.duration.Duration;
import services.instance.ImportDataUtil;

public class Primers extends Controller {
	
	private final NGLApplication app;
	
	@Inject
	public Primers(NGLApplication app) {
		this.app = app;
	}
	
	public Result migration() {
		new MigrationPrimers(app).startScheduling(ImportDataUtil.getDurationForNextSeconds(5),Duration.create(96, TimeUnit.HOURS));
		return ok();
	}

}
