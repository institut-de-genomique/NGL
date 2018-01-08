package models;

import java.text.DecimalFormat;
import java.util.Calendar;

public class Constants {
	
	public final static String NGL_DATA_USER = "ngl-data";

	// time from today to import new samples
	protected final static Long DIFFERANCE = (30*24*60*60*1000L); 
	  
	// formated today's date
	public static String today(){
		Calendar td = Calendar.getInstance();
		return creationDate(td);
	}

	//formated sample creation limit date to import
	public static String startDay(){
			
			Calendar sd = Calendar.getInstance();
			sd.setTimeInMillis(Calendar.getInstance().getTimeInMillis() - DIFFERANCE);
			
			return creationDate(sd);

}
	// adapt date format to SQL import String
	private static String creationDate(Calendar c) {
		DecimalFormat towDigitFormat= new DecimalFormat("00");
		//Calendar.MONTH is in 0-11
	   return c.get(Calendar.YEAR) +"-"+ towDigitFormat.format(Double.valueOf(c.get(Calendar.MONTH)+1)) +"-"+ towDigitFormat.format(Double.valueOf(c.get(Calendar.DATE)));
		
	}
}
