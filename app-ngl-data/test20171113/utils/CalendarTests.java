package utils;

import static org.junit.Assert.*;

import java.util.Calendar;

import org.junit.Test;

public class CalendarTests {

	@Test
	public void test() {
		Calendar c = Calendar.getInstance();
		c.set(Calendar.HOUR_OF_DAY, 20);
		c.set(Calendar.MINUTE, 0);
		
		System.out.println( (c.getTimeInMillis() - System.currentTimeMillis()) +" ms");
		System.out.println( (c.getTimeInMillis() - System.currentTimeMillis()) / 1000 +" s");
		System.out.println( (c.getTimeInMillis() - System.currentTimeMillis()) / 1000 / 60 +" m");
		System.out.println( (c.getTimeInMillis() - System.currentTimeMillis()) / 1000 / 60 / 60 +" h");
		
		
		//c.set(0, Calendar.MINUTE);
		
	}



}
