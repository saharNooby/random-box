package me.saharnooby.plugins.randombox.nms;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public final class ReflectionUtil {

	/**
	 * Recursivly finds the field named <code>name</code> from class <code>c</code> to <code>Object</code>
	 * @param c Class
	 * @param name Name of the field
	 * @return Field
	 */
	public static Field findField(Class c, String name) throws NoSuchFieldException {
		try {
			return c.getDeclaredField(name);
		} catch (NoSuchFieldException e) {
			if (c != Object.class) {
				return findField(c.getSuperclass(), name);
			}
			throw e;
		}
	}

	/**
	 * Gets value of the field named <code>name</code> of object <code>o</code>
	 * @param o Object
	 * @param name Name of the field
	 * @return Field value
	 */
	public static Object getField(Object o, String name) throws NoSuchFieldException, IllegalAccessException {
		Field field = findField(o.getClass(), name);
		field.setAccessible(true);
		return field.get(o);
	}

	public static void setField(Object o, String name, Object value) throws NoSuchFieldException, IllegalAccessException {
		Field field = findField(o.getClass(), name);
		field.setAccessible(true);

		if ((field.getModifiers() & Modifier.FINAL) != 0) {
			Field modifiersField = Field.class.getDeclaredField("modifiers");
			modifiersField.setAccessible(true);
			modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
		}

		field.set(o, value);
	}

}
