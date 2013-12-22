package mobi.threeam.npang.common;

import java.lang.reflect.Field;

import android.util.Log;

public class StringifyUtils {

	public static String toString(Object obj) {
		if (obj == null) {
			return "null";
		}
		Class<?> clazz = obj.getClass();
		StringBuilder builder = new StringBuilder();
		builder.append(clazz.getSimpleName() +"(");
		for (Field field : clazz.getDeclaredFields()) {
			try {
				builder.append(field.getName()).append("=").append(field.get(obj)).append(",");
			} catch (IllegalArgumentException e) {
				builder.append("!!!exception!!!");
			} catch (IllegalAccessException e) {
				builder.append("!!!exception!!!");
			}
			
		}
		builder.append(")");
		return builder.toString();
	}

}
