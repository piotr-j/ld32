package io.piotrjastrzebski.ld32.game.state;

import com.badlogic.gdx.utils.StringBuilder;

import java.math.BigDecimal;

/**
 * Formats big decimals to specified format
 * Created by EvilEntity on 18/04/2015.
 */
public class NumberFormatter {
	public enum FormatType {ENGINEER}

	private static StringBuilder sb;

	public static String format (BigDecimal bigDecimal, FormatType type) {
		switch (type) {
		case ENGINEER:
			return formatEngineer(bigDecimal);
		}
		return formatEngineer(bigDecimal);
	}

	public static String formatEngineer (BigDecimal bigDecimal) {
		if (bigDecimal == null)
			return "BigIntFormatter null";
		if (sb == null) {
			sb = new StringBuilder();
		}
		sb.setLength(0);

		// TODO localize stuff
		//FIXME can we avoid this string?
		String valAsStr = bigDecimal.toString();

		int length = valAsStr.length();

		int rem = length % 3;
		// we want 3 if we get no remainder
		rem = (rem == 0) ? 3 : rem;
		// add significant digits
		for (int i = 0; i < rem; i++) {
			sb.append(valAsStr.charAt(i));
		}
		// we want at most 3 numbers for precision, but we cant go over max offset
		int prec = Math.min(3, length - rem);
		// we want to ignore 0 in precision
		// to achieve that, we ignore 0 starting from the end
		// of relevant part of the value
		int added = 0;
		for (int i = prec - 1; i >= 0; i--) {
			char ch = valAsStr.charAt(rem + i);
			// skip trailing zeroes
			if (ch == '0' && added == 0)
				continue;
			sb.insert(sb.length - added, ch);
			added++;
		}

		// add . if needed
		if (added > 0) {
			sb.insert(sb.length - added, '.');
		}

		// no need for exponent in representation if its 0
		int exp = length - rem;
		if (exp > 0) {
			sb.append("e");
			sb.append(exp);
		}

		return sb.toString();
	}
}
