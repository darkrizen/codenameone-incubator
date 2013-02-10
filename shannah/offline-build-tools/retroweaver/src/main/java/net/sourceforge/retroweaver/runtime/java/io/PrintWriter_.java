package net.sourceforge.retroweaver.runtime.java.io;

import java.io.PrintWriter;
import java.util.Locale;

public class PrintWriter_ {

	public static PrintWriter append(PrintWriter w, char c) {
		w.write(c);
		return w;
	}

	public static PrintWriter append(PrintWriter w, CharSequence csq) {
		w.write(csq==null?"null":csq.toString());
		return w;
	}

	public static PrintWriter append(PrintWriter w, CharSequence csq, int start, int end) {
		w.write(csq==null?"null".substring(start, end):csq.subSequence(start, end).toString());
		return w;
	}

	public static PrintWriter printf(PrintWriter w, String format, Object... args) {
		return format(w, format, args);
	}
	
	public static PrintWriter printf(PrintWriter w, Locale l,
                    String format,
                    Object... args){
		return format(w, l, format, args);
	}

	public static PrintWriter format(PrintWriter w, String format,
            Object... args){
		w.write(String.format(format, args));
		return w;
	}

	public static PrintWriter format(PrintWriter w, Locale l,
            String format,
            Object... args){
		w.write(String.format(l, format, args));
		return w;
	}

}
