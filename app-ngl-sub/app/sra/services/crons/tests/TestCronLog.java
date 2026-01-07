package sra.services.crons.tests;

public class TestCronLog implements Runnable {
	private static final play.Logger.ALogger logger = play.Logger.of(TestCronLog.class);
	public void run() {
		logger.warn("Test dans TestCronLog");
	}
}