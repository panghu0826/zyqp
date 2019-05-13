package com.buding.common.util;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.BeanUtils;

import com.google.gson.GsonBuilder;

public class ObjectUtil {

	public static void deepToString(Object obj, List vals, List<Object> allValus) throws Exception {
		// 是null
		if (obj == null) {
			vals.add(null);
			return;
		}

		Class<?> cls = obj.getClass();

		// 是基本类型
		if (isBasicType(cls)) {
			vals.add(obj);
			return;
		}

		for (Object old : allValus) {
			if (obj == old) {
				vals.add("[RECUSIVE OBJECT]");
				return;
			}
		}

		allValus.add(obj);

		// 是List,Set
		if (Collection.class.isAssignableFrom(cls)) {
			Collection list = (Collection) obj;
			List<Object> retList = new ArrayList<Object>();
			vals.add(retList);
			for (Object ele : list) {
				deepToString(ele, retList, allValus);
			}
		}
		
		//是数组
		if(Array.class.isAssignableFrom(cls)) {
			List retList = new ArrayList();
			vals.add(retList);
			int len = Array.getLength(obj);
			for(int i = 0; i < len; i++) {
				Object subObj = Array.get(obj, i);
				deepToString(subObj, retList, allValus);
			}
			return;
		}

		// 是Map
		if (Map.class.isAssignableFrom(cls)) {
			Map map = (Map) obj;
			Map<Object, Object> retMap = new HashMap();
			vals.add(retMap);
			for (Object key : map.keySet()) {
				deepToString(key, map.get(key), retMap, allValus);
			}
		}

		// 是其它对象
		Map retMap = new HashMap();
		vals.add(retMap);
		List<Field> fields = new ArrayList<Field>();
		getAllFields(cls, fields);
		for (Field field : fields) {
			if (Modifier.isTransient(field.getModifiers())) {
				retMap.put(field.getName(), "[TRANSIENT FIELD]");
				continue;
			}
			if (Modifier.isStatic(field.getModifiers())) {
				retMap.put(field.getName(), "[STATIC FIELD]");
				continue;
			}

			field.setAccessible(true);
			Object subObj = field.get(obj);
			deepToString(field.getName(), subObj, retMap, allValus);
		}
	}

	public static void deepToString(Object key, Object obj, Map<Object, Object> vals, List<Object> allValus) throws Exception {
		// 是null
		if (obj == null) {
			vals.put(key, null);
			return;
		}

		for (Object old : allValus) {
			if (obj == old) {
				vals.put(key, "[RECUSIVE OBJECT]");
				return;
			}
		}

		Class<?> cls = obj.getClass();

		// 是基本类型
		if (isBasicType(cls)) {
			vals.put(key, obj);
			return;
		}

		for (Object old : allValus) {
			if (obj == old) {
				vals.put(key, "[RECUSIVE OBJECT]");
				return;
			}
		}

		allValus.add(obj);

		// 是List,Set
		if (Collection.class.isAssignableFrom(cls)) {
			Collection list = (Collection) obj;

			List<Object> retList = new ArrayList<Object>();
			vals.put(key, retList);
			for (Object ele : list) {
				deepToString(ele, retList, allValus);
			}
			return;
		}

		// 是Array
		if(Array.class.isAssignableFrom(cls)) {
			List retList = new ArrayList();
			vals.put(key, retList);
			int len = Array.getLength(obj);
			for(int i = 0; i < len; i++) {
				Object subObj = Array.get(obj, i);
				deepToString(subObj, retList, allValus);
			}
			return;
		}

		// 是Map
		if (Map.class.isAssignableFrom(cls)) {
			Map map = (Map) obj;
			Map<Object, Object> retMap = new HashMap();
			vals.put(key, retMap);
			for (Object kk : map.keySet()) {
				deepToString(kk, map.get(kk), retMap, allValus);
			}
			return;
		}

		// 是其它对象
		Map retMap = new HashMap();
		vals.put(key, retMap);
		List<Field> fields = new ArrayList<Field>();
		getAllFields(cls, fields);
		for (Field field : fields) {
			if (Modifier.isTransient(field.getModifiers())) {
				retMap.put(field.getName(), "[TRANSIENT FIELD]");
				continue;
			}
			if (Modifier.isStatic(field.getModifiers())) {
				retMap.put(field.getName(), "[STATIC FIELD]");
				continue;
			}

			field.setAccessible(true);
			Object subObj = field.get(obj);
			deepToString(field.getName(), subObj, retMap, allValus);
		}
	}

	public static boolean isBasicType(Class cls) {
		if (cls.isPrimitive()) {
			return true;
		}
		if (cls.getName().startsWith("java.")) {
			if (List.class.isAssignableFrom(cls)) {
				return false;
			}
			if (Map.class.isAssignableFrom(cls)) {
				return false;
			}
			if (Set.class.isAssignableFrom(cls)) {
				return false;
			}
			return true;
		}

		return false;
	}

	public static Object deepToString(Object obj) throws Exception {
		//是null
		if (obj == null) {
			return null;
		}
		Class cls = obj.getClass();
		
		//是基本类型
		if (isBasicType(cls)) {
			return obj;
		}

		List allVals = new ArrayList();

		//是List, Set
		if (Collection.class.isAssignableFrom(cls)) {
			List retList = new ArrayList();
			Collection list = (Collection) obj;

			for (Object ele : list) {
				deepToString(ele, retList, allVals);
			}
			return retList;
		}

		// 是Map
		if (Map.class.isAssignableFrom(cls)) {
			Map map = (Map) obj;
			Map<Object, Object> retMap = new HashMap();
			for (Object kk : map.keySet()) {
				deepToString(kk, map.get(kk), retMap, allVals);
			}
			return retMap;
		}
		
		//是数组
		if(Array.class.isAssignableFrom(cls)) {
			List retList = new ArrayList();
			int len = Array.getLength(obj);
			for(int i = 0; i < len; i++) {
				Object subObj = Array.get(obj, i);
				deepToString(subObj, retList, allVals);
			}
			return retList;
		}
		
		// 是其它对象
		Map retMap = new HashMap();
		List<Field> fields = new ArrayList<Field>();
		getAllFields(cls, fields);
		for (Field field : fields) {
			if (Modifier.isTransient(field.getModifiers())) {
				retMap.put(field.getName(), "[TRANSIENT FIELD]");
				continue;
			}
			if (Modifier.isStatic(field.getModifiers())) {
				retMap.put(field.getName(), "[STATIC FIELD]");
				continue;
			}

			field.setAccessible(true);
			Object subObj = field.get(obj);
			deepToString(field.getName(), subObj, retMap, allVals);
		}
		return retMap;
	}
	
	public static void getAllFields(Class cls, List<Field> list) throws Exception {
		if(cls == null) return;
		for(Field field : cls.getDeclaredFields()) {
			list.add(field);
		}
		getAllFields(cls.getSuperclass(), list);
	}
	
	public static void getAllMethods(Class cls, List<Method> list) throws Exception {
		if(cls == null) return;
		for(Method method : cls.getDeclaredMethods()) {
			list.add(method);
		}
		getAllMethods(cls.getSuperclass(), list);
	}
	
	public static Field getFieldByName(Class cls, String fieldName) throws Exception {
		if(cls == null) return null;
		for(Field field : cls.getDeclaredFields()) {
			if(field.getName().equals(fieldName)) {
				return field;
			}
		}
		return getFieldByName(cls.getSuperclass(), fieldName);
	}
	
	public static Method getMethodByName(Class cls, String methodName) throws Exception {
		if(cls == null) return null;
		for(Method m: cls.getDeclaredMethods()) {
			if(m.getName().equals(methodName)) {
				return m;
			}
		}
		return getMethodByName(cls.getSuperclass(), methodName);
	}

	public static void main(String[] args) throws Exception {
		 A a = new A();
		 a.a2 = a;
		 Object obj = deepToString(a);
		System.out.println(new GsonBuilder().setPrettyPrinting().serializeNulls().create().toJson(obj));
	}
	
	static class A {
		int a1 = 1;
		A a2;
		int a3[] = new int[]{1,2,3};
	}
}
