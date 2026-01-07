package services.instance;

import java.util.Calendar;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

import scala.concurrent.duration.FiniteDuration;

public class ImportDataUtilTest {
	
	@Test
	public void testAt_1() {
		FiniteDuration fd = ImportDataUtil.at(0, 0);
		Assert.assertTrue(fd.toMillis() > 0);
	}

	@Test
	public void testAt_2() {
		FiniteDuration fd = ImportDataUtil.at(24, 0);
		Assert.assertTrue(fd.toMillis() > 0);
	}
	
	@Test
	public void test() {
		// si cron demandé pour 18h10 et qu'il est 18h50, alors finiteDuration vaut -40 mn au lieu de 23h20
		//FiniteDuration fd = ImportDataUtil.getDurationInHourAndMinute(18, 0);
	    // si cron demandé pour 18h10 et qu'il est 18h50, alors finiteDuration vaut bien 23h20
		//FiniteDuration fd = ImportDataUtil.at(18, 0);
		//Assert.assertTrue(fd.toMillis() > 0);
	}

}
