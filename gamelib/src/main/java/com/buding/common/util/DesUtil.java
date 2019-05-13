package com.buding.common.util;

import java.security.MessageDigest;

public class DesUtil {
	private final static char[] hexDigits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

	public static String md5(String input, int bit) {
		try {
			MessageDigest md = MessageDigest.getInstance(System.getProperty("MD5.algorithm", "MD5"));
			if (bit == 16)
				return bytesToHex(md.digest(input.getBytes("utf-8"))).substring(8, 24);
			return bytesToHex(md.digest(input.getBytes("utf-8")));
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Could not found MD5 algorithm.", e);
		}
	}
	
	public static String md5(byte[] input, int bit) {
		try {
			MessageDigest md = MessageDigest.getInstance(System.getProperty("MD5.algorithm", "MD5"));
			if (bit == 16)
				return bytesToHex(md.digest(input)).substring(8, 24);
			return bytesToHex(md.digest(input));
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Could not found MD5 algorithm.", e);
		}
	}

	private static String bytesToHex(byte[] bytes) {
		StringBuffer sb = new StringBuffer();
		int t;
		for (int i = 0; i < 16; i++) {
			t = bytes[i];
			if (t < 0) {
				t += 256;
			}
			sb.append(hexDigits[(t >>> 4)]);
			sb.append(hexDigits[(t % 16)]);
		}
		return sb.toString();
	}
}
