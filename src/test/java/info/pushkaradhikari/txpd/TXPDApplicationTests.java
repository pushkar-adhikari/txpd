package info.pushkaradhikari.txpd;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class TXPDApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	void testDateConversion() {
		String dateTime1 = "2020-07-01 00:00:00.000000000";
		long epochTime1 = toEpochMilli(dateTime1);
		assertNotNull(epochTime1);

		String dateTime2 = "2023-12-01 07:18:21.306666";
		long epochTime2 = toEpochMilli(dateTime2);
		assertNotNull(epochTime2);

		String dateTime3 = "2023-12-01 07:18:21.306";
		long epochTime3 = toEpochMilli(dateTime3);
		assertNotNull(epochTime3);

		String dateTime4 = "2023-12-01 07:18:21";
		long epochTime4 = toEpochMilli(dateTime4);
		assertNotNull(epochTime4);

		String dateTime5 = "2023-12-01 07:18:21.37789";
		long epochTime5 = toEpochMilli(dateTime5);
		assertNotNull(epochTime5);
	}

	private long toEpochMilli(String dateTime) {
		String baseFormat = "yyyy-MM-dd HH:mm:ss";
		SimpleDateFormat baseDateFormat = new SimpleDateFormat(baseFormat);

		try {
			int dotIndex = dateTime.indexOf('.');
			if (dotIndex != -1 && dotIndex < dateTime.length() - 1) {
				String withoutFraction = dateTime.substring(0, dotIndex);
				String fraction = dateTime.substring(dotIndex + 1);
				Date parsedDate = baseDateFormat.parse(withoutFraction);
				long time = parsedDate.getTime();

				if (fraction.length() > 3) {
					fraction = fraction.substring(0, 3); // Truncate to milliseconds
				}
				while (fraction.length() < 3) {
					fraction += "0"; // Pad to three digits
				}
				long milliFraction = Integer.parseInt(fraction);
				return time + milliFraction;
			} else {
				// No fractional seconds
				return baseDateFormat.parse(dateTime).getTime();
			}
		} catch (ParseException e) {
			fail("Date parsing failed: " + e.getMessage());
		}
		return 0;
	}

}
