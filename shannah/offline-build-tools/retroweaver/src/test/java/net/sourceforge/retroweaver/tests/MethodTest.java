package net.sourceforge.retroweaver.tests;

import junit.framework.TestSuite;
import junit.textui.TestRunner;

public class MethodTest extends AbstractTest {
	
	public void t1(Integer i) {}
		
	public void t2(String...strings) {}

	public void testIsVarArgs() throws Exception {
		try {
			assertFalse("isVarArgs for Integer method", MethodTest.class.getMethod("t1", Integer.class).isVarArgs());
			assertTrue("isVarArgs for String method", MethodTest.class.getMethod("t2", String[].class).isVarArgs());
	
			assertFalse("isVarArgs for Integer method", NamedInnerClass.class.getMethod("t1", Integer.class).isVarArgs());
			assertTrue("isVarArgs for String method", NamedInnerClass.class.getMethod("t2", String[].class).isVarArgs());
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	public void testIsBridge() throws Exception {
		try {
			assertFalse("isBridge for Integer method", MethodTest.class.getMethod("t1", Integer.class).isBridge());
			assertFalse("isBridge for String method", MethodTest.class.getMethod("t2", String[].class).isBridge());
			assertFalse("isBridge for compareTo(String) method", MyComparator.class.getMethod("compareTo", String.class).isBridge());
			assertTrue("isBridge for compareTo(Object) method", MyComparator.class.getMethod("compareTo", Object.class).isBridge());
	
			assertFalse("isBridge for Integer method", NamedInnerClass.class.getMethod("t1", Integer.class).isBridge());
			assertFalse("isBridge for String method", NamedInnerClass.class.getMethod("t2", String[].class).isBridge());
			assertFalse("isBridge for compareTo(String) method", NamedInnerClass.class.getMethod("compareTo", String.class).isBridge());
			assertTrue("isBridge for compareTo(Object) method", NamedInnerClass.class.getMethod("compareTo", Object.class).isBridge());
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	public void testIsSynthetic() throws Exception {
		try {
			assertFalse("isSynthetic for compareTo(String) method", MyComparator.class.getMethod("compareTo", String.class).isSynthetic());
			assertTrue("isSynthetic for compareTo(Object) method", MyComparator.class.getMethod("compareTo", Object.class).isSynthetic());
	
			assertFalse("isSynthetic for compareTo(String) method", NamedInnerClass.class.getMethod("compareTo", String.class).isSynthetic());
			assertTrue("isSynthetic for compareTo(Object) method", NamedInnerClass.class.getMethod("compareTo", Object.class).isSynthetic());
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	public static void main(String[] args) {
		TestSuite suite = new TestSuite();
		suite.addTestSuite(MethodTest.class);
		TestRunner.run(suite);
	}

	public static class NamedInnerClass implements Comparable<String> {
		public int compareTo(String c) {
			return 1;
		}
		public void t1(Integer i) {}
		
		public void t2(String...strings) {}
	}

}

class MyComparator implements Comparable<String> {
	public int compareTo(String c) {
		return 1;
	}
}