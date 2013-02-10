package net.sourceforge.retroweaver.runtime.java.lang.reflect;

import java.lang.reflect.*;

public class Member_ {

	public static boolean isSynthetic(Member m) {
		if (m instanceof Method) {
			return Method_.isSynthetic((Method) m);
		} else if (m instanceof Field) {
			return Field_.isSynthetic((Field) m);
		} else if (m instanceof Constructor) {
			return Constructor_.isSynthetic((Constructor) m);
		} else {
			try {
				// not part of jdk, use reflection
				Method method = m.getClass().getMethod("isSynthetic");
				return ((Boolean) method.invoke(m)).booleanValue();
			} catch (RuntimeException e) {
				throw e;
			} catch (Exception orig) {
				NoSuchMethodError e =  new NoSuchMethodError(orig.getMessage());
				e.initCause(orig);
				throw e;
			}
		}
	}

}