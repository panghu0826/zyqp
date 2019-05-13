package com.buding.common.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CollectionUtils {
	public static String collection2Str(Collection<? extends Object> items, char splitChar) {
		StringBuilder sb = new StringBuilder();
		if(items != null && items.isEmpty() == false) {
			for(Object item : items) {
				sb.append(item.toString()).append(splitChar);
			}
			sb.deleteCharAt(sb.length() - 1);
		}
		return sb.toString();
	}
	
	public static <T> List<T> subListCopy(List<T> source, int start, int count) {
		if ((source == null) || (source.size() == 0)) {
			return new ArrayList<T>(0);
		}

		int fromIndex = (start <= 0) ? 0 : start;
		if (start > source.size()) {
			fromIndex = source.size();
		}

		count = (count <= 0) ? 0 : count;
		int endIndex = fromIndex + count;
		if (endIndex > source.size()) {
			endIndex = source.size();
		}
		return new ArrayList<T>(source.subList(fromIndex, endIndex));
	}
	
	public static <T> List<T> asList(T ...items) {
		List<T> list = new ArrayList<T>();
		if(items != null) {
			for(T item : items) {
				list.add(item);
			}
		}
		return list;
	}
}
