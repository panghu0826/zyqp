package com.guosen.webx.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropHelper {

    public static final int SCOPE_CP = 0;
    public static final int SCOPE_ABS = 1;

    private static final String ENCODING = "utf-8";

    private static Logger log = LoggerFactory.getLogger(PropHelper.class);

    public static Properties loadProperties(String name, int scope) {
        Properties prop = new Properties();

        try {
            switch (scope) {
                case SCOPE_CP:
                    InputStreamReader reader = new InputStreamReader(PropHelper.class.getResourceAsStream(name), ENCODING);
                    prop.load(reader);
                    reader.close();
                    break;
                default:
                    File f = new File(name);
                    if (f.exists()) {
                        if (f.isFile()) {
                            InputStreamReader r = new InputStreamReader(new FileInputStream(f), ENCODING);
                            prop.load(r);
                            r.close();
                        } else {
                            File[] fl = f.listFiles();
                            for (int i = 0; i < fl.length; i++) {
                                File sf = fl[i];
                                prop.putAll(loadProperties(sf.getAbsolutePath(), SCOPE_ABS));
                            }
                        }
                    } else {
                        log.error("PropFile not found, path " + f.getAbsolutePath());
                    }
                    break;
            }
        } catch (Exception e) {
            log.error("load properties error", e);
        }
        return prop;
    }

    public static Properties loadProperties(String name) {
        return loadProperties(name, SCOPE_CP);
    }

    // get subset
    public static Properties loadProperties(String name, String pre, int scope) {
        Properties prop = loadProperties(name, scope);
        Properties rprop = new Properties();

        Iterator<?> it = prop.keySet().iterator();
        while (it.hasNext()) {
            String rkey = (String) it.next();
            if (rkey.startsWith(pre)) {
                rprop.put(rkey.substring(pre.length()), prop.get(rkey));
            }
        }
        return rprop;
    }

    public static String getProperty(String propName, String key, String defaultValue, int scope) {
        String v = loadProperties(propName, scope).getProperty(key);
        return v == null ? defaultValue : v;
    }
}
