package com.guosen.webx.d;

import groovy.sql.GroovyRowResult;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author kerry
 */
public class NamingStyleUtils {
    public static String toCamel(String str, boolean isFirstLowerCase) {
        if (str == null) {
            return str;
        }
        if (str.length() <= 1) {
            return isFirstLowerCase ? str.toLowerCase() : str;
        }

        StringBuilder sb = new StringBuilder();
        String[] arr = str.toLowerCase().split("_");
        int size = arr.length;
        for (int i = 0; i < size; i++) {
            String cc = arr[i];
            int len = cc.length();
            if (len == 0)
                continue;

            if (i == 0 && isFirstLowerCase) {
                sb.append(cc);
            } else {
                sb.append(cc.substring(0, 1).toUpperCase());
                if (len > 1)
                    sb.append(cc.substring(1));
            }
        }
        return sb.toString();
    }

    public static String toUnderline(String str) {
        if (str == null) {
            return str;
        }
        if (str.length() <= 1) {
            return str.toLowerCase();
        }

        StringBuilder sb = new StringBuilder();
        str = str.substring(0, 1).toLowerCase() + str.substring(1);
        int size = str.length();
        for (int i = 0; i < size; i++) {
            char cc = str.charAt(i);
            if (Character.isUpperCase(cc)) {
                sb.append("_");
                sb.append(Character.toLowerCase(cc));
            } else {
                sb.append(cc);
            }
        }
        return sb.toString();
    }

    // groovy Sql query result object transfer -> camel style
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static Map<String, Object> transform(GroovyRowResult src) {
        Map<String, Object> r = new HashMap<String, Object>();
        for (Iterator<Entry> it = src.entrySet().iterator(); it.hasNext(); ) {
            Entry entry = (Entry) it.next();
            r.put(toCamel((String) entry.getKey(), true), entry.getValue());
        }
        return r;
    }
}
