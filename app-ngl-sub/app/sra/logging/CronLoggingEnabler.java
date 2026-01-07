package sra.logging;

import org.slf4j.MDC;

/**
 * Activateur de la journalisation des crons
 * <p>
 * <a href="https://logback.qos.ch/manual/filters.html"> Documentation </a> 
 * <p>
 * Voir ex de logger.xml:
 * <p>
 * href="https://github.com/qos-ch/logback/blob/master/logback-examples/src/main/resources/chapters/filters/mdcfilter.xml"
 * @author sgas
 *
 */
//public class CronLoggingEnabler implements AutoCloseable {
//	public static final String key = "specialLogger";
//	public static final String value = "CRON";
//	
//	public CronLoggingEnabler() {
//		MDC.put(key, value);
//	}
//
//	@Override
//	public void close() throws Exception {
//		MDC.remove(key);		
//	}
//	
//	public static CronLoggingEnabler getEnabler() {
//		return new CronLoggingEnabler();
//	}
//	
//}