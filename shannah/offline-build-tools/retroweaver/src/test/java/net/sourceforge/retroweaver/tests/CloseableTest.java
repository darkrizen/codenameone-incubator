package net.sourceforge.retroweaver.tests;

import java.io.*;

/**
 * Demonstrates Retroweaver's support for java.io.Closeable.
 */
public class CloseableTest extends AbstractTest {

	public void testOutputStream() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		baos.close();
	}
	
	public void testNull() throws IOException {
		try {
			Closeable c = null;
			c.close();
			fail("testNull");
		} catch (NullPointerException npe) { // NOPMD by xlv
			success("testNull");
		}
	}

	public void testWeavedCloseable() throws IOException {
		// This test works just fine, because the anonymous class is weaved
		// by Retroweaver.

		Closeable c = new MyCloseable();
		c.close();
	}

	private static class MyCloseable implements Closeable {
		public void close() {}
	}
}
