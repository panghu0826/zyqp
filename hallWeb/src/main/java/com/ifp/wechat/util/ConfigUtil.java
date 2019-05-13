package com.ifp.wechat.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
/**
 * 配置文件工具类
 * @author caspar.chen
 * @version 1.0
 * 
 */
public class ConfigUtil {
	
	private static Properties props = new Properties();

	static {
		try {
			props.load(Thread.currentThread().getContextClassLoader()
					.getResourceAsStream("wechat.properties"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String get(String key) {
		return props.getProperty(key);
	}
	
	public static boolean getBoolean(String key, boolean def) {
		String val = props.getProperty(key);
		return StringUtils.isBlank(val)? def : "true".equals(val.trim());
	}

	public static void setProps(Properties p) {
		props = p;
	}
}
