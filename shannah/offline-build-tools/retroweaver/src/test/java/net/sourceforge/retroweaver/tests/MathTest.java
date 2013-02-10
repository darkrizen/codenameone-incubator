package net.sourceforge.retroweaver.tests;

public class MathTest extends AbstractTest {

	public void testLog10() {
		assertEquals("testLog10", -1d, Math.log10(0.1d), 1E-15);
		assertEquals("testLog10", 0d, Math.log10(1d));
		assertEquals("testLog10", 1.0d, Math.log10(10d));
		assertEquals("testLog10", 2.0d, Math.log10(100d));
	}

	public void testTanh() {
		assertTrue("testTanh", Double.isNaN(Math.tanh(Double.NaN)));
		assertEquals("testTanh", -0d, Math.tanh(-0d));
		assertEquals("testTanh", 0d, Math.tanh(0d));
		assertEquals("testTanh", 1d, Math.tanh(Double.POSITIVE_INFINITY));
		assertEquals("testTanh", -1d, Math.tanh(Double.NEGATIVE_INFINITY));
		
		assertEquals("testTanh", 0.7615941559557649, Math.tanh(1), 1E-15);
	}

}
