package sra.scripts.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.cea.ig.lfw.utils.HashMapBuilder;

public class DateTools {
	
//	private static final Map <String, String> mapMois = new HashMap<String, String>() {
//		{put("janv", "1");
//		put("f�vr", "2");
//		put("mars", "3");
//		put("avr" , "4");
//		put("mai" , "5");
//		put("juin", "6");
//		put("juil", "7");
//		put("ao�t", "8");
//		put("sept", "9");
//		put("oct" ,"10");
//		put("nov" ,"11");
//		put("d�c" ,"12");
//		put("1"   , "1");
//		put("2"   , "2");
//		put("3"   , "3");
//		put("4"   , "4");
//		put("5"   , "5");
//		put("6"   , "6");
//		put("7"   , "7");
//		put("8"   , "8");
//		put("9"   , "9");
//		put("10"  ,"10");
//		put("11"  ,"11");
//		put("12"  ,"12");
//		}
//	};
	
	private static final Map <String, String> mapMois = new HashMapBuilder<String, String>()
		.put("janv", "1")
		.put("f�vr", "2")
		.put("mars", "3")
		.put("avr" , "4")
		.put("mai" , "5")
		.put("juin", "6")
		.put("juil", "7")
		.put("ao�t", "8")
		.put("sept", "9")
		.put("oct" ,"10")
		.put("nov" ,"11")
		.put("d�c" ,"12")
		.put("1"   , "1")
		.put("2"   , "2")
		.put("3"   , "3")
		.put("4"   , "4")
		.put("5"   , "5")
		.put("6"   , "6")
		.put("7"   , "7")
		.put("8"   , "8")
		.put("9"   , "9")
		.put("01"   , "1")
		.put("02"   , "2")
		.put("03"   , "3")
		.put("04"   , "4")
		.put("05"   , "5")
		.put("06"   , "6")
		.put("07"   , "7")
		.put("08"   , "8")
		.put("09"   , "9")
		.put("10"  ,"10")
		.put("11"  ,"11")
		.put("12"  ,"12")
		.asMap();		

	public static final String monthOrdinal(String month) {
		if (!mapMois.containsKey(month.toLowerCase())) {
			throw new RuntimeException("mois '" + month + "' non defini");
		}
		return  mapMois.get(month.toLowerCase()); 
	}

	// d=1
	// m=2
	// y=3
	public static final Date dmy(String sep, String date) {
		return internalFormatDateSep(1, 2, 3, sep, date);
	}
	
	//  dmyReg("(\d+)\s+([A-Z]+),\s+(\d+)","28   Aout, 2020")
	public static final Date dmyReg(String reg, String date) {
		return internalFormatDateReg(1, 2, 3, reg, date);
	}

	public static final Date mdy(String sep, String date) {
		return internalFormatDateSep(2, 1, 3, sep, date);
	}
	
	private static final Date internalFormatDateSep(int d, int m, int y, String sep, String userDate) {
		String [] tmp = userDate.split(sep);
		Date date = getDate(tmp[d-1], tmp[m-1], tmp[y-1]);
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.HOUR_OF_DAY, 12);
		date = cal.getTime();
		return date;
	}
	
	// userPattern=j/m/d
	// userPattern=m-d-j
	// userPattern=mm-dd-jjjj
	private static final Date internalFormatDateReg(int d, int m, int y, String userPattern, String userDate) {
		java.util.regex.Pattern p = Pattern.compile(userPattern);

		String userDay;
		String userYear;
		String userMonth;

		Matcher matcher = p.matcher(userDate);
		if (matcher.matches()) {
			userDay = matcher.group(d);
			userMonth = matcher.group(m);
			userYear = matcher.group(y);
		} else {
			throw new RuntimeException("");
		}

		return getDate(userDay, userMonth, userYear);
	}

	public static final Date getDate( String day, String month, String year) {
		try {
			return DateFormat
					.getDateInstance(DateFormat.SHORT, Locale.FRANCE)
					.parse(day + "/" + monthOrdinal(month)+ "/" + year);
		} catch (ParseException e) {
			throw new RuntimeException("",e);
		}
	}

}
