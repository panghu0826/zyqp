package com.buding.common.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class DateUtil {
	public static final String PATTERN_DATE = "yyyy-MM-dd";
	public static final String PATTERN_TIME = "HH:mm:ss";
	public static final String PATTERN_DATE_TIME = "yyyy-MM-dd HH:mm:ss";
	
	public static Date addHour(Date date, TimeUnit timeUnit, int time) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		System.out.println(new SimpleDateFormat("yyyy-MM-dd").format(date));
		c.add(Calendar.SECOND, (int)(timeUnit.toSeconds(time)));
		System.out.println(new SimpleDateFormat("yyyy-MM-dd").format(c.getTime()));
		return c.getTime();
	}

	public static int minusHour(Date before, Date after) {
		long diff = after.getTime() - before.getTime();
		return (int)(diff/(1000*60*60));
	}
	
	public static int getYYYYMMdd(Date date) {
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		return Integer.valueOf(format.format(date));
	}
	
	public static long getYYYYMMddHHmmss(Date date) {
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
		return Long.valueOf(format.format(date));
	}
	
	public static int getHHmmss(Date date) {
		SimpleDateFormat format = new SimpleDateFormat("HHmmss");
		return Integer.valueOf(format.format(date));
	}
	
	public static int getHHmm(Date date) {
		SimpleDateFormat format = new SimpleDateFormat("HHmm");
		return Integer.valueOf(format.format(date));
	}
	
	public static String format(Date date, String pattern) {
		SimpleDateFormat format = new SimpleDateFormat(pattern);
		return format.format(date);
	}
	
	public static Date parse(String date, String pattern) throws Exception {
		SimpleDateFormat format = new SimpleDateFormat(pattern);
		return format.parse(date);
	}
	
	public static int minuteDiff(Date d1, Date d2) {
		long mills = d2.getTime() - d1.getTime();
		return (int)(mills/(60*1000));
	}
	
	public static Date getDayEndOf(Date d) throws Exception {
		return parse(format(d, "yyyy-MM-dd")+" 23:59:59", "yyyy-MM-dd HH:mm:ss");
	}
	
	public static Date getDayStartOf(Date d) throws Exception {
		return parse(format(d, "yyyy-MM-dd")+" 00:00:00", "yyyy-MM-dd HH:mm:ss");
	}
}
