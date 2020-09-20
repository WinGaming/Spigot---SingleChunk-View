package eu.spiforge.reddit;

import java.lang.reflect.Field;

public class Utils {
	
	public static Object getValue(Class<?> clazz, String fieldName, Object object) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Object result;
		
		Field field = clazz.getDeclaredField(fieldName);
		field.setAccessible(true);
		result = field.get(object);
		field.setAccessible(false);
		
		return result;
	}
	
	public static void setValue(Object object, String fieldName, Object value) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Field field = object.getClass().getDeclaredField(fieldName);
		field.setAccessible(true);
		field.set(object, value);
		field.setAccessible(false);
	}
}