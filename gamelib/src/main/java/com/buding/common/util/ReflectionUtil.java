package com.buding.common.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;


public class ReflectionUtil {
	public static void overrideDefaultField(Object src, Object to, String field, Object nullVal) throws Exception {
		Field fromField = src.getClass().getDeclaredField(field);
		Field toField = to.getClass().getDeclaredField(field);
		fromField.setAccessible(true);
		toField.setAccessible(true);
		Object fromVal = fromField.get(src);
		Object toVal = toField.get(to);
		if((nullVal == null && toVal == null) || (nullVal != null && nullVal.equals(toVal))) {
			toField.set(to, fromVal);
		}
	}
	
	public static void copyInstanceVar2StaticVar(Object obj) throws Exception {
		Class cls = obj.getClass();
		Field fields[] = cls.getDeclaredFields();
		for(Field field : fields) {
			if(Modifier.isStatic(field.getModifiers())) {
				String iName = "_"+field.getName();
				try {
					Field f = cls.getDeclaredField(iName);
					if(f != null) {
						f.setAccessible(true);
						field.setAccessible(true);
						field.set(null, f.get(obj));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}
