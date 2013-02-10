package net.sourceforge.retroweaver.tests;

import java.lang.reflect.Constructor;

import junit.framework.TestSuite;
import junit.textui.TestRunner;

public class ConstructorTest extends AbstractTest {

	public void testIsVarArgs() throws Exception {
		try {
			assertFalse("isVarArgs for Integer constructor", Test.class.getDeclaredConstructor(Integer.class).isVarArgs());
			assertTrue("isVarArgs for String constructor", Test.class.getDeclaredConstructor(String[].class).isVarArgs());
	
			assertFalse("isVarArgs for Integer constructor", NamedInnerClass.class.getDeclaredConstructor(Integer.class).isVarArgs());
			assertTrue("isVarArgs for String constructor", NamedInnerClass.class.getDeclaredConstructor(String[].class).isVarArgs());
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	public void testIsSynthetic() throws Exception {
		try {
			assertFalse("isSynthetic for Integer constructor", Test.class.getDeclaredConstructor(Integer.class).isSynthetic());
			assertFalse("isSynthetic for String constructor", Test.class.getDeclaredConstructor(String[].class).isSynthetic());
	
			Constructor[] c = SyntheticConstructor.class.getDeclaredConstructors();
			assertEquals(c.length, 2);
			assertEquals("isSynthetic for SyntheticConstructor", c[0].isSynthetic(), !c[1].isSynthetic());
	
			c = NamedInnerSyntheticConstructor.class.getDeclaredConstructors();
			assertEquals(c.length, 2);
			assertEquals("isSynthetic for SyntheticConstructor", c[0].isSynthetic(), !c[1].isSynthetic());
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	public static void main(String[] args) {
		TestSuite suite = new TestSuite();
		suite.addTestSuite(ConstructorTest.class);
		TestRunner.run(suite);
	}

	public static class NamedInnerClass {
		public NamedInnerClass(Integer i) {}
		
		public NamedInnerClass(String...strings) {}
	}
	static class NamedInnerSyntheticConstructor {
	    private NamedInnerSyntheticConstructor() {}
	    class Inner {
		// Compiler will generate a synthetic constructor since
		// SyntheticConstructor() is private.
		Inner() { new NamedInnerSyntheticConstructor(); }
	    }
	}
}

class Test {
	Test(Integer i) {}
	
	Test(String...strings) {}
}

class SyntheticConstructor {
    private SyntheticConstructor() {}
    class Inner {
	// Compiler will generate a synthetic constructor since
	// SyntheticConstructor() is private.
	Inner() { new SyntheticConstructor(); }
    }
}
