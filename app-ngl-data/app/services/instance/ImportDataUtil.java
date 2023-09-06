package services.instance;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import org.joda.time.DateTime;
import org.joda.time.Seconds;

import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

public class ImportDataUtil {

	/**
	 * Méthode permettant de calculer le nombre de secondes entre maintenant et la prochaine exécution d'un CRON.
	 * 
	 * @param hour L'heure d'exécution du CRON.
	 * @param minute Les minutes d'exécution du CRON.
	 * 
	 * @return Un nombre de secondes représentant le nombre de secondes entre maintenant et la prochaine date d'exécution du CRON.
	 */
	public static int nextExecutionInSeconds(int hour, int minute) {
		return Seconds.secondsBetween(new DateTime(), nextExecution(hour, minute)).getSeconds();
	}

	/**
	 * Méthode permettant de calculer la prochaine exécution d'un CRON à partir d'une heure "théorique" (heure / minutes).
	 * Si l'heure est déjà passée, on ajoute 24h pour avoir le jour d'après et la prochaine exécution.
	 * 
	 * @param hour L'heure d'exécution du CRON.
	 * @param minute Les minutes d'exécution du CRON.
	 * 
	 * @return Un objet "DateTime" représentant la prochaine date d'exécution du CRON.
	 */
	public static DateTime nextExecution(int hour, int minute) {
		DateTime next = new DateTime().withHourOfDay(hour).withMinuteOfHour(minute).withSecondOfMinute(0).withMillisOfSecond(0);

		return (next.isBeforeNow()) ? next.plusHours(24) : next;
	}

	/**
	 * Méthode permettant de définir une heure (+ minutes) où on veut exécuter un CRON à partir du lancement de DATA.
	 * Si l'heure donnée est déjà passée dans la journée (il est 20h et on veut exécuter le CRON à 19h), on lance le CRON le lendemain, du coup.
	 * 
	 * @param hour L'heure à laquelle on veut lancer le CRON.
	 * @param minutes Les minutes à laquelle on veut lancer le CRON.
	 * 
	 * @return Un objet "FiniteDuration" qui permettra de lancer le CRON à l'heure voulue.
	 */
	public static FiniteDuration getDurationInHourAndMinute(int hour, int minutes) {
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
	 * Méthode permettant de définir un intervalle en minutes à partir duquel on veut exécuter un CRON à partir du lancement de DATA.
	 * Si les minutes données sont déjà passées dans l'heure (il est 20h30 et on veut exécuter le CRON à 15), on lance le CRON l'heure d'après, du coup.
	 * 
	 * @param minutes L'intervalle en minutes à partir duquel on veut exécuter un CRON.
	 * 
	 * @return Un objet "FiniteDuration" qui permettra de lancer le CRON à l'heure voulue.
	 */
	public static FiniteDuration getDurationInMinute(int minutes) {
		Calendar c = Calendar.getInstance();
		int hour = c.get(Calendar.HOUR_OF_DAY);

		if (c.get(Calendar.MINUTE) > minutes) {
			hour = hour + 1;

			c.set(Calendar.HOUR_OF_DAY, hour);
			c.set(Calendar.MINUTE, minutes);			
		} else {
			c.set(Calendar.MINUTE, minutes);			
		}

		return Duration.create(c.getTimeInMillis() - System.currentTimeMillis(),TimeUnit.MILLISECONDS);
	}

	/**
	 * Méthode permettant de définir un intervalle en secondes à partir duquel on veut exécuter un CRON à partir du lancement de DATA.
	 * 
	 * @param nbSeconds L'intervalle en secondes à partir duquel on veut exécuter un CRON.
	 * 
	 * @return Un objet "FiniteDuration" qui permettra de lancer le CRON à l'heure voulue.
	 */
	public static FiniteDuration getDurationForNextSeconds(Integer nbSeconds) {
		return Duration.create(nbSeconds, TimeUnit.SECONDS);
	}
}
