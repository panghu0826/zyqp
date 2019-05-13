package utils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.Properties;

/**
 * Created by kerry on 2016/9/7.
 */
public class Conf {
    private Properties config = new Properties();
    private final String defaultConfPath = "/s.properties";

    public Conf() {
    }

    public void load() throws IOException {
        load(defaultConfPath);
    }

    public void load(String path) throws IOException {
        InputStreamReader r = new InputStreamReader(Conf.class.getResourceAsStream(path), "utf-8");
        config.load(r);
        r.close();
    }

    public void loadFromString(String thisServerConf) throws IOException {
        Properties p = new Properties();
        p.load(new StringReader(thisServerConf));
        this.config.putAll(p);
    }

    public boolean check() {
        if (get("redis.host") == null)
            return false;

        return true;
    }

    public String get(String key) {
        return config.getProperty(key);
    }

    public String get(String key, String defaultVal) {
        String val = get(key);
        return val == null ? defaultVal : val;
    }

    public int getInt(String key, int defaultVal) {
        String val = get(key);
        return val != null ? Integer.parseInt(val) : defaultVal;
    }

    public String toString() {
        return config.toString();
    }

}
