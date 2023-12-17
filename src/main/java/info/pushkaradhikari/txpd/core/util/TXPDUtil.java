package info.pushkaradhikari.txpd.core.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class TXPDUtil {

	private TXPDUtil() {
		throw new IllegalStateException("Utility class");
	}

	public static <T> List<T> toList(final Iterable<T> iterable) {
		return StreamSupport.stream(iterable.spliterator(), false).collect(Collectors.toList());
	}

	public static boolean isNumeric(String str) {
		return str.matches("-?\\d+(\\.\\d+)?"); // match a number with optional '-' and decimal.
	}

	public static long toEpochMilli(LocalDateTime localDateTime) {
		return localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
	}
	
	public static String formatBreaks(String input) {
        StringBuilder result = new StringBuilder();
        int lineLength = 0;

        for (String word : input.split(" ")) {
            if (lineLength + word.length() > 30) {
                result.append("<br/>");
                lineLength = 0;
            } else if (lineLength > 0) {
                result.append(" ");
                lineLength++;
            }

            result.append(word);
            lineLength += word.length();
        }

        return result.toString();
    }

}
