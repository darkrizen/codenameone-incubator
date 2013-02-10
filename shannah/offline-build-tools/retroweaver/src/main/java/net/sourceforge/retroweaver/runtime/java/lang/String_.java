package net.sourceforge.retroweaver.runtime.java.lang;

import java.util.Locale;
import net.sourceforge.retroweaver.harmony.runtime.java.util.Formatter;

public class String_ {

	private String_() {
		// private constructor
	}

	public static String replace(String s, CharSequence target,
            CharSequence replacement) {
		
        if (target == null) {
            throw new NullPointerException("target should not be null");
        }
        if (replacement == null) {
            throw new NullPointerException("replacement should not be null");
        }
        String ts = target.toString();
        int index = s.indexOf(ts, 0);

        if (index == -1)
            return s;

        String rs = replacement.toString();
        StringBuilder buffer = new StringBuilder(s.length());
        int tl = target.length();
        int tail = 0;
        char[] value = s.toCharArray();
        int offset = 0;
        do {
            buffer.append(value, offset + tail, index - tail);
            buffer.append(rs);
            tail = index + tl;
        } while ((index = s.indexOf(ts, tail)) != -1);
        //append trailing chars 
        buffer.append(value, offset + tail, s.length() - tail);

        return buffer.toString();
	}

	public static String format(String s, Object... params) {
		return new Formatter().format(s, params).toString();
	}

	public static String format(Locale l, String s, Object... params) {
		return new Formatter(l).format(s, params).toString();
	}

	public static boolean contains(String s, CharSequence seq) {
		if (seq == null) {
			throw new NullPointerException();
		}
		return s.indexOf(seq.toString()) != -1;
	}

}
