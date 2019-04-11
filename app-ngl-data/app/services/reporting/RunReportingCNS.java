//package services.reporting;
//
//import java.util.concurrent.TimeUnit;
//
//import javax.inject.Inject;
//
//import fr.cea.ig.play.migration.NGLContext;
//import scala.concurrent.duration.Duration;
//import services.instance.ImportDataUtil;
//
//public class RunReportingCNS {
//	
//	@Inject
//	public RunReportingCNS(NGLContext ctx) {
//		new ReportingCNS(Duration.create(ImportDataUtil.nextExecutionInSeconds(8, 0),TimeUnit.SECONDS)
//				,Duration.create(1,TimeUnit.DAYS), ctx);
//	}
//
//}
//
//
//
//
