
import javax.inject.Inject;

import org.joda.time.DateTime;
import org.joda.time.Seconds;

import fr.cea.ig.ngl.NGLApplication;
import nglapps.IApplicationData;

/**
 * Old play application global start.
 * 
 * @author vrd
 *
 */
public class Global {
	
	/**
	 * Logger.
	 */
	private static final play.Logger.ALogger logger = play.Logger.of(Global.class);
	
	/**
	 * 
	 */
	private final NGLApplication app;
	private final IApplicationData appData;
	
	
	@Inject
	public Global (NGLApplication app, IApplicationData appData) {
		this.app     = app;
		this.appData = appData;
	}
	
//	// @Override
//	public void onStart(play.Application app, IApplicationData appData) {
//		logger.info("NGL has started");
//
////		try {
////			RulesServices6.getInstance();
////		} catch (Throwable e) {
////			logger.error("Error Load knowledge base");
////			logger.error("Drools Singleton error: "+e.getMessage(),e);
////			//Shutdown application
////			// Play.stop();
////		}
//
//		importData();
//		generateReporting();
//		
//	}

	public void start() {
		logger.info("NGL has started");
		importData();
		generateReporting();		
	}

//	// @Override
//	public void onStop(play.Application app) {
//		logger.info("NGL shutdown...");
//	}

//	public void importData() {
//		// if (play.Play.application().configuration().getBoolean("import.data")) {
////		if (IGGlobals.configuration().getBoolean("import.data",false)) {
//		if (ctx.config().getBoolean("import.data",false)) {
//			logger.info("NGL import data has started");
//			try {
//
//				// String institute=play.Play.application().configuration().getString("import.institute");
//				String institute = ctx.config().getString("import.institute");
//				logger.info("Import institute {}", institute);
//
//				if ("CNG".equals(institute)) {
//					new ImportDataCNG(ctx).run();
//				} else if ("CNS".equals(institute)) {
//					new ImportDataCNS(ctx).run();
//				} else {
//					throw new RuntimeException("La valeur de l'attribut import.institute dans application.conf n'a pas d'implementation");
//				}
//
//			} catch(Exception e) {
//				throw new RuntimeException("L'attribut import.institute dans application.conf n'est pas renseigné",e);
//			}
//
//		} else { 
//			logger.info("No import data"); 
//		}
//	}
	public void importData() {
		// if (play.Play.application().configuration().getBoolean("import.data")) {
//		if (IGGlobals.configuration().getBoolean("import.data",false)) {
		if (app.nglConfig().getBoolean("import.data",false)) {
			logger.info("NGL import data has started");
			appData.getImportingCronStarter().accept(app);
		} else { 
			logger.info("No import data"); 
		}
	}

//	public void generateReporting() {
//		if (ctx.config().getBoolean("reporting.active",false)) {
//			logger.info("NGL reporting has started");
//			try {
//				String institute = ctx.config().getInstitute();
//				logger.info("institute for the reporting : "+ institute);
//				if (institute.equals("CNS")) {
////					new RunReportingCNS(ctx);
//					new ReportingCNS(ctx)
//						.startScheduling(Duration.create(ImportDataUtil.nextExecutionInSeconds(8, 0),TimeUnit.SECONDS),
//								         Duration.create(1, TimeUnit.DAYS));
//				} else {
//					throw new RuntimeException("La valeur de l'attribut institute dans application.conf n'a pas d'implementation");
//				}
//			} catch(Exception e) {
//				throw new RuntimeException("L'attribut institute dans application.conf n'est pas renseigné");
//			}
//
//		} else { 
//			logger.info("No reporting"); 
//		}
//	}
	public void generateReporting() {
		if (app.nglConfig().getBoolean("reporting.active",false)) {
			logger.info("NGL reporting has started");
			appData.getReportingCronStarter().accept(app);
		} else { 
			logger.info("No reporting"); 
		}
	}

	public static int nextExecutionInSeconds(int hour, int minute) {
		return Seconds.secondsBetween(new DateTime(), nextExecution(hour, minute)).getSeconds();
		// return (int)(nextExecution(hour,minute).getMillis() - System.currentTimeMillis()) / 1000; 
	}

	public static DateTime nextExecution(int hour, int minute) {
		DateTime next = new DateTime()
				.withHourOfDay     (hour)
				.withMinuteOfHour  (minute)
				.withSecondOfMinute(0)
				.withMillisOfSecond(0);
		return (next.isBeforeNow()) ? next.plusHours(24) : next;
	}

}
