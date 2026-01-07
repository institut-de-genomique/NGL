package sra.services.scheduler;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

public class Tools {
	
	/**
	 * Méthode permettant de définir une heure (+ minutes) où on veut exécuter un CRON. 
	 * @param hour       L'heure à laquelle on veut lancer le CRON.
	 * @param minutes    Les minutes auxlaquelles on veut lancer le CRON.
	 * @return           Un objet "FiniteDuration" qui permettra de lancer le CRON à l'heure voulue.
	 */	    
	public static FiniteDuration at(int hour, int minutes) {
		Calendar dateLancementCron = Calendar.getInstance();
		dateLancementCron.setTimeZone(TimeZone.getTimeZone("Europe/Paris"));
		dateLancementCron.set(Calendar.HOUR_OF_DAY, hour);
		dateLancementCron.set(Calendar.MINUTE, minutes);	
		dateLancementCron.set(Calendar.SECOND, 0);
		dateLancementCron.set(Calendar.MILLISECOND, 0);
		long maintenant = System.currentTimeMillis();  // new Date().getTime();
		if( dateLancementCron.getTimeInMillis() < maintenant ) {
			dateLancementCron.add(Calendar.DAY_OF_YEAR, 1);
		}
		return Duration.create(dateLancementCron.getTimeInMillis() - maintenant, TimeUnit.MILLISECONDS);	
	}	

	
	public static FiniteDuration days(int days) {
		return Duration.create(days, TimeUnit.DAYS);
	}	
	
	//https://stackoverflow.com/questions/76819940/simpledateformatyyyy-mm-ddthhmmssz-customize-the-timezone
	public static String getStringDate() {
		String output = 
			    ZonedDateTime
			    .now(
			        ZoneId.of( "Europe/Paris") 
			    )
			    .toOffsetDateTime()
			    .truncatedTo( ChronoUnit.SECONDS )
			    .toString()
			;
		return output;
	}
	
}
