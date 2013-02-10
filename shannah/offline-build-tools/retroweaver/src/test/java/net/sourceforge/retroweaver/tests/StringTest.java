package net.sourceforge.retroweaver.tests;

import java.util.Locale;

public class StringTest extends AbstractTest {

	public void testStringReplace() {
		CharSequence cs1 = "ab";
		CharSequence cs2 = "12";
		
		String in = "abcdef ab";

		String out = in.replace(cs1, cs2);

		assertEquals("testStringReplace", out, "12cdef 12");
	}
	
	public void testStringReplace2() {
		// from bug 1893185
		String fileName = "c:\\myfile.txt";
		String command = "lame -S -h -b %b %s -";

		command = command.replace("%s", '"' + fileName + '"');
		assertEquals("testStringReplace2", "lame -S -h -b %b \"c:\\myfile.txt\" -", command);
	}

	public void testStringFormat() {
		String s = String.format("%% Hello %s %%", "World");
		assertEquals("testStringFormat", "% Hello World %", s);

		s = String.format("Hello %s %d", "World", 2);
		assertEquals("testStringFormat", "Hello World 2", s);

		s = String.format(Locale.FRENCH, "Hello %s %.1f", "World", 2.5f);
		assertEquals("testStringFormat", "Hello World 2,5", s);
	}

}
