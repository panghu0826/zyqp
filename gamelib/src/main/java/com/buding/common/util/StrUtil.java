package com.buding.common.util;

public class StrUtil {
	public static String null2string(String str, String defaultStr) {
		if(str == null) {
			return defaultStr;
		}
		return str.trim();
	}
}
