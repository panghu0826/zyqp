package com.buding.common.conf;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.buding.common.util.ObjectUtil;

public class ValVerifyUtil {
	public static void check(Object obj) throws RuntimeException {
		try {
			List<Field> list = new ArrayList<Field>();
			ObjectUtil.getAllFields(obj.getClass(), list);
			for(Field f : list) {
				ValRequired v = f.getAnnotation(ValRequired.class);
				if(v == null) {
					continue;
				}
				f.setAccessible(true);
				Object ret = f.get(obj);
				if(ret == null) {
					throw new RuntimeException(obj.getClass().getName() + "." + f.getName() + " cannot be null");
				}
				if(ret.getClass().getName().startsWith("com.buding")) {
					check(ret);
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
