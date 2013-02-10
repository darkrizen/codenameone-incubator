package net.sourceforge.retroweaver.tests;

import java.lang.reflect.*;
import java.util.*;

/*
 * Tests for reflection calls used by jruby 1.1
 */

public class JRubyTest extends AbstractTest {

	public static class DummyMemberChild implements Member {
		public Class<?> getDeclaringClass() { return null; }
	    public String getName() { return null; }
	    public int getModifiers() { return 0; }
	    public boolean isSynthetic() { return false; }
	}

	public static class JRubyCalls {
		public String field1;
		public List<String> field2;
		public JRubyCalls(String s) throws InterruptedException, Exception {}
		public JRubyCalls(List<String> l, String s) {}
		public <T> JRubyCalls(List<T> l, Object o) {}
		public String method1(String s) throws InterruptedException, Exception { return null; }
		public List<String> method2(List<String> l, String s) { return null; }
		public <T> boolean method3(List<T> l, String s) {return false;}
	}

	public static enum Color { red, green, blue; }
	
	public void testIsSyntheticMember() throws Exception {
		Member m;
		
		m = new DummyMemberChild();
		assertFalse("DummyMemberChild", m.isSynthetic());
		
		m = JRubyCalls.class.getDeclaredMethod("method1", String.class);
		assertFalse("Method", m.isSynthetic());
		
		m = JRubyCalls.class.getDeclaredField("field1");
		assertFalse("Field", m.isSynthetic());

		m = JRubyCalls.class.getDeclaredConstructor(String.class);
		assertFalse("Constructor", m.isSynthetic());
	}

	public void testFieldMethods() throws Exception {
		Field f;
		
		f = JRubyCalls.class.getDeclaredField("field1");
		assertFalse("isEnumConstant", f.isEnumConstant());

		f = JRubyCalls.class.getDeclaredField("field2");
		assertFalse("isEnumConstant", f.isEnumConstant());

		f = Color.class.getDeclaredField("red");
		assertTrue("isEnumConstant", f.isEnumConstant());
	}
	
}
