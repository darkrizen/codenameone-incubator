package net.sourceforge.retroweaver.runtime.java.lang.reflect;

import static org.objectweb.asm.Opcodes.ACC_BRIDGE;
import static org.objectweb.asm.Opcodes.ACC_SYNTHETIC;
import static org.objectweb.asm.Opcodes.ACC_VARARGS;

import java.lang.reflect.Method;

import net.sourceforge.retroweaver.runtime.java.lang.annotation.AIB;
import net.sourceforge.retroweaver.runtime.java.lang.annotation.Annotation;

/**
 * A mirror of java.lang.reflect.Method.
 * 
 * @author Toby Reyelts Date: Feb 20, 2005 Time: 11:10:46 PM
 */
public class Method_ {

	private Method_() {
		// private constructor
	}

	// Returns the default value for the annotation member represented by this
	// <tt>Method</tt> instance.
	public static Object getDefaultValue(final Method m) {
		final Class c = m.getDeclaringClass();

		if (!c.isAnnotation()) {
			return null;
		}

		return AIB.getAib(c).getDefaultValue(m.getName());
	}

	// Returns this element's annotation for the specified type if such an
	// annotation is present, else null.
	public static <T extends Annotation> T getAnnotation(final Method m, final Class<T> annotationType) {
		final Class c = m.getDeclaringClass();
		return AIB.getAib(c).getMethodAnnotation(m.getName(), m.getParameterTypes(), m.getReturnType(), annotationType);
	}

	// Returns all annotations present on this element.
	public static Annotation[] getAnnotations(final Method m) {
		return getDeclaredAnnotations(m);
	}

	// Returns all annotations that are directly present on this element.
	public static Annotation[] getDeclaredAnnotations(final Method m) {
		final Class c = m.getDeclaringClass();
		return AIB.getAib(c).getMethodAnnotations(m.getName(), m.getParameterTypes(), m.getReturnType());
	}

	// Returns true if an annotation for the specified type is present on this
	// element, else false.
	public static boolean isAnnotationPresent(final Method m, final Class<? extends Annotation> annotationType) {
		return getAnnotation(m, annotationType) != null;
	}

	public static Annotation[][] getParameterAnnotations(final Method m) {
		final Class c = m.getDeclaringClass();
		return AIB.getAib(c).getMethodParameterAnnotations(m.getName(), m.getParameterTypes(), m.getReturnType());
	}

	// Returns true if this method is a bridge method; returns false otherwise.
	public static boolean isBridge(final Method m) {
		final Class c = m.getDeclaringClass();
		return ReflectionDescriptor.getReflectionDescriptor(c).testMethodAccess(m, ACC_BRIDGE);
	}

	// Returns true if this method was declared to take a variable number of arguments; returns false otherwise.
	public static boolean isVarArgs(final Method m) {
		final Class c = m.getDeclaringClass();
		return ReflectionDescriptor.getReflectionDescriptor(c).testMethodAccess(m, ACC_VARARGS);
	}

	// Returns true if this method is a synthetic method; returns false otherwise.
	public static boolean isSynthetic(final Method m) {
		final Class c = m.getDeclaringClass();
		return ReflectionDescriptor.getReflectionDescriptor(c).testMethodAccess(m, ACC_SYNTHETIC);
	}

}
