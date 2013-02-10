package net.sourceforge.retroweaver.tests;

import junit.framework.TestSuite;
import junit.textui.TestRunner;

public class FieldTest extends AbstractTest {
	
	private class Test {
		int i;
		String s;
	}
	int i;
	String s;

	public void testIsSynthetic() throws Exception {
		try {
			assertFalse("isSynthetic for int", FieldTest.class.getDeclaredField("i").isSynthetic());
			assertFalse("isSynthetic for s", FieldTest.class.getDeclaredField("s").isSynthetic());
	
			assertFalse("isSynthetic for Test.i", Test.class.getDeclaredField("i").isSynthetic());
			assertFalse("isSynthetic for Test.s", Test.class.getDeclaredField("s").isSynthetic());
	
			assertTrue("isSynthetic for Test.this$0", Test.class.getDeclaredField("this$0").isSynthetic());
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	public static void main(String[] args) {
		TestSuite suite = new TestSuite();
		suite.addTestSuite(FieldTest.class);
		TestRunner.run(suite);
	}

}
