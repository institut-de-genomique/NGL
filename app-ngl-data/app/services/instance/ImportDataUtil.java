package services.instance;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import org.joda.time.DateTime;
import org.joda.time.Seconds;

import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

public class ImportDataUtil {

	public static int nextExecutionInSeconds(int hour, int minute) {
		return Seconds.secondsBetween(new DateTime(), nextExecution(hour, minute)).getSeconds();
	}

	public static DateTime nextExecution(int hour, int minute) {
		DateTime next = new DateTime().withHourOfDay(hour).withMinuteOfHour(minute).withSecondOfMinute(0)
				.withMillisOfSecond(0);

		return (next.isBeforeNow()) ? next.plusHours(24) : next;
	}

	/**
	 * Get the duration for a specific time (hour and minute).
	 * @param hour    hours
	 * @param minutes minutes
	 * @return        duration
	 */
	public static FiniteDuration getDurationInMillinsBefore(int hour, int minutes) {
		Calendar c = Calendar.getInstance();
		
		if (c.get(Calendar.HOUR_OF_DAY) > hour) {
			int day = c.get(Calendar.DAY_OF_YEAR) + 1;
			c.set(Calendar.DAY_OF_YEAR, day);
			c.set(Calendar.HOUR_OF_DAY, hour);
			c.set(Calendar.MINUTE, minutes);			
		} else {
			c.set(Calendar.HOUR_OF_DAY, hour);
			c.set(Calendar.MINUTE, minutes);		
		}
		return Duration.create(c.getTimeInMillis() - System.currentTimeMillis(),TimeUnit.MILLISECONDS);
	}
	
	/**
	 * Get the duration for next time with specific hour.
	 * @param hour hour
	 * @return     duration
	 */
	public static FiniteDuration getDurationForNextHour(int hour) {
		Calendar c = Calendar.getInstance();
		if (c.get(Calendar.MINUTE) > hour) {
			hour = c.get(Calendar.HOUR_OF_DAY) + 1;
			c.set(Calendar.HOUR_OF_DAY, hour);
			c.set(Calendar.MINUTE, hour);			
		} else {
			c.set(Calendar.MINUTE, hour);			
		}
		return Duration.create(c.getTimeInMillis() - System.currentTimeMillis(),TimeUnit.MILLISECONDS);
	}

	public static FiniteDuration getDurationForNextSeconds(Integer nbSeconds) {
		return Duration.create(nbSeconds,TimeUnit.SECONDS);
	}

}
