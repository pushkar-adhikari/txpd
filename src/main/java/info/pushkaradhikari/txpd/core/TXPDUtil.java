package info.pushkaradhikari.txpd.core;

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

}
