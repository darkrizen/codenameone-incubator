package net.sourceforge.retroweaver.tests;

import java.util.*;
import java.io.*;

import java.util.Calendar;
import java.util.GregorianCalendar;
import static java.util.Calendar.*;

public class FormatterTest extends AbstractTest {

	public void testJavaDocExpectedUsage() {
       Formatter formatter = new Formatter(Locale.US);
       // Explicit argument indices may be used to re-order output.
       String s = formatter.format("%4$2s %3$2s %2$2s %1$2s", "a", "b", "c", "d").toString();
       // -> " d  c  b  a"
       assertEquals("testJavaDocExpectedUsage", " d  c  b  a", s);

       formatter = new Formatter(Locale.US);
       // Optional locale as the first argument can be used to get
       // locale-specific formatting of numbers.  The precision and width can be
       // given to round and align the value.
       s = formatter.format(Locale.FRANCE, "e = %+10.4f", Math.E).toString();
       // -> "e =    +2,7183"
       assertEquals("testJavaDocExpectedUsage", "e =    +2,7183", s);

       formatter = new Formatter(Locale.US);
       // The '(' numeric flag may be used to format negative numbers with
       // parentheses rather than a minus sign.  Group separators are
       // automatically inserted.
       float balanceDelta = -6217.58f;
       s = formatter.format("Amount gained or lost since last statement: $ %(,.2f",
                        balanceDelta).toString();
       // -> "Amount gained or lost since last statement: $ (6,217.58)"
       assertEquals("testJavaDocExpectedUsage", "Amount gained or lost since last statement: $ (6,217.58)", s);
	}


	public void testJavaDocConvenianceMethods() {
		GregorianCalendar c = new GregorianCalendar(2008, 05, 14, 17, 58, 30);
		
		String s;
		s = String.format("Local time: %tT", c);
        // -> "Local time: 13:34:18"
		assertEquals("testJavaDocConvenianceMethods", "Local time: 17:58:30", s);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream st = new PrintStream(baos);

		String fileName = "food";
		try {
			new FileReader(new File(fileName));
		} catch (IOException e) {
			st.printf("Unable to open file '%1$s': %2$s",
                         fileName, e.getMessage());
		}
        // -> "Unable to open file 'food': No such file or directory"

		assertEquals("testJavaDocConvenianceMethods", "Unable to open file 'food': food (No such file or directory)", baos.toString());
	}
  
	public void testSprintf() {
       // Format a string containing a date.

       Calendar c = new GregorianCalendar(1995, MAY, 23);
       String s = String.format("Duke's Birthday: %1$tm %1$te,%1$tY", c);
       // -> s == "Duke's Birthday: May 23, 1995"
       assertEquals("testSprintf", "Duke's Birthday: 05 23,1995", s);
	}

}
