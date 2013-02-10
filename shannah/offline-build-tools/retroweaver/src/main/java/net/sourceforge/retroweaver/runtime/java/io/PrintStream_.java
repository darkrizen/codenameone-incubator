package net.sourceforge.retroweaver.runtime.java.io;

import java.io.PrintStream;
import java.util.Locale;

public class PrintStream_ {

	public static PrintStream append(PrintStream s, char c) {
		s.print(c);
		return s;
	}

	public static PrintStream append(PrintStream s, CharSequence csq) {
		s.print(csq==null?"null":csq.toString());
		return s;
	}

	public static PrintStream append(PrintStream s, CharSequence csq, int start, int end) {
		s.print(csq==null?"null".substring(start, end):csq.subSequence(start, end).toString());
		return s;
	}

	public static PrintStream printf(PrintStream w, String format, Object... args) {
		return format(w, format, args);
	}
	
	public static PrintStream printf(PrintStream w, Locale l,
                    String format,
                    Object... args){
		return format(w, l, format, args);
	}

	public static PrintStream format(PrintStream w, String format,
            Object... args){
		w.print(String.format(format, args));
		return w;
	}

	public static PrintStream format(PrintStream w, Locale l,
            String format,
            Object... args){
		w.print(String.format(l, format, args));
		return w;
	}

}
